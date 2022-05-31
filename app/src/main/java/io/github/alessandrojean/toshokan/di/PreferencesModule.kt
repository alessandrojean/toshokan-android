package io.github.alessandrojean.toshokan.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

  @Singleton
  @Provides
  fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)

  @Singleton
  @Provides
  fun provideFlowSharedPreferences(sharedPreferences: SharedPreferences): FlowSharedPreferences =
    FlowSharedPreferences(sharedPreferences)

}