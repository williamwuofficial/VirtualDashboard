package com.projects.team.experimental.virtualdashboard.Editor;

import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.projects.team.experimental.virtualdashboard.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class EditorViewFragment extends Fragment {

    private int currentDisplayPos = 0;
    private EditorMemoryState internalMemory;

    private RelativeLayout widgetPane;
    private RelativeLayout editPane;
    private static final double VIEW_EDITOR_SCALER = 2.5;

    private TextView tvSkinDisplayName;
    private EditText etNewSkinName;

    private GenericView currentEditingView; // default 1st one
    private GenericView gvVelocity;
    private GenericView gvAcceleration;
    private GenericView gvRPM;
    private GenericView gvDistance;
    private GenericView gvHorsepower;
    private ArrayList<CharSequence> widgetNames;
    private ArrayList<GenericView> fullWidgetList = new ArrayList<GenericView>();

    /*Stuff for the editor pane*/
    private Spinner spWidgetPicker;
    public ArrayAdapter<CharSequence> aadValidWidgets;
    private SeekBar sbSize;
    private Spinner spColorTheme;
    private ArrayAdapter<CharSequence> aadValidTheme;
    private Spinner spDisplayFormat;
    private ArrayAdapter<CharSequence> aadValidFormat;

    private CheckBox cbVelocity;
    private CheckBox cbAcceleration;
    private CheckBox cbRPM;
    private CheckBox cbDistance;
    private CheckBox cbHorsepower;
    private ArrayList<CheckBox> cbList = new ArrayList<CheckBox>();

    private Button btSave;
    private Button btSetView;
    private Button btSetSkinName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor_view,container, false);

        internalMemory = new EditorMemoryState(getActivity()); // Obtain a reference to file loader

        //System.err.println("View Fragment Params" + view.getHeight() + view.getMeasuredHeight());
        //Need width and height of widget pane. And store it. :C


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        widgetPane = (RelativeLayout) getView().findViewById(R.id.widgetPane);
        widgetPane.setOnDragListener(new DragListener());
        editPane = (RelativeLayout) getView().findViewById(R.id.editorPane);
        tvSkinDisplayName = (TextView) getView().findViewById(R.id.tvSkinName);
        etNewSkinName = (EditText) getView().findViewById(R.id.etNewSkinName);
        btSetSkinName = (Button) getView().findViewById(R.id.btSetSkinName);
        btSetSkinName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internalMemory!=null) {
                    String name = etNewSkinName.getText().toString();
                    etNewSkinName.setText("");
                    tvSkinDisplayName.setText(name);
                    // Load into memory and broadcast change back to the ListFragment
                    //currentDisplayPos
                    internalMemory.setCustomName(name, currentDisplayPos-2);
                }
            }
        });

        initWidgetMemory(); // Load in all the generic views
        initEditorViewReferences(); // Link all objects to the view.
    }

    @Override
    public void onStart(){
        super.onStart();
        showEditViewForPos(currentDisplayPos);
        saveWindowDimensions(); //Used back in main activity to scale the widgets back to size
    }

    @Override
    public void onPause(){
        super.onPause();
        saveCurrentEditView();
    }

    private void saveWindowDimensions(){
        int width, height;
        width = widgetPane.getWidth();
        height = widgetPane.getHeight();
        System.err.println("WidgetPane [X,Y] " + width + ", " + height);
        if(width > 0 && height > 0){
            internalMemory.setPanelWidth(width);
            internalMemory.setPanelHeight(height);
        }
    }

    private void initEditorViewReferences(){
        spWidgetPicker = (Spinner) getView().findViewById(R.id.spEditWidget);
        aadValidWidgets = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
        /*ArrayAdapter.createFromResource(getActivity(), R.array.default_widgets, android.R.layout.simple_spinner_item);
        validWidgets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
        setListToAdapter(aadValidWidgets, widgetNames);
        spWidgetPicker.setAdapter(aadValidWidgets);
        spWidgetPicker.setOnItemSelectedListener(new SpinnerWidgetListener());

        sbSize = (SeekBar) getView().findViewById(R.id.sbSize);
        sbSize.setMax(100);
        sbSize.setOnSeekBarChangeListener(new SeekBarListener());

        spColorTheme = (Spinner) getView().findViewById(R.id.spColor);
        aadValidTheme = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
        spColorTheme.setAdapter(aadValidTheme);
        spColorTheme.setOnItemSelectedListener(new SpinnerThemeListener());

        spDisplayFormat = (Spinner) getView().findViewById(R.id.spDisplayFormat);
        aadValidFormat = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<CharSequence>());
        spDisplayFormat.setAdapter(aadValidFormat);
        spDisplayFormat.setOnItemSelectedListener(new SpinnerFormatListener());

        cbVelocity = (CheckBox) getView().findViewById(R.id.cbVelocity);
        cbVelocity.setOnCheckedChangeListener(new CheckBoxListener());
        cbList.add(cbVelocity);
        cbAcceleration = (CheckBox) getView().findViewById(R.id.cbAcceleration);
        cbAcceleration.setOnCheckedChangeListener(new CheckBoxListener());
        cbList.add(cbAcceleration);
        cbRPM = (CheckBox) getView().findViewById(R.id.cbRPM);
        cbRPM.setOnCheckedChangeListener(new CheckBoxListener());
        cbList.add(cbRPM);
        cbDistance = (CheckBox) getView().findViewById(R.id.cbDistance);
        cbDistance.setOnCheckedChangeListener(new CheckBoxListener());
        cbList.add(cbDistance);
        cbHorsepower = (CheckBox) getView().findViewById(R.id.cbHorsepower);
        cbHorsepower.setOnCheckedChangeListener(new CheckBoxListener());
        cbList.add(cbHorsepower);

        btSave = (Button) getView().findViewById(R.id.bSave);
        btSave.setEnabled(false);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentEditView();
                Toast.makeText(getActivity(), "COMPLETE: Changes to the view saved",Toast.LENGTH_SHORT).show();
            }
        });
        btSetView = (Button) getView().findViewById(R.id.bSet);
        btSetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internalMemory.setSkinForPos(currentDisplayPos);
                Toast.makeText(getActivity(), "COMPLETE: Set dashboard to current skin",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initWidgetMemory(){
        widgetNames = new ArrayList<CharSequence>();

        gvVelocity = new GenericView(getActivity());
        gvVelocity.setValue(10);
        gvVelocity.setUnits("km/hr");
        gvVelocity.setAnalogueRange(0,100);
        RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p1.height = 150;
        p1.width = 300;
        //p1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //p1.addRule(RelativeLayout.ALIGN_PARENT_END);
        gvVelocity.setLayoutParams(p1);
        gvVelocity.setOnTouchListener(new TouchListener(0));
        //gvVelocity.onTouchEvent(new Mo);
        widgetPane.addView(gvVelocity);
        widgetNames.add("Velocity");
        fullWidgetList.add(gvVelocity);

        gvAcceleration = new GenericView(getActivity());
        gvAcceleration.setValue(20);
        gvAcceleration.setUnits("m/s2");
        gvAcceleration.setAnalogueRange(-20,20);
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p2.height = 150;
        p2.width = 300;
        //p2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //p2.addRule(RelativeLayout.ALIGN_PARENT_END);
        gvAcceleration.setLayoutParams(p2);
        gvAcceleration.setOnTouchListener(new TouchListener(1));
        widgetPane.addView(gvAcceleration);
        widgetNames.add("Acceleration");
        fullWidgetList.add(gvAcceleration);

        gvRPM = new GenericView(getActivity());
        gvRPM.setValue(30);
        gvRPM.setUnits("rpm");
        gvRPM.setAnalogueRange(0,400);
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p3.height = 150;
        p3.width = 300;
        //p3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //p3.addRule(RelativeLayout.ALIGN_PARENT_END);
        gvRPM.setLayoutParams(p3);
        gvRPM.setOnTouchListener(new TouchListener(2));
        widgetPane.addView(gvRPM);
        widgetNames.add("RPM");
        fullWidgetList.add(gvRPM);

        gvDistance = new GenericView(getActivity(), GenericView.DIGITAL);
        gvDistance.setValue(40);
        gvDistance.setUnits("km");
        RelativeLayout.LayoutParams p4 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p4.height = 150;
        p4.width = 300;
        //p4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //p4.addRule(RelativeLayout.ALIGN_PARENT_END);
        gvDistance.setLayoutParams(p4);
        gvDistance.setOnTouchListener(new TouchListener(3));
        widgetPane.addView(gvDistance);
        widgetNames.add("Distance");
        fullWidgetList.add(gvDistance);

        gvHorsepower = new GenericView(getActivity());
        gvHorsepower.setValue(50);
        gvHorsepower.setUnits("kW");
        gvHorsepower.setAnalogueRange(0,200);
        RelativeLayout.LayoutParams p5 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p5.height = 150;
        p5.width = 300;
        //p5.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //p5.addRule(RelativeLayout.ALIGN_PARENT_END);
        gvHorsepower.setLayoutParams(p5);
        gvHorsepower.setOnTouchListener(new TouchListener(4));
        widgetPane.addView(gvHorsepower);
        widgetNames.add("Horsepower");
        fullWidgetList.add(gvHorsepower);
    }

    //Loads in the view data
    public void showEditViewForPos(int pos){
        currentDisplayPos = pos; // Needed when saving to file. Which edit view it is

        //Issues with Android intialisation scheduling
        if(aadValidTheme != null) {aadValidTheme.clear();}
        if(aadValidFormat != null) {aadValidFormat.clear();}
        if(sbSize != null){sbSize.setProgress(0);}

        Boolean dataLoadable = false;
        JSONObject jso = new JSONObject();
        try {
            if ( (pos == 0) || (pos == 1) )  {
                if (pos==0) {
                    jso = internalMemory.loadDefaultData1();
                } else {
                    jso = internalMemory.loadDefaultData2();
                }
            } else {
                jso = internalMemory.loadEditState(pos-1);
            }
            dataLoadable = true;
        } catch (FileNotFoundException f) {
            System.err.println(f);
        } catch (Exception e) {
            System.err.println(e);
        }


        try {
            if (!dataLoadable) {
                internalMemory.storeDefaultData1();
                internalMemory.storeDefaultData2();
                jso = EditorMemoryState.generateDefault1JSON();
            }

            JSONArray jsa = jso.getJSONArray(EditorMemoryState.CHECKBOX_STATE);
            for(int i = 0; i<jsa.length(); i++){
                boolean checked = jsa.getBoolean(i);
                if(cbList.size()>0){cbList.get(i).setChecked(checked);}
            }

            int tmpSize = 0;
            JSONArray jsaWidgets = jso.getJSONArray(EditorMemoryState.EDITOR_WIDGET_STATE);
            for(int i = 0; i<jsaWidgets.length(); i++){
                GenericView gvWidget = fullWidgetList.get(i);
                JSONObject jsoWidget = jsaWidgets.getJSONObject(i);

                gvWidget.loadViewState(jsoWidget.getJSONObject(EditorMemoryState.EDITOR_GENERIC_VIEW_STATE));

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.MATCH_PARENT);

                if (i == 0) {
                    tmpSize = (int) jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_HEIGHT);
                }

                params.width = (int) jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_WIDTH);
                params.height = (int) jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_HEIGHT);
                gvWidget.setLayoutParams(params);

                gvWidget.setX((float) jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_POS_X));
                gvWidget.setY((float) jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_POS_Y));
            }

            currentEditingView = fullWidgetList.get(0);
            spWidgetPicker.setSelection(0);
            sbSize.setProgress((int)(tmpSize/VIEW_EDITOR_SCALER));
            refreshColorAndFormatSpinner(true);

            if ( (currentDisplayPos == 0) || (currentDisplayPos == 1) ) {
                btSave.setEnabled(false);
                btSetSkinName.setEnabled(false);
                etNewSkinName.setEnabled(false);
                tvSkinDisplayName.setText("Default " + (currentDisplayPos + 1));
            } else {
                btSave.setEnabled(true);
                btSetSkinName.setEnabled(true);
                etNewSkinName.setEnabled(true);
                //Load from memory
                tvSkinDisplayName.setText(internalMemory.getCustomName(pos-2));
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void saveCurrentEditView(){
        try {
            JSONObject jso = new JSONObject();
            JSONArray jsa = new JSONArray();
            for(CheckBox cb : cbList){
                jsa.put(cb.isChecked());
            }
            jso.put(EditorMemoryState.CHECKBOX_STATE, jsa);

            //Array of JSONObjects, representing the different views.
            JSONArray jsaWidgets = new JSONArray();

            for(GenericView gv : fullWidgetList){
                JSONObject jsoWidget = new JSONObject();
                jsoWidget.put(EditorMemoryState.EDITOR_WIDGET_POS_X, gv.getX());
                jsoWidget.put(EditorMemoryState.EDITOR_WIDGET_POS_Y, gv.getY());
                jsoWidget.put(EditorMemoryState.EDITOR_WIDGET_WIDTH, gv.getWidth());
                jsoWidget.put(EditorMemoryState.EDITOR_WIDGET_HEIGHT, gv.getHeight());
                jsoWidget.put(EditorMemoryState.EDITOR_GENERIC_VIEW_STATE, gv.saveViewState());
                jsaWidgets.put(jsoWidget);
            }

            jso.put(EditorMemoryState.EDITOR_WIDGET_STATE, jsaWidgets);

            internalMemory.saveEditState(jso, currentDisplayPos-1);
        } catch (Exception e){

            System.err.println(e);
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progress = (int) (progress * VIEW_EDITOR_SCALER);
            if (progress > 15) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.MATCH_PARENT);
                params.height = progress;
                params.width = progress*2;

                currentEditingView.setLayoutParams(params);
                float xPos = currentEditingView.getX();
                float yPos = currentEditingView.getY();
                if (xPos < 0) { xPos = 0; }
                if (yPos < 0) { yPos = 0; }
                currentEditingView.setX(xPos);
                currentEditingView.setY(yPos);

                System.err.println("P X Y " + progress + ", " + xPos +  ", " + yPos);
            }
        }
    }

    private class SpinnerWidgetListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            currentEditingView = fullWidgetList.get(pos);
            if(!cbList.get(pos).isChecked()){
                Toast.makeText(getActivity(),
                        "NOTE: " + widgetNames.get(pos) + " widget is not currently visible",
                        Toast.LENGTH_LONG).show();
            }
            refreshColorAndFormatSpinner();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private class SpinnerThemeListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            currentEditingView.setTheme(aadValidTheme.getItem(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private class SpinnerFormatListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(("Analogue").equals(aadValidFormat.getItem(pos))){
                currentEditingView.setCurrentState(GenericView.ANALOGUE);
            } else {
                currentEditingView.setCurrentState(GenericView.DIGITAL);
            }
            refreshColorAndFormatSpinner();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public void setListToAdapter(ArrayAdapter<CharSequence> aad, ArrayList<CharSequence> al){
        aad.clear();
        for (CharSequence c : al) {
            aad.add(c);
        }
    }

    private void refreshColorAndFormatSpinner(){
        setListToAdapter(aadValidTheme, currentEditingView.getSupportedColorTheme());
        setListToAdapter(aadValidFormat, currentEditingView.getSupportedDisplayFormat());

        String currentViewTheme = currentEditingView.getTheme();
        String currentViewFormat = currentEditingView.getCurrentState(false);

        spColorTheme.setSelection(aadValidTheme.getPosition(currentViewTheme));
        spDisplayFormat.setSelection(aadValidFormat.getPosition(currentViewFormat));
    }

    private void refreshColorAndFormatSpinner(boolean reset_view){
        refreshColorAndFormatSpinner();
        if (!reset_view) {
            sbSize.setProgress((int) (currentEditingView.getHeight() / VIEW_EDITOR_SCALER));
        }
    }


    private class CheckBoxListener implements CheckBox.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            GenericView gv = fullWidgetList.get(getCheckBoxPosition(buttonView.getId()));
            if(isChecked){
                gv.setVisibility(View.VISIBLE);
            } else {
                gv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private int getCheckBoxPosition(int checkBoxId){
        switch (checkBoxId){
            case R.id.cbVelocity:
                return 0;
            case R.id.cbAcceleration:
                return 1;
            case R.id.cbRPM:
                return 2;
            case R.id.cbDistance:
                return 3;
            case R.id.cbHorsepower:
                return 4;
            default:
                return -1;
        }
    }

    public class TouchListener implements View.OnTouchListener {
        int position = 0;
        public TouchListener(int pos){
            this.position = pos;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            spWidgetPicker.setSelection(position);

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //Add UI Border
                Drawable enterBorder = getResources().getDrawable(R.drawable.highlight_border);
                view.setBackground(enterBorder);

                //setup drag, view is copied to a shadow
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //start dragging the item touched
                view.startDrag(data, shadowBuilder, view, 0);

                //Remove the border on original
                view.setBackground(null);
                return true;
            }
            else {
                return false;
            }
        }
    }

    public class DragListener implements View.OnDragListener {


        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    if(event != null){
                        View view = (View) event.getLocalState();
                        if (view != null) {
                            view.setX(event.getX()-(view.getWidth()/2));
                            view.setY((float)(event.getY()-(view.getHeight()/2)));
                            //v.invalidate();
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }
        }
    }


}
