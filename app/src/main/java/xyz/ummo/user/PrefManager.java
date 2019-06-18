package xyz.ummo.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class PrefManager  {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "androidhive-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }


    //public boolean isFirstTimeLaunch() {
    //    return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    //}

    boolean isFirstTimeLaunch() {

        return PreferenceManager
                .getDefaultSharedPreferences(this._context)
                .getString("jwt", "")
                .isEmpty();
    }

    public String getUserId(){
        try{
            String jwt =  PreferenceManager
                    .getDefaultSharedPreferences(this._context)
                    .getString("jwt", "")
             .split(Pattern.quote("."))[1];
            return new JSONObject(jwt).getString("_id");
        }catch (JSONException jse){
            return null;
        }

    }
}
