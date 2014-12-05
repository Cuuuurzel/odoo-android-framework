package com.odoo.demo.controls.com;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.odoo.R;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;
import com.odoo.util.OControls;

public class SearchableItemActivity extends ActionBarActivity implements
		OnItemClickListener, TextWatcher {

	private EditText edt_searchable_input;
	private ListView mList = null;
	private OListAdapter mAdapter;
	private List<Object> objects = new ArrayList<Object>();
	private int selected_position = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_control_searchable_layout);
		setResult(RESULT_CANCELED);
		edt_searchable_input = (EditText) findViewById(R.id.edt_searchable_input);
		edt_searchable_input.addTextChangedListener(this);
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			if (extra.containsKey("selected_position")) {
				selected_position = extra.getInt("selected_position");
			}
			if (extra.containsKey("search_hint")) {
				edt_searchable_input.setHint("Search "
						+ extra.getString("search_hint"));
			}
			String[] arrays = extra.getStringArray("labels");
			for (int i = 0; i < arrays.length; i++) {
				ODataRow row = new ODataRow();
				row.put("index", i);
				row.put("title", arrays[i]);
				objects.add(row);
			}

			mList = (ListView) findViewById(R.id.searchable_items);
			mList.setOnItemClickListener(this);
			mAdapter = new OListAdapter(this,
					android.R.layout.simple_expandable_list_item_1, objects) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = convertView;
					if (v == null)
						v = getLayoutInflater().inflate(getResource(), parent,
								false);
					ODataRow row = (ODataRow) objects.get(position);
					OControls.setText(v, android.R.id.text1,
							row.getString("title"));
					if (selected_position == row.getInt("index")) {
						v.setBackgroundColor(getResources().getColor(
								R.color.control_selection_selected));
					} else {
						v.setBackgroundColor(Color.TRANSPARENT);
					}
					return v;
				}
			};
			mList.setAdapter(mAdapter);
		} else {
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent("searchable_value_select");
		ODataRow data = (ODataRow) objects.get(position);
		intent.putExtra("selected_position", data.getInt("index"));
		sendBroadcast(intent);
		finish();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mAdapter.getFilter().filter(s);
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

}
