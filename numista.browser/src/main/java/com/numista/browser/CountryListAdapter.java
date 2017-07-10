package com.numista.browser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class CountryListAdapter extends ArrayAdapter
{
	List<Data.Country> listCountries;

	public CountryListAdapter(Context context)
	{
		super(context, android.R.layout.simple_spinner_item);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		listCountries = new ArrayList<Data.Country>();
		listCountries.add(new Data.Country(0));
		listCountries.get(0).visual_name = context.getString(R.string.main_all);

		for (String Country : Data.Coins.keySet())
		{
			List<Data.Entry> list = Data.Coins.get(Country);
			listCountries.add(new Data.Country(Country, list.size()));
			listCountries.get(0).coin_count += list.size();
		}

		addAll(listCountries);
	}
}
