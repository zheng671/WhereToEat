package com.cse5236Group11.wheretoeat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

 /**
  * Create More Info page
  * */

public class MoreInfo extends Activity {
    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // Place Details
    PlaceDetails placeDetails;

    // Progress dialog
    ProgressDialog pDialog;

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.more_info);

        Intent i = this.getIntent();

        // Place referece id
        final String reference = i.getStringExtra(KEY_REFERENCE);

        // Calling a Async Background thread
        new LoadSinglePlaceDetails().execute(reference);

        View btnDirection = this.findViewById(R.id.Direction);
        // when direction button is pressed
        btnDirection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreInfo.this
                        .getApplicationContext(), Direction.class);
                intent.putExtra("name", MoreInfo.this.placeDetails.result.name);
                intent.putExtra("address",
                        MoreInfo.this.placeDetails.result.formatted_address);
                intent.putExtra("lat",
                        MoreInfo.this.placeDetails.result.geometry.location.lat);
                intent.putExtra("lng",
                        MoreInfo.this.placeDetails.result.geometry.location.lng);

                MoreInfo.this.startActivity(intent);
            }
        });
        
        // when share button is called
        View btnShare = this.findViewById(R.id.Share);
        btnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                Intent intent = new Intent(MoreInfo.this
                        .getApplicationContext(), Share.class);
                intent.putExtra("name", ((TextView) parent
                        .findViewById(R.id.name)).getText().toString());
                intent.putExtra("address", ((TextView) parent
                        .findViewById(R.id.address)).getText().toString());
                MoreInfo.this.startActivity(intent);
            }
        });
    }

    /**
     * Background Async Task to Load Google places
     * */
    class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MoreInfo.this.pDialog = new ProgressDialog(MoreInfo.this);
            MoreInfo.this.pDialog.setMessage("Loading profile ...");
            MoreInfo.this.pDialog.setIndeterminate(false);
            MoreInfo.this.pDialog.setCancelable(false);
            MoreInfo.this.pDialog.show();
        }

        /**
         * getting Profile JSON
         * */
        @Override
        protected String doInBackground(String... args) {
            String reference = args[0];

            // creating Places class object
            MoreInfo.this.googlePlaces = new GooglePlaces();

            // Check if used is connected to Internet
            try {
                MoreInfo.this.placeDetails = MoreInfo.this.googlePlaces
                        .getPlaceDetails(reference);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            MoreInfo.this.pDialog.dismiss();
            // updating UI from Background Thread
            MoreInfo.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    if (MoreInfo.this.placeDetails != null) {
                        String status = MoreInfo.this.placeDetails.status;

                        // check place deatils status
                        // Check for all possible status
                        if (status.equals("OK")) {
                            if (MoreInfo.this.placeDetails.result != null) {
                                String name = MoreInfo.this.placeDetails.result.name;
                                String address = MoreInfo.this.placeDetails.result.formatted_address;
                                String phone = MoreInfo.this.placeDetails.result.formatted_phone_number;
                                String latitude = Double
                                        .toString(MoreInfo.this.placeDetails.result.geometry.location.lat);
                                String longitude = Double
                                        .toString(MoreInfo.this.placeDetails.result.geometry.location.lng);

                                Log.d("Place ", name + address + phone
                                        + latitude + longitude);

                                // Displaying all the details in the view
                                // single_place.xml
                                TextView lbl_name = (TextView) MoreInfo.this
                                        .findViewById(R.id.name);
                                TextView lbl_address = (TextView) MoreInfo.this
                                        .findViewById(R.id.address);
                                TextView lbl_phone = (TextView) MoreInfo.this
                                        .findViewById(R.id.phone);

                                // Check for null data from google
                                // Sometimes place details might missing
                                name = name == null ? "Not present" : name;                                         
                                address = address == null ? "Not present" : address;
                                phone = phone == null ? "Not present" : phone;
                                latitude = latitude == null ? "Not present" : latitude;
                                longitude = longitude == null ? "Not present" : longitude;

                                // assign value to view
                                lbl_name.setText(name);
                                lbl_address.setText(address);
                                lbl_phone.setText(Html
                                        .fromHtml("<b>Phone:</b> " + phone));

                            }
                        } else if (status.equals("ZERO_RESULTS")) {
                            MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                    "Near Places", "Sorry no place found.",
                                    false);
                        } else if (status.equals("UNKNOWN_ERROR")) {
                            MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                    "Places Error",
                                    "Sorry unknown error occured.", false);
                        } else if (status.equals("OVER_QUERY_LIMIT")) {
                            MoreInfo.this.alert
                                    .showAlertDialog(
                                            MoreInfo.this,
                                            "Places Error",
                                            "Sorry query limit to google places is reached",
                                            false);
                        } else if (status.equals("REQUEST_DENIED")) {
                            MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                    "Places Error",
                                    "Sorry error occured. Request is denied",
                                    false);
                        } else if (status.equals("INVALID_REQUEST")) {
                            MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                    "Places Error",
                                    "Sorry error occured. Invalid Request",
                                    false);
                        } else {
                            MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                    "Places Error", "Sorry error occured.",
                                    false);
                        }
                    } else {
                        MoreInfo.this.alert.showAlertDialog(MoreInfo.this,
                                "Places Error", "Sorry error occured.", false);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, Setting.class);
                this.startActivity(i);
                break;
        }
        return true;
    }
}
