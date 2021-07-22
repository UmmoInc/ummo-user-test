package xyz.ummo.user.api

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import io.socket.client.IO
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R.string.*
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import java.net.URISyntaxException

abstract class Login(var context: Context, name: String, email: String, mobile_contact: String, user_pid: String) {

    private var delegatedServiceViewModel: DelegatedServiceViewModel? = null
    private val delegatedServiceEntity = DelegatedServiceEntity()

    init {

        val _user = JSONObject()
                .put("name", name)
                .put("email", email)
                .put("mobile_contact", mobile_contact)
                .put("user_pid", user_pid)

        Fuel.post("/user/login")
                .jsonBody(_user.toString())
                .response { request, response, result ->
                    if (response.statusCode == 200) {
                        val payload = JSONObject(response.body().asString("application/json")).getJSONObject("payload")
                        val jwt = payload.getString("jwt")

                        FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)

                        PreferenceManager
                                .getDefaultSharedPreferences(context)
                                .edit()
                                .putString("jwt", jwt)
                                .putString("user", payload.toString())
                                .apply()

                        initializeSocket(User.getUserId(jwt))
                        //SocketIO.mSocket?.connect()
                        SocketIO.mSocket?.on("connect") {
                            Timber.e("Connected to ")
                        }
                        SocketIO.mSocket?.on("message1") {
                            Timber.e(it[0].toString())
                        }

                        SocketIO.mSocket?.on("service-created") {
                            val intent = Intent(context, MainScreen::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra("SERVICE_ID", JSONObject(it[0].toString())
                                    .getString("_id"))
                            intent.putExtra("SERVICE_AGENT_ID", JSONObject(it[0].toString())
                                    .getString("agent"))
                            intent.putExtra("DELEGATED_PRODUCT_ID", JSONObject(it[0]
                                    .toString()).getString("product"))

                            intent.putExtra("OPEN_DELEGATED_SERVICE_FRAG", 1)
                            Timber.e("SERVICE-created with ID->${JSONObject(it[0].toString())}")

                            val serviceId: String = JSONObject(it[0].toString()).getString("_id")
                            val delegatedProductId: String = JSONObject(it[0].toString()).getString("product")
                            val serviceAgentId: String = JSONObject(it[0].toString()).getString("agent")
                            val serviceProgress: ArrayList<String> = arrayListOf()//JSONArray = JSONObject(it[0].toString()).getJSONArray("progress")
//                val serviceName: String = JSONObject(it[0].toString()).getJSONArray("progress")

                            /*delegatedServiceEntity.serviceId = serviceId
                            delegatedServiceEntity.delegatedProductId = delegatedProductId
                            delegatedServiceEntity.serviceAgentId = serviceAgentId*/
//                delegatedServiceEntity.serviceProgress = serviceProgress //TODO: add real progress
                            Timber.e("Populating ServiceEntity: Agent->${delegatedServiceEntity
                                    .serviceAgentId}; ProductModel->${delegatedServiceEntity.delegatedProductId}")
                            delegatedServiceViewModel?.insertDelegatedService(delegatedServiceEntity)

                            context.startActivity(intent)
                        }

                        SocketIO.mSocket?.on("connect_error") {
                            Timber.e("Socket Connect-ERROR-> ${it[0]}")
                        }

                        SocketIO.mSocket?.on("error") {
                            Timber.e("Socket ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
                        }

                        /*  SocketIO.mSocket?.on("message", Emitter.Listener {
                              Log.e("Message",it[0].toString())
                          })*/
                    }

                    done(response.data, response.statusCode)
                }
    }

    private fun initializeSocket(_id: String) {
        try {
            Timber.e("Trying connection...")
            SocketIO.mSocket = IO.socket(context.getString(serverUrl))
            Timber.e("${context.getString(serverUrl)}/user-$_id")
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if (SocketIO.mSocket == null) {
                Timber.e("Probably not connected")
            } else {
                Timber.e("Probably connected")
            }
        } catch (e: URISyntaxException) {
            Timber.e("URI Syntax Exception -> $e")
        }
    }

    abstract fun done(data: ByteArray, code: Number)

}