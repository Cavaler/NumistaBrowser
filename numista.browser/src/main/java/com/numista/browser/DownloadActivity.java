package com.numista.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
	Button btnCancel;
	ProgressBar progressBar;
	CheckBox cbDebugging;

	boolean isRunning = false;
	Data.Response lastResponse = null;
	int nextPage = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_download);

		editID = (EditText) findViewById(R.id.editNumistaID);
		btnDownload = (Button) findViewById(R.id.btnDownload);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		progressBar = (ProgressBar) findViewById(R.id.barProgress);
		cbDebugging = (CheckBox) findViewById(R.id.cbDebugging);

		editID.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start,
			                              int count, int after) {}

			public void onTextChanged(CharSequence s, int start,
			                          int before, int count) {
				nextPage = 1;
				btnDownload.setText(getString(R.string.dl_download));
			}
		});

		SharedPreferences sharedPref = getSharedPreferences("prefs", MODE_PRIVATE);

		if (BuildConfig.DEBUG)
		{
			editID.setText(sharedPref.getString("ID", "37918"));
		}
		else
		{
			cbDebugging.setVisibility(View.GONE);
		}
	}

	public void onDebugClick(View view)
	{
		nextPage = 1;
		btnDownload.setText(getString(R.string.dl_download));
	}

	public void onStart()
	{
		isRunning = true;

		super.onStart();
	}

	public void onStop()
	{
		isRunning = false;

		SaveID();

		super.onStop();
	}

	void SaveID()
	{
		SharedPreferences sharedPref = getSharedPreferences("prefs", MODE_PRIVATE);
		SharedPreferences.Editor edit = sharedPref.edit();

		edit.putString("ID", editID.getText().toString());
		edit.commit();
	}

	public void onDownloadClick(View view)
	{
		SaveID();

		editID.setEnabled(false);
		btnDownload.setEnabled(false);
		btnDownload.setText(getString(R.string.dl_downloading));
		btnCancel.setEnabled(true);

		if (nextPage <= 1)
		{
			Data.Coins.clear();
			Database.ClearDatabase(this);
			nextPage = 1;
		}

		StartRetrieveJson(nextPage);
	}

	public void onCancelClick(View view)
	{
		task.cancel(true);
	}

	public void StartRetrieveJson(int page)
	{
		lastResponse = null;
		nextPage = page;

		int ID;
		try
		{
			ID = Integer.parseInt(editID.getText().toString());
		}
		catch (NumberFormatException e)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.toast_no_id), Toast.LENGTH_SHORT).show();
			return;
		}

		task = new DownloadTask();
		task.execute(ID, page, cbDebugging.isChecked() ? 1 : 0);
	}

	class DownloadTask extends AsyncTask<Integer, Void, String>
	{
		@Override
		protected String doInBackground(Integer... params)
		{
			int ID = params[0];
			int page = params[1];
			int debug = params[2];

			return RetrieveJson(ID, page, debug > 0);
		}

		@Override
		protected void onPostExecute(final String result)
		{
			super.onPostExecute(result);
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (lastResponse != null) OnJsonReceived(); else OnReceiveError(result);
				}
			});
		}

		@Override
		protected void onCancelled()
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					OnCancelled();
				}
			});
			super.onCancelled();
		}
	}

	DownloadTask task;

	public String RetrieveJson(int ID, int page, boolean debug)
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
		}
		catch (MalformedURLException e)
		{
			return e.getMessage();
		}

		StringBuilder result = new StringBuilder();
		try
		{
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setReadTimeout(10000);
			try
			{
				InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
				BufferedReader buffer = new BufferedReader(reader);

				String line;
				while ((line = buffer.readLine()) != null)
				{
					result.append(line);
				}
			}
			finally
			{
				urlConnection.disconnect();
			}
		}
		catch (java.net.SocketTimeoutException e)
		{
			return e.getMessage();
		}
		catch (IOException e)
		{
			return e.getMessage();
		}

		JSONObject json;
		try
		{
			json = new JSONObject(result.toString());
		}
		catch (JSONException e)
		{
			return e.getMessage();
		}

		lastResponse = new Data.Response(json);

		return "";
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

	public void OnReceiveError(String result)
	{
		OnCancelled();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Error");
		builder.setMessage(result);
		builder.setPositiveButton("OK", null);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void OnCancelled()
	{
		if (!isRunning) return;

		editID.setEnabled(true);
		btnDownload.setEnabled(true);
		btnDownload.setText(getString(nextPage <= 1 ? R.string.dl_download : R.string.dl_continue));
		btnCancel.setEnabled(false);
	}
}
