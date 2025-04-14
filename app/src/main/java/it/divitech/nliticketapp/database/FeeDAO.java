package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Fee;

@Dao
public interface FeeDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Fee> list );

    @Query("DELETE FROM fees_table")
    void deleteAll();

    @Query( "SELECT COUNT(*) FROM fees_table WHERE status = 'E'" )
    int getRecordCount();

    @Query( "SELECT * FROM fees_table WHERE status = 'E' AND standalone = 1 AND src_media IS NULL ORDER BY priority DESC" )
    List<Fee> getMainFees();

    @Query( "SELECT * FROM fees_table WHERE id = :id AND status = 'E'" )
    Fee getFeeById( int id );

    @Query( "SELECT * FROM fees_table WHERE id IN(:idList) AND status = 'E' ORDER BY priority DESC" )
    List<Fee> getFeesByIdList( List<Integer> idList );
}