package com.machineinsight_it.btkeyboard.bt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

import java.util.UUID

val CONTROLLER_SERVICE: UUID = UUID.fromString("8e5473d9-3a31-4d7b-9ed1-229c95634318")
val CURRENT_STATE: UUID = UUID.fromString("b24b81c5-a85c-44cd-b9c9-f22920e88f0b")
val CURRENT_STATE_DESCRIPTOR_CONFIG: UUID = UUID.fromString("12a6e1ac-c5e1-4cd7-a202-31ff366d816c")


/**
 * @author Marcin Przepiórkowski
 * @since 29.12.2017
 */
object MediaControllerProfile {
    fun createService(): BluetoothGattService {
        val service = BluetoothGattService(CONTROLLER_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val currentState = BluetoothGattCharacteristic(CURRENT_STATE,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)
        val configDescriptor = BluetoothGattDescriptor(CURRENT_STATE_DESCRIPTOR_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)

        currentState.addDescriptor(configDescriptor)

        service.addCharacteristic(currentState)

        return service
    }
}