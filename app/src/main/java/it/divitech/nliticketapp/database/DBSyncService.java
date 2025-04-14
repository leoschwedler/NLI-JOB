package it.divitech.nliticketapp.database;

import static it.divitech.nliticketapp.NLITicketApplication.PREFERENCES;
import static it.divitech.nliticketapp.NLITicketApplication.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;
import it.divitech.nliticketapp.data.ticketing.Validation;
import it.divitech.nliticketapp.httpclients.TecBusApiClient;
import retrofit2.Call;
import retrofit2.Response;

public class DBSyncService extends Service
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onCreate()
    {
        super.onCreate();

        application = (NLITicketApplication)getApplication();

        this.sessionsTable = application.getSessionsTable();
        this.paymentsTable = application.getPaymentsTable();
        this.issuesTable = application.getIssuesTable();
        this.validationsTable = application.getValidationsTable();

        preferences = getApplicationContext().getSharedPreferences( PREFERENCES, Context.MODE_PRIVATE );
        preferencesEditor = preferences.edit();

        // Avvia un HandlerThread dedicato (per esecuzione in background)
        handlerThread = new HandlerThread( "DBSyncServiceThread" );
        handlerThread.start();

        backgroundHandler = new Handler( handlerThread.getLooper() );

        NotificationChannel channel = new NotificationChannel( CHANNEL_ID, "DB Sync Channel", NotificationManager.IMPORTANCE_LOW );
        channel.setDescription( "" );

        NotificationManager manager = getSystemService(NotificationManager.class);

        if( manager != null )
            manager.createNotificationChannel( channel );

        uploadTask = new Runnable()
        {
            @Override
            public void run()
            {
                // TEST MAU -> TESTING interferenze con stampa riepilogo. Ripristinare per l'uso normale
                if( !isPaused )
                    uploadData();

                backgroundHandler.postDelayed( this, INTERVAL_MS );
            }
        };
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onDestroy()
    {
        backgroundHandler.removeCallbacks(uploadTask);
        handlerThread.quitSafely();

        super.onDestroy();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public IBinder onBind( Intent intent )
    {
        return binder;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        if( intent != null )
        {
            String action = intent.getAction();

            if( ACTION_START.equals( action ) )
            {
                startForegroundService();
            }
            else if( ACTION_PAUSE.equals( action ) )
            {
                pauseService();
            }
            else if( ACTION_RESUME.equals( action ) )
            {
                resumeService();
            }
            else if( ACTION_STOP.equals( action ) )
            {
                stopForegroundService();
            }
        }

        return START_STICKY;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void startForegroundService()
    {
        Notification notification = createNotification( "DB Sync Service in esecuzione" );

        startForeground( NOTIFICATION_ID, notification );

        backgroundHandler.post( uploadTask );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void pauseService()
    {
        isPaused = true;

        updateNotification( "DB Sync Service in pausa" );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void resumeService()
    {
        isPaused = false;

        updateNotification( "DB Sync Service in esecuzione" );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void stopForegroundService()
    {
        backgroundHandler.removeCallbacks( uploadTask );

        stopForeground( true );
        stopSelf();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void uploadData()
    {
        try
        {
            int maxDays = application.getMaxDaysToStoreDBData();
            String thresholdDateToDelete = application.toIso8601Local( ZonedDateTime.now().minusDays( maxDays ) );

            // Upload Sessioni CHIUSE
            int lastSessionId = getLastUploadedSessionId();
            List<OperatorSession> sessions = sessionsTable.getClosedSessionsBlock( lastSessionId, UPLOAD_BLOCK_SIZE );

            if( !sessions.isEmpty() )
            {
                Call<Object> call = TecBusApiClient.getDatabaseUploadService().uploadSessions( "Bearer " + application.getAccessToken(), sessions );

                Response response = call.execute();

                if( response.code() == 200 )
                {
                    int newSessionId = (int)sessions.get( sessions.size() - 1 ).id;

                    saveLastUploadedSessionId( newSessionId );

                    // Rimuove tutti i dati caricati dopo un tot tempo
                    sessionsTable.deleteFromDate( thresholdDateToDelete );
                }
                else
                {
                    // Handle failure (fire event?)
                    // TODO
                }
            }

            // Upload Sessione APERTA
            OperatorSession openSession = sessionsTable.getOpenSession();

            if( openSession != null )
            {
                List<OperatorSession> opnSess = new ArrayList<>();
                opnSess.add( openSession );

                Call<Object> call = TecBusApiClient.getDatabaseUploadService().uploadSessions( "Bearer " + application.getAccessToken(), opnSess );

                Response response = call.execute();

                if( response.code() == 200 )
                {
                    // Non cancellare. Quella aperta è sempre attiva
                }
                else
                {
                    // Handle failure (fire event?)
                    // TODO
                }
            }

            // Payments
            uploadPaymentsTable( thresholdDateToDelete );

            // Issues
            uploadIssuesTable( thresholdDateToDelete );

            // Validations
            uploadValidationsTable( thresholdDateToDelete );

        }
        catch( Exception e )
        {
            // Handle failure (fire event?)
            // TODO

            Log.e( TAG, e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void uploadPaymentsTable( String thresholdDateToDelete )
    {
        if( !isNetworkAvailable() )
        {
            Log.d( TAG, "Connessione non disponibile: SKIPPED uploadPaymentsTable" );
            return;
        }

        int lastPaymentId = getLastUploadedPaymentId();
        List<Payment> payments = paymentsTable.getPaymentsBlock( lastPaymentId, UPLOAD_BLOCK_SIZE );

        if( !payments.isEmpty() )
        {
            Call<Object> call = TecBusApiClient.getDatabaseUploadService().uploadPayments( "Bearer " + application.getAccessToken(), payments );

            try
            {
                Response response = call.execute();

                if( response.code() == 200 )
                {
                    int newPaymentId = (int)payments.get( payments.size() - 1 ).id;

                    saveLastUploadedPaymentId( newPaymentId );

                    // Rimuove tutti i dati caricati dopo un tot tempo
                    paymentsTable.deleteFromDate( thresholdDateToDelete );
                }
                else
                {
                    // Handle failure (fire event?)
                    // TODO
                }

            }
            catch( Exception e )
            {

            }


        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void uploadIssuesTable( String thresholdDateToDelete )
    {
        if( !isNetworkAvailable() )
        {
            Log.d( TAG, "Connessione non disponibile: SKIPPED uploadIssuesTable" );
            return;
        }

        int lastIssueId = getLastUploadedIssueId();
        List<Issue> issues = issuesTable.getIssuesBlock( lastIssueId, UPLOAD_BLOCK_SIZE );

        if( !issues.isEmpty() )
        {
            Call<Object> call = TecBusApiClient.getDatabaseUploadService().uploadIssues( "Bearer " + application.getAccessToken(), issues );

            try
            {
                Response response = call.execute();

                if( response.code() == 200 )
                {
                    int newIssueId = (int)issues.get( issues.size() - 1 ).id;

                    saveLastUploadedIssueId( newIssueId );

                    // Rimuove tutti i dati caricati dopo un tot tempo
                    issuesTable.deleteFromDate( thresholdDateToDelete );
                }
                else
                {
                    // Handle failure (fire event?
                    // TODO
                }

            }
            catch( Exception e )
            {

            }

        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void uploadValidationsTable( String thresholdDateToDelete )
    {
        if( !isNetworkAvailable() )
        {
            Log.d( TAG, "Connessione non disponibile: SKIPPED uploadValidationsTable" );
            return;
        }

        int lastValidationId = getLastUploadedValidationId();
        List<Validation> validations = validationsTable.getValidationsBlock( lastValidationId, UPLOAD_BLOCK_SIZE );

        if( !validations.isEmpty() )
        {
            Call<Object> call = TecBusApiClient.getDatabaseUploadService().uploadValidations( "Bearer " + application.getAccessToken(), validations );

            try
            {
                Response response = call.execute();

                if( response.code() == 200 )
                {
                    int newValidationId = (int)validations.get( validations.size() - 1 ).localId;

                    saveLastUploadedValidationId( newValidationId );

                    // Rimuove tutti i dati caricati dopo un tot tempo
                    validationsTable.deleteFromDate( thresholdDateToDelete );
                }
                else
                {
                    // Handle failure (fire event?
                    // TODO
                }

            }
            catch( Exception e )
            {

            }

        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private int getLastUploadedValidationId()
    {
        return preferences.getInt( PREFERENCES_LAST_VALIDATIONS_ID_UPLOADED, 0 );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void saveLastUploadedValidationId( int id )
    {
        preferencesEditor.putInt( PREFERENCES_LAST_VALIDATIONS_ID_UPLOADED, id );
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private int getLastUploadedPaymentId()
    {
        return preferences.getInt( PREFERENCES_LAST_PAYMENTS_ID_UPLOADED, 0 );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void saveLastUploadedPaymentId( int id )
    {
        preferencesEditor.putInt( PREFERENCES_LAST_PAYMENTS_ID_UPLOADED, id );
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private int getLastUploadedIssueId()
    {
        return preferences.getInt( PREFERENCES_LAST_ISSUES_ID_UPLOADED, 0 );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void saveLastUploadedIssueId( int id )
    {
        preferencesEditor.putInt( PREFERENCES_LAST_ISSUES_ID_UPLOADED, id );
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private int getLastUploadedSessionId()
    {
        return preferences.getInt( PREFERENCES_LAST_SESSIONS_ID_UPLOADED, 0 );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void saveLastUploadedSessionId( int id )
    {
        preferencesEditor.putInt( PREFERENCES_LAST_SESSIONS_ID_UPLOADED, id );
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Notification createNotification( String text )
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, CHANNEL_ID )
                .setContentTitle( "NLI_DBSync_FgService" )
                .setContentText( text )
                .setSmallIcon( R.drawable.baseline_cloud_upload_24_white )
                .setOngoing( true );

        return builder.build();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private boolean isNetworkAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService( Context.CONNECTIVITY_SERVICE );

        if( cm != null )
        {
            NetworkCapabilities nc = cm.getNetworkCapabilities( cm.getActiveNetwork() );

            return nc != null && ( nc.hasTransport( NetworkCapabilities.TRANSPORT_WIFI ) ||
                                   nc.hasTransport( NetworkCapabilities.TRANSPORT_CELLULAR ) ||
                                   nc.hasTransport( NetworkCapabilities.TRANSPORT_ETHERNET ) );
        }

        return false;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateNotification( String text )
    {
        Notification notification = createNotification( text );

        startForeground( NOTIFICATION_ID, notification );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public class LocalBinder extends Binder
    {
        public DBSyncService getService()
        {
            return DBSyncService.this;
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private final IBinder binder = new LocalBinder();

    private boolean isPaused = false;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private Runnable uploadTask;

    private NLITicketApplication application = null;

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor preferencesEditor = null;

    private OperatorSessionDAO sessionsTable = null;
    private PaymentDAO paymentsTable = null;
    private IssueDAO issuesTable = null;
    private ValidationDAO validationsTable = null;

    public static final String PREFERENCES_LAST_PAYMENTS_ID_UPLOADED = "LAST_PAYMENTS_ID_UPLOADED";
    public static final String PREFERENCES_LAST_ISSUES_ID_UPLOADED = "LAST_ISSUES_ID_UPLOADED";
    public static final String PREFERENCES_LAST_SESSIONS_ID_UPLOADED = "LAST_SESSIONS_ID_UPLOADED";
    public static final String PREFERENCES_LAST_VALIDATIONS_ID_UPLOADED = "LAST_VALIDATIONS_ID_UPLOADED";

    private static final int UPLOAD_BLOCK_SIZE = 100;

    private static final long INTERVAL_MS =  30 * 1000;  //5 * 60 * 1000; // Esempio: 5 minuti
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "NLI_DBSync_FgService_ID";

}
