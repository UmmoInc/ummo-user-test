package xyz.ummo.user.api

import android.app.Activity
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.perf.metrics.AddTrace
import timber.log.Timber

abstract class GetCategorySummary(private val activity: Activity) {
    init {
        getCatSum()
    }

    @AddTrace(name = "get_category_summary")
    private fun getCatSum() {
        Fuel.get("/product/summary?update_type=1")
            .response { request, response, result ->
                activity.runOnUiThread {
                    if (response.data.isNotEmpty()) {
                        done(response.data, response.statusCode)
                        Timber.e("CAT SUM -> ${String(response.data)}")
                        Timber.e("CAT SUM CODE -> ${response.statusCode}")
                    } else
                        Timber.e("EMPTY RESPONSE")
                }
            }
    }

    abstract fun done(data: ByteArray, code: Number)
}