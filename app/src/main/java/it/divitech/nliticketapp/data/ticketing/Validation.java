package it.divitech.nliticketapp.data.ticketing;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

@Entity( tableName = "validations_table" )
public class Validation
{
    @PrimaryKey( autoGenerate = true )
    @JsonProperty( "local_id" )
    public int localId;

    @ColumnInfo( name = "unit_id" )
    @JsonProperty( "unit_id" )
    public int unitId;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;

    @ColumnInfo( name = "op_session_id" )
    @JsonProperty( "op_session_id" )
    public int opSessionId;

    @ColumnInfo( name = "media_type" )
    @Nullable
    @JsonProperty( "media_type" )
    public String mediaType = null;

    @ColumnInfo( name = "media_hwid" )
    @Nullable
    @JsonProperty( "media_hwid" )
    public String mediaHwid = null;

    @ColumnInfo( name = "extension" )
    @Nullable
    @JsonProperty( "extension" )
    public String extension = null;

    @ColumnInfo( name = "document_id" )
    @Nullable
    @JsonProperty( "document_id" )
    public String documentId = null;

    @ColumnInfo( name = "residual_trips" )
    @JsonProperty( "residual_trips" )
    public int residualTrips;

    @ColumnInfo( name = "details" )
    @JsonProperty( "details" )
    public Details details;

    @ColumnInfo( name = "ts" )
    @Nullable
    @JsonProperty( "ts" )
    public String ts = null;

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class Details
    {
        @JsonProperty( "fee_id" )
        public int feeId;

        @JsonProperty( "valid_from" )
        public long validFrom;

        @JsonProperty( "valid_to" )
        public long validTo;

        @JsonProperty( "user_id" )
        public int userId;

        @JsonProperty( "from_zone_id" )
        public int fromZoneId;

        @JsonProperty( "to_zone_id" )
        public int toZoneId;
    }
}
