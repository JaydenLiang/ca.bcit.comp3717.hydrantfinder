package comp3717.bcit.ca.hydrantfinder;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.SearchAddressActivity;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.GeoLocHydrants;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

import static comp3717.bcit.ca.hydrantfinder.R.id.fab;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainMapFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = MainMenuActivity.class.getName();
    private static final int PERMISSION_REQ_CODE_LOCATION_SERVICE = 1000;
    private static final int PERMISSION_REQ_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    boolean twice;
    //broadcast message receiver for relocation event
    private BroadcastReceiver addressRelocationReceiver;
    //broadcast message receiver for hydrantItem retrieved event
    private BroadcastReceiver retrieveHydrantItemEventReceiver;
    private MainMapFragment mapFragment;
    private FloatingActionButton showMyLocationButton;
    private GoogleApiClient googleApiClient;
    private boolean resolvingError = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showMyLocationButton = (FloatingActionButton) findViewById(fab);
        showMyLocationButton.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initRelocationEventListener();
        initRetrieveHydrantItemEventListener();

        requestForPermission();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.googleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.googleApiClient.disconnect();
    }

    /**
     * Initialize the event listener for relocation
     */
    private void initRelocationEventListener() {
        addressRelocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GeoLocHydrants geoLocHydrants = intent.getParcelableExtra("geoLocHydrants");
                //add markers on the map
                Toast.makeText(getApplicationContext(), "Found " + geoLocHydrants.getHydrantItems().size() +
                        " Hydrant(s) around Location: " + geoLocHydrants.getGeoLocation().latitude + "," + geoLocHydrants
                        .getGeoLocation().longitude, Toast.LENGTH_LONG).show();
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(addressRelocationReceiver, new
                IntentFilter(BroadcastType.LOCAL_ADDRESS_RELOCATION));
    }

    /**
     * Initialize the event listener for hydrant item retrieval
     */
    private void initRetrieveHydrantItemEventListener() {
        retrieveHydrantItemEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HydrantItem hydrantItem = intent.getParcelableExtra("hydrantItem");
                Toast.makeText(getApplicationContext(), "navigate to show hydrant: id = " + hydrantItem.getHydrantId(),
                        Toast.LENGTH_LONG).show();
                displayHydrantItem(hydrantItem);
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(retrieveHydrantItemEventReceiver,
                new IntentFilter(BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM));
    }

    private void requestForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                        .ACCESS_FINE_LOCATION}, PERMISSION_REQ_CODE_LOCATION_SERVICE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ_CODE_LOCATION_SERVICE:
                if (permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted.
                    buildGoogleApiClient();
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

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mapFragment.initLocationAutoUpdate(googleApiClient);
        mapFragment.updateMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (resolvingError) {
            return;
        }

        if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(this, PERMISSION_REQ_RESOLVE_ERROR);
            } catch (final IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        final ErrorDialogFragment dialogFragment;
        final Bundle args;

        dialogFragment = new ErrorDialogFragment();
        args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "error dialog");
    }

    public void onDialogDismissed() {
        resolvingError = false;
    }

    @Override
    public void onBackPressed() {
        if(twice == true) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            Toast.makeText(MainMenuActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    twice = false;
                }
            }, 1000);
            twice = true;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_report) {
            Intent intentToOpenReportBug = new Intent(this, ReportBug.class);
            startActivity(intentToOpenReportBug);
        } else if (id == R.id.nav_account) {
            Intent intentToOpenAccountManage = new Intent(this, AccountManage.class);
            startActivity(intentToOpenAccountManage);

        } else if (id == R.id.nav_share) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("Text/plain");
                String shareBody = "Your body here";
                String shareSub = "Your subject here";
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                startActivity(Intent.createChooser(myIntent, "Share Using"));
        } else if (id == R.id.nav_sign_in) {
            Intent intentToOpenSignIn = new Intent(this, SignIn.class);
            startActivity(intentToOpenSignIn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * open set filter page
     *
     * @param view
     */
    public void navigateToSetFilterActivity(final View view){
        Log.d(TAG, "enter navigateToSetFilterActivity");
        Intent intentToOpenSetFilter = new Intent(this, SetFilterActivity.class);
        startActivity(intentToOpenSetFilter);
    }

    /**
     * open search address page
     *
     * @param view
     */
    public void navigateToSearchAddressActivity(final View view){
        Log.d(TAG, "enter navigateToSearchAddressActivity");
        Intent intentToOpenSearchAddress = new Intent(this, SearchAddressActivity.class);
        startActivity(intentToOpenSearchAddress);
    }

    /**
     * open display hydrant item page
     *
     * @param hydrantItem hydrant item that's to display
     */
    private void displayHydrantItem(HydrantItem hydrantItem) {
        Log.d(TAG, "enter displayHydrantItem");
        Intent intentToDisplayHydrantItem = new Intent(this, ShowItemActivity.class);
        intentToDisplayHydrantItem.putExtra("hydrantItem", hydrantItem);
        startActivity(intentToDisplayHydrantItem);
    }

    @Override
    public void onFragmentInteraction(android.support.v4.app.Fragment fragment, String interactionType) {
        if (interactionType == FragmentInteractionType.FRAGMENT_ON_ATTACH) {
            mapFragment = (MainMapFragment) fragment;
        }
    }

    @Override
    public void onClick(View v) {
        if (this.mapFragment != null) {
            this.mapFragment.centerToMyCurrentLocation();
        } else {
            Toast.makeText(this, "Sorry, GoogleMap is not available now.", Toast.LENGTH_SHORT).show();
        }
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
            final MainMenuActivity mainActivity;

            mainActivity = (MainMenuActivity) getActivity();
            mainActivity.onDialogDismissed();
        }
    }
}


