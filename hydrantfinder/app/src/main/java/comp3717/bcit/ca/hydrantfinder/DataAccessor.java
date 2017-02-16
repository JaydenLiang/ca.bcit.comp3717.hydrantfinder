package comp3717.bcit.ca.hydrantfinder;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import comp3717.bcit.ca.hydrantfinder.SearchAddress.SearchAddressListItem;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.GeoLocHydrants;
import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

/**
 * This class provides methods to use data from backend
 * Created by jaydenliang on 2017-01-21.
 */
public class DataAccessor {
    private static DataAccessor instance;
    private String data_url = "http://52.34.187.15/api/hydrantfinder.php";
    //    private String data_url = "http://localhost/hydrantfinder.php";
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

    private void onQueryResponse(Context context, JSONObject response, String broadcastType) {
        if (broadcastType != null) {
            ArrayList<HydrantItem> hydrantItemArrayList;
            try {
                switch (broadcastType) {
                    case BroadcastType.LOCAL_RETRIEVE_HYDRANTS_ON_LOCATION:
                        Intent intentToOpenFilter = new Intent(BroadcastType.LOCAL_RETRIEVE_HYDRANTS_ON_LOCATION);
                        LatLng geoLocation = new LatLng(Double.parseDouble(response.get("latitude").toString()), Double
                                .parseDouble(response.get("longitude").toString()));//BCIT SE12
                        hydrantItemArrayList = hydrantItemParser(response.getJSONArray("result"));
                        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, Double.parseDouble(response
                                .get("radius").toString()), hydrantItemArrayList);
                        intentToOpenFilter.putExtra("geoLocHydrants", geoLocHydrants);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToOpenFilter);
                        break;
                    case BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM:
                        hydrantItemArrayList = hydrantItemParser(response.getJSONArray("result"));
                        if (hydrantItemArrayList.size() > 0) {
                            Intent intentToDisplayHydrantItem = new Intent(BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM);
                            intentToDisplayHydrantItem.putExtra("hydrantItem", hydrantItemArrayList.get(0));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intentToDisplayHydrantItem);
                        }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<HydrantItem> hydrantItemParser(JSONArray json) {
        ArrayList<HydrantItem> hydrantItemArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < json.length(); i++) {
                HydrantItem hydrantItem = HydrantItem.parse(json.getJSONObject(i));
                if (hydrantItem != null)
                    hydrantItemArrayList.add(hydrantItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return hydrantItemArrayList;
        }
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
        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, 100, hydrantItemArrayList);
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
        AsyncTaskHttpGet runner = new AsyncTaskHttpGet(context, this.data_url,
                BroadcastType.LOCAL_DISPLAY_HYDRANT_ITEM);
        HashMap<String, String> data = new HashMap<>();
        data.put("hydrantfinder", "1");
        data.put("id", Integer.toString(itemId));
        runner.execute(data);
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
    public void retrieveHydrantsOnLocation(Context context, LatLng geoLocation, double searchRadius) {
        //TODO need implementation, now here is a testing code
        AsyncTaskHttpGet runner = new AsyncTaskHttpGet(context, this.data_url,
                BroadcastType.LOCAL_RETRIEVE_HYDRANTS_ON_LOCATION);
        HashMap<String, String> data = new HashMap<>();
        data.put("hydrantfinder", "1");
        data.put("rad", Double.toString(searchRadius));
        data.put("long", Double.toString(geoLocation.longitude));
        data.put("lat", Double.toString(geoLocation.latitude));
        runner.execute(data);
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
        Intent intentToOpenFilter = new Intent(BroadcastType.LOCAL_SET_SEARCH_FILTER);
        LatLng geoLocation = new LatLng(49.250024, -123.001528);//BCIT SE12
        ArrayList<HydrantItem> hydrantItemArrayList = new ArrayList<>();
        hydrantItemArrayList.add(new HydrantItem(1, 0, 7, 10));//good
        hydrantItemArrayList.add(new HydrantItem(2, 1, 4, 6));//low
        hydrantItemArrayList.add(new HydrantItem(3, 2, 1, 3));//bad
        GeoLocHydrants geoLocHydrants = new GeoLocHydrants(geoLocation, 30, hydrantItemArrayList);
        intentToOpenFilter.putExtra("geoLocHydrants", geoLocHydrants);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToOpenFilter);
        return false;
    }

    private class AsyncTaskHttpGet extends AsyncTask<HashMap<String, String>, Integer, Double> {
        private Context context;
        private String url;
        private String broadcastType = null;

        public AsyncTaskHttpGet(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        public AsyncTaskHttpGet(Context context, String url, String broadcastType) {
            this.context = context;
            this.url = url;
            this.broadcastType = broadcastType;
        }

        @Override
        protected Double doInBackground(HashMap<String, String>... params) {
            postData(params[0]);
            return null;
        }

        protected void onPostExecute(Double result) {
            if (context != null) {
                //Toast.makeText(context, "retrieving data...", Toast.LENGTH_LONG).show();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
//            pb.setProgress(progress[0]);
            if (context != null) {
                //Toast.makeText(context, "retrieving data...", Toast.LENGTH_LONG).show();
            }
        }

        public void setBroadcastType(String broadcastType) {
            this.broadcastType = broadcastType;
        }

        public void postData(HashMap<String, String> data) {
            // Create a new HttpClient and Post Header
            URL url;
            HttpURLConnection urlConnection = null;
            String content = "";
            StringBuilder urlBulder = new StringBuilder();
            JSONObject json = null;
            try {
                urlBulder.append(this.url);
                if (data != null && data.size() > 0) {
                    urlBulder.append("?");
                    int size = 0;
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        urlBulder.append(entry.getKey() + "=" + entry.getValue() + (++size < data.size() ? "&" : ""));
                    }
                }
                url = new URL(urlBulder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;

                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                json = new JSONObject(content);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (json != null) {
                    onQueryResponse(context, json, this.broadcastType);
                }
            }
        }

    }
}
