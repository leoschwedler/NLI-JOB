package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Zone;

@Dao
public interface ZoneDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Zone> list );

    @Query("DELETE FROM zones_table")
    void deleteAll();

    @Query( "SELECT COUNT(*) FROM zones_table" )
    int getRecordCount();

    @Query( "SELECT label FROM zones_table" )
    List<String> getZoneLabels();

    @Query( "SELECT id FROM zones_table" )
    List<Integer> getZoneIds();

    @Query( "SELECT * FROM zones_table" )
    List<Zone> getZones();

    @Query( "SELECT * FROM zones_table WHERE id = :id" )
    Zone getZoneById( int id );
}
