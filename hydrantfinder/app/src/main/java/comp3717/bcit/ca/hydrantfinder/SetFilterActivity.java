package comp3717.bcit.ca.hydrantfinder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class SetFilterActivity extends AppCompatActivity {
    private static final String TAG = SetFilterActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "enter SetFilterActivity");
        setContentView(R.layout.activity_set_filter);
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

        setWaterPressureRangeLowerBound();
        setWaterPressureRangeUpperBound();
    }

    private void setWaterPressureRangeLowerBound() {
        // Spinner element
        Spinner lowerLevelSpinner = (Spinner) findViewById(R.id.spinner_set_filter_pressure_level_lower);

        // Spinner click listener
        lowerLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        ArrayList<String> lowerLevel = DataAccessor.getInstance().getWaterPressureLevelRangeLowerBound();

        // Creating adapter for spinner
        ArrayAdapter<String> lowerLevelDataAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item,
                lowerLevel);

        // Drop down layout style - list view with radio button
        lowerLevelDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        lowerLevelSpinner.setAdapter(lowerLevelDataAdapter);
    }

    private void setWaterPressureRangeUpperBound() {
        // Spinner element
        Spinner upperLevelSpinner = (Spinner) findViewById(R.id.spinner_set_filter_pressure_level_upper);

        // Spinner click listener
        upperLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        ArrayList<String> upperLevel = DataAccessor.getInstance().getWaterPressureLevelRangeUpperBound();

        // Creating adapter for spinner
        ArrayAdapter<String> upperLevelDataAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item,
                upperLevel);

        // Drop down layout style - list view with radio button
        upperLevelDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        upperLevelSpinner.setAdapter(upperLevelDataAdapter);
    }

    public void applyFilter(final View view) {
        finish();
    }

    public void cancelSetFilter(final View view) {
        finish();
    }
}
