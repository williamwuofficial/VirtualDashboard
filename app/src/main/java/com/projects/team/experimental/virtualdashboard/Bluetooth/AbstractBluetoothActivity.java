package com.projects.team.experimental.virtualdashboard.Bluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.projects.team.experimental.virtualdashboard.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractBluetoothActivity extends Activity {



    @Override
    public abstract void onCreate(Bundle savedInstanceState);


    /*private ServiceConnection mServiceConnection = new ServiceConnection() {
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
    };*/

    /**
     * Return a default IntentFilter object to register for UARTService local broadcasts
     */
    public static IntentFilter makeGattUpdateIntentFilter() {
        List<String> filters = new ArrayList<String>();
        filters.add(UartService.ACTION_GATT_CONNECTED);
        filters.add(UartService.ACTION_GATT_DISCONNECTED);
        filters.add(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        filters.add(UartService.ACTION_DATA_AVAILABLE);
        filters.add(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return makeGattUpdateIntentFilter(filters);
    }

    /**
     * Return a IntentFilter (specified by List of Strings) object
     * to register for UARTService local broadcasts
     */
    public static IntentFilter makeGattUpdateIntentFilter(List<String> filterActions){
        final IntentFilter intentFilter = new IntentFilter();
        for (String s : filterActions) {
            intentFilter.addAction(s);
        }
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

                    }
                });
            }

        }
    };

}
