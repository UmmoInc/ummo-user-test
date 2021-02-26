package xyz.ummo.user.data.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object Converters {
    @JvmStatic
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        val listType = object : TypeToken<ArrayList<String?>?>() {}.type
        //        Timber.e("FROM-STRING: LIST-TYPE->%s", new Gson().fromJson(value, listType));
        return Gson().fromJson(value, listType)
    }

    @JvmStatic
    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        val gson = Gson()
        //        Timber.e("FROM-ARRAY-LIST: GSON->%s", gson.toJson(list));
        return gson.toJson(list)
    }
}