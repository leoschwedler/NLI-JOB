package it.divitech.nliticketapp.httpclients.registration;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RegistrationDeviceInterface
{
    @POST( "units/register" )
    Call<RegisterDeviceResponse> registerDevice( @Header( "X-REGISTRATION-TOKEN" ) String registrationToken,
                                                 @Header( "X-REQUEST-ID" ) String requestId, // uuid generato [opzionale ma consigliato]
                                                 @Body RegisterDeviceRequest request );

    @POST( "units/confirm" )
    Call<ConfirmRegistrationResponse> confirmRegistration( @Header( "X-REGISTRATION-TOKEN" ) String registrationToken,
                                                           @Header( "X-REQUEST-ID" ) String requestId,  // uuid generato [opzionale ma consigliato]
                                                           @Body RegisterConfirmRequest request );

    @FormUrlEncoded
    @POST( "units/login" )
    Call<LoginDeviceResponse> loginDevice( @Field("username") String appUUID,
                                           @Field("password") String secret,
                                           @Field("client_id") String devHwid );

}
