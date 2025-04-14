package it.divitech.nliticketapp.httpclients.databasedownload;

import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.data.ticketing.Zone;

public class ZonesResponse
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ZonesResponse()
    {
        entity = "";
        version = "";
        data = new ArrayList<>();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String entity;
    public String version;
    public List<Zone> data;
}
