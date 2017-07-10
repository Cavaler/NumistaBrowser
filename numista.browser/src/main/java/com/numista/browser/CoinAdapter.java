package com.numista.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

class CoinAdapter extends BaseExpandableListAdapter
{
	private LayoutInflater layoutInflater;
	public CoinAdapter(LayoutInflater inflater)
	{
		layoutInflater = inflater;
	}

	public boolean hasStableIds()
	{
		return true;
	}

	public int getGroupCount()
	{
		return Data.CoinsList.size();
	}

	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	public Object getGroup(int groupPosition)
	{
		return Data.CoinsList.get(groupPosition);
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		if (convertView == null)
			convertView = layoutInflater.inflate(R.layout.group_item, null);

		TextView text = (TextView)convertView.findViewById(R.id.txtCoinName);
		text.setText(Data.CoinsList.get(groupPosition).TextView());

		return convertView;
	}

	public int getChildrenCount(int groupPosition)
	{
		return Data.CoinsList.get(groupPosition).years.size();
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return (groupPosition << 32) + childPosition;
	}

	public Object getChild(int groupPosition, int childPosition)
	{
		return Data.CoinsList.get(groupPosition).years.get(childPosition);
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		if (convertView == null)
			convertView = layoutInflater.inflate(R.layout.child_item, null);

		TextView text = (TextView)convertView.findViewById(R.id.txtYear);
		text.setText(Data.CoinsList.get(groupPosition).years.get(childPosition).TextView());

		return convertView;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
}
