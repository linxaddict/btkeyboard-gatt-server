package com.machineinsight_it.btkeyboard.di

import android.bluetooth.BluetoothGattService
import com.machineinsight_it.btkeyboard.bluetooth.profile.BtKeyboardGattServiceFactory
import com.machineinsight_it.btkeyboard.bluetooth.profile.BtKeyboardGattServiceFactoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BluetoothModule {
    @Provides
    @Singleton
    fun provideBtKeyboardGattServiceFactory(): BtKeyboardGattServiceFactory = BtKeyboardGattServiceFactoryImpl()

    @Provides
    @Singleton
    fun provideBtKeyboardGattService(factory: BtKeyboardGattServiceFactory): BluetoothGattService = factory.createGattService()
}