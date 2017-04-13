package comp3717.bcit.ca.hydrantfinder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.AddressServiceConstants;
import comp3717.bcit.ca.hydrantfinder.Services.GoogleAPIClientService;
import comp3717.bcit.ca.hydrantfinder.Services.ServiceCallback;

import static comp3717.bcit.ca.hydrantfinder.ApplicationConstants.DIALOG_ERROR;
import static comp3717.bcit.ca.hydrantfinder.ApplicationConstants.PERMISSION_REQ_CODE_LOCATION_SERVICE;
import static comp3717.bcit.ca.hydrantfinder.ApplicationConstants.PERMISSION_REQ_RESOLVE_ERROR;

/**
 * This class is to provide with interactions for requesting permission for google service, and handling the failures
 * on some google services, using dialogs.
 */
public class GoogleServicesEnhancedActivity extends AppCompatActivity {
    protected GoogleAPIClientService googleAPIClientService;
    private boolean resolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleAPIClientService = ((HydrantFinderApplication) getApplication()).getGoogleAPIClientService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleAPIClientService.startService();
        } else
            requestForPermission();
    }

    protected void requestForPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                        .ACCESS_FINE_LOCATION}, ApplicationConstants.PERMISSION_REQ_CODE_LOCATION_SERVICE);
            }
        }
    }

    protected final void startGoogleServiceWithSolver() {
        if (googleAPIClientService.getGoogleApiClient().isConnected())
            return;
        googleAPIClientService.addOnceOnConnectionFailedCallback(new GoogleAPIClientConnectionFailureSolver())
                .startService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ_CODE_LOCATION_SERVICE:
                if (permissions.length == 1 &&
                        permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted.
                    //if service connection failed, try to prompt for the user to solve this.
                    //if don't have any solution, call onServiceConnectionFailed() in the end.
                    googleAPIClientService
                            .addOnceOnConnectionFailedCallback(new GoogleAPIClientConnectionFailureSolver())
                            .startService();
                } else {
                    // Permission was denied. Display an error message.
                    Toast.makeText(this, "You need to grant this app permission to access your " +
                            "location in order to show hydrants around you.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return;
        }
    }

    /**
     * show error dialog
     *
     * @param errorCode
     */
    private void showErrorDialog(int errorCode) {
        final ErrorDialogFragment dialogFragment;
        final Bundle args;

        dialogFragment = new ErrorDialogFragment();
        args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "error dialog");
    }

    /**
     * override this method to stop showing a toast message.
     */
    protected void onServiceConnectionFailed() {
        Toast.makeText(this, "Google location service is temporarily unavailable.", Toast.LENGTH_SHORT).show();
    }

    /**
     * start a service
     *
     * @param forIntentService
     */
    protected void startLocationService(Intent forIntentService) {
        if (forIntentService.getParcelableArrayExtra(AddressServiceConstants.RECEIVER) == null) {
            //TODO multi-threading concerns: receiver handler thread is set to null
            forIntentService.putExtra(AddressServiceConstants.RECEIVER, new GeoCodingResultReceiver(null));
        }
        startService(forIntentService);
    }

    protected void onReceiveGeoCodingResult(GeoCodingResultReceiver resultReceiver) {

    }

    public static class ErrorDialogFragment
            extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final int errorCode;

            errorCode = this.getArguments().getInt(DIALOG_ERROR);

            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(),
                    errorCode,
                    PERMISSION_REQ_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((GoogleServicesEnhancedActivity) getActivity()).resolvingError = false;
        }
    }

    class GoogleAPIClientConnectionFailureSolver implements ServiceCallback<ConnectionResult> {
        @Override
        public void run(ConnectionResult callbackParameter) {
            if (resolvingError) {
                return;
            }

            if (callbackParameter.hasResolution()) {
                try {
                    resolvingError = true;
                    callbackParameter.startResolutionForResult(getParent(), PERMISSION_REQ_RESOLVE_ERROR);
                } catch (final IntentSender.SendIntentException e) {
                    ((HydrantFinderApplication) getApplication()).getGoogleAPIClientService().startService();
                }
            } else {
                showErrorDialog(callbackParameter.getErrorCode());
                resolvingError = true;
                ((GoogleServicesEnhancedActivity) getParent()).onServiceConnectionFailed();
            }
        }
    }

    class GeoCodingResultReceiver extends ResultReceiver {
        public GeoCodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            onReceiveGeoCodingResult(this);
        }
    }
}
