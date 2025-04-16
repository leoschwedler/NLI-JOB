package it.divitech.nliticketapp.ui.activities.main;

import static androidx.navigation.Navigation.findNavController;
import static it.divitech.nliticketapp.NLITicketApplication.TAG;

import android.animation.LayoutTransition;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.usdk.apiservice.aidl.vectorprinter.Alignment;
import com.usdk.apiservice.aidl.vectorprinter.OnPrintListener;
import com.usdk.apiservice.aidl.vectorprinter.TextSize;
import com.usdk.apiservice.aidl.vectorprinter.VectorPrinterData;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.Ticketdata;
import it.divitech.nliticketapp.data.carrello.PrintQueueData;
import it.divitech.nliticketapp.data.carrello.PurchaseSummaryData;
import it.divitech.nliticketapp.data.carrello.ShoppingCart;
import it.divitech.nliticketapp.data.carrello.ShoppingCartItem;
import it.divitech.nliticketapp.data.payment.PaymentResponse;
import it.divitech.nliticketapp.data.payment.TerminalStatusResponse;
import it.divitech.nliticketapp.data.ticketing.Fare;
import it.divitech.nliticketapp.data.ticketing.Fee;
import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;
import it.divitech.nliticketapp.data.ticketing.Validation;
import it.divitech.nliticketapp.data.ticketing.Zone;
import it.divitech.nliticketapp.database.DBSyncService;
import it.divitech.nliticketapp.databinding.ActivityMainBinding;
import it.divitech.nliticketapp.helpers.Base45;
import it.divitech.nliticketapp.helpers.DeviceHelper;
import it.divitech.nliticketapp.httpclients.TecBusApiClient;
import it.divitech.nliticketapp.httpclients.validation.SearchTicketResponse;
import it.divitech.nliticketapp.ui.activities.logout.LogoutActivity;
import it.divitech.nliticketapp.ui.activities.settings.SettingsActivity;
import it.divitech.nliticketapp.ui.adapters.FeeAdapter;
import it.divitech.nliticketapp.ui.adapters.ZoneAdapter;
import it.divitech.nliticketapp.ui.controls.CarrelloView;
import it.divitech.nliticketapp.ui.controls.QuantitativoTitoloViaggioView;
import it.divitech.nliticketapp.ui.controls.QuantitySelectorView;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DeviceHelper.ServiceReadyListener
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        application = (NLITicketApplication)getApplication();
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView( binding.getRoot() );

        binding.emissioneTicketView.getRoot().setVisibility( View.GONE );
        binding.validazioneControlloTicketView.getRoot().setVisibility( View.GONE );

        binding.logoutButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                logout();
            }
        } );

        cleanupEmissioneTicketUI();
        updateCarrelloButtonStatus();
        showTotale( "" );

        request_CHECK_POS_STATUS();

        init();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        DeviceHelper.getInstance().setServiceListener( null );
        DeviceHelper.getInstance().unregister();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume()
    {
        super.onResume();

        if( binding.validazioneControlloTicketView.getRoot().getVisibility() == View.VISIBLE )
            restartCameraPreview();

        if( bNfcReadingActive && nfcAdapter != null )
            nfcAdapter.enableForegroundDispatch( this, nfcPendingIntent, intentFilters, null );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onPause()
    {
        super.onPause();

        if( nfcAdapter != null )
            nfcAdapter.disableForegroundDispatch( this );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onStart()
    {
        super.onStart();

        bindToDBSyncService();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onStop()
    {
        super.onStop();

        if( dbSyncServiceBound )
        {
            dbSyncServiceBound = false;
            unbindService( dbSyncServiceConnection );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onDeviceServiceReady( String version )
    {
        // Ora possiamo usare l'SDK di Ingienico
        DeviceHelper.getInstance().register( true );

        application.initPrinter();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void init()
    {
        // setupNfcReader();

        binding.backButton.setVisibility( View.GONE );
        //binding.hamburgerMenu.setVisibility( View.INVISIBLE );

        DeviceHelper.getInstance().setServiceListener( this );

        // Gestione pagine
        binding.bottomNavigationBar.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected( @NonNull MenuItem item )
            {
                int itemId = item.getItemId();

                if( itemId == R.id.validazioneTicketFragment )
                {
                    modalitaViewValidazioneControllo = MODALITA_VALIDAZIONE_TICKET;

                    binding.emissioneTicketView.getRoot().setVisibility( View.GONE );
                    binding.validazioneControlloTicketView.getRoot().setVisibility( View.VISIBLE );

                    setTopbarTitle( "Validazione" );
                    showQrCodeScanner();

                    return true;
                }

                if( itemId == R.id.controlloTicketFragment )
                {
                    modalitaViewValidazioneControllo = MODALITA_CONTROLLO_TICKET;

                    binding.emissioneTicketView.getRoot().setVisibility( View.GONE );
                    binding.validazioneControlloTicketView.getRoot().setVisibility( View.VISIBLE );

                    setTopbarTitle( "Controllo" );
                    showQrCodeScanner();

                    return true;
                }

                if( itemId == R.id.emissioneTicketFragment )
                {
                    binding.emissioneTicketView.getRoot().setVisibility( View.VISIBLE );
                    binding.validazioneControlloTicketView.getRoot().setVisibility( View.GONE );

                    setTopbarTitle( "Emissione" );
                    hideQrCodeScanner();

                    return true;
                }

                return false;
            }
        } );

        setBottobarSelection( R.id.emissioneTicketFragment );

        // Gestione Menu
        binding.hamburgerMenu.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                showPopupMenu( view );
            }
        } );

        // Emissione
        setupSpinnerTipologiaDocumentoDiViaggio();
        setupListenerTipologiaDocumentoDiViaggio();
        setupListenerButtonsSelezioneZone();
        setupListenerSwitchAndataRitorno();
        setupListenerQuantita();
        setupListenerCheckboxTassaMI();
        setupListenerButtonEmissione();
        setupListenerButtonAggiungiCarrello();
        setupListenerGestisciCarrello();
        createDialog_GestioneCarrello();

        // Validazione/Controllo
        setupSpinnerZona();
        setupListenerSpinnerZona();
        setupQRCodeScanner();

        // DB Sync service
        startDBSyncService();

        // hideProgressBar();
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
                        // MainActivity.super.onBackPressed();

                        Intent logoutActivityIntent = new Intent( MainActivity.this, LogoutActivity.class );
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

                        Intent logoutActivityIntent = new Intent( MainActivity.this, LogoutActivity.class );
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

    public void setBottobarSelection( int id )
    {
        binding.bottomNavigationBar.setSelectedItemId( id );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupNfcReader()
    {
        // Inizializza l'NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter( this );

        if( nfcAdapter == null )
        {
            Toast.makeText( this, "NFC non supportato dal dispositivo", Toast.LENGTH_LONG ).show();
        }
        else if( !nfcAdapter.isEnabled() )
        {
            Toast.makeText( this, "Abilitare la lettura NFC dalle impostazioni del dispositivo", Toast.LENGTH_LONG ).show();
        }
        else
        {
            nfcPendingIntent = PendingIntent.getActivity( this,
                    0,
                    new Intent( this, getClass() )
                    .addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP ), PendingIntent.FLAG_UPDATE_CURRENT );

            intentFilters = new IntentFilter[]
            {
                new IntentFilter( NfcAdapter.ACTION_TAG_DISCOVERED ),
                new IntentFilter( NfcAdapter.ACTION_NDEF_DISCOVERED )
            };

        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void startNfcReading()
    {
        if( nfcAdapter != null && nfcAdapter.isEnabled() )
        {
            bNfcReadingActive = true;

            nfcAdapter.enableForegroundDispatch( this, nfcPendingIntent, intentFilters, null );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void stopNfcReading()
    {
        bNfcReadingActive = false;

        if( nfcAdapter != null && nfcAdapter.isEnabled() )
            nfcAdapter.disableForegroundDispatch( this );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Location getLastKnownGpsLocation()
    {
        return gpsLocation;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setTopbarTitle( String title )
    {
        binding.topBarTitleTextView.setText( title );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onNewIntent( @NonNull Intent intent )
    {
        super.onNewIntent( intent );

        String action = intent.getAction();

        if( action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED )
        {
            // passare la lettura al Fragment
            // TODO

            // TAG ID
            Tag tag = intent.getParcelableExtra( NfcAdapter.EXTRA_TAG );

            if( tag != null )
            {
                byte[] tagId = tag.getId();
                StringBuilder tagIdStr = new StringBuilder();

                for( byte b : tagId )
                {
                    tagIdStr.append( String.format( "%02x", b ) );
                }

                runOnUiThread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText( MainActivity.this, "NFC TAG ID: " + tagIdStr.toString(), Toast.LENGTH_LONG ).show();
                    }

                } );
            }

            // NDEF TAG
            if( action == NfcAdapter.ACTION_NDEF_DISCOVERED )
            {
                Parcelable[] ndefMessages = intent.getParcelableArrayExtra( NfcAdapter.EXTRA_NDEF_MESSAGES );

                if( ndefMessages != null && ndefMessages.length > 0 )
                {
                    NdefMessage ndefMessage = (NdefMessage) ndefMessages[ 0 ];
                    NdefRecord[] records = ndefMessage.getRecords();

                    for( NdefRecord record : records )
                    {
                        byte[] payload = record.getPayload();
                        String text = new String( payload, Charset.forName( "UTF-8" ) );

                        runOnUiThread( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText( MainActivity.this, "NFC DATA: " + text, Toast.LENGTH_LONG ).show();
                            }

                        } );
                    }
                }
            }

        }
        else if( action == Intent.ACTION_VIEW )
        {
            if( intent.getData() != null )
            {
                if( intent.getData().toString().startsWith( "nliticketapp://" ) )
                {
                    Uri uri = intent.getParcelableExtra( "uri" );

                    handleCB2Response( uri, true );
                }

            }
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showPopupMenu( View view )
    {
        PopupMenu popup = new PopupMenu( this, view );
        popup.getMenuInflater().inflate( R.menu.popup_menu, popup.getMenu() );

        popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick( MenuItem item )
            {
                int selection = item.getItemId();

                if( selection ==  R.id.menu_settings )
                {
                    showSettingsActivity();
                }
                else if( selection ==  R.id.menu_riepilogo )
                {
                    showDialog_Riepilogo();
                }
                else if( selection ==  R.id.menu_annulla_ultimo_ordine )
                {
                    gestisciAnnullamentoOrdine();
                }

                return false;
            }
        } );

        popup.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_Riepilogo()
    {
        riepilogoDlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        riepilogoDlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        riepilogoDlg.setContentView( R.layout.dialog_riepilogo );
        riepilogoDlg.setCancelable( false ); // BACK non chiude la dialog

        TextView dateTextView = riepilogoDlg.findViewById( R.id.dataRiepilogo_textView );

        SimpleDateFormat sdf = new SimpleDateFormat( "dd MMMM yyyy", Locale.getDefault() );
        String formattedDate = sdf.format( new Date() );

        dateTextView.setText( formattedDate );

        Button printButton = riepilogoDlg.findViewById( R.id.stampaRiepilogo_button );
        Button cancelButton = riepilogoDlg.findViewById( R.id.cancel_button );

        //---

        printButton.setOnClickListener( v ->
        {
            riepilogoDlg.dismiss();

            stampaRiepilogo();
        } );

        cancelButton.setOnClickListener( v ->
        {
            riepilogoDlg.dismiss();

        } );

        //---

        // All DB Calls must be done in a separate thread!
        new Thread( ()->
        {
            if( riepilogoDlg != null && riepilogoDlg.isShowing() )
            {
                PurchaseSummaryData summary = getDatiRiepilogo();

                runOnUiThread( () ->
                {
                    TextView unitIdTextView = riepilogoDlg.findViewById( R.id.unitId_TextView );
                    TextView turnoTextView = riepilogoDlg.findViewById( R.id.tag_TextView );
                    TextView dataturnoTextView = riepilogoDlg.findViewById( R.id.data_sessione_TextView );
                    TextView operatorIdTextView = riepilogoDlg.findViewById( R.id.loginId_TextView );
                    TextView emissioniTextView = riepilogoDlg.findViewById( R.id.emissioni_TextView );
                    TextView annullamentiTextView = riepilogoDlg.findViewById( R.id.annullamenti_TextView );
                    TextView pagamentiCashTextView = riepilogoDlg.findViewById( R.id.cash_TextView );
                    TextView pagamentiPosTextView = riepilogoDlg.findViewById( R.id.pos_TextView );

                    unitIdTextView.setText( String.valueOf( summary.unitID ) );
                    turnoTextView.setText( summary.turno );
                    dataturnoTextView.setText( summary.dataAperturaSessione );
                    operatorIdTextView.setText( summary.loginUserId );

                    emissioniTextView.setText( summary.regularIssuesCount + "x  " + application.convertCentesimiInEuro( summary.regularIssuesValue )  );
                    annullamentiTextView.setText( summary.voidedIssuesCount + "x  -" + application.convertCentesimiInEuro( summary.voidedIssuesValue )  );

                    pagamentiCashTextView.setText( summary.cashIssuesCount + "x  " + application.convertCentesimiInEuro( summary.cashIssuesValue )  );
                    pagamentiPosTextView.setText( summary.posIssuesCount + "x  " + application.convertCentesimiInEuro( summary.posIssuesValue )  );

                } );
            }


        } ).start();

        //---

        riepilogoDlg.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private PurchaseSummaryData getDatiRiepilogo()
    {
        PurchaseSummaryData summary = new PurchaseSummaryData();

        // Recupera sessione operatore corrente
        summary.unitID = application.getUnitId();
        summary.sessionId = (int) application.getCurrentSessionInfo().currentSession.id;
        summary.agencyId = application.getCurrentSessionInfo().currentSession.agencyId;
        summary.loginUserId = application.getCurrentSessionInfo().currentSession.loginUserId;
        summary.turno = application.getCurrentSessionInfo().currentSession.tag;

        String dataAperturaSessioneISO8601 = application.getCurrentSessionInfo().currentSession.tsOpen;
        String dataCorrenteISO8601 = application.toIso8601Local( ZonedDateTime.now() );

        ZonedDateTime dataAperturaSessione = application.fromIso8601ToLocal( dataAperturaSessioneISO8601 );
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm:ss", Locale.getDefault() );

        summary.dataAperturaSessione = dataAperturaSessione.format( dtf );

        // Recupera dati emissioni
        List<Issue> regularIssues = application.getIssuesTable().getRegularIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );
        List<Issue> regularIssuesCash = application.getIssuesTable().getCashRegularIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );
        List<Issue> regularIssuesPos = application.getIssuesTable().getPosRegularIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );
        List<Issue> voidedIssuesCash = application.getIssuesTable().getCashVoidedIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );
        List<Issue> voidedIssuesPos = application.getIssuesTable().getPosVoidedIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );
        List<Issue> voidedIssues = application.getIssuesTable().getvoidedIssuesInRange( summary.sessionId, dataAperturaSessioneISO8601, dataCorrenteISO8601 );

        summary.regularIssuesCount = 0;
        summary.regularIssuesValue = 0;

        for( Issue issue : regularIssues )
        {
            summary.regularIssuesCount += issue.quantity;
            summary.regularIssuesValue += issue.value * issue.quantity;
        }

        summary.voidedIssuesCount = 0;
        summary.voidedIssuesValue = 0;

        for( Issue issue : voidedIssues )
        {
            summary.voidedIssuesCount += issue.quantity;
            summary.voidedIssuesValue += Math.abs( issue.value ) * issue.quantity;
        }

        int cashRegularIssuesCount = 0;
        int cashRegularIssuesValue = 0;
        int cashVoidedIssuesCount = 0;
        int cashVoidedIssuesValue = 0;
        int posRegularIssuesCount = 0;
        int posRegularIssuesValue = 0;
        int posVoidedIssuesCount = 0;
        int posVoidedIssuesValue = 0;

        for( Issue issue : regularIssuesCash )
        {
            cashRegularIssuesCount += issue.quantity;
            cashRegularIssuesValue += issue.value * issue.quantity;
        }

        for( Issue issue : voidedIssuesCash )
        {
            cashVoidedIssuesCount += issue.quantity;
            cashVoidedIssuesValue += issue.value * issue.quantity;
        }

        for( Issue issue : regularIssuesPos )
        {
            posRegularIssuesCount += issue.quantity;
            posRegularIssuesValue += issue.value * issue.quantity;
        }

        for( Issue issue : voidedIssuesPos )
        {
            posVoidedIssuesCount += issue.quantity;
            posVoidedIssuesValue += issue.value * issue.quantity;
        }

        summary.cashIssuesCount = cashRegularIssuesCount - cashVoidedIssuesCount;
        summary.cashIssuesValue = cashRegularIssuesValue - cashVoidedIssuesValue;
        summary.posIssuesCount = posRegularIssuesCount - posVoidedIssuesCount;
        summary.posIssuesValue = posRegularIssuesValue - posVoidedIssuesValue;

        return summary;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void stampaRiepilogo()
    {
        new Thread( ()->
        {
            runOnUiThread( ()->
            {
                showProgressBar();
            } );

            try
            {
                PurchaseSummaryData summary = getDatiRiepilogo();

                SimpleDateFormat sdf = new SimpleDateFormat( "dd MMMM yyyy", Locale.getDefault() );
                String formattedDate = sdf.format( new Date() ).toUpperCase();

                // LOGO NLI
                Bundle imgSettings = new Bundle();
                application.getVectorPrinter().addImage( imgSettings, BitmapFactory.decodeResource( getResources(), R.drawable.logo_nli_black_300 ) );

                // P.IVA
                application.setTextToPrint( "P.IVA 03000970164\n", Alignment.CENTER, TextSize.NORMAL, true );

                // TITOLO
                Bitmap separatorBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.line_separator );
                application.getVectorPrinter().addImage( imgSettings, separatorBitmap );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.NORMAL, false );
                application.setTextToPrint( "RIEPILOGO AL " + formattedDate, Alignment.CENTER, TextSize.NORMAL, true );
                application.getVectorPrinter().addImage( imgSettings, separatorBitmap );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.TINY, false );

                application.setTextToPrint( "ID Dispositivo\n" + String.valueOf( summary.unitID ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Turno\n" + summary.turno, Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Data inizio turno\n" + summary.dataAperturaSessione, Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "ID Operatore\n" + summary.loginUserId, Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "ID Agenzia\n" + summary.agencyId, Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Emissioni\n" + summary.regularIssuesCount + "x  " + application.convertCentesimiInEuro( summary.regularIssuesValue ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Annullamenti\n" + summary.voidedIssuesCount + "x -" + application.convertCentesimiInEuro( summary.voidedIssuesValue ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Pagamenti contanti\n" + summary.cashIssuesCount + "x  " + application.convertCentesimiInEuro( summary.cashIssuesValue ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                application.setTextToPrint( "Pagamenti POS\n" + summary.posIssuesCount + "x  " + application.convertCentesimiInEuro( summary.posIssuesValue ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.NORMAL, TextSize.TINY, false );

                /*
                application.setTextToPrint( application.getPaddedText( "ID Dispositivo", String.valueOf( summary.unitID ), LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( application.getPaddedText( "Turno", summary.turno, LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( application.getPaddedText( "Data inizio turno", summary.dataAperturaSessione, LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( application.getPaddedText( "ID Operatore", summary.loginUserId, LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.SMALL, false );

                application.setTextToPrint( application.getPaddedText( "Emissioni", summary.regularIssuesCount + "x  " + application.convertCentesimiInEuro( summary.regularIssuesValue ), LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( application.getPaddedText( "Annullamenti", summary.voidedIssuesCount + "x  " + application.convertCentesimiInEuro( summary.voidedIssuesValue ), LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.SMALL, false );

                application.setTextToPrint( application.getPaddedText( "Pagamenti contanti", summary.cashRegularIssuesCount + "x  " + application.convertCentesimiInEuro( summary.cashRegularIssuesValue ), LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( application.getPaddedText( "Pagamenti POS", summary.posRegularIssuesCount + "x -" + application.convertCentesimiInEuro( summary.posRegularIssuesValue ), LINE_WIDTH_NRM ), Alignment.NORMAL, TextSize.NORMAL, true );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.SMALL, false );
                */

                application.getVectorPrinter().startPrint( new OnPrintListener.Stub()
                {
                    @Override
                    public void onFinish() throws RemoteException
                    {

                    }

                    @Override
                    public void onStart() throws RemoteException
                    {

                    }

                    @Override
                    public void onError( int i, String s ) throws RemoteException
                    {

                    }

                } );


            }
            catch( Exception e )
            {

            }

            runOnUiThread( ()->
            {
                hideProgressBar();
            } );


        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void gestisciAnnullamentoOrdine()
    {
        // All DB Calls must be done in a separate thread!
        new Thread( ()->
        {
            try
            {
                Payment lastPayment = application.getPaymentsTable().getLastPayment();

                if( lastPayment != null )
                {
                    int value = lastPayment.value;

                    if( lastPayment.type.equals( "R" ) )
                    {
                        runOnUiThread( () ->
                        {
                            showSnackbarMessage( "Ultimo ordine già annullato" );
                        } );

                        return;
                    }

                    if( lastPayment.method.equals( "pos" ) )
                    {
                        // Richiesta Storno ultimo pagamento
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

                        PaymentResponse paymentResponse = objectMapper.readValue( lastPayment.details, PaymentResponse.class );

                        if( paymentResponse != null )
                        {
                            request_VOID( paymentResponse.stan );
                        }

                    }
                    else if( lastPayment.method.equals( "cash" ) )
                    {
                        if( tipoCarrello == CARRELLO_EMISSIONE_SINGOLA )
                        {
                            registraAnnullamento( "cash", lastPayment.orderUuid, null );
                        }
                        else if( tipoCarrello == CARRELLO_NORMALE )
                        {
                            registraAnnullamento( "cash", lastPayment.orderUuid, null );
                        }
                    }

                }
                else
                {
                    runOnUiThread( () ->
                    {
                        showSnackbarMessage( "Nessun ordine da annullare" );
                    } );
                }
            }
            catch( Exception e )
            {
                Log.e( TAG, e.getMessage() );
            }

        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void registraAnnullamento( String type, String orderUUID, String reason )
    {
        // WARNING: mettere in pausa il DB-Sync in bkgnd durante le operazioni di emissione/annullamento
        pauseDBSyncService();
        Log.d( TAG, "pauseDBSyncService() on registraAnnullamento()");

        // Annulla pagamanento del carrello attuale
        Payment prevPayment = application.getPaymentsTable().getPaymentByUUID( orderUUID );

        if( prevPayment != null )
        {
            // Crea nuovo record
            Payment newPayment = new Payment( prevPayment );

            newPayment.id = 0;
            newPayment.type = "R";
            newPayment.value *= -1.0;
            newPayment.ts = application.toIso8601Local( ZonedDateTime.now() );
            newPayment.details = ( reason != null && reason.isBlank() ) ? null : reason;

            application.getPaymentsTable().insert( newPayment );

            // Annulla tutte le relative emissioni
            List<Issue> prevIssues = application.getIssuesTable().getIssuesByOrderUUID( orderUUID );
            List<Issue> newIssues = new ArrayList<>();

            for( Issue prevIssue : prevIssues )
            {
                // Crea nuovo record
                Issue newIssue = new Issue( prevIssue );

                newIssue.id = 0;
                newIssue.parentUuid = prevIssue.uuid; // UUID della Issue che si va ad annullare
                newIssue.uuid = UUID.randomUUID().toString();
                newIssue.type = "V";
                newIssue.value *= -1.0;
                newIssue.ts = newPayment.ts;
                newIssue.paymentId = newPayment.id;

                newIssues.add( newIssue );
            }

            if( !newIssues.isEmpty() )
                application.getIssuesTable().insert( newIssues );

            if( tipoCarrello == CARRELLO_NORMALE )
            {
                application.clearCarrelloAcquisti();
            }
            else if( tipoCarrello == CARRELLO_EMISSIONE_SINGOLA )
            {

            }

            runOnUiThread( () ->
            {
                hideProgressBar();
                updateCarrelloButtonStatus();

                showDialog_Message( "Ordine annullato" );
            } );

        }

        // WARNING: Ripristina il DB-Sync in bkgnd dopo le operazioni di emissione/annullamento (indipendemente dall'esito)
        resumeDBSyncService();
        Log.d( TAG, "startDBSyncService() on registraAnnullamento()");
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void createDialog_GestioneCarrello()
    {
        carrelloDlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        carrelloDlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        carrelloDlg.setContentView( R.layout.dialog_gestione_carrello );
        carrelloDlg.setCancelable( false ); // BACK non chiude la dialog
        carrelloDlg.hide();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showSettingsActivity()
    {
        Intent settingsActivityIntent = new Intent( MainActivity.this, SettingsActivity.class );
        startActivity( settingsActivityIntent );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void showProgressBar()
    {
        binding.progressBar.setVisibility( View.VISIBLE );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void hideProgressBar()
    {
        binding.progressBar.setVisibility( View.GONE );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private ActivityResultLauncher<Intent> APILauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
            activityResult->
            {
                Intent data = activityResult.getData();

                if( data == null || data.getParcelableExtra( "uri" ) == null )
                    return;

                Uri uri = data.getParcelableExtra( "uri" );

                handleCB2Response( uri, false );

            } );

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_CHECK_POS_STATUS()
    {
        try
        {
            CB2Command = "CHECK_POS_STATUS";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +
                             "op=" + CB2Command +
                             "&callerPackage=" + application.getPackageName() +
                             "&enablerKey=1234567890" +
                             "&uuid=" + CB2CommandUUID;


            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            // TEST
            ComponentName activityAvailable = intent.resolveActivity( getPackageManager() );

            APILauncher.launch( intent );
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_INIT()
    {
        try
        {
            CB2Command = "DLL_FIRST";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +
                             "op=" + CB2Command +
                             "&callerPackage=" + application.getPackageName() +
                             "&responseUri=nliticketapp://result" +
                             "&enablerKey=1234567890" +
                             "&printerMode=1" + // 0 stampa automatica dal POS / 1 invio al caller / 2 stampa automatica ed invio al caller
                             "&uuid=" + CB2CommandUUID;

            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            ComponentName activityAvailable = intent.resolveActivity( getPackageManager() );

            APILauncher.launch( intent );
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_PURCHASE( int amount )
    {
        try
        {
            CB2Command = "PAYMENT";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +                             // Dominio e comando
                             "op=" + CB2Command +                               // Tipo di operazione richiesta
                             "&amount=" + amount +                              // Importo della transazione
                             "&cardType=0" +                                    // Tipo di carta (0=autodetect)
                             "&printerMode=2" +
                             "&enablerKey=1234567890" +                         // Chiave di licenza fornita da Ingenico (non attivo, usare una stringa qualsiasi)
                             "&responseUri=nliticketapp://result" +             // URI per ricevere la risposta
                             "&uuid=" + CB2CommandUUID +                        // Identificatore univoco
                             "&callerPackage=" + application.getPackageName();  // Nome del package dell'app chiamante

            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            startActivity( intent ); // USING DEEP LINK
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_REFUND( int amount )
    {
        try
        {
            CB2Command = "REFUND";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +                    // Dominio e comando
                    "op=" + CB2Command +                               // Tipo di operazione richiesta
                    "&amount=" + amount +                              // Importo della transazione
                    "&cardType=0" +                                    // Tipo di carta (0=autodetect)
                    "&printerMode=2" +
                    "&enablerKey=1234567890" +                         // Chiave di licenza fornita da Ingenico (non attivo, usare una stringa qualsiasi)
                    "&responseUri=nliticketapp://result" +             // URI per ricevere la risposta
                    "&uuid=" + CB2CommandUUID +                        // Identificatore univoco
                    "&callerPackage=" + application.getPackageName();  // Nome del package dell'app chiamante

            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            startActivity( intent ); // USING DEEP LINK
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_VOID( String stan )
    {
        try
        {
            CB2Command = "VOID";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +                             // Dominio e comando
                             "op=" + CB2Command +                               // Tipo di operazione richiesta
                             "&stan=" + stan +                                   // STAN
                             "&printerMode=2" +
                             "&enablerKey=1234567890" +                         // Chiave di licenza fornita da Ingenico (non attivo, usare una stringa qualsiasi)
                             "&responseUri=nliticketapp://result" +             // URI per ricevere la risposta
                             "&uuid=" + CB2CommandUUID +                        // Identificatore univoco
                             "&callerPackage=" + application.getPackageName();  // Nome del package dell'app chiamante

            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            startActivity( intent ); // USING DEEP LINK
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void request_RETROACTIVE_VOID( String acquirerId,
                                          String stan,
                                          String authCode,
                                          int amount,
                                          int voidOperation,
                                          String cardRef,
                                          String dataSpec,
                                          String transDate )
    {
        try
        {
            CB2Command = "VOID_RETROACTIVE";
            CB2CommandUUID = UUID.randomUUID().toString();

            String request = "cb2inge://execute?" +                             // Dominio e comando
                             "op=" + CB2Command +                               // Tipo di operazione richiesta
                             "&amount=" + amount +                              // Importo della transazione

                             "&printerMode=2" +
                             "&enablerKey=1234567890" +                         // Chiave di licenza fornita da Ingenico (non attivo, usare una stringa qualsiasi)
                             "&responseUri=nliticketapp://result" +             // URI per ricevere la risposta
                             "&uuid=" + CB2CommandUUID +                        // Identificatore univoco
                             "&callerPackage=" + application.getPackageName();  // Nome del package dell'app chiamante

            Uri uri = Uri.parse( request );
            Intent intent = new Intent( Intent.ACTION_VIEW, uri );

            startActivity( intent ); // USING DEEP LINK
        }
        catch( Exception e )
        {
            showSnackbarMessage( "PAGAMENTO ELETTRONICO NON DISPONIBILE" );
            Log.e( TAG , e.getMessage() );
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void handleCB2Response( Uri uri, boolean deepLink )
    {
        if( uri != null )
        {
            try
            {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor( uri, "r" );

                if( pfd != null )
                {
                    FileDescriptor fd = pfd.getFileDescriptor();

                    if( fd != null )
                    {
                        FileInputStream fileInputStream = new FileInputStream( fd );
                        long size = fileInputStream.available();

                        if( size > 0 )
                        {
                            byte[] data = new byte[(int)size];
                            fileInputStream.read( data );
                            String jsonData = new String( data );

                            if( jsonData != null && !jsonData.isBlank() )
                            {
                                // Convert JSON To response class
                                ObjectMapper objectMapper = new ObjectMapper();
                                objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

                                // TERMINAL STATUS
                                TerminalStatusResponse statusResponse = objectMapper.readValue( jsonData, TerminalStatusResponse.class );

                                if( statusResponse != null && !deepLink && CB2Command.equals( "CHECK_POS_STATUS" ) )
                                {
                                    CB2Command = "";
                                    CB2CommandUUID = "";

                                    if( statusResponse.result.equals( "RESULT_OK" ) )
                                    {

                                    }
                                    else if( statusResponse.result.equals( "ERROR" ) )
                                    {

                                    }
                                }

                                // PAYMENT
                                PaymentResponse paymentResponse = objectMapper.readValue( jsonData, PaymentResponse.class );

                                if( paymentResponse != null && deepLink && CB2Command.equals( "PAYMENT" ) )
                                {
                                    CB2Command = "";
                                    CB2CommandUUID = "";

                                    if( paymentResponse.result.equals( "RESULT_OK" ) )
                                    {
                                        // Transazione andata a buon fine, registrare il pagamento
                                        //byte[] decodedBytes = Base64.decode( paymentResponse.receipt, Base64.DEFAULT );
                                        //String decodedReceipt = new String( decodedBytes, StandardCharsets.UTF_8 );

                                        if( tipoCarrello == CARRELLO_EMISSIONE_SINGOLA )
                                        {
                                            registraPagamento( "pos", application.getCarrelloAcquistiOneItem(), jsonData /*decodedReceipt*/ ); // Su indicazioni di TecBus salviamo l'intera response
                                        }
                                        else if( tipoCarrello == CARRELLO_NORMALE )
                                        {
                                            registraPagamento( "pos", application.getCarrelloAcquisti(), jsonData /*decodedReceipt*/ );
                                        }

                                    }
                                    else if( paymentResponse.result.equals( "BUSY" ) )
                                    {
                                        runOnUiThread( () ->
                                        {
                                            showSnackbarMessage( "Applicazione di pagamento OCCUPATA" );
                                        } );
                                    }
                                    else if( paymentResponse.result.equals( "ERROR" ) )
                                    {
                                        // Transazione fallita
                                        handleFailedPayment( paymentResponse );
                                    }
                                }

                                // VOID
                                PaymentResponse voidResponse = objectMapper.readValue( jsonData, PaymentResponse.class );

                                if( voidResponse != null && deepLink && CB2Command.equals( "VOID" ) )
                                {
                                    CB2Command = "";
                                    CB2CommandUUID = "";

                                    if( voidResponse.result.equals( "RESULT_OK" ) )
                                    {
                                        new Thread( ()->
                                        {
                                            // Transazione andata a buon fine, registrare lo storno
                                            if( tipoCarrello == CARRELLO_EMISSIONE_SINGOLA )
                                            {
                                                registraAnnullamento( "pos", application.getCarrelloAcquistiOneItem().orderUUID, jsonData ); // Su indicazioni di TecBus salviamo l'intera response
                                            }
                                            else if( tipoCarrello == CARRELLO_NORMALE )
                                            {
                                                registraAnnullamento( "pos", application.getCarrelloAcquisti().orderUUID, jsonData );
                                            }

                                        } ).start();

                                    }
                                    else if( paymentResponse.result.equals( "BUSY" ) )
                                    {
                                        runOnUiThread( () ->
                                        {
                                            showSnackbarMessage( "Applicazione di pagamento OCCUPATA" );
                                        } );
                                    }
                                    else if( paymentResponse.result.equals( "ERROR" ) )
                                    {
                                        // Transazione fallita
                                        handleFailedPayment( paymentResponse );
                                    }
                                }
                            }

                        }
                        fileInputStream.close();
                    }
                    else
                    {
                        pfd.close();
                    }
                }

            }
            catch( Exception err )
            {
                Log.e( TAG, err.getMessage() );
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void showSnackbarMessage( String msg )
    {
        Snackbar snackbar = Snackbar.make( binding.getRoot(), msg, Snackbar.LENGTH_LONG );
        snackbar.setBackgroundTint( Color.YELLOW );
        snackbar.setTextColor( Color.BLACK );
        snackbar.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupSpinnerTipologiaDocumentoDiViaggio()
    {
        // All DB Calls must be done in a separate thread!
        new Thread( ()->
        {
            List<Fee> listaTipologie = new ArrayList<>();

            listaTipologie = application.getTipologiePrincipali();

            // Update UI
            List<Fee> finalListaTipologie = listaTipologie;

            runOnUiThread( () ->
            {
                FeeAdapter adapter = new FeeAdapter( this, finalListaTipologie );

                binding.emissioneTicketView.listaTipoDocViaggioSpinner.setEnabled( true );
                binding.emissioneTicketView.listaTipoDocViaggioSpinner.setAdapter( adapter );

            } );

        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerTipologiaDocumentoDiViaggio()
    {
        binding.emissioneTicketView.listaTipoDocViaggioSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                showProgressBar();
                cleanupEmissioneTicketUI();

                // All DB Calls must be done in a separate thread!
                new Thread( ()->
                {
                    // Cleanup valori precedenti
                    tipologiaSelezionata = null;
                    tariffaTipologiaSelezionata = null;
                    tipologieAccessorie.clear();
                    tariffeAccessorie.clear();
                    zonaPartenza = 0;
                    zonaArrivo = 0;
                    zonaPartenzaDescr = "";
                    zonaArrivoDescr = "";

                    int idZonaDefault = 0;
                    Zone zonaDefault = application.getZonesTable().getZoneById( application.getCurrentSessionInfo().zonaDefaultId );

                    if( zonaDefault != null )
                        idZonaDefault = zonaDefault.id;

                    updateDatiTariffazione( (Fee)binding.emissioneTicketView.listaTipoDocViaggioSpinner.getSelectedItem(), idZonaDefault, idZonaDefault );

                } ).start();
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent )
            {
                tipologiaSelezionata = null;
            }

        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateDatiTariffazione( Fee tipologia, int zonaPartenza, int zonaArrivo )
    {
        if( tipologia != null )
        {
            // Aggiorna tipologia di documento di viaggio corrente
            tipologiaSelezionata = new Fee( tipologia );

            // Se attivata e disponibile recupera ed utilizza la tipologia A/R al posto di quella normale
            if( binding.emissioneTicketView.andataRitornoSwitch.isChecked() && tipologiaSelezionata.arFeeId != null )
            {
                Fee tipologiaAR = application.getFeesTable().getFeeById( tipologiaSelezionata.arFeeId );

                // Aggiorna tariffa principale (lo so cosa state pensando! ma non c'è altro modo di farlo attualmente)
                if( tipologiaAR != null )
                {
                    tipologiaSelezionata = tipologiaAR;
                }
            }

            this.zonaPartenza = zonaPartenza;
            this.zonaArrivo = zonaArrivo;

            if( !tipologiaSelezionata.hasZones )
            {
                this.zonaPartenza = 0;
                this.zonaArrivo = 0;
            }
            else if( this.zonaPartenza == 0 && this.zonaArrivo == 0 )
            {
                int idZonaDefault = 0;
                Zone zonaDefault = application.getZonesTable().getZoneById( application.getCurrentSessionInfo().zonaDefaultId );

                if( zonaDefault != null )
                    idZonaDefault = zonaDefault.id;

                this.zonaPartenza = idZonaDefault;
                this.zonaArrivo = idZonaDefault;
            }

            tariffaTipologiaSelezionata = getRegolaTariffaria( tipologiaSelezionata.id, this.zonaPartenza, this.zonaArrivo );

            Zone startZone = application.getZonesTable().getZoneById( this.zonaPartenza );
            Zone EndZone = application.getZonesTable().getZoneById( this.zonaArrivo );

            boolean usaTassaMI = false;

            zonaPartenzaDescr = "";
            zonaArrivoDescr = "";

            if( startZone != null )
            {
                zonaPartenzaDescr = startZone.name;

                if( startZone.tags != null )
                    usaTassaMI = startZone.tags.contains( "tax_fee" );
            }

            if( EndZone != null )
            {
                zonaArrivoDescr = EndZone.name;

                if( EndZone.tags != null && !usaTassaMI )
                    usaTassaMI = EndZone.tags.contains( "tax_fee" );
            }

            // Recupera le tipologie/tariffe accessorie
            tipologieAccessorie.clear();
            tariffeAccessorie.clear();

            if( tipologiaSelezionata.childFees != null )
            {
                tipologieAccessorie = application.getFeesTable().getFeesByIdList( tipologiaSelezionata.childFees );

                tariffeAccessorie.clear();

                List<Fare> tmpList = new ArrayList<>();

                for( Fee tipologiaAcc : tipologieAccessorie )
                {
                    Fare tariffaAcc = application.getFaresTable().getFareByFeeId( tipologiaAcc.id );
                    tmpList.add( tariffaAcc );
                }

                for( Fare fare : tmpList )
                {
                    if( fare.fromZoneId == null && fare.toZoneId == null )
                    {
                        tariffeAccessorie.add( fare );
                    }
                    else if( fare.fromZoneId >= this.zonaPartenza && fare.toZoneId <= this.zonaArrivo )
                    {
                        tariffeAccessorie.add( fare );
                    }
                }

                if( tipologieAccessorie.size() != tariffeAccessorie.size() )
                {
                    showSnackbarMessage( "ERRORE NEL RECUPERO TARIFFE ACCESSORIE!" );

                    tipologiaSelezionata.childFees = null;
                    tipologieAccessorie.clear();
                    tariffeAccessorie.clear();
                }

            }

            // Se presente recupera la tipologia Tassa MI (tassa valida se partenza o arrivo su zona C) e la aggiunge alla tariffe accessorie
            // NOTA: La tassa MI potrebe essere ridondata nelle tariffe accessorie (problema di TecBus), nel caso, sostituirla
            boolean giornaliero = tipologiaSelezionata.description.equalsIgnoreCase( "GIORNALIERO" ); // Forzatura

            if( tipologiaSelezionata.taxFeeId != null && ( usaTassaMI || giornaliero ) )
            {
                Fee tipologiaTassaMI = application.getFeesTable().getFeeById( tipologiaSelezionata.taxFeeId );

                if( tipologiaTassaMI != null )
                {
                    AddOrReplaceTipologiaTassaMI( tipologiaTassaMI );
                    AddOrReplaceTariffaTassaMI( application.getFaresTable().getFareByFeeId( tipologiaTassaMI.id ) );
                }
            }

        }

        // Crea sempre un carrello con la singola tariffa
        aggiornaSingolaEmissione();

        // Update UI
        runOnUiThread( () ->
        {
            updateEmissioneTicketUI();
            calcolaImportoTotale();
            hideProgressBar();
        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Fare getRegolaTariffaria( int feeId, int fromZone, int toZone )
    {
        Fare result = null;

        if( fromZone > 0 && toZone > 0 )
        {
            result = application.getFaresTable().getFareByFeeIdAndZone( feeId, fromZone, toZone );
        }
        else
        {
            result = application.getFaresTable().getFareByFeeId( feeId );
        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void AddOrReplaceTipologiaTassaMI( Fee tipologiaTassaMI )
    {
        if( tipologiaTassaMI == null )
            return;

        for( int i = 0; i < tipologieAccessorie.size(); i++ )
        {
            if( tipologieAccessorie.get( i ).id == tipologiaTassaMI.id )
            {
                tipologieAccessorie.set( i, tipologiaTassaMI );
                return;
            }
        }

        tipologieAccessorie.add( tipologiaTassaMI );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void AddOrReplaceTariffaTassaMI( Fare tariffaTassaMI )
    {
        if( tariffaTassaMI == null )
            return;

        for( int i = 0; i < tariffeAccessorie.size(); i++ )
        {
            if( tariffeAccessorie.get( i ).id == tariffaTassaMI.id )
            {
                tariffeAccessorie.set( i, tariffaTassaMI );
                return;
            }
        }

        tariffeAccessorie.add( tariffaTassaMI );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void aggiornaSingolaEmissione()
    {
        // Crea un nuovo carrello 'one item' con i dati correnti
        application.clearCarrelloAcquistiOneItem();
        aggiungiDatiCorrentiAlCarrello( application.getCarrelloAcquistiOneItem() );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void aggiungiDatiCorrentiAlCarrello( ShoppingCart carrelloAcquisti )
    {
        if( carrelloAcquisti != null && tipologiaSelezionata != null && tariffaTipologiaSelezionata != null )
        {
            ShoppingCartItem newCartItem = new ShoppingCartItem();
            int quantity = binding.emissioneTicketView.quantitaLayout.getValue();

            // Principale
            newCartItem.mainIssue = new ShoppingCartItem.IssueLight();
            newCartItem.mainIssue.feeId = tipologiaSelezionata.id;
            newCartItem.mainIssue.feeDescription = tipologiaSelezionata.description;
            newCartItem.mainIssue.tripsCount = tariffaTipologiaSelezionata.tripsCount;
            newCartItem.mainIssue.minutes = tariffaTipologiaSelezionata.minutes;
            newCartItem.mainIssue.quantity = quantity;
            newCartItem.mainIssue.fromZoneId = zonaPartenza;
            newCartItem.mainIssue.toZoneId = zonaArrivo;
            newCartItem.mainIssue.fromZoneLabel = zonaPartenzaDescr;
            newCartItem.mainIssue.toZoneLabel = zonaArrivoDescr;
            newCartItem.mainIssue.value = tariffaTipologiaSelezionata.value;
            newCartItem.mainIssue.outMedia = tipologiaSelezionata.outMedia;
            newCartItem.mainIssue.autoValidation = tipologiaSelezionata.validatableOnIssue;

            // Tariffa MI (Accessoria)
            if( binding.emissioneTicketView.tassaSbarcoCheckBox.isChecked() )
            {
                int[] indiceTassaMI = IntStream.range( 0, tipologieAccessorie.size() )
                        .filter( idx -> tipologieAccessorie.get( idx ).description.equals( "Tassa MI" ) )
                        .toArray();

                if( indiceTassaMI != null && indiceTassaMI.length > 0 )
                {
                    Fee tipologiaTassaMI = tipologieAccessorie.get( indiceTassaMI[ 0 ] );
                    Fare tariffaTassaMI = tariffeAccessorie.get( indiceTassaMI[ 0 ] );

                    ShoppingCartItem.IssueLight newChildIssue = new ShoppingCartItem.IssueLight();
                    newChildIssue.feeId = tipologiaTassaMI.id;
                    newChildIssue.feeDescription = tipologiaTassaMI.description;
                    newChildIssue.quantity = 1;
                    newChildIssue.value = tariffaTassaMI.value;
                    newChildIssue.fromZoneId = zonaPartenza;
                    newChildIssue.toZoneId = zonaArrivo;
                    newChildIssue.fromZoneLabel = zonaPartenzaDescr;
                    newChildIssue.toZoneLabel = zonaArrivoDescr;

                    newCartItem.childrenIssues.add( newChildIssue );
                }

            }

            // Altre Tariffe Accessorie
            if( !tipologieAccessorie.isEmpty() && !tariffeAccessorie.isEmpty() )
            {
                for( int k = 0; k < binding.emissioneTicketView.tariffeAccesorieLayout.getChildCount(); k++ )
                {
                    QuantitativoTitoloViaggioView control = (QuantitativoTitoloViaggioView) binding.emissioneTicketView.tariffeAccesorieLayout.getChildAt( k );

                    if( control == null || control.getValue() == 0 )
                        continue;

                    Fee tipologiaAccessoria = tipologieAccessorie.get( k );
                    Fare tariffatipologiaAccessoria = tariffeAccessorie.get( k );

                    ShoppingCartItem.IssueLight newChildIssue = new ShoppingCartItem.IssueLight();
                    newChildIssue.feeId = tipologiaAccessoria.id;
                    newChildIssue.feeDescription = tipologiaAccessoria.description;
                    newChildIssue.quantity = control.getValue();
                    newChildIssue.value = tariffatipologiaAccessoria.value;
                    newChildIssue.fromZoneId = zonaPartenza;
                    newChildIssue.toZoneId = zonaArrivo;
                    newChildIssue.fromZoneLabel = zonaPartenzaDescr;
                    newChildIssue.toZoneLabel = zonaArrivoDescr;

                    newCartItem.childrenIssues.add( newChildIssue );
                }
            }

            carrelloAcquisti.addItem( newCartItem );
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void cleanupEmissioneTicketUI()
    {
        binding.emissioneTicketView.andataRitornoSwitch.setVisibility( View.VISIBLE );
        binding.emissioneTicketView.andataRitornoSwitch.setChecked( true );
        binding.emissioneTicketView.andataRitornoTextView.setText( "SI" );
        binding.emissioneTicketView.quantitaLayout.setValue( 1 );
        binding.emissioneTicketView.quantitaLayout.setMinValue( 1 );
        binding.emissioneTicketView.tassaSbarcoCheckBox.setChecked( false );
        binding.emissioneTicketView.tariffeAccesorieLayout.removeAllViews();
        binding.emissioneTicketView.zonaAButton.setChecked( false );
        binding.emissioneTicketView.zonaBButton.setChecked( false );
        binding.emissioneTicketView.zonaCButton.setChecked( false );
        binding.emissioneTicketView.zonaDButton.setChecked( false );
        binding.emissioneTicketView.zonaEButton.setChecked( false );
        binding.emissioneTicketView.zonaTutteButton.setChecked( false );
        binding.emissioneTicketView.zonaAButton.setBackgroundResource( R.drawable.button_rounded_left_top_disabled );
        binding.emissioneTicketView.zonaBButton.setBackgroundResource( R.drawable.button_squared_disabled );
        binding.emissioneTicketView.zonaCButton.setBackgroundResource( R.drawable.button_rounded_right_top_disabled );
        binding.emissioneTicketView.zonaDButton.setBackgroundResource( R.drawable.button_rounded_left_bottom_disabled );
        binding.emissioneTicketView.zonaEButton.setBackgroundResource( R.drawable.button_squared_disabled );
        binding.emissioneTicketView.zonaTutteButton.setBackgroundResource( R.drawable.button_rounded_right_bottom_enabled );
        binding.emissioneTicketView.totaleTextView.setText( "" );
        binding.emissioneTicketView.emissioneButton.setEnabled( true );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateEmissioneTicketUI()
    {
        if( tipologiaSelezionata == null || tariffaTipologiaSelezionata == null )
        {
            Log.e( TAG, "tipologia o tariffa non valorizzate!" );
            return;
        }

        // Switch A/R
        boolean hasOneWayFee = tipologiaSelezionata.onewayFeeId != null;
        boolean hasARFee = tipologiaSelezionata.arFeeId != null;

        if( hasOneWayFee || hasARFee )
        {
            binding.emissioneTicketView.andataRitornoLayout.setVisibility( View.VISIBLE );
        }
        else
        {
            binding.emissioneTicketView.andataRitornoLayout.setVisibility( View.GONE );
        }

        //---

        // Zone
        binding.emissioneTicketView.zonaAButton.setBackgroundResource( R.drawable.button_rounded_left_top_disabled );
        binding.emissioneTicketView.zonaBButton.setBackgroundResource( R.drawable.button_squared_disabled );
        binding.emissioneTicketView.zonaCButton.setBackgroundResource( R.drawable.button_rounded_right_top_disabled );
        binding.emissioneTicketView.zonaDButton.setBackgroundResource( R.drawable.button_rounded_left_bottom_disabled );
        binding.emissioneTicketView.zonaEButton.setBackgroundResource( R.drawable.button_squared_disabled );
        binding.emissioneTicketView.zonaTutteButton.setBackgroundResource( R.drawable.button_rounded_right_bottom_enabled );

        if( tariffaTipologiaSelezionata.fromZoneId != null && tariffaTipologiaSelezionata.fromZoneId > 0 && tariffaTipologiaSelezionata.toZoneId != null && tariffaTipologiaSelezionata.toZoneId > 0 )
        {
            binding.emissioneTicketView.zoneGridLayout.setVisibility( View.VISIBLE );

            for( int i = tariffaTipologiaSelezionata.fromZoneId; i <= tariffaTipologiaSelezionata.toZoneId; i++ )
            {
                switch( i )
                {
                    case ZONA_A:
                        binding.emissioneTicketView.zonaAButton.setBackgroundResource( R.drawable.button_rounded_left_top_enabled );
                        break;

                    case ZONA_B:
                        binding.emissioneTicketView.zonaBButton.setBackgroundResource( R.drawable.button_squared_enabled );
                        break;

                    case ZONA_C:
                        binding.emissioneTicketView.zonaCButton.setBackgroundResource( R.drawable.button_rounded_right_top_enabled );
                        break;

                    case ZONA_D:
                        binding.emissioneTicketView.zonaDButton.setBackgroundResource( R.drawable.button_rounded_left_bottom_enabled );
                        break;

                    case ZONA_E:
                        binding.emissioneTicketView.zonaEButton.setBackgroundResource( R.drawable.button_squared_enabled );
                        break;
                }

            }

        }
        else
        {
            binding.emissioneTicketView.zoneGridLayout.setVisibility( View.GONE );
        }

        //---

        // Tassa MI
        boolean hasMITax =  tipologieAccessorie.stream().anyMatch(fee -> fee.description.equals( "Tassa MI" ) );

        if( hasMITax )
        {
            binding.emissioneTicketView.tassaSbarcoCheckBox.setVisibility( View.VISIBLE );

            if( ( tariffaTipologiaSelezionata.fromZoneId != null && tariffaTipologiaSelezionata.fromZoneId == ZONA_C ) ||
                ( tariffaTipologiaSelezionata.toZoneId != null && tariffaTipologiaSelezionata.toZoneId == ZONA_C ) )
            {
                binding.emissioneTicketView.tassaSbarcoCheckBox.setChecked( true );
            }
            else
            {
                binding.emissioneTicketView.tassaSbarcoCheckBox.setChecked( false );
            }
        }
        else
        {
            binding.emissioneTicketView.tassaSbarcoCheckBox.setVisibility( View.GONE );
        }

        //---

        // Tariffe Accessorie
        ShoppingCartItem currSavedItem = application.getCarrelloAcquistiOneItem().getItemAt( 0 );

        binding.emissioneTicketView.tariffeAccesorieLayout.setLayoutTransition( null );
        binding.emissioneTicketView.tariffeAccesorieLayout.removeAllViews();

        for( int i = 0; i < tipologieAccessorie.size(); i++ )
        {
            Fee tipologiaAcc = tipologieAccessorie.get( i );

            // Rimuove contatore Tassa MI. Usare la checkbox 'tassa di sbarco'
            if( tipologiaAcc.description.equalsIgnoreCase( "Tassa MI" ) )
                continue;

            QuantitativoTitoloViaggioView view = new QuantitativoTitoloViaggioView( binding.getRoot().getContext() );

            view.setLabel( tipologiaAcc.description );

            // Ripristino quantitativo precedentemente impostato (lo so, fa schifo sto codice...)
            if( currSavedItem != null && !currSavedItem.childrenIssues.isEmpty() )
            {
                for( ShoppingCartItem.IssueLight child : currSavedItem.childrenIssues )
                {
                    if( child.feeId == tipologiaAcc.id )
                    {
                        view.setValue( child.quantity );
                        break;
                    }
                }
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
            params.setMargins( 24, 12, 24, 0 );

            view.setLayoutParams( params );

            setupListenerQuantitativoTariffeAccessorie( view );

            binding.emissioneTicketView.tariffeAccesorieLayout.addView( view );
        }

        binding.emissioneTicketView.tariffeAccesorieLayout.setLayoutTransition( new LayoutTransition() );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerQuantitativoTariffeAccessorie( QuantitativoTitoloViaggioView view )
    {
        view.setOnValueChangedListener( new QuantitySelectorView.OnValueChangedListener()
        {
            @Override
            public void onValueChanged( View view, int newValue, Object extraData )
            {
                aggiornaSingolaEmissione();
                calcolaImportoTotale();
            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void calcolaImportoTotale()
    {
        importoTotale = 0;

        if( application.getCarrelloAcquistiOneItem() != null )
            importoTotale = application.getCarrelloAcquistiOneItem().calculateTotalAmount( application.getGroupsCap() );

        showTotale( "Totale: " + application.convertCentesimiInEuro( importoTotale ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showTotale( String msg )
    {
        binding.emissioneTicketView.totaleTextView.setText( msg );
        binding.emissioneTicketView.totaleTextView.setBackgroundResource( R.color.iseo_gray );
        binding.emissioneTicketView.totaleTextView.setTextColor( Color.BLACK );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerButtonsSelezioneZone()
    {
        // Selezione/Deselezione  Zona A
        binding.emissioneTicketView.zonaAButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_A );
            }
        } );

        // Selezione/Deselezione Zona B
        binding.emissioneTicketView.zonaBButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_B );
            }
        } );

        // Selezione/Deselezione Zona C
        binding.emissioneTicketView.zonaCButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_C );
            }
        } );

        // Selezione/Deselezione  ona D
        binding.emissioneTicketView.zonaDButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_D );
            }
        } );

        // Selezione/Deselezione Zona E
        binding.emissioneTicketView.zonaEButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_E );
            }
        } );

        // Selezione/Deselezione Zona TUTTE
        binding.emissioneTicketView.zonaTutteButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setZona( ZONA_TUTTE );
            }
        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setZona( int zona )
    {
        if( zona == ZONA_TUTTE )
        {
            if( zonaPartenza == ZONA_A && zonaArrivo == ZONA_E )
            {
                zonaPartenza = 0;
                zonaArrivo = 0;
            }
            else
            {
                zonaPartenza = ZONA_A;
                zonaArrivo = ZONA_E;
            }
        }
        else
        {
            if( zonaPartenza != 0 && zonaPartenza == zona )
            {
                // Clicking on start zone again -> turn off
                zonaPartenza = Math.min( zonaPartenza + 1, zonaArrivo );
            }
            else if( zonaArrivo != 0 && zonaArrivo == zona )
            {
                // Clicking on end zone again -> turn off
                zonaArrivo = Math.max( zonaArrivo - 1, zonaPartenza );
            }
            else
            {
                if( zona < zonaPartenza || zonaPartenza == 0 )
                    zonaPartenza = zona;

                if( zona > zonaArrivo || zonaArrivo == 0 )
                    zonaArrivo = zona;
            }
        }

        showProgressBar();

        // All DB Calls must be done in a separate thread!
        new Thread( ()->
        {
            updateDatiTariffazione( (Fee)binding.emissioneTicketView.listaTipoDocViaggioSpinner.getSelectedItem(), zonaPartenza, zonaArrivo );

        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerSwitchAndataRitorno()
    {
        binding.emissioneTicketView.andataRitornoSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
            {
                if( isChecked )
                {
                    binding.emissioneTicketView.andataRitornoTextView.setText( "SI" );
                }
                else
                {
                    binding.emissioneTicketView.andataRitornoTextView.setText( "NO" );
                }

                showProgressBar();

                // All DB Calls must be done in a separate thread!
                new Thread( ()->
                {
                    updateDatiTariffazione( (Fee)binding.emissioneTicketView.listaTipoDocViaggioSpinner.getSelectedItem(), zonaPartenza, zonaArrivo );

                } ).start();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerQuantita()
    {
        binding.emissioneTicketView.quantitaLayout.setOnValueChangedListener( new QuantitySelectorView.OnValueChangedListener()
        {
            @Override
            public void onValueChanged( View view, int newValue, Object extraData )
            {
                aggiornaSingolaEmissione();
                calcolaImportoTotale();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerCheckboxTassaMI()
    {
        binding.emissioneTicketView.tassaSbarcoCheckBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
            {
                aggiornaSingolaEmissione();
                calcolaImportoTotale();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerButtonEmissione()
    {
        binding.emissioneTicketView.emissioneButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                tipoCarrello = CARRELLO_EMISSIONE_SINGOLA;
                showDialog_selezioneMetodoPagamento( application.getCarrelloAcquistiOneItem() );
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerButtonAggiungiCarrello()
    {
        binding.emissioneTicketView.aggiungiCarrelloButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                aggiungiDatiCorrentiAlCarrello( application.getCarrelloAcquisti() );

                showSnackbarMessage( "Aggiunto al carrello" );
                updateCarrelloButtonStatus();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateCarrelloButtonStatus()
    {
        if( application.getCarrelloAcquisti() != null && !application.getCarrelloAcquisti().getItems().isEmpty() )
        {
            binding.emissioneTicketView.carrelloButton.setEnabled( true );
        }
        else
        {
            binding.emissioneTicketView.carrelloButton.setEnabled( false );
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerGestisciCarrello()
    {
        binding.emissioneTicketView.carrelloButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                tipoCarrello = CARRELLO_NORMALE;

                showDialog_gestisciCarrello();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_selezioneMetodoPagamento( ShoppingCart carrelloAcquisti )
    {
        Dialog dlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        dlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dlg.setContentView( R.layout.dialog_scelta_tipo_pagamento );
        dlg.setCancelable( false ); // BACK non chiude la dialog

        Button confirmButton = dlg.findViewById( R.id.confirm_button );
        Button cancelButton = dlg.findViewById( R.id.cancel_button );
        RadioGroup paymentType = dlg.findViewById( R.id.tipoPagamento_radiogroup );

        confirmButton.setOnClickListener( v ->
        {
            dlg.dismiss();

            if( paymentType.getCheckedRadioButtonId() == R.id.cartaCredito_radiobutton )
            {
                avviaPagamentoConCarta( carrelloAcquisti );
            }
            else
            {
                avviaPagamentoContanti( carrelloAcquisti );
            }

        } );

        cancelButton.setOnClickListener( v ->
        {
            dlg.dismiss();
        } );

        dlg.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void avviaPagamentoContanti( ShoppingCart carrelloAcquisti )
    {
        registraPagamento( "cash", carrelloAcquisti, null );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void avviaPagamentoConCarta( ShoppingCart carrelloAcquisti )
    {
        int totale = carrelloAcquisti.calculateTotalAmount( application.getGroupsCap() );

        request_PURCHASE( totale );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void handleFailedPayment( PaymentResponse paymentResponse )
    {
        runOnUiThread( () ->
        {
            try
            {
                Dialog dlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

                dlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
                dlg.setContentView( R.layout.dialog_errore_pagamento );

                TextView infoTextView = dlg.findViewById( R.id.errorInfo_TextView );

                // dlg.setCancelable( false ); // BACK non chiude la dialog

                if( paymentResponse != null )
                {
                    infoTextView.setText( "OPERAZIONE FALLITA\n(" + paymentResponse.opEcho + ")\n\n"+ paymentResponse.trxResultMessage );
                }

                Button cancelButton = dlg.findViewById( R.id.close_button );

                cancelButton.setOnClickListener( v ->
                {
                    dlg.dismiss();

                } );

                dlg.show();
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }

        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void registraPagamento( String method, ShoppingCart carrelloAcquisti, String receipt )
    {
        showProgressBar();

        new Thread( ()->
        {
            // WARNING: mettere in pausa il DB-Sync in bkgnd durante le operazioni di emissione/annullamento
            pauseDBSyncService();
            Log.d( TAG, "pauseDBSyncService() on registraPagamento()");

            try
            {
                if( carrelloAcquisti != null )
                {
                    // Registra ORDINE (carrello)
                    Payment paymentData = new Payment();

                    paymentData.id = 0;
                    paymentData.unitId = application.getUnitId();
                    paymentData.opSessionId = application.getCurrentSessionInfo().currentSession.id;
                    paymentData.orderUuid = UUID.randomUUID().toString();
                    paymentData.method = method;
                    paymentData.type = "P";
                    paymentData.value = carrelloAcquisti.calculateTotalAmount( application.getGroupsCap() );
                    paymentData.details = receipt;
                    paymentData.ts = application.toIso8601Local( ZonedDateTime.now() );

                    long paymentId = application.getPaymentsTable().insert( paymentData );

                    carrelloAcquisti.orderUUID = paymentData.orderUuid;

                    // Registra singole EMISSIONI (NOTA: se il nr di passeggeri supera la soglia, generare un'emissione di gruppo)
                    for( ShoppingCartItem item : carrelloAcquisti.getItems() )
                    {
                        int grpCap = application.getGroupsCap() > 0 ? application.getGroupsCap() : 1;

                        if( item.mainIssue.quantity >= grpCap )
                        {
                            registraEmissione( item, paymentData.orderUuid, (int)paymentId, item.mainIssue.quantity, true );
                        }
                        else
                        {
                            for( int i = 0; i <  item.mainIssue.quantity; i++ )
                            {
                                registraEmissione( item, paymentData.orderUuid, (int)paymentId, 1, ( i == 0 ) );
                            }

                        }

                    }

                    // Registrazione pagamento avvenuta con successo, forza upload tabelle immediato
                    if( dbSyncServiceBound && dbSyncService != null )
                    {
                        int maxDays = application.getMaxDaysToStoreDBData();
                        String thresholdDateToDelete = application.toIso8601Local( ZonedDateTime.now().minusDays( maxDays ) );

                        dbSyncService.uploadPaymentsTable( thresholdDateToDelete );
                        dbSyncService.uploadIssuesTable( thresholdDateToDelete );
                    }

                    // Avvia stampa
                    runOnUiThread( () ->
                    {
                        hideProgressBar();
                        showDialog_stampaTitoliDiViaggio( carrelloAcquisti );
                    } );
                }

            }
            catch( Exception err )
            {
                Log.e( TAG, "Something went wrong on registraPagamento() -> \n" + err.getMessage() );

                runOnUiThread( () ->
                {
                    hideProgressBar();
                } );
            }

            // WARNING: Ripristina il DB-Sync in bkgnd dopo le operazioni di emissione/annullamento (indipendemente dall'esito)
            resumeDBSyncService();
            Log.d( TAG, "startDBSyncService() on registraPagamento()");

        } ).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void registraEmissione( ShoppingCartItem cartItem, String orderUUID, int paymentId, int quantity, boolean addChildren )
    {
        // Emissione principale
        Issue mainIssue = new Issue();

        mainIssue.id = 0;
        mainIssue.unitId = application.getUnitId();
        mainIssue.orderUuid = orderUUID;
        mainIssue.uuid = UUID.randomUUID().toString();
        mainIssue.parentUuid = null;
        mainIssue.type = "T";
        mainIssue.userId = NLI_USER_ID;
        mainIssue.feeId = cartItem.mainIssue.feeId;
        mainIssue.value = cartItem.mainIssue.value;
        mainIssue.discountValue = 0;
        mainIssue.discountReason = null;
        mainIssue.ts = application.toIso8601Local( ZonedDateTime.now() );
        mainIssue.paymentId = paymentId;
        mainIssue.fromZoneId = cartItem.mainIssue.fromZoneId > 0 ? cartItem.mainIssue.fromZoneId : null;
        mainIssue.toZoneId = cartItem.mainIssue.toZoneId > 0 ? cartItem.mainIssue.toZoneId : null;
        mainIssue.minutes = cartItem.mainIssue.minutes;
        mainIssue.tripsCount = cartItem.mainIssue.tripsCount;
        mainIssue.details = null;
        mainIssue.travelerId = null;

        cartItem.mainIssue.ts = mainIssue.ts;

        if( cartItem.mainIssue.outMedia != null )
        {
            if( cartItem.mainIssue.outMedia.equals( "qr-code" ) )
                mainIssue.mediaType = "Q";

            if( cartItem.mainIssue.outMedia.equals( "calypso" ) )
                mainIssue.mediaType = "C";

            if( cartItem.mainIssue.outMedia.equals( "mifare-ul" ) )
                mainIssue.mediaType = "U";
        }

        mainIssue.mediaHwid = null;
        mainIssue.validFrom = null; // TODO
        mainIssue.validTo = null; // TODO
        mainIssue.quantity = quantity;
        mainIssue.agencyId = application.getCurrentSessionInfo().currentSession.agencyId;
        mainIssue.opSessionId = application.getCurrentSessionInfo().currentSession.id;
        mainIssue.version = application.getFaresTableVersion();

        long mainIssueId = application.getIssuesTable().insert( mainIssue );

        // Emissioni accessorie
        if( addChildren )
        {
            for( ShoppingCartItem.IssueLight childLightIssue : cartItem.childrenIssues )
            {
                Issue childIssue = new Issue();

                childIssue.id = 0;
                childIssue.unitId = application.getUnitId();
                childIssue.orderUuid = orderUUID;
                childIssue.uuid = UUID.randomUUID().toString();
                childIssue.parentUuid = mainIssue.uuid;
                childIssue.type = "T";
                childIssue.userId = NLI_USER_ID;
                childIssue.feeId = childLightIssue.feeId;
                childIssue.value = childLightIssue.value;
                childIssue.discountValue = 0;
                childIssue.discountReason = null;
                childIssue.ts = application.toIso8601Local( ZonedDateTime.now() );
                childIssue.paymentId = paymentId;
                childIssue.fromZoneId = childLightIssue.fromZoneId;
                childIssue.toZoneId = childLightIssue.toZoneId;
                childIssue.minutes = mainIssue.minutes;
                childIssue.tripsCount = mainIssue.tripsCount;
                childIssue.details = null;
                childIssue.travelerId = null;
                childIssue.mediaType = mainIssue.mediaType;
                childIssue.mediaHwid = mainIssue.mediaHwid;
                childIssue.validFrom = null;
                childIssue.validTo = null;
                childIssue.quantity = childLightIssue.quantity;
                childIssue.agencyId = application.getCurrentSessionInfo().currentSession.agencyId;
                childIssue.opSessionId = application.getCurrentSessionInfo().currentSession.id;
                childIssue.version = application.getFaresTableVersion();

                application.getIssuesTable().insert( childIssue );
            }

        }

        // Se 'auto-validazione' è attiva... aggiorna direttamente la tabella di validazione
        if( cartItem.mainIssue.autoValidation )
        {
            registraValidazione( application.getUnitId(),
                                 (int)mainIssueId,
                                 mainIssue.mediaType,
                                 mainIssue.mediaHwid,
                                 mainIssue.tripsCount,
                                 mainIssue.feeId,
                                 0, //mainIssue.validFrom,
                                 0, //mainIssue.validTo,
                                 mainIssue.fromZoneId,
                                 mainIssue.toZoneId );
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_stampaTitoliDiViaggio( ShoppingCart carrelloAcquisti )
    {
        Dialog dlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        dlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dlg.setContentView( R.layout.dialog_stampa_titoli_di_viaggio );

        dlg.setCancelable( false ); // BACK non chiude la dialog

        Button confirmButton = dlg.findViewById( R.id.confirm_button );
        Button cancelButton = dlg.findViewById( R.id.cancel_button );

        confirmButton.setOnClickListener( v ->
        {
            dlg.dismiss();

            stampaOrdine( carrelloAcquisti.orderUUID );
        } );

        cancelButton.setOnClickListener( v ->
        {
            dlg.dismiss();
        } );

        dlg.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void stampaOrdine( String orderUUID )
    {
        // All DB Calls must be done in a separate thread!
        new Thread( ()->
        {
            runOnUiThread( ()->
            {
                showProgressBar();
            } );

            int cardId = 0;

            // Recupera TUTTE le main issues dell'ordine
            List<Issue> mainIssues = application.getIssuesTable().getParentIssuesByOrderId( orderUUID );
            Payment payment = application.getPaymentsTable().getPaymentByUUID( orderUUID );

            try
            {
                printOrderUUID = orderUUID;
                orderVatTable.clear();

                // Crea coda di stampa
                printQueue.clear();

                for( int i = 0; i < mainIssues.size(); i++ )
                {
                    Issue issue = mainIssues.get( i );
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    ZonedDateTime emissionTsISO8601 = application.fromIso8601ToLocal( issue.ts );
                    String emissionTs = emissionTsISO8601.format( dtf );

                    PrintQueueData printData = new PrintQueueData();

                    printData.issueUUID = issue.uuid;
                    printData.logoResourceId = R.drawable.logo_nli_black_300;
                    printData.separatorResourceId = R.drawable.line_separator;

                    //printData.title = "___________________________\nRICEVUTA DI VENDITA\n___________________________\n\n";

                    printData.title = "RICEVUTA DI VENDITA";
                    printData.timeStamp = emissionTs;

                    // Tariffa Principale
                    Fee tipologia = application.getFeesTable().getFeeById( issue.feeId );

                    printData.typology = tipologia.description.toUpperCase();
                    printData.zones = "";

                    Zone partenza = null;
                    Zone arrivo = null;

                    if( issue.fromZoneId != null && issue.toZoneId != null )
                    {
                        partenza = application.getZonesTable().getZoneById( issue.fromZoneId );
                        arrivo = application.getZonesTable().getZoneById( issue.toZoneId );
                    }

                    if( partenza != null && arrivo != null )
                        printData.zones = partenza.label + " - " + arrivo.label;

                    printData.quantity = "";

                    if( issue.quantity > 1 )
                        printData.quantity = issue.quantity + " PASSEGGERI";

                    // VAT - Tariffa Principale
                    int mainIssueValue = ( issue.value * issue.quantity );
                    int mainIssueVatPerc = tipologia.vatPerc;
                    int mainIssueVatValue = application.calcIvaScorporata( issue.value, issue.quantity, mainIssueVatPerc );
                    String vatText = "";

                    if( !orderVatTable.containsKey( mainIssueVatPerc ) )
                    {
                        // Insert new vat in map
                        int index = orderVatTable.size();

                        orderVatTable.put( mainIssueVatPerc, new PrintQueueData.VatInfo( mainIssueVatPerc, mainIssueValue, mainIssueVatValue, index ) );

                        vatText = application.convertNumberToLetter( index );
                    }
                    else
                    {
                        // If vat is already present
                        PrintQueueData.VatInfo prevVatInfo = orderVatTable.get( mainIssueVatPerc );

                        // Adds VatValue
                        prevVatInfo.vatValue += mainIssueVatValue;

                        // Update map
                        orderVatTable.replace( mainIssueVatPerc, prevVatInfo );

                        vatText = application.convertNumberToLetter( prevVatInfo.index );
                    }

                    //---

                    printData.price = application.getPaddedText( "Tit. viaggio", issue.quantity + "x " + application.convertCentesimiInEuro( issue.value ) + " *" + vatText, LINE_WIDTH_NRM );

                    // Tariffe Accessorie
                    List<Issue> childIssues = application.getIssuesTable().getIssuesByOrderIdAndParentId( orderUUID, issue.uuid );

                    int childrenValue = 0;

                    for( Issue child : childIssues )
                    {
                        Fee tipologiaChild = application.getFeesTable().getFeeById( child.feeId );

                        if( tipologiaChild != null )
                        {
                            childrenValue += child.value * child.quantity;

                            // VAT - Tariffe Accessorie
                            int childIssueValue = ( child.value * child.quantity );
                            int childIssueVatPerc = tipologiaChild.vatPerc;
                            int childIssueVatValue = application.calcIvaScorporata( child.value, child.quantity, childIssueVatPerc );
                            vatText = "";

                            if( !orderVatTable.containsKey( childIssueVatPerc ) )
                            {
                                // Insert new vat in map
                                int index = orderVatTable.size();

                                orderVatTable.put( childIssueVatPerc, new PrintQueueData.VatInfo( childIssueVatPerc, childIssueValue, childIssueVatValue, index ) );

                                vatText = application.convertNumberToLetter( index );
                            }
                            else
                            {
                                // If vat is already present
                                PrintQueueData.VatInfo prevVatInfo = orderVatTable.get( childIssueVatPerc );

                                // Adds VatValue
                                prevVatInfo.vatValue += childIssueVatValue;

                                // Update map
                                orderVatTable.replace( childIssueVatPerc, prevVatInfo );

                                vatText = application.convertNumberToLetter( prevVatInfo.index );
                            }

                            printData.additionalFees.add( application.getPaddedText( "" + tipologiaChild.description, child.quantity + "x " + application.convertCentesimiInEuro( child.value ) + " *" + vatText, LINE_WIDTH_NRM ) );
                        }
                    }

                    // Totale parziale
                    printData.partialAmount = ( issue.value * issue.quantity ) + childrenValue;

                    if( issue.mediaType != null && issue.mediaType.equals( "Q" ) )
                    {
                        ZonedDateTime tsValidFrom = application.fromIso8601ToLocal( issue.validFrom );
                        ZonedDateTime tsValidTo = application.fromIso8601ToLocal( issue.validTo );

                        long epochTs = emissionTsISO8601.toEpochSecond();
                        long epochTsValidFrom = tsValidFrom != null ? tsValidFrom.toEpochSecond() : 0L;
                        long epochTsValidTo = tsValidTo != null ? tsValidTo.toEpochSecond() : 0L;

                        printData.qrCodeString = encodeTicketData( epochTs,
                                                                   issue.feeId,
                                                                   issue.fromZoneId == null ? 0 : issue.fromZoneId,
                                                                   issue.toZoneId == null ? 0 : issue.toZoneId,
                                                                   epochTsValidFrom,
                                                                   epochTsValidTo,
                                                                   issue.tripsCount,
                                                                   issue.minutes,
                                                                   issue.quantity,
                                                                   issue.id,
                                                                   cardId );
                    }

                    String paddedUnitId = String.valueOf( application.getUnitId() ); //String.format( "%010d", application.getUnitId() );
                    String paddedId = String.format( "%08d", issue.id );
                    String serialeEmissione = paddedUnitId + "-" + paddedId;

                    printData.idEmissione = "ID TITOLO  " + serialeEmissione;
                    printData.idOperatore = application.getPaddedText( "ID OPERATORE", application.getCurrentSessionInfo().currentSession.loginUserId, LINE_WIDTH_SML );
                    printData.idDispositivo = application.getPaddedText( "ID DISPOSITIVO", "" + application.getCurrentSessionInfo().currentSession.unitId , LINE_WIDTH_SML );

                    if( i == mainIssues.size() - 1 )
                    {
                        printData.paymentType = payment.method.equals( "pos" ) ? "(POS)" : "";
                        printData.totalAmount = payment.value;
                    }

                    printQueue.add( printData );
                }

                // Setup queue data
                printQueueId = orderUUID;
                printQueueInitialSize = printQueue.size();
                lastPrintedIssueUUID = "";

                printQueueElement();

            }
            catch( Exception e )
            {
                handlePrintError( e.getMessage() );
            }

            runOnUiThread( ()->
            {
                hideProgressBar();
            } );

        } ).start();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void printQueueElement()
    {
        if( !printQueue.isEmpty() )
        {
            try
            {
                currentPrintedItem = printQueue.poll();
                lastPrintedIssueUUID = currentPrintedItem.issueUUID;
                String msg = "Stampa Titolo di Viaggio\n" + ( printQueueInitialSize - printQueue.size() ) + " di "  + printQueueInitialSize;
                boolean lastElement = printQueue.isEmpty();

                runOnUiThread( ()->
                {
                    showProgressoStampa( msg, false, false, false );
                } );

                // LOGO NLI
                Bundle imgSettings = new Bundle();
                application.getVectorPrinter().addImage( imgSettings, BitmapFactory.decodeResource( getResources(), currentPrintedItem.logoResourceId ) );

                // P.IVA
                application.setTextToPrint( "P.IVA 03000970164\n", Alignment.CENTER, TextSize.NORMAL, true );

                // TITOLO
                Bitmap separatorBitmap = BitmapFactory.decodeResource( getResources(), currentPrintedItem.separatorResourceId );
                application.getVectorPrinter().addImage( imgSettings, separatorBitmap );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.NORMAL, false );
                application.setTextToPrint( currentPrintedItem.title, Alignment.CENTER, TextSize.NORMAL, true );
                application.getVectorPrinter().addImage( imgSettings, separatorBitmap );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.TINY, false );

                // DATA/ORA EMISSIONE
                application.setTextToPrint( currentPrintedItem.timeStamp, Alignment.CENTER, TextSize.NORMAL, false );
                application.setTextToPrint( "\n\n", Alignment.CENTER, TextSize.TINY, false );

                // TIPOLOGIA DOC. VIAGGIO
                application.setTextToPrint( currentPrintedItem.typology, Alignment.CENTER, TextSize.LARGE, true );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.TINY, false );

                // ZONE
                application.setTextToPrint( currentPrintedItem.zones, Alignment.CENTER, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.CENTER, TextSize.TINY, false );

                // NR. PASSEGGERI
                application.setTextToPrint( currentPrintedItem.quantity, Alignment.CENTER, TextSize.NORMAL, true );
                application.setTextToPrint( "\n\n", Alignment.CENTER, TextSize.TINY, false );

                // IMPORTO
                application.setTextToPrint( currentPrintedItem.price, Alignment.NORMAL, TextSize.NORMAL, false );

                // ADDITIONAL FEES
                for( String additionalFee : currentPrintedItem.additionalFees )
                {
                    application.setTextToPrint( additionalFee, Alignment.NORMAL, TextSize.NORMAL, false );
                }

                application.setTextToPrint( "---------------------------\n", Alignment.CENTER, TextSize.NORMAL, true );

                // TOTALE PARZIALE
                String totaleParziale = application.getPaddedText( "TOTALE BIGLIETTO:", application.convertCentesimiInEuro( currentPrintedItem.partialAmount ), LINE_WIDTH_NRM );
                application.setTextToPrint( totaleParziale, Alignment.NORMAL, TextSize.NORMAL, true );

                // LEGENDA IVA (VAT)
                String vatLegend = "";
                List<PrintQueueData.VatInfo> vats = new ArrayList<>( orderVatTable.values() );
                vats.sort( ( vat1, vat2 ) -> Integer.compare( vat1.index, vat2.index ) );

                for( PrintQueueData.VatInfo info : vats )
                {
                    if( info.vat > 0 )
                    {
                        vatLegend += "*" + application.convertNumberToLetter( info.index ) + " IVA " + info.vat + "%  ";
                    }
                    else
                    {
                        vatLegend += "*" + application.convertNumberToLetter( info.index ) + " NO IVA  ";
                    }
                }

                application.setTextToPrint( vatLegend + "\n", Alignment.NORMAL, TextSize.SMALL, true );
                application.setTextToPrint( "\n", Alignment.NORMAL, TextSize.TINY, false );

                // QR-CODE
                if( !currentPrintedItem.qrCodeString.isBlank() )
                {
                    Bundle qrCodeSettings = new Bundle();
                    qrCodeSettings.putInt( VectorPrinterData.QRCODE_SIZE, 220 );

                    application.getVectorPrinter().addQrCode( qrCodeSettings, currentPrintedItem.qrCodeString, null );
                }

                application.setTextToPrint( currentPrintedItem.idEmissione, Alignment.CENTER, TextSize.SMALL, true );
                application.setTextToPrint( "\n", Alignment.CENTER, TextSize.TINY, false );

                application.setTextToPrint( currentPrintedItem.idOperatore, Alignment.NORMAL, TextSize.SMALL, true );
                application.setTextToPrint( currentPrintedItem.idDispositivo, Alignment.NORMAL, TextSize.SMALL, true );

                //---

                // TOTALE
                if( lastElement && currentPrintedItem.totalAmount > 0 )
                {
                    String totaleOrdine = application.getPaddedText( "IMPORTO TOTALE" + currentPrintedItem.paymentType + ":", application.convertCentesimiInEuro( currentPrintedItem.totalAmount ), LINE_WIDTH_NRM );

                    application.setTextToPrint( "\n---------------------------\n", Alignment.CENTER, TextSize.NORMAL, true );
                    application.setTextToPrint( totaleOrdine, Alignment.NORMAL, TextSize.NORMAL, true );

                    // VALORI DI IVA (VAT)
                    String vatValues = "";

                    for( PrintQueueData.VatInfo info : vats )
                    {
                        if( info.vat > 0 )
                        {
                            String label = "Di cui IVA al " + info.vat + "%";

                            String value = application.convertCentesimiInEuro( info.vatValue );
                            String paddedTxt = application.getPaddedText( label, value, LINE_WIDTH_SML );

                            application.setTextToPrint( paddedTxt, Alignment.NORMAL, TextSize.SMALL, true );
                        }
                    }

                }

                //application.getPrinter().addImage( imgSettings, separatorBitmap );
                //application.setTextToPrint( "\n_._._._._._._._._._._._._._\n", Alignment.NORMAL, TextSize.NORMAL, false );

                print();
            }
            catch( RemoteException e )
            {
                handlePrintError( e.getMessage() );
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void print()
    {
        try
        {
            application.getVectorPrinter().startPrint( new OnPrintListener.Stub()
            {
                @Override
                public void onFinish() throws RemoteException
                {
                    if( printQueue.size() == 0 )
                    {
                        // Tutti gli elementi sono stati stampati
                        printQueueId = "";
                        printOrderUUID = "";
                        currentPrintedItem = null;
                        printing = false;
                        lastPrintError = "";

                        if( tipoCarrello == CARRELLO_EMISSIONE_SINGOLA )
                        {
                            // Cleanup UI?
                        }
                        else if( tipoCarrello == CARRELLO_NORMALE )
                        {
                            // Pulisce carrello normale
                            application.clearCarrelloAcquisti();

                            // Cleanup UI?
                        }

                        runOnUiThread(() ->
                        {
                            updateCarrelloButtonStatus();
                            showProgressoStampa( "Stampa completata con successo", false, false, true );

                        } );

                    }
                    else
                    {
                        try
                        {
                            Thread.sleep( 2000 );
                        }
                        catch( InterruptedException e )
                        {

                        }

                        printQueueElement();
                    }

                }

                @Override
                public void onStart() throws RemoteException
                {
                    printing = true;
                    lastPrintError = "";
                }

                @Override
                public void onError( int i, String s ) throws RemoteException
                {
                    printing = false;
                    lastPrintError = s;

                    handlePrintError( s );
                }

            } );

        }
        catch( RemoteException e )
        {
            handlePrintError( e.getMessage() );
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void handlePrintError( String exceptionError )
    {
        String msg = "ERRORE DI STAMPA\n";

        if( !lastPrintError.isBlank() && !lastPrintError.isEmpty() )
            msg += "\nErrore specifico:\n" + lastPrintError;

        if( msg.isBlank() )
            msg += exceptionError;

        String finalMsg = msg;

        runOnUiThread( () ->
        {
            showProgressoStampa( finalMsg, true, true, false );

        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showProgressoStampa( String message, boolean showRitentaStampa, boolean showAnnullaOrdine, boolean showChiudi )
    {
        if( dlgProgressoStampa != null && dlgProgressoStampa.isShowing() )
            dlgProgressoStampa.dismiss();

        dlgProgressoStampa = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        dlgProgressoStampa.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dlgProgressoStampa.setContentView( R.layout.dialog_stampa );

        TextView infoMessageTextView = dlgProgressoStampa.findViewById( R.id.printInfo_TextView );
        infoMessageTextView.setText( message );

        Button annullaOrdineButton = dlgProgressoStampa.findViewById( R.id.annullaOrdine_button );
        Button ritentaStampaButton = dlgProgressoStampa.findViewById( R.id.ritentaStampa_button );
        Button chiudiButton = dlgProgressoStampa.findViewById( R.id.close_button );

        annullaOrdineButton.setVisibility( showAnnullaOrdine ? View.VISIBLE : View.GONE );
        ritentaStampaButton.setVisibility( showRitentaStampa ? View.VISIBLE : View.GONE );
        chiudiButton.setVisibility( showChiudi ? View.VISIBLE : View.GONE );

        // listeners
        annullaOrdineButton.setOnClickListener( v ->
        {
            dlgProgressoStampa.dismiss();

            // Visualizza dialog di conferma
            AlertDialog.Builder dlgBld = new AlertDialog.Builder( this );
            dlgBld.setTitle( "Annulla Ordine" );
            dlgBld.setIcon( android.R.drawable.ic_dialog_alert );
            dlgBld.setMessage( "Sei sicuro di voler annullare l'ordine?" );

            dlgBld.setPositiveButton( "Sì", ( dialog, which )->
            {
                dialog.dismiss();
                gestisciAnnullamentoOrdine();
            } );

            dlgBld.setNegativeButton( "No", ( dialog, which )->
            {
                dialog.dismiss();
            } );

            dlgBld.show();
        } );

        ritentaStampaButton.setOnClickListener( v ->
        {
            dlgProgressoStampa.dismiss();

            // Ritenta stampa -> Riaggiunge l'elemento corrente in coda
            if( currentPrintedItem != null )
            {
                dlgProgressoStampa.dismiss();

                printQueue.addFirst( currentPrintedItem );

                currentPrintedItem = null;

                // restart print
                printQueueElement();
            }

        } );

        chiudiButton.setOnClickListener( v ->
        {
            dlgProgressoStampa.dismiss();

        } );

        dlgProgressoStampa.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Ticketdata.TicketData decodeTicketData( String qrText )
    {
        Ticketdata.TicketData ticketData = null;

        try
        {
            if( qrText.trim().startsWith( "NLI" ) )
            {
                String qrTextNoHeader = qrText.substring( 5 );
                byte[] decodedBytes = Base45.decode( qrTextNoHeader.getBytes( StandardCharsets.ISO_8859_1 ) );
                ticketData = Ticketdata.TicketData.parseFrom( decodedBytes );

                if( ticketData != null )
                {
                    int originalCRC = ticketData.getCrc();
                    Ticketdata.TicketData.Builder builder = ticketData.toBuilder().setCrc( 0 ); // Ricrea tickdata no CRC
                    byte[] dataWithoutCRC = builder.build().toByteArray();
                    byte[] crcInput = application.concatenate( dataWithoutCRC, SECRET.getBytes( StandardCharsets.UTF_8 ) );
                    int calculatedCRC = application.calculateCRC32( crcInput );

                    if( calculatedCRC != originalCRC )
                        ticketData = null;
                }
            }

        }
        catch( InvalidProtocolBufferException e )
        {

        }

        return ticketData;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private String encodeTicketData( long timeStamp, int feeId, int startZone, int endZone, long validityTsFrom, long validityTsTo, int tripsCount, int minutes, int quantity, long issueId, int cardId )
    {
        String result = "";
        String unpaddedUintiId = String.valueOf( application.getUnitId() );
        String paddedUnitId = String.format( "%010d", application.getUnitId() );
        String paddedId = String.format( "%08d", issueId );
        String serialeEmissione = paddedUnitId + paddedId;

        try
        {
            Ticketdata.TicketData.Builder ticketBuilder = Ticketdata.TicketData.newBuilder()
                    .setDomainId( NLI_DOMAIN_ID )
                    .setUserId( NLI_USER_ID )
                    .setFeeId( feeId )
                    .setZoneFrom( startZone )
                    .setZoneTo( endZone )
                    .setTsValidFrom( validityTsFrom )
                    .setTsValidTo( validityTsTo )
                    .setTripsCount( tripsCount )
                    .setMinutes( minutes )
                    .setTsIssue( timeStamp )
                    .setTripCode( "" )
                    .setQuantity( quantity )
                    .setUnitId( application.getUnitId() )
                    .setSerial( (int) issueId )
                    .setCardId( cardId )
                    .setCrc( 0 );

            byte[] ticketBytes = ticketBuilder.build().toByteArray();

            // Calcolo CRC32 (TicketData + SECRET)
            byte[] crcInput = application.concatenate( ticketBytes, SECRET.getBytes( StandardCharsets.UTF_8 ) );
            int crcValue = application.calculateCRC32( crcInput );

            // Aggiorniamo con il CRC
            ticketBuilder.setCrc( crcValue );
            Ticketdata.TicketData ticketDataFinal = ticketBuilder.build();
            ticketBytes = ticketDataFinal.toByteArray();
            byte[] encodedRaw = Base45.encode( ticketBytes );

            String base45Encoded =  new String( encodedRaw, 0, 0, encodedRaw.length );

            return TICKET_HEADER + base45Encoded;
        }
        catch( Exception e )
        {
            Log.e( TAG, "encodeTicketData ERROR ->" + e.getMessage() );
        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_gestisciCarrello()
    {
        if( application.getCarrelloAcquisti() != null && carrelloDlg != null )
        {
            showProgressBar();

            CarrelloView listaEmissionView = carrelloDlg.findViewById( R.id.listaEmissioniView );
            listaEmissionView.setCarrelloAcquisti( application.getCarrelloAcquisti(), this );

            Button confirmButton = carrelloDlg.findViewById( R.id.confirm_button );
            Button cancelButton = carrelloDlg.findViewById( R.id.cancel_button );

            confirmButton.setOnClickListener( v ->
            {
                carrelloDlg.hide();

                showDialog_selezioneMetodoPagamento( application.getCarrelloAcquisti() );

            } );

            cancelButton.setOnClickListener( v ->
            {
                updateCarrelloButtonStatus();

                carrelloDlg.hide();

            } );

            carrelloDlg.show();
            hideProgressBar();
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupSpinnerZona()
    {
        getListaZone();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupListenerSpinnerZona()
    {
        // Selezione Zona
        binding.validazioneControlloTicketView.listaFermateSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                currentZone = listaZone.get( position );
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent )
            {
                currentZone = null;
            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void getListaZone()
    {
        listaZone.clear();

        // ASYNC get zones
        new Thread( ()->
        {
            listaZone = application.getZonesTable().getZones();

            if( zoneFermate != null )
            {
                Zone defaultZone = application.getZonesTable().getZoneById( application.getCurrentSessionInfo().zonaDefaultId );
                int SelZoneId = 0;

                if( SelZoneId <= 0 )
                    SelZoneId = defaultZone.id;

                // RUN ON UI THREAD
                int finalSelZoneId = SelZoneId;

                runOnUiThread(() ->
                {
                    ZoneAdapter adapter = new ZoneAdapter( this, listaZone );

                    binding.validazioneControlloTicketView.listaFermateSpinner.setEnabled( true );
                    binding.validazioneControlloTicketView.listaFermateSpinner.setAdapter( adapter );
                    binding.validazioneControlloTicketView.listaFermateSpinner.setSelection( adapter.getPositionById( finalSelZoneId ) );
                } );
            }

        } ).start();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showQrCodeScanner()
    {
        binding.validazioneControlloTicketView.qrcodeScannerLayout.setVisibility( View.VISIBLE );
        binding.validazioneControlloTicketView.cameraSurfaceView.resume();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void hideQrCodeScanner()
    {
        binding.validazioneControlloTicketView.qrcodeScannerLayout.setVisibility( View.INVISIBLE );
        binding.validazioneControlloTicketView.cameraSurfaceView.pause();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void setupQRCodeScanner()
    {
        binding.validazioneControlloTicketView.cameraSurfaceView.setStatusText("");

        // Imposta la lettura dei formati QR_CODE e CODE_39
        List<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        binding.validazioneControlloTicketView.cameraSurfaceView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));

        // Lettura continua
        binding.validazioneControlloTicketView.cameraSurfaceView.decodeContinuous(new BarcodeCallback()
        {
            @Override
            public void barcodeResult(BarcodeResult result)
            {
                try
                {
                    if (processingQRCode)
                        return;

                    processingQRCode = true;

                    String qrText = result.getText() == null ? "" : result.getText();
                    Ticketdata.TicketData ticketData = decodeTicketData(qrText);

                    if (ticketData != null)
                    {
                        new Thread(() ->
                        {
                            int error = NO_ERROR;
                            boolean dominioAziendaValidi = false;
                            boolean zonaValide = false;
                            boolean durataValida = true;
                            boolean intervalloDateValido = true;
                            boolean nrCorseValido = true;

                            SearchTicketResponse srcTicketResponse = null;
                            Fee ticketFee = null;

                            try
                            {
                                if (DeviceHelper.getInstance().isServiceReady()) {
                                    DeviceHelper.getInstance().getBeeper().startBeep(200);
                                }

                                Ticketdata.TicketData finalTicketData = ticketData;
                                ticketFee = application.getFeesTable().getFeeById(ticketData.getFeeId());

                                if (finalTicketData.getZoneFrom() > 0 && finalTicketData.getZoneTo() > 0)
                                {
                                    ticketZonaDa = application.getZonesTable().getZoneById(finalTicketData.getZoneFrom());
                                    ticketZonaA = application.getZonesTable().getZoneById(finalTicketData.getZoneTo());
                                }
                                else
                                {
                                    ticketZonaDa = null;
                                    ticketZonaA = null;
                                }

                                try
                                {
                                    String paddedUnitId = String.format("%010d", ticketData.getUnitId());
                                    String paddedId = String.format("%08d", ticketData.getSerial());
                                    String tickedID = paddedUnitId + paddedId;

                                    Call<SearchTicketResponse> call = TecBusApiClient.getValidationService().searchTicket("Bearer " + application.getAccessToken(), tickedID);
                                    Response response = call.execute();

                                    if (response != null && response.body() != null && response.code() == 200)
                                    {
                                        srcTicketResponse = (SearchTicketResponse) response.body();
                                    }
                                    else
                                    {
                                        error = SEARCH_TICKET_BAD_RESPONSE;
                                    }

                                }
                                catch (Exception e)
                                {
                                    error = SEARCH_TICKET_EXCEPTION;
                                }

                                if (finalTicketData.getDomainId() == NLI_DOMAIN_ID &&
                                        finalTicketData.getUserId() == NLI_USER_ID)
                                {
                                    dominioAziendaValidi = true;
                                }
                                else
                                {
                                    error = INVALID_DOMAIN_OR_USERID;
                                }

                                if (currentZone != null && ticketZonaDa != null && ticketZonaA != null)
                                {
                                    if (currentZone.sequence >= ticketZonaDa.sequence &&
                                            currentZone.sequence <= ticketZonaA.sequence)
                                        zonaValide = true;
                                }
                                else if (ticketZonaDa == null && ticketZonaA == null)
                                {
                                    zonaValide = true;
                                }

                                if (srcTicketResponse != null)
                                {
                                    if (srcTicketResponse.state.ts_last_validation != null)
                                    {
                                        ZonedDateTime lastValidationDate = application.fromIso8601ToLocal(srcTicketResponse.state.ts_last_validation);

                                        if (srcTicketResponse.valid_from != null && srcTicketResponse.valid_to != null)
                                        {
                                            ZonedDateTime from = application.fromIso8601ToLocal(srcTicketResponse.valid_from);
                                            ZonedDateTime to = application.fromIso8601ToLocal(srcTicketResponse.valid_to);

                                            if (ZonedDateTime.now().isBefore(from) || ZonedDateTime.now().isAfter(to))
                                                intervalloDateValido = false;
                                        }

                                        if (finalTicketData.getMinutes() > 0)
                                        {
                                            ZonedDateTime dataScadenza = ZonedDateTime.now().plusMinutes(finalTicketData.getMinutes());

                                            if (ZonedDateTime.now().isAfter(dataScadenza))
                                                durataValida = false;
                                        }
                                        else
                                        {
                                            durataValida = true;
                                        }
                                    }
                                }

                                if (srcTicketResponse != null && srcTicketResponse.state.residual_trips == 0)
                                    nrCorseValido = false;

                            }
                            catch (RemoteException e)
                            {
                                error = EXCEPTION;
                            }

                            if (error != NO_ERROR && error != SEARCH_TICKET_EXCEPTION)
                            {
                                runOnUiThread(() ->
                                {
                                    try
                                    {
                                        if (DeviceHelper.getInstance().isServiceReady()) {
                                            DeviceHelper.getInstance().getBeeper().startBeepNew(200, 500);
                                        }
                                        showDialog_Message("QR-Code non valido!");
                                    }
                                    catch (RemoteException e)
                                    {
                                        Log.e("QRCode", "Erro ao tocar beep de erro", e);
                                    }
                                });

                            }
                            else
                            {
                                SearchTicketResponse finalSrcTicketResponse = srcTicketResponse;
                                Fee finalTicketFee = ticketFee;
                                boolean finalDominioAziendaValidi = dominioAziendaValidi;
                                boolean finalZonaValide = zonaValide;
                                boolean finalDurataValida = durataValida;
                                boolean finalIntervalloDateValido = intervalloDateValido;
                                boolean finalNrCorseValido = nrCorseValido;

                                runOnUiThread(() ->
                                {
                                    showDialog_RisultatoLettura(ticketData,
                                            finalTicketFee,
                                            finalSrcTicketResponse,
                                            finalDominioAziendaValidi,
                                            finalZonaValide,
                                            finalDurataValida,
                                            finalIntervalloDateValido,
                                            finalNrCorseValido);
                                });
                            }

                        }).start();

                    }
                    else
                    {
                        runOnUiThread(() ->
                        {
                            try
                            {
                                if (DeviceHelper.getInstance().isServiceReady()) {
                                    DeviceHelper.getInstance().getBeeper().startBeepNew(200, 500);
                                }
                                showDialog_Message("QR-Code non valido!");
                            }
                            catch (RemoteException e)
                            {
                                Log.e("QRCode", "Erro ao tocar beep QR inválido", e);
                            }
                        });
                    }

                }
                catch (Exception e)
                {
                    showSnackbarMessage("ERRORE NELLA DECODIFICA QR-CODE!");
                }

            }

        });

        binding.validazioneControlloTicketView.cameraSurfaceView.resume();
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_RisultatoLettura( Ticketdata.TicketData ticketData, Fee ticketFee, SearchTicketResponse srcTicketResponse, boolean dominioAziendaValidi, boolean zonaValide, boolean durataMinutiValida, boolean intervalloDateValido, boolean nrCorseValido )
    {
        // Nascondi la vista della fotocamera e metti in pausa la lettura QRCode
        hideQrCodeScanner();

        Dialog dlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        dlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dlg.setContentView( R.layout.dialog_risultato_lettura );
        dlg.setCancelable( false ); // BACK non chiude la dialog

        TextView validationResultTextView = dlg.findViewById( R.id.validationResult_textView );
        TextView dataEmissioneTextView = dlg.findViewById( R.id.dataEmissione_textView );
        TextView tipologiaTextView = dlg.findViewById( R.id.tipologia_textView );
        TextView zoneTextView = dlg.findViewById( R.id.zone_textView );
        TextView nrPasseggeriTextView = dlg.findViewById( R.id.quantity_textView );
        TextView residualTripTextView = dlg.findViewById( R.id.residualTrip_textView );
        TextView lastValidationTextView = dlg.findViewById( R.id.lastValidation_textView );
        Button validateButton = dlg.findViewById( R.id.validaTicket_button );
        Button closeButton = dlg.findViewById( R.id.close_button );

        validationResultTextView.setText( "" );
        dataEmissioneTextView.setText( "" );
        tipologiaTextView.setText( "" );
        zoneTextView.setText( "" );
        nrPasseggeriTextView.setText( "" );
        residualTripTextView.setText( "" );
        lastValidationTextView.setText( "" );

        lastValidationTextView.setVisibility( View.GONE );
        validateButton.setVisibility( View.GONE );
        closeButton.setVisibility( View.VISIBLE );

        String title = "VALIDO";
        int titleBkgndColor = Color.parseColor( "#00A000");

        if( !durataMinutiValida || srcTicketResponse.state.status.equalsIgnoreCase( "X" ) )
        {
            title = "SCADUTO / ESAUSTO";
            titleBkgndColor = Color.parseColor( "#a0a0C0");
        }
        else if( srcTicketResponse.state.status.equalsIgnoreCase( "D" ) )
        {
            title = "BLOCCATO / ANNULLATO";
            titleBkgndColor = Color.parseColor( "#800000");
        }
        else if( !zonaValide )
        {
            title = "ZONA NON VALIDA";
            titleBkgndColor = Color.parseColor( "#800000");
        }

        validationResultTextView.setText( title );
        validationResultTextView.setBackgroundColor( titleBkgndColor );

        dataEmissioneTextView.setText( "Emesso il " + application.convertTimestampToDate( ticketData.getTsIssue() ) );
        tipologiaTextView.setText( ticketFee.description != null ? ticketFee.description : "" );

        if( ticketZonaDa != null && ticketZonaA != null )
            zoneTextView.setText( ticketZonaDa.label + " - " +  ticketZonaA.label );

        nrPasseggeriTextView.setText( "Passeggeri " + ticketData.getQuantity() );

        if( srcTicketResponse.state.residual_trips >= 0 )
            residualTripTextView.setText( "Corse residue " + srcTicketResponse.state.residual_trips );

        if( srcTicketResponse.state.ts_last_validation != null )
        {
            ZonedDateTime lastValidationDate = application.fromIso8601ToLocal( srcTicketResponse.state.ts_last_validation );
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm:ss", Locale.getDefault() );

            lastValidationTextView.setText( "ULTIMA VALIDAZIONE:\n" + lastValidationDate.format( dtf ) );
            lastValidationTextView.setVisibility( View.VISIBLE );
        }

        if( modalitaViewValidazioneControllo == MODALITA_VALIDAZIONE_TICKET &&
            dominioAziendaValidi &&
            zonaValide &&
            durataMinutiValida &&
            intervalloDateValido &&
            nrCorseValido )
        {
            validateButton.setVisibility( View.VISIBLE );
        }

        validateButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                processingQRCode = false;
                dlg.dismiss();

                // Ripristina la lettura del QRCode
                showQrCodeScanner();
                showProgressBar();

                registraValidazione( ticketData.getUnitId(),
                                     ticketData.getSerial(),
                                     srcTicketResponse.media_type,
                                     srcTicketResponse.media_hwid,
                                     srcTicketResponse.state.residual_trips,
                                     ticketData.getFeeId(),
                                     ticketData.getTsValidFrom(),
                                     ticketData.getTsValidTo(),
                                     ticketData.getZoneFrom(),
                                     ticketData.getZoneTo() );
            }

        } );

        closeButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                processingQRCode = false;
                dlg.dismiss();

                // Ripristina la lettura del QRCode
                showQrCodeScanner();
            }

        } );

        dlg.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void registraValidazione( Integer tickedUnitID, int ticketId, String mediaType, String mediaHwid, int residualTrips, int feeId, long validFrom, long validTo, int zoneFrom, int zoneTo )
    {
        new Thread( ()->
        {
            String paddedUnitId = String.format( "%010d", tickedUnitID );
            String paddedId = String.format( "%08d", ticketId );
            String serialeEmissione = paddedUnitId + paddedId;

            Validation newValidation = new Validation();

            newValidation.unitId = application.getUnitId();
            newValidation.userId = NLI_USER_ID;
            newValidation.opSessionId = (int)application.getCurrentSessionInfo().currentSession.id;
            newValidation.mediaType = mediaType;
            newValidation.mediaHwid = mediaHwid;
            newValidation.extension = "nli_1.0.0";
            newValidation.documentId = serialeEmissione;
            newValidation.residualTrips = residualTrips;

            newValidation.residualTrips--;

            if( newValidation.residualTrips < 0 )
                newValidation.residualTrips = 0;

            newValidation.ts = application.toIso8601Local( ZonedDateTime.now() );
            newValidation.details = new Validation.Details();
            newValidation.details.feeId = feeId;
            newValidation.details.validFrom = validFrom;
            newValidation.details.validTo = validTo;
            newValidation.details.userId = NLI_USER_ID;
            newValidation.details.fromZoneId = zoneFrom;
            newValidation.details.toZoneId = zoneTo;

            application.getValidationsTable().insert( newValidation );

            // Registrazione validazione avvenuta con successo, forza upload tabelle immediato
            if( dbSyncServiceBound && dbSyncService != null )
            {
                int maxDays = application.getMaxDaysToStoreDBData();
                String thresholdDateToDelete = application.toIso8601Local( ZonedDateTime.now().minusDays( maxDays ) );

                dbSyncService.uploadValidationsTable( thresholdDateToDelete );
            }

            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    hideProgressBar();
                }
            } );

        } ).start();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void restartCameraPreview()
    {
        binding.validazioneControlloTicketView.cameraSurfaceView.pause();
        binding.validazioneControlloTicketView.cameraSurfaceView.resume();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_Message( String message )
    {
        Dialog dlg = new Dialog( this, android.R.style.Theme_Translucent_NoTitleBar );

        dlg.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dlg.setContentView( R.layout.dialog_message );

        TextView messageTextView = dlg.findViewById( R.id.message_textView );
        messageTextView.setText( message );

        Button closeButton = dlg.findViewById( R.id.close_button );

        closeButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                dlg.dismiss();

                processingQRCode = false; // mmh
            }

        } );

        dlg.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void startDBSyncService()
    {
        Intent startIntent = new Intent( this, DBSyncService.class );

        startIntent.setAction( DBSyncService.ACTION_START );
        ContextCompat.startForegroundService( this, startIntent );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void resumeDBSyncService()
    {
        Intent pauseIntent = new Intent( this, DBSyncService.class );

        pauseIntent.setAction( DBSyncService.ACTION_RESUME );
        ContextCompat.startForegroundService( this, pauseIntent );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void pauseDBSyncService()
    {
        Intent pauseIntent = new Intent( this, DBSyncService.class );

        pauseIntent.setAction( DBSyncService.ACTION_PAUSE );
        ContextCompat.startForegroundService( this, pauseIntent );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void bindToDBSyncService()
    {
        Intent intent = new Intent( this, DBSyncService.class );

        bindService( intent, dbSyncServiceConnection, Context.BIND_AUTO_CREATE );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceConnection dbSyncServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected( ComponentName name, IBinder service )
        {
            DBSyncService.LocalBinder binder = (DBSyncService.LocalBinder)service;
            dbSyncService = binder.getService();
            dbSyncServiceBound = true;
        }

        @Override
        public void onServiceDisconnected( ComponentName name )
        {
            dbSyncServiceBound = false;
        }

    };

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private DBSyncService dbSyncService = null;
    private boolean dbSyncServiceBound = false;

    private Zone currentZone = null;

    private String CB2Command = "";
    private String CB2CommandUUID = "";

    private Dialog dlgProgressoStampa = null;
    private Dialog riepilogoDlg = null;

    private boolean bNfcReadingActive = false;
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] intentFilters;
    private NLITicketApplication application;
    private ActivityMainBinding binding;
    private NavHostFragment fragmentNavigatorHost = null;
    private NavController fragmentNavigatorController = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location gpsLocation;

    private Fee tipologiaSelezionata = null;
    private Fare tariffaTipologiaSelezionata = null;
    private List<Fee> tipologieAccessorie = new ArrayList<>();
    private List<Fare> tariffeAccessorie = new ArrayList<>();
    private int zonaPartenza = 0;
    private int zonaArrivo = 0;
    private String zonaPartenzaDescr = "";
    private String zonaArrivoDescr = "";

    private final int CARRELLO_EMISSIONE_SINGOLA = 111;
    private final int CARRELLO_NORMALE = 222;

    private int tipoCarrello = CARRELLO_EMISSIONE_SINGOLA;
    private int importoTotale = 0;
    private Dialog carrelloDlg = null;
    private boolean printing = false;
    private String lastPrintError = "";
    private int printQueueInitialSize = 0;
    public HashMap<Integer, PrintQueueData.VatInfo> orderVatTable = new HashMap<Integer, PrintQueueData.VatInfo>();
    private String printOrderUUID = "";
    private String lastPrintedIssueUUID = "";
    private String printQueueId = ""; // identifica coda corrente (usare il paymendtID)
    private Deque<PrintQueueData> printQueue = new LinkedList<>();
    private PrintQueueData currentPrintedItem = null;


    private final int MODALITA_VALIDAZIONE_TICKET = 222;
    private final int MODALITA_CONTROLLO_TICKET = 333;

    private int modalitaViewValidazioneControllo = MODALITA_VALIDAZIONE_TICKET;
    private List<String> zoneFermate = new ArrayList<>();
    private List<Zone> listaZone = new ArrayList<>();
    private boolean processingQRCode = false;
    private Zone ticketZonaDa = null;
    private Zone ticketZonaA = null;

    private final int ZONA_A = 1;
    private final int ZONA_B = 2;
    private final int ZONA_C = 3;
    private final int ZONA_D = 4;
    private final int ZONA_E = 5;
    private final int ZONA_TUTTE = 6;
    private final int LINE_WIDTH_NRM = 27;
    private final int LINE_WIDTH_SML = 37;

    private final int NO_ERROR = 0;
    private final int SEARCH_TICKET_BAD_RESPONSE = 10;
    private final int SEARCH_TICKET_EXCEPTION = 20;
    private final int INVALID_DOMAIN_OR_USERID = 30;
    private final int EXCEPTION = 40;

    public final static String TICKET_HEADER = "NLI01";
    public final static int NLI_DOMAIN_ID = 8; // valore fisso 8 = Sistema NLI
    public final static int NLI_USER_ID = 2;   // valore fisso 2 = Azienda NLI
    public final static String SECRET = "c306afe820c3a7e5"; // valore fisso


}