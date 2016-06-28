package com.numista.browser;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadActivity extends Activity
{
	EditText editID;
	Button btnDownload;
	ProgressBar progressBar;
	CheckBox cbDebugging;

	boolean isRunning = false;
	Data.Response lastResponse = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_download);

		editID = (EditText)findViewById(R.id.editNumistaID);
		btnDownload = (Button)findViewById(R.id.btnDownload);
		progressBar = (ProgressBar)findViewById(R.id.barProgress);
		cbDebugging = (CheckBox)findViewById(R.id.cbDebugging);

		SharedPreferences sharedPref = getSharedPreferences("prefs", MODE_PRIVATE);
		editID.setText(sharedPref.getString("ID", "37918"));
	}


	public void onStart()
	{
		isRunning = true;

		super.onStart();
	}

	public void onStop()
	{
		isRunning = false;

		SharedPreferences sharedPref = getSharedPreferences("prefs", MODE_PRIVATE);
		SharedPreferences.Editor edit = sharedPref.edit();

		edit.putString("ID", editID.getText().toString());
		edit.commit();

		super.onStop();
	}

	public void onDownloadClick(View view)
	{
		Data.Coins.clear();

		editID.setEnabled(false);
		btnDownload.setEnabled(false);
		btnDownload.setText(getString(R.string.dl_downloading));

		Database.ClearDatabase(this);
		StartRetrieveJson(1);
	}

	public void StartRetrieveJson(int page)
	{
		lastResponse = null;

		int ID;
		try
		{
			ID = Integer.parseInt(editID.getText().toString());
		} catch(NumberFormatException e)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.toast_no_id), Toast.LENGTH_SHORT).show();
			return;
		}

		task = new DownloadTask();
		task.execute(ID, page, cbDebugging.isChecked() ? 1 : 0);
	}

	class DownloadTask extends AsyncTask<Integer, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Integer... params)
		{
			int ID = params[0];
			int page = params[1];
			int debug = params[2];

			return RetrieveJson(ID, page, debug > 0);
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			runOnUiThread(new Runnable() {
				@Override
				public void run() { OnJsonReceived(); }
			});
		}
	}
	DownloadTask task;

	public boolean RetrieveJson(int ID, int page, boolean debug)
	{
		lastResponse = null;

		String strURL;
		if (debug)
			strURL = String.format("http://qmegas.info/numista-api/user/%d/collection/?filter_country=france&page=%d", ID, page);
		else
			strURL = String.format("http://qmegas.info/numista-api/user/%d/collection/?page=%d", ID, page);

		URL url;
		try
		{
			url = new URL(strURL);
		} catch (MalformedURLException e)
		{
			return false;
		}

		StringBuilder result = new StringBuilder();
		try
		{
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
			BufferedReader buffer = new BufferedReader(reader);

			String line;
			while ((line = buffer.readLine()) != null)
			{
				result.append(line);
			}
		} catch (IOException e)
		{
			return false;
		}

		JSONObject json;
		try
		{
			json = new JSONObject(result.toString());
		} catch (JSONException e)
		{
			return false;
		}

		lastResponse = new Data.Response(json);

		return true;
	}

	public void OnJsonReceived()
	{
		if (!isRunning) return;
		if (lastResponse == null) return;

		progressBar.setMax(lastResponse.pages.max);
		progressBar.setProgress(lastResponse.pages.current);

		btnDownload.setText(String.format(getString(R.string.dl_downloading_of), lastResponse.pages.current, lastResponse.pages.max));

		for (Data.Entry entry : lastResponse.list)
			Data.AddEntry(entry);
		Database.SaveDatabase(this, lastResponse);

		if (lastResponse.pages.current < lastResponse.pages.max)
		{
			StartRetrieveJson(lastResponse.pages.current + 1);
		}
		else
		{
			setResult(RESULT_OK, null);
			finish();
		}
	}
}
