package xyz.ummo.user.data.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import timber.log.Timber;

public class Converters {

    @TypeConverter
    public static ArrayList<String> fromString(String value){
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
//        Timber.e("FROM-STRING: LIST-TYPE->%s", new Gson().fromJson(value, listType));

        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<String> list){
        Gson gson = new Gson();
//        Timber.e("FROM-ARRAY-LIST: GSON->%s", gson.toJson(list));
        return gson.toJson(list);
    }
}
