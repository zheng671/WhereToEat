package com.cse5236Group11.wheretoeat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Direction extends MapActivity {
    // Nearest places
    PlacesList nearPlaces;

    // Map view
    MapView mapView;

    // Map overlay items
    List<Overlay> mapOverlays;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    AddItemizedOverlay itemizedOverlay;

    GeoPoint geoPoint;
    // Map controllers
    MapController mc;

    double latitude;
    double longitude;
    String name;
    String address;

    OverlayItem overlayitem;
    GPSTracker gps;
    PlaceDetails placeDetail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.direction);

        // Getting intent data
        Intent i = this.getIntent();

        // Nearplaces list

        this.latitude = i.getDoubleExtra("lat", 0.0);
        this.longitude = i.getDoubleExtra("lng", 0.0);
        this.name = i.getStringExtra("name");
        this.address = i.getStringExtra("address");

        this.mapView = (MapView) this.findViewById(R.id.mapView);
        this.mapView.setBuiltInZoomControls(true);

        this.mapOverlays = this.mapView.getOverlays();
        this.gps = new GPSTracker(this);

        double user_latitude, user_longitude;

        if (this.gps.canGetLocation()) {
            user_latitude = this.gps.getLatitude();
            user_longitude = this.gps.getLongitude();
        } else {
            // Can't get user's current location
            this.alert.showAlertDialog(Direction.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS",
                    false);
            // stop executing code by return
            return;
        }

        SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(this.getBaseContext());
        String PrefStreetAddress = SP.getString("PrefStreetAddress", "");
        String PrefCity = SP.getString("PrefCity", "");
        String PrefState = SP.getString("PrefState", "");
        boolean PrefCurrentLocation = SP
                .getBoolean("PrefCurrentLocation", true);

        String startAddress = "";

        if (PrefCurrentLocation == false) {
            if (PrefStreetAddress == "") {
                this.alert.showAlertDialog(Direction.this,
                        "Missing Street Address",
                        "Please enter the Street Address", false);
            } else if (PrefCity == "") {
                this.alert.showAlertDialog(Direction.this, "Missing City",
                        "Please enter the City", false);
            } else if (PrefState == "") {
                this.alert.showAlertDialog(Direction.this,
                        "Missing State Address", "Please enter the State",
                        false);
            } else {
                startAddress = PrefStreetAddress + "+" + PrefCity + "+"
                        + PrefState;
                startAddress = startAddress.replaceAll("\\s", "+");
            }
        }

        /*
         * get the startAddress from setting
         */

        // Geopoint to place on map

        this.geoPoint = new GeoPoint((int) (this.gps.getLatitude() * 1e6),
                (int) (this.gps.getLongitude() * 1e6));

        // Drawable marker icon
        Drawable drawable_user = this.getResources().getDrawable(
                R.drawable.mark_red);

        this.itemizedOverlay = new AddItemizedOverlay(drawable_user, this);

        // Map overlay item
        this.overlayitem = new OverlayItem(this.geoPoint, "Your Location",
                "That is you!");

        this.itemizedOverlay.addOverlay(this.overlayitem);

        this.mapOverlays.add(this.itemizedOverlay);
        this.itemizedOverlay.populateNow();

        // Drawable marker icon
        Drawable drawable = this.getResources().getDrawable(
                R.drawable.mark_blue);

        this.itemizedOverlay = new AddItemizedOverlay(drawable, this);

        this.mc = this.mapView.getController();

        // check whether we get lat and lng from spinner or moreinfo
        if (this.latitude != 0.0 & this.longitude != 0.0) {

            // Geopoint to place on map
            this.geoPoint = new GeoPoint((int) (this.latitude * 1E6),
                    (int) (this.longitude * 1E6));

            // Map overlay item
            this.overlayitem = new OverlayItem(this.geoPoint, this.name,
                    this.address);

            this.itemizedOverlay.addOverlay(this.overlayitem);

            this.mapOverlays.add(this.itemizedOverlay);

            // showing overlay item
            this.itemizedOverlay.populateNow();
        }

        
        this.mc.setZoom(18);
        this.mc.animateTo(this.geoPoint);
        String lat = Double.toString(this.latitude);
        String lon = Double.toString(this.longitude);

        /*
         * String user = " ";
         */
        String des = lat + "," + lon;
        if (startAddress == "") {
            // start from user
            startAddress = Double.toString(user_latitude) + ","
                    + Double.toString(user_longitude);
        }
       

        new GoogleDirection().execute(startAddress, des);
        this.mapView.postInvalidate();

    }

    public static JSONObject getLocationInfo(String address) {

        HttpGet httpGet = new HttpGet("http://maps.google."
                + "com/maps/api/geocode/json?address=" + address
                + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    // After executing this, another method converts that JSONObject into a
    // GeoPoint.

    public static GeoPoint getGeoPoint(JSONObject jsonObject) {

        Double lon = new Double(0);
        Double lat = new Double(0);

        try {

            lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
    }

    private class GoogleDirection extends
            AsyncTask<String, Integer, List<GeoPoint>> {
        private final List<GeoPoint> _points = new ArrayList<GeoPoint>();

        // gefei

        @Override
        protected List<GeoPoint> doInBackground(String... params) {
            if (params.length < 0) {
                return null;
            }
            String start = params[0];

            String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
                    + start + "&destination=" + params[1] + "&sensor=false";
            Log.i("map", url);
            HttpGet get = new HttpGet(url);
            String strResult = "";
            try {
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);

                HttpResponse httpResponse = null;
                httpResponse = httpClient.execute(get);

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    strResult = EntityUtils.toString(httpResponse.getEntity());

                    JSONObject jsonObjectD = new JSONObject(strResult);
                    JSONArray routeObject = jsonObjectD.getJSONArray("routes");
                    String polyline = routeObject.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    if (polyline.length() > 0) {
                        this.decodePolylines(polyline);

                    }

                }
            } catch (Exception e) {
                Log.e("map", e.toString());
            }
            return this._points;
        }

        private void decodePolylines(String poly) {
            int len = poly.length();
            int index = 0;
            int lat = 0;
            int lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                GeoPoint p = new GeoPoint((int) ((lat / 1E5) * 1E6),
                        (int) ((lng / 1E5) * 1E6));
                this._points.add(p);

            }
        }

        @Override
        protected void onPostExecute(List<GeoPoint> points) {
            if (points.size() > 0) {
           
                Direction.this.mapView.getOverlays().add(new MyOverLay(points));

            }
        }

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

}
