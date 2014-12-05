package com.odoo.demo.controls.com;

import odoo.controls.OControlHelper;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OHtml;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OTimestamp;
import com.odoo.orm.types.OVarchar;
import com.odoo.util.OControls;

public class CustomControl extends LinearLayout implements OnItemClickListener {

	private Context mContext = null;
	private FieldType mType = FieldType.Text;
	private OColumn mColumn = null;
	private String mHint, mLabel, mField_name;
	private Object mValue = null;
	private EditText mEditText = null;
	private RadioGroup mradioGrp = null;
	private boolean mEditable = true, showIcon = true, show_label = true;
	private TextView label_view = null, mTextView = null;
	private int resId, tint_color = Color.BLACK;
	private ImageView img_icon = null;
	private ViewGroup container = null;
	private CheckBox mCheckBox = null;
	private Boolean with_bottom_padding = true, with_top_padding = true;
	private WidgetType mWidgetType = null;
	private View mControl = null;
	private Integer mValueArrayId = null;

	public enum WidgetType {
		Switch, RadioGroup, SelectionDialog, Searchable;

		public static WidgetType getWidgetType(int widget) {
			switch (widget) {
			case 0:
				return WidgetType.Switch;
			case 1:
				return WidgetType.RadioGroup;
			case 2:
				return WidgetType.SelectionDialog;
			case 3:
				return WidgetType.Searchable;
			}
			return null;
		}
	}

	public enum FieldType {
		Text, Boolean, ManyToOne, Chips, Selection;

		public static FieldType getTypeValue(int type_val) {
			switch (type_val) {
			case 0:
				return FieldType.Text;
			case 1:
				return FieldType.Boolean;
			case 2:
				return FieldType.ManyToOne;
			case 3:
				return FieldType.Chips;
			case 4:
				return FieldType.Selection;
			}
			return FieldType.Text;
		}
	}

	public CustomControl(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public CustomControl(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	@SuppressLint("NewApi")
	public CustomControl(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public CustomControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {
			TypedArray types = mContext.obtainStyledAttributes(attrs,
					R.styleable.customcontrol);
			mField_name = types
					.getString(R.styleable.customcontrol_ofield_name);
			mHint = mField_name;
			resId = types.getResourceId(
					R.styleable.customcontrol_icon_resource, 0);
			showIcon = types.getBoolean(R.styleable.customcontrol_show_icon,
					true);
			tint_color = types.getColor(R.styleable.customcontrol_icon_tint, 0);
			show_label = types.getBoolean(R.styleable.customcontrol_show_label,
					true);
			int type_value = types.getInt(R.styleable.customcontrol_fieldType,
					0);
			mType = FieldType.getTypeValue(type_value);

			with_bottom_padding = types.getBoolean(
					R.styleable.customcontrol_with_bottom_padding, true);
			with_top_padding = types.getBoolean(
					R.styleable.customcontrol_with_top_padding, true);
			mLabel = types.getString(R.styleable.customcontrol_control_label);
			mValue = types.getString(R.styleable.customcontrol_default_value);
			mValueArrayId = types.getResourceId(
					R.styleable.customcontrol_value_array, 0);
			mWidgetType = WidgetType.getWidgetType(types.getInt(
					R.styleable.customcontrol_customWidget, -1));
			types.recycle();
		}
		initcontrol();
	}

	private void initLayout() {
		removeAllViews();
		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.custom_control_template, this, false);
		int top_padd = layout.getPaddingTop();
		int right_padd = layout.getPaddingRight();
		int bottom_padd = layout.getPaddingBottom();
		int left_padd = layout.getPaddingLeft();
		if (!with_bottom_padding) {
			layout.setPadding(left_padd, top_padd, right_padd, 0);
		}
		if (!with_top_padding) {
			layout.setPadding(left_padd, 0, right_padd, bottom_padd);
		}
		addView(layout);
		container = (ViewGroup) findViewById(R.id.control_container);
		img_icon = (ImageView) findViewById(android.R.id.icon);
		img_icon.setColorFilter(tint_color);
		setImageIcon();
	}

	private void createTextView() {
		mTextView = new TextView(mContext);
		mTextView.setTextColor(Color.BLACK);
		if (!show_label)
			mTextView.setPadding(0, 8, 0, 0);
	}

	public void initcontrol() {
		if (!getEditable())
			createTextView();
		initLayout();
		View controlView = null;
		if (show_label) {
			label_view = getLabelView();
			container.addView(label_view);
		}
		switch (mType) {
		case Text:
			controlView = initTextControl();
			break;
		case Boolean:
			controlView = initBooleanControl();
			break;
		case Chips:
			break;
		case ManyToOne:

			break;
		case Selection:
			controlView = initRadioGroup();
			break;
		default:
			break;
		}
		mControl = controlView;
		container.addView(controlView);
	}

	private void setImageIcon() {
		if (showIcon) {
			if (resId != 0)
				img_icon.setImageResource(resId);
			if (tint_color != 0)
				img_icon.setColorFilter(tint_color);
		} else
			img_icon.setVisibility(View.GONE);
	}

	public <T> void setColumn(OColumn column) {
		mType = getType(column.getType());
		mColumn = column;
		mLabel = column.getLabel();
		setLabelText(mColumn.getLabel());
	}

	private <T> FieldType getType(Class<T> type_class) {
		try {
			// Varchar
			if (type_class.isAssignableFrom(OVarchar.class)
					|| type_class.isAssignableFrom(OInteger.class)
					|| type_class.isAssignableFrom(OReal.class)) {
				return FieldType.Text;
			}
			// boolean
			if (type_class.isAssignableFrom(OBoolean.class)) {
				return FieldType.Boolean;
			}

			// Blob
			if (type_class.isAssignableFrom(OBlob.class)) {
				return null;
			}
			// DateTime
			if (type_class.isAssignableFrom(ODateTime.class)
					|| type_class.isAssignableFrom(OTimestamp.class)) {
				return null;
			}
			// Text
			if (type_class.isAssignableFrom(OText.class)) {
				return FieldType.Text;
			}
			// Text
			if (type_class.isAssignableFrom(OHtml.class)) {
				return null;
			}
			// ManyToOne
			// if (mColumn.getRelationType() != null
			// && mColumn.getRelationType() == RelationType.ManyToOne) {
			// OInteger integer = new OInteger();
			// return integer.getType();
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setLabelText(String text) {
		mLabel = text;
		if (label_view != null) {
			label_view.setText(mLabel);
		}
	}

	public String getLabelText() {
		if (mLabel != null)
			return mLabel;
		return getFieldName();
	}

	public void setValue(Object value) {
		mValue = value;
		if (mValue != null) {
			switch (mType) {
			case Text:
				if (getEditable())
					mEditText.setText(getValue().toString());
				else
					mTextView.setText(getValue().toString());
				break;
			case Boolean:
				Boolean checked = Boolean.parseBoolean(getValue().toString());
				if (getEditable()) {
					if (mWidgetType != null) {
						switch (mWidgetType) {
						case Switch:
							Switch bool_switch = (Switch) mControl;
							if (bool_switch != null)
								bool_switch.setChecked(checked);
							break;
						default:
						}
					} else
						mCheckBox.setChecked(checked);
				} else {
					if (checked)
						mTextView.setText(getCheckBoxLabel());
					else
						setVisibility(GONE);
				}
				break;
			case Chips:
				break;
			case ManyToOne:
				break;
			case Selection:

				if (mWidgetType != null) {
					switch (mWidgetType) {
					case SelectionDialog:
						TextView dialogView = (TextView) mControl;
						Integer pos = Integer.parseInt(getValue().toString());
						dialogView.setText(getLabels()[pos]);
						AlertDialog dailog = (AlertDialog) dialogView.getTag();
						dailog.dismiss();
						break;
					case Searchable:
						TextView txvView = (TextView) mControl;
						pos = Integer.parseInt(getValue().toString());
						txvView.setText(getLabels()[pos]);
						break;
					default:
					}
				} else {
					Spinner mSpinner = (Spinner) mControl;
					int position = Integer.parseInt(getValue().toString());
					mSpinner.setSelection(position);
				}
				break;
			}
		}
	}

	public Object getValue() {
		return mValue;
	}

	public void setEditable(Boolean editable) {
		mEditable = editable;
		initcontrol();
	}

	public boolean getEditable() {
		return mEditable;
	}

	public void setHint(String hint) {
		mHint = hint;
	}

	public String getHint() {
		return mHint;
	}

	public String getFieldName() {
		return mField_name;
	}

	private View initTextControl() {
		setOrientation(VERTICAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (getEditable()) {
			mEditText = new EditText(mContext);
			mEditText.setTypeface(OControlHelper.lightFont());
			mEditText.setLayoutParams(params);
			mEditText.setBackgroundColor(Color.TRANSPARENT);
			mEditText.setPadding(0, 10, 10, 10);
			if (getValue() != null) {
				mEditText.setText(getValue().toString());
			}
			mEditText.setHint(mHint);
			return mEditText;
		} else {
			if (getValue() != null)
				mTextView.setText(getValue().toString());
			return mTextView;
		}

	}

	private TextView getLabelView() {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		TextView label = new TextView(mContext);
		label.setLayoutParams(params);
		label.setGravity(Gravity.LEFT);
		label.setText(getLabelText());
		label.setAllCaps(true);
		return label;
	}

	private View initBooleanControl() {
		if (getEditable()) {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			if (mWidgetType != null) {
				switch (mWidgetType) {
				case Switch:
					Switch mSwitch = new Switch(mContext);
					mSwitch.setLayoutParams(params);
					setValue(getValue());
					mSwitch.setText(getLabelText());
					return mSwitch;
				default:
				}
			}
			mCheckBox = new CheckBox(mContext);
			mCheckBox.setLayoutParams(params);
			if (getLabelText() != null)
				mCheckBox.setText(getLabelText());
			else
				mCheckBox.setText(getFieldName());
			return mCheckBox;
		} else {
			if (getValue() != null
					&& !(Boolean.parseBoolean(getValue().toString()))) {
				setVisibility(GONE);
			}
			mTextView.setText(getCheckBoxLabel());
			return mTextView;
		}
	}

	private String getCheckBoxLabel() {
		String label = "";
		if (getValue() != null && Boolean.parseBoolean(getValue().toString())) {
			label = "✔ ";
		}
		label += getLabelText();
		return label;
	}

	private String[] getLabels() {
		String[] labels = {};
		if (mValueArrayId != null
				&& !mContext.getClass().getSimpleName()
						.contains("BridgeContext")) {
			labels = mContext.getResources().getStringArray(
					(Integer) mValueArrayId);
		}
		return labels;
	}

	private int getArrayPosition() {
		int selected_position = -1;
		if (getValue() != null && getLabels().length > 0) {
			selected_position = Integer.parseInt(getValue().toString());
		}
		return selected_position;
	}

	private View initRadioGroup() {
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		final String[] labels = getLabels();

		if (mWidgetType != null) {
			int array_pos = getArrayPosition();
			switch (mWidgetType) {
			case RadioGroup:
				mradioGrp = new RadioGroup(mContext);
				mradioGrp.setLayoutParams(params);
				for (String label : labels) {
					RadioButton rdoBtn = new RadioButton(mContext);
					rdoBtn.setLayoutParams(params);
					rdoBtn.setText(label);
					mradioGrp.addView(rdoBtn);
				}
				if (array_pos != -1)
					((RadioButton) mradioGrp.getChildAt(array_pos))
							.setChecked(true);
				return mradioGrp;
			case SelectionDialog:
				final TextView txvDialogValue = new TextView(mContext);
				txvDialogValue.setLayoutParams(params);
				int pos = array_pos;
				if (pos == -1 && labels.length > 0)
					pos = 0;
				if (array_pos != -1) {
					txvDialogValue.setText(labels[pos]);
				} else
					txvDialogValue.setText("Nothing Selected");
				setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog dialog = createSelectionDialog(
								getArrayPosition(), labels, params);
						txvDialogValue.setTag(dialog);
						dialog.show();
					}
				});
				return txvDialogValue;
			case Searchable:
				TextView txvValue = new TextView(mContext);
				txvValue.setLayoutParams(params);
				pos = array_pos;
				if (pos == -1 && labels.length > 0)
					pos = 0;
				if (array_pos != -1) {
					txvValue.setText(labels[pos]);
				} else
					txvValue.setText("Nothing Selected");

				setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext,
								SearchableItemActivity.class);
						intent.putExtra("labels", getLabels());
						intent.putExtra("selected_position", getArrayPosition());
						intent.putExtra("search_hint", getLabelText());
						try {
							mContext.unregisterReceiver(valueReceiver);
						} catch (Exception e) {

						}
						mContext.registerReceiver(valueReceiver,
								new IntentFilter("searchable_value_select"));
						mContext.startActivity(intent);
					}
				});
				return txvValue;
			default:
			}
		}

		Spinner mSpinner = new Spinner(mContext);
		mSpinner.setLayoutParams(params);
		mSpinner.setAdapter(new ArrayAdapter<String>(mContext,
				android.R.layout.simple_list_item_1, labels));
		if (getArrayPosition() != -1)
			mSpinner.setSelection(getArrayPosition());
		return mSpinner;
	}

	private AlertDialog createSelectionDialog(final int selected_position,
			final String labels[], LayoutParams params) {
		final AlertDialog.Builder builder = new Builder(mContext);
		ListView dialogView = new ListView(mContext);
		dialogView.setAdapter(new ArrayAdapter<String>(mContext,
				android.R.layout.simple_expandable_list_item_1, labels) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null)
					v = LayoutInflater.from(mContext).inflate(
							android.R.layout.simple_expandable_list_item_1,
							parent, false);
				OControls.setText(v, android.R.id.text1, labels[position]);
				if (selected_position == position) {
					v.setBackgroundColor(mContext.getResources().getColor(
							R.color.control_selection_selected));
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				return v;
			}
		});
		dialogView.setTag(labels);
		dialogView.setOnItemClickListener(this);
		dialogView.setLayoutParams(params);
		builder.setView(dialogView);
		return builder.create();
	}

	public void setIcon(int resourceId) {
		img_icon.setImageResource(resourceId);
	}

	public int getIcon() {
		return resId;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		setValue(position);
	}

	BroadcastReceiver valueReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setValue(intent.getIntExtra("selected_position", 0));
			mContext.unregisterReceiver(valueReceiver);
		}
	};

}
