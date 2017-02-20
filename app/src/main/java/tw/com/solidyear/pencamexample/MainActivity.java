package tw.com.solidyear.pencamexample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;



public class MainActivity extends AppCompatActivity {

    private static final String myTAG = "penCamera_app";
    private static final String ACTION_USB_PERMISSION =
            "tw.com.solidyear.pencamexample.USB_PERMISSION";
    private static final int REQ_TYPE_SET = 0x21;
    private static final int REQ_TYPE_GET = 0xa1;
    private static final int UVC_RC_UNDEFINED = 0x00;
    private static final int UVC_SET_CUR = 0x01;
    private static final int UVC_GET_CUR = 0x81;
    private static final int UVC_GET_MIN = 0x82;
    private static final int UVC_GET_MAX = 0x83;
    private static final int UVC_GET_RES = 0x84;
    private static final int UVC_GET_LEN = 0x85;
    private static final int UVC_GET_INFO = 0x86;
    private static final int UVC_GET_DEF = 0x87;

    private static final int UVC_XU_CTRL_ID = 0x06;
    private static final int AUV_XU_EXT_ROM = 0x03;
    private static final int AUV_XU_REG = 0x06;
    private static final int AUV_XU_REG_SIZE = 11;

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    IntentFilter filterAttached_and_Detached = null;
    UsbManager mUsbManager = null;
    UsbInterface mUsbInterface = null;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(myTAG, "onReceive: " + action);
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {
                        Log.d(myTAG, "DEATTCHED-" + device);
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(myTAG, "ATTACHED-" + device);
                        }
                    } else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    Log.i(myTAG, "called");
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    mUsbInterface = device.getInterface(0);
                    Log.d(myTAG, "bInterfaceNumber: " + mUsbInterface.getId() + "\n" +
                            "EndpointCount: " + mUsbInterface.getEndpointCount() + "\n" +
                            "bInterfaceClass: " + mUsbInterface.getInterfaceClass() + "\n" +
                            "bInterfaceSubClass: " + mUsbInterface.getInterfaceSubclass() + "\n" +
                            "bInterfaceProtocol: " + mUsbInterface.getInterfaceProtocol() + "\n" +
                            "%s" + mUsbInterface.toString());
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.i(myTAG, "PERMISSION-" + device);
                        // Example of a call to a native method
//                      TextView tv = (TextView) findViewById(R.id.sample_text);
//                      tv.setText(stringFromJNI());


                        try {
                            UsbDeviceConnection dc;
                            dc = mUsbManager.openDevice(device);

                            if (dc != null) {
//                                if (dc.claimInterface(mUsbInterface, true)) {
//                                    Log.d(myTAG, "claim interface success!");
//                                    byte data[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//                                    if (dc.controlTransfer(REQ_TYPE_GET, UVC_GET_LEN, UVC_XU_CTRL_ID << 8, AUV_XU_REG << 8, data, 2, 0) < 0) {
//                                        Log.e(myTAG, "transfer error!");
//                                    } else {
//                                        Log.d(myTAG, "Data: " + data[0] + " "
//                                                + data[1] + " "
//                                                + data[2] + " "
//                                                + data[3] + " ");
//                                    }
//                                } else {
//                                    Log.e(myTAG, "claim interface failed.");
//                                }
                                Log.d(myTAG, "Usb Device Connection acquired.");
                                PenCameraXuCtrl xu = new PenCameraXuCtrl(dc, device);

                                xu.getHotkeyStatus();

                                Log.d(myTAG, "uid: " + xu.getUIDS());
                            }
                        } catch (Exception e) {
                            Log.d(myTAG, "exception: " + e);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(myTAG, deviceList.size() + " USB device(s) found");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (device.getVendorId() == 0x60B && device.getProductId() == 0x8074) {
                Log.d(myTAG, "found PenCamera!");
                synchronized (this) {
                    PendingIntent mPermissionIntent;
                    mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
