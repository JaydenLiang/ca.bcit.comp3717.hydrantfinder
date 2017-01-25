package comp3717.bcit.ca.hydrantfinder.ValueObjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jaydenliang on 2017-01-25.
 */

public class HydrantItem implements Parcelable {
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<HydrantItem> CREATOR = new Parcelable
            .Creator<HydrantItem>() {
        public HydrantItem createFromParcel(Parcel in) {
            int hydrantId = in.readInt();
            GeoItem geoLocation = in.readParcelable(GeoItem.class.getClassLoader());
            return new HydrantItem(hydrantId, geoLocation);
        }

        public HydrantItem[] newArray(int size) {
            return new HydrantItem[size];
        }
    };
    private int hydrantId;
    private GeoItem geoLocation;

    // example constructor that takes a Parcel and gives you an object populated with it's values
    public HydrantItem(int hydrantId, String posx, String posy) {
        this.hydrantId = hydrantId;
        this.geoLocation = new GeoItem(posx, posy);
    }

    public HydrantItem(int hydrantId, GeoItem geoLocation) {
        this.hydrantId = hydrantId;
        this.geoLocation = geoLocation;
    }

    public int getHydrantId() {
        return hydrantId;
    }

    public GeoItem getGeoLocation() {
        return geoLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hydrantId);
        dest.writeParcelable(geoLocation, flags);
    }
}
