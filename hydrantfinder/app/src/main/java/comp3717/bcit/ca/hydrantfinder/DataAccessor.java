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

    public DataAccessor() {
        this.historySearchAddressListItems = new ArrayList<>();
    }

    public static DataAccessor getInstance(){
        if(instance == null){
            instance = new DataAccessor();
        }
        return instance;
    }

    public ArrayList<SearchAddressListItem> getAddressSuggestion(String query) {
        ArrayList<SearchAddressListItem> searchResult = new ArrayList<>();
        SearchAddressListItem item;
        item = new SearchAddressListItem(query.trim() + " BC, Canada", R.mipmap.ic_launcher);
        searchResult.add(item);
        item = new SearchAddressListItem(query.trim() + " USA", R.mipmap.ic_launcher);
        searchResult.add(item);
        return searchResult;
    }

    public void applySearchAddress(Context context, String address) {
        Intent intentToRelocate = new Intent(BroadcastType.LOCAL_ADDRESS_RELOCATION);
        LatLng geoLocation = new LatLng(49.2509962, -123.0119258);
        ArrayList<HydrantItem> hydrantItems = new ArrayList<>();
        hydrantItems.add(new HydrantItem(1, new LatLng(49.2509902, -123.0119208)));
        hydrantItems.add(new HydrantItem(2, new LatLng(49.2509762, -123.0119558)));
        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, hydrantItems);
        intentToRelocate.putExtra("geoLocHydrants", geoLocHydrants);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToRelocate);
    }

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

    public ArrayList<SearchAddressListItem> getHistorySearchAddress() {
        return historySearchAddressListItems;
    }

    public void retrieveHydrantItem(Context context, int itemId, boolean showItem) {
        //retrieve data from db
        HydrantItem hydrantItem = new HydrantItem(1, new LatLng(49.2509962, -123.0119258), 1, 2, 5.5);
        //broadcast to display the item
        if (showItem) {
            Intent intentToDisplayHydrantItem = new Intent(BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM);
            intentToDisplayHydrantItem.putExtra("hydrantItem", hydrantItem);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentToDisplayHydrantItem);
        }
    }

    public ArrayList<String> getWaterPressureLevelRangeLowerBound() {
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

    public ArrayList<String> getWaterPressureLevelRangeUpperBound() {
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
}
