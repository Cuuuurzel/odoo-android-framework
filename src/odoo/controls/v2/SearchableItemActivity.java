package odoo.controls.v2;

import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.odoo.orm.OColumn.ColumnDomain;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OFieldsHelper;
import com.odoo.orm.OModel;
import com.odoo.orm.ServerDataHelper;
import com.odoo.support.listview.OListAdapter;
import com.odoo.support.listview.OListAdapter.OnSearchChange;
import com.odoo.util.OControls;

public class SearchableItemActivity extends ActionBarActivity implements
		OnItemClickListener, TextWatcher, OnClickListener, OnSearchChange {

	private EditText edt_searchable_input;
	private ListView mList = null;
	private OListAdapter mAdapter;
	private List<Object> objects = new ArrayList<Object>();
	private int selected_position = -1;
	private Boolean mLiveSearch = false;
	private int resource_array_id = -1;
	private OModel mModel = null;
	private Integer mRowId = null;
	private LiveSearch mLiveDataLoader = null;
	private OColumn mCol = null;

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
				OModel rel_model = null;
				if (extra.containsKey("column_name")) {
					mCol = mModel.getColumn(extra.getString("column_name"));
					rel_model = mModel.createInstance(mCol.getType());
					objects.addAll(OSelectionField.getRecordItems(rel_model,
							mCol));
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
					if (row.contains(OColumn.ROW_ID)
							&& selected_position == row.getInt(OColumn.ROW_ID)) {
						v.setBackgroundColor(getResources().getColor(
								R.color.control_selection_selected));
					} else {
						v.setBackgroundColor(Color.TRANSPARENT);
					}
					return v;
				}
			};
			if (mLiveSearch) {
				mAdapter.setOnSearchChange(this);
			}
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
		if (!data.contains(OColumn.ROW_ID)) {
			mModel.getSyncHelper().quickStoreRecords(data);
			data.put(OColumn.ROW_ID, mModel.selectRowId(data.getInt("id")));
		}
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

	@Override
	public void onSearchChange(List<Object> newRecords) {
		if (newRecords.size() <= 0) {
			if (mLiveDataLoader != null)
				mLiveDataLoader.cancel(true);
			if (edt_searchable_input.getText().length() >= 3) {
				mLiveDataLoader = new LiveSearch();
				mLiveDataLoader.execute(edt_searchable_input.getText()
						.toString());
			}
		}
	}

	private class LiveSearch extends AsyncTask<String, Void, List<ODataRow>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
			mList.setVisibility(View.GONE);
		}

		@Override
		protected List<ODataRow> doInBackground(String... params) {
			try {
				Thread.sleep(300);
				ServerDataHelper helper = mModel.getSyncHelper().dataHelper();
				ODomain domain = new ODomain();
				domain.add("name", "ilike", params[0]);
				if (mCol != null) {
					for (String key : mCol.getDomains().keySet()) {
						ColumnDomain dom = mCol.getDomains().get(key);
						domain.add(dom.getColumn(), dom.getOperator(),
								dom.getValue());
					}
				}
				OFieldsHelper fields = new OFieldsHelper(mModel.getColumns());
				return helper.searchRecords(fields, domain, 10);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<ODataRow> result) {
			super.onPostExecute(result);
			findViewById(R.id.loading_progress).setVisibility(View.GONE);
			mList.setVisibility(View.VISIBLE);
			if (result != null && result.size() > 0) {
				objects.addAll(result);
				mAdapter.notifiyDataChange(objects);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			findViewById(R.id.loading_progress).setVisibility(View.GONE);
			mList.setVisibility(View.VISIBLE);
		}
	}

}
