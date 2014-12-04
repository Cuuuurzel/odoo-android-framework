package com.odoo.demo.controls.com;

import odoo.controls.OControlHelper;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

@SuppressLint("NewApi")
public class CustomControl extends LinearLayout {

	private Context mContext = null;
	private boolean required = false;
	private FieldType mType = FieldType.Text;
	private OColumn mColumn = null;
	private String mHint, mLabel, mField_name;
	private Object mValue = null;
	private EditText mEditText = null;
	private RadioGroup mradioGrp = null;
	private String[] labels = null;
	private boolean mEditable = false, showIcon = true, show_label = true;
	private View mControl = null;
	private TextView label_view = null, mTextView = null;
	private int resId, tint_color = Color.BLACK;
	private ImageView img_icon = null;
	private ViewGroup container = null;
	private CheckBox mCheckBox = null;

	public enum Orientation {
		Vertical, Horizantal;
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
			types.recycle();
		}
		initcontrol();
	}

	private void initLayout() {
		removeAllViews();
		View layout = LayoutInflater.from(mContext).inflate(
				R.layout.custom_control_template, this, false);
		addView(layout);
		container = (ViewGroup) findViewById(R.id.control_container);
		img_icon = (ImageView) findViewById(android.R.id.icon);
		img_icon.setColorFilter(tint_color);
		setImageIcon();
	}

	private void createTextView() {
		mTextView = new TextView(mContext);
		mTextView.setTextColor(Color.BLACK);
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
		switch (mType) {
		case Text:
			if (getEditable())
				mEditText.setText(getValue().toString());
			else
				mTextView.setText(getValue().toString());
			break;
		case Boolean:
			if (getEditable())
				mCheckBox.setChecked(Boolean.getBoolean(getValue().toString()));
			else
				mTextView.setText(getCheckBoxLabel());
			break;
		case Chips:
			break;
		case ManyToOne:
			break;
		case Selection:
			break;
		}
	}

	public <T> T getValue() {
		return (T) mValue;
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
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT);
			mCheckBox = new CheckBox(mContext);
			mCheckBox.setLayoutParams(params);
			if (getValue() != null) {
				String bool_val = getValue().toString();
				mCheckBox.setChecked(Boolean.parseBoolean(bool_val));
			}
			if (getLabelText() != null)
				mCheckBox.setText(getLabelText());
			else
				mCheckBox.setText(getFieldName());
			return mCheckBox;
		} else {
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

	private View initRadioGroup() {
		setOrientation(VERTICAL);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mradioGrp = new RadioGroup(mContext);
		mradioGrp.setLayoutParams(params);
		for (String label : labels) {
			RadioButton rdoBtn = new RadioButton(mContext);
			rdoBtn.setText(label);
			mradioGrp.addView(rdoBtn);
		}
		return mradioGrp;
	}

	public void setRadioLabels(String[] labels) {
		this.labels = labels;
	}

	public String[] getRadioLabels() {
		return labels;
	}

	public void setIcon(int resourceId) {
		img_icon.setImageResource(resourceId);
	}

	public int getIcon() {
		return resId;
	}
}
