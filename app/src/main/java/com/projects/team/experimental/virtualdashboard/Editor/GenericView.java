package com.projects.team.experimental.virtualdashboard.Editor;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.projects.team.experimental.virtualdashboard.AnalogueGauge.SpeedometerView;
import com.projects.team.experimental.virtualdashboard.DigitalGauge.DigitalDisplayView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.IllegalFormatException;

public class GenericView extends View {

    public static final int ANALOGUE = 0;
    public static final int DIGITAL = 1;
    public static final int BOTH_DISPLAY = 2;

    private int displayType = BOTH_DISPLAY;
    private int currentState = DIGITAL;

    private SpeedometerView sv;
    private DigitalDisplayView dv;

    public interface GenericWidget{
        public void setValue(double v);
        public double getValue();
        public void setUnits(String s);
        public String getUnits();
        public void setColor(String val);
        public String getColor();
        public ArrayList<CharSequence> getColorThemes();

    }

    public GenericView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public GenericView(Context context, int t) {
        super(context);
        this.displayType = t;
        if(this.displayType != BOTH_DISPLAY){
            this.currentState = t;
        }
        init(context, null, 0);
    }

    public GenericView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GenericView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        switch (displayType){
            case ANALOGUE:
                sv = new SpeedometerView(context, attrs, defStyle);
                break;
            case DIGITAL:
                dv = new DigitalDisplayView(context, attrs, defStyle);
                break;
            case BOTH_DISPLAY:
                sv = new SpeedometerView(context, attrs, defStyle);
                dv = new DigitalDisplayView(context, attrs, defStyle);
                setStateVisibility();
                break;
        }
    }

    public int getCurrentState(){
        return currentState;
    }

    public String getCurrentState(Boolean verbose){
        if (currentState == ANALOGUE) {
            return "Analogue";
        } else {
            return "Digital";
        }
    }

    public void setCurrentState(int s){
        this.currentState = s;
        setStateVisibility();
    }

    public void switchState(){
        this.currentState = (currentState+1)%2;
        setStateVisibility();
    }

    private void setStateVisibility(){
        switch (currentState){
            case ANALOGUE:
                if (dv != null) {dv.setVisibility(INVISIBLE);}
                break;
            case DIGITAL:
                if (sv != null) {sv.setVisibility(INVISIBLE);}
                break;
            default:
                if (sv != null) {sv.setVisibility(INVISIBLE);}
                break;
        }
        invalidate();
    }

    public ArrayList<CharSequence> getSupportedDisplayFormat(){
        ArrayList<CharSequence> ret = new ArrayList<CharSequence>();

        if(currentState == DIGITAL){
            ret.add("Digital");
        } else {
            ret.add("Analogue");
        }

        if(displayType == BOTH_DISPLAY){
            if(currentState == DIGITAL){
                ret.add("Analogue");
            } else {
                ret.add("Digital");
            }
        }
        return ret;
    }

    public void setSupportedColorTheme(String s){
        switch (currentState){
            case ANALOGUE:
                sv.setColor(s);
                break;
            case DIGITAL:
                dv.setColor(s);
                break;
            default:
                //Should not happen
                break;
        }
    }

    public ArrayList<CharSequence> getSupportedColorTheme(){
        switch (currentState){
            case ANALOGUE:
                return sv.getColorThemes();
            case DIGITAL:
                return dv.getColorThemes();
            default:
                return dv.getColorThemes();
        }
    }

    public void setValue(int v){
        if(sv!=null){sv.setValue(v);}
        if(dv!=null){dv.setValue(v);}
        invalidate();
    }

    public void setValue(double v){
        if(sv!=null){sv.setValue(v);}
        if(dv!=null){dv.setValue(v);}
        invalidate();
    }

    public void setUnits(String s){
        if(dv != null){ dv.setUnits(s); }
        if(sv != null){ sv.setUnits(s); }
    }

    public String getUnits(){
        if(dv != null){
            return dv.getUnits();
        }
        if(sv != null){
            return sv.getUnits();
        }
        return "";
    }

    public void setTheme(String selectedTheme){
        if (currentState == ANALOGUE) {
            if (sv != null) {sv.setColor(selectedTheme);}
        } else {
            if (dv != null) {dv.setColor(selectedTheme);}
        }
        invalidate();
    }

    public void setAnalogueRange(int min, int max){
        sv.setMinVal(min);
        sv.setMaxVal(max);

    }

    public String getTheme(){
        if (currentState == ANALOGUE) {
            if (sv != null) {return sv.getColor();}
        } else {
            if (dv != null) {return dv.getColor();}
        }
        return "";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (currentState){
            case ANALOGUE:
                if(sv!=null){sv.draw(canvas);}
                break;
            case DIGITAL:
                if(dv!=null){dv.draw(canvas);}
                break;
            default:
                if(dv!=null){dv.draw(canvas);}
                break;
        }

    }


    /* Save
    1. Supported display formats, for checking
    2. Currently in analogue or digital view
    3. Current theme for both analogue and digital view

     */
    public JSONObject saveViewState() throws JSONException{
        JSONObject jso = new JSONObject();
        jso.put("Supported Display Format", displayType);
        jso.put("Current Display State", currentState);
        if(sv!=null){jso.put("Analogue Theme", sv.getColor());}
        if(dv!=null){jso.put("Digital Theme", dv.getColor());}

        return jso;
    }

    public void loadViewState(JSONObject jso) throws JSONException, NoSuchFieldException{
        int st = jso.getInt("Supported Display Format");
        int ct = jso.getInt("Current Display State");

        if(st == BOTH_DISPLAY){
            if(sv == null || dv == null){
                throw new NoSuchFieldException("Display conflicts with instance view");
            } else {
                currentState = ct;
                sv.setColor(jso.getString("Analogue Theme"));
                dv.setColor(jso.getString("Digital Theme"));
            }
        } else if (st == ANALOGUE) {
            sv.setColor(jso.getString("Analogue Theme"));
        } else {
            dv.setColor(jso.getString("Digital Theme"));
        }

        setStateVisibility();
        invalidate();
    }




}
