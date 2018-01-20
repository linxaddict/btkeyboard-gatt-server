package com.machineinsight_it.btkeyboard

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.machineinsight_it.btkeyboard.ble.CONTROLLER_SERVICE
import com.machineinsight_it.btkeyboard.ble.CURRENT_STATE
import com.machineinsight_it.btkeyboard.ble.MediaControllerProfile
import java.io.IOException


class HomeActivity : Activity() {
    // http://nilhcem.com/android-things/bluetooth-low-energy

    lateinit var bluetoothManager: BluetoothManager
    var bluetoothGattServer: BluetoothGattServer? = null
    var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    /* Collection of notification subscribers */
    val registeredDevices = hashSetOf<BluetoothDevice>()

    lateinit var gpio02: Gpio

    /**
     * Callback to receive information about the advertisement process.
     */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "LE Advertise Started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode)
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    startAdvertising()
                    startServer()
                }
                BluetoothAdapter.STATE_OFF -> {
                    stopServer()
                    stopAdvertising()
                }
            }// Do nothing

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/24865120/any-way-to-implement-ble-notifications-in-android-l-preview/25508053#25508053

        setContentView(R.layout.activity_home)

        val manager = PeripheralManagerService()
        gpio02 = manager.openGpio("BCM4")

        // Initialize the pin as an input
        gpio02.setDirection(Gpio.DIRECTION_IN)
        // High voltage is considered active
        gpio02.setActiveType(Gpio.ACTIVE_HIGH)

        System.out.println("current state: " + gpio02.value)

        gpio02.setEdgeTriggerType(Gpio.EDGE_BOTH)
        gpio02.registerGpioCallback(object : GpioCallback() {
            override fun onGpioEdge(gpio: Gpio?): Boolean {
                Log.i("KEYPAD", "button pressed")
                return true
            }

            override fun onGpioError(gpio: Gpio?, error: Int) {
                super.onGpioError(gpio, error)
                System.out.println("gpio error")
            }
        })

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.getAdapter()
        // We can't continue without proper Bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)) {
            finish()
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)
        if (!bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling")
            bluetoothAdapter.enable()
        } else {
            Log.d(TAG, "Bluetooth enabled...starting services")
            startAdvertising()
            startServer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            System.out.println("close gpio")
            gpio02.close()
        } catch (e: IOException) {
            Log.w("BtMediaController", "Unable to close GPIO", e)
        }
    }

    private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean {

        if (bluetoothAdapter == null) {
            Log.w("BtMediaController", "Bluetooth is not supported")
            return false
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w("BtMediaController", "Bluetooth LE is not supported")
            return false
        }

        return true
    }

    private fun startAdvertising() {
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (bluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser")
            return
        }

        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

        val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(CONTROLLER_SERVICE))
                .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    /**
     * Stop Bluetooth advertisements.
     */
    private fun stopAdvertising() {
        if (bluetoothLeAdvertiser == null) return

        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(this, mGattServerCallback)
        if (bluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server")
            return
        }

        bluetoothGattServer?.addService(MediaControllerProfile.createService())
    }

    /**
     * Shut down the GATT server.
     */
    private fun stopServer() {
        if (bluetoothGattServer == null) return

        bluetoothGattServer?.close()
    }

    private val mGattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device)
                //Remove device from any active subscriptions
                registeredDevices.remove(device)
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            var response = ByteArray(8)

            if (CURRENT_STATE == characteristic.uuid) {
                bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        response)
            } else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
                bluetoothGattServer?.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null)
            }
        }
    }
}
