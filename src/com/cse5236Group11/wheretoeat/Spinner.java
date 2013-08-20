package com.cse5236Group11.wheretoeat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Spinner extends Activity {
 
    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlacesList nearPlaces;

    // GPS Location
    GPSTracker gps;

    // GPS location saved so that gps does not have to be called multiple times
    double latitude, longitude;

    // Button
    Button btnShowOnMap, btnRandom, btnMoreInfo, btnChooseRest;

    // Progress dialog
    ProgressDialog pDialog;

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    public static String KEY_ADDRESS = "address"; // formatted address
    public static String KEY_LAT = "latitude"; // location-latitude
    public static String KEY_LNG = "longitude"; // location-longitude

    private int position = 0; 

    TextView text, reference, address;
    long starttime = 0;
    // this posts a message to the main thread from our timertask
    // and updates the textfield
    final Handler h = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (Spinner.this.placesListItems.size() > 0) {
                long millis = System.currentTimeMillis()
                        - Spinner.this.starttime;
                int seconds = (int) (millis / 50);
                seconds = seconds % Spinner.this.placesListItems.size();

                Random rand = new Random();
                int r = rand.nextInt(255);
                int g = rand.nextInt(255);
                int b = rand.nextInt(255);

                Spinner.this.position = seconds;
                Spinner.this.text.setText(Spinner.this.placesListItems.get(
                        seconds).get(KEY_NAME));
                Spinner.this.reference.setText(Spinner.this.placesListItems
                        .get(seconds).get(KEY_REFERENCE));
             
                Spinner.this.text.setTextColor(Color.rgb(r, g, b));
            }
            return false;
        }
    });

    // tells handler to send a message
    class firstTask extends TimerTask {
        @Override
        public void run() {
            Spinner.this.h.sendEmptyMessage(0);
        }
    };

    Timer timer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.spinner);

        this.cd = new ConnectionDetector(this.getApplicationContext());

        // Check if Internet present
        this.isInternetPresent = this.cd.isConnectingToInternet();
        if (!this.isInternetPresent) {
            // Internet Connection is not present
            this.alert.showAlertDialog(Spinner.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // creating GPS Class object
        this.gps = new GPSTracker(this);

        // check if GPS location can get
        if (this.gps.canGetLocation()) {
            this.latitude = this.gps.getLatitude();
            this.longitude = this.gps.getLongitude();
            Log.d("Your Location", "latitude:" + this.gps.getLatitude()
                    + ", longitude: " + this.gps.getLongitude());

        } else {
            // Can't get user's current location
            this.alert.showAlertDialog(Spinner.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS",
                    false);
            // stop executing code by return
            return;
        }

        // Getting listview

        // button show on map
        this.btnShowOnMap = (Button) this.findViewById(R.id.btn_show_map);
        this.btnMoreInfo = (Button) this.findViewById(R.id.MoreInfoInSpinner);
        this.btnChooseRest = (Button) this.findViewById(R.id.ChooseRest);

        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        // new LoadPlaces().execute();

        // Find all textView field
        this.text = (TextView) this.findViewById(R.id.text);
        this.reference = (TextView) this.findViewById(R.id.reference);
        this.address = (TextView) this.findViewById(R.id.address);

        this.btnRandom = (Button) this.findViewById(R.id.Random);

        // set OnCLickListener for Random Button
        this.btnRandom.setText("Randomize");
        this.btnRandom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Button b = (Button) view;
                if (b.getText().equals("Select Restaurant")) {

                    Spinner.this.timer.cancel();
                    Spinner.this.timer.purge();
                    b.setText("Randomize");
                } else {
                    Spinner.this.starttime = System.currentTimeMillis();
                    Spinner.this.timer = new Timer();
                    Spinner.this.timer.schedule(new firstTask(), 0, 20);
                    b.setText("Select Restaurant");
                }
            }
        });

        /** Button click event for shown on map */
        this.btnShowOnMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Spinner.this.getApplicationContext(),
                        PlacesMapActivity.class);
                // Sending user current geo location
                i.putExtra("user_latitude",
                        Double.toString(Spinner.this.latitude));
                i.putExtra("user_longitude",
                        Double.toString(Spinner.this.longitude));

                // passing near places to map activity
                i.putExtra("near_places", Spinner.this.nearPlaces);
                // staring activity
                Spinner.this.startActivity(i);
            }
        });

        this.btnMoreInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                HashMap<String, String> map = new HashMap<String, String>();
                Intent i = new Intent(Spinner.this.getApplicationContext(),
                        MoreInfo.class);
                i.putExtra(KEY_REFERENCE, ((TextView) parent
                        .findViewById(R.id.reference)).getText().toString());
                map.put(KEY_REFERENCE, ((TextView) parent
                        .findViewById(R.id.reference)).getText().toString());
                map.put(KEY_NAME, ((TextView) parent.findViewById(R.id.text))
                        .getText().toString());
                map.put(KEY_ADDRESS, ((TextView) parent
                        .findViewById(R.id.address)).getText().toString());
                RecentFound.RecentFoundList.add(map);
                Spinner.this.startActivity(i);
            }
        });

        this.btnChooseRest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        Spinner.this.getApplicationContext(), Direction.class);

                intent.putExtra(
                        "lat",
                        Double.parseDouble(Spinner.this.placesListItems.get(
                                Spinner.this.position).get(KEY_LAT)));

                intent.putExtra(
                        "lng",
                        Double.parseDouble(Spinner.this.placesListItems.get(
                                Spinner.this.position).get(KEY_LNG)));
                intent.putExtra("address",
                        Spinner.this.placesListItems.get(Spinner.this.position)
                                .get(KEY_ADDRESS));
                intent.putExtra("name",
                        Spinner.this.placesListItems.get(Spinner.this.position)
                                .get(KEY_NAME));

                Spinner.this.startActivity(intent);
               
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadPlaces().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.pDialog.isShowing()) {
            this.pDialog.dismiss();
        }
    }

    /**
     * Background Async Task to Load Google places
     * */
    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Spinner.this.pDialog = new ProgressDialog(Spinner.this);
            Spinner.this.pDialog.setMessage(Html
                    .fromHtml("<b>Search</b><br/>Loading Places..."));
            Spinner.this.pDialog.setIndeterminate(false);
            Spinner.this.pDialog.setCancelable(false);
            Spinner.this.pDialog.show();
        }

        /**
         * getting Places JSON
         * */
        @Override
        protected String doInBackground(String... args) {
            // creating Places class object
            Spinner.this.googlePlaces = new GooglePlaces();

            try {
                
                String types = "restaurant"; // Listing places only cafes,
                                             // restaurants

                // Radius in meters - increase this value if you don't find any
                // places
                SharedPreferences SP = PreferenceManager
                        .getDefaultSharedPreferences(Spinner.this
                                .getBaseContext());
                double radius = Double.parseDouble(SP.getString("PrefRadius",
                        "1000")); // 1000 meters

                // get nearest places
                Spinner.this.nearPlaces = Spinner.this.googlePlaces.search(
                        Spinner.this.latitude, Spinner.this.longitude, radius,
                        types);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            Spinner.this.pDialog.dismiss();
            // updating UI from Background Thread
            Spinner.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = Spinner.this.nearPlaces.status;

                    // Check for all possible status
                    if (status.equals("OK")) {
                        // Successfully got places details
                        if (Spinner.this.nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : Spinner.this.nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                // Place reference won't display in listview -
                                // it will be hidden
                                // Place reference is used to get
                                // "place full details"
                                map.put(KEY_REFERENCE, p.reference);

                                // Place name
                                map.put(KEY_NAME, p.name);

                                map.put(KEY_ADDRESS, p.formatted_address);
                                map.put(KEY_LAT, Double
                                        .toString(p.geometry.location.lat));
                                map.put(KEY_LNG, Double
                                        .toString(p.geometry.location.lng));

                                // adding HashMap to ArrayList
                                Spinner.this.placesListItems.add(map);
                            }
                        }
                    } else if (status.equals("ZERO_RESULTS")) {
                        // Zero results found
                        Spinner.this.alert
                                .showAlertDialog(
                                        Spinner.this,
                                        "Near Places",
                                        "Sorry no places found. Try to change the types of places",
                                        false);
                    } else if (status.equals("UNKNOWN_ERROR")) {
                        Spinner.this.alert.showAlertDialog(Spinner.this,
                                "Places Error", "Sorry unknown error occured.",
                                false);
                    } else if (status.equals("OVER_QUERY_LIMIT")) {
                        Spinner.this.alert
                                .showAlertDialog(
                                        Spinner.this,
                                        "Places Error",
                                        "Sorry query limit to google places is reached",
                                        false);
                    } else if (status.equals("REQUEST_DENIED")) {
                        Spinner.this.alert
                                .showAlertDialog(
                                        Spinner.this,
                                        "Places Error",
                                        "Sorry error occured. Request is denied",
                                        false);
                    } else if (status.equals("INVALID_REQUEST")) {
                        Spinner.this.alert.showAlertDialog(Spinner.this,
                                "Places Error",
                                "Sorry error occured. Invalid Request", false);
                    } else {
                        Spinner.this.alert.showAlertDialog(Spinner.this,
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