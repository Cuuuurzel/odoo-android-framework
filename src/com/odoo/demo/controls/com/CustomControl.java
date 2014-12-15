package com.odoo.demo.controls.com;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.demo.controls.com.OControlData.ValueUpdateListener;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OHtml;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OTimestamp;
import com.odoo.orm.types.OVarchar;

public class CustomControl extends LinearLayout implements ValueUpdateListener {

	private Context mContext = null;
	private FieldType mType = FieldType.Text;
	private OColumn mColumn = null;
	private OModel mModel = null;
	private String mLabel, mField_name;
	private Object mValue = null;
	private boolean mEditable = false, showIcon = true, show_label = true;
	private TextView label_view = null;
	private int resId, tint_color = Color.BLACK, mValueArrayId = -1;
	private ImageView img_icon = null;
	private ViewGroup container = null;
	private Boolean with_bottom_padding = true, with_top_padding = true;
	private WidgetType mWidgetType = null;
	private String mParsePattern = null;

	// Controls
	private OControlData mControlData = null;

	public enum WidgetType {
		Switch, RadioGroup, SelectionDialog, Searchable, SearchableLive;

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
			case 4:
				return WidgetType.SearchableLive;
			}
			return null;
		}
	}

	public enum FieldType {
		Text, Boolean, ManyToOne, Chips, Selection, Date, DateTime;

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
			case 5:
				return FieldType.Date;
			case 6:
				return FieldType.DateTime;
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
			mParsePattern = types
					.getString(R.styleable.customcontrol_parse_pattern);
			mValueArrayId = types.getResourceId(
					R.styleable.customcontrol_value_array, -1);
			mWidgetType = WidgetType.getWidgetType(types.getInt(
					R.styleable.customcontrol_customWidget, -1));
			types.recycle();
		}
		if (mContext.getClass().getSimpleName().contains("BridgeContext"))
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

	public void initcontrol() {
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
		case Selection:
			controlView = initSelectionWidget();
			break;
		case Date:
		case DateTime:
			controlView = initDateTimeControl(mType);
			break;
		default:
			break;
		}
		mControlData.setValueUpdateListener(this);
		mControlData.setEditable(getEditable());
		mControlData.setValue(mValue);
		mControlData.initControl();
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
		mColumn = column;
		mType = getType(column.getType());
		if (label_view != null) {
			label_view.setText(mLabel);
		}
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
			if (mColumn.getRelationType() != null
					&& mColumn.getRelationType() == RelationType.ManyToOne) {
				return FieldType.ManyToOne;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getLabelText() {
		if (mLabel != null)
			return mLabel;
		if (mColumn != null)
			return mColumn.getLabel();
		if (mControlData != null)
			return mControlData.getLabel();
		return getFieldName();
	}

	public void setValue(Object value) {
		mValue = value;
		if (mValue != null && mControlData != null) {
			mControlData.setValue(mValue);
		}
	}

	public Object getValue() {
		if (mControlData != null)
			return mControlData.getValue();
		return null;
	}

	public void setEditable(Boolean editable) {
		mEditable = editable;
		Object value = getValue();
		mControlData.setEditable(editable);
		mControlData.initControl();
		if (value != null)
			mControlData.setValue(value);
	}

	public boolean getEditable() {
		return mEditable;
	}

	public String getFieldName() {
		return mField_name;
	}

	// EditText control (TextView, EditText)
	private View initTextControl() {
		setOrientation(VERTICAL);
		OEditText edt = new OEditText(mContext);
		mControlData = edt;
		edt.setColumn(mColumn);
		edt.setHint(mLabel);
		return edt;
	}

	// Boolean Control (Checkbox, W-Switch)
	private View initBooleanControl() {
		OBooleanField bool = new OBooleanField(mContext);
		mControlData = bool;
		bool.setColumn(mColumn);
		bool.setLabelText(getLabelText());
		bool.setWidgetType(mWidgetType);
		return bool;
	}

	// Selection, Searchable, SearchableLive
	private View initSelectionWidget() {
		OSelectionField selection = new OSelectionField(mContext);
		mControlData = selection;
		selection.setLabelText(getLabelText());
		selection.setModel(mModel);
		selection.setArrayResourceId(mValueArrayId);
		selection.setFieldType(mType);
		selection.setColumn(mColumn);
		selection.setWidgetType(mWidgetType);
		return selection;
	}

	private View initDateTimeControl(FieldType type) {
		ODateTimeField datetime = new ODateTimeField(mContext);
		mControlData = datetime;
		datetime.setFieldType(type);
		datetime.setParsePattern(mParsePattern);
		datetime.setLabelText(getLabelText());
		datetime.setColumn(mColumn);
		return datetime;
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

	public void setIcon(int resourceId) {
		img_icon.setImageResource(resourceId);
	}

	public int getIcon() {
		return resId;
	}

	public void setModel(OModel model) {
		mModel = model;
	}

	public OModel getModel() {
		return mModel;
	}

	@Override
	public void onValueUpdate(Object value) {
		mValue = value;
	}

	@Override
	public void visibleControl(boolean isVisible) {
		if (isVisible) {
			setVisibility(View.VISIBLE);
		} else {
			setVisibility(View.GONE);
		}
	}

}
