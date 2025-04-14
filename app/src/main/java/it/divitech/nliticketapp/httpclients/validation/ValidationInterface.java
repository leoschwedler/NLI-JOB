package it.divitech.nliticketapp.httpclients.validation;

import it.divitech.nliticketapp.httpclients.registration.ConfirmRegistrationResponse;
import it.divitech.nliticketapp.httpclients.registration.LoginDeviceResponse;
import it.divitech.nliticketapp.httpclients.registration.RegisterConfirmRequest;
import it.divitech.nliticketapp.httpclients.registration.RegisterDeviceRequest;
import it.divitech.nliticketapp.httpclients.registration.RegisterDeviceResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ValidationInterface
{
    @GET( "tickets/{ticketId}" )
    Call<SearchTicketResponse> searchTicket( @Header( "Authorization" ) String auth, @Path( "ticketId" ) String ticketId );


}
