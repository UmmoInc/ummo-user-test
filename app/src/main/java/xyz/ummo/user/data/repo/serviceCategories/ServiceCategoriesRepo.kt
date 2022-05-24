package xyz.ummo.user.data.repo.serviceCategories

import android.app.Activity
import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.dao.CategoryDao
import xyz.ummo.user.data.db.ServiceCategoryDatabase
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.utilities.serviceCategoryName
import xyz.ummo.user.utilities.serviceCategoryTotal
import java.util.concurrent.TimeUnit

/** This repo will get data from DB and/or our API and propagate it to the viewModel associated **/
class ServiceCategoriesRepo(
    val db: ServiceCategoryDatabase,
    private val activity: Activity
) {
    private lateinit var serviceCategoryEntity: ServiceCategoryEntity
    private var serviceCategoryDao: CategoryDao
    private var categoryDatabase = ServiceCategoryDatabase.invoke(activity.applicationContext)

    private var serviceCategoriesArrayList = ArrayList<ServiceCategoryEntity>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES).build()

    init {
        serviceCategoryDao = categoryDatabase.serviceCategoryDao()!!
    }

    /** Auth JWT **/
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        .getString("jwt", "").toString()

    /** - Using an OkHttp request, we're fetching service data from the server and returning a
     * string value to be parsed later by [parseServicesStringReturnServiceEntityArrayList].
     *
     * - We currently need the [jwt] to authenticate our API request. **/
    private fun fetchAllServiceCategoriesFromServer(): String {
        val request = Request.Builder()
            .url("${activity.resources.getString(R.string.serverUrl)}/product/summary?update_type=1")
            .header("jwt", jwt)
            .build()

        return client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
    }

    private suspend fun parseServiceCategoriesReturnServiceCategoryArrayList():
            ArrayList<ServiceCategoryEntity> {
        val serviceCategoryResponse = withContext(Dispatchers.IO) {
            fetchAllServiceCategoriesFromServer()
        }

        if (serviceCategoryResponse.isNotEmpty()) {
            val allServiceCategories = JSONObject(serviceCategoryResponse).getJSONArray("payload")
            Timber.e("SERVICE CATS -> $allServiceCategories")
            var serviceCategory: JSONObject

            try {
                for (i in 0 until allServiceCategories.length()) {
                    serviceCategory = allServiceCategories[i] as JSONObject
                    serviceCategoryName = serviceCategory.getString("_id")
                    serviceCategoryTotal = serviceCategory.getInt("total")
                    serviceCategoryEntity = ServiceCategoryEntity(
                        serviceCategoryName,
                        serviceCategoryTotal
                    )

                    serviceCategoriesArrayList.add(serviceCategoryEntity)
                    Timber.e("SERVICE CATEGORIES -> $serviceCategoryEntity")
                }
            } catch (jse: JSONException) {
                Timber.e("FAILED TO PARSE SERVICE CATEGORIES -> $jse")
            }
        }
        return serviceCategoriesArrayList
    }

    suspend fun saveServiceCategoriesInRoom() {
        val mServiceCategoriesArrayList = parseServiceCategoriesReturnServiceCategoryArrayList()
        for (serviceCategoryEntity in mServiceCategoriesArrayList) {
            serviceCategoryDao.upsertCategory(serviceCategoryEntity)
        }
    }

    fun getLocallyStoredServiceCategories(): ArrayList<ServiceCategoryEntity> {
        return serviceCategoryDao.serviceCategoryList as ArrayList<ServiceCategoryEntity>
    }
}