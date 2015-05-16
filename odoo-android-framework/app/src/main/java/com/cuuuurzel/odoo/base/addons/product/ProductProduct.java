package com.odoo.base.addons.product;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Cuuuurzel on 16/05/15.
 */
public class ProductProduct extends OModel {

    public static final String AUTHORITY = "com.odoo.core.provider.content.sync.product_product";
    //OColumn uom_id   = new OColumn( "uom id",  ProductUom.class,  OColumn.RelationType.ManyToOne);
    //OColumn categ_id = new OColumn("categ id", ProductCategory.class, OColumn.RelationType.ManyToOne);
    OColumn default_code  = new OColumn( "DefaultCode",   OVarchar.class ).setRequired();
    OColumn description   = new OColumn( "Description",   OVarchar.class ).setRequired();
    OColumn ean13         = new OColumn( "EAN13",         OVarchar.class ).setRequired();
    OColumn list_price    = new OColumn( "list price",    OFloat.class   ).setRequired();
    OColumn name          = new OColumn( "Name",          OVarchar.class ).setRequired();
    OColumn qty_available = new OColumn( "qty available", OFloat.class   ).setRequired();
    OColumn sale_ok       = new OColumn( "sale ok",       OBoolean.class ).setRequired();;
    OColumn code          = new OColumn( "code",          OVarchar.class ).setRequired();
    OColumn image         = new OColumn( "Image",         OBlob.class    );
    OColumn categ_id = new OColumn( "categ_id", ProductCategory.class, OColumn.RelationType.ManyToOne );

    Context mContext;
    OUser mUser;

    public ProductProduct(Context context, OUser user) {
        super(context, "product.product", user);
        setHasMailChatter(false);

        this.mContext = context;
        this.mUser = user;
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

}

