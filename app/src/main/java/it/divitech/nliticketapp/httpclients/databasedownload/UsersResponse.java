package it.divitech.nliticketapp.httpclients.databasedownload;

import java.util.ArrayList;
import java.util.List;

import it.divitech.nliticketapp.data.login.User;

public class UsersResponse
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public UsersResponse()
    {
        entity = "";
        version = "";
        data = new ArrayList<>();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String entity;
    public String version;
    public List<User> data;
}
