package it.divitech.nliticketapp.data.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TerminalStatusResponse
{
    @JsonProperty( "additionalData" )
    public Object additionalData = null;

    @JsonProperty( "gtId" )
    public String gtId = "";

    @JsonProperty( "receipt" )
    public String receipt = "";

    @JsonProperty( "result" )
    public String result = "";

    @JsonProperty( "terminalDate" )
    public String terminalDate = "";

    @JsonProperty( "terminalId" )
    public String terminalId = "";

    @JsonProperty( "terminalSW" )
    public String terminalSW = "";

    @JsonProperty( "terminalStatus" )
    public String terminalStatus = "";

    @JsonProperty( "timestamp" )
    public String timestamp = "";

    @JsonProperty( "transactionMode" )
    public String transactionMode = "";

    @JsonProperty( "uuid" )
    public String uuid = "";

}
