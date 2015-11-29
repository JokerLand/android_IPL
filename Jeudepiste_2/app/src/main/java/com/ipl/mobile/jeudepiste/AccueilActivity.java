package com.ipl.mobile.jeudepiste;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ipl.mobile.jeudepiste.util.ChargerXML;
import com.ipl.mobile.jeudepiste.util.Util;

import java.util.HashMap;

public class AccueilActivity extends Activity {
    private static final int START_GEO = 1;
    private static final int STOP_GEO = 2;
    private static final int CHANGE_TIME_REFRESH_GEO = 3;
    private int etat;
    public static final int COMMENCE = 1;
    public static final int REPRENDRE = 2;
    public static final int EN_COURS = 3;
    private WebView webview;
    private SharedPreferences settings;
    public static Location bestLocation;
    private ChargerXML myXml;
    private LocationManager lm;
    private LocationListener ll;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        //charger les préférence
        //TODO
        myXml = new ChargerXML(this);
        settings = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
        int numEtape = settings.getInt(Util.etape, 0);
        int numEpreuve = settings.getInt(Util.epreuve, 0);

        if (numEtape == 0) {
            etat = COMMENCE;
        }

        faireLaLocalisation(0,START_GEO);

        final Button button = (Button) findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        webview = (WebView) findViewById(R.id.webviewAccueil);
        webview.setVisibility(View.INVISIBLE);
        webview.setWebViewClient(new WebViewClient() {
            // you tell the webclient you want to catch when a url is about to load
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            // here you execute an action when the URL you want is about to load
            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.equals(myXml.getEpreuvePreference().get(Util.uri) + "/")) {
                    Intent intent = new Intent(AccueilActivity.this, EpreuveActivity.class);
                    startActivity(intent);

                }

            }
        });

        if (etat == REPRENDRE) {
            button.setText("REPRENDRE");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setVisibility(View.INVISIBLE);
                    etat = EN_COURS;
                    reprendre();
                }

            });

        } else if (etat == COMMENCE) {
            button.setText("Commencer");
            button.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    button.setVisibility(View.INVISIBLE);
                    etat = EN_COURS;
                    chargerWebView();

                }
            });
        }


    }

    private void reprendre() {
        int numEtape = settings.getInt(Util.etape, 0);
        int numEpreuve = settings.getInt(Util.epreuve, 0);

        //Normalement, numEtape et numEpreuve sont différent de 0
        if (numEtape == 0 && numEpreuve == 0) {
            chargerWebView();
            return;
        }


    }


    //final Button buttonRep = (Button) findViewById(R.id.buttonRep);
    //button.setVisibility(View.INVISIBLE);


    @TargetApi(Build.VERSION_CODES.M)
    private void faireLaLocalisation(long time, int mode) {

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ll == null) ll = new MyLocationListener();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            Toast.makeText(AccueilActivity.this, "Meeeeeerdeeeuuux", Toast.LENGTH_LONG).show(); //TODO message erreur correct


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
               /* ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);*/
                Toast.makeText(AccueilActivity.this, "Bouuuuhhhh", Toast.LENGTH_LONG).show(); //TODO message erreur correct
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            }

            return;
        }


        switch (mode) {
            case START_GEO:
                Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    bestLocation = lastKnownLocation;
                }
                if (time >= 0) { // Valeur par défaut

                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, Util.THIRTY_SECONDS, 5, ll);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Util.THIRTY_SECONDS, 5, ll);
                } else {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 5, ll);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 5, ll);
                }

                break;
            case STOP_GEO:
                lm.removeUpdates(ll);

                break;
            case CHANGE_TIME_REFRESH_GEO:
                lm.removeUpdates(ll);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 5, ll);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 5, ll);

                break;

            default : break;
        }

        Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            bestLocation = lastKnownLocation;
        }


    }


    private void chargerWebView() {

        String myUrl = (String) myXml.getEtape(settings.getInt(Util.etape, 1)).get(Util.url);
        webview.setVisibility(View.VISIBLE);
        webview.loadUrl(myUrl);


    }

    @Override
    protected void onResume() {
        super.onResume();
        faireLaLocalisation(Util.THIRTY_SECONDS,CHANGE_TIME_REFRESH_GEO);
        verifLocation();
    }

    private void verifLocation() {

        if (etat != EN_COURS) return;
        Button b = (Button) findViewById(R.id.button);
        TextView t = (TextView) findViewById(R.id.textViewZone);

        HashMap<String, Object> zone = (HashMap) myXml.getEtape(settings.getInt(Util.etape, 1)).get(Util.zone);
        Location loc1 = new Location("");
        double lat = (double) zone.get(Util.latitude);
        double lon = (double) zone.get(Util.longitude);
        loc1.setLatitude(lat);
        loc1.setLongitude(lon);
        if (bestLocation != null) {
            if (loc1.distanceTo(bestLocation) <= (int) zone.get(Util.rayon)) {
                t.setVisibility(View.INVISIBLE);
                webview.setVisibility(View.VISIBLE);
                b.setEnabled(true);
                webview = (WebView) findViewById(R.id.webviewAccueil);
                webview.loadUrl((String) myXml.getEtape(settings.getInt(Util.etape, 1)).get(Util.url));
            } else {

                t.setVisibility(View.VISIBLE);
                webview.setVisibility(View.INVISIBLE);
                b.setEnabled(false);

                t.setText("Vous n'êtes pas dans la zone de l'étape. Dirigez-vous vers les coordonnées suivantes : Latitude : " + lat + " Longitude : " + lon);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        faireLaLocalisation(Util.FIVE_MINUTES*2,CHANGE_TIME_REFRESH_GEO);


    }

    @Override
    protected void onStop() {
        super.onStop();
        faireLaLocalisation(0,STOP_GEO);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {


            if (Util.isBetterLocation(location, bestLocation)) bestLocation = location;
            verifLocation();
        }

        @Override
        public void onProviderDisabled(String provider) {
            String msg = String.format(
                    getResources().getString(
                            R.string.app_geoloc_provider_disabled), provider);
            Toast.makeText(AccueilActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            String msg = String.format(
                    getResources().getString(
                            R.string.app_geoloc_provider_enabled), provider);
            Toast.makeText(AccueilActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String newStatus = "";

            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    newStatus = "OUT_OF_SERVICE";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    newStatus = "TEMPORARILY_UNAVAILABLE";
                    break;
                case LocationProvider.AVAILABLE:
                    newStatus = "AVAILABLE";
                    break;
            }

            String msg = String.format(
                    getResources().getString(
                            R.string.app_geoloc_provider_new_status), provider,
                    newStatus);
            Toast.makeText(AccueilActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    faireLaLocalisation(0, START_GEO);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
