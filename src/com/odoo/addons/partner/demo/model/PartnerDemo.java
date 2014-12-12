package com.odoo.addons.partner.demo.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.base.res.ResPartner;
import com.odoo.demo.controls.com.CustomForm;
import com.odoo.orm.ODataRow;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.logger.OLog;

public class PartnerDemo extends BaseFragment implements OnClickListener {

	View mView = null;
	ODataRow row = null;
	CustomForm cForm = null;

	public enum Keys {
		Demo
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.partner_demo, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.toggleMode).setOnClickListener(this);
		init();
	}

	private void init() {
		cForm = (CustomForm) mView.findViewById(R.id.form);
		row = new ODataRow();
		ResPartner resPartner = new ResPartner(getActivity());
		row = resPartner.select(6);
		cForm.setData(row);
		cForm.setEditable(true);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new PartnerDemoModel(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(PartnerDemo.class.getName(), "Demo", 0, 0,
				object(Keys.Demo)));
		return menu;
	}

	private Fragment object(Keys value) {
		Fragment f = (value == Keys.Demo) ? new PartnerDemo()
				: new PartnerDemo();
		Bundle args = new Bundle();
		args.putString("id", "5");
		f.setArguments(args);
		return f;
	}

	@Override
	public void onClick(View v) {
		cForm.setEditable(!cForm.getEditable());
		OLog.log(">>>>>>>>>>> " + cForm.getValues());
	}

}
