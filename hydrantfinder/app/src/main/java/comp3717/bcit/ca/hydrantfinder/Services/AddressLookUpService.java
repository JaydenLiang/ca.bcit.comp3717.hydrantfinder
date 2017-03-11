package comp3717.bcit.ca.hydrantfinder.Services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.AddressServiceConstants;

/**
 * Created by jaydenliang on 2017-03-09.
 */

public class AddressLookUpService extends IntentService {
    public static String TAG = "AddressLookUpService";
    private static AddressLookUpService instance;
    private Activity activity;
    private boolean serviceStarted = false;

    public AddressLookUpService() {
        super(TAG);
    }

    public static AddressLookUpService getInstance() {
        if (instance == null) {
            instance = new AddressLookUpService();
        }
        return instance;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public Intent getLookUpAddressIntent(Activity activity, Location location) {
        if (activity == null)
            return null;
        Intent intent = new Intent(activity, AddressLookUpService.class);
        intent.putExtra(AddressServiceConstants.INTENT_TYPE_LOOK_UP,
                AddressServiceConstants.LOOK_UP_TYPE_LOCATION_TO_ADDRESS);
        intent.putExtra(AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA, location);
        return intent;
    }

    public void getLookUpAddressIntent(Activity activity, String address, ResultReceiver receiver) {
        if (activity == null)
            return;
        Intent intent = new Intent(activity, AddressLookUpService.class);
        intent.putExtra(AddressServiceConstants.INTENT_TYPE_LOOK_UP,
                AddressServiceConstants.LOOK_UP_TYPE_ADDRESS_SEARCH);
        intent.putExtra(AddressServiceConstants.RECEIVER, receiver);
        intent.putExtra(AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA, address);
        startService(intent);
    }

    public void getLookUpLocationIntent(Activity activity, String address, ResultReceiver receiver) {
        if (activity == null)
            return;
        Intent intent = new Intent(activity, AddressLookUpService.class);
        intent.putExtra(AddressServiceConstants.INTENT_TYPE_LOOK_UP,
                AddressServiceConstants.LOOK_UP_TYPE_ADDRESS_TO_LOCATION);
        intent.putExtra(AddressServiceConstants.RECEIVER, receiver);
        intent.putExtra(AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA, address);
        startService(intent);
    }

    private void onHandleLocationToAddress(Location location, ResultReceiver receiver) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String errorMessage = "";
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    //get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid geo location values. " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude();
            Log.e(TAG, errorMessage, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Address not found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(receiver, AddressServiceConstants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found.");
            deliverResultToReceiver(receiver, AddressServiceConstants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void onHandleAddressToLocation(String address, ResultReceiver receiver) {

    }

    private void onHandleAddressSearch(String address, ResultReceiver receiver) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the location passed to this service through an extra.
        String lookUpType = intent.getStringExtra(AddressServiceConstants.INTENT_TYPE_LOOK_UP);
        String address;
        ResultReceiver receiver;
        switch (lookUpType) {
            case AddressServiceConstants.LOOK_UP_TYPE_LOCATION_TO_ADDRESS:
                Location location = intent.getParcelableExtra(
                        AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA);
                receiver = intent.getParcelableExtra(AddressServiceConstants.RECEIVER);
                onHandleLocationToAddress(location, receiver);
                break;
            case AddressServiceConstants.LOOK_UP_TYPE_ADDRESS_TO_LOCATION:
                address = intent.getStringExtra(
                        AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA);
                receiver = intent.getParcelableExtra(AddressServiceConstants.RECEIVER);
                onHandleAddressToLocation(address, receiver);
                break;
            case AddressServiceConstants.LOOK_UP_TYPE_ADDRESS_SEARCH:
                address = intent.getStringExtra(
                        AddressServiceConstants.INTENT_EXTRA_LOOK_UP_DATA);
                receiver = intent.getParcelableExtra(AddressServiceConstants.RECEIVER);
                onHandleAddressSearch(address, receiver);
                break;
            default:
                Log.e(TAG, "no matched look-up type found: " + lookUpType);
        }
    }

    private void deliverResultToReceiver(ResultReceiver receiver, int resultCode, String message) {
        if (receiver == null)
            return;
        Bundle bundle = new Bundle();
        bundle.putString(AddressServiceConstants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
