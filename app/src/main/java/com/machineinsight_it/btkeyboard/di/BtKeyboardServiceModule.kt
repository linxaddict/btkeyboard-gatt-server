package com.machineinsight_it.btkeyboard.di

import android.bluetooth.BluetoothGattService
import com.machineinsight_it.btkeyboard.ble.BtKeyboardGattServiceFactory
import com.machineinsight_it.btkeyboard.ble.BtKeyboardGattServiceFactoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BtKeyboardServiceModule {
    @Provides
    @Singleton
    fun provideBtKeyboardGattServiceFactory(): BtKeyboardGattServiceFactory = BtKeyboardGattServiceFactoryImpl()

    @Provides
    @Singleton
    fun provideBtKeyboardGattService(factory: BtKeyboardGattServiceFactory): BluetoothGattService = factory.createGattService()
}