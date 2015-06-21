package com.projects.team.experimental.virtualdashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.projects.team.experimental.virtualdashboard.Bluetooth.DeviceListActivity;
import com.projects.team.experimental.virtualdashboard.Bluetooth.UartService;
import com.projects.team.experimental.virtualdashboard.Editor.EditorMemoryState;
import com.projects.team.experimental.virtualdashboard.Editor.GenericView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private GenericView gvVelocity;
    private GenericView gvAcceleration;
    private GenericView gvRPM;
    private GenericView gvDistance;
    private GenericView gvHorsepower;
    private ArrayList<GenericView> fullWidgetList = new ArrayList<GenericView>();
    public static final int DEFAULT_WINDOW_WIDTH = 1024;
    public static final int DEFAULT_WINDOW_HEIGHT = 412;
    private static final double DISPLAY_VIEW_SCALING = 1.4;
    private static final int DISPLAY_X_SCALE = 20;
    private static final int DISPLAY_Y_SCALE = 10;
    private static final int DISPLAY_X_TRANSLATION = 0;
    private static final int DISPLAY_Y_TRANSLATION = 0;


    EditorMemoryState internalMemory;
    RelativeLayout virtualDisplay;
    Button btnConnectDisconnect;

    long hiddenTrigggered = 0;
    int hiddenCounter = 0;
    int counterSet = 5;
    int timeOut = 1000; // 1 second

    //Used during requesting startActivityForResult()
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //Keeping track of BL state
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private int mState = UART_PROFILE_DISCONNECTED;

    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private UartService mService = null;

    long lastDataUpdateTime = 0;
    long updateDelayThreshold = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internalMemory = new EditorMemoryState(this);
        internalMemory.settingsPrefs.registerOnSharedPreferenceChangeListener(this);

        virtualDisplay = (RelativeLayout) findViewById(R.id.rlDisplay);
        btnConnectDisconnect = (Button) findViewById(R.id.btConnectDisconnect);
        btnConnectDisconnect.setOnLongClickListener(new BtHiddenDebugListener());
        btnConnectDisconnect.setOnClickListener(new BtClickListener());

        initialiseGenericViews();
        //Load in default view data from sharedPrefs
        loadDisplayViewData();

        //Direct boot
        //Intent i = new Intent(getApplicationContext(), BluetoothDebugger.class);
        //Intent i = new Intent(this, ViewEditor.class);
        //startActivity(i);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        service_init();

    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * Create a ServiceConnection object to bind the Service to the an UARTService instance
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth Service");
                finish();
            }
            Log.d(TAG, "onServiceConnected mService - " + mService);
        }
        public void onServiceDisconnected(ComponentName classname) {
            //mService.disconnect(mDevice);
            //mService.disconnect();
            mService = null;
        }
    };

    /**
     * Return an IntentFilter object to register for UARTService local broadcasts
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    /**
     * Create a LocalBroadcast Receiver object to register/unregister for events from UARTService
     */
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;

            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "BroadcastReceived - GATT CONNECTED");
                        btnConnectDisconnect.setText("Disconnect");
                        mState = UART_PROFILE_CONNECTED;
                        //TODO Implement timer task
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "BroadcastReceived - GATT DISCONNECTED");
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        btnConnectDisconnect.setText("Connect");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                Log.d(TAG, "BroadcastReceived - GATT DISCOVERED");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                mService.enableTXNotification();
            }

            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                Log.d(TAG, "BroadcastReceived - GATT DATA AVAILABLE");
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        UartService.WheelData parsedData = UartService.parseMessage(txValue);

                        if (parsedData.isDataReady()) {
                            lastDataUpdateTime = System.currentTimeMillis();
                            gvVelocity.setValue(parsedData.getSpeed());
                            gvDistance.setValue(parsedData.getDistance());
                            gvAcceleration.setValue(parsedData.getAcceleration());
                            gvHorsepower.setValue(parsedData.getPower());
                            gvRPM.setValue(parsedData.getRPM());
                        }
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                Log.d(TAG, "BroadcastReceived - UART NOT SUPPORTED");
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };



    @Override
    protected void onResume(){
        super.onResume();


        int width, height;
        width = virtualDisplay.getWidth();
        height = virtualDisplay.getHeight();
        System.err.println("Display [X,Y] " + width + ", " + height);

        Log.d(TAG, "onResume()");
        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        /* ViewTarget viewTarget = new ViewTarget(R.id.btConnectDisconnect, this);
        ShowcaseView sv = new ShowcaseView.Builder(this, true)
                .setTarget(viewTarget)
                .setContentTitle("Welcome! \nThis is the Virtual Dashboard")
                .setContentText("To get started, try  connecting to an AMS bluetooth device")
                .setStyle(R.style.CustomShowcaseTheme4)
                .singleShot(42)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

/*                        new ShowcaseView.Builder(getApplicationContext(), true)
                                .setTarget(viewTarget2)
                                .setContentTitle("Menu")
                                .setContentText("More")
                                .singleShot(42)
                                .build();

                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                })
                .build();
        sv.setHideOnTouchOutside(true); */
    }

    private class BtHiddenDebugListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(getApplicationContext(), "Long Pressed", Toast.LENGTH_SHORT).show();
            hiddenTrigggered = System.currentTimeMillis();
            hiddenCounter = 0;
            return true;
        }
    }

    private class BtClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if ( (System.currentTimeMillis() - hiddenTrigggered) < timeOut ) {
                hiddenCounter++;
                hiddenTrigggered = System.currentTimeMillis();
                if (hiddenCounter == counterSet) {
                    hiddenCounter = 0;
                    Intent i = new Intent(getApplicationContext(), BluetoothDebugger.class);
                    startActivity(i);
                }
            } else {
                hiddenCounter = 0;
                if (!mBtAdapter.isEnabled()) {
                    Log.d(TAG, "onClick() - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (mState == UART_PROFILE_DISCONNECTED){
                        Intent newIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        if (mDevice!=null) {
                            mService.disconnect();
                        }
                    }
                }
            }
        }
    }

    /**
     * This is used to parse the ActivityResult after DeviceListActivity returns with BL Addr
     * or when BL activation request has returned
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "onActivityResult deviceAddress - " + mDevice + ": mserviceValue - " + mService);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting...");
                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth activated ", Toast.LENGTH_SHORT).show();
                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        Toast.makeText(this, "BLE - Not supported", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "BLE Activation failed");
                        //btnConnectDisconnect.setEnabled(false);
                    }
                } else {
                    Log.d(TAG, "Error! - BT not enabled");
                    Toast.makeText(this, "ERROR! - BT not activated", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.d(TAG, "0nActivityResult - Incorrect request code");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("Bluetooth Service running in background.\n             " +
                    "Disconnect to exit");
        } else {
            /*new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Virtual Dashboard")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
                    */
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        internalMemory.settingsPrefs.unregisterOnSharedPreferenceChangeListener(this);

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.d(TAG, ignore.toString());
            Log.d(TAG, "LocalBroadcastManager - Unregister Issue in onDestroy()");
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_editor:
                Intent i = new Intent(this, ViewEditor.class);
                startActivity(i);
                return true; // processing consumed
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        System.err.println("Changes in prefs " + sharedPreferences.getInt(EditorMemoryState.SHARED_VIEW_POS, -1));


        if (EditorMemoryState.SHARED_VIEW_POS.equals(key) && (sharedPreferences.getInt(key, -1) != -1)) {
            //Load in new view
            loadDisplayViewData();
        }
    }

    private void initialiseGenericViews(){
        gvVelocity = new GenericView(this);
        gvVelocity.setValue(10);
        gvVelocity.setUnits("km/hr");
        gvVelocity.setAnalogueRange(0,100);
        RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p1.height = 150;
        p1.width = 300;
        gvVelocity.setLayoutParams(p1);
        virtualDisplay.addView(gvVelocity);
        fullWidgetList.add(gvVelocity);

        gvAcceleration = new GenericView(this);
        gvAcceleration.setValue(20);
        gvAcceleration.setUnits("m/s2");
        gvAcceleration.setAnalogueRange(-20,20);
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p2.height = 150;
        p2.width = 300;
        gvAcceleration.setLayoutParams(p2);
        virtualDisplay.addView(gvAcceleration);
        fullWidgetList.add(gvAcceleration);

        gvRPM = new GenericView(this);
        gvRPM.setValue(30);
        gvRPM.setUnits("rpm");
        gvRPM.setAnalogueRange(0,400);
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p3.height = 150;
        p3.width = 300;
        gvRPM.setLayoutParams(p3);
        virtualDisplay.addView(gvRPM);
        fullWidgetList.add(gvRPM);

        gvDistance = new GenericView(this, GenericView.DIGITAL);
        gvDistance.setValue(40);
        gvDistance.setUnits("km");
        RelativeLayout.LayoutParams p4 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p4.height = 150;
        p4.width = 300;
        gvDistance.setLayoutParams(p4);
        virtualDisplay.addView(gvDistance);
        fullWidgetList.add(gvDistance);

        gvHorsepower = new GenericView(this);
        gvHorsepower.setValue(50);
        gvHorsepower.setUnits("kW");
        gvHorsepower.setAnalogueRange(0,200);
        RelativeLayout.LayoutParams p5 = new RelativeLayout.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        p5.height = 150;
        p5.width = 300;
        gvHorsepower.setLayoutParams(p5);
        virtualDisplay.addView(gvHorsepower);
        fullWidgetList.add(gvHorsepower);
    }

    private void loadDisplayViewData(){
        Boolean dataLoadable = false;
        JSONObject viewData = new JSONObject();

        try{
            viewData = internalMemory.loadDisplayState();
            dataLoadable = true;
        } catch (FileNotFoundException f) {
            System.err.println(f);
        } catch (Exception e){
            System.err.println(e);
        }

        try {
            if (!dataLoadable) {
                internalMemory.storeDefaultData1();
                internalMemory.storeDefaultData2();
                viewData = EditorMemoryState.generateDefault1JSON();
            }
        } catch (Exception e){
            System.err.println(e);
        }


        try {
            loadViewData(viewData);
        } catch (Exception e) {
            System.err.println(e);
        }


    }

    private void loadViewData(JSONObject jso) throws JSONException, NoSuchFieldException{

        int windowX, windowY;
        windowX = internalMemory.getPanelWidth();
        windowY = internalMemory.getPanelHeight();

        JSONArray jsa = jso.getJSONArray(EditorMemoryState.CHECKBOX_STATE);
        for(int i = 0; i<jsa.length(); i++){
            boolean checked = jsa.getBoolean(i);
            int vis = (checked)? View.VISIBLE : View.INVISIBLE;
            if(fullWidgetList.size()>0){fullWidgetList.get(i).setVisibility(vis);}
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

            //Apply scaling
            params.width = (int) (jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_WIDTH) * DISPLAY_VIEW_SCALING);
            params.height = (int) (jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_HEIGHT) * DISPLAY_VIEW_SCALING);
            gvWidget.setLayoutParams(params);

            //Apply dynamic percentage scaling
            double scaleX, scaleY;
            scaleX = jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_POS_X);
            scaleX = (scaleX/windowX * DEFAULT_WINDOW_WIDTH) + DISPLAY_X_TRANSLATION;
            scaleY = jsoWidget.getDouble(EditorMemoryState.EDITOR_WIDGET_POS_Y);
            scaleY = (scaleY/windowY * DEFAULT_WINDOW_HEIGHT) + DISPLAY_Y_TRANSLATION;
            gvWidget.setX((float)  scaleX);
            gvWidget.setY((float)  scaleY);
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
