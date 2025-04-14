package it.divitech.nliticketapp.data.ticketing;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity( tableName = "fare_table" )
public class Fare
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Fare()
    {
        // this.id = -1; NON IMPOSTARE QUESTO VALORE!
        this.feeId = -1;
        this.rangeName = "";
        this.fromZoneId = -1;
        this.fromZoneName = "";
        this.fromZoneLabel = "";
        this.toZoneId = -1;
        this.toZoneName = "";
        this.toZoneLabel = "";
        this.tripsCount = 0;
        this.minutes = 0;
        this.value = 0;
        this.fareOptions = new FareOptions();
        this.status = "";
        this.userId = -1;

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Fare( Fare other )
    {
        this.id = other.id;
        this.feeId = other.feeId;
        this.rangeName = other.rangeName;
        this.fromZoneId = other.fromZoneId;
        this.fromZoneName = other.fromZoneName;
        this.fromZoneLabel = other.fromZoneLabel;
        this.toZoneId = other.toZoneId;
        this.toZoneName = other.toZoneName;
        this.toZoneLabel = other.toZoneLabel;
        this.tripsCount = other.tripsCount;
        this.minutes = other.minutes;
        this.value = other.value;
        this.fareOptions = other.fareOptions;
        this.status = other.status;
        this.userId = other.userId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class FareOptions
    {
        @JsonProperty( "validity" )
        public Validity validity = new Validity();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class Validity
    {
        @JsonProperty( "trigger" )
        String trigger = "";

        @JsonProperty( "unit" )
        String unit = "";

        @JsonProperty( "start" )
        String start = "";

        @JsonProperty( "stop" )
        String stop = "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    //@ColumnInfo( name = "id" )
    @JsonProperty( "id" )
    public int id;

    @ColumnInfo( name = "fee_id" )
    @JsonProperty( "fee_id" )
    public int feeId;

    @ColumnInfo( name = "range_name" )
    @JsonProperty( "range_name" )
    public String rangeName;

    @ColumnInfo( name = "from_zone_id" )
    @JsonProperty( "from_zone_id" )
    public Integer fromZoneId;

    @ColumnInfo( name = "from_zone_name" )
    @JsonProperty( "from_zone_name" )
    public String fromZoneName;

    @ColumnInfo( name = "from_zone_label" )
    @JsonProperty( "from_zone_label" )
    public String fromZoneLabel;

    @ColumnInfo( name = "to_zone_id" )
    @JsonProperty( "to_zone_id" )
    public Integer toZoneId;

    @ColumnInfo( name = "to_zone_name" )
    @JsonProperty( "to_zone_name" )
    public String toZoneName;

    @ColumnInfo( name = "to_zone_label" )
    @JsonProperty( "to_zone_label" )
    public String toZoneLabel;

    @ColumnInfo( name = "trips_count" )
    @JsonProperty( "trips_count" )
    public int tripsCount;

    @ColumnInfo( name = "minutes" )
    @JsonProperty( "minutes" )
    public int minutes;

    @ColumnInfo( name = "value" )
    @JsonProperty( "value" )
    public int value;

    @ColumnInfo( name = "options" )
    @JsonProperty( "options" )
    public FareOptions fareOptions;

    @ColumnInfo( name = "status" )
    @JsonProperty( "status" )
    public String status;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;

}
