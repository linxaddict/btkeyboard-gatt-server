package com.machineinsight_it.btkeyboard.di

import com.machineinsight_it.btkeyboard.ui.HomeActivity
import com.machineinsight_it.btkeyboard.ui.HomeModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [HomeModule::class])
    abstract fun bindHomeActivity(): HomeActivity
}