package it.divitech.nliticketapp.ui.activities.SelezioneTurno;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.login.User;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.databinding.ActivitySelezioneTurnoBinding;
import it.divitech.nliticketapp.ui.activities.login.LoginActivity;
import it.divitech.nliticketapp.ui.activities.logout.LogoutActivity;
import it.divitech.nliticketapp.ui.activities.main.MainActivity;
import it.divitech.nliticketapp.ui.activities.splashscreen.SplashScreenActivity;
import it.divitech.nliticketapp.ui.adapters.UserTagAdapter;


public class SelezioneTurnoActivity extends AppCompatActivity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        application = (NLITicketApplication)getApplication();
        binding = it.divitech.nliticketapp.databinding.ActivitySelezioneTurnoBinding.inflate(getLayoutInflater());

        setContentView( binding.getRoot() );

        setupListeners();
        setupSpinner();

        binding.progressBar.setVisibility( View.GONE );

        binding.logoutButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                logout();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void logout()
    {
        new AlertDialog.Builder( this )
                .setMessage( "Sei sicuro di voler effettuare il LOGOUT?" )
                .setCancelable( false )
                .setPositiveButton( "Sì", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        // SelezioneTurnoActivity.super.onBackPressed();

                        Intent logoutActivityIntent = new Intent( SelezioneTurnoActivity.this, LogoutActivity.class );
                        startActivity( logoutActivityIntent );
                        finish();
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
        // Conferma selezione turno Button click
        binding.selezionaTurnoButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                binding.progressBar.setVisibility( View.VISIBLE );

                User.UserTag selectedUserTag = (User.UserTag)binding.listaTurniSpinner.getSelectedItem();
                int position = binding.listaTurniSpinner.getSelectedItemPosition();

                if( selectedUserTag != null )
                {
                    // Se il turno è cambiato, chiudere la sessione corrente e crearne una nuova!
                    if( !application.getCurrentSessionInfo().currentSession.tag.equalsIgnoreCase( selectedUserTag.id ) )
                    {
                        OperatorSession session = application.getCurrentSessionInfo().currentSession;

                        if (session != null) {
                            session.status = "C";
                            session.tsClose = application.toIso8601Local(ZonedDateTime.now());

                            new Thread(() -> {
                                application.getSessionsTable().update(session);
                            }).start();
                        }
                    }

                    binding.selezionaTurnoButton.setEnabled( false );
                    application.hideKeyboard( SelezioneTurnoActivity.this );

                    application.getCurrentSessionInfo().currentSession.tag = selectedUserTag.id;
                    application.getCurrentSessionInfo().zonaDefaultId = selectedUserTag.defaultZoneId;

                    // ASYNC
                    new Thread( ()->
                    {
                        application.getSessionsTable().update( application.getCurrentSessionInfo().currentSession );

                        Intent mainActivityIntent = new Intent( SelezioneTurnoActivity.this, MainActivity.class );
                        startActivity( mainActivityIntent );

                        finish();

                    } ).start();
                }

            }

        } );

        // Selezione turno
        binding.listaTurniSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                binding.selezionaTurnoButton.setEnabled( true );
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent )
            {
                binding.selezionaTurnoButton.setEnabled( false );
            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupSpinner()
    {
        binding.listaTurniSpinner.setEnabled( false );
        binding.progressBar.setVisibility( View.VISIBLE );

        // Valore 'finto' per non avere lo spinner 'collassato' in fase iniziale
        List<String> dummyList = new ArrayList<>( Arrays.asList( " " ) );
        ArrayAdapter dummyAdapter = new ArrayAdapter<String>( this, R.layout.spinner_item_dati_fermata, R.id.spinner_item_text, dummyList );
        dummyAdapter.setDropDownViewResource( R.layout.spinner_dropdown_item );

        //---

        binding.listaTurniSpinner.setAdapter( dummyAdapter );
        binding.listaTurniSpinner.setEnabled( false );

        // ASYNC User check
        new Thread( ()->
        {
            try
            {
                String loggedUserId = application.getCurrentSessionInfo().currentUser.loginUserId;
                listaTurni = application.getUserListaTurni( loggedUserId );

                // UI thread operation
                runOnUiThread(() ->
                {
                    binding.listaTurniSpinner.setEnabled( true );
                    binding.progressBar.setVisibility( View.GONE );

                    UserTagAdapter adapter = new UserTagAdapter( this, listaTurni );

                    binding.listaTurniSpinner.setAdapter( adapter );
                    binding.listaTurniSpinner.setSelection( -1, false );

                } );
            }
            catch( Exception e )
            {
                Toast.makeText( SelezioneTurnoActivity.this, "ERRORE NEL RECUPERO INFORMAZIONI DI SESSIONE", Toast.LENGTH_LONG ).show();
            }

        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed()
    {
        /*
        new AlertDialog.Builder( this )
                .setMessage( "Sei sicuro di voler effettuare il LOGOUT?" )
                .setCancelable( false )
                .setPositiveButton( "Sì", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        // SelezioneTurnoActivity.super.onBackPressed();

                        Intent logoutActivityIntent = new Intent( SelezioneTurnoActivity.this, LogoutActivity.class );
                        startActivity( logoutActivityIntent );
                        finish();
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
        */
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private List<User.UserTag> listaTurni = new ArrayList<>();

    private NLITicketApplication application;
    private ActivitySelezioneTurnoBinding binding;

}