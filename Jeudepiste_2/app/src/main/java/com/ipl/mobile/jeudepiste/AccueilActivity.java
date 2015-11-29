/*
 * Copyright (c) <2015> <Institut Paul Lambin>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


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
import android.os.CountDownTimer;
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

    private CountDownTimer timer;

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

        myXml = new ChargerXML(this);
        settings = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
        int numEtape = settings.getInt(Util.etape, 0);
        int numEpreuve = settings.getInt(Util.epreuve, 0);

        //gestion du timer
        timer = getTimer();

        if (numEtape == 0 && numEpreuve == 0) {
            etat = COMMENCE;
        } else {
            etat = REPRENDRE;
        }

        faireLaLocalisation(0, START_GEO);

        final Button button = (Button) findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);

        final Button partage  = (Button) findViewById(R.id.partageId);
        partage.setVisibility(View.VISIBLE);
        webview = (WebView) findViewById(R.id.webviewAccueil);
        webview.setVisibility(View.INVISIBLE);

        if (etat == REPRENDRE) {
            button.setText("REPRENDRE");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setVisibility(View.INVISIBLE);
                    partage.setVisibility(View.INVISIBLE);
                    etat = EN_COURS;
                    chargerWebView();
                }

            });

        } else if (etat == COMMENCE) {
            button.setText("Commencer");
            button.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    timer.start();
                    findViewById(R.id.timer).setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                    partage.setVisibility(View.INVISIBLE);
                    etat = EN_COURS;
                    chargerWebView();

                }
            });



            partage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    partageScore();
                }
            });


        }

        webview.setWebViewClient(new WebViewClient() {
            // you tell the webclient you want to catch when a url is about to load
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            // here you execute an action when the URL you want is about to load
            @Override
            public void onLoadResource(WebView view, String url) {

                if (url.contains("epreuve" + settings.getInt(Util.epreuve, 1))) {
                    view.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(AccueilActivity.this, EpreuveActivity.class);
                    startActivity(intent);
                } else {
                    int numEpreuveVoulue = Integer.parseInt("" + url.charAt(14));
                    int numEpreuveAFaire = settings.getInt(Util.epreuve, 1);

                    String texte;
                    if (numEpreuveVoulue > numEpreuveAFaire)
                        texte = "Vous devez faire les epreuves precedentes avant de faire celle ci.";
                    else
                        texte = "Vous avez deja fait cet épreuve. Vous ne pouvez pas la recommencer.";

                    Toast.makeText(getApplicationContext(), texte, Toast.LENGTH_SHORT).show();
                }

                webview.loadUrl((String) myXml.getEtape(settings.getInt(Util.etape, 1)).get(Util.url));
            }
        });

        Button recommencer = (Button) findViewById(R.id.recommencer);
        recommencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed = settings.edit();
                ed.putInt(Util.etape, 0);
                ed.putInt(Util.epreuve, 0);
                ed.putFloat(Util.score, 0);
                for(int i = 0; i<myXml.getNbEtapes(); i++)
                    ed.putFloat(Util.score+i, 0);
                ed.putLong(Util.time, Util.ONE_HOUR);
                ed.commit();

                Button b = (Button) findViewById(R.id.button);
                b.setVisibility(View.VISIBLE);

                Button part = (Button) findViewById(R.id.partageId);
                part.setVisibility(View.VISIBLE);

                TextView zone = (TextView) findViewById(R.id.textViewZone);
                zone.setVisibility(View.INVISIBLE);
                Button boutonRecommencer = (Button) findViewById(R.id.recommencer);
                boutonRecommencer.setVisibility(View.INVISIBLE);

                TextView titre = (TextView) findViewById(R.id.idTitre);
                titre.setVisibility(View.VISIBLE);

                timer = getTimer();
                etat = COMMENCE;
            }
        });
    }

    //final Button buttonRep = (Button) findViewById(R.id.buttonRep);
    //button.setVisibility(View.INVISIBLE);


    private CountDownTimer getTimer() {
        final TextView timerText = (TextView) findViewById(R.id.timer);

        return new CountDownTimer(settings.getLong(Util.time, Util.ONE_HOUR), Util.ONE_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished/1000;
                long min = sec / 60;
                sec = sec % 60;
                timerText.setText("Il vous reste " + min + "minutes " + sec +"secondes avant la fin du jeu");
                SharedPreferences.Editor ed = settings.edit();
                ed.putLong(Util.time, millisUntilFinished);
                ed.commit();
            }

            @Override
            public void onFinish() {
                SharedPreferences.Editor ed = settings.edit();
                ed.putInt(Util.etape, myXml.getNbEtapes() + 1);
                ed.commit();
                verifContenu(); //mise a jour de la page et fin du jeu
            }
        };
    }

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
            Toast.makeText(AccueilActivity.this, R.string.app_geo_erreur_permission, Toast.LENGTH_LONG).show(); //TODO message erreur correct


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                Toast.makeText(AccueilActivity.this, R.string.app_geo_erreur_permission, Toast.LENGTH_LONG).show(); //TODO message erreur correct
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

        if(settings.getInt(Util.etape, 1) == 0) { //Quand on recommence, le numero d etape et d epreuve est a 0 alors que quand on commence en partant de rien ils sont non définis (cela pose probleme ici et plus tard dans le programme)
            settings.edit().putInt(Util.etape, 1).commit();
            settings.edit().putInt(Util.epreuve, 1).commit();
        }
        String myUrl = (String) myXml.getEtape(settings.getInt(Util.etape,1)).get(Util.url);

        webview.setVisibility(View.VISIBLE);
        webview.loadUrl(myUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        faireLaLocalisation(Util.FIVE_MINUTES*2,CHANGE_TIME_REFRESH_GEO);


    }

    @Override
    protected void onStop() {
        super.onStop();
        faireLaLocalisation(0, STOP_GEO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        faireLaLocalisation(Util.THIRTY_SECONDS, CHANGE_TIME_REFRESH_GEO);
        verifContenu();
    }

    private void verifContenu() {
        if(myXml.getNbEtapes() + 1 == settings.getInt(Util.etape, 1)) {
            timer.cancel();

            TextView timerText = (TextView) findViewById(R.id.timer);
            timerText.setVisibility(View.INVISIBLE);
            TextView titre = (TextView) findViewById(R.id.idTitre);
            titre.setVisibility(View.INVISIBLE);
            Button b = (Button) findViewById(R.id.button);
            b.setVisibility(View.INVISIBLE);
            webview.setVisibility(View.INVISIBLE);
            TextView zone = (TextView) findViewById(R.id.textViewZone);
            zone.setVisibility(View.VISIBLE);

            long time  = settings.getLong(Util.time, Util.ONE_HOUR);
            time = Util.ONE_HOUR - time;
            time /= 1000;

            long min = time / 60;
            long sec = time % 60;

            float score = settings.getFloat(Util.score, 0);
            float bestScore = settings.getFloat(Util.bestScore, 0);
            String texte = "Vous avez fini ! Bravo ! Votre score final est de " + score + ". Vous avez mis " + min +"minutes " + sec +"secondes pour finir le jeu";
            if(score > bestScore) {
                if(bestScore != 0)
                    texte = texte +"\nBravo vous avez battu votre meilleur score qui etait de " + bestScore;
                SharedPreferences.Editor ed = settings.edit();
                ed.putFloat(Util.bestScore, score);
                ed.commit();
            }

            zone.setText(texte);

            Button boutonRecommencer = (Button) findViewById(R.id.recommencer);
            boutonRecommencer.setVisibility(View.VISIBLE);
        }
        verifLocation();
    }

    private void verifLocation() {
        if (etat != EN_COURS) return;
        if(myXml.getNbEtapes() + 1 == settings.getInt(Util.etape, 1))
            return;

        Button b = (Button) findViewById(R.id.button);
        TextView t = (TextView) findViewById(R.id.textViewZone);

        HashMap<String, Object> zone = (HashMap) myXml.getEtape(settings.getInt(Util.etape, 1)).get(Util.zone);
        Location loc1 = new Location("");
        double lat = (double) zone.get(Util.latitude);
        double lon = (double) zone.get(Util.longitude);
        loc1.setLatitude(lat);
        loc1.setLongitude(lon);

        if(bestLocation != null && loc1.distanceTo(bestLocation) <= (int) zone.get(Util.rayon)) {
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

    public void partageScore() {
        Intent intent = new Intent(AccueilActivity.this, Partage.class);
        startActivity(intent);
    }


}
