package xyz.ummo.user;

import android.app.Activity;
//import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

public class Splash extends Activity {
    private String TAG = "Splash";
    private String splashPrefs = "UMMO_USER_PREFERENCES";
    private int mode = Activity.MODE_PRIVATE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        //Context context = this.getApplicationContext();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie) {
                    Log.e(TAG, " onCreate->"+ie);
                }
                finish();

                SharedPreferences splashPreferences = getSharedPreferences(splashPrefs, mode);
                boolean signedUp = splashPreferences.getBoolean("SIGNED_UP", false);

                if (signedUp){
                    Log.e(TAG, "onCreate - User has already signed up");
                    startActivity(new Intent(Splash.this, MainScreen.class));
                    finish();
                } else {
                    Log.e(TAG, "onCreate - User has not signed up yet!");
                    startActivity(new Intent(Splash.this, SlideIntro.class));
                    finish();
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}