package it.divitech.nliticketapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.divitech.nliticketapp.R;
import it.divitech.nliticketapp.data.login.User;

public class UserTagAdapter extends ArrayAdapter<User.UserTag>
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public UserTagAdapter( Context context, List<User.UserTag> userTags )
    {
        super( context, 0, userTags );

        this.context = context;
        this.userTagList = userTags;

        //setDropDownViewResource( R.layout.spinner_dropdown_item );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
            convertView = LayoutInflater.from( context ).inflate( R.layout.spinner_item_dati_turno, parent, false );

        TextView textView = (TextView)convertView.findViewById( R.id.spinner_item_text );
        User.UserTag item = userTagList.get( position );

        textView.setText( item.label /*+ " / " + key*/ );

        return convertView;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public View getDropDownView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
            convertView = LayoutInflater.from( context ).inflate( R.layout.spinner_dropdown_item, parent, false );

        TextView textView = (TextView)convertView.findViewById( R.id.spinner_item_text );
        User.UserTag item = userTagList.get( position );

        textView.setText( item.label );

        return convertView;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public int getCount()
    {
        return userTagList.size();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private Context context;
    private List<User.UserTag> userTagList = new ArrayList<>();
}
