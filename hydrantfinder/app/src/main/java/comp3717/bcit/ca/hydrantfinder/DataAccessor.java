package comp3717.bcit.ca.hydrantfinder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.SearchAddressListItem;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.GeoLocHydrants;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

/**
 * This class provides methods to use data from backend
 * Created by jaydenliang on 2017-01-21.
 */
public class DataAccessor {
    private static DataAccessor instance;
    private ArrayList<SearchAddressListItem> historySearchAddressListItems;
//    private

    public DataAccessor() {
        this.historySearchAddressListItems = new ArrayList<>();
    }

    /**
     * Get the instance of the DataAccessor. This is a thread safe static method.
     *
     * @return
     */
    public synchronized static DataAccessor getInstance() {
        if(instance == null){
            instance = new DataAccessor();
        }
        return instance;
    }

    /**
     * When typing in an address, this method a list of suggested addresses
     *
     * @param query whatever text that will be looked up as an address
     * @return
     */
    public ArrayList<SearchAddressListItem> getAddressSuggestion(String query) {
        ArrayList<SearchAddressListItem> searchResult = new ArrayList<>();
        SearchAddressListItem item;
        item = new SearchAddressListItem(query.trim() + " BC, Canada", R.mipmap.ic_launcher);
        searchResult.add(item);
        item = new SearchAddressListItem(query.trim() + " USA", R.mipmap.ic_launcher);
        searchResult.add(item);
        return searchResult;
    }

    /**
     * Apply a search address query to backend. A LOCAL_ADDRESS_RELOCATION type message will be broadcasting once
     * result is retrieved successfully from backend.
     *
     * @param context Usually pass getApplicationContext() or getContext() of an activity where call this method.
     * @param address an address to lookup.
     */
    public void applySearchAddress(Context context, String address) {
        //Broadcast a message
        Intent intentToRelocate = new Intent(BroadcastType.LOCAL_ADDRESS_RELOCATION);
        LatLng geoLocation = new LatLng(49.250024, -123.001528);//BCIT SE12
        ArrayList<HydrantItem> hydrantItemArrayList = new ArrayList<>();
        hydrantItemArrayList.add(new HydrantItem(1, new LatLng(49.249377, -123.000594)));//BCIT SE14
        hydrantItemArrayList.add(new HydrantItem(2, new LatLng(49.251113, -123.002590)));//BCIT SW1
        hydrantItemArrayList.add(new HydrantItem(3, new LatLng(49.251232, -123.000842)));//BCIT SE2
        hydrantItemArrayList.add(new HydrantItem(4, new LatLng(49.250812, -122.999082)));//BCIT SE1
        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, hydrantItemArrayList);
        intentToRelocate.putExtra("geoLocHydrants", geoLocHydrants);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToRelocate);
    }

    /**
     * Add a search result to search history. A search result history list will display on the search page. The
     * history should be ideally saved to a DB.
     * @param item
     */
    public void addHistorySearchAddress(SearchAddressListItem item) {
        if(item == null) return;
        SearchAddressListItem historyItem = null;
        for (int i = 0; i < historySearchAddressListItems.size(); i ++) {
            if(historySearchAddressListItems.get(i).getAddress().equalsIgnoreCase(item.getAddress())) {
                historyItem = historySearchAddressListItems.remove(i);
                break;
            }
        }
        if(historyItem == null) {
            historyItem = item;
        }
        historySearchAddressListItems.add(0, historyItem);
    }

    /**
     * return the search history list
     * @return
     */
    public ArrayList<SearchAddressListItem> getHistorySearchAddress() {

        return historySearchAddressListItems;
    }

    /**
     * retrieve a hydrant item
     * @param context Usually pass getApplicationContext() or getContext() of an activity where call this method.
     * @param itemId hydrant item id
     * @param showItem set to true to broadcast a display item message.
     */
    public void retrieveHydrantItem(Context context, int itemId, boolean showItem) {
        //retrieve data from db
        HydrantItem hydrantItem = new HydrantItem(itemId, new LatLng(49.2509962, -123.0119258), 1, 2, 5.5, "Test " +
                "Hydrant Item");
        //broadcast to display the item
        if (showItem) {
            Intent intentToDisplayHydrantItem = new Intent(BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM);
            intentToDisplayHydrantItem.putExtra("hydrantItem", hydrantItem);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentToDisplayHydrantItem);
        }
    }

    /**
     * get a list for water pressure options which are used in set filter page
     *
     * @return
     */
    public ArrayList<String> getFilterWaterPressureLevelRangeLowerBound() {
        ArrayList<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        list.add("10");
        return list;
    }

    /**
     * get a list for water pressure options which are used in set filter page
     *
     * @return
     */
    public ArrayList<String> getFilterWaterPressureLevelRangeUpperBound() {
        ArrayList<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        list.add("10");
        return list;
    }

    /**
     * retrieve all hydrants around a geo location within a specified range
     *
     * @param geoLocation  the geo location to search for hydrant
     * @param searchRadius the radius (meter) bases on the geo location
     * @return
     */
    public GeoLocHydrants retrieveHydrantsOnLocation(LatLng geoLocation, double searchRadius) {
        //TODO need implementation, now here is a testing code
        ArrayList<HydrantItem> hydrantItemArrayList = new ArrayList<>();
        hydrantItemArrayList.add(new HydrantItem(1, new LatLng(49.249377, -123.000594)));//BCIT SE14
        hydrantItemArrayList.add(new HydrantItem(2, new LatLng(49.251113, -123.002590)));//BCIT SW1
        hydrantItemArrayList.add(new HydrantItem(3, new LatLng(49.251232, -123.000842)));//BCIT SE2
        hydrantItemArrayList.add(new HydrantItem(4, new LatLng(49.250812, -122.999082)));//BCIT SE1
        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, hydrantItemArrayList);
        return geoLocHydrants;
    }

    /**
     * apply a search filter query. A LOCAL_SET_SEARCH_FILTER type message will be broadcasting once
     * result is retrieved successfully from backend.
     *
     * @param context          Usually pass getApplicationContext() or getContext() of an activity where call this method.
     * @param state
     * @param pressureRangeMin
     * @param pressureRangeMax
     * @return
     */
    public boolean applySearchFilter(Context context, int state, double pressureRangeMin, double pressureRangeMax) {
        //TODO needs implementation
        //when server return the result, call the broadcast manager to broadcast a message to display hydrant items
        //the broadcast part is similar as
        return false;
    }
}
