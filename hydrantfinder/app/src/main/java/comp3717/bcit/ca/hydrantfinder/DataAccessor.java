package comp3717.bcit.ca.hydrantfinder;

import java.util.ArrayList;

/**
 * Created by jaydenliang on 2017-01-21.
 */

public class DataAccessor {
    private static DataAccessor instance;
    private ArrayList<SearchAddressListItem> historySearchAddressListItems;
    public static DataAccessor getInstance(){
        if(instance == null){
            instance = new DataAccessor();
        }
        return instance;
    }

    public DataAccessor() {
        this.historySearchAddressListItems = new ArrayList<>();
    }

    public ArrayList<SearchAddressListItem> searchAddress(String address){
        ArrayList<SearchAddressListItem> searchResult = new ArrayList<>();
        SearchAddressListItem item;
        item = new SearchAddressListItem(address.trim() + " BC, Canada", R.mipmap.ic_launcher);
        searchResult.add(item);
        item = new SearchAddressListItem(address.trim() + " USA", R.mipmap.ic_launcher);
        searchResult.add(item);
        return searchResult;
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
