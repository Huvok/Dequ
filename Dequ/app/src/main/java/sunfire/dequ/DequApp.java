package sunfire.dequ;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class DequApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }
}
