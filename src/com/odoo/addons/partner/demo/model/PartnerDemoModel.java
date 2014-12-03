package com.odoo.addons.partner.demo.model;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;

public class PartnerDemoModel extends OModel {
	OColumn name = new OColumn("Name", OText.class);
	OColumn is_company = new OColumn("Is Company", OBoolean.class)
			.setDefault(false);
	OColumn street = new OColumn("Street", OText.class);
	OColumn zip = new OColumn("Zip", OVarchar.class, 10);

	public PartnerDemoModel(Context context) {
		super(context, "res.partner");
	}

}
