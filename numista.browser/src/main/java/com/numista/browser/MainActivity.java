package com.numista.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity
		extends Activity
		implements AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener, ExpandableListView.OnChildClickListener
{

	Spinner spinCountry;
	EditText editYearFrom;
	EditText editYearTo;
	EditText editFilter;
	CheckBox cbInverse;
	ExpandableListView listItems;
	LinearLayout layoutFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		spinCountry = (Spinner) findViewById(R.id.spinCountry);
		editYearFrom = (EditText) findViewById(R.id.editYearFrom);
		editYearTo = (EditText) findViewById(R.id.editYearTo);
		editFilter = (EditText) findViewById(R.id.editFilter);
		cbInverse = (CheckBox) findViewById(R.id.cbInverse);
		listItems = (ExpandableListView) findViewById(R.id.listItems);
		layoutFilter = (LinearLayout) findViewById(R.id.filterLayout);

		spinCountry.setOnItemSelectedListener(this);
		editYearFrom.setOnEditorActionListener(this);
		editYearTo.setOnEditorActionListener(this);
		editFilter.setOnEditorActionListener(this);
		listItems.setOnChildClickListener(this);


		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheOnDisk(true)
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.defaultDisplayImageOptions(options)
				.build();

		ImageLoader.getInstance().init(config);

		Database.LoadDatabase(this);
		RebuildCountryList();
		RebuildList();

		layoutFilter.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.filter_coins:
			if (layoutFilter.getVisibility() == View.VISIBLE)
			{
				layoutFilter.setVisibility(View.GONE);
				View focus = getCurrentFocus();
				if (focus != null)
				{
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
				}
			}
			else
			{
				layoutFilter.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.download_coins:
			startActivityForResult(new Intent(this, DownloadActivity.class), 0);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			RebuildCountryList();
			RebuildList();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	void RebuildCountryList()
	{
		spinCountry.setAdapter(new CountryListAdapter(this));
	}

	private String CurrentCountry = "";

	private void RebuildList()
	{
		String strYearFrom = editYearFrom.getText().toString();
		String strYearTo = editYearTo.getText().toString();
		String strFilter = editFilter.getText().toString();
		Boolean bInverse = cbInverse.isChecked();

		int nYearFrom = 0, nYearTo = 9999;

		try
		{
			nYearFrom = Integer.parseInt(strYearFrom);
		}
		catch (NumberFormatException e) {}

		try
		{
			nYearTo = Integer.parseInt(strYearTo);
		}
		catch (NumberFormatException e) {}

		Data.FilterList(CurrentCountry, strFilter, bInverse, nYearFrom, nYearTo);

		listItems.setAdapter(new CoinAdapter(getLayoutInflater()));

		Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_filtered), Data.CoinsList.size()), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
	{
		if (adapterView == spinCountry)
		{
			Data.Country country = (Data.Country) spinCountry.getItemAtPosition(i);
			CurrentCountry = country.country_name;
			RebuildList();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView)
	{
		if (adapterView == spinCountry)
		{
			RebuildList();
		}
	}

	@Override
	public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
	{
		RebuildList();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

		return false;
	}

	public void onCopyYearClick(View view)
	{
		editYearTo.setText(editYearFrom.getText());
		RebuildList();
	}

	public void onClearYearClick(View view)
	{
		editYearFrom.setText("");
		editYearTo.setText("");
		RebuildList();
	}

	public void onInverseClick(View view)
	{
		RebuildList();
	}

	public void onClearFilterClick(View view)
	{
		editFilter.setText("");
		RebuildList();
	}

	@Override
	public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition,
	                            int childPosition, long l)
	{
		Intent intent = new Intent(this, CoinInfoActivity.class);
		intent.putExtra("Coin", groupPosition);
		startActivity(intent);

		return false;
	}
}
