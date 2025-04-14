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
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class IssueLight
    {
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
    public IssueLight taxIssue = null;
    public List<IssueLight> childrenIssues = new ArrayList<>();

}
