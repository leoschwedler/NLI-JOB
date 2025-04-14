package it.divitech.nliticketapp.data.carrello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.divitech.nliticketapp.ui.activities.main.MainActivity;

public class PrintQueueData
{
    public String issueUUID = "";

    public int logoResourceId = -1;
    public int separatorResourceId = -1;
    public String title = "";
    public String timeStamp = "";
    public String typology = "";
    public String zones = "";
    public String quantity = "";
    public String price = "";
    public List<String> additionalFees = new ArrayList<>();
    public String qrCodeString = "";
    public int partialAmount = 0;

    public String idOperatore = "";
    public String idDispositivo = "";
    public String idEmissione = "";

    // ---

    // public HashMap<Integer, VatInfo> vatTable = new HashMap<Integer, VatInfo>();
    public int totalAmount = 0;
    public String paymentType = "";

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner Class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class VatInfo
    {
        public int vat = 0;
        public int value = 0;
        public int vatValue = 0;
        public int index = 0;

        public VatInfo()
        {
            vat = 0;
            value = 0;
            vatValue = 0;
            index = 0;
        }

        public VatInfo( int vat, int value, int vatValue, int index )
        {
            this.vat = vat;
            this.value = value;
            this.vatValue = vatValue;
            this.index = index;
        }

    }
}
