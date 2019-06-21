package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.core.FuelManager

abstract class Logout(context: Context) {
    init {
        FuelManager.instance.baseHeaders = mapOf()
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .remove("jwt")
                .apply()
        done()
    }

    abstract fun done()

}