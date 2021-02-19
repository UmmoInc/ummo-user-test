package xyz.ummo.user.api

import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.core.FuelManager
import timber.log.Timber

abstract class Logout(context: Context) {
    init {
        FuelManager.instance.baseHeaders = mapOf()
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .remove("jwt")
                .apply()

        Timber.e("Logging out!")
        done()
    }

    abstract fun done()

}