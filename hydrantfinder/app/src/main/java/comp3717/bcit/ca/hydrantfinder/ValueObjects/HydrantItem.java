package comp3717.bcit.ca.hydrantfinder.ValueObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jaydenliang on 2017-01-25.
 */

public class HydrantItem implements Parcelable {
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<HydrantItem> CREATOR = new Parcelable
            .Creator<HydrantItem>() {
        public HydrantItem createFromParcel(Parcel in) {
            int hydrantId = in.readInt();
            int connector_size = in.readInt();
            int port_number = in.readInt();
            double water_pressure = in.readDouble();
            LatLng geoLocation = in.readParcelable(LatLng.class.getClassLoader());
            return new HydrantItem(hydrantId, geoLocation, connector_size, port_number, water_pressure);
        }

        public HydrantItem[] newArray(int size) {
            return new HydrantItem[size];
        }
    };
    private int hydrantId;
    private int connector_size;
    private int port_number;
    private double water_pressure;
    private LatLng geoLocation;

    // example constructor that takes a Parcel and gives you an object populated with it's values
    public HydrantItem(int hydrantId, LatLng geoLocation, int connector_size, int port_number, double water_pressure) {
        this.hydrantId = hydrantId;
        this.geoLocation = new LatLng(geoLocation.latitude, geoLocation.longitude);
        this.connector_size = connector_size;
        this.port_number = port_number;
        this.water_pressure = water_pressure;
    }

    public HydrantItem(int hydrantId, LatLng geoLocation) {
        this.hydrantId = hydrantId;
        this.geoLocation = geoLocation;
    }

    public int getConnector_size() {
        return connector_size;
    }

    public int getPort_number() {
        return port_number;
    }

    public double getWater_pressure() {
        return water_pressure;
    }

    public int getHydrantId() {
        return hydrantId;
    }

    public LatLng getGeoLocation() {
        return geoLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hydrantId);
        dest.writeInt(connector_size);
        dest.writeInt(port_number);
        dest.writeDouble(water_pressure);
        dest.writeParcelable(geoLocation, flags);
    }
}
