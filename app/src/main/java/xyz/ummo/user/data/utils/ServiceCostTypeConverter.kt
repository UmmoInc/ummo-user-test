package xyz.ummo.user.data.utils

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import xyz.ummo.user.models.ServiceCostModel

object ServiceCostTypeConverter {
    @JvmStatic
    @TypeConverter
    fun toServiceCostArrayList(string: String): ArrayList<ServiceCostModel> {
        val listType = object : TypeToken<ArrayList<ServiceCostModel>>() {}.type
        return GsonBuilder().create().fromJson(string, listType)
    }
    /*fun fromStringToServiceCostModel(serviceCostString: String): ServiceCostModel {
        val serviceSpec = serviceCostString.substring(0, serviceCostString.indexOf("E"))
        val specCost = serviceCostString.substring(serviceCostString.indexOf("E")).toInt()
        return ServiceCostModel(serviceSpec, specCost)
    }*/

    @JvmStatic
    @TypeConverter
    fun toServiceCostString(serviceCostArrayList: ArrayList<ServiceCostModel>): String {
        return GsonBuilder().create().toJson(serviceCostArrayList)
    }
    /*fun fromServiceCostModelToString(serviceCostModel: ServiceCostModel): String {
        val serviceSpec = serviceCostModel.serviceSpec
        val specCost = serviceCostModel.specCost
        return serviceSpec + "E" + specCost
    }*/
}