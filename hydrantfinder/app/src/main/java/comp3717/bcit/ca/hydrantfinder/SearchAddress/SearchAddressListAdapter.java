package comp3717.bcit.ca.hydrantfinder.SearchAddress;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import comp3717.bcit.ca.hydrantfinder.R;

/**
 * Created by jaydenliang on 2017-01-21.
 */

public class SearchAddressListAdapter extends ArrayAdapter<SearchAddressListItem> {
    private Toast toastObject;
    private int selectedItemPosition;
    public SearchAddressListAdapter(Context context, List<SearchAddressListItem> listItems) {
        super(context, android.R.layout.simple_list_item_1, listItems);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SearchAddressListItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_search_address_list_item,
                    parent, false);
        }
        final View finalView = convertView;
        // Lookup view for data population
        final ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.search_address_list_item_icon);
        imageViewIcon.setImageResource(item.getIcon());
        final TextView textViewAddress = (TextView) convertView.findViewById(R.id.search_address_list_item_text);
        // Populate the data into the template view using the data object
        textViewAddress.setText(item.getAddress());
        // Bind some event listeners to the view
        imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use your imagination here to do whatever you want
                selectedItemPosition = position;
                toast(finalView, "Relocate to: " + textViewAddress.getText());
                ((SearchAddressActivity)getContext()).onSearchAddressListItemSelected(item);
            }
        });
        textViewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use your imagination here to do whatever you want
                selectedItemPosition = position;
                toast(finalView, "Relocate to: " + textViewAddress.getText());
                ((SearchAddressActivity)getContext()).onSearchAddressListItemSelected(item);
            }
        });
        // Return the completed view to render on screen
        return convertView;

    }

    public SearchAddressListItem getSelectedItem(){
        return getItem(selectedItemPosition);
    }

    private void toast(View view, String text) {
        if(toastObject != null) {
            toastObject.cancel();
        }
        toastObject = Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT);
        toastObject.show();
    }

    public void filterData(String query) {

    }
}
