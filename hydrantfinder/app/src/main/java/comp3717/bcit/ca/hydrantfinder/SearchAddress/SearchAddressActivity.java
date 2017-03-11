package comp3717.bcit.ca.hydrantfinder.SearchAddress;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;

import comp3717.bcit.ca.hydrantfinder.DataAccessor;
import comp3717.bcit.ca.hydrantfinder.GoogleServicesEnhancedActivity;
import comp3717.bcit.ca.hydrantfinder.R;

public class SearchAddressActivity extends GoogleServicesEnhancedActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private static Context instance;
    private SearchManager searchManager;
    private android.widget.SearchView searchView;
    private ListView searchResultListView;
    private SearchAddressListAdapter searchResultListAdapter;
    private ArrayList<SearchAddressListItem> searchAddressListItems;
    private MenuItem menuItem;
    private DataAccessor dataAccessor;
    private EditText searchInputTextView;
    private LatLngBounds bounds;
    private AutocompleteFilter autocompleteFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        searchInputTextView = (EditText) findViewById(R.id.textview_search_input);
        searchInputTextView.addTextChangedListener(new SearchInputWatcher());

        searchAddressListItems = new ArrayList<>();
        searchResultListAdapter = new SearchAddressListAdapter(SearchAddressActivity.this, searchAddressListItems);

        searchResultListView = (ListView) findViewById(R.id.listview_search_result);
        searchResultListView.setAdapter(searchResultListAdapter);

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        dataAccessor = DataAccessor.getInstance();

        displaySearchHistory();

        if (!googleAPIClientService.getGoogleApiClient().isConnected()) {
            startGoogleServiceWithSolver();
        }

        //TODO should not hardcode here
        bounds = new LatLngBounds(new LatLng(49.182482, -123.053223), new LatLng(49.251329, -122.877098));
        autocompleteFilter = new AutocompleteFilter.Builder().setCountry("CA")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
    }

    private void displaySearchHistory(){
        searchResultListAdapter.clear();
        searchResultListAdapter.addAll(dataAccessor.getHistorySearchAddress());
    }

    public void onSearchAddressListItemSelected(SearchAddressListItem item){
        DataAccessor.getInstance().addHistorySearchAddress(item);
        getPlaceInfo(item.getPlaceId());
//        DataAccessor.getInstance().applySearchAddress(getApplicationContext(), item.getAddress());
//        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_address, menu);
        menuItem = menu.findItem(R.id.menu_item_search_address);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ArrayList<SearchAddressListItem> addressSuggestionListItems = dataAccessor.getAddressSuggestion(newText);
        searchResultListAdapter.clear();
        searchResultListAdapter.addAll(addressSuggestionListItems);
        return false;
    }

    private void updateSearchResultList(AutocompletePredictionBuffer autocompletePredictions) {
        Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
        searchResultListAdapter.clear();
        while (iterator.hasNext()) {
            AutocompletePrediction prediction = iterator.next();
            SearchAddressListItem listItem = new SearchAddressListItem(prediction.getFullText(null).toString()
                    , R.mipmap.ic_launcher);
            listItem.setPlaceId(prediction.getPlaceId());
            searchResultListAdapter.add(listItem);
        }
        autocompletePredictions.release();
    }

    /**
     * Look up an address using google services
     *
     * @param view
     */
    public void applySearch(View view) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (!googleAPIClientService.getGoogleApiClient().isConnected()) {
            return;
        }
        googleAPIClientService.getPlacePredictions(
                searchInputTextView.getText().toString(), bounds, autocompleteFilter)
                .setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                        // If unsuccessful, return
                        if (!autocompletePredictions.getStatus().isSuccess()) {
                            // Deal with failure
                            return;
                        }
                        updateSearchResultList(autocompletePredictions);
                    }
                });
//        Location lastLocation = googleAPIClientService.getLastLocation();
//        EditText editText = (EditText)findViewById(R.id.textview_search_input);
//        if (lastLocation != null) {
//            //call the fetch address process directly
//            Intent intent = AddressLookUpService.getInstance().getLookUpAddressIntent(this, lastLocation);
//            if(intent != null)
//                startLocationService(intent);
//        }
    }

    private void getPlaceInfo(String placeId) {
        Places.GeoDataApi.getPlaceById(googleAPIClientService.getGoogleApiClient(), placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        LatLng location;
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place place = places.get(0);
                            location = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                            places.release();
                            DataAccessor.getInstance().applySearchAddress(getApplicationContext(), location);
                            finish();
                        } else {
                            places.release();
                            Toast.makeText(getApplicationContext(), "Address is currently unavailable to look up. " +
                                    "Please try again later.", Toast.LENGTH_LONG).show();
                            searchResultListAdapter.clear();
                        }
                    }
                });
    }

    private class SearchInputWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
//            PendingResult<AutocompletePredictionBuffer> result = googleAPIClientService.getPlacePredictions(
//                    searchInputTextView.getText().toString(), bounds, autocompleteFilter);

        }
    }
}
