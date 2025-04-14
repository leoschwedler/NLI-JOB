package it.divitech.nliticketapp.data.carrello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;

public class ShoppingCart
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCart()
    {
        init();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void init()
    {
        clear();

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void clear()
    {
        orderUUID = "";

        items.clear();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public List<ShoppingCartItem> getItems()
    {
        return items;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int addItem( ShoppingCartItem newItem )
    {
        items.add( newItem );

        return items.size() - 1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean removeItem( ShoppingCartItem newItem )
    {
        return items.remove( newItem );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCartItem getItemAt( int index )
    {
        return items.get( index );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int calculateTotalAmount( int grpCap )
    {
        int total = 0;

        for( ShoppingCartItem item : items )
        {
            total += item.mainIssue.value * item.mainIssue.quantity;

            // Le tariffe MI vanno aggiunte a tutti i passeggeri
            for( ShoppingCartItem.IssueLight child : item.childrenIssues )
            {
                int childTotal = child.value * child.quantity;

                if( child.feeDescription.equalsIgnoreCase( "Tassa MI" ) )
                    childTotal *= item.mainIssue.quantity;

                total += childTotal;
            }

        }

        return total;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String orderUUID = "";

    private List<ShoppingCartItem> items = new ArrayList<>();

}
