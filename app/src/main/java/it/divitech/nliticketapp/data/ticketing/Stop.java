package it.divitech.nliticketapp.data.ticketing;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity( tableName = "stops_table" )
public class Stop
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Stop()
    {
        this.id = -1;
        this.name = "";
        this.description = "";
        this.zoneId = -1;
        this.userId = -1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Stop( Stop other )
    {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.zoneId = other.zoneId;
        this.userId = other.userId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey
    @JsonProperty( "id" )
    public int id;

    @ColumnInfo( name = "name" )
    @JsonProperty( "name" )
    public String name;

    @ColumnInfo( name = "description" )
    @JsonProperty( "description" )
    public String description;

    @ColumnInfo( name = "zone_id" )
    @JsonProperty( "zone_id" )
    public int zoneId;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;

}
