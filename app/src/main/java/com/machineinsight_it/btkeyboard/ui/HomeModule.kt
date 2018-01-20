package com.machineinsight_it.btkeyboard.ui

import dagger.Module
import dagger.Provides

@Module
class HomeModule {
    @Provides
    fun provideMainAccess(homeActivity: HomeActivity): HomeViewAccess = homeActivity
}