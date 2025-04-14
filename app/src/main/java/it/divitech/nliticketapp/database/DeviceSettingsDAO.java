package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.DeviceSettings;
import it.divitech.nliticketapp.data.ticketing.Zone;

@Dao
public interface DeviceSettingsDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<DeviceSettings> list );

    @Query("DELETE FROM settings_table")
    void deleteAll();

    @Query( "SELECT COUNT(*) FROM settings_table" )
    int getRecordCount();

    @Query( "SELECT * FROM settings_table WHERE key = :key" )
    DeviceSettings getSetting( String key );
}
