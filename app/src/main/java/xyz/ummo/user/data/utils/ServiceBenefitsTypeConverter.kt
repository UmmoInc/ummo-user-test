package xyz.ummo.user.data.utils

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import xyz.ummo.user.models.ServiceBenefit

class ServiceBenefitsTypeConverter {

    @TypeConverter
    fun toServiceBenefitArrayList(string: String): ArrayList<ServiceBenefit> {
        val listType = object : TypeToken<ArrayList<ServiceBenefit>>() {}.type
        return GsonBuilder().create().fromJson(string, listType)
    }

    @TypeConverter
    fun toServiceBenefitString(serviceBenefitArrayList: ArrayList<ServiceBenefit>): String {
        return GsonBuilder().create().toJson(serviceBenefitArrayList)
    }
}