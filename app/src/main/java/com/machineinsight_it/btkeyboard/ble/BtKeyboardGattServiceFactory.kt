package com.machineinsight_it.btkeyboard.ble

import android.bluetooth.BluetoothGattService

interface BtKeyboardGattServiceFactory {
    fun createGattService(): BluetoothGattService
}