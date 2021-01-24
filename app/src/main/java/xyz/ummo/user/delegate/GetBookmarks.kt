package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import timber.log.Timber
import xyz.ummo.user.R

abstract class GetBookmarks(var context: Context, userContact: String) {

    init {
        Fuel.put("${context.getString(R.string.serverUrl)}/api/get_bookmarks")
                .jsonBody(userContact)
                .response { request, response, result ->
//                    (context as Activity).runOnUiThread {
                        done(response.data, response.statusCode)

                        if (response.statusCode == 200) {
                            Timber.e("Fetching User Bookmarks -> ${response.data}")
                        } else {
                            Timber.e("Status Code -> ${response.statusCode}")
                        }

//                    }
                }
    }

    abstract fun done(data: ByteArray, code: Number)
}