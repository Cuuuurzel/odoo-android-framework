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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.support.listview.OListAdapter;
import com.odoo.util.OControls;

public class SearchableItemActivity extends ActionBarActivity implements
		OnItemClickListener, TextWatcher, OnClickListener {

	private EditText edt_searchable_input;
	private ListView mList = null;
	private OListAdapter mAdapter;
	private List<Object> objects = new ArrayList<Object>();
	private int selected_position = -1;
	private Boolean mLiveSearch = false;
	private int resource_array_id = -1;
	private OModel mModel = null;
	private Integer mRowId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_control_searchable_layout);
		setResult(RESULT_CANCELED);
		edt_searchable_input = (EditText) findViewById(R.id.edt_searchable_input);
		edt_searchable_input.addTextChangedListener(this);
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			if (extra.containsKey("resource_id")) {
				resource_array_id = extra.getInt("resource_id");
			}
			if (extra.containsKey(OColumn.ROW_ID)) {
				mRowId = extra.getInt(OColumn.ROW_ID);
			}
			if (extra.containsKey("model")) {
				mModel = OModel.get(this, extra.getString("model"));
			}
			if (extra.containsKey("live_search")) {
				mLiveSearch = extra.getBoolean("live_search");
			}
			if (extra.containsKey("selected_position")) {
				selected_position = extra.getInt("selected_position");
			}
			if (extra.containsKey("search_hint")) {
				edt_searchable_input.setHint("Search "
						+ extra.getString("search_hint"));
			}
			if (resource_array_id != -1) {
				String[] arrays = getResources().getStringArray(
						resource_array_id);
				for (int i = 0; i < arrays.length; i++) {
					ODataRow row = new ODataRow();
					row.put(OColumn.ROW_ID, i);
					row.put("name", arrays[i]);
					objects.add(row);
				}
			} else {
				OColumn col = null;
				OModel rel_model = null;
				if (extra.containsKey("column_name")) {
					col = mModel.getColumn(extra.getString("column_name"));
					rel_model = mModel.createInstance(col.getType());
					objects.addAll(OSelectionField.getRecordItems(rel_model,
							col));
				}
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
							row.getString("name"));
					if (selected_position == row.getInt(OColumn.ROW_ID)) {
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
		intent.putExtra("selected_position", data.getInt(OColumn.ROW_ID));
		if (mRowId != null) {
			intent.putExtra("record_id", true);
		}
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
		ImageView imgView = (ImageView) findViewById(R.id.search_icon);
		if (s.length() > 0) {
			imgView.setImageResource(R.drawable.ic_action_content_remove);
			imgView.setOnClickListener(this);
			imgView.setClickable(true);
		} else {
			imgView.setClickable(false);
			imgView.setImageResource(R.drawable.ic_action_search);
			imgView.setOnClickListener(null);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void onClick(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

}
