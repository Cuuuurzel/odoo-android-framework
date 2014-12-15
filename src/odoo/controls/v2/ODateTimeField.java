package odoo.controls.v2;

import java.util.Date;
import java.util.TimeZone;

import odoo.controls.v2.OField.FieldType;
import odoo.controls.v2.DateTimePicker.Builder;
import odoo.controls.v2.DateTimePicker.PickerCallBack;
import odoo.controls.v2.DateTimePicker.Type;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.orm.OColumn;
import com.odoo.util.ODate;

public class ODateTimeField extends LinearLayout implements OControlData,
		PickerCallBack {

	private Context mContext;
	private Boolean mEditable = false;
	private OColumn mColumn;
	private String mLabel, mHint;
	private ValueUpdateListener mValueUpdateListener = null;
	private FieldType mFieldType;
	private TextView txvText;
	private Object mValue;
	private String mParsePattern = ODate.DEFAULT_DATE_FORMAT;
	private DateTimePicker.Builder builder = null;
	private String mDate;
	private Boolean mReady = false;

	public ODateTimeField(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public ODateTimeField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public ODateTimeField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public ODateTimeField(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {

		}
		mReady = false;
		initControl();
	}

	public void setFieldType(FieldType type) {
		mFieldType = type;
		if (mFieldType == FieldType.DateTime) {
			mParsePattern = ODate.DEFAULT_FORMAT;
		}
	}

	@Override
	public void initControl() {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		removeAllViews();
		setOrientation(VERTICAL);
		txvText = new TextView(mContext);
		txvText.setLayoutParams(params);
		txvText.setOnClickListener(null);
		if (isEditable()) {
			txvText.setOnClickListener(onClick);
		}
		if (mValue != null) {
			txvText.setText(getDate(mValue.toString(), mParsePattern));
		}
		addView(txvText);
	}

	@Override
	public void setValue(Object value) {
		mValue = value;
		if (value == null)
			return;
		txvText.setText(getDate(mValue.toString(), mParsePattern));
		if (mValueUpdateListener != null) {
			mValueUpdateListener.onValueUpdate(value);
		}
	}

	@Override
	public Object getValue() {
		if (!TextUtils.isEmpty(mValue.toString())) {
			if (mFieldType == FieldType.Date)
				return mValue.toString().replaceAll(" 00:00:00", "");
			return mValue;
		}
		return null;
	}

	@Override
	public void setEditable(Boolean editable) {
		if (mEditable != editable) {
			mEditable = editable;
		}
	}

	@Override
	public Boolean isEditable() {
		return mEditable;
	}

	@Override
	public void setLabelText(String label) {
		mLabel = label;
	}

	@Override
	public void setColumn(OColumn column) {
		mColumn = column;
	}

	@Override
	public String getLabel() {
		if (mLabel != null)
			return mLabel;
		if (mColumn != null)
			return mColumn.getLabel();
		if (mHint != null)
			return mHint;
		return "unknown";
	}

	@Override
	public void setValueUpdateListener(ValueUpdateListener listener) {
		mValueUpdateListener = listener;
	}

	View.OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			builder = new Builder(mContext);
			if (mFieldType == FieldType.Date) {
				builder.setType(Type.Date);
			} else {
				builder.setType(Type.DateTime);
			}
			builder.setCallBack(ODateTimeField.this);
			builder.build().show();
		}
	};

	private String getDate(String date, String format) {
		if (date.contains("now()") || date.contains("NOW()")) {
			mValue = ODate
					.getUTCDate((mFieldType == FieldType.Date) ? ODate.DEFAULT_DATE_FORMAT
							: ODate.DEFAULT_FORMAT);
			return ODate.getDate(mContext, ODate.getDate(), TimeZone
					.getDefault().getID(), format);
		} else {
			if (mFieldType == FieldType.Date) {
				date += " 00:00:00";
			}
			return ODate.getDate(mContext, date, TimeZone.getDefault().getID(),
					format);
		}
	}

	@Override
	public void onDatePick(String date) {
		mDate = date;
		if (mFieldType == FieldType.Date) {
			setValue(mDate + " 00:00:00");
		}
	}

	@Override
	public void onTimePick(String time) {
		Date dt = ODate.convertToDate(mDate + " " + time, ODate.DEFAULT_FORMAT,
				false);
		String utc_date = ODate.getUTCDate(dt, ODate.DEFAULT_FORMAT);
		setValue(utc_date);
	}

	public void setParsePattern(String parsePattern) {
		if (parsePattern != null)
			mParsePattern = parsePattern;
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
