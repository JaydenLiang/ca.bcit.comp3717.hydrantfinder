package comp3717.bcit.ca.hydrantfinder.ValueObjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jaydenliang on 2017-01-22.
 */

public class GeoItem implements Parcelable {
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<GeoItem> CREATOR = new Parcelable
            .Creator<GeoItem>() {
        public GeoItem createFromParcel(Parcel in) {
            return new GeoItem(in);
        }

        public GeoItem[] newArray(int size) {
            return new GeoItem[size];
        }
    };
    private String x;
    private String y;

    public GeoItem(String x, String y) {
        this.x = x;
        this.y = y;
    }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private GeoItem(Parcel in) {
        x = in.readString();
        y = in.readString();
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(x);
        dest.writeString(y);
    }
}
