package com.odoo.demo.controls.com;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;

@SuppressLint("NewApi")
public class CustomForm extends LinearLayout {

	private Boolean mEditable = false;
	private String mModel;
	private OModel model = null;
	private HashMap<String, CustomControl> mFormFieldControls = new HashMap<String, CustomControl>();
	private Context mContext = null;
	private ODataRow mRecord = null;

	public CustomForm(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public CustomForm(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	@SuppressLint("NewApi")
	public CustomForm(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public CustomForm(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public void setEditable(Boolean editable) {
		mEditable = editable;
		for (String key : mFormFieldControls.keySet()) {
			CustomControl control = mFormFieldControls.get(key);
			control.setEditable(editable);
		}
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {
			TypedArray types = mContext.obtainStyledAttributes(attrs,
					R.styleable.customform);
			mModel = types.getString(R.styleable.customform_modelName);
			types.recycle();
		}
		initForm();
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
		mRecord = new ODataRow();
		mRecord = record;
		initForm();
	}

	public ODataRow getData() {
		return mRecord;
	}

	public void initForm(ODataRow record) {
		mRecord = new ODataRow();
		mRecord = record;
	}

	public void initForm() {
		findAllFields(this);
		model = OModel.get(mContext, mModel);
		setOrientation(VERTICAL);
		for (String key : mFormFieldControls.keySet()) {
			View v = mFormFieldControls.get(key);
			if (v instanceof CustomControl) {
				CustomControl c = (CustomControl) v;
				OColumn column = model.getColumn(c.getFieldName());
				if (column != null) {
					c.setColumn(column);
				}
				if (mRecord != null && mRecord.contains(c.getFieldName())) {
					// setting value to control
					c.setValue(mRecord.get(c.getFieldName()));
				}
				c.setEditable(mEditable);
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
			if (v instanceof CustomControl) {
				CustomControl field = (CustomControl) v;
				mFormFieldControls.put(field.getFieldName(), field);
			}
		}
	}
}
