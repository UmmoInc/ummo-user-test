package xyz.ummo.user.data.utils

import androidx.room.TypeConverter
import xyz.ummo.user.models.ServiceCostModel

class ServiceCostTypeConverter {
    @TypeConverter
    fun fromStringToServiceCostModel(serviceCostString: String): ServiceCostModel {
        val serviceSpec = serviceCostString.substring(0, serviceCostString.indexOf("E"))
        val specCost = serviceCostString.substring(serviceCostString.indexOf("E")).toInt()
        return ServiceCostModel(serviceSpec, specCost)
    }

    @TypeConverter
    fun fromServiceCostModelToString(serviceCostModel: ServiceCostModel): String {
        val serviceSpec = serviceCostModel.serviceSpec
        val specCost = serviceCostModel.specCost
        return serviceSpec + "E" + specCost
    }
}