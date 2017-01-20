package comp3717.bcit.ca.hydrantfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

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

    }

    public void cancelSetFilter(final View view) {
        navigateToMainMenu(view);
    }

    public void navigateToMainMenu(final View view){
        Intent intentToNavigate = new Intent(this, MainMenuActivity.class);
        startActivity(intentToNavigate);
    }

}
