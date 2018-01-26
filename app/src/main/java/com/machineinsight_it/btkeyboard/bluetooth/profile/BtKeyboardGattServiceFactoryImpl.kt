package com.machineinsight_it.btkeyboard.bluetooth.profile

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

class BtKeyboardGattServiceFactoryImpl : BtKeyboardGattServiceFactory {
    private fun createCharacteristic(characteristicUuid: UUID, descriptorUuid: UUID): BluetoothGattCharacteristic {
        val configDescriptor = BluetoothGattDescriptor(descriptorUuid,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)

        val characteristic = BluetoothGattCharacteristic(characteristicUuid,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)
        characteristic.addDescriptor(configDescriptor)

        return characteristic
    }

    override fun createGattService(): BluetoothGattService {
        val service = BluetoothGattService(BtKeyboardServiceProfile.controllerService,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key1Characteristic,
                BtKeyboardServiceProfile.clientConfig))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key2Characteristic,
                BtKeyboardServiceProfile.clientConfig))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key3Characteristic,
                BtKeyboardServiceProfile.clientConfig))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key4Characteristic,
                BtKeyboardServiceProfile.clientConfig))

        return service
    }
}