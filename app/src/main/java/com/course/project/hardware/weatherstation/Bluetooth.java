package com.course.project.hardware.weatherstation;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Bluetooth {

    private final static UUID UUID_TEMPERATURE =
            UUID.fromString(Constants.UUID_TEMPERATURE);
    private final static UUID UUID_HUMIDITY =
            UUID.fromString(Constants.UUID_HUMIDITY);
    private final static UUID UUID_PRESSURE =
            UUID.fromString(Constants.UUID_PRESSURE);
    private final static UUID UUID_ACCELERATION =
            UUID.fromString(Constants.UUID_ACCELERATION);
    private final static UUID UUID_GYROSCOPE =
            UUID.fromString(Constants.UUID_GYROSCOPE);
    private final static UUID UUID_MAGNETIC =
            UUID.fromString(Constants.UUID_MAGNETIC);

    private static final long SCAN_PERIOD = 6000;

    private MainActivity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private DevicesList devicesList;
    private BluetoothDevice mBluetoothDevice;
    private int devicePosition;
    private int status;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<BluetoothGattCharacteristic> charas;

    Bluetooth(MainActivity activity) {
        mActivity = activity;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    devicesList.add(device);
                    ((ConnectFragment) mActivity.getFragment(Constants.CONNECT_PAGE))
                            .getAdapter().notifyDataSetChanged();
                }
            };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        devicesList.setConnectedId(devicePosition);
                        mBluetoothGatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        displayGattServices(mBluetoothGatt.getServices());
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        sendMessage(characteristic);
                    }
                }

            };

    boolean isEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    String getName(){
        if (status == Constants.BT_NOT_FOUND) return "Bluetooth Not Found";
        return mBluetoothAdapter.getName();
    }

    String getAddress(){
        if (mBluetoothAdapter == null) return "00:00:00:00:00:00";
        return mBluetoothAdapter.getAddress();
    }

    DevicesList getDevicesList(){
        return this.devicesList;
    }

    int init() {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            status = Constants.BT_NOT_FOUND;
            return status;
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = 0;
            permissionCheck += mActivity.checkSelfPermission(Manifest.permission.BLUETOOTH);
            permissionCheck += mActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                mActivity.requestPermissions(
                        new String[]{Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN},
                        Constants.ACCESS_BLUETOOTH);
            }

            permissionCheck = 0;
            permissionCheck += mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                mActivity.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.ACCESS_LOCATION);
            }
        }

        devicesList = new DevicesList();

        mHandler = new Handler();
        charas = new ArrayList<>();
        return status;
    }

    int enable() {
        if(status == Constants.BT_IDLE||status == Constants.BT_DISCOVERY) return status;
        devicesList.clear();

        try {
            mBluetoothAdapter.enable();
            status = Constants.BT_IDLE;
        } catch (NullPointerException e) {
            status = Constants.BT_NOT_FOUND;
        }

        return status;
    }

    int disable() {
        if(status == Constants.BT_OFF) return status;
        disconnect();
        devicesList.clear();

        try {
            mBluetoothAdapter.disable();
            status = Constants.BT_OFF;
        } catch (NullPointerException e) {
            status = Constants.BT_NOT_FOUND;
        }
        return status;
    }

    int scan() {
        if(mScanning) return status;
        devicesList.clear();

        scanLeDevice(true);
        status = Constants.BT_DISCOVERY;
        return status;
    }

    int stopScan() {
        if(!mScanning) return status;

        scanLeDevice(false);
        status = Constants.BT_IDLE;
        return status;
    }

    int connect(int id) {
        devicePosition = id;
        scanLeDevice(false);
        return connectLeDevice(devicesList.get(id));
    }

    int disconnect() {
        if(mBluetoothAdapter == null) return status;

        if(mBluetoothGatt!=null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        charas.clear();
        status = Constants.BT_IDLE;
        devicesList.setConnectedId(Constants.INVALID);
        return status;
    }

    void readAll() {
        int exp = 1;
        for(BluetoothGattCharacteristic characteristic : charas) {
            if(characteristic == null) continue;

            exp = (exp!=1) ? exp/2 : 1;
            //exp = 1;
            while(!readCharacteristic(characteristic)) {
                if(exp>128) break;
                try {
                    Thread.sleep(exp * Constants.INTERVAL);
                } catch (InterruptedException e) {
                    Log.e("Bluetooth#readAll", e.toString());
                }
                exp *= 2;
            }
        }

    }

    @SuppressWarnings("deprecation")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mActivity.showSnackbar(mActivity.getResources()
                            .getString(R.string.discovery_finished));
                }
            }, SCAN_PERIOD);

            mScanning = true;
            devicesList.clear();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.discovery_finished));
        }
    }


/*
    private int connectLeDevice(BluetoothDevice device) {
        if(mBluetoothAdapter == null) return status;

        if(device == mBluetoothDevice && mBluetoothGatt != null) {
            if(mBluetoothGatt.connect()) status = Constants.BT_CONNECTED;
            else status = Constants.BT_IDLE;
            return status;
        }
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        mBluetoothDevice = device;
        status = Constants.BT_CONNECTED;
        return status;
    }*/


    private int connectLeDevice(BluetoothDevice device) {
        if(mBluetoothAdapter == null) return status;
        //...
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        mBluetoothDevice = device;
        //...
        return status;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                addCharacteristicToList(gattCharacteristic);
            }

        }

    }

    private void addCharacteristicToList(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }


        if (UUID_TEMPERATURE.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }
        else if (UUID_HUMIDITY.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }
        else if (UUID_PRESSURE.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }
        else if (UUID_ACCELERATION.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }
        else if (UUID_GYROSCOPE.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }
        else if (UUID_MAGNETIC.equals(characteristic.getUuid())) {
            charas.add(characteristic);
        }

    }

    private boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return false;
        }

        if(mBluetoothGatt.readCharacteristic(characteristic))
            Log.d("READ_CHARA", "UUID: "+characteristic.getUuid().toString());
        else
            Log.d("READ_CHARA_NOT_OK", "UUID: "+characteristic.getUuid().toString());

        return true;
    }

    private void sendMessage(final BluetoothGattCharacteristic characteristic) {
        final Handler handler = mActivity.getHandler();
        Message msg = null;
        int messageType = Constants.INVALID;
        Double data;
        ArrayList<Double> data3D;

        if(UUID_TEMPERATURE.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_TEMPERATURE;
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
            data = (double) rawData * 0.1;
            msg = handler.obtainMessage(messageType, data);
        }

        else if(UUID_HUMIDITY.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_HUMIDITY;
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            data = (double) rawData * 0.1;
            msg = handler.obtainMessage(messageType, data);
        }

        else if(UUID_PRESSURE.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_PRESSURE;
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
            data = (double) rawData * 0.01;
            msg = handler.obtainMessage(messageType, data);
        }

        else if(UUID_ACCELERATION.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_ACCELERATION;
            data3D = new ArrayList<>();
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
            data = (double) rawData * 0.0098;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
            data = (double) rawData * 0.0098;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
            data = (double) rawData * 0.0098;
            data3D.add(data);
            msg = handler.obtainMessage(messageType, data3D);
        }

        else if(UUID_GYROSCOPE.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_GYROSCOPE;
            data3D = new ArrayList<>();
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
            data = (double) rawData * Math.PI / 180000;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
            data = (double) rawData * Math.PI / 180000;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
            data = (double) rawData * Math.PI / 180000;
            data3D.add(data);
            msg = handler.obtainMessage(messageType, data3D);
        }

        else if(UUID_MAGNETIC.equals(characteristic.getUuid())) {
            messageType = Constants.MESSAGE_MAGNETIC;
            data3D = new ArrayList<>();
            Integer rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
            data = (double) rawData * 0.1;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
            data = (double) rawData * 0.1;
            data3D.add(data);
            rawData =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
            data = (double) rawData * 0.1;
            data3D.add(data);
            msg = handler.obtainMessage(messageType, data3D);
        }

        if(msg !=null) msg.sendToTarget();
    }

    class DevicesList {

        private ArrayList<BluetoothDevice> devicesList;
        private int connectedId = Constants.INVALID;

        DevicesList() {
            devicesList = new ArrayList<>();
        }

        BluetoothDevice get(int id) {
            if(id>devicesList.size()-1) return null;
            return devicesList.get(id);
        }

        String getName(int id) {
            if(id>devicesList.size()-1) return null;
            String name = devicesList.get(id).getName();
            if(name == null) return mActivity.getResources().getString(R.string.unknown_device);
            return name;
        }

        String getAddress(int id) {
            if(id>devicesList.size()-1) return null;
            return devicesList.get(id).getAddress();
        }

        void add(BluetoothDevice device) {
            if(!devicesList.contains(device)) {
                devicesList.add(device);
            }
        }

        void clear() {
            devicesList.clear();
        }

        int size() {
            return devicesList.size();
        }

        int getConnectedId() {
            return connectedId;
        }

        void setConnectedId(int id) {
            if(id<devicesList.size() || id==Constants.INVALID) {
                connectedId = id;
            }
        }

    }

}
