package com.cse5236Group11.wheretoeat;

import java.util.ArrayList;
import java.util.HashMap;

import com.cse5236Group11.wheretoeat.R;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.app.Activity;
import android.view.View.OnClickListener;
import android.content.Intent;

/**
 * Create RecentFound Page
 * **/

public class RecentFound extends Activity implements OnClickListener  {
	public static ArrayList<HashMap<String,String>> RecentFoundList = new ArrayList<HashMap<String,String>>();
	
	private ListView listView ;
	private String currentRef,currentName,currentAddr;
	
	private AlertDialogManager alert = new AlertDialogManager();
	
    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_ADDRESS = "address"; // address of the place
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recentfound);
		
		// list view of recent found
		listView = (ListView) findViewById(R.id.list);
		
		// list adapter
		SimpleAdapter adapter = new SimpleAdapter(
				this,
				RecentFoundList,
				R.layout.recentfoundrow,
				new String[] {KEY_REFERENCE,KEY_ADDRESS,KEY_NAME},
				new int[] {R.id.reference,R.id.address,R.id.name}
				);

		listView.setAdapter(adapter);
		
		// on click listener for list view
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				currentRef = ((TextView) view.findViewById(R.id.reference)).getText().toString();
				currentName = ((TextView) view.findViewById(R.id.name)).getText().toString();
				currentAddr = ((TextView) view.findViewById(R.id.address)).getText().toString();
			}
		});
		
		View btnDirection = this.findViewById(R.id.moreInfoInRecentFound);
		btnDirection.setOnClickListener(this);
		View btnShare = this.findViewById(R.id.ShareInRecnetFound);
		btnShare.setOnClickListener(this);
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
	
    public void onClick(View v) {
    	// no item selected
    	if (currentRef == null)
            this.alert.showAlertDialog(RecentFound.this,
                    "No item selected",
                    "Please select one restaurant from below", false);
    	// store the name and addr of selected item
    	else{
	        switch (v.getId()) {
	            case R.id.ShareInRecnetFound:
	            	Intent ShareIntent = new Intent(this, Share.class);
	            	ShareIntent.putExtra(KEY_NAME, currentName);
	            	ShareIntent.putExtra(KEY_ADDRESS, currentAddr);
	            	this.startActivity(ShareIntent);
	                break;
	            case R.id.moreInfoInRecentFound:
	            	Intent MoreInfoIntent = new Intent(this, MoreInfo.class);
	            	MoreInfoIntent.putExtra(KEY_REFERENCE,currentRef);
	                this.startActivity(MoreInfoIntent);
	                break;
	        }
    	}
    }
}