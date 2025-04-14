package it.divitech.nliticketapp.ui.activities.splashscreen;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.data.ticketing.DeviceSettings;
import it.divitech.nliticketapp.databinding.ActivitySplashScreenBinding;
import it.divitech.nliticketapp.helpers.PermissionsManager;
import it.divitech.nliticketapp.httpclients.registration.ConfirmRegistrationResponse;
import it.divitech.nliticketapp.httpclients.registration.LoginDeviceResponse;
import it.divitech.nliticketapp.httpclients.registration.RegisterConfirmRequest;
import it.divitech.nliticketapp.httpclients.registration.RegisterDeviceRequest;
import it.divitech.nliticketapp.httpclients.registration.RegisterDeviceResponse;
import it.divitech.nliticketapp.httpclients.TecBusApiClient;
import it.divitech.nliticketapp.ui.activities.login.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        application = (NLITicketApplication)getApplication();
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());

        setContentView( binding.getRoot() );

        String appVersion = "0.0.0";

        try
        {
            PackageInfo pinfo = getPackageManager().getPackageInfo( getPackageName(), 0 );
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            appVersion = versionName;

            binding.versionTextView.setText( appVersion );
        }
        catch( PackageManager.NameNotFoundException e )
        {

        }

        // Check permissions
        PermissionsManager.setPermissionsCallback( new PermissionsManager.PermissionsCallback()
        {
            @Override
            public void onAllPermissionsGranted()
            {
                checkDeviceRegistration();

                // TEST
                //binding.infoTextView.setText( "(DEBUG)\nAvvio applicazione" );
                //showNextActivity( 3 );
            }

            @Override
            public void onPermissionDenied( String permission )
            {
                Toast.makeText( getApplicationContext(), "Autorizzazione necessaria: " + permission.toUpperCase(), Toast.LENGTH_LONG ).show();
                finish();
            }
        } );

        PermissionsManager.checkPermissions( this );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        PermissionsManager.handlePermissionResult( this, requestCode, permissions, grantResults );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkDeviceRegistration()
    {
        binding.infoTextView.setText( "Verifica registrazione dispositivo" );

        // Recupera dati di registrazione salvati sul device
        String secretToken = application.getSecretToken();
        int unitId = application.getUnitId();

        // Se non presenti / non validi, avvia la procedura di registrazione
        if( secretToken.isBlank() || unitId == 0 )
        {
            binding.infoTextView.setText( "Dispositivo non registrato.\nAvvio procedura di registrazione" );

            // Recupera i dati necessari alla chiamata di registrazione
            String registrationToken = application.getRegistrationToken();
            String androidID = application.getAndroidID();
            String deviceUUID = application.getDeviceUUID();

            if( deviceUUID.isBlank() )
            {
                // Genera un nuovo device UUID se non presente
                deviceUUID = UUID.randomUUID().toString();

                application.saveDeviceUUID( deviceUUID );
            }

            // Effettua la chiamata di registrazione
            RegisterDevice( registrationToken, androidID, deviceUUID, NLITicketApplication.DEVICE_TYPE );

            return false;
        }
        else
        {
            // Avvia fase di login device
            loginDevice();
        }

        return true;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void RegisterDevice( String registrationToken, String hwID, String deviceUUID, String appType )
    {
        RegisterDeviceRequest request = new RegisterDeviceRequest();
        request.devHwid = hwID;
        request.appUuid = deviceUUID;
        request.appType = appType;

        String reqId = UUID.randomUUID().toString();
        Call<RegisterDeviceResponse> call = TecBusApiClient.getRegistrationDeviceService().registerDevice( registrationToken, reqId, request );

        // Async call
        call.enqueue( new Callback<RegisterDeviceResponse>()
        {
            @Override
            public void onResponse( Call<RegisterDeviceResponse> call, Response<RegisterDeviceResponse> response )
            {
                if( response.isSuccessful() )
                {
                    RegisterDeviceResponse regResponse = response.body();

                    // Invia conferma di registrazione al B.E.
                    binding.infoTextView.setText( "Invio conferma di registrazione..." );

                    confirmRegistration( registrationToken, regResponse );
                }
                else
                {
                    /*if( response.code() == 999 ) // Handle specific code for access token expired
                    {
                        binding.infoTextView.setText( "Autorizzazione scaduta. Riavvio dell'applicazione" );
                        application.restartApp( SplashScreenActivity.this, SplashScreenActivity.class );
                    }
                    else
                    {*/

                        binding.infoTextView.setText( "Errore di registrazione" );
                        terminateApp( 5 );

                    /*}*/
                }
            }

            @Override
            public void onFailure( Call<RegisterDeviceResponse> call, Throwable t )
            {
                binding.infoTextView.setText( "Chiamata di registrazione dispositivo fallita.\nL'applicazione verrà terminata" );
                terminateApp( 5 );
            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void confirmRegistration( String registrationToken, RegisterDeviceResponse regResponse )
    {
        // Chiamata per confermare la registrazione del dispositivo
        RegisterConfirmRequest request = new RegisterConfirmRequest();
        request.devHwid = application.getAndroidID();
        request.appUuid = application.getDeviceUUID();
        request.appType = NLITicketApplication.DEVICE_TYPE;
        request.unitId = regResponse.unitId;

        String reqId = UUID.randomUUID().toString();
        Call<ConfirmRegistrationResponse> call = TecBusApiClient.getRegistrationDeviceService().confirmRegistration( registrationToken, reqId, request );

        // Async call
        call.enqueue( new Callback<ConfirmRegistrationResponse>()
        {
            @Override
            public void onResponse( Call<ConfirmRegistrationResponse> call, Response<ConfirmRegistrationResponse> response )
            {
                if( response.isSuccessful() )
                {
                    ConfirmRegistrationResponse confirmRegResponse = response.body();

                    // Salva i dati di registrazione sul dispositivo
                    application.saveSecretToken( regResponse.secret );
                    application.saveUnitId( regResponse.unitId );

                    // Avvia fase di login device
                    loginDevice();
                }
                else
                {
                    binding.infoTextView.setText( "Errore durante la conferma della registrazione" );
                    terminateApp( 5 );
                }
            }

            @Override
            public void onFailure( Call<ConfirmRegistrationResponse> call, Throwable t )
            {
                binding.infoTextView.setText( "Chiamata conferma registrazione fallita.\nL'applicazione verrà terminata" );
                terminateApp( 5 );
            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void loginDevice()
    {
        Call<LoginDeviceResponse> call = TecBusApiClient.getRegistrationDeviceService().loginDevice( application.getDeviceUUID(),
                                                                                                     application.getSecretToken(),
                                                                                                     application.getAndroidID() );

        // Async call
        call.enqueue( new Callback<LoginDeviceResponse>()
        {
            @Override
            public void onResponse( Call<LoginDeviceResponse> call, Response<LoginDeviceResponse> response )
            {
                if( response.isSuccessful() )
                {
                    binding.infoTextView.setText( "Dispositivo autorizzato" );
                    application.saveAccessToken( response.body().accessToken );

                    // Verifica aggiornamenti tabelle database
                    checkDatabaseUpdates();

                }
                else
                {
                    binding.infoTextView.setText( "Login dispositivo non riuscita.\nModalità OFFLINE" );
                    //terminateApp( 5 );

                    // Verifica aggiornamenti tabelle database
                    checkDatabaseUpdates();
                }
            }

            @Override
            public void onFailure( Call<LoginDeviceResponse> call, Throwable t )
            {
                binding.infoTextView.setText( "Chiamata Login dispositivo fallita.\nModalità OFFLINE" );
                //terminateApp( 5 );

                // Verifica aggiornamenti tabelle database
                checkDatabaseUpdates();
            }

        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void checkDatabaseUpdates()
    {
        binding.infoTextView.setText( "Verifica aggiornamenti database" );

        // ASYNC
        new Thread( () ->
        {
            boolean usersOK = application.checkUsersDatabaseUpdate();
            boolean feesOK = application.checkFeesDatabaseUpdate();
            boolean faresOK = application.checkFaresDatabaseUpdate();
            boolean zonesOK = application.checkZonesDatabaseUpdate();
            boolean settingsOK = application.checkSettingsDatabaseUpdate();

            if( settingsOK )
            {
                DeviceSettings setting = application.getDeviceSettingsTable().getSetting( "group_ticket_threshold" );

                try
                {
                    if( setting != null )
                        application.setGroupsCap( Integer.valueOf( setting.value ) );
                }
                catch( Exception err )
                {

                }
            }

            application.initDB();

            runOnUiThread( () ->
            {
                if( usersOK && feesOK && faresOK && zonesOK && settingsOK )
                {
                    binding.infoTextView.setText( "Avvio applicazione" );
                    showNextActivity( 3 );
                }
                else
                {
                    binding.infoTextView.setText( "Aggiornamento database fallito.\nL'applicazione verrà terminata" );
                    terminateApp( 5 );
                }
            } );

        } ).start();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showNextActivity( long delay )
    {
        new Handler( Looper.getMainLooper() ).postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                Intent loginActivityIntent = new Intent( SplashScreenActivity.this, LoginActivity.class );
                startActivity( loginActivityIntent );

                finish();
            }

        }, delay * 1000 );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void terminateApp( long delay )
    {
        new Handler( Looper.getMainLooper() ).postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                finishAndRemoveTask();
            }

        }, delay * 1000 );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private NLITicketApplication application;
    private ActivitySplashScreenBinding binding;

}