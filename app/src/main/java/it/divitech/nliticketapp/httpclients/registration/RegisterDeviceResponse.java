package it.divitech.nliticketapp.httpclients.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterDeviceResponse
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterDeviceResponse()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public RegisterDeviceResponse( RegisterDeviceResponse other )
    {
        this.secret = other.secret;
        this.unitId = other.unitId;
        this.status = other.status;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty("secret")
    public String secret = "";

    @JsonProperty("unit_id")
    public int unitId = 0;

    @JsonProperty("status")
    public String status = "";

}
