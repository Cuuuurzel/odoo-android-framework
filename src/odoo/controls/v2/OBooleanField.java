package odoo.controls.v2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.odoo.orm.OColumn;

public class OBooleanField extends LinearLayout implements OControlData,
		OnCheckedChangeListener {

	private Context mContext;
	private OColumn mColumn;
	private Boolean mEditable = false;
	private String mLabel = null;
	private Boolean mValue = false;
	private OField.WidgetType mWidget = null;
	private ValueUpdateListener mValueUpdateListener = null;
	// Controls
	private TextView txvView = null;
	private CheckBox mCheckbox = null;
	private Switch mSwitch = null;
	private Boolean mReady = false;

	public OBooleanField(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public OBooleanField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public OBooleanField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public OBooleanField(Context context) {
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

	public void initControl() {
		mReady = false;
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		removeAllViews();
		setOrientation(VERTICAL);
		if (isEditable()) {
			if (mWidget != null) {
				switch (mWidget) {
				case Switch:
					mSwitch = new Switch(mContext);
					mSwitch.setLayoutParams(params);
					mSwitch.setOnCheckedChangeListener(this);
					setValue(getValue());
					if (mLabel != null)
						mSwitch.setText(mLabel);
					addView(mSwitch);
					break;
				default:
					break;
				}
			} else {
				mCheckbox = new CheckBox(mContext);
				mCheckbox.setLayoutParams(params);
				mCheckbox.setOnCheckedChangeListener(this);
				if (mLabel != null)
					mCheckbox.setText(mLabel);
				addView(mCheckbox);
			}
		} else {
			txvView = new TextView(mContext);
			txvView.setLayoutParams(params);
			txvView.setText(getCheckBoxLabel());
			addView(txvView);
		}
	}

	@Override
	public void setValue(Object value) {
		if (value == null)
			return;
		mValue = Boolean.parseBoolean(value.toString());
		if (isEditable()) {
			if (mWidget != null) {
				switch (mWidget) {
				case Switch:
					mSwitch.setChecked(Boolean.parseBoolean(getValue()
							.toString()));
					break;
				default:
					break;
				}
			} else {
				mCheckbox.setChecked(Boolean
						.parseBoolean(getValue().toString()));
			}
		} else {
			txvView.setText(getCheckBoxLabel());
		}
		if (mValueUpdateListener != null) {
			mValueUpdateListener.onValueUpdate(value);
			if (!isEditable() && mValue == false) {
				mValueUpdateListener.visibleControl(false);
			} else {
				mValueUpdateListener.visibleControl(true);
			}
		}
	}

	@Override
	public Object getValue() {
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

	public void setWidgetType(OField.WidgetType type) {
		mWidget = type;
		initControl();
	}

	@Override
	public void setLabelText(String label) {
		mLabel = label;
	}

	private String getCheckBoxLabel() {
		String label = "";
		if (getValue() != null && Boolean.parseBoolean(getValue().toString())) {
			label = "✔ ";
		}
		label += getLabel();
		return label;
	}

	@Override
	public String getLabel() {
		if (mLabel != null)
			return mLabel;
		if (mColumn != null)
			return mColumn.getLabel();
		return "unknown";
	}

	@Override
	public void setColumn(OColumn column) {
		mColumn = column;
		if (mLabel == null && mColumn != null)
			mLabel = mColumn.getLabel();
	}

	@Override
	public void setValueUpdateListener(ValueUpdateListener listener) {
		mValueUpdateListener = listener;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		setValue(isChecked);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mReady = true;
	}

	@Override
	public Boolean isControlReady() {
		return mReady;
	}

	@Override
	public void resetData() {
		setValue(getValue());
	}
}
