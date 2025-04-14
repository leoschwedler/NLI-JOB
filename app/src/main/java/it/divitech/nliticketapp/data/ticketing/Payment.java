package it.divitech.nliticketapp.data.ticketing;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity( tableName = "payments_table" )
public class Payment
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Payment()
    {
        this.id = 0;
        this.unitId = 0;
        this.opSessionId = 0;
        this.orderUuid = null;
        this.method = "";
        this.type = "";
        this.value = 0;
        this.details = null;
        this.ts = null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Payment( Payment other )
    {
        this.id = other.id;
        this.unitId = other.unitId;
        this.opSessionId = other.opSessionId;
        this.orderUuid = other.orderUuid;
        this.method = other.method;
        this.type = other.type;
        this.value = other.value;
        this.details = other.details;
        this.ts = other.ts;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    @JsonProperty( "id" )
    public long id; // Auto Increment Primary Key

    @ColumnInfo( name = "unit_id" )
    @JsonProperty( "unit_id" )
    public int unitId; // Quello ottenuto in fase di registrazione

    @ColumnInfo( name = "op_session_id" )
    @JsonProperty( "op_session_id" )
    public long opSessionId; // Id sessione corrente (OperatorSession)

    @ColumnInfo( name = "order_uuid" )
    @Nullable
    @JsonProperty( "order_uuid" )
    public String orderUuid; // ID Riferimento all’ordine (carrello)

    @ColumnInfo( name = "method" )
    @JsonProperty( "method" )
    public String method; // cash →contanti / pos →carta di credito / invoice →pagamento con fattura

    @ColumnInfo( name = "type" )
    @JsonProperty( "type" )
    public String type; // P → payment (pagamento)/ R → refund (storno)

    @ColumnInfo( name = "value" )
    @JsonProperty( "value" )
    public int value; // Ammontare del pagamento. Se type=P value deve essere sempre ≥ 0; se type=R deve essere < 0

    @ColumnInfo( name = "details" )
    @Nullable
    @JsonProperty( "details" )
    public String details; // JSON object con tutti i dettagli di pagamento ottenuti in risposta dal POS (codice autorizzazione POS, n.fattura, ecc…)

    @ColumnInfo( name = "ts" )
    @JsonProperty( "ts" )
    public String ts ; // Data e ora di pagamento ISO 8601
}
