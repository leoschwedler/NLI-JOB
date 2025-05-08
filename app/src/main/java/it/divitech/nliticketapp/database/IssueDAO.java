package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;

@Dao
public interface IssueDAO

{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert( List<Issue> list );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert( Issue newIssue );

    @Query("SELECT * FROM issues_table ")
    List<Issue> getIssues();

    @Query("SELECT * FROM issues_table WHERE order_uuid = :orderId")
    List<Issue> getIssuesByOrderUUID( String orderId );

    @Query("SELECT * FROM issues_table WHERE order_uuid = :orderId AND parent_uuid IS NULL")
    List<Issue> getParentIssuesByOrderId( String orderId );

    @Query("SELECT * FROM issues_table WHERE id = :id")
    Issue getIssueByID( long id );

    @Query("SELECT * FROM issues_table WHERE order_uuid = :orderId AND parent_uuid = :parentId")
    List<Issue> getIssuesByOrderIdAndParentId( String orderId, String parentId );

    @Query("SELECT * FROM issues_table WHERE id > :startId ORDER BY id ASC LIMIT :blockSize")
    List<Issue> getIssuesBlock( long startId, int blockSize );

    @Query("DELETE FROM issues_table WHERE id <= :lastId")
    void deletePreviousIssuesBlock( long lastId );

    @Query("DELETE FROM issues_table WHERE datetime( ts ) <= datetime( :dateISO8601 )")
    void deleteFromDate( String dateISO8601 );

    @Query("SELECT * FROM issues_table WHERE type = 'T' AND ts BETWEEN :fromDate AND :toDate AND op_session_id = :sessionId")
    List<Issue> getRegularIssuesInRange( long sessionId, String fromDate, String toDate );


    @Query("SELECT * FROM issues_table WHERE type = 'V' AND ts BETWEEN :fromDate AND :toDate AND op_session_id = :sessionId")
    List<Issue> getvoidedIssuesInRange( long sessionId, String fromDate, String toDate );

    @Query("SELECT issues_table.* FROM issues_table " +
            "INNER JOIN payments_table ON payments_table.id = issues_table.payment_id " +
            "WHERE payments_table.method = 'cash' " +
            "AND issues_table.type = 'T' AND issues_table.ts BETWEEN :fromDate AND :toDate AND issues_table.op_session_id = :sessionId")
    List<Issue> getCashRegularIssuesInRange( long sessionId, String fromDate, String toDate );

    @Query("SELECT issues_table.* FROM issues_table " +
            "INNER JOIN payments_table ON payments_table.id = issues_table.payment_id " +
            "WHERE payments_table.method = 'cash' " +
            "AND issues_table.type = 'V' AND issues_table.ts BETWEEN :fromDate AND :toDate AND issues_table.op_session_id = :sessionId")
    List<Issue> getCashVoidedIssuesInRange( long sessionId, String fromDate, String toDate );

    @Query("SELECT issues_table.* FROM issues_table " +
            "INNER JOIN payments_table ON payments_table.id = issues_table.payment_id " +
            "WHERE payments_table.method = 'pos' " +
            "AND issues_table.type = 'T' AND issues_table.ts BETWEEN :fromDate AND :toDate AND issues_table.op_session_id = :sessionId")
    List<Issue> getPosRegularIssuesInRange( long sessionId, String fromDate, String toDate );

    @Query("SELECT issues_table.* FROM issues_table " +
            "INNER JOIN payments_table ON payments_table.id = issues_table.payment_id " +
            "WHERE payments_table.method = 'pos' " +
            "AND issues_table.type = 'V' AND issues_table.ts BETWEEN :fromDate AND :toDate AND issues_table.op_session_id = :sessionId")
    List<Issue> getPosVoidedIssuesInRange( long sessionId, String fromDate, String toDate );
}
