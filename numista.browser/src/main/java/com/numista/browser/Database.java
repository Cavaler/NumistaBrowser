package com.numista.browser;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.numista.browser.Data;

public final class Database
{
	public static class Coins
	{
		public static final String TABLE_NAME = "coins";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_COUNTRY_NAME = "country_name";
		public static final String COLUMN_OBVERSE = "obverse";
		public static final String COLUMN_REVERSE = "reverse";
		public static final String COLUMN_KM = "km";
		public static final String COLUMN_ORDER = "corder";

		public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ COLUMN_ID + " INTEGER PRIMARY KEY, "
				+ COLUMN_NAME + " TEXT, "
				+ COLUMN_COUNTRY_NAME + " TEXT, "
				+ COLUMN_OBVERSE + " TEXT, "
				+ COLUMN_REVERSE + " TEXT, "
				+ COLUMN_KM + " TEXT, "
				+ COLUMN_ORDER + " INTEGER "
				+ ")";
		public static final String SQL_INSERT = "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)";
		public static final String SQL_SELECT = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ORDER + " ASC";
		public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	public static class Years
	{
		public static final String TABLE_NAME = "years";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_YEAR = "year";
		public static final String COLUMN_VG = "vg";
		public static final String COLUMN_F = "f";
		public static final String COLUMN_VF = "vf";
		public static final String COLUMN_XF = "xf";
		public static final String COLUMN_UNC = "unc";
		public static final String COLUMN_EXCH = "exch";

		public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ COLUMN_ID + " INTEGER, "
				+ COLUMN_YEAR + " TEXT, "
				+ COLUMN_VG + " INTEGER, "
				+ COLUMN_F + " INTEGER, "
				+ COLUMN_VF + " INTEGER, "
				+ COLUMN_XF + " INTEGER, "
				+ COLUMN_UNC + " INTEGER, "
				+ COLUMN_EXCH + " INTEGER "
				+ ")";
		public static final String SQL_INSERT = "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		public static final String SQL_SELECT = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=%d";
		public static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
		public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	public static int DATABASE_VERSION = 3;
	public static String DATABASE_NAME = "coins.db";

	public static class CoinsDbHelper extends SQLiteOpenHelper
	{
		public CoinsDbHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(Coins.SQL_CREATE);
			db.execSQL(Years.SQL_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL(Coins.SQL_DROP);
			db.execSQL(Years.SQL_DROP);

			onCreate(db);
		}
	}

	public static void LoadDatabase(Context ctx)
	{
		CoinsDbHelper DbHelper = new CoinsDbHelper(ctx);

		SQLiteDatabase db = DbHelper.getReadableDatabase();

		long started = System.currentTimeMillis();
		Log.i("DBRead", "Started reading database...");

		Cursor cc = db.rawQuery(Coins.SQL_SELECT, null);
		cc.moveToFirst();

		Map<Integer, Data.Entry> mapId = new HashMap<Integer, Data.Entry>();

		while (!cc.isAfterLast())
		{
			Data.Entry entry = new Data.Entry();

			entry.id = cc.getInt(0);
			entry.name = cc.getString(1);
			entry.country_name = cc.getString(2);
			entry.obverse = cc.getString(3);
			entry.reverse = cc.getString(4);
			entry.km = cc.getString(5);
			entry.years = new ArrayList<Data.YearEntry>();
			entry.order = cc.getInt(6);
			Data.AddEntry(entry);

			mapId.put(entry.id, entry);

			cc.moveToNext();
		}
		cc.close();

		Cursor cy = db.rawQuery(Years.SQL_SELECT_ALL, null);
		cy.moveToFirst();
		while (!cy.isAfterLast())
		{
			Data.YearEntry year = new Data.YearEntry();

			int id = cy.getInt(0);
			year.year = cy.getString(1);
			year.vg = cy.getInt(2);
			year.f = cy.getInt(3);
			year.vf = cy.getInt(4);
			year.xf = cy.getInt(5);
			year.unc = cy.getInt(6);
			year.exchange = cy.getInt(7);

			mapId.get(id).years.add(year);

			cy.moveToNext();
		}
		cy.close();

		long elapsed = System.currentTimeMillis() - started;
		Log.i("DBRead", "Finished reading database, took " + elapsed + " ms");

		DbHelper.close();
	}

	public static void ClearDatabase(Context ctx)
	{
		CoinsDbHelper DbHelper = new CoinsDbHelper(ctx);

		SQLiteDatabase db = DbHelper.getWritableDatabase();

		db.delete(Coins.TABLE_NAME, null, null);
		db.delete(Years.TABLE_NAME, null, null);

		DbHelper.close();
	}

	public static void SaveDatabase(Context ctx, Data.Response resp)
	{
		CoinsDbHelper DbHelper = new CoinsDbHelper(ctx);

		SQLiteDatabase db = DbHelper.getWritableDatabase();

		SQLiteStatement coinStmt = db.compileStatement(Coins.SQL_INSERT);
		SQLiteStatement yearStmt = db.compileStatement(Years.SQL_INSERT);

		db.beginTransaction();

		for (Data.Entry entry : resp.list)
		{
			coinStmt.bindLong(1, entry.id);
			coinStmt.bindString(2, entry.name);
			coinStmt.bindString(3, entry.country_name);
			coinStmt.bindString(4, entry.obverse);
			coinStmt.bindString(5, entry.reverse);
			coinStmt.bindString(6, entry.km);
			coinStmt.bindLong(7, entry.order);
			coinStmt.execute();

			for (Data.YearEntry year : entry.years)
			{
				yearStmt.bindLong(1, entry.id);
				yearStmt.bindString(2, year.year);
				yearStmt.bindLong(3, year.vg);
				yearStmt.bindLong(4, year.f);
				yearStmt.bindLong(5, year.vf);
				yearStmt.bindLong(6, year.xf);
				yearStmt.bindLong(7, year.unc);
				yearStmt.bindLong(8, year.exchange);
				yearStmt.execute();
			}
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		coinStmt.close();
		yearStmt.close();

		DbHelper.close();
	}
}
