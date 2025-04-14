package it.divitech.nliticketapp.ui.activities.settings;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.usdk.apiservice.aidl.printer.AlignMode;
import com.usdk.apiservice.aidl.printer.ECLevel;
import com.usdk.apiservice.aidl.printer.OnPrintListener;
import com.usdk.apiservice.aidl.printer.PrintFormat;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.databinding.ActivityLoginBinding;
import it.divitech.nliticketapp.databinding.ActivitySettingsBinding;
import it.divitech.nliticketapp.helpers.DeviceHelper;
import it.divitech.nliticketapp.ui.activities.login.LoginActivity;

public class SettingsActivity extends AppCompatActivity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        application = (NLITicketApplication)getApplication();
        binding = ActivitySettingsBinding.inflate( getLayoutInflater() );

        setContentView( binding.getRoot() );

        binding.testStampanteImmagineButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                byte[] imageData = DeviceHelper.convertBitmapToByteArray( BitmapFactory.decodeResource( getResources(), R.drawable.logo_splash_3 ) );

                try
                {
                    DeviceHelper.getInstance().getPrinter().addImage( AlignMode.CENTER, imageData );
                    DeviceHelper.getInstance().getPrinter().feedLine( 3 );

                    print();
                }
                catch( RemoteException e )
                {

                }

            }
        } );

        binding.testStampanteQrcodeButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                try
                {
                    DeviceHelper.getInstance().getPrinter().addQrCode( AlignMode.CENTER, 240, ECLevel.ECLEVEL_H, "TI SALUTA STO PAZZO" );
                    DeviceHelper.getInstance().getPrinter().feedLine( 3 );

                    print();
                }
                catch( RemoteException e )
                {

                }

            }
        } );

        binding.testStampanteTestButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                try
                {
                    DeviceHelper.getInstance().getPrinter().setPrintFormat( PrintFormat.FORMAT_FONTMODE, PrintFormat.VALUE_FONTMODE_BOLDLEVEL2 );
                    DeviceHelper.getInstance().getPrinter().addText( AlignMode.CENTER, "TEST TITLE Abcdeghijkl" );

                    DeviceHelper.getInstance().getPrinter().setPrintFormat( PrintFormat.FORMAT_FONTMODE, PrintFormat.VALUE_FONTMODE_BOLDLEVEL1 );
                    DeviceHelper.getInstance().getPrinter().addText( AlignMode.CENTER, "TEST TITLE Abcdeghijkl" );

                    DeviceHelper.getInstance().getPrinter().setPrintFormat( PrintFormat.FORMAT_FONTMODE, PrintFormat.VALUE_FONTMODE_DEFAULT );
                    DeviceHelper.getInstance().getPrinter().addText( AlignMode.CENTER, "TEST TEXT Abcdeghijkl" );

                    DeviceHelper.getInstance().getPrinter().setPrintFormat( PrintFormat.FORMAT_FONTMODE, PrintFormat.VALUE_FONTMODE_BOLDLEVEL1 );
                    DeviceHelper.getInstance().getPrinter().addText( AlignMode.CENTER, "------------------------" );

                    DeviceHelper.getInstance().getPrinter().feedLine( 5 );
                }
                catch( RemoteException e )
                {

                }

                print();
            }
        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void print()
    {
        try
        {
            DeviceHelper.getInstance().getPrinter().startPrint( new OnPrintListener()
            {
                @Override
                public void onFinish() throws RemoteException
                {

                }

                @Override
                public void onError( int i ) throws RemoteException
                {
                    runOnUiThread( ()->
                    {
                        Toast.makeText( SettingsActivity.this, "ERRORE DI STAMPA:" + DeviceHelper.getInstance().getErrorDetail( i ), Toast.LENGTH_LONG ).show();
                    } );
                }

                @Override
                public IBinder asBinder()
                {
                    return null;
                }

            } );

        }
        catch( RemoteException e )
        {

        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private NLITicketApplication application;
    private ActivitySettingsBinding binding;
}