package com.flintcore.chat_app_android_22.application;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.utilities.Messages.notications.NotificationManager;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.google.common.base.Objects;

import java.util.Date;

public class AppPrincipal extends Application {
    protected interface Constants {
        //        Preferences
        String KEY_REFERENCES = "glPreferences";
        String KEY_LAST_LOGIN = "lastLogin";

        //        Messaging
        String NOTIFICATIONS_FAIL = "Not notifications available";
    }

    //    Notification manager
    private NotificationManager notificationManager;

    //    Global preferences
    private PreferencesManager globalPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        this.notificationManager = new NotificationManager(this);

        setPreferencesManager();
    }

    /**
     * Null if the context not an activity
     */
    @Nullable
    public NotificationManager getNotificationManager(@NonNull Context context) {
        if (!(context instanceof AppCompatActivity || context instanceof Application)) {
            return null;
        }

        return this.notificationManager;
    }

    private void setPreferencesManager() {
        this.globalPreferences = new PreferencesManager(this, Constants.KEY_REFERENCES);
    }

    /*Set the last logged date, using the actual system date*/
    public synchronized void setLastLoggedInDate() {
        this.globalPreferences.put(Constants.KEY_LAST_LOGIN,
                Long.toString(System.currentTimeMillis()));
    }

    /*Get the saved last logged date, or actual moment if first time.*/
    public synchronized Date getLastLoggedInDate() {
        if (!this.globalPreferences.contains(Constants.KEY_LAST_LOGIN)) {
            setLastLoggedInDate();
        }

        String longStringDate = this.globalPreferences.getString(Constants.KEY_LAST_LOGIN);
        long longDate = Long.parseLong(longStringDate);

        return new Date(longDate);
    }

    /*Save the date */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        this.setLastLoggedInDate();
    }
}
