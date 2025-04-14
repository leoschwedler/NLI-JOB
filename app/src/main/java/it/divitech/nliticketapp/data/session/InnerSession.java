package it.divitech.nliticketapp.data.session;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.divitech.nliticketapp.data.login.User;

public class InnerSession
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public InnerSession()
    {
        currentUser = null;
        currentSession = null;
        this.zonaDefaultId = 0;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public InnerSession( InnerSession other )
    {
        this.currentUser = other.currentUser;
        this.currentSession = other.currentSession;
        this.zonaDefaultId = other.zonaDefaultId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public User currentUser;
    public OperatorSession currentSession;
    public int zonaDefaultId;
}