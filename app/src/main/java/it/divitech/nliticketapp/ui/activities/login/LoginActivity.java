package it.divitech.nliticketapp.ui.activities.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.login.User;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.databinding.ActivityLoginBinding;
import it.divitech.nliticketapp.ui.activities.SelezioneTurno.SelezioneTurnoActivity;
import it.divitech.nliticketapp.ui.activities.logout.LogoutActivity;
import it.divitech.nliticketapp.ui.activities.main.MainActivity;

public class LoginActivity extends AppCompatActivity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        application = (NLITicketApplication)getApplication();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView( binding.getRoot() );

        // ** TEST / DEBUG ** remove before final release
        binding.usernameTextEdit.setText( "1070" );
        binding.passwordTextEdit.setText( "71za" );

        binding.loginButton.setEnabled( false );

        setupListeners();

        // PASSWORD
        // User: 1070        Pwd: 71za

        binding.progressBar.setVisibility( View.GONE );
        binding.loginButton.setEnabled( true );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListeners()
    {
        // Login button click
        binding.loginButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                application.hideKeyboard( LoginActivity.this );

                String userName = binding.usernameTextEdit.getText().toString();
                String userPwd = binding.passwordTextEdit.getText().toString();

                if( userName == null || userName.isBlank() )
                {
                    Toast.makeText( LoginActivity.this, "Compilare il campo NOME UTENTE", Toast.LENGTH_LONG ).show();
                    return;
                }

                if( userPwd == null || userPwd.isBlank() )
                {
                    Toast.makeText( LoginActivity.this, "Compilare il campo PASSWORD", Toast.LENGTH_LONG ).show();
                    return;
                }

                //---

                // Login
                checkLoginOffline( userName, userPwd );

            }
        } );

        // Mostra/Nasconde password in chiaro (toggle visibility)
        binding.passwordTextEdit.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch( View v, MotionEvent event )
            {
                // Attiva la funzionalità solo se presente un right drawable
                if( binding.passwordTextEdit.getCompoundDrawablesRelative()[ 2 ] == null )
                    return false;

                // Dimensione orizzontale del drawable
                int rightDrawableWidth = binding.passwordTextEdit.getCompoundDrawablesRelative()[ 2 ].getBounds().right;

                // Coordinata dx del controllo EditText (escluso drawable)
                int editTextRight = binding.passwordTextEdit.getRight() - binding.passwordTextEdit.getPaddingRight() -  rightDrawableWidth;

                // Attiva la funzionalità solo se il touch è di tipo DOWN ed è avvenuto nella zona del drawable
                if( event.getRawX() < editTextRight || event.getAction() == MotionEvent.ACTION_UP )
                {
                    binding.passwordTextEdit.setTransformationMethod( PasswordTransformationMethod.getInstance() );
                    binding.passwordTextEdit.setSelection( binding.passwordTextEdit.getText().length() );
                    binding.passwordTextEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);

                    return false;
                }

                // In tutti gli altri casi, la password deve essere nascosta
                binding.passwordTextEdit.setTransformationMethod( HideReturnsTransformationMethod.getInstance() );
                binding.passwordTextEdit.setSelection( binding.passwordTextEdit.getText().length() );
                binding.passwordTextEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_on, 0);

                return true;
            }

        } );


    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private boolean checkLogin( String userName, String userPwd )
    {

        return false;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private boolean checkLoginOffline( String userName, String userPwd )
    {
        binding.progressBar.setVisibility( View.VISIBLE );
        binding.loginButton.setEnabled( false );

        // ASYNC User check (OFFLINE)
        new Thread( ()->
        {
            User user = application.getUsersTable().getUserByLoginId( userName );

            if( user != null )
            {
                String hashPwd = application.generateLoginPasswordHash( userPwd, String.valueOf( user.loginUserId ), user.salt );

                if( !hashPwd.equals( user.password ) )
                {
                    runOnUiThread( ()->
                    {
                        // Login FAILURE
                        Toast.makeText( LoginActivity.this, "PASSWORD errata", Toast.LENGTH_LONG ).show();
                        binding.progressBar.setVisibility( View.GONE );
                        binding.loginButton.setEnabled( true );

                        return;
                    } );
                }
                else
                {
                    // Chiude sessioni di altri operatori eventualmente ancora aperte, altrimenti usa la sessione esistente se già aperta da noi o ne crea una nuova
                    OperatorSession lastOpenSession = application.getSessionsTable().getOpenSession();

                    if( lastOpenSession != null )
                    {
                        boolean closeLastSession = false;

                        // Controlla se è una sessione dell'utente corrente
                        if( lastOpenSession.loginUserId.equals( user.loginUserId ) &&
                            lastOpenSession.agencyId == user.agencyId )
                        {
                            // Check Sessione scaduta (la sessione può rimanere aperta max 24h)
                            ZonedDateTime tsOpen = application.fromIso8601ToLocal( lastOpenSession.tsOpen );
                            ZonedDateTime now = ZonedDateTime.now();
                            Duration elapsed = Duration.between( tsOpen, now );

                            long elapsedMins = elapsed.toMinutes();

                            if( elapsedMins >= 1440 )
                                closeLastSession = true;
                        }
                        else
                        {
                            closeLastSession = true;
                        }

                        if( closeLastSession )
                        {
                            // Force Close session
                            application.closeCurrentOperatorSession( true );

                            lastOpenSession = null;
                        }

                    }

                    long sessionId = -1;
                    OperatorSession session = null;

                    if( lastOpenSession == null )
                    {
                        // Sessione nulla o scaduta, ne creo una nuova
                        session = new OperatorSession();
                        session.loginUserId = user.loginUserId;
                        session.agencyId = user.agencyId;
                        session.unitId = application.getUnitId();
                        session.status = "O";
                        session.tsOpen = application.toIso8601Local( ZonedDateTime.now() );

                        // Aggiorna DB
                        sessionId = application.getSessionsTable().insert( session );
                        session.id = sessionId;
                    }
                    else
                    {
                        session = lastOpenSession;
                    }

                    if( session != null )
                    {
                        application.saveLoginInfo( user, session );

                        if( user.tags == null || user.tags.isEmpty() )
                        {
                            // Non sono presenti tag associati (TURNI), vai direttamente alla MainActivity
                            Intent mainActivityIntent = new Intent( LoginActivity.this, MainActivity.class );
                            startActivity( mainActivityIntent );
                        }
                        else
                        {
                            // Visualizza activity di scelta turno
                            Intent turnoActivityIntent = new Intent( LoginActivity.this, SelezioneTurnoActivity.class );
                            startActivity( turnoActivityIntent );
                        }
                    }

                    // FAILURE
                    OperatorSession finalSession = session;

                    runOnUiThread( ()->
                    {
                        String error = "";

                        if( finalSession == null )
                            error = "ERRORE nel recupero della Sessione";

                        Toast.makeText( LoginActivity.this, error, Toast.LENGTH_LONG ).show();
                        binding.progressBar.setVisibility( View.GONE );
                        binding.loginButton.setEnabled( true );

                    } );

                }

            }
            else
            {
                runOnUiThread( ()->
                {
                    // Login FAILURE
                    Toast.makeText( LoginActivity.this, "NOME UTENTE non riconosciuto", Toast.LENGTH_LONG ).show();
                    binding.progressBar.setVisibility( View.GONE );
                    binding.loginButton.setEnabled( true );

                } );
            }

        } ).start();

        return true;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder( this )
                .setMessage( "Sei sicuro di voler uscire?" )
                .setCancelable( false )
                .setPositiveButton( "Sì", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        // LoginActivity.super.onBackPressed();

                        finishAffinity();
                    }
                } )
                .setNegativeButton( "No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.dismiss();
                    }
                } )
                .show();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private NLITicketApplication application;
    private ActivityLoginBinding binding;

    private final int MS_IN_A_DAY = 86400000;
}