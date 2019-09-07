package xyz.ummo.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainAct.";
    private static final String ummoUserPreferences = "UMMO_USER_PREFERENCES";
    private int mode = Activity.MODE_PRIVATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        auth.setLanguageCode("en");
        SharedPreferences mainActPrefs = getSharedPreferences(ummoUserPreferences, mode);
        SharedPreferences.Editor editor;
//        editor = mainActPrefs.edit();
        String userNamePref = mainActPrefs.getString("USER_NAME", "");
        Log.e(TAG, "Username->"+userNamePref);
    }
}
