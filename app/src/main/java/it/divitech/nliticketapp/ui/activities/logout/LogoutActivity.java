package it.divitech.nliticketapp.ui.activities.logout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.time.ZonedDateTime;
import java.util.UUID;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.databinding.ActivityLoginBinding;
import it.divitech.nliticketapp.databinding.ActivityLogoutScreenBinding;
import it.divitech.nliticketapp.ui.activities.login.LoginActivity;
import it.divitech.nliticketapp.ui.activities.splashscreen.SplashScreenActivity;

public class LogoutActivity extends AppCompatActivity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        binding = ActivityLogoutScreenBinding.inflate( getLayoutInflater());
        application = (NLITicketApplication)getApplication();

        setContentView( binding.getRoot() );

        showMessage( "Logout" );

        // Chiudere sessione corrente
        // ASYNC - Close prev. session (if any)
        new Thread( ()->
        {
            if( application.getCurrentSessionInfo() != null )
            {
                OperatorSession session = application.getCurrentSessionInfo().currentSession;

                if( session != null )
                {
                    session.status = "C";
                    session.tsClose = application.toIso8601Local( ZonedDateTime.now() );

                    application.getSessionsTable().update( session );
                    application.clearCurrentLoggedUserData();
                }

            }

            runOnUiThread( ()->
            {
                binding.progressBar.setVisibility( View.GONE );
                showNextActivity( 3 );
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
                Intent loginActivityIntent = new Intent( LogoutActivity.this, LoginActivity.class );
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

    private void showMessage( String text )
    {
        runOnUiThread( ()->
        {
            binding.infoTextView.setText( text );
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private NLITicketApplication application;
    private ActivityLogoutScreenBinding binding;

}