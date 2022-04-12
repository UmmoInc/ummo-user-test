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
    /*fun fromStringArrayListToServiceBenefitArrayList(stringArrayList: String): ArrayList<ServiceBenefit> {
        val serviceBenefitArrayList = ArrayList<ServiceBenefit>()

        for (charValue in stringArrayList) {
            val stringValue = charValue.toString()
            val serviceBenefitTitleBeforeTrimming = stringValue.substringBefore(",")
            val serviceBenefitTitleFinal = serviceBenefitTitleBeforeTrimming.substringAfter("=")
            val serviceBenefitBody = stringValue.substringAfterLast("=")
            val serviceBenefit = ServiceBenefit(serviceBenefitTitleFinal, serviceBenefitBody)
            serviceBenefitArrayList.add(serviceBenefit)
        }
        return serviceBenefitArrayList
    }*/

    /*@TypeConverter
    fun fromServiceBenefitArrayListToStringArrayList(serviceBenefitArrayList: ArrayList<ServiceBenefit>): ArrayList<String> {
        val stringArrayList = ArrayList<String>()

        for (serviceBenefit in serviceBenefitArrayList) {
            stringArrayList.add("benefitTitle=${serviceBenefit.benefitTitle}, benefitBody=${serviceBenefit.benefitBody}")
        }
        return stringArrayList
    }*/

    /*@TypeConverter
    fun fromServiceBenefitArrayListToString(serviceBenefitArrayList: ArrayList<ServiceBenefit>): String {
        var serviceBenefitString: String = ""
        for (serviceBenefit in serviceBenefitArrayList) {
            serviceBenefitString = serviceBenefit.toString()
        }
        return serviceBenefitString
    }*/
}