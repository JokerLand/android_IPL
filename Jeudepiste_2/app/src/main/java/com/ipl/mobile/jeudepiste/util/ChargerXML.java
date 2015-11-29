package com.ipl.mobile.jeudepiste.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by JokerLand on 25-11-15.
 */
public class ChargerXML {

    private XPathFactory xPathFactory = XPathFactory.newInstance();
    private XPath xPath = xPathFactory.newXPath();
    private String filePath = "jeuDePiste.xml";
    private Context context;
    private SharedPreferences settings;

    public ChargerXML(Activity act) {
        context = act.getApplicationContext();
        this.settings = act.getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
    }

    /**
     *
     * @param numEtape
     * @return une map avec une valeur pour longitude, latitude et rayon
     */
    public HashMap<String, Object> getEtapeZone(int numEtape) {
        NodeList nl = null;
        try {
            nl = (NodeList) xPath.evaluate("//Etape[" + numEtape + "]/Zone", new InputSource(context.getAssets().open(filePath)), XPathConstants.NODESET);
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(Util.latitude, Double.parseDouble(nl.item(0).getChildNodes().item(1).getTextContent()));
        map.put(Util.longitude, Double.parseDouble(nl.item(0).getChildNodes().item(3).getTextContent()));
        map.put(Util.rayon, Integer.parseInt(nl.item(0).getChildNodes().item(5).getTextContent()));

        return map;
    }


    public HashMap<String, Object> getEtape(int numEtape) {
        NodeList nl = null;
        try {
            nl = (NodeList) xPath.evaluate("//Etape[" + numEtape + "]", new InputSource(context.getAssets().open(filePath)), XPathConstants.NODESET);
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(Util.nombreEpreuves, Integer.parseInt(nl.item(0).getAttributes().getNamedItem(Util.nombreEpreuves).getTextContent()));
        map.put(Util.etape, Integer.parseInt(nl.item(0).getAttributes().getNamedItem(Util.num).getTextContent()));
        map.put(Util.url, nl.item(0).getAttributes().getNamedItem(Util.url).getTextContent());
        map.put(Util.zone, getEtapeZone(numEtape));

        return map;
    }



    public HashMap<String, Object> getEtapeEpreuveZone(int numEtape, int numEpreuve) {
        NodeList nl = null;
        try {
            nl = (NodeList) xPath.evaluate("//Etape[" + numEtape + "]//Epreuve[" + numEpreuve + "]/Zone", new InputSource(context.getAssets().open(filePath)), XPathConstants.NODESET);
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(Util.latitude, Double.parseDouble(nl.item(0).getChildNodes().item(1).getTextContent()));
        map.put(Util.longitude, Double.parseDouble(nl.item(0).getChildNodes().item(3).getTextContent()));
        map.put(Util.rayon, Integer.parseInt(nl.item(0).getChildNodes().item(5).getTextContent()));

        return map;
    }

    public HashMap<String, Object> getEpreuvePreference() {
        int numEtape = settings.getInt(Util.etape, 1);
        int numEpreuve = settings.getInt(Util.epreuve, 1);
        NodeList nl = null;
        try {
            nl = (NodeList) xPath.evaluate("//Etape[" + numEtape + "]//Epreuve[" + numEpreuve + "]",new InputSource(context.getAssets().open(filePath)), XPathConstants.NODESET);
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Util.type, nl.item(0).getAttributes().getNamedItem(Util.type).getTextContent());
        map.put(Util.points, Integer.parseInt(nl.item(0).getAttributes().getNamedItem(Util.points).getTextContent()));
        map.put(Util.uri, nl.item(0).getAttributes().getNamedItem(Util.uri).getTextContent());
        map.put(Util.question, nl.item(0).getChildNodes().item(1).getTextContent());

        if(map.get(Util.type).equals(Util.photo)) {
            map.put(Util.zone, getEtapeEpreuveZone(numEtape, numEpreuve));
        } else if(map.get("type").equals("QCM")) {
            map.put(Util.reponse1, nl.item(0).getChildNodes().item(3).getTextContent());
            map.put(Util.bonne1, Boolean.parseBoolean(nl.item(0).getChildNodes().item(3).getAttributes().getNamedItem(Util.bonne).getTextContent()));
            map.put(Util.reponse2, nl.item(0).getChildNodes().item(5).getTextContent());
            map.put(Util.bonne2, Boolean.parseBoolean(nl.item(0).getChildNodes().item(5).getAttributes().getNamedItem(Util.bonne).getTextContent()));
            map.put(Util.reponse3, nl.item(0).getChildNodes().item(7).getTextContent());
            map.put(Util.bonne3, Boolean.parseBoolean(nl.item(0).getChildNodes().item(7).getAttributes().getNamedItem(Util.bonne).getTextContent()));
        } else if(map.get(Util.type).equals(Util.texte)) {
            map.put(Util.reponse, nl.item(0).getChildNodes().item(3).getTextContent());
        }

        return map;
    }

    public int getNbEtapes() {
        NodeList nl = null;
        try {
            nl = (NodeList) xPath.evaluate("//Jeu",new InputSource(context.getAssets().open(filePath)), XPathConstants.NODESET);
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return -1;
        }
        return Integer.parseInt(nl.item(0).getAttributes().getNamedItem(Util.nombreEtapes).getTextContent());
    }
}