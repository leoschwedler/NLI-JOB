package it.divitech.nliticketapp.data.ticketing;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity( tableName = "issues_table" )
public class Issue
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Issue()
    {
        this.id = 0;
        this.unitId = 0;
        this.orderUuid = null;
        this.uuid = null;
        this.parentUuid = null;
        this.type = "";
        this.userId = 0;
        this.feeId = 0;
        this.value = 0;
        this.discountValue = 0;
        this.discountReason = null;
        this.ts = null;
        this.paymentId = 0;
        this.fromZoneId = null;
        this.toZoneId = null;
        this.minutes = 0;
        this.tripsCount = 0;
        this.details = null;
        this.travelerId = null;
        this.mediaType = null;
        this.mediaHwid = null;
        this.validFrom = null;
        this.validTo = null;
        this.quantity = 0;
        this.agencyId = 0;
        this.opSessionId = 0;

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Issue( Issue other )
    {
        this.id = other.id;
        this.unitId = other.unitId;
        this.orderUuid = other.orderUuid;
        this.uuid = other.uuid;
        this.parentUuid = other.parentUuid;
        this.type = other.type;
        this.userId = other.userId;
        this.feeId = other.feeId;
        this.value = other.value;
        this.discountValue = other.discountValue;
        this.discountReason = other.discountReason;
        this.ts = other.ts;
        this.paymentId = other.paymentId;
        this.version = other.version;
        this.fromZoneId = other.fromZoneId;
        this.toZoneId = other.toZoneId;
        this.minutes = other.minutes;
        this.tripsCount = other.tripsCount;
        this.details = other.details;
        this.travelerId = other.travelerId;
        this.mediaType = other.mediaType;
        this.mediaHwid = other.mediaHwid;
        this.validFrom = other.validFrom;
        this.validTo = other.validTo;
        this.quantity = other.quantity;
        this.agencyId = other.agencyId;
        this.opSessionId = other.opSessionId;

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    @JsonProperty( "id" )
    public long id; // Auto Increment Primary Key

    @ColumnInfo( name = "unit_id" )
    @JsonProperty( "unit_id" )
    public int unitId; // Quello ottenuto in fase di registrazione

    @ColumnInfo( name = "order_uuid" )
    @Nullable
    @JsonProperty( "order_uuid" )
    public String orderUuid; // uuid riferimento ordine (carrello)

    @ColumnInfo( name = "uuid" )
    @Nullable
    @JsonProperty( "uuid" )
    public String uuid; // uuid riferimento record di emissione

    @ColumnInfo( name = "parent_uuid" )
    @Nullable
    @JsonProperty( "parent_uuid" )
    public String parentUuid; // uuid riferimento record emissione “padre”

    @ColumnInfo( name = "type" )
    @JsonProperty( "type" )
    public String type; // T → ticket (emissione ticket) / V → void (annullamento ticket)

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId; // 2 = NLI

    @ColumnInfo( name = "fee_id" )
    @JsonProperty( "fee_id" )
    public int feeId; // codice tariffa

    @ColumnInfo( name = "value" )
    @JsonProperty( "value" )
    public int value; // valore emissione in eurocent

    @ColumnInfo( name = "discount_value" )
    @JsonProperty( "discount_value" )
    public int discountValue; // valore assoluto sconto in eurocent; default 0

    @ColumnInfo( name = "discount_reason" )
    @Nullable
    @JsonProperty( "discount_reason" )
    public String discountReason; // codice motivazione sconto

    @ColumnInfo( name = "ts" )
    @JsonProperty( "ts" )
    public String ts ; // Data e ora di emissione ISO 8601

    @ColumnInfo( name = "payment_id" )
    @JsonProperty( "payment_id" )
    public long paymentId; // Id record di pagamento (Payment)

    @ColumnInfo( name = "version" )
    @JsonProperty( "version" )
    public String version; // Versione del tariffario utilizzato per l’emissione

    @ColumnInfo( name = "from_zone_id" )
    @JsonProperty( "from_zone_id" )
    public Integer fromZoneId; // id zona di partenza

    @ColumnInfo( name = "to_zone_id" )
    @JsonProperty( "to_zone_id" )
    public Integer toZoneId; // id zona di arrivo

    @ColumnInfo( name = "minutes" )
    @JsonProperty( "minutes" )
    public int minutes; // Validità singola corsa in minuti, indicata nella tariffa. 0 - nessuna restrizione ≥1 validità in minuti

    @ColumnInfo( name = "trips_count" )
    @JsonProperty( "trips_count" )
    public int tripsCount; // Numero di corse associate all’emissione (indicato nella tariffa)/ 0 → Corse Illimitate-Non Definite / 1 → Corsa Singola / >1 →Multicorsa

    @ColumnInfo( name = "details" )
    @Nullable
    @JsonProperty( "details" )
    public String details; // Per uso futuro

    @ColumnInfo( name = "traveler_id" )
    @JsonProperty( "traveler_id" )
    public Integer travelerId; // Codice tesserato. Valido solo per tariffe  personali →personal: true

    @ColumnInfo( name = "media_type" )
    @Nullable
    @JsonProperty( "media_type" )
    public String mediaType; // Tipologia del supporto utilizzato. Q → qr-code / C → calypso / U → mifare-ul

    @ColumnInfo( name = "media_hwid" )
    @JsonProperty( "media_hwid" )
    public String mediaHwid; // Hardware ID del supporto utilizzato. Obbligatorio solo per typecalypso e mifare-ul

    @ColumnInfo( name = "valid_from" )
    @JsonProperty( "valid_from" )
    public String validFrom ; // Data e ora di inizio validità ISO 8601. Solo per tariffe con indicazione di validità fissata all’emissione (validity.trigger=issue)

    @ColumnInfo( name = "valid_to" )
    @JsonProperty( "valid_to" )
    public String validTo ; // Data e ora di fine validità ISO 8601. Solo per tariffe con indicazione di validità fissata all’emissione (validity.trigger=issue)

    @ColumnInfo( name = "quantity" )
    @JsonProperty( "quantity" )
    public int quantity; // Utilizzato per emissioni di un biglietto singolo per gruppi di persone con quantità variabile

    @ColumnInfo( name = "agency_id" )
    @JsonProperty( "agency_id" )
    public int agencyId; // agency_id associato alla sessione utente corrente

    @ColumnInfo( name = "op_session_id" )
    @JsonProperty( "op_session_id" )
    public long opSessionId; // Riferimento alla sessione operatore

}
