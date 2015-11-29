package com.ipl.mobile.jeudepiste.util;

import android.location.Location;

/**
 * Created by Mathieu on 28/11/2015.
 */
public class Util {

    public final static String etape = "etape";
    public final static String epreuve = "epreuve";

    public final static String QCM = "QCM";
    public final static String photo = "Photo";
    public final static String texte = "Texte";

    public final static String nombreEpreuves  = "nombreEpreuves";
    public final static String nombreEtapes = "nombreEtapes";
    public final static String num = "num";
    public final static String url = "url";
    public final static String type = "type";
    public final static String question = "question";
    public final static String points = "points";
    public final static String score = "score";
    public final static String uri = "uri";

    public final static String reponse = "reponse";
    public final static String reponse1 = "reponse1";
    public final static String reponse2 = "reponse2";
    public final static String reponse3 = "reponse3";
    public final static String bonne = "bonne";
    public final static String bonne1 = "bonne1";
    public final static String bonne2 = "bonne2";
    public final static String bonne3 = "bonne3";

    public final static String zone = "zone";
    public final static String latitude = "latitude";
    public final static String longitude = "longitude";
    public final static String rayon = "rayon";

    public final static String preferences = "preferences";


    public final static int TWO_MINUTES = 1000 * 60 * 2;
    public final static int FIVE_MINUTES = 1000*60*5;
    public final static int THIRTY_SECONDS = 1000 *30;
    public final static int ONE_SECOND = 1000 * 1;
    public static final long FIVE_SECONDS = 1000 * 5 ;
    public static final int ONE_HOUR = 1000 * 60 * 60;


    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public final static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public final static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


}
