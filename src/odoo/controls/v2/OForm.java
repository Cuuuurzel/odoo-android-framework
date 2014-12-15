package odoo.controls.v2;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.ColumnDomain;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;

@SuppressLint("NewApi")
public class OForm extends LinearLayout {

	private Boolean mEditable = false;
	private String mModel;
	private OModel model = null;
	private HashMap<String, OField> mFormFieldControls = new HashMap<String, OField>();
	private Context mContext = null;
	private ODataRow mRecord = null;

	public OForm(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public OForm(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	@SuppressLint("NewApi")
	public OForm(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public OForm(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public void setEditable(Boolean editable) {
		mEditable = editable;
		for (String key : mFormFieldControls.keySet()) {
			OField control = mFormFieldControls.get(key);
			control.setEditable(editable);
		}
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {
			TypedArray types = mContext.obtainStyledAttributes(attrs,
					R.styleable.OFormV2);
			mModel = types.getString(R.styleable.OFormV2_modelName);
			mEditable = types.getBoolean(R.styleable.OFormV2_editableMode,
					false);
			types.recycle();
		}
		initForm();
		LayoutTransition transition = new LayoutTransition();
		setLayoutTransition(transition);
	}

	public boolean getEditable() {
		return mEditable;
	}

	public void setModel(String model) {
		mModel = model;
	}

	public String getModel() {
		return mModel;
	}

	public void setData(ODataRow record) {
		initForm(record);
	}

	public ODataRow getData() {
		return mRecord;
	}

	public void initForm(ODataRow record) {
		mRecord = new ODataRow();
		mRecord = record;
		initForm();
	}

	private void initForm() {
		findAllFields(this);
		model = OModel.get(mContext, mModel);
		setOrientation(VERTICAL);
		for (String key : mFormFieldControls.keySet()) {
			View v = mFormFieldControls.get(key);
			if (v instanceof OField) {
				OField c = (OField) v;
				c.setModel(model);
				OColumn column = model.getColumn(c.getFieldName());
				if (column != null) {
					c.setColumn(column);
					// Setting OnChange Event
					if (column.hasOnChange()) {
						setOnChangeForControl(column, c);
					}

					// Setting domain Filter for column
					if (column.hasDomainFilterColumn()) {
						setOnDomainFilterCallBack(column, c);
					}
				}
				c.initcontrol();
				Object val = c.getValue();
				if (mRecord != null) {
					if (mRecord.contains(c.getFieldName()))
						val = mRecord.get(c.getFieldName());
				}
				if (val != null)
					c.setValue(val);
			}
		}
	}

	private void findAllFields(ViewGroup view) {
		int childs = view.getChildCount();
		for (int i = 0; i < childs; i++) {
			View v = view.getChildAt(i);
			if (v instanceof LinearLayout || v instanceof RelativeLayout) {
				findAllFields((ViewGroup) v);
			}
			if (v instanceof OField) {
				OField field = (OField) v;
				mFormFieldControls.put(field.getFieldName(), field);
			}
		}
	}

	public OValues getValues() {
		OValues values = new OValues();
		for (String key : mFormFieldControls.keySet()) {
			OField control = mFormFieldControls.get(key);
			Object val = control.getValue();
			if (val.toString().equals("-1")) {
				val = false;
			}
			values.put(key, val);
		}
		return values;
	}

	// OnDomainFilterCallBack callbacks
	private void setOnDomainFilterCallBack(final OColumn column, OField field) {
		LinkedHashMap<String, ColumnDomain> filterDomain = column
				.getFilterDomains();
		for (String key : filterDomain.keySet()) {
			ColumnDomain domain = filterDomain.get(key);
			if (domain.getColumn() != null) {
				OField fld = mFormFieldControls.get(domain.getColumn());
				if (fld != null) {
					setFilterDomainCallback(domain, fld, field, column);
				}
			}
		}

	}

	private void setFilterDomainCallback(ColumnDomain domain,
			final OField field, final OField oField, final OColumn column) {
		field.setOnFilterDomainCallBack(domain, new OnDomainFilterCallbacks() {

			@Override
			public void onFieldValueChanged(ColumnDomain dm) {
				column.addDomain(dm.getColumn(), dm.getOperator(),
						dm.getValue());
				column.setHasDomainFilterColumn(false);
				oField.setColumn(column);
				oField.resetData();
			}
		});
	}

	// OnChange event for control column
	private void setOnChangeForControl(final OColumn column, OField field) {
		field.setOnChangeCallbackListener(new OnChangeCallback() {

			@Override
			public void onValueChange(final ODataRow row) {
				if (!column.isOnChangeBGProcess()) {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							ODataRow vals = model.getOnChangeValue(column, row);
							fillOnChangeData(vals);
						}
					}, 300);
				} else {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							OnChangeBackground bgProcess = new OnChangeBackground(
									column);
							bgProcess.execute(row);
						}
					}, 300);
				}
			}
		});
	}

	private void fillOnChangeData(ODataRow vals) {
		if (vals != null) {
			for (String key : vals.keys()) {
				if (mFormFieldControls.containsKey(key)) {
					OField fld = mFormFieldControls.get(key);
					fld.setValue(vals.get(key));
				}
			}
		}
	}

	private class OnChangeBackground extends
			AsyncTask<ODataRow, Void, ODataRow> {
		private ProgressDialog mDialog;

		private OColumn mCol;

		public OnChangeBackground(OColumn col) {
			mCol = col;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = new ProgressDialog(mContext);
			mDialog.setTitle(mContext.getString(R.string.title_working));
			mDialog.setMessage(mContext.getString(R.string.title_please_wait));
			mDialog.setCancelable(false);
			mDialog.show();
		}

		@Override
		protected ODataRow doInBackground(ODataRow... params) {
			try {
				Thread.sleep(300);
				return model.getOnChangeValue(mCol, params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(ODataRow result) {
			super.onPostExecute(result);
			if (result != null) {
				fillOnChangeData(result);
			}
			mDialog.dismiss();
		}
	}
}
