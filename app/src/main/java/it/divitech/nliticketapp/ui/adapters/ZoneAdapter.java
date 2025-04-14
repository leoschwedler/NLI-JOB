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
import it.divitech.nliticketapp.data.ticketing.Zone;

public class ZoneAdapter extends ArrayAdapter<Zone>
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ZoneAdapter( Context context, List<Zone> zones )
    {
        super( context, 0, zones );

        this.context = context;
        this.zones = zones;

        //setDropDownViewResource( R.layout.spinner_dropdown_item );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
            convertView = LayoutInflater.from( context ).inflate( R.layout.spinner_item_dati_fermata, parent, false );

        TextView textView = convertView.findViewById( R.id.spinner_item_text );

        if( position >= 0 && position < zones.size() )
        {
            textView.setText( zones.get( position ).label.toUpperCase() + " / " + zones.get( position ).description );
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

        if( position >= 0 && position < zones.size() )
        {
            textView.setText( zones.get( position ).label.toUpperCase() + " / " + zones.get( position ).description );
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
        return zones.size();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getPositionById( int id )
    {
        for( int i = 0; i < zones.size(); i++ )
        {
            if( zones.get( i ).id == id )
                return i;
        }

        return -1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Context context;
    private List<Zone> zones = new ArrayList<>();
}
