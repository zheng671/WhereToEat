package com.cse5236Group11.wheretoeat;

import com.cse5236Group11.wheretoeat.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	
    // flag for Internet connection status
    boolean isInternetPresent = false;
    
    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.homepage);
		View btnSpinner = this.findViewById(R.id.FindRestaurant);
		btnSpinner.setOnClickListener(this);
        View btnRecentFound = this.findViewById(R.id.RecentFound);
        btnRecentFound.setOnClickListener(this);
	}
	
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.FindRestaurant:
            	this.cd = new ConnectionDetector(this.getApplicationContext());
                // Check if Internet present
                this.isInternetPresent = this.cd.isConnectingToInternet();
                if (!this.isInternetPresent) {
                    // Internet Connection is not present
                    this.alert.showAlertDialog(MainActivity.this,
                            "Internet Connection Error",
                            "Please connect to working Internet connection", false);
                    // stop executing code by return
                    return;
                }
                else
	                this.startActivity(new Intent(this, Spinner.class));
	            break;
            case R.id.RecentFound:
                this.startActivity(new Intent(this, RecentFound.class));
                break;
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_settings:
	            Intent i = new Intent(this, Setting.class);
	            startActivity(i);
	            break;
        }
        return true;
    }
}
