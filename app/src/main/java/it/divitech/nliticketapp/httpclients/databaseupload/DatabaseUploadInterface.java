package it.divitech.nliticketapp.httpclients.databaseupload;

import java.util.List;

import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;
import it.divitech.nliticketapp.data.ticketing.Validation;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DatabaseUploadInterface
{
    @Headers( { "accept: application/json", "Content-Type: application/json" } )
    @POST( "upload/operatorssessions/" )
    Call<Object> uploadSessions( @Header( "Authorization" ) String auth, @Body List<OperatorSession> sessions );

    @Headers( { "accept: application/json", "Content-Type: application/json" } )
    @POST( "upload/payments/" )
    Call<Object> uploadPayments( @Header( "Authorization" ) String auth, @Body List<Payment> payments );

    @Headers( { "accept: application/json", "Content-Type: application/json" } )
    @POST( "upload/issues/" )
    Call<Object> uploadIssues( @Header( "Authorization" ) String auth, @Body List<Issue> issues );

    @Headers( { "accept: application/json", "Content-Type: application/json" } )
    @POST( "upload/validations/" )
    Call<Object> uploadValidations( @Header( "Authorization" ) String auth, @Body List<Validation> validations );
}
