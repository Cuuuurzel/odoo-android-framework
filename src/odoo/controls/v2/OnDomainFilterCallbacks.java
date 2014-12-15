package odoo.controls.v2;

import com.odoo.orm.OColumn.ColumnDomain;

public interface OnDomainFilterCallbacks {
	public void onFieldValueChanged(ColumnDomain domain);
}
