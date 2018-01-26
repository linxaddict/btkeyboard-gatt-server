package com.machineinsight_it.btkeyboard.ui

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
import com.machineinsight_it.btkeyboard.R
import com.machineinsight_it.btkeyboard.bluetooth.profile.BtKeyboardServiceProfile
import dagger.android.AndroidInjection
import java.io.IOException
import java.util.*
import javax.inject.Inject


class HomeActivity : Activity(), HomeViewAccess {
    @Inject
    lateinit var btKeyboardGattService: BluetoothGattService

    lateinit var bluetoothManager: BluetoothManager

    lateinit var gpio02: Gpio

    var bluetoothGattServer: BluetoothGattServer? = null
    var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    val registeredDevices = hashSetOf<BluetoothDevice>()

    var stateKey1 = BtKeyboardServiceProfile.stateReleased
    var stateKey2 = BtKeyboardServiceProfile.stateReleased
    var stateKey3 = BtKeyboardServiceProfile.stateReleased
    var stateKey4 = BtKeyboardServiceProfile.stateReleased

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
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidInjection.inject(this)

        setContentView(R.layout.activity_home)

        val manager = PeripheralManagerService()
        gpio02 = manager.openGpio("BCM4")

        gpio02.setDirection(Gpio.DIRECTION_IN)
        gpio02.setActiveType(Gpio.ACTIVE_HIGH)
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

        if (!checkBluetoothSupport(bluetoothManager.adapter)) {
            finish()
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)

        if (!bluetoothManager.adapter.isEnabled) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling")
            bluetoothManager.adapter.enable()
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
                .addServiceUuid(ParcelUuid(BtKeyboardServiceProfile.controllerService))
                .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(this, mGattServerCallback)
        if (bluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server")
        }

        bluetoothGattServer?.addService(btKeyboardGattService)
    }

    private fun stopServer() {
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
            val response = when (characteristic.uuid) {
                BtKeyboardServiceProfile.key1Characteristic -> stateKey1
                BtKeyboardServiceProfile.key2Characteristic -> stateKey2
                BtKeyboardServiceProfile.key3Characteristic -> stateKey3
                BtKeyboardServiceProfile.key4Characteristic -> stateKey4
                else -> { null }
            }

            if (response == null) {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                        0, response)
            } else {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                        0, response)
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            if (BtKeyboardServiceProfile.clientConfig == descriptor?.uuid) {
                val returnValue = if (registeredDevices.contains(device)) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }

                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                        0, returnValue)
            } else {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                        0, null)
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (BtKeyboardServiceProfile.controllerService == descriptor?.uuid) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    device?.let { registeredDevices.add(it) }
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    registeredDevices.remove(device)
                }

                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            0, null)
                }
            } else {
                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                            0, null)
                }
            }
        }
    }
}
