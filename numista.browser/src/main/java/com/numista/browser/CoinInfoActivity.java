package com.numista.browser;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;


public class CoinInfoActivity extends Activity
{
	Data.Entry coin;

	TextView txtCountryName;
	TextView txtCoinName;
	TextView txtKM;
	ImageView imgObverse;
	ImageView imgReverse;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coin_info);

		int index = getIntent().getIntExtra("Coin", 0);
		coin = Data.CoinsList.get(index);

		txtCountryName = (TextView) findViewById(R.id.txtCountry);
		txtCoinName = (TextView) findViewById(R.id.txtCoinName);
		txtKM = (TextView) findViewById(R.id.txtKM);
		imgObverse = (ImageView) findViewById(R.id.imgObverse);
		imgReverse = (ImageView) findViewById(R.id.imgReverse);

		txtCountryName.setText(coin.country_name);
		txtCoinName.setText(coin.name);
		txtKM.setText(coin.km);

		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(coin.obverse, imgObverse);
		imageLoader.displayImage(coin.reverse, imgReverse);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_coin_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
