package com.numista.browser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

public class JSONBasicFieldReflector
{
	public JSONBasicFieldReflector()
	{
	}

	public JSONBasicFieldReflector(JSONObject json)
	{
		Class<?> c = getClass();

		for (Field field : c.getFields())
		{
			Class<?> cf = field.getType();
			String tn = cf.getCanonicalName();
			if (tn.equals("int"))
			{
				try
				{
					field.setInt(this, json.optInt(field.getName(), field.getInt(this)));
				}
				catch (IllegalAccessException e) {}
			}
			else if (tn.equals("java.lang.String"))
			{
				try
				{
					Object v = field.get(this);
					field.set(this, json.optString(field.getName(), (v != null) ? v.toString() : ""));
				}
				catch (IllegalAccessException e) {}
			}
			else
			{
				try
				{
					Constructor constr = cf.getDeclaredConstructor(JSONObject.class);
					Object inst = constr.newInstance(json.optJSONObject(field.getName()));
					field.set(this, inst);
				}
				catch (Exception e)
				{
					String msg = e.getMessage();
				}
			}
		}
	}

	public void ParseList(JSONObject json, String field, Class<?> clazz)
	{
		ParseList(json, field, clazz, "");
	}

	public void ParseList(JSONObject json, String field, Class<?> clazz, String orderField)
	{
		try
		{
			String canName = clazz.getCanonicalName();
			ArrayList list = new ArrayList();

			JSONArray arr = json.optJSONArray(field);

			int index = 0;
			JSONObject year;
			while ((year = arr.optJSONObject(index++)) != null)
			{
				Constructor constr = clazz.getDeclaredConstructor(year.getClass());
				Object inst = constr.newInstance(year);

				if (orderField.length() > 0)
					clazz.getField(orderField).setInt(inst, index);

				list.add(inst);
			}

			getClass().getField(field).set(this, list);
		}
		catch (Exception e)
		{
			String msg = e.getMessage();
		}
	}

}
