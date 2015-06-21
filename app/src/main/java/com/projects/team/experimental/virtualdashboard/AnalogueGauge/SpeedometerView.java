package com.projects.team.experimental.virtualdashboard.AnalogueGauge;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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


import com.projects.team.experimental.virtualdashboard.Editor.GenericView;
import com.projects.team.experimental.virtualdashboard.R;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class SpeedometerView extends View implements GenericView.GenericWidget{
    private static final String TAG = SpeedometerView.class.getSimpleName();

    // TODO Change some color scheme, looks better
    private static final int NIGHT_TIME = 0;
    private static final int DAY_TIME = 1;
    private static final CharSequence[] sColorPicker = {"Vampire Night", "Crystal Day"};

    private static final int DEFAULT_MIN_VAL = 0;
    private static final int DEFAULT_MAX_VAL = 120;

    private static final int DEFAULT_TICK_STEP_INTERVAL = 20;
    private static final int DEFAULT_TICKS_PER_INTERVAL = 1;

    private Drawable mbackgroundImage;
    private int mCustomTheme = NIGHT_TIME;
    private int mMinVal = DEFAULT_MIN_VAL;
    private int mMaxVal = DEFAULT_MAX_VAL;
    private int mTickStepInterval = DEFAULT_TICK_STEP_INTERVAL;
    private int mTicksPerInterval = DEFAULT_TICKS_PER_INTERVAL;
    private double currentVal = 0.3*DEFAULT_MAX_VAL;
    private String mDisplayUnits = "KM/hr";

    private DisplayPainter picasso;

    public SpeedometerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpeedometerView, defStyle, 0);

        mMinVal = a.getInt(R.styleable.SpeedometerView_minVal, DEFAULT_MIN_VAL);
        mMaxVal = a.getInt(R.styleable.SpeedometerView_maxVal, DEFAULT_MAX_VAL);
        mTicksPerInterval = a.getInt(R.styleable.SpeedometerView_ticksPerInterval, DEFAULT_TICKS_PER_INTERVAL);
        mTickStepInterval = a.getInt(R.styleable.SpeedometerView_tickStepInterval, DEFAULT_TICK_STEP_INTERVAL);
        if(a.hasValue(R.styleable.SpeedometerView_displayUnits)){
            mDisplayUnits = a.getString(R.styleable.SpeedometerView_displayUnits);
        }

        mCustomTheme = a.getInteger(R.styleable.SpeedometerView_customTheme, NIGHT_TIME);
        if (a.hasValue(R.styleable.SpeedometerView_backgroundImage)) {
            mbackgroundImage = a.getDrawable(R.styleable.SpeedometerView_backgroundImage);
            mbackgroundImage.setCallback(this);
        }

        a.recycle();

        /* ___ Initialise a Painter __ */
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        picasso = new DisplayPainter(mCustomTheme);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        drawBg(canvas);
        drawTickIntervals(canvas);
        drawDial(canvas);
    }

    public void setMinVal(int val){
        if(val >= mMaxVal){
            throw new IllegalArgumentException("Value Specified Greater than Maximum");
        }
        mMinVal = val;
        invalidate();
    }

    public int getMinVal(){
        return mMinVal;
    }

    public void setTickStepInterval(int val){
        if(val < (mMaxVal-mMinVal)){
            mTickStepInterval = val;
        }
    }

    public int getTickStepInterval(){
        return mTickStepInterval;
    }

    public void setTicksPerInterval(int val){
        mTicksPerInterval = val;
    }

    public int getTicksPerInterval(){
        return mTicksPerInterval;
    }

    public void setMaxVal(int val){
        if(val <= mMinVal){
            throw new IllegalArgumentException("Value Specified Less then Minimum");
        }
        mMaxVal = val;
        invalidate();
    }

    public int getMaxVal(){
        return mMaxVal;
    }

    @Override
    public double getValue(){
        return currentVal;
    }

    @Override
    public void setValue(double val){
        if(val < mMinVal){
            currentVal = mMinVal;
        } else if (val > mMaxVal) {
            currentVal = mMaxVal;
        } else {
            currentVal = val;
        }
        invalidate();
    }

    public ValueAnimator setValue(double val, long duration, long initDelay){
        if(val < mMinVal){
            val = mMinVal;
        } else if (val>mMaxVal) {
            val = mMaxVal;
        }

        ValueAnimator disney = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, Double.valueOf(getValue()), Double.valueOf(val));

        disney.setDuration(duration);
        disney.setStartDelay(initDelay);
        disney.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setValue(value);
            }
        });
        disney.start();
        return disney;
    }

    @Override
    public void setColor(String val){
        int index = getColorThemes().indexOf(val);
        picasso = new DisplayPainter(index);
    }

    @Override
    public String getColor(){
        return sColorPicker[picasso.currentTheme].toString();
    }

    @Override
    public ArrayList<CharSequence> getColorThemes(){
        ArrayList<CharSequence> ret = new ArrayList<CharSequence>();
        for(CharSequence c: sColorPicker){
            ret.add(c);
        }
        return ret;
    }

    @Override
    public void setUnits(String s){
        if(s!=null){
            mDisplayUnits = s;
        }
        invalidate();
    }

    public String getUnits(){
        return mDisplayUnits;
    }

    private RectF createCircle(Canvas canvas, float factor) {
        RectF ret;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight*2 >= canvasWidth) {
            ret = new RectF(0, 0, canvasWidth*factor, canvasWidth*factor);
        } else {
            ret = new RectF(0, 0, canvasHeight*2*factor, canvasHeight*2*factor);
        }

        ret.offset((canvasWidth-ret.width())/2 + getPaddingLeft(), (canvasHeight*2-ret.height())/2 + getPaddingTop());

        return ret;
    }


    private void drawBg(Canvas cv){
        RectF circ = createCircle(cv, 1);
        cv.drawArc(circ, 180, 180, true, picasso.bgPainter);

        RectF innerCirc = createCircle(cv, 0.9f);
        cv.drawArc(innerCirc, 180, 180, true, picasso.bgInPainter);

        Bitmap mask = Bitmap.createScaledBitmap(picasso.mask, (int) (circ.width() * 1.1), (int) (circ.height() * 1.1) / 2, true);
        cv.drawBitmap(mask, circ.centerX() - circ.width()*1.1f/2, circ.centerY()-circ.width()*1.1f/2, picasso.mskPainter);

    }

    private void drawTickIntervals(Canvas cv){
        float drawAngle = 160;
        double tickInterval = mTickStepInterval; // Android Issue: Value needs to be copied
        float mMajorStepAngle = (float) (tickInterval/(mMaxVal - mMinVal) * drawAngle); // CHECK changed
        float mMinorStepAngle = mMajorStepAngle / (1+mTicksPerInterval);

        float majorTicksLength = 30;
        float minorTicksLength = majorTicksLength/2;

        RectF bgCircle = createCircle(cv, 1);
        float radius = bgCircle.width()*0.35f;

        float currentAngle = 10;
        double currentVal = mMinVal; // CHECK Changed
        while (currentAngle <= 170) {

            cv.drawLine(
                    (float) (bgCircle.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (bgCircle.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (bgCircle.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    (float) (bgCircle.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    picasso.tickPainter
            );

            for (int i=1; i<= mTicksPerInterval; i++) {
                float angle = currentAngle + i*mMinorStepAngle;
                if (angle >= 170 + mMinorStepAngle/2) {
                    break;
                }
                cv.drawLine(
                        (float) (bgCircle.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius),
                        (float) (bgCircle.centerY() - Math.sin(angle / 180 * Math.PI) * radius),
                        (float) (bgCircle.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)),
                        (float) (bgCircle.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)),
                        picasso.tickPainter
                );
            }

            cv.save();
            cv.rotate(180 + currentAngle, bgCircle.centerX(), bgCircle.centerY());
            float txtX = bgCircle.centerX() + radius + majorTicksLength/2 + 8;
            float txtY = bgCircle.centerY();
            cv.rotate(+90, txtX, txtY);
            cv.drawText(String.valueOf((int) Math.round(currentVal)), txtX, txtY, picasso.txtPainter);
            cv.restore();

            currentAngle += mMajorStepAngle;
            currentVal += tickInterval;
        }

        RectF smallOval = createCircle(cv, 0.7f);
        picasso.coloredLinesPainter.setColor(Color.rgb(180, 180, 180));
        cv.drawArc(smallOval, 185, 170, false, picasso.coloredLinesPainter);

        /*for (ColoredRange range: ranges) {
            colorLinePaint.setColor(range.getColor());
            canvas.drawArc(smallOval, (float) (190 + range.getBegin()/ maxSpeed *160), (float) ((range.getEnd() - range.getBegin())/ maxSpeed *160), false, colorLinePaint);
        }*/
    }


    private void drawDial(Canvas canvas) {
        RectF dialStartPoint = createCircle(canvas, 1);
        float dialLength = dialStartPoint.width()*0.35f -10;
        RectF centreDialPiece = createCircle(canvas, 0.18f);

        float angle = 10 + (float) (currentVal/mMaxVal*160);

        if(mCustomTheme == DAY_TIME){
            canvas.drawLine(
                    (float) (dialStartPoint.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * centreDialPiece.width()*0.45f),
                    (float) (dialStartPoint.centerY() - Math.sin(angle / 180 * Math.PI) * centreDialPiece.width()*0.45f),
                    (float) (dialStartPoint.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (dialLength)),
                    (float) (dialStartPoint.centerY() - Math.sin(angle / 180 * Math.PI) * (dialLength)),
                    picasso.dialPainter
            );
            float xPos = dialStartPoint.centerX();
            float yPos = dialStartPoint.centerY()-centreDialPiece.width()*0.1f;

            canvas.drawText(mDisplayUnits, xPos, yPos, picasso.txtPainter);
        } else {
            canvas.drawLine(
                    (float) (dialStartPoint.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * centreDialPiece.width()*0.5f),
                    (float) (dialStartPoint.centerY() - Math.sin(angle / 180 * Math.PI) * centreDialPiece.width()*0.5f),
                    (float) (dialStartPoint.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (dialLength)),
                    (float) (dialStartPoint.centerY() - Math.sin(angle / 180 * Math.PI) * (dialLength)),
                    picasso.dialPainter
            );
            canvas.drawArc(centreDialPiece, 180, 180, true, picasso.bgPainter);

            float xPos = dialStartPoint.centerX();
            float yPos = dialStartPoint.centerY()-centreDialPiece.width()*0.1f;

            canvas.drawText(mDisplayUnits, xPos, yPos, picasso.txtPainter);
        }
    }

    private class DisplayPainter{
        private TextPaint txtPainter;
        private Paint coloredLinesPainter; // Pending depreciation
        private Paint tickPainter;
        private Paint dialPainter;
        private Paint mskPainter;
        private Paint bgPainter;
        private Paint bgInPainter;
        private Paint unitsPainter;

        private Bitmap mask;

        private int textLabelSize;
        private static final int DP_SIZE = 10;

        private int currentTheme = 0;

        public DisplayPainter(int theme){
            textLabelSize = Math.round(DP_SIZE*(getResources().getDisplayMetrics().density));

            switch (theme){
                case NIGHT_TIME:
                    setNightTimeTheme();
                    break;
                case DAY_TIME:
                    setDayTimeTheme();
                    break;
                default:
                    setNightTimeTheme();
                    break;
            }
        }

        public int getCurrentTheme(){
            return currentTheme;
        }

        private void setNightTimeTheme(){
            bgPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPainter.setStyle(Paint.Style.FILL);
            bgPainter.setColor(Color.rgb(127, 0, 0));

            bgInPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgInPainter.setStyle(Paint.Style.FILL);
            bgInPainter.setColor(Color.rgb(237, 41, 57));

            txtPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            txtPainter.setColor(Color.WHITE);
            txtPainter.setTextSize(textLabelSize);
            txtPainter.setTextAlign(Paint.Align.CENTER);
            txtPainter.setLinearText(true);

            mask = BitmapFactory.decodeResource(getResources(), R.drawable.mask);
            mask = Bitmap.createBitmap(mask, 0, 0, mask.getWidth(), mask.getHeight()/2);

            mskPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            mskPainter.setDither(true);

            tickPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            tickPainter.setStyle(Paint.Style.STROKE);
            tickPainter.setStrokeWidth(3.1f);
            tickPainter.setColor(Color.rgb(180, 180, 180));

            coloredLinesPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            coloredLinesPainter.setStyle(Paint.Style.STROKE);
            coloredLinesPainter.setStrokeWidth(5.1f);
            coloredLinesPainter.setColor(Color.rgb(180, 180, 180));

            dialPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            dialPainter.setStyle(Paint.Style.STROKE);
            dialPainter.setStrokeWidth(7.2f);
            dialPainter.setColor(Color.argb(200,255,0,0));
            //dialPainter.setColor(Color.argb(200,255,255,255));

            currentTheme = NIGHT_TIME;
        }

        private void setDayTimeTheme(){
            bgPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPainter.setStyle(Paint.Style.FILL);
            bgPainter.setColor(Color.rgb(167,237,252));

            bgInPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgInPainter.setStyle(Paint.Style.FILL);
            bgInPainter.setColor(Color.rgb(184,241,254));

            txtPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            txtPainter.setColor(Color.BLACK);
            txtPainter.setTextSize(textLabelSize);
            txtPainter.setTextAlign(Paint.Align.CENTER);
            txtPainter.setLinearText(true);

            mask = BitmapFactory.decodeResource(getResources(), R.drawable.mask_w3);
            mask = Bitmap.createBitmap(mask, 0, 0, mask.getWidth(), mask.getHeight()/2);

            mskPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            mskPainter.setDither(true);

            tickPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            tickPainter.setStyle(Paint.Style.STROKE);
            tickPainter.setStrokeWidth(3.1f);
            tickPainter.setColor(Color.BLACK);

            coloredLinesPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            coloredLinesPainter.setStyle(Paint.Style.STROKE);
            coloredLinesPainter.setStrokeWidth(5.1f);
            coloredLinesPainter.setColor(Color.rgb(180, 180, 180));

            dialPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
            dialPainter.setStyle(Paint.Style.STROKE);
            dialPainter.setStrokeWidth(7.2f);
           // dialPainter.setColor(Color.argb(200,255,0,0));
            dialPainter.setColor(Color.argb(200,36,41,247));

            currentTheme = DAY_TIME;
        }

    }

}
