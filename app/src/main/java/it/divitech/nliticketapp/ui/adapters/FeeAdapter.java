package it.divitech.nliticketapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.ticketing.Fee;

public class FeeAdapter extends ArrayAdapter<Fee>
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public FeeAdapter( Context context, List<Fee> fees )
    {
        super( context, 0, fees );

        this.context = context;
        this.fees = fees;

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
            convertView = LayoutInflater.from( context ).inflate( R.layout.spinner_item_dati_tipo_ticket, parent, false );

        TextView textView = convertView.findViewById( R.id.spinner_item_text );

        if( position >= 0 && position < fees.size() )
        {
            textView.setText( fees.get( position ).description );
        }
        else
        {
            textView.setText( "" );
        }

        return convertView;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public View getDropDownView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
            convertView = LayoutInflater.from( context ).inflate( R.layout.spinner_dropdown_item, parent, false );

        TextView textView = convertView.findViewById( R.id.spinner_item_text );

        if( position >= 0 && position < fees.size() )
        {
            textView.setText( fees.get( position ).description );
        }
        else
        {
            textView.setText( "" );
        }

        return convertView;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public int getCount()
    {
        return fees.size();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getPositionById( int id )
    {
        for( int i = 0; i < fees.size(); i++ )
        {
            if( fees.get( i ).id == id )
                return i;
        }

        return -1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Context context;
    private List<Fee> fees = new ArrayList<>();
}
