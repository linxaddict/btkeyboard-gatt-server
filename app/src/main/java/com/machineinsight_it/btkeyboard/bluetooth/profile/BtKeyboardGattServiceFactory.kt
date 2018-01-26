package com.machineinsight_it.btkeyboard.bluetooth.profile

import android.bluetooth.BluetoothGattService

interface BtKeyboardGattServiceFactory {
    fun createGattService(): BluetoothGattService
}