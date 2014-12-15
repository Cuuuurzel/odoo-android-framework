package com.odoo.demo.controls.com;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.odoo.demo.controls.com.CustomControl.FieldType;
import com.odoo.demo.controls.com.CustomControl.WidgetType;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.ColumnDomain;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OM2ORecord;
import com.odoo.orm.OModel;
import com.odoo.util.CursorUtil;
import com.odoo.util.OControls;

public class OSelectionField extends LinearLayout implements OControlData,
		OnItemSelectedListener, OnItemClickListener, OnCheckedChangeListener {

	private Context mContext;
	private Object mValue = null;
	private Boolean mEditable = false;
	private CustomControl.WidgetType mWidget = null;
	private Integer mResourceArray = null;
	private FieldType mType;
	private OColumn mCol;
	private String mLabel;
	private OModel mModel;
	private List<ODataRow> items = new ArrayList<ODataRow>();
	private ValueUpdateListener mValueUpdateListener = null;
	// Controls
	private Spinner mSpinner = null;
	private SpinnerAdapter mAdapter;
	private RadioGroup mRadioGroup = null;
	private TextView txvView = null;

	public OSelectionField(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public OSelectionField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public OSelectionField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public OSelectionField(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {

		}
		if (mContext.getClass().getSimpleName().contains("BridgeContext"))
			initControl();
	}

	@Override
	public void initControl() {
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		removeAllViews();
		setOrientation(VERTICAL);
		createItems();
		if (isEditable()) {
			if (mWidget != null) {
				switch (mWidget) {
				case RadioGroup:
					mRadioGroup = new RadioGroup(mContext);
					mRadioGroup.setLayoutParams(params);
					mRadioGroup.removeAllViews();
					mRadioGroup.setOnCheckedChangeListener(this);
					for (ODataRow label : items) {
						RadioButton rdoBtn = new RadioButton(mContext);
						rdoBtn.setLayoutParams(params);
						rdoBtn.setText(label.getString("name"));
						mRadioGroup.addView(rdoBtn);
					}
					addView(mRadioGroup);
					return;
				case SelectionDialog:
					txvView = new TextView(mContext);
					txvView.setLayoutParams(params);
					mAdapter = new SpinnerAdapter(mContext,
							android.R.layout.simple_list_item_1, items);
					setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							AlertDialog dialog = createSelectionDialog(
									getPos(), items, params);
							txvView.setTag(dialog);
							dialog.show();
						}
					});
					addView(txvView);
					return;
				case Searchable:
				case SearchableLive:
					txvView = new TextView(mContext);
					txvView.setLayoutParams(params);
					setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext,
									SearchableItemActivity.class);
							intent.putExtra("resource_id", mResourceArray);
							intent.putExtra("selected_position", getPos());
							intent.putExtra(OColumn.ROW_ID, getPos());
							intent.putExtra("search_hint", getLabel());
							if (mCol != null)
								intent.putExtra("column_name", mCol.getName());
							intent.putExtra("model", mModel.getModelName());
							intent.putExtra("live_search",
									(mWidget == WidgetType.SearchableLive));
							try {
								mContext.unregisterReceiver(valueReceiver);
							} catch (Exception e) {

							}
							mContext.registerReceiver(valueReceiver,
									new IntentFilter("searchable_value_select"));
							mContext.startActivity(intent);
						}
					});
					addView(txvView);
					return;
				default:
					break;
				}
			}

			// Default View
			mSpinner = new Spinner(mContext);
			mSpinner.setLayoutParams(params);
			mAdapter = new SpinnerAdapter(mContext,
					android.R.layout.simple_list_item_1, items);
			mSpinner.setAdapter(mAdapter);
			mSpinner.setOnItemSelectedListener(this);
			addView(mSpinner);
		} else {
			setOnClickListener(null);
			txvView = new TextView(mContext);
			addView(txvView);
		}
	}

	private void createItems() {
		items.clear();
		if (!mContext.getClass().getSimpleName().contains("BridgeContext")) {
			if (mResourceArray != null && mResourceArray != -1) {
				String[] items_list = mContext.getResources().getStringArray(
						mResourceArray);
				for (int i = 0; i < items_list.length; i++) {
					ODataRow row = new ODataRow();
					row.put(OColumn.ROW_ID, i);
					row.put("name", items_list[i]);
					items.add(row);
				}
			} else {
				items.addAll(getRecordItems(mModel, mCol));
			}
		}
	}

	private int getPos() {
		if (mResourceArray != -1 && mValue != null) {
			return Integer.parseInt(mValue.toString());
		} else {
			ODataRow rec = getValueForM2O();
			if (rec != null) {
				return rec.getInt(OColumn.ROW_ID);
			}
		}
		return -1;
	}

	@Override
	public void setValue(Object value) {
		mValue = value;
		if (mValue == null)
			return;
		if (isEditable()) {
			if (mWidget != null) {
				switch (mWidget) {
				case RadioGroup:
					if (mResourceArray != -1) {
						((RadioButton) mRadioGroup.getChildAt(getPos()))
								.setChecked(true);
					} else {
						Integer row_id = null;
						if (value instanceof OM2ORecord)
							row_id = ((OM2ORecord) value).getId();
						else
							row_id = (Integer) value;
						int index = -1;
						for (int i = 0; i < items.size(); i++) {
							if (items.get(i).getInt(OColumn.ROW_ID) == row_id) {
								index = i;
								break;
							}
						}
						((RadioButton) mRadioGroup.getChildAt(index))
								.setChecked(true);
					}
					break;
				case Searchable:
				case SearchableLive:
				case SelectionDialog:
					ODataRow row = null;
					if (mResourceArray != -1) {
						row = items.get(getPos());
					} else {
						if (value instanceof OM2ORecord)
							row = ((OM2ORecord) value).browse();
						else
							row = getRecordData((Integer) value);
					}
					txvView.setText(row.getString("name"));
					if (txvView.getTag() != null) {
						AlertDialog dialog = (AlertDialog) txvView.getTag();
						dialog.dismiss();
					}
					break;
				}
			} else {
				if (mResourceArray != -1) {
					mSpinner.setSelection(getPos());
				} else {
					Integer row_id = null;
					if (value instanceof OM2ORecord)
						row_id = ((OM2ORecord) value).getId();
					else
						row_id = (Integer) value;
					int index = -1;
					for (int i = 0; i < items.size(); i++) {
						if (items.get(i).getInt(OColumn.ROW_ID) == row_id) {
							index = i;
							break;
						}
					}
					mSpinner.setSelection(index);
				}
			}
		} else {
			ODataRow row = null;
			if (mResourceArray != -1) {
				row = items.get(getPos());
			} else {
				if (value instanceof OM2ORecord) {
					row = ((OM2ORecord) value).browse();
				} else {
					int row_id = (Integer) value;
					row = getRecordData(row_id);
				}
			}
			txvView.setText(row.getString("name"));
		}
		if (mValueUpdateListener != null) {
			mValueUpdateListener.onValueUpdate(value);
		}
	}

	private ODataRow getValueForM2O() {
		if (getValue() != null) {
			if (getValue() instanceof OM2ORecord)
				return ((OM2ORecord) getValue()).browse();
			else
				return getRecordData((Integer) getValue());
		}
		return null;
	}

	@Override
	public Object getValue() {
		if (mValue instanceof OM2ORecord) {
			return ((OM2ORecord) mValue).getId();
		}
		return mValue;
	}

	@Override
	public void setEditable(Boolean editable) {
		mEditable = editable;
		initControl();
	}

	@Override
	public Boolean isEditable() {
		return mEditable;
	}

	public void setWidgetType(CustomControl.WidgetType type) {
		mWidget = type;
		initControl();
	}

	public void setArrayResourceId(int res_id) {
		mResourceArray = res_id;
	}

	public void setFieldType(FieldType type) {
		mType = type;
	}

	public void setColumn(OColumn col) {
		mCol = col;
		if (mCol != null && mLabel == null) {
			mLabel = mCol.getLabel();
		}
	}

	private ODataRow getRecordData(int row_id) {
		ODataRow row = new ODataRow();
		OModel rel_model = mModel.createInstance(mCol.getType());
		row = rel_model.select(row_id);
		return row;
	}

	private class SpinnerAdapter extends ArrayAdapter<ODataRow> {

		public SpinnerAdapter(Context context, int resource,
				List<ODataRow> objects) {
			super(context, resource, objects);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return generateView(position, convertView, parent);
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return generateView(position, convertView, parent);
		}

		private View generateView(int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = LayoutInflater.from(mContext).inflate(
						android.R.layout.simple_list_item_1, parent, false);
			ODataRow row = getItem(position);
			OControls.setText(v, android.R.id.text1, row.getString("name"));
			return v;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mValue = items.get(position).get(OColumn.ROW_ID);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mValue = null;
	}

	@Override
	public void setLabelText(String label) {
		mLabel = label;
	}

	@Override
	public String getLabel() {
		if (mLabel != null)
			return mLabel;
		if (mCol != null)
			return mCol.getLabel();
		return "unknown";
	}

	private AlertDialog createSelectionDialog(final int selected_position,
			final List<ODataRow> items, LayoutParams params) {
		final AlertDialog.Builder builder = new Builder(mContext);
		ListView dialogView = new ListView(mContext);
		dialogView.setAdapter(mAdapter);
		dialogView.setOnItemClickListener(this);
		dialogView.setLayoutParams(params);
		builder.setView(dialogView);
		return builder.create();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		setValue(position);
	}

	BroadcastReceiver valueReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setValue(intent.getIntExtra("selected_position", -1));
			mContext.unregisterReceiver(valueReceiver);
		}
	};

	public void setModel(OModel model) {
		mModel = model;
	}

	public static List<ODataRow> getRecordItems(OModel model, OColumn column) {
		List<ODataRow> items = new ArrayList<ODataRow>();

		OModel rel_model = model.createInstance(column.getType());
		StringBuffer whr = new StringBuffer();
		List<Object> args_list = new ArrayList<Object>();
		for (String key : column.getDomains().keySet()) {
			ColumnDomain domain = column.getDomains().get(key);
			if (domain.getConditionalOperator() != null) {
				whr.append(domain.getConditionalOperator());
			} else {
				whr.append(" ");
				whr.append(domain.getColumn());
				whr.append(" ");
				whr.append(domain.getOperator());
				whr.append(" ? ");
				args_list.add(domain.getValue().toString());
			}
		}
		String where = null;
		String[] args = null;
		if (args_list.size() > 0) {
			where = whr.toString();
			args = args_list.toArray(new String[args_list.size()]);
		}
		Cursor cr = rel_model.resolver().query(new String[] { "name" }, where,
				args, "name");
		if (cr.moveToFirst()) {
			do {
				items.add(CursorUtil.toDatarow(cr));
			} while (cr.moveToNext());
		}
		return items;
	}

	@Override
	public void setValueUpdateListener(ValueUpdateListener listener) {
		mValueUpdateListener = listener;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int index = mRadioGroup.indexOfChild(group.findViewById(checkedId));
		ODataRow row = items.get(index);
		setValue(row.getInt(OColumn.ROW_ID));
	}
}
