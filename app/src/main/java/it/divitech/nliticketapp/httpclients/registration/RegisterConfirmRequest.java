package it.divitech.nliticketapp.httpclients.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterConfirmRequest
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterConfirmRequest()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterConfirmRequest( RegisterConfirmRequest other )
    {
        this.unitId = other.unitId;
        this.devHwid = other.devHwid;
        this.appUuid = other.appUuid;
        this.appType = other.appType;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty("unit_id")
    public int unitId = 0;

    @JsonProperty("dev_hwid")
    public String devHwid = ""; // hw fingerprint device (IMEI/altro)

    @JsonProperty("app_uuid")
    public String appUuid; // uuidv7 generated during app initialization

    @JsonProperty("app_type")
    public String appType; // Application type palm6k|desk6k
}
