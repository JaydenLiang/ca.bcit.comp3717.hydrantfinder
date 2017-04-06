package comp3717.bcit.ca.hydrantfinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.SearchAddressActivity;
import comp3717.bcit.ca.hydrantfinder.Services.GoogleAPIClientService;
import comp3717.bcit.ca.hydrantfinder.Services.ServiceCallback;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.GeoLocHydrants;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

import static comp3717.bcit.ca.hydrantfinder.R.id.fab;

public class MainMenuActivity extends GoogleServicesEnhancedActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainMapFragment.OnFragmentInteractionListener,
        View.OnClickListener {
    private static final String TAG = MainMenuActivity.class.getName();
    boolean twice;
    //broadcast message receiver for relocation event
    private BroadcastReceiver addressRelocationReceiver;
    //broadcast message receiver for hydrantItem retrieved event
    private BroadcastReceiver retrieveHydrantItemEventReceiver;
    private MainMapFragment mapFragment;
    private FloatingActionButton showMyLocationButton;
    private boolean resolvingError = false;
    private GoogleAPIClientService googleAPIClientService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showMyLocationButton = (FloatingActionButton) findViewById(fab);
        showMyLocationButton.setOnClickListener(this);
        showMyLocationButton.setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SeekBar seekBar = (SeekBar) findViewById(R.id.vertical_seekbar_filter_radius);
        seekBar.setProgress(mapFragment.getSearchRadiusPercentage());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("onProgressChanged:" + progress);
                mapFragment.setSearchRadiusPercentage(progress);
                mapFragment.updateRangeCircle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("onStopTrackingTouch");
                searchAtMapCenter(null);
            }
        });

        initRelocationEventListener();
        initRetrieveHydrantItemEventListener();

        googleAPIClientService = ((HydrantFinderApplication) getApplication()).getGoogleAPIClientService();
        googleAPIClientService.addOnceOnConnectedCallback(new GoogleAPIClientConnectedCallback());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleAPIClientService.stopService();
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
        intentToOpenSearchAddress.putExtra("searchRadius", mapFragment.getSearchRadius());
        startActivity(intentToOpenSearchAddress);
    }

    public void searchAtMapCenter(final View view) {
        DataAccessor.getInstance().applySearchAddress(getApplicationContext(), mapFragment.getMapCenterLocation(),
                mapFragment.getSearchRadius());
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

    public class GoogleAPIClientConnectedCallback implements ServiceCallback<GoogleApiClient> {
        @Override
        public void run(GoogleApiClient googleApiClient) {
            mapFragment.initLocationAutoUpdate(googleApiClient);
            mapFragment.updateMyLocation(true);
        }
    }
}


