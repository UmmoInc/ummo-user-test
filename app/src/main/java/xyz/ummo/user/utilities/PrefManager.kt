package xyz.ummo.user.utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class PrefManager(private val _context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    var isFirstTimeLaunch: Boolean
        get() = PreferenceManager
                .getDefaultSharedPreferences(_context)
                .getString("jwt", "")
                ?.isEmpty()!!

        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    val userId: String?
        get() = try {
            val jwt = PreferenceManager
                    .getDefaultSharedPreferences(_context)
                    .getString("jwt", "")
                    ?.split(Pattern.quote(".")
                            .toRegex())?.toTypedArray()?.get(1)
            JSONObject(String(Base64.decode(jwt, Base64.DEFAULT))).getString("_id")
        } catch (jse: JSONException) {
            null
        }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "UMMO_USER_PREFERENCES"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    init {
        // shared pref mode
        val PRIVATE_MODE = 0
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
        editor.apply()
    }
}