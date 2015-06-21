package com.projects.team.experimental.virtualdashboard.DigitalGauge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.projects.team.experimental.virtualdashboard.Editor.GenericView;
import com.projects.team.experimental.virtualdashboard.R;

import java.util.ArrayList;


public class DigitalDisplayView extends View implements GenericView.GenericWidget{

    private static final int DP_SCALE = 10;
    private static final int[] mColorPicker = {Color.RED, Color.rgb(255,165,0), Color.YELLOW,
                            Color.GREEN, Color.BLUE, Color.rgb(128,0,128), Color.rgb(238, 130, 238)};
    private static final CharSequence[] sColorPicker = {"Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Violet"};


    private double displayVal = 100.23;
    private String displayUnits = "Zenny";

    private TextPaint valPainter;
    private TextPaint unitsPainter;

    private int labelSize = 10;

    public DigitalDisplayView(Context context) {
        super(context);
        init(null, 0);
    }

    public DigitalDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DigitalDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DigitalDisplayView, defStyle, 0);

        int colorPicked = a.getInt(R.styleable.DigitalDisplayView_colorPicker, 0);
        if(a.hasValue(R.styleable.DigitalDisplayView_subscriptUnits)){
            displayUnits = a.getString(R.styleable.DigitalDisplayView_subscriptUnits);
        }

        a.recycle();

        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        labelSize = Math.round(DP_SCALE*(getResources().getDisplayMetrics().density));
        setColor(mColorPicker[colorPicked]);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = canvas.getWidth() - paddingLeft - paddingRight;
        int contentHeight = canvas.getHeight() - paddingTop - paddingBottom;

        canvas.drawColor(Color.parseColor("#01000000"));
        //canvas.drawColor(Color.WHITE);
        canvas.drawColor(Color.TRANSPARENT);

        labelSize = (int) (0.15*contentWidth);
        rescaleLabel();
        canvas.drawText(String.valueOf(displayVal), contentWidth/3, contentHeight/2+contentHeight/7, valPainter);
        canvas.drawText(displayUnits, (float) (contentWidth/3+labelSize*2.5), contentHeight/2+contentHeight/7, unitsPainter);
    }

    @Override
    public void setValue(double val){
        this.displayVal = val;
        invalidate();
    }

    @Override
    public double getValue(){
        return this.displayVal;
    }

    @Override
    public void setUnits(String s){
        this.displayUnits = s;
        invalidate();
    }


    @Override
    public String getUnits(){
        return this.displayUnits;
    }

    @Override
    public void setColor(String val){
        int index = getColorThemes().indexOf(val);
        setColor(mColorPicker[index]);
    }

    public void setColor(int val){
        valPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        valPainter.setColor(val);
        valPainter.setTextSize(labelSize);
        valPainter.setTextAlign(Paint.Align.CENTER);
        valPainter.setLinearText(true);

        unitsPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        unitsPainter.setColor(val);
        unitsPainter.setTextSize((float)(labelSize*0.5));
        unitsPainter.setTextAlign(Paint.Align.CENTER);
        unitsPainter.setLinearText(true);

        invalidate();
    }

    @Override
    public String getColor(){
        int color = valPainter.getColor();
        for(int i = 0; i<mColorPicker.length; i++){
            if(mColorPicker[i] == color){
                return sColorPicker[i].toString();
            }
        }
        return "";
    }

    @Override
    public ArrayList<CharSequence> getColorThemes(){
        ArrayList<CharSequence> ret = new ArrayList<>();
        for(CharSequence c: sColorPicker){
            ret.add(c);
        }
        return ret;
    }


    public void rescaleLabel(){
        valPainter.setTextSize(labelSize);
        unitsPainter.setTextSize((float)(labelSize*0.65));
    }


}

