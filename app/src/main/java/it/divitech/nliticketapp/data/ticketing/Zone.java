package it.divitech.nliticketapp.data.ticketing;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Entity( tableName = "zones_table" )
public class Zone
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Zone()
    {
        this.id = -1;
        this.name = "";
        this.sequence = -1;
        this.label = "";
        this.description = "";
        this.userId = -1;
        this.tags = new ArrayList<>();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Zone( Zone other )
    {
        this.id = other.id;
        this.name = other.name;
        this.sequence = other.sequence;
        this.label = other.label;
        this.description = other.description;
        this.userId = other.userId;
        this.tags = other.tags != null ? new ArrayList<>( other.tags ) : new ArrayList<>();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey
    @JsonProperty( "id" )
    public int id;

    @ColumnInfo( name = "name" )
    @JsonProperty( "name" )
    public String name;

    @ColumnInfo( name = "sequence" )
    @JsonProperty( "sequence" )
    public int sequence;

    @ColumnInfo( name = "label" )
    @JsonProperty( "label" )
    public String label;

    @ColumnInfo( name = "description" )
    @JsonProperty( "description" )
    public String description;

    @ColumnInfo( name = "tags" )
    @JsonProperty( "tags" )
    public List<String> tags;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;
}
