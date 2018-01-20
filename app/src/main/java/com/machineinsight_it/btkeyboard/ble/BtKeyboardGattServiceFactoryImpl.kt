package com.machineinsight_it.btkeyboard.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

class BtKeyboardGattServiceFactoryImpl : BtKeyboardGattServiceFactory {
    private fun createCharacteristic(characteristicUuid: UUID, descriptorUuid: UUID): BluetoothGattCharacteristic {
        val characteristic = BluetoothGattCharacteristic(characteristicUuid,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)
        val descriptor = BluetoothGattDescriptor(descriptorUuid, BluetoothGattDescriptor.PERMISSION_READ)
        characteristic.addDescriptor(descriptor)

        return characteristic
    }

    override fun createGattService(): BluetoothGattService {
        val service = BluetoothGattService(BtKeyboardServiceProfile.controllerService,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)

        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key1Characteristic,
                BtKeyboardServiceProfile.key1Descriptor))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key2Characteristic,
                BtKeyboardServiceProfile.key2Descriptor))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key3Characteristic,
                BtKeyboardServiceProfile.key3Descriptor))
        service.addCharacteristic(createCharacteristic(BtKeyboardServiceProfile.key4Characteristic,
                BtKeyboardServiceProfile.key4Descriptor))

        return service
    }
}