package it.divitech.nliticketapp.ui.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.divitech.nliticketapp.R;

public class QuantitativoTitoloViaggioView extends LinearLayout
{

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitativoTitoloViaggioView( Context context )
    {
        super( context );

        init( context, null );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitativoTitoloViaggioView( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitativoTitoloViaggioView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void init( Context context, AttributeSet attrs )
    {
        LayoutInflater.from( context ).inflate( R.layout.view_quantitativo_titolo_viaggio, this, true );

        labelTextView = findViewById( R.id.label_textView );
        label2TextView = findViewById( R.id.label2_textView );
        quantitySelector = findViewById( R.id.quantita_View );

        labelTextView.setText( "Abcd");
        label2TextView.setText( "123");

        quantitySelector.setRange( 0, 999 );
        quantitySelector.setValue( 0 );

        hideLabel2();

        // custom attributes
        if( attrs != null )
        {
            TypedArray attributes = context.getTheme().obtainStyledAttributes( attrs, R.styleable.quantitySelector, 0, 0 );

            int minQuantity = attributes.getInt( R.styleable.quantitySelector_minQuantity, 0 );
            int maxQuantity = attributes.getInt( R.styleable.quantitySelector_maxQuantity, 100 );
            int quantity = attributes.getInt( R.styleable.quantitySelector_initialQuantity, 0 );
            String label = attributes.getString( R.styleable.quantitySelector_label );

            quantitySelector.setRange( minQuantity, maxQuantity );
            quantitySelector.setValue( quantity );
            labelTextView.setText( label );
        }

        // Value changed event
        quantitySelector.setOnValueChangedListener( new QuantitySelectorView.OnValueChangedListener()
        {
            @Override
            public void onValueChanged( View view, int newValue, Object extraData )
            {
                if( onValueChangedListener != null )
                    onValueChangedListener.onValueChanged( view, newValue, extraData /*getLabel()*/ );
            }

        } );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setLabel( String newLabel )
    {
        labelTextView.setText( newLabel );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getLabel()
    {
        return labelTextView.getText().toString();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void showLabel2()
    {
        label2TextView.setVisibility( View.VISIBLE );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void hideLabel2()
    {
        label2TextView.setVisibility( View.GONE );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setLabel2( String newLabel )
    {
        label2TextView.setText( newLabel );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getLabel2()
    {
        return label2TextView.getText().toString();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setValue( int newVal )
    {
        quantitySelector.setValue( newVal );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setMinValue( int newVal )
    {
        quantitySelector.setMinValue( newVal );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setMaxValue( int newVal )
    {
        quantitySelector.setMaxValue( newVal );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getValue()
    {
        return quantitySelector.getValue();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitySelectorView getQuantitySelector()
    {
        return quantitySelector;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setOnValueChangedListener( QuantitySelectorView.OnValueChangedListener listener )
    {
        this.onValueChangedListener = listener;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private QuantitySelectorView.OnValueChangedListener onValueChangedListener;
    private TextView labelTextView;
    private TextView label2TextView;
    private QuantitySelectorView quantitySelector;

}
