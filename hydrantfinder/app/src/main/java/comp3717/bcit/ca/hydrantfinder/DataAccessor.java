package comp3717.bcit.ca.hydrantfinder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.GeoItem;
import comp3717.bcit.ca.hydrantfinder.SearchAddress.SearchAddressListItem;

/**
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
        intentToRelocate.putExtra("location", new GeoItem("49.2718919", "-123.0001673"));
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
}
