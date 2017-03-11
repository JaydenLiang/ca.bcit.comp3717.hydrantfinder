package comp3717.bcit.ca.hydrantfinder.Services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Vector;

/**
 * Created by jaydenliang on 2017-03-09.
 */

public class GoogleAPIClientService implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {
    private static GoogleAPIClientService instance;
    private Context context;
    private GoogleApiClient googleApiClient;
    private Vector<ServiceCallback> onConnectedCallback = new Vector<>();
    private Vector<ServiceCallback> onConnectionFailedCallback = new Vector<>();

    private GoogleAPIClientService(Context context) {
        this.context = context;
    }

    public GoogleAPIClientService() throws Exception {
        throw new Exception("Service construction is not allowed. Please use getInstance(Context context) instead.");
    }

    public static GoogleAPIClientService getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleAPIClientService(context);

        }
        return instance;
    }

    public final void startService() {
        if (googleApiClient == null) {
            buildGoogleApiClient();
        } else if (!googleApiClient.isConnected())
            googleApiClient.connect();
    }

    public final void stopService() {
        if (googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.connect();
    }

    public GoogleApiClient getGoogleApiClient() {
        if (googleApiClient == null) {
            buildGoogleApiClient();
        }
        return googleApiClient;
    }

    public GoogleAPIClientService addOnceOnConnectedCallback(ServiceCallback callback) {
        if (onConnectedCallback.contains(callback))
            return this;
        onConnectedCallback.add(callback);
        return this;
    }

    public GoogleAPIClientService addOnceOnConnectionFailedCallback(ServiceCallback callback) {
        if (onConnectionFailedCallback.contains(callback))
            return this;
        onConnectionFailedCallback.add(callback);
        return this;
    }

    public Location getLastLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        while (onConnectedCallback.size() > 0) {
            onConnectedCallback.remove(0).run(googleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        while (onConnectionFailedCallback.size() > 0) {
            onConnectionFailedCallback.remove(0).run(connectionResult);
        }
    }

    public PendingResult<AutocompletePredictionBuffer> getPlacePredictions(String query, LatLngBounds bounds,
                                                                           AutocompleteFilter filter) {
        if (googleApiClient == null)
            return null;
        //TODO is there any background thread concern ? Is it able to put this in background async thread?
        return Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query, bounds, filter);
    }
}
