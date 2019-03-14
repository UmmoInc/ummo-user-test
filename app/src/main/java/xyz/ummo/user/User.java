package xyz.ummo.user;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

//import com.parse.Parse;

public class User extends Application {
    private static User user;

    @Override
    public void onCreate() {
        super.onCreate();

        setUser();

        /*Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("ummo-delegate-dev-server")
                .clientKey("")
                .server("https://ummo-dev.herokuapp.com/parse")
                .build()
        );*/
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void setUser() {
        user = this;
    }

    public static User getUser() {
        return user;
    }
}
