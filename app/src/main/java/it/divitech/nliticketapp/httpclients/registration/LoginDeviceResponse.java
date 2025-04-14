package it.divitech.nliticketapp.httpclients.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginDeviceResponse
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public LoginDeviceResponse()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public LoginDeviceResponse( LoginDeviceResponse other )
    {
        this.accessToken = other.accessToken;
        this.tokenType = other.tokenType;

        if( other.unitSetup != null )
        {
            this.unitSetup = new UnitSetup();
            this.unitSetup.unitId = other.unitSetup.unitId;
            this.unitSetup.userId = other.unitSetup.userId;
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty( "access_token" )
    public String accessToken = "";

    @JsonProperty( "token_type" )
    public String tokenType = "";

    @JsonProperty( "unit_setup" )
    public UnitSetup unitSetup = null;


    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner Class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class UnitSetup
    {
        @JsonProperty( "unit_id" )
        public int unitId;

        @JsonProperty( "user_id" )
        public int userId;
    }
}
