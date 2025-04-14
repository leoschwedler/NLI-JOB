package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.Payment;

@Dao
public interface OperatorSessionDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<OperatorSession> list );

    @Insert( onConflict = OnConflictStrategy.REPLACE )
    long insert( OperatorSession newEntry );

    @Update
    void update( OperatorSession newEntry );

    @Delete
    void delete( OperatorSession session );

    @Query( "DELETE FROM sessions_table" )
    void deleteAll();

    @Query( "SELECT * FROM sessions_table " +
            "WHERE login_userid = :loginUserID " +
            "AND agency_id = :agencyId " +
            "AND status = 'O'" )
    OperatorSession getSession( String loginUserID, int agencyId );

    @Query( "SELECT * FROM sessions_table " )
    List<OperatorSession> getSessions();

    @Query( "SELECT * FROM sessions_table WHERE id >= :startId AND STATUS = 'C' ORDER BY id ASC LIMIT :blockSize" )
    List<OperatorSession> getClosedSessionsBlock( long startId, int blockSize );

    @Query( "SELECT * FROM sessions_table WHERE STATUS = 'O'" )
    OperatorSession getOpenSession();

    @Query( "DELETE FROM sessions_table WHERE id <= :lastId" )
    void deletePreviousSessionsBlock( long lastId );

    @Query( "DELETE FROM sessions_table WHERE STATUS <> 'O' AND datetime( ts_close ) <= datetime( :dateISO8601 )" )
    void deleteFromDate( String dateISO8601 );
}
