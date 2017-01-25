package comp3717.bcit.ca.hydrantfinder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

public class ShowItemActivity extends AppCompatActivity {
    private HydrantItem hydrantItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_item);
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
        hydrantItem = getIntent().getParcelableExtra("hydrantItem");
        displayItem();
    }

    public void displayItem() {
        Toast.makeText(getApplicationContext(), "Display hydrant item: id = " + hydrantItem.getHydrantId() + ", x: " +
                        hydrantItem.getGeoLocation().getX() + ", y: " + hydrantItem.getGeoLocation().getY(),
                Toast.LENGTH_LONG).show();
    }

    public void exitShowItem(final View view) {
        finish();
    }

}
