package it.divitech.nliticketapp.data.ticketing;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Entity( tableName = "settings_table" )
public class DeviceSettings
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public DeviceSettings()
    {
        this.key = "";
        this.value = "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public DeviceSettings( DeviceSettings other )
    {
        this.key = other.key;
        this.value = other.value;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @JsonProperty("key")
    @ColumnInfo(name = "key")
    public String key;

    @JsonProperty("value")
    @ColumnInfo(name = "value")
    public String value;
}
