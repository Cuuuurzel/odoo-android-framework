package odoo.controls.v2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.odoo.orm.OColumn;
import com.odoo.util.logger.OLog;

public class OBlobField extends LinearLayout implements OControlData {

	private Context mContext;
	private Boolean mReady = false, isEditable = false;
	private ValueUpdateListener mValueUpdateListener = null;
	private String mLabel;
	private OColumn mCol;
	private Object mValue;
	private BezelImageView imgView;

	public OBlobField(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public OBlobField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public OBlobField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public OBlobField(Context context) {
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
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		removeAllViews();
		setOrientation(VERTICAL);
		imgView = new BezelImageView(mContext);
		imgView.setLayoutParams(params);
		addView(imgView);
	}

	@Override
	public void setValue(Object value) {
		mValue = value;
		OLog.log(value + "");
	}

	@Override
	public Object getValue() {
		return mValue;
	}

	@Override
	public void setEditable(Boolean editable) {
		isEditable = editable;
	}

	@Override
	public Boolean isEditable() {
		return isEditable;
	}

	@Override
	public void setColumn(OColumn column) {
		mCol = column;
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

	@Override
	public void setValueUpdateListener(ValueUpdateListener listener) {
		mValueUpdateListener = listener;
	}

	@Override
	public Boolean isControlReady() {
		return mReady;
	}

	@Override
	public void resetData() {

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mReady = true;
	}

}
