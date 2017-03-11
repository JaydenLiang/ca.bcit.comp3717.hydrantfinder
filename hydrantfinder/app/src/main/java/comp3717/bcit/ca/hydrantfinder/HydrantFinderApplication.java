package comp3717.bcit.ca.hydrantfinder;

import android.app.Application;

import comp3717.bcit.ca.hydrantfinder.Services.GoogleAPIClientService;


/**
 * Created by jaydenliang on 2017-03-09.
 */

public class HydrantFinderApplication extends Application {
    private GoogleAPIClientService googleAPIClientService;

    public GoogleAPIClientService getGoogleAPIClientService() {
        if (googleAPIClientService == null) {
            googleAPIClientService = GoogleAPIClientService.getInstance(getApplicationContext());
        }
        return googleAPIClientService;
    }
}
