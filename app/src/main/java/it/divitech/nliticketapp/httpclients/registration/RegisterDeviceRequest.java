package it.divitech.nliticketapp.httpclients.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterDeviceRequest
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterDeviceRequest()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterDeviceRequest( RegisterDeviceRequest other )
    {
        this.devHwid = other.devHwid;
        this.appUuid = other.appUuid;
        this.appType = other.appType;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty("dev_hwid")
    public String devHwid = ""; // hw fingerprint device (IMEI/altro)

    @JsonProperty("app_uuid")
    public String appUuid; // uuidv7 generated during app initialization

    @JsonProperty("app_type")
    public String appType; // Application type palm6k|desk6k
}
