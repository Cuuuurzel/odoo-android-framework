package odoo.controls.v2;

import com.odoo.orm.ODataRow;

public interface OnChangeCallback {
	public void onValueChange(ODataRow row);
}
