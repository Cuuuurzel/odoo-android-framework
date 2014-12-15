package odoo.controls.v2;

import odoo.controls.OControlHelper;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.orm.OColumn;

public class OEditTextField extends LinearLayout implements OControlData,
		OnFocusChangeListener {

	private Context mContext;
	private EditText edtText;
	private TextView txvText;
	private Boolean mEditable = false, mReady = false;;
	private OColumn mColumn;
	private String mLabel, mHint;
	private ValueUpdateListener mValueUpdateListener = null;

	public OEditTextField(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public OEditTextField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public OEditTextField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public OEditTextField(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		mContext = context;
		if (attrs != null) {

		}
		mReady = false;
		if (mContext.getClass().getSimpleName().contains("BridgeContext"))
			initControl();
	}

	public void initControl() {
		// Creating control
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		removeAllViews();
		setOrientation(VERTICAL);
		if (mEditable) {
			edtText = new EditText(mContext);
			edtText.setTypeface(OControlHelper.lightFont());
			edtText.setLayoutParams(params);
			edtText.setBackgroundColor(Color.TRANSPARENT);
			edtText.setPadding(0, 10, 10, 10);
			edtText.setHint(getLabel());
			edtText.setOnFocusChangeListener(this);
			addView(edtText);
		} else {
			txvText = new TextView(mContext);
			txvText.setTypeface(OControlHelper.lightFont());
			txvText.setLayoutParams(params);
			txvText.setBackgroundColor(Color.TRANSPARENT);
			txvText.setPadding(0, 10, 10, 10);
			addView(txvText);
		}
	}

	@Override
	public void setValue(Object value) {
		if (value == null)
			return;
		if (mEditable) {
			edtText.setText(value.toString());
		} else {
			txvText.setText(value.toString());
		}
		if (mValueUpdateListener != null) {
			mValueUpdateListener.onValueUpdate(value);
		}
	}

	@Override
	public Object getValue() {
		if (mEditable)
			return edtText.getText();
		if (txvText != null)
			return txvText.getText();
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

	public void setHint(String hint) {
		mHint = hint;
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

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus && edtText.getText().length() > 0) {
			setValue(edtText.getText());
		}
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
