package com.numista.browser;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;

public final class Data
{
	public static class Entry extends JSONBasicFieldReflector
	{
		public String name;
		public String obverse;
		public String reverse;
		public String km;
		public String country_name;
		public int id;
		public ArrayList<YearEntry> years;
		public int order;

		Entry()
		{
			years = new ArrayList<YearEntry>();
		}

		Entry(JSONObject json)
		{
			super(json);
			ParseList(json, "years", YearEntry.class);
		}

		public String TextView()
		{
			if (Data.CountrySelected)
				return name;
			else
				return country_name + " => " + name;
		}

		public boolean Filtered(String strFilterUC, Boolean bInverse, int nYearFrom, int nYearTo)
		{
			if (name.toUpperCase().contains(strFilterUC) == bInverse) return false;

			for (YearEntry year : years)
			{
				int nYear = year.Year();
				if ((nYear >= nYearFrom) && (nYear <= nYearTo)) return true;
			}

			return false;
		}
	}

	public static class Country
	{
		Country(String name, int count)
		{
			country_name = name;
			visual_name = name;
			coin_count = count;
		}

		Country(int count)
		{
			country_name = "";
			visual_name = "*";
			coin_count = count;
		}

		public String visual_name;
		public String country_name;
		public int coin_count;

		@Override
		public String toString()
		{
			return String.format("%s (%d)", visual_name, coin_count);
		}
	}

	public static class YearEntry extends JSONBasicFieldReflector
	{
		public String year;
		public int vg;
		public int f;
		public int vf;
		public int xf;
		public int unc;
		public int exchange;

		YearEntry()
		{
		}

		YearEntry(JSONObject json)
		{
			super(json);
		}

		public String TextView()
		{
			int count = vg + f + vf + xf + unc;
			if (exchange > 0)
				return String.format("%s - %d (%d)", year, count, exchange);
			else
				return String.format("%s - %d", year, count);
		}

		public int Year()
		{
			try
			{
				return Integer.parseInt(year);
			}
			catch (NumberFormatException e)
			{
			}

			Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(year);
			if (m.find())
			{
				return Integer.parseInt(m.group(1));
			}

			m = Pattern.compile("\\d+").matcher(year);
			if (m.find())
			{
				return Integer.parseInt(m.group());
			}

			return 0;
		}
	}

	public static class Pages extends JSONBasicFieldReflector
	{
		public int current;
		public int max;

		Pages()
		{
		}

		Pages(JSONObject json)
		{
			super(json);
		}
	}

	public static class Filter extends JSONBasicFieldReflector
	{
		public String country;

		Filter()
		{
		}

		Filter(JSONObject json)
		{
			super(json);
		}
	}

	public static class Response extends JSONBasicFieldReflector
	{
		public List<Entry> list;
		public Pages pages;
		public Filter filter;

		Response()
		{
			list = new ArrayList<Entry>();
			pages = new Pages();
			filter = new Filter();
		}

		Response(JSONObject json)
		{
			super(json);

			ParseList(json, "list", Entry.class, "order");
		}
	}

	public static Map<String, List<Entry>> Coins = new TreeMap<String, List<Entry>>();

	public static void AddEntry(Entry entry)
	{
		if (!Coins.containsKey(entry.country_name))
			Coins.put(entry.country_name, new ArrayList<Entry>());

		Coins.get(entry.country_name).add(entry);
	}

	public static boolean CountrySelected = false;
	public static List<Entry> CoinsList = new ArrayList<Entry>();

	public static void FilterList(String Country, String strFilter, Boolean bInverse, int nYearFrom, int nYearTo)
	{
		CoinsList.clear();
		String strFilterUC = strFilter.toUpperCase();

		long started = System.currentTimeMillis();
		Log.i("DBRead", "Started filtering list...");

		if (Country == "")
		{
			CountrySelected = false;
			for (List<Entry> list : Data.Coins.values())
			{
				for (Data.Entry coin : list)
				{
					if (coin.Filtered(strFilterUC, bInverse, nYearFrom, nYearTo))
						CoinsList.add(coin);
				}
			}
		}
		else
		{
			CountrySelected = true;
			if (Data.Coins.containsKey(Country))
			{
				for (Data.Entry coin : Data.Coins.get(Country))
				{
					if (coin.Filtered(strFilterUC, bInverse, nYearFrom, nYearTo))
						CoinsList.add(coin);
				}
			}
		}

		long elapsed = System.currentTimeMillis() - started;
		Log.i("DBRead", "Finished filtering list, took " + elapsed + " ms");
	}
}
