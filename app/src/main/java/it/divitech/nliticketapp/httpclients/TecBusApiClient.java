package it.divitech.nliticketapp.httpclients;

import static it.divitech.nliticketapp.NLITicketApplication.TAG;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import it.divitech.nliticketapp.httpclients.databasedownload.DatabaseDownloadInterface;
import it.divitech.nliticketapp.httpclients.databaseupload.DatabaseUploadInterface;
import it.divitech.nliticketapp.httpclients.registration.RegistrationDeviceInterface;
import it.divitech.nliticketapp.httpclients.validation.ValidationInterface;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class TecBusApiClient
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    private TecBusApiClient()
    {

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static String getBaseUrl()
    {
        return BASE_URL;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static void setBaseUrl( String newURL )
    {
        BASE_URL = newURL;
        retrofit = null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static RegistrationDeviceInterface getRegistrationDeviceService()
    {
        if( registrationService == null )
            registrationService = getRetrofitInstance().create( RegistrationDeviceInterface.class );

        return registrationService;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static DatabaseDownloadInterface getDatabaseDownloadService()
    {
        if( downloadDbService == null )
            downloadDbService = getRetrofitInstance().create( DatabaseDownloadInterface.class );

        return downloadDbService;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static DatabaseUploadInterface getDatabaseUploadService()
    {
        if( uploadDbService == null )
            uploadDbService = getRetrofitInstance().create( DatabaseUploadInterface.class );

        return uploadDbService;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static ValidationInterface getValidationService()
    {
        if( validationService == null )
            validationService = getRetrofitInstance().create( ValidationInterface.class );

        return validationService;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static Retrofit getRetrofitInstance()
    {
        if( retrofit == null )
        {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor( new SafeHttpLogger() );
            logging.setLevel( HttpLoggingInterceptor.Level.BODY );

            OkHttpClient okClient = new OkHttpClient.Builder()
                    .addInterceptor( logging )
                    .readTimeout( 1, TimeUnit.MINUTES )
                    .connectTimeout( 1, TimeUnit.MINUTES )
                    .writeTimeout( 1, TimeUnit.MINUTES )
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            retrofit = new retrofit2.Retrofit.Builder().baseUrl( BASE_URL )
                    .addConverterFactory( JacksonConverterFactory.create( objectMapper ) )
                    .client( okClient )
                    .build();

        }

        return retrofit;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Helper class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class SafeHttpLogger implements HttpLoggingInterceptor.Logger
    {
        @Override
        public void log( String message )
        {
            String safeMessage = message;
            safeMessage = safeMessage.replaceAll( "(?i)(Authorization:\\s*Bearer\\s+)[^\\s]+", "$1****" );

            Log.d( TAG, safeMessage );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static String BASE_URL = "https://nli.tecbus.dev/api/v1/";
    private static Retrofit retrofit = null;
    private static RegistrationDeviceInterface registrationService;
    private static DatabaseDownloadInterface downloadDbService;
    private static DatabaseUploadInterface uploadDbService;
    private static ValidationInterface validationService;
}
