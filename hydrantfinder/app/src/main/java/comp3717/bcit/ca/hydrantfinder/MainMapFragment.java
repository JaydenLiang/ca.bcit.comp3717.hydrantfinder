package comp3717.bcit.ca.hydrantfinder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import comp3717.bcit.ca.hydrantfinder.ValueObjects.GeoLocHydrants;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        LocationListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int AUTO_UPDATE_LOCATION_INTERVAL = 10000;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap googleMap;
    private MapView mapView;
    private View view;

    private boolean locationServicePermissionGranted = false;
    private boolean mapReady = false;

    private OnFragmentInteractionListener mListener;

    private LocationRequest locationRequest;
    private Location lastLocation;
    private Circle circle;
    private CircleOptions circleOptions;
    private Circle fixedCenteredCircle;
    private boolean hideFixedCenteredCircleOnNextIdle = true;
    private float mapZoomLevelDefault = 15;
    private boolean mapZoomReset = false;
    private int searchRadiusDefault = 300;//circle radius in meters
    private int searchRadiusMin = 100;//circle radius in meters
    private int searchRadiusMax = 1000;//circle radius in meters
    private double searchRadius = searchRadiusDefault;

    private BroadcastReceiver retrieveHydrantsOnLocationReceiver;

    private Bitmap iconHighPress;
    private Bitmap iconLowPress;

    /**
     * a hash map that store Marker - Hydrant key-value pairs
     */
    private HashMap<Marker, HydrantItem> markerMapping;

    public MainMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMapFragment newInstance(String param1, String param2) {
        MainMapFragment fragment = new MainMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        iconLowPress = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_hydrant)
                , 75, 75, false);
        iconHighPress = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_hydrant3)
                , 75, 75, false);

        initRetrieveHydrantsOnLocationEventListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(retrieveHydrantsOnLocationReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_main_map, container, false);
        return this.view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.mapView_main_google_map);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.onFragmentInteraction(this, FragmentInteractionType.FRAGMENT_ON_ATTACH);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * initialize the map component
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setOnCameraMoveListener(this);
        this.googleMap.setOnCameraIdleListener(this);
        this.mapReady = true;
    }

    public void initLocationAutoUpdate(GoogleApiClient googleApiClient) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(AUTO_UPDATE_LOCATION_INTERVAL); // Update location every second

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void initRetrieveHydrantsOnLocationEventListener() {
        retrieveHydrantsOnLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GeoLocHydrants geoLocHydrants = intent.getParcelableExtra("geoLocHydrants");
//                if(lastLocation) {
//                    lastLocation.setAltitude(geoLocHydrants.getGeoLocation().latitude);
//                    lastLocation.setLongitude(geoLocHydrants.getGeoLocation().longitude);
//                }
                //camera center to
                moveCamera(geoLocHydrants.getGeoLocation(), true, geoLocHydrants.getRadius(), false);
                //add markers on the map
                updateHydrantsOnMap(geoLocHydrants);
                Toast.makeText(getContext(), "Found " + geoLocHydrants.getHydrantItems().size() +
                        " Hydrant(s) around Location: " + geoLocHydrants.getGeoLocation().latitude + "," +
                        geoLocHydrants.getGeoLocation().longitude, Toast.LENGTH_LONG).show();
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(retrieveHydrantsOnLocationReceiver, new
                IntentFilter(BroadcastType.LOCAL_RETRIEVE_HYDRANTS_ON_LOCATION));
    }

    /**
     * This function will place map markers on the map where each marker is associated with a hydrant item
     *
     * @param geoLocHydrants an value object that consists of a geo location and a list of hydrants around that
     *                       location.
     */
    public void updateHydrantsOnMap(GeoLocHydrants geoLocHydrants) {
        if (this.googleMap == null || !this.mapReady)
            return;
        //clear the marker-hydrantItem key-value pairs on every update
        if (markerMapping == null) {
            markerMapping = new HashMap<>();
        } else {
            for (Map.Entry<Marker, HydrantItem> entry : markerMapping.entrySet()) {
                entry.getKey().remove();
            }
            markerMapping.clear();
        }
        //update markers
        MarkerOptions markerOptions;
        Marker marker;
        //loop to create markers and key-value pairs
        for (HydrantItem hydrantItem : geoLocHydrants.getHydrantItems()) {
            markerOptions = new MarkerOptions().position(hydrantItem.getGeoLocation()).title(hydrantItem
                    .getDescription()).snippet("Status: good");
            if (hydrantItem.getState() == HydrantItem.STATE_HIGH_PRESS) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconHighPress));
            } else if (hydrantItem.getState() == HydrantItem.STATE_MIDIUM_PRESS) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconHighPress));
            } else
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconLowPress));
            marker = this.googleMap.addMarker(markerOptions);
            //update marker-hydrantItem key-value pairs
            markerMapping.put(marker, hydrantItem);
        }
        hideFixedCenteredCircleOnNextIdle = true;
        updateFixedCenteredCircle(false);
    }

    private void updateFixedCenteredCircle(boolean visible) {
        if (mapReady) {
            if (fixedCenteredCircle != null) {
                fixedCenteredCircle.remove();
            }
            circleOptions = new CircleOptions();
            circleOptions.center(this.googleMap.getCameraPosition().target)
                    .radius(circle != null ? circle.getRadius() : searchRadiusDefault)
                    .strokeColor(Color.argb(128, 66, 194, 244));
            fixedCenteredCircle = this.googleMap.addCircle(circleOptions);
            fixedCenteredCircle.setVisible(visible);
        }
    }

    /**
     * handle user permissions such as allow to show current location or not
     */
    public void updateMyLocation(boolean resetZoomLevel) {
        //TODO request user permission to show current location
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission
                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            this.googleMap.setMyLocationEnabled(true);
            boolean initialChange = this.lastLocation == null;
            lastLocation = ((HydrantFinderApplication) getActivity().getApplication()).getGoogleAPIClientService()
                    .getLastLocation();
            //update hydrants on the map only when location is available.
            if (initialChange) {
                if (lastLocation != null) {
                    moveCamera(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            true, searchRadius, initialChange);
                    //retrieve hydrants around the current location / selected location
                    DataAccessor.getInstance().retrieveHydrantsOnLocation(getContext(), new LatLng(lastLocation
                            .getLatitude(), lastLocation.getLongitude()), searchRadius);
                }
            }
        }
    }

    /**
     * @param location
     * @param showCircle
     * @param circleRadius unit is meter
     */
    private void moveCamera(LatLng location, boolean showCircle, double circleRadius, boolean resetZoomLevel) {
        if (mapReady) {
            //no need to move to the same location
            if (location.longitude == this.googleMap.getCameraPosition().target.longitude
                    && location.latitude == this.googleMap.getCameraPosition().target.latitude) {
                return;
            }
            if (showCircle) {
                //draw circle
                if (circle != null) {
                    circle.remove();
                }
                circleOptions = new CircleOptions();
                circleOptions.center(location)
                        .radius(circleRadius)
                        .strokeColor(Color.argb(255, 66, 194, 244))
                        .fillColor(Color.argb(128, 66, 194, 244));
                circle = this.googleMap.addCircle(circleOptions);
                updateFixedCenteredCircle(false);
            }
            float zoom = resetZoomLevel || mapZoomReset ? mapZoomLevelDefault : this.googleMap.getCameraPosition().zoom;
            float bearing = resetZoomLevel || mapZoomReset ? 0 : this.googleMap.getCameraPosition().bearing;
            float tilt = resetZoomLevel || mapZoomReset ? 0 : this.googleMap.getCameraPosition().tilt;
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(location).zoom(zoom).bearing(bearing).tilt(tilt).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            if (resetZoomLevel && mapZoomReset == false) {
                mapZoomReset = true;
            }
            this.googleMap.animateCamera(cameraUpdate);
        }
    }

    private void cameraCenterToLocation(Location location, boolean showCircle, double circleRadius) {
        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), showCircle, circleRadius, mapZoomReset);
    }

    public void centerToMyCurrentLocation() {
        if (this.lastLocation != null) {
            cameraCenterToLocation(this.lastLocation, true, searchRadius);
        } else {
            Toast.makeText(getContext(), "You need to grant this app permission to access your location in order " +
                    "to show hydrants around you.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Been called when a marker of the map is clicked
     *
     * @param marker the clicked marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
//        HashMap<Marker, HydrantItem> a = new HashMap<>();
        //try to get the hydrant item paired with the marker
        HydrantItem hydrantItem = markerMapping.get(marker);
        //if hydrant item is found, retrieve its info and display it
        if (hydrantItem != null) {
            DataAccessor.getInstance().retrieveHydrantItem(getContext(), hydrantItem.getHydrantId(), true);
            return true;
        }
        return false;
    }

    public int getSearchRadius() {
        return (int) searchRadius;
    }

    public int getSearchRadiusMin() {
        return searchRadiusMin;
    }

    public int getSearchRadiusMax() {
        return searchRadiusMax;
    }

    public int getSearchRadiusDefault() {
        return searchRadiusDefault;
    }

    public void updateRangeCircle() {
        if (circle != null)
            circle.setRadius(this.searchRadius);
    }

    public int getSearchRadiusPercentage() {
        return (int) (this.searchRadius / (this.searchRadiusMax - this.searchRadiusMin) * 100);
    }

    public void setSearchRadiusPercentage(int percentage) {
        this.searchRadius = (double) percentage / 100 * (this.searchRadiusMax - this.searchRadiusMin) + this
                .searchRadiusMin;
    }

    public LatLng getMapCenterLocation() {
        return this.googleMap.getCameraPosition().target;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
    }

    @Override
    public void onCameraMoveStarted(int i) {
    }

    @Override
    public void onCameraMove() {
        fixedCenteredCircle.setVisible(false);
    }

    @Override
    public void onCameraIdle() {
        mapZoomReset = false;
        updateFixedCenteredCircle(!hideFixedCenteredCircleOnNextIdle);
        hideFixedCenteredCircleOnNextIdle = false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Fragment fragment, String interactionType);
    }
}
