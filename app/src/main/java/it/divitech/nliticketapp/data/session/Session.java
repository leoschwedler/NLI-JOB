package it.divitech.nliticketapp.data.session;

public class Session
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Session()
    {
        this.loginUserId = "";
        this.password = "";
        this.turno = "";
        this.zonaId = -1;
        this.zonaDefaultId = -1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Session( Session other )
    {
        this.loginUserId = other.loginUserId;
        this.password = other.password;
        this.turno = other.turno;
        this.zonaId = other.zonaId;
        this.zonaDefaultId = other.zonaDefaultId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String loginUserId;
    public String password;
    public String turno;
    public int zonaId;
    public int zonaDefaultId;

}