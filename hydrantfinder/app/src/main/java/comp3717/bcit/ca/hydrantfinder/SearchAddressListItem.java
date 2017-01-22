package comp3717.bcit.ca.hydrantfinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jaydenliang on 2017-01-21.
 */

public class SearchAddressListItem implements Parcelable {
    private int icon;
    private String address;
    public SearchAddressListItem(String address, int icon) {
        this.address = address;
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeString(address);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<SearchAddressListItem> CREATOR = new Parcelable
            .Creator<SearchAddressListItem>() {
        public SearchAddressListItem createFromParcel(Parcel in) {
            return new SearchAddressListItem(in);
        }

        public SearchAddressListItem[] newArray(int size) {
            return new SearchAddressListItem[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private SearchAddressListItem(Parcel in) {
        icon = in.readInt();
        address = in.readString();
    }
}
