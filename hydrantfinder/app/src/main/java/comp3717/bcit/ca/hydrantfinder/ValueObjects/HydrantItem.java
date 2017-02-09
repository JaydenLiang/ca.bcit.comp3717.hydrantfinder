package comp3717.bcit.ca.hydrantfinder.ValueObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jaydenliang on 2017-01-25.
 */

/**
 * A Value Object that represents a hydrant item.
 * This Value Object is a Parcelable and able to be transfer via an intent.
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
            String description = in.readString();
            LatLng geoLocation = in.readParcelable(LatLng.class.getClassLoader());
            return new HydrantItem(hydrantId, geoLocation, connector_size, port_number, water_pressure, description);
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
    private String description;
    private int state;
    private double pressureRangeMin;
    private double pressureRangeMax;

    // example constructor that takes a Parcel and gives you an object populated with it's values
    public HydrantItem(int hydrantId, LatLng geoLocation, int connector_size, int port_number, double water_pressure,
                       String description) {
        this.hydrantId = hydrantId;
        this.geoLocation = new LatLng(geoLocation.latitude, geoLocation.longitude);
        this.connector_size = connector_size;
        this.port_number = port_number;
        this.water_pressure = water_pressure;
        this.description = description;
    }

    public HydrantItem(int hydrantId, LatLng geoLocation) {
        this.hydrantId = hydrantId;
        this.geoLocation = geoLocation;
    }

    public HydrantItem(int hydrantId, int state, double pressureRangeMin, double pressureRangeMax) {
        this.hydrantId = hydrantId;
        this.state = state;
        this.pressureRangeMin = pressureRangeMin;
        this.pressureRangeMax = pressureRangeMax;
    }

    /**
     * Connector size
     *
     * @return
     */
    public int getConnector_size() {
        return connector_size;
    }

    /**
     * Port Number
     *
     * @return
     */
    public int getPort_number() {
        return port_number;
    }

    /**
     * Water Pressure
     *
     * @return
     */
    public double getWater_pressure() {
        return water_pressure;
    }

    /**
     * Hydrant Id
     * @return
     */
    public int getHydrantId() {
        return hydrantId;
    }

    /**
     * Geo location of this item
     * @return
     */
    public LatLng getGeoLocation() {
        return geoLocation;
    }

    /**
     * A description of this hydrant
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    public int getState() {
        return state;
    }

    public double getRressureRangeMin(){
        return pressureRangeMin;
    }

    public double getRressureRangeMax(){
        return pressureRangeMax;
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
        dest.writeString(description);
        dest.writeParcelable(geoLocation, flags);
    }
}
