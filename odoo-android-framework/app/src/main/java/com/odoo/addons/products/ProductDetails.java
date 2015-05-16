package com.odoo.addons.products;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.products.utils.ShareUtil;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.base.addons.product.ProductCategory;
import com.odoo.base.addons.product.ProductProduct;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooFields;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import org.json.JSONObject;

import odoo.ODomain;
import odoo.Odoo;
import odoo.controls.OField;
import odoo.controls.OForm;

public class ProductDetails extends ActionBarActivity implements View.OnClickListener, OField.IOnFieldValueChangeListener {

    public static final String TAG = ProductDetails.class.getSimpleName();
    private final String KEY_MODE = "key_edit_mode";
    private final String KEY_NEW_IMAGE = "key_new_image";
    private ActionBar actionBar;
    private Bundle extras;
    private ProductProduct productProduct;
    private ODataRow record = null;
    private ParallaxScrollView parallaxScrollView;
    private ImageView userImage = null, captureImage = null;
    private TextView mTitleView = null;
    private OForm mForm;
    private App app;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private OFileManager fileManager;
    private String newImage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);
        OActionBarUtils.setActionBar(this, false);
        fileManager = new OFileManager(this);
        actionBar = getSupportActionBar();
        actionBar.setTitle("");
        if (savedInstanceState != null) {
            mEditMode = savedInstanceState.getBoolean(KEY_MODE);
            newImage = savedInstanceState.getString(KEY_NEW_IMAGE);
        }
        app = (App) getApplicationContext();
        parallaxScrollView = (ParallaxScrollView) findViewById(R.id.parallaxScrollView);
        parallaxScrollView.setActionBar(actionBar);
        userImage = (ImageView) findViewById(android.R.id.icon);
        mTitleView = (TextView) findViewById(android.R.id.title);
        productProduct = new ProductProduct(this, null);
        extras = getIntent().getExtras();
        if (extras == null)
            mEditMode = true;
        setupActionBar();

        /* Product category
        ProductCategory pcateg = new ProductCategory( this, OUser.current(this) );
        String xxx = record.getString("categ_id").replace( "[", "" ).replace( "]", "" ).replace( "\"", "" );
        xxx = xxx.substring( xxx.indexOf(",")+1 );
*/
        TextView txv = (TextView)findViewById( R.id.product_category_name );
        //txv.setText( "    " + xxx ); //categ.getString("complete_name") );

        txv.setText( record.getM2ORecord("categ_id").getName() );
    }

    private void setMode(Boolean edit) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_product_detail_more).setVisible(!edit);
            mMenu.findItem(R.id.menu_product_edit).setVisible(!edit);
            mMenu.findItem(R.id.menu_product_save).setVisible(edit);
            mMenu.findItem(R.id.menu_product_cancel).setVisible(edit);
        }
        int color = Color.DKGRAY;
        if (record != null) {
            color = OStringColorUtil.getStringColor(this, record.getString("name"));
        }
        if (edit) {
            if (extras != null)
                actionBar.setTitle(R.string.label_edit);
            else
                actionBar.setTitle(R.string.label_new);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            mForm = (OForm) findViewById(R.id.productFormEdit);
            captureImage = (ImageView) findViewById(R.id.captureImage);
            captureImage.setOnClickListener(this);
            userImage = (ImageView) findViewById(android.R.id.icon1);
            findViewById(R.id.parallaxScrollView).setVisibility(View.GONE);
            findViewById(R.id.productScrollViewEdit).setVisibility(View.VISIBLE);
            OField is_company = (OField) findViewById(R.id.is_company_edit);
            is_company.setOnValueChangeListener(this);
        } else {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_shade));
            userImage = (ImageView) findViewById(android.R.id.icon);
            mForm = (OForm) findViewById(R.id.productForm);
            findViewById(R.id.productScrollViewEdit).setVisibility(View.GONE);
            findViewById(R.id.parallaxScrollView).setVisibility(View.VISIBLE);
        }
        setColor(color);
    }

    private void setupActionBar() {
        if (extras == null) {
            setMode(mEditMode);
            userImage.setColorFilter(Color.parseColor("#ffffff"));
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = productProduct.browse(rowId);
            record.put("full_address", "ResPartner Full Address" ); //productProduct.getAddress(record));
            checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
            mTitleView.setText(record.getString("name"));
            setProductImage();
            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
                BigImageLoader bigImageLoader = new BigImageLoader();
                bigImageLoader.execute(record.getInt("id"));
            }
        }
    }

    @Override
    public void onClick(View v) {
    }

    private void checkControls() {}

    private void setProductImage() {
        if (!record.getString("image").equals("false")) {
            String base64 = record.getString("image");
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, base64));
        } else {
            userImage.setColorFilter(Color.parseColor("#ffffff"));
        }
    }

    private void setColor(int color) {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.parallax_view);
        frameLayout.setBackgroundColor(color);
        parallaxScrollView.setParallaxOverLayColor(color);
        parallaxScrollView.setBackgroundColor(color);
        mForm.setIconTintColor(color);
        findViewById(R.id.parallax_view).setBackgroundColor(color);
//        findViewById(R.id.parallax_view_edit).setBackgroundColor(color);
        findViewById(R.id.productScrollViewEdit).setBackgroundColor(color);
        if (captureImage != null) {
            GradientDrawable shapeDrawable =
                    (GradientDrawable) getResources().getDrawable(R.drawable.circle_mask_primary);
            shapeDrawable.setColor(color);
            captureImage.setBackgroundDrawable(shapeDrawable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_product_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    if (newImage != null) {
                        values.put("image", newImage);
                    }
                    if (record != null) {
                        productProduct.update(record.getInt(OColumn.ROW_ID), values);
                        Toast.makeText(this, R.string.toast_information_saved, Toast.LENGTH_LONG).show();
                        mEditMode = !mEditMode;
                        setupActionBar();
                    } else {
                        values.put("sale_ok", "true");
                        final int row_id = productProduct.insert(values);
                        if (row_id != OModel.INVALID_ROW_ID) {
                            finish();
                        }
                    }
                }
                break;
            case R.id.menu_product_cancel:
                if (record == null) {
                    finish();
                    return true;
                }
            case R.id.menu_product_edit:
                mEditMode = !mEditMode;
                setMode(mEditMode);
                mForm.setEditable(mEditMode);
                mForm.initForm(record);
                setProductImage();
                break;
            case R.id.menu_product_share:
                ShareUtil.shareContact(this, record, true);
                break;
            case R.id.menu_product_import:
                ShareUtil.shareContact(this, record, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        mMenu = menu;
        setMode(mEditMode);
        return true;
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {
    }


    private class BigImageLoader extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String image = null;
            try {
                Thread.sleep(300);
                Odoo odoo = app.getOdoo(productProduct.getUser());
                if (odoo == null) {
                    odoo = OSyncAdapter.createOdooInstance(ProductDetails.this, productProduct.getUser());
                }
                ODomain domain = new ODomain();
                domain.add("id", "=", params[0]);
                JSONObject result = odoo.search_read(productProduct.getModelName(),
                        new OdooFields(new String[]{"image"}).get(),
                        domain.get());
                JSONObject records = result.getJSONArray("records")
                        .getJSONObject(0);
                if (!records.getString("image").equals("false")) {
                    image = records.getString("image");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                if (!result.equals("false")) {
                    OValues values = new OValues();
                    values.put("large_image", result);
                    productProduct.update(record.getInt(OColumn.ROW_ID), values);
                    record.put("large_image", result);
                    setProductImage();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_MODE, mEditMode);
        outState.putString(KEY_NEW_IMAGE, newImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OValues values = fileManager.handleResult(requestCode, resultCode, data);
        if (values != null && !values.contains("size_limit_exceed")) {
            newImage = values.getString("datas");
            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            userImage.setColorFilter(null);
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, newImage));
        } else if (values != null) {
            Toast.makeText(this, R.string.toast_image_size_too_large, Toast.LENGTH_LONG).show();
        }
    }
}
