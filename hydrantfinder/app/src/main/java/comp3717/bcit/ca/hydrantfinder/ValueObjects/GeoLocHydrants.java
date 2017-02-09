package comp3717.bcit.ca.hydrantfinder.ValueObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by jaydenliang on 2017-01-26.
 */

/**
 * A Value Object that consists of a geo location and a hydrantItem array list.
 * This Value Object is a Parcelable and able to be transfer via an intent.
 */
public class GeoLocHydrants implements Parcelable {
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<GeoLocHydrants> CREATOR = new Parcelable
            .Creator<GeoLocHydrants>() {
        public GeoLocHydrants createFromParcel(Parcel in) {
            LatLng geoLocation = in.readParcelable(LatLng.class.getClassLoader());
            double radius = in.readDouble();
            ArrayList<HydrantItem> hydrantItems = in.readArrayList(HydrantItem.class.getClassLoader());
            return new GeoLocHydrants(geoLocation, radius, hydrantItems);
        }

        public GeoLocHydrants[] newArray(int size) {
            return new GeoLocHydrants[size];
        }
    };

    private LatLng geoLocation;
    private ArrayList<HydrantItem> hydrantItems;
    private double radius;

    public GeoLocHydrants(LatLng geoLocation, double radius, ArrayList<HydrantItem> hydrantItems) {
        this.geoLocation = geoLocation;
        this.radius = radius;
        if (hydrantItems != null) {
            this.hydrantItems = hydrantItems;
        } else {
            this.hydrantItems = new ArrayList<>();
        }
    }

    /**
     * Geo location
     *
     * @return
     */
    public LatLng getGeoLocation() {
        return geoLocation;
    }

    /**
     * radius that all hydrants within it from geo location. (meters)
     * @return
     */
    public double getRadius() {
        return radius;
    }

    /**
     * An array list of hydrant items
     *
     * @return
     */
    public ArrayList<HydrantItem> getHydrantItems() {
        return hydrantItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.geoLocation, flags);
        dest.writeDouble(this.radius);
        dest.writeList(this.hydrantItems);
    }
}
