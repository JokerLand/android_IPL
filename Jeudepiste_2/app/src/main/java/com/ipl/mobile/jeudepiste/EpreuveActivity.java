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
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ipl.mobile.jeudepiste.util.ChargerXML;
import com.ipl.mobile.jeudepiste.util.Photo;
import com.ipl.mobile.jeudepiste.util.Util;

import java.util.HashMap;

public class EpreuveActivity extends Activity {


    private SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epreuve);

        settings = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);

        final ChargerXML xml = new ChargerXML(this);

        final HashMap<String, Object> map = xml.getEpreuvePreference();

        final String typeQuestion = (String) map.get(Util.type);

        final TextView question = (TextView) findViewById(R.id.textQuestion);
        question.setText((String) map.get(Util.question));

        final RadioButton r1 = (RadioButton) findViewById(R.id.reponse1);
        final RadioButton r2 = (RadioButton) findViewById(R.id.reponse2);
        final RadioButton r3 = (RadioButton) findViewById(R.id.reponse3);

        final EditText edTxt = (EditText) findViewById(R.id.editText);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        final Button boutonPhoto = (Button) findViewById(R.id.boutonPhoto);
        boutonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Photo().prendreUnePhoto(EpreuveActivity.this);
            }
        });

        if(typeQuestion.equals(Util.QCM)) {
            radioGroup.setVisibility(View.VISIBLE);

            r1.setText((String) map.get(Util.reponse1));
            r2.setText((String) map.get(Util.reponse2));
            r3.setText((String) map.get(Util.reponse3));
        } else if(typeQuestion.equals(Util.texte)) {
            edTxt.setVisibility(View.VISIBLE);
        } else if(typeQuestion.equals(Util.photo)) {
            boutonPhoto.setVisibility(View.VISIBLE);
        }

        final Button boutonFin = (Button) findViewById(R.id.boutonFin);
        final TextView textReponse = (TextView) findViewById(R.id.textResult);

        final Button boutonRepondre = (Button) findViewById(R.id.boutonRepondre);
        boutonRepondre.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                question.setVisibility(View.INVISIBLE);
                boolean ok = false;
                if (typeQuestion.equals(Util.QCM)) {
                    radioGroup.setVisibility(View.INVISIBLE);
                    if (r1.isChecked() && (boolean) map.get(Util.bonne1)) ok = true;
                    if (r2.isChecked() && (boolean) map.get(Util.bonne2)) ok = true;
                    if (r3.isChecked() && (boolean) map.get(Util.bonne3)) ok = true;

                } else if (typeQuestion.equals(Util.texte)) {
                    edTxt.setVisibility(View.INVISIBLE);
                    ok = edTxt.getText().toString().toLowerCase().contains((String) map.get(Util.reponse)); //verifie si la reponse donnée par l utilisateur contient la reponse dans la map
                } else if (typeQuestion.equals(Util.photo)) {
                    boutonPhoto.setVisibility(View.INVISIBLE);
                    Location loc1 = AccueilActivity.bestLocation;
                    Location loc2 = new Location("");
                    loc2.setLatitude((double) ((HashMap) map.get(Util.zone)).get(Util.latitude));
                    loc2.setLongitude((double) ((HashMap) map.get(Util.zone)).get((Util.longitude)));

                    ok = loc1.distanceTo(loc2) <= (int) ((HashMap) map.get(Util.zone)).get(Util.rayon);
                }
                SharedPreferences.Editor ed = settings.edit();
                ed.putInt(Util.epreuve, settings.getInt(Util.epreuve, 1) + 1);
                if (ok) {
                    float proportion = ((float) settings.getLong(Util.time, Util.ONE_HOUR)) / Util.ONE_HOUR;
                    int pts = (int) map.get(Util.points);
                    float points = pts * proportion; // calcul des points gagnés pour cette épreuve
                    float score = settings.getFloat(Util.score, 0) + points; //score total
                    ed.putFloat(Util.score, score);
                    float scoreEtape = settings.getFloat(Util.score + settings.getInt(Util.etape, 1), 0) +points; //score de l etape
                    ed.putFloat(Util.score + settings.getInt(Util.etape, 1), scoreEtape);
                    textReponse.setText("Vous avez réussi la question. Votre score est de " + score +". Pout cette étape vous avez " + scoreEtape + " points. Vous avez gagné " + points + " points grace a cette question.");
                } else {
                    textReponse.setText("Vous avez raté la question. Votre score est de " + settings.getFloat(Util.score, 0));
                }
                ed.commit();
                if(settings.getInt(Util.epreuve, 1) > (Integer) xml.getEtape(settings.getInt(Util.etape, 1)).get(Util.nombreEpreuves)) {
                    ed.putInt(Util.etape, settings.getInt(Util.etape, 1) + 1);
                    ed.putInt(Util.epreuve, 1);// Nouvelles epreuves d'une nouvelle etape
                }

                ed.commit();

                boutonRepondre.setVisibility(View.INVISIBLE);
                boutonFin.setVisibility(View.VISIBLE);
                textReponse.setVisibility(View.VISIBLE);
            }
        });


        boutonFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpreuveActivity.this.finish();
            }
        });
    }
}
