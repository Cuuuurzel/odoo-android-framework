package odoo.controls.v2;

import com.odoo.orm.OColumn;

public interface OControlData {

	public void setValue(Object value);

	public Object getValue();

	public void setEditable(Boolean editable);

	public Boolean isEditable();

	public void setLabelText(String label);

	public void setColumn(OColumn column);

	public void initControl();

	public String getLabel();

	public void setValueUpdateListener(ValueUpdateListener listener);

	public static interface ValueUpdateListener {
		public void onValueUpdate(Object value);

		public void visibleControl(boolean isVisible);
	}

	public Boolean isControlReady();

	public void resetData();
}
