package comp3717.bcit.ca.hydrantfinder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

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
public class MainMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap googleMap;
    private MapView mapView;
    private View view;

    private boolean permissionGetMyCurrentLocation = false;

    private OnFragmentInteractionListener mListener;

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

        handleUserPermissions();

        //retrieve hydrants around the current location / selected location
        //TODO need to get current geo location by goole API, now use BCIT's SE12 as current location
        LatLng currentLoc = new LatLng(49.250024, -123.001528);//BCIT SE12
        updateHydrantsOnMap(DataAccessor.getInstance().retrieveHydrantsOnLocation(currentLoc, 500));
    }

    /**
     * This function will place map markers on the map where each marker is associated with a hydrant item
     *
     * @param geoLocHydrants an value object that consists of a geo location and a list of hydrants around that
     *                       location.
     */
    public void updateHydrantsOnMap(GeoLocHydrants geoLocHydrants) {
        //clear the marker-hydrantItem key-value pairs on every update
        if (markerMapping == null) {
            markerMapping = new HashMap<>();
        } else {
            markerMapping.clear();
        }
        MarkerOptions markerOptions;
        Marker marker;
        //loop to create markers and key-value pairs
        for (HydrantItem hydrantItem : geoLocHydrants.getHydrantItems()) {
            markerOptions = new MarkerOptions().position(hydrantItem.getGeoLocation()).title(hydrantItem
                    .getDescription()).snippet("COMP3717 is awesome!");
            marker = this.googleMap.addMarker(markerOptions);
            //update marker-hydrantItem key-value pairs
            markerMapping.put(marker, hydrantItem);
        }
        CameraPosition cameraPosition = CameraPosition.builder().target(geoLocHydrants.getGeoLocation()).zoom
                (16).bearing(0).tilt(0).build();
        this.googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * handle user permissions such as allow to show current location or not
     */
    private void handleUserPermissions() {
        //TODO request user permission to show current location
    }

    /**
     * Been called when a marker of the map is clicked
     *
     * @param marker the clicked marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        HashMap<Marker, HydrantItem> a = new HashMap<>();
        //try to get the hydrant item paired with the marker
        HydrantItem hydrantItem = markerMapping.get(marker);
        //if hydrant item is found, retrieve its info and display it
        if (hydrantItem != null) {
            DataAccessor.getInstance().retrieveHydrantItem(getContext(), hydrantItem.getHydrantId(), true);
            return true;
        }
        return false;
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
        void onFragmentInteraction(Uri uri);
    }
}
