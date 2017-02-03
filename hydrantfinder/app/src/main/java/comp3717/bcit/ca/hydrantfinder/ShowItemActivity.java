package comp3717.bcit.ca.hydrantfinder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import comp3717.bcit.ca.hydrantfinder.ValueObjects.HydrantItem;

public class ShowItemActivity extends AppCompatActivity {
    private HydrantItem hydrantItem;
    private boolean editMode;

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
        //this is the value object that contains info of a hydrantItem
        hydrantItem = getIntent().getParcelableExtra("hydrantItem");
        editMode = false;
        Button button_updateItem = (Button) findViewById(R.id.button_show_item_update);
        button_updateItem.setVisibility(editMode ? View.VISIBLE : View.GONE);
        displayItem();
    }

    public void displayItem() {
        Toast.makeText(getApplicationContext(), "Display hydrant item: id = " + hydrantItem.getHydrantId() + ", x: " +
                        hydrantItem.getGeoLocation().latitude + ", y: " + hydrantItem.getGeoLocation().longitude,
                Toast.LENGTH_LONG).show();
        //posx
        EditText editText_posX = (EditText) findViewById(R.id.editText_show_item_coord_x);
        editText_posX.setText(Double.toString(hydrantItem.getGeoLocation().latitude));
        //posy
        EditText editText_posY = (EditText) findViewById(R.id.editText_show_item_coord_y);
        editText_posY.setText(Double.toString(hydrantItem.getGeoLocation().longitude));
        //connector size
        EditText editText_connector_size = (EditText) findViewById(R.id.editText_show_item_con_size);
        editText_connector_size.setText(Integer.toString(hydrantItem.getConnector_size()));
        //port number
        EditText editText_port_number = (EditText) findViewById(R.id.editText_show_item_port_number);
        editText_port_number.setText(Integer.toString(hydrantItem.getPort_number()));
        //water pressure
        EditText editText_water_pressure = (EditText) findViewById(R.id.editText_show_item_water_pressure);
        editText_water_pressure.setText(Double.toString(hydrantItem.getWater_pressure()));
    }

    public void updateItem(final View view) {
        finish();
    }

    public void exitShowItem(final View view) {
        finish();
    }

}
