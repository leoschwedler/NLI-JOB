package it.divitech.nliticketapp.data.carrello;

import androidx.room.ColumnInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Issue;

public class ShoppingCartItem
{

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCartItem()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCartItem( ShoppingCartItem other )
    {
        if( other != null )
        {
            this.mainIssue = new IssueLight( other.mainIssue );
            this.registeredMainIssueIds = other.registeredMainIssueIds != null ? new ArrayList<>( other.registeredMainIssueIds ) : new ArrayList<>();

            for( IssueLight issue : other.childrenIssues )
                this.childrenIssues.add( new IssueLight( issue ) );
        }
    }



    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class IssueLight
    {
        public IssueLight()
        {

        }

        //---

        public IssueLight( IssueLight original )
        {
            if( original == null )
                return;

            this.ts = original.ts;
            this.feeId = original.feeId;
            this.feeDescription = original.feeDescription;
            this.quantity = original.quantity;
            this.tripsCount = original.tripsCount;
            this.minutes = original.minutes;
            this.fromZoneId = original.fromZoneId;
            this.toZoneId = original.toZoneId;
            this.fromZoneLabel = original.fromZoneLabel;
            this.toZoneLabel = original.toZoneLabel;
            this.value = original.value;
            this.outMedia = original.outMedia;
            this.orderUuid = original.orderUuid;
            this.parentUuid = original.parentUuid;
            this.uuid = original.uuid;
            this.autoValidation = original.autoValidation;
        }

        //---

        public String ts = null;
        public int feeId = 0;
        public String feeDescription = "";
        public int quantity = 0;
        public int tripsCount = 0;
        public int minutes = 0;
        public int fromZoneId = 0;
        public int toZoneId = 0;
        public String fromZoneLabel = "";
        public String toZoneLabel = "";
        public int value = 0;
        public String outMedia = "";
        public String orderUuid = null;
        public String parentUuid = null;
        public String uuid = "";
        public boolean autoValidation = false;

    }




    //-----------------------------------------------------------------------------------------------------------------------------------------

    public IssueLight mainIssue = null;
    public List<IssueLight> childrenIssues = new ArrayList<>();
    public List<Long> registeredMainIssueIds = new ArrayList<>();

}
