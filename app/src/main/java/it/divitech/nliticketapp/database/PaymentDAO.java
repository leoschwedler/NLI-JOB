package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Payment;

@Dao
public interface PaymentDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<Payment> list );

    @Update
    void update( Payment payment );

    @Insert( onConflict = OnConflictStrategy.REPLACE )
    long insert( Payment payment );

    @Query( "SELECT * FROM payments_table " )
    List<Payment> getPayments();

    @Query( "SELECT * FROM payments_table WHERE id = :id" )
    Payment getPaymentById( int id );

    @Query( "SELECT * FROM payments_table WHERE order_uuid = :UUID" )
    Payment getPaymentByUUID( String UUID );

    @Query( "SELECT * FROM payments_table " +
            "WHERE unit_id = :unitID " +
            "AND type = :type " +
            "AND method = :method " +
            "AND op_session_id = :sessionId " +
            "AND ts >= :sessionStartTs " +
            "AND ts <= :currentTs" )
    List<Payment> getPaymentsForSummary( int unitID, int sessionId, int sessionStartTs, int currentTs, String type, String method );

    @Query( "SELECT * FROM payments_table ORDER BY id DESC LIMIT 1" )
    Payment getLastPayment();

    @Query( "SELECT * FROM payments_table WHERE id > :startId ORDER BY id ASC LIMIT :blockSize" )
    List<Payment> getPaymentsBlock( long startId, int blockSize );

    @Query( "DELETE FROM payments_table WHERE id <= :lastId" )
    void deletePreviousPaymentsBlock( long lastId );

    @Query( "DELETE FROM payments_table WHERE datetime( ts ) <= datetime( :dateISO8601 )" )
    void deleteFromDate( String dateISO8601 );
}
