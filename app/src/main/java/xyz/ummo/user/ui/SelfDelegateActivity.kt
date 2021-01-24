package xyz.ummo.user.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.delegate.GetService
import xyz.ummo.user.delegate.NewlyDelegated
import xyz.ummo.user.utilities.PrefManager
import java.util.*

class SelfDelegateActivity : AppCompatActivity() {

    private val TAG = "SelfDelegateActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_delegate)
        handlingDeepLinks()
    }
    fun handlingDeepLinks() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    var deepLink: Uri? = null

                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }

                    Log.e(TAG, "Deeplink ${deepLink}")

                    if (PrefManager(this).isFirstTimeLaunch) {
                        // Finish sign up and load service  and forward to service
                        object : NewlyDelegated(this, deepLink!!.toString().split("?")[1]) {
                            override fun done(data: ByteArray, code: Number) {
                                Log.e(TAG, String(data))
                            }
                        }
                    } else {
                        val service_id = deepLink!!
                                .toString()
                                .split("?")[1]
                                .split("&")[1]
                                .split("=")[1]

                        Log.e(TAG, service_id)
                        object : GetService(this, service_id) {
                            override fun done(data: ByteArray, code: Number) {
                                val intent = Intent(applicationContext, MainScreen::class.java)
                                if (code != 200) {
                                    return
                                }
                                Timber.e("Done: ${String(data)}")

                                //TODO: To FIX!
                                /*try {
                                    val repo = AppRepository(application)
                                    val entity = DelegatedServiceEntity()
                                    val obj = JSONObject(String(data))
                                    val s: JSONObject = obj
                                    val p = s.getJSONObject("product")
                                    entity.delegatedProductId = s.getJSONObject("product").getString("_id")
                                    entity.serviceAgentId = s.getJSONObject("agent").getString("_id")
                                    entity.serviceId = s.getString("_id")
                                    entity.serviceProgress = listFromJSONArray(s.getJSONArray("progress"))
                                    repo.insertDelegatedService(entity)

                                    val productEntity = ProductEntity()
                                    productEntity.isDelegated = true
                                    productEntity.productCost = p.getJSONObject("requirements").getString("procurement_cost")
                                    productEntity.productDescription = "";
                                    if (p.has("product_description")) {
                                        productEntity.productDescription = p.getString("product_description")
                                    }
                                    *//*productEntity.productDocuments = listFromJSONArray(p.getJSONObject("requirements").getJSONArray("documents"))
                                    productEntity.productDuration = p.getString("duration")*//*
                                    productEntity.productId = p.getString("_id")
                                    productEntity.productName = p.getString("product_name")
                                    productEntity.productProvider = p.getString("public_service")
                                    productEntity.productSteps = listFromJSONArray(p.getJSONArray("procurement_process"))
                                    repo.insertProduct(productEntity)


                                    val arr = obj.getJSONArray("progress")
                                    intent.putExtra("SERVICE_ID", obj.getString("_id"))
                                    intent.putExtra("SERVICE_AGENT_ID", obj.getString("agent"))
                                    intent.putExtra("DELEGATED_PRODUCT_ID", obj.getJSONObject("product").getString("_id"))
                                    intent.putExtra("OPEN_DELEGATED_SERVICE_FRAG", 1)
                                    intent.putExtra("progress", arr.toString())
                                    startActivity(intent)
                                } catch (err: JSONException) {
                                    Timber.e(err.toString())
                                }*/

                            }
                        }
                    }
                    Timber.e(" onSuccess Deep Link ->%s", deepLink!!.toString().split("?")[1])


                }
                .addOnFailureListener(this) { e -> Timber.e(" onFailure ->$e") }
    }

    private fun listFromJSONArray(arr: JSONArray): ArrayList<String> {
        return try {
            val tbr = ArrayList<String>()
            for (i in 0 until arr.length()) {
                tbr.add(arr.getString(i))
            }
            tbr
        } catch (e: JSONException) {
            ArrayList()
        }
    }
}
