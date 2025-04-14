package it.divitech.nliticketapp.ui.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.divitech.nliticketapp.R;


public class QuantitySelectorView extends LinearLayout
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitySelectorView( Context context )
    {
        super( context );

        init( context, null );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitySelectorView( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public QuantitySelectorView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        init( context, attrs );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void init( Context context, AttributeSet attrs )
    {
        LayoutInflater.from( context ).inflate( R.layout.view_quantity_selector, this, true );

        decreaseQuantityButton = findViewById( R.id.decreaseBtn );
        increaseQuantityButton = findViewById( R.id.increaseBtn );
        quantityTextView = findViewById( R.id.tvQuantitaValue );

        // custom attributes
        if( attrs != null )
        {
            TypedArray attributes = context.getTheme().obtainStyledAttributes( attrs, R.styleable.quantitySelector, 0, 0 );

            minQuantity = attributes.getInt( R.styleable.quantitySelector_minQuantity, 0 );
            maxQuantity = attributes.getInt( R.styleable.quantitySelector_maxQuantity, 100 );
            quantity = attributes.getInt( R.styleable.quantitySelector_initialQuantity, 1 );
        }

        quantityTextView.setText( String.valueOf( quantity ) );

        decreaseQuantityButton.setOnClickListener( new OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                decreaseQuantity();
            }
        } );

        increaseQuantityButton.setOnClickListener( new OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                increaseQuantity();
            }
        } );

        quantityTextView.addTextChangedListener( new TextWatcher()
        {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {

            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {
                int value = 0;

                try
                {
                    if( s != null && s.length() > 0 )
                        value = Integer.parseInt( s.toString() );

                    quantity = value;

                    if( onValueChangedListener != null )
                        onValueChangedListener.onValueChanged( QuantitySelectorView.this, quantity, extraData );
                }
                catch( NumberFormatException e )
                {

                }

            }

            @Override
            public void afterTextChanged( Editable s )
            {

            }

        } );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void increaseQuantity()
    {
        quantity += 1;

        if( quantity > maxQuantity )
            quantity = maxQuantity;

        quantityTextView.setText( String.valueOf( quantity ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void decreaseQuantity()
    {
        quantity -= 1;

        if( quantity < minQuantity )
            quantity = minQuantity;

        quantityTextView.setText( String.valueOf( quantity ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private void updateQuantity( int delta )
    {
        quantity += delta;

        if( quantity < minQuantity )
            quantity = minQuantity;

        if( quantity > maxQuantity )
            quantity = maxQuantity;

        quantityTextView.setText( String.valueOf( quantity ) );

        //if( onValueChangedListener != null )
        //    onValueChangedListener.onValueChanged( this, quantity, extraData, ( delta < 0 ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setRange( int min, int max )
    {
        minQuantity = min;
        maxQuantity = max;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setMinValue( int min )
    {
        minQuantity = min;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setMaxValue( int max )
    {
        maxQuantity = max;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setValue( int newValue )
    {
        quantity = newValue;

        quantityTextView.setText( String.valueOf( quantity ) );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getValue()
    {
       return quantity;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setLabelTextSize( float size )
    {
        quantityTextView.setTextSize( size );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setExtraData( Object extraData )
    {
        this.extraData = extraData;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Object getExtraData()
    {
        return extraData;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setOnValueChangedListener( OnValueChangedListener listener )
    {
        this.onValueChangedListener = listener;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public interface OnValueChangedListener
    {
        void onValueChanged( View view, int newValue, Object extraData );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private OnValueChangedListener onValueChangedListener;

    private Button decreaseQuantityButton;
    private Button increaseQuantityButton;
    private EditText quantityTextView;
    private int quantity = 0;
    private int minQuantity = 0;
    private int maxQuantity = 999;
    private Object extraData = null;
}
