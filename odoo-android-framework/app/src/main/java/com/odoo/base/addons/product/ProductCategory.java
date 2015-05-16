package com.odoo.base.addons.product;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Cuuuurzel on 16/05/15.
 */
public class ProductCategory extends OModel {

    public static final String AUTHORITY = "com.odoo.core.provider.content.sync.product_category";
    OColumn name = new OColumn( "name", OVarchar.class ).setRequired();
    OColumn complete_name = new OColumn( "complete name", OVarchar.class ).setRequired();

    public ProductCategory(Context context, OUser user) {
        super(context, "product.category", user);
        setHasMailChatter(false);
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }
}

