package it.divitech.nliticketapp.data.session;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity( tableName = "sessions_table" )
public class OperatorSession
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public OperatorSession()
    {
        this.id = 0;
        this.unitId = 0;
        this.agencyId = 0;
        this.loginUserId = "";
        this.tag = "";
        this.status = "";
        this.tsOpen = null;
        this.tsClose = null;
        this.details = null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public OperatorSession( OperatorSession other )
    {
        this.id = other.id;
        this.unitId = other.unitId;
        this.agencyId = other.agencyId;
        this.loginUserId = other.loginUserId;
        this.tag = other.tag;
        this.status = other.status;
        this.tsOpen = other.tsOpen;
        this.tsClose = other.tsClose;
        this.details = other.details;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    @JsonProperty( "id" )
    public long id; // Auto Increment Primary Key

    @ColumnInfo( name = "unit_id" )
    @JsonProperty( "unit_id" )
    public int unitId; // Quello ottenuto in fase di registrazione

    @ColumnInfo( name = "agency_id" )
    @JsonProperty( "agency_id" )
    public int agencyId; // id associato alla login utente corrente

    @ColumnInfo( name = "login_userid" )
    @JsonProperty( "login_userid" )
    public String loginUserId ; // Codice Login Operatore (UnitLoginUsers)

    @ColumnInfo( name = "tag" )
    @Nullable
    @JsonProperty( "tag" )
    public String tag ; // tag associato alla sessione utente corrente (TURNO)

    @ColumnInfo( name = "status" )
    @JsonProperty( "status" )
    public String status ; // O - open, C - closed, F - forced

    @ColumnInfo( name = "ts_open" )
    @JsonProperty( "ts_open" )
    public String tsOpen ; // Datetime apertura sessione ISO8601

    @ColumnInfo( name = "ts_close" )
    @Nullable
    @JsonProperty( "ts_close" )
    public String tsClose ; // Datetime chiusura  sessione ISO8601

    @ColumnInfo( name = "details" )
    @Nullable
    @JsonProperty( "details" )
    public String details ; // Eventuali dettagli di sessione

}
