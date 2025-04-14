package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Stop;

@Dao
public interface StopDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Stop> list );

    @Query( "SELECT COUNT(*) FROM stops_table" )
    int getRecordCount();

    @Query( "SELECT name FROM stops_table" )
    List<String> getStopsNames();

    @Query( "SELECT name FROM stops_table WHERE zone_id = :zoneId"  )
    List<String> getStopsNamesByZone( int zoneId );
}
