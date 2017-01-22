package comp3717.bcit.ca.hydrantfinder;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import static comp3717.bcit.ca.hydrantfinder.R.id.fab;
import static java.security.AccessController.getContext;

public class SearchAddressActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private SearchManager searchManager;
    private android.widget.SearchView searchView;
//    private SearchExpandableListAdapter searchResultListAdapter;
//    private ExpandableListView searchResultListView;
//    private ArrayList<ParentRow> parentList = new ArrayList<>();
//    private ArrayList<ParentRow> showTheseParentList = new ArrayList<>();
    private ListView searchResultListView;
    private SearchAddressListAdapter searchResultListAdapter;
    private ArrayList<SearchAddressListItem> historySearchAddressListItems;
    private ArrayList<SearchAddressListItem> searchAddressListItems;
    private MenuItem menuItem;
    private DataAccessor dataAccessor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

//        searchResultListView = (ExpandableListView) findViewById(R.id
//                .expandableListView_search_result_container);
//        parentList = new ArrayList<>();

        searchAddressListItems = new ArrayList<>();
        searchResultListAdapter = new SearchAddressListAdapter(SearchAddressActivity.this, searchAddressListItems);

        searchResultListView = (ListView) findViewById(R.id.listview_search_result);
        searchResultListView.setAdapter(searchResultListAdapter);

        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        dataAccessor = DataAccessor.getInstance();

        displaySearchHistory();
        //displayList();
        //expandAll();
    }

    private void displaySearchHistory(){
        searchResultListAdapter.clear();
        searchResultListAdapter.addAll(dataAccessor.getHistorySearchAddress());
    }

    private void loadData(){
//        ParentRow parentRow = null;
//
//        ArrayList<ChildRow> childRows = new ArrayList<>();
//        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
//        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Sit Fido, sit."));
//        parentRow = new ParentRow("First Group", childRows);
//        parentList.add(parentRow);
//
//        childRows = new ArrayList<>();
//        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Fido is the name of my dog."));
//        childRows.add(new ChildRow(R.mipmap.ic_launcher, "Add two plus two."));
//        parentRow = new ParentRow("Second Group", childRows);
//        parentList.add(parentRow);
    }

    private void expandAll(){
//        int count = searchResultListAdapter.getGroupCount();
//        for (int i = 0; i < count; i ++){
//            searchResultListView.expandGroup(i);
//        }
    }

    private void displayList(){
//        loadData();
//
//        searchResultListView = (ExpandableListView) findViewById(R.id.expandableListView_search_result_container);
//        searchResultListAdapter = new SearchExpandableListAdapter(SearchAddressActivity.this, parentList);
//
//        searchResultListView.setAdapter(searchResultListAdapter);
    }

    public void onSearchAddressListItemSelected(SearchAddressListItem item){
        DataAccessor.getInstance().addHistorySearchAddress(item);
        Intent intentToNavigateToSearchAddressActivity = new Intent(this, MainMenuActivity.class);
        intentToNavigateToSearchAddressActivity.putExtra("searchAddressListItem", item);
        startActivity(intentToNavigateToSearchAddressActivity);
    }

    public void cancelSearch(final View view){
        Intent intentToNavigate = new Intent(this, MainMenuActivity.class);
        startActivity(intentToNavigate);
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
        //save to history

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ArrayList<SearchAddressListItem> searchAddressListItems = dataAccessor.searchAddress(newText);
        searchResultListAdapter.clear();
        searchResultListAdapter.addAll(searchAddressListItems);
        //searchResultListAdapter.filterData(newText);
        return false;
    }
}
