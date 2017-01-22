package comp3717.bcit.ca.hydrantfinder.SearchAddress;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import comp3717.bcit.ca.hydrantfinder.DataAccessor;
import comp3717.bcit.ca.hydrantfinder.R;

public class SearchAddressActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private static Context instance;
    private SearchManager searchManager;
    private android.widget.SearchView searchView;
    private ListView searchResultListView;
    private SearchAddressListAdapter searchResultListAdapter;
    private ArrayList<SearchAddressListItem> searchAddressListItems;
    private MenuItem menuItem;
    private DataAccessor dataAccessor;

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

        searchAddressListItems = new ArrayList<>();
        searchResultListAdapter = new SearchAddressListAdapter(SearchAddressActivity.this, searchAddressListItems);

        searchResultListView = (ListView) findViewById(R.id.listview_search_result);
        searchResultListView.setAdapter(searchResultListAdapter);

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        dataAccessor = DataAccessor.getInstance();

        displaySearchHistory();
    }

    private void displaySearchHistory(){
        searchResultListAdapter.clear();
        searchResultListAdapter.addAll(dataAccessor.getHistorySearchAddress());
    }

    public void onSearchAddressListItemSelected(SearchAddressListItem item){
        DataAccessor.getInstance().addHistorySearchAddress(item);
        DataAccessor.getInstance().applySearchAddress(getApplicationContext(), item.getAddress());
        finish();
    }

    public void cancelSearch(final View view){
        finish();
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
}
