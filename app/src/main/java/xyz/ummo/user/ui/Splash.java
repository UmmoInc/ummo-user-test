package xyz.ummo.user.ui;

import android.app.Activity;
//import android.content.Context;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.Nullable;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import timber.log.Timber;
import xyz.ummo.user.R;
import xyz.ummo.user.Register;
import xyz.ummo.user.ui.signup.RegisterActivity;

public class Splash extends Activity {
    private String splashPrefs = "UMMO_USER_PREFERENCES";
    private int mode = Activity.MODE_PRIVATE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        Context context = this.getApplicationContext();

        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(context,
                        getResources().getString(R.string.mixpanelToken));

        if (mixpanel != null) {
            mixpanel.track("appLaunched");
        }

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ie) {
                Timber.e(" onCreate->%s", ie);
            }
            finish();

            SharedPreferences splashPreferences = getSharedPreferences(splashPrefs, mode);
            boolean signedUp = splashPreferences.getBoolean("SIGNED_UP", false);

            if (signedUp){
                Timber.e("onCreate - User has already signed up");
                startActivity(new Intent(Splash.this, MainScreen.class));
            } else {
                Timber.e("onCreate - User has not signed up yet!");
                startActivity(new Intent(Splash.this, RegisterActivity.class));
            }
            finish();
        }).start();
    }

    @Override
    protected void onDestroy() {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(),
                        getResources().getString(R.string.mixpanelToken));
        mixpanel.flush();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}