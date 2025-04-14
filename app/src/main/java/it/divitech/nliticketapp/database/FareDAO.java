package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Fare;

@Dao
public interface FareDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Fare> list );

    @Query("DELETE FROM fare_table")
    void deleteAll();

    @Query( "SELECT COUNT(*) FROM fare_table WHERE status = 'E'" )
    int getRecordCount();

    @Query( "SELECT * FROM fare_table WHERE fee_id = :id AND status = 'E'" )
    Fare getFareByFeeId( int id );

    @Query( "SELECT * FROM fare_table WHERE fee_id = :id AND ( from_zone_id = :fromZone AND to_zone_id = :toZone ) AND status = 'E'" )
    Fare getFareByFeeIdAndZone( int id, int fromZone, int toZone );

    @Query( "SELECT * FROM fare_table WHERE status = 'E' " )
    List<Fare> getFares();

    @Query( "SELECT * FROM fare_table WHERE fee_id IN( :idList ) AND status = 'E'" )
    List<Fare> getFaresByFeeIdList( List<Integer> idList );

    @Query( "SELECT * FROM fare_table WHERE fee_id IN( :idList ) AND ( from_zone_id = :fromZone AND to_zone_id = :toZone ) AND status = 'E'" )
    List<Fare> getFaresByFeeIdListAndZone( List<Integer> idList, int fromZone, int toZone );
}