package it.divitech.nliticketapp.data.ticketing;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Entity( tableName = "fees_table", indices = { @Index( "description" ), @Index( "src_media" ) } )
public class Fee
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Fee()
    {
        this.id = -1;
        this.description = "";
        this.shortDescription = "";
        this.vatPerc = -1;
        this.childFees = new ArrayList<>();
        this.arFeeId = null;
        this.onewayFeeId = null;
        this.taxFeeId = null;
        this.personal = false;
        this.travelerClasses = new ArrayList<>();
        this.hasZones = false;
        this.validity = "";
        this.feeOptions = null;
        this.validatableOnIssue = false;
        this.srcMedia = new ArrayList<>();
        this.outMedia = "";
        this.status = "";
        this.userId = -1;
        this.priority = -1;
        this.standalone = false;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Fee( Fee other )
    {
        this.id = other.id;
        this.description = other.description;
        this.shortDescription = other.shortDescription;
        this.vatPerc = other.vatPerc;
        this.childFees = other.childFees != null ? new ArrayList<>( other.childFees ) : new ArrayList<>();
        this.arFeeId = other.arFeeId != null ? new Integer( other.arFeeId ) : null;
        this.onewayFeeId = other.onewayFeeId != null ? new Integer( other.onewayFeeId ) : null;
        this.taxFeeId = other.taxFeeId != null ? new Integer( other.taxFeeId ) : null;
        this.personal = other.personal;
        this.travelerClasses = other.travelerClasses != null ? new ArrayList<>( other.travelerClasses ) : new ArrayList<>();
        this.hasZones = other.hasZones;
        this.validity = other.validity;
        this.feeOptions = other.feeOptions;
        this.validatableOnIssue = other.validatableOnIssue;
        this.srcMedia = other.srcMedia != null ? new ArrayList<>( other.srcMedia ) : new ArrayList<>();
        this.outMedia = other.outMedia;
        this.status = other.status;
        this.userId = other.userId;
        this.priority = other.priority;
        this.standalone = other.standalone;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class FeeOptions
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
        public String trigger = "";

        @JsonProperty( "unit" )
        public String unit = "";

        @JsonProperty( "start" )
        public String start = "";

        @JsonProperty( "stop" )
        public String stop = "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey
    @JsonProperty( "id" )
    public int id;

    @ColumnInfo( name = "description" )
    @JsonProperty( "description" )
    public String description;

    @ColumnInfo( name = "short_description" )
    @JsonProperty( "short_description" )
    public String shortDescription;

    @ColumnInfo( name = "vat_perc" )
    @JsonProperty( "vat_perc" )
    public int vatPerc;

    @ColumnInfo( name = "child_fees" )
    @JsonProperty( "child_fees" )
    public List<Integer> childFees;

    @ColumnInfo( name = "ar_fee_id" )
    @JsonProperty( "ar_fee_id" )
    public Integer arFeeId;

    @ColumnInfo(name = "oneway_fee_id")
    @JsonProperty("oneway_fee_id")
    public Integer onewayFeeId;

    @ColumnInfo( name = "tax_fee_id" )
    @JsonProperty( "tax_fee_id" )
    public Integer taxFeeId;

    @ColumnInfo( name = "personal" )
    @JsonProperty( "personal" )
    public boolean personal;

    @ColumnInfo( name = "traveler_classes" )
    @JsonProperty( "traveler_classes" )
    public List<Integer> travelerClasses;

    @ColumnInfo( name = "has_zones" )
    @JsonProperty( "has_zones" )
    public boolean hasZones;

    @ColumnInfo( name = "validity" )
    @JsonProperty( "validity" )
    public String validity;

    @ColumnInfo( name = "options" )
    @JsonProperty( "options" )
    public FeeOptions feeOptions;

    @ColumnInfo( name = "validatable_on_issue" )
    @JsonProperty( "validatable_on_issue" )
    public boolean validatableOnIssue;

    @ColumnInfo( name = "src_media" )
    @JsonProperty( "src_media" )
    @Nullable
    public List<String> srcMedia;

    @ColumnInfo( name = "out_media" )
    @JsonProperty( "out_media" )
    public String outMedia;

    @ColumnInfo( name = "status" )
    @JsonProperty( "status" )
    public String status;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;

    @ColumnInfo( name = "priority" )
    @JsonProperty( "priority" )
    public int priority;

    @ColumnInfo( name = "standalone" )
    @JsonProperty( "standalone" )
    public boolean standalone;
}
