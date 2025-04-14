package it.divitech.nliticketapp.ui.controls;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import it.divitech.nliticketapp.NLITicketApplication;
import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.carrello.ShoppingCart;
import it.divitech.nliticketapp.data.carrello.ShoppingCartItem;

public class CarrelloView extends ConstraintLayout
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public CarrelloView( Context context )
    {
        super( context );

        init( context, null );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public CarrelloView( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public CarrelloView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void init( Context context, AttributeSet attrs )
    {
        LayoutInflater.from( context ).inflate( R.layout.view_carrello, this, true );

        application = (NLITicketApplication)context.getApplicationContext();

        listaTipologieLinearLayout = findViewById( R.id.listaTipologie_LinearLayout );
        totaleTextView = findViewById( R.id.totale_textView );
        totaleTextView.setText( "" );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setCarrelloAcquisti( ShoppingCart carrello, Activity mainActivity )
    {
        this.carrelloAcquisti = carrello;
        this.mainActivity = mainActivity;

        updateListaTipologie();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateListaTipologie()
    {
        listaTipologieLinearLayout.setLayoutTransition( null );
        listaTipologieLinearLayout.removeAllViews();

        if( carrelloAcquisti != null )
        {
            for( ShoppingCartItem cartItem : carrelloAcquisti.getItems() )
            {
                if( cartItem.mainIssue == null /*|| cartItem.mainIssue.quantity == 0*/ )
                    continue;

                // TIPOLOGIA PRINCIPALE
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View view = inflater.inflate( R.layout.view_carrello_item, null );

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
                params.setMargins( 24, 12, 24, 0 );

                QuantitativoTitoloViaggioView tipologiaPrincipaleControl = view.findViewById( R.id.tipologiaPrincipale_Control );
                LinearLayout tipologieAccessorieLinearLayout = view.findViewById( R.id.tipologieAccessorie_LinearLayout );

                String descr = cartItem.mainIssue.feeDescription;

                if( cartItem.mainIssue.fromZoneId > 0 && cartItem.mainIssue.toZoneId > 0 )
                    descr += " (Zone " + cartItem.mainIssue.fromZoneLabel + "-" + cartItem.mainIssue.toZoneLabel + ")";

                tipologiaPrincipaleControl.setLabel( descr );
                tipologiaPrincipaleControl.setValue( cartItem.mainIssue.quantity );
                tipologiaPrincipaleControl.getQuantitySelector().setExtraData( cartItem );
                tipologiaPrincipaleControl.showLabel2();
                tipologiaPrincipaleControl.setLabel2( "" + application.convertCentesimiInEuro( cartItem.mainIssue.value ) );

                // Listener TIPOLOGIA PRINCIPALE
                tipologiaPrincipaleControl.setOnValueChangedListener( new QuantitySelectorView.OnValueChangedListener()
                {
                    @Override
                    public void onValueChanged( View view, int newValue, Object extraData )
                    {
                        ShoppingCartItem selectedCartItem = (ShoppingCartItem)extraData;
                        ShoppingCartItem.IssueLight selectedIssue = selectedCartItem.mainIssue;
                        selectedIssue.quantity = newValue;

                        if( newValue == 0 )
                            showDialog_ConfermaEliminazioneTariffaPrincipale( selectedCartItem );

                        updateListaTipologie();
                        CalcTotalAmount();
                    }
                } );

                //---

                // TIPOLOGIE ACCESSORIE
                tipologieAccessorieLinearLayout.setLayoutTransition( null );
                tipologieAccessorieLinearLayout.removeAllViews();

                for( int i = 0; i < cartItem.childrenIssues.size(); i++ )
                {
                    QuantitativoTitoloViaggioView accView = new QuantitativoTitoloViaggioView( getContext() );

                    accView.setLabel( cartItem.childrenIssues.get( i ).feeDescription );
                    accView.setValue( cartItem.childrenIssues.get( i ).quantity );
                    accView.getQuantitySelector().setExtraData( cartItem.childrenIssues.get( i ) );
                    accView.showLabel2();
                    accView.setLabel2( "" + application.convertCentesimiInEuro( cartItem.childrenIssues.get( i ).value ) );

                    // Listener TIPOLOGIE ACCESSORIE
                    accView.setOnValueChangedListener( new QuantitySelectorView.OnValueChangedListener()
                    {
                        @Override
                        public void onValueChanged( View view, int newValue, Object extraData )
                        {
                            ShoppingCartItem.IssueLight modifiedIssue = (ShoppingCartItem.IssueLight)accView.getQuantitySelector().getExtraData();
                            modifiedIssue.quantity = newValue;

                            if( newValue == 0 )
                                cartItem.childrenIssues.remove( modifiedIssue );

                            updateListaTipologie();
                            CalcTotalAmount();
                        }
                    } );

                    tipologieAccessorieLinearLayout.addView( accView );
                }

                view.setLayoutParams( params );

                listaTipologieLinearLayout.addView( view );

                tipologieAccessorieLinearLayout.setLayoutTransition( new LayoutTransition() );
            }

            CalcTotalAmount();
        }

        listaTipologieLinearLayout.setLayoutTransition( new LayoutTransition() );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void CalcTotalAmount()
    {
        totaleTextView.setText( "Totale: " +  application.convertCentesimiInEuro( carrelloAcquisti.calculateTotalAmount( application.getGroupsCap() ) ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void showDialog_ConfermaEliminazioneTariffaPrincipale( ShoppingCartItem cartItem )
    {
        AlertDialog.Builder dlgBld = new AlertDialog.Builder( mainActivity );
        dlgBld.setTitle( "Annulla emissione Documento di Viaggio" );
        dlgBld.setIcon( android.R.drawable.ic_dialog_alert );
        dlgBld.setMessage( "Cancellare definitivamente?" );

        dlgBld.setPositiveButton( "Sì", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                dialog.dismiss();

                if( cartItem != null )
                    carrelloAcquisti.removeItem( cartItem );

                updateListaTipologie();
                CalcTotalAmount();
            }

        } );

        dlgBld.setNegativeButton( "No", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                dialog.dismiss();

                ShoppingCartItem.IssueLight selectedIssue = cartItem.mainIssue;
                selectedIssue.quantity = 1;

                updateListaTipologie();
                CalcTotalAmount();
            }
        } );

        dlgBld.show();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Activity mainActivity = null;
    private NLITicketApplication application;
    private TextView totaleTextView = null;
    private LinearLayout listaTipologieLinearLayout = null;
    private ShoppingCart carrelloAcquisti = null;
}
