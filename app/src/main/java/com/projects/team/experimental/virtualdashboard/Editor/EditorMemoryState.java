package com.projects.team.experimental.virtualdashboard.Editor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

public class EditorMemoryState {

    public static final String DEFAULT_VIEW_1_FILE = "Default View 1.txt";
    public static final String DEFAULT_VIEW_2_FILE = "Default View 2.txt";
    public static final String EDITOR_VIEW_FILE = "Editor View ";
    public static final String FILE_EXTENSION = ".txt";
    public static final String CHECKBOX_STATE = "Checkbox State";
    public static final String EDITOR_WIDGET_STATE = "Widgets State";

    public static final String EDITOR_WIDGET_POS_X = "Pos X";
    public static final String EDITOR_WIDGET_POS_Y = "Pos Y";
    public static final String EDITOR_WIDGET_WIDTH = "Width";
    public static final String EDITOR_WIDGET_HEIGHT = "Height";
    public static final String EDITOR_GENERIC_VIEW_STATE = "Generic";

    public static final String PANEL_WIDTH = "PanelX";
    public static final int PANEL_DEFAULT_WIDTH = 712;
    public static final String PANEL_HEIGHT = "PanelY";
    public static final int PANEL_DEFAULT_HEIGHT = 310;

    public static final String WHEEL_RADIUS = "WHEEL_RAD";
    public static final int WHEEL_DEFAULT_RADIUS = 20; // Assumed centimeter as the base units.

    public static final String CUSTOM_KEY_NAME_1 = "CUSTOM_NAME_1";
    public static final String CUSTOM_DEFAULT_NAME_1 = "CUSTOM 1";
    public static final String CUSTOM_KEY_NAME_2 = "CUSTOM_NAME_2";
    public static final String CUSTOM_DEFAULT_NAME_2 = "CUSTOM 2";
    public static final String CUSTOM_KEY_NAME_3 = "CUSTOM_NAME_3";
    public static final String CUSTOM_DEFAULT_NAME_3 = "CUSTOM 3";


    Context globalContext;
    public SharedPreferences settingsPrefs;
    public static final String SETTING_FILENAME = "settings.xml";
    public static final String SHARED_VIEW_POS = "SHOW_VIEW_POS";

    //Share application state with rest of application, has the app been recently onCreated
    public static final String APP_CREATED = "APP_STARTED";

    public EditorMemoryState(Context context){
        this.globalContext = context;
        settingsPrefs = globalContext.getSharedPreferences(SETTING_FILENAME, Context.MODE_PRIVATE);
    }

    public void storeDefaultData1() throws JSONException, IOException{
        File file = new File(globalContext.getFilesDir(), DEFAULT_VIEW_1_FILE);
        FileOutputStream outputStream;
        FileOutputStream fos = globalContext.openFileOutput(DEFAULT_VIEW_1_FILE, Context.MODE_PRIVATE);
        fos.write(generateDefault1JSON().toString().getBytes());
        fos.close();
    }

    public static JSONObject generateDefault1JSON() throws JSONException{
        JSONObject jso = new JSONObject();
        JSONArray jsa = new JSONArray();
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jso.put(CHECKBOX_STATE, jsa);


        //Array of JSONObjects, representing the different views.
        JSONArray jsaWidgets = new JSONArray();

        //VELOCITY WIDGET
        JSONObject jsoVelocity = new JSONObject();
        jsoVelocity.put(EDITOR_WIDGET_POS_X, 374);
        jsoVelocity.put(EDITOR_WIDGET_POS_Y, 5);
        jsoVelocity.put(EDITOR_WIDGET_WIDTH, 330);
        jsoVelocity.put(EDITOR_WIDGET_HEIGHT, 165);
        JSONObject jsoGV = new JSONObject();
        jsoGV.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoGV.put("Current Display State", GenericView.ANALOGUE);
        jsoGV.put("Analogue Theme", "Crystal Day");
        jsoGV.put("Digital Theme", "Orange");
        jsoVelocity.put(EDITOR_GENERIC_VIEW_STATE, jsoGV);
        jsaWidgets.put(jsoVelocity);


        //ACCCELERATION WIDGET
        JSONObject jsoAcceleration = new JSONObject();
        jsoAcceleration.put(EDITOR_WIDGET_POS_X, 11);
        jsoAcceleration.put(EDITOR_WIDGET_POS_Y, 52);
        jsoAcceleration.put(EDITOR_WIDGET_WIDTH, 500);
        jsoAcceleration.put(EDITOR_WIDGET_HEIGHT, 250);
        JSONObject jsoAcc = new JSONObject();
        jsoAcc.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoAcc.put("Current Display State", GenericView.ANALOGUE);
        jsoAcc.put("Analogue Theme", "Vampire Night");
        jsoAcc.put("Digital Theme", "Yellow");
        jsoAcceleration.put(EDITOR_GENERIC_VIEW_STATE, jsoAcc);
        jsaWidgets.put(jsoAcceleration);


        //RPM WIDGET
        JSONObject jsoRPM = new JSONObject();
        jsoRPM.put(EDITOR_WIDGET_POS_X, 468);
        jsoRPM.put(EDITOR_WIDGET_POS_Y, 126);
        jsoRPM.put(EDITOR_WIDGET_WIDTH, 304);
        jsoRPM.put(EDITOR_WIDGET_HEIGHT, 152);
        JSONObject jsoRM = new JSONObject();
        jsoRM.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoRM.put("Current Display State", GenericView.DIGITAL);
        jsoRM.put("Analogue Theme", "Crystal Day");
        jsoRM.put("Digital Theme", "Green");
        jsoRPM.put(EDITOR_GENERIC_VIEW_STATE, jsoRM);
        jsaWidgets.put(jsoRPM);



        //DISTANCE WIDGET
        JSONObject jsoDistance = new JSONObject();
        jsoDistance.put(EDITOR_WIDGET_POS_X, 466);
        jsoDistance.put(EDITOR_WIDGET_POS_Y, 179);
        jsoDistance.put(EDITOR_WIDGET_WIDTH, 304);
        jsoDistance.put(EDITOR_WIDGET_HEIGHT, 152);
        JSONObject jsoDist = new JSONObject();
        jsoDist.put("Supported Display Format", GenericView.DIGITAL);
        jsoDist.put("Current Display State", GenericView.DIGITAL);
        jsoDist.put("Analogue Theme", "Crystal Day");
        jsoDist.put("Digital Theme", "Blue");
        jsoDistance.put(EDITOR_GENERIC_VIEW_STATE, jsoDist);
        jsaWidgets.put(jsoDistance);



        //HORSEPOWER WIDGET
        JSONObject jsoHorsepower = new JSONObject();
        jsoHorsepower.put(EDITOR_WIDGET_POS_X, -52);
        jsoHorsepower.put(EDITOR_WIDGET_POS_Y, -32);
        jsoHorsepower.put(EDITOR_WIDGET_WIDTH, 304);
        jsoHorsepower.put(EDITOR_WIDGET_HEIGHT, 152);
        JSONObject jsoHp = new JSONObject();
        jsoHp.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoHp.put("Current Display State", GenericView.DIGITAL);
        jsoHp.put("Analogue Theme", "Crystal Day");
        jsoHp.put("Digital Theme", "Purple");
        jsoHorsepower.put(EDITOR_GENERIC_VIEW_STATE, jsoHp);
        jsaWidgets.put(jsoHorsepower);



        jso.put(EDITOR_WIDGET_STATE, jsaWidgets);



        return jso;
    }

    public JSONObject loadDefaultData1() throws JSONException, FileNotFoundException, IOException{
        FileInputStream fis = globalContext.openFileInput(DEFAULT_VIEW_1_FILE);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        JSONObject jso = new JSONObject(sb.toString());
        return jso;
    }

    public void storeDefaultData2() throws JSONException, IOException{
        JSONObject jso = new JSONObject();
        JSONArray jsa = new JSONArray();
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jsa.put(true);
        jso.put(CHECKBOX_STATE, jsa);


        //Array of JSONObjects, representing the different views.
        JSONArray jsaWidgets = new JSONArray();

        //VELOCITY WIDGET
        JSONObject jsoVelocity = new JSONObject();
        jsoVelocity.put(EDITOR_WIDGET_POS_X, 450);
        jsoVelocity.put(EDITOR_WIDGET_POS_Y, 175);
        jsoVelocity.put(EDITOR_WIDGET_WIDTH, 254);
        jsoVelocity.put(EDITOR_WIDGET_HEIGHT, 127);
        JSONObject jsoGV = new JSONObject();
        jsoGV.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoGV.put("Current Display State", GenericView.ANALOGUE);
        jsoGV.put("Analogue Theme", "Vampire Night");
        jsoGV.put("Digital Theme", "Orange");
        jsoVelocity.put(EDITOR_GENERIC_VIEW_STATE, jsoGV);
        jsaWidgets.put(jsoVelocity);


        //ACCCELERATION WIDGET
        JSONObject jsoAcceleration = new JSONObject();
        jsoAcceleration.put(EDITOR_WIDGET_POS_X, 455);
        jsoAcceleration.put(EDITOR_WIDGET_POS_Y, 23);
        jsoAcceleration.put(EDITOR_WIDGET_WIDTH, 224);
        jsoAcceleration.put(EDITOR_WIDGET_HEIGHT, 112);
        JSONObject jsoAcc = new JSONObject();
        jsoAcc.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoAcc.put("Current Display State", GenericView.ANALOGUE);
        jsoAcc.put("Analogue Theme", "Vampire Night");
        jsoAcc.put("Digital Theme", "Yellow");
        jsoAcceleration.put(EDITOR_GENERIC_VIEW_STATE, jsoAcc);
        jsaWidgets.put(jsoAcceleration);


        //RPM WIDGET
        JSONObject jsoRPM = new JSONObject();
        jsoRPM.put(EDITOR_WIDGET_POS_X, 12);
        jsoRPM.put(EDITOR_WIDGET_POS_Y, 7);
        jsoRPM.put(EDITOR_WIDGET_WIDTH, 450);
        jsoRPM.put(EDITOR_WIDGET_HEIGHT, 225);
        JSONObject jsoRM = new JSONObject();
        jsoRM.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoRM.put("Current Display State", GenericView.ANALOGUE);
        jsoRM.put("Analogue Theme", "Crystal Day");
        jsoRM.put("Digital Theme", "Green");
        jsoRPM.put(EDITOR_GENERIC_VIEW_STATE, jsoRM);
        jsaWidgets.put(jsoRPM);



        //DISTANCE WIDGET
        JSONObject jsoDistance = new JSONObject();
        jsoDistance.put(EDITOR_WIDGET_POS_X, -18);
        jsoDistance.put(EDITOR_WIDGET_POS_Y, 201);
        jsoDistance.put(EDITOR_WIDGET_WIDTH, 304);
        jsoDistance.put(EDITOR_WIDGET_HEIGHT, 152);
        JSONObject jsoDist = new JSONObject();
        jsoDist.put("Supported Display Format", GenericView.DIGITAL);
        jsoDist.put("Current Display State", GenericView.DIGITAL);
        jsoDist.put("Analogue Theme", "Crystal Day");
        jsoDist.put("Digital Theme", "Blue");
        jsoDistance.put(EDITOR_GENERIC_VIEW_STATE, jsoDist);
        jsaWidgets.put(jsoDistance);



        //HORSEPOWER WIDGET
        JSONObject jsoHorsepower = new JSONObject();
        jsoHorsepower.put(EDITOR_WIDGET_POS_X, 211);
        jsoHorsepower.put(EDITOR_WIDGET_POS_Y, 205);
        jsoHorsepower.put(EDITOR_WIDGET_WIDTH, 304);
        jsoHorsepower.put(EDITOR_WIDGET_HEIGHT, 152);
        JSONObject jsoHp = new JSONObject();
        jsoHp.put("Supported Display Format", GenericView.BOTH_DISPLAY);
        jsoHp.put("Current Display State", GenericView.DIGITAL);
        jsoHp.put("Analogue Theme", "Crystal Day");
        jsoHp.put("Digital Theme", "Purple");
        jsoHorsepower.put(EDITOR_GENERIC_VIEW_STATE, jsoHp);
        jsaWidgets.put(jsoHorsepower);



        jso.put(EDITOR_WIDGET_STATE, jsaWidgets);


        File file = new File(globalContext.getFilesDir(), DEFAULT_VIEW_2_FILE);
        FileOutputStream outputStream;
        FileOutputStream fos = globalContext.openFileOutput(DEFAULT_VIEW_2_FILE, Context.MODE_PRIVATE);
        fos.write(jso.toString().getBytes());
        fos.close();
    }

    public JSONObject loadDefaultData2() throws JSONException, FileNotFoundException, IOException{
        FileInputStream fis = globalContext.openFileInput(DEFAULT_VIEW_2_FILE);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        JSONObject jso = new JSONObject(sb.toString());
        return jso;
    }

    public void saveEditState( JSONObject jso, int pos) throws IOException{
        File file = new File(globalContext.getFilesDir(), EDITOR_VIEW_FILE + pos + FILE_EXTENSION);
        FileOutputStream outputStream;
        FileOutputStream fos = globalContext.openFileOutput(EDITOR_VIEW_FILE + pos + FILE_EXTENSION, Context.MODE_PRIVATE);
        fos.write(jso.toString().getBytes());
        fos.close();
    }

    public JSONObject loadEditState( int pos) throws IOException, JSONException{
        FileInputStream fis = globalContext.openFileInput(EDITOR_VIEW_FILE + pos + FILE_EXTENSION);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        JSONObject jso = new JSONObject(sb.toString());
        return jso;
    }

    public void setSkinForPos(int pos){
        int currentPos = settingsPrefs.getInt(SHARED_VIEW_POS, -1);

        if (currentPos == pos) {
            settingsPrefs.edit().putInt(SHARED_VIEW_POS, -1).apply();
        }
        settingsPrefs.edit().putInt(SHARED_VIEW_POS, pos).apply();
    }

    //Figure out which file it is from the shared preferences
    public JSONObject loadDisplayState() throws JSONException, IOException{
        int showPos = settingsPrefs.getInt(SHARED_VIEW_POS, 0);

        if (showPos == 0) {
            return loadDefaultData1();
        } else if (showPos == 1) {
            return loadDefaultData2();
        } else {
            return loadEditState(showPos-1);
        }
    }

    public void setPanelWidth(int width){
        settingsPrefs.edit().putInt(PANEL_WIDTH, width).apply();
    }

    public void setPanelHeight(int height){
        settingsPrefs.edit().putInt(PANEL_HEIGHT, height).apply();
    }

    public void setCustomName(String name, int pos){
        String key = CUSTOM_KEY_NAME_1;
        switch (pos){
            case 0:
                key = CUSTOM_KEY_NAME_1;
                break;
            case 1:
                key = CUSTOM_KEY_NAME_2;
                break;
            case 2:
                key = CUSTOM_KEY_NAME_3;
                break;
        }
        settingsPrefs.edit().putString(key, name).apply();
    }

    public String getCustomName(int pos){
        String value = "";
        String key = CUSTOM_KEY_NAME_1;
        String defValue = CUSTOM_DEFAULT_NAME_1;
        switch (pos){
            case 0:
                key = CUSTOM_KEY_NAME_1;
                break;
            case 1:
                key = CUSTOM_KEY_NAME_2;
                defValue = CUSTOM_DEFAULT_NAME_2;
                break;
            case 2:
                key = CUSTOM_KEY_NAME_3;
                defValue = CUSTOM_DEFAULT_NAME_3;
                break;
        }
        return settingsPrefs.getString(key, defValue);
    }

    public int getPanelWidth(){
        return settingsPrefs.getInt(PANEL_WIDTH, PANEL_DEFAULT_WIDTH);
    }

    public int getPanelHeight(){
        return settingsPrefs.getInt(PANEL_HEIGHT, PANEL_DEFAULT_HEIGHT);
    }

    public void test(){

    }


}
