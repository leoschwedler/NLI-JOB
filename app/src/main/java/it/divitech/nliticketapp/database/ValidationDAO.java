package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Validation;

@Dao
public interface ValidationDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Validation> list );

    @Insert( onConflict = OnConflictStrategy.REPLACE )
    long insert( Validation newEntry );

    @Update
    void update( Validation newEntry );

    @Delete
    void delete( Validation session );

    @Query( "DELETE FROM validations_table" )
    void deleteAll();

    @Query("SELECT * FROM validations_table WHERE localId > :startId ORDER BY localId ASC LIMIT :blockSize" )
    List<Validation> getValidationsBlock( long startId, int blockSize );

    @Query( "DELETE FROM validations_table WHERE datetime( ts ) <= datetime( :dateISO8601 )" )
    void deleteFromDate( String dateISO8601 );
}
