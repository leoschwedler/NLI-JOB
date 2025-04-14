package it.divitech.nliticketapp.data.ticketing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DummyObject
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public DummyObject()
    {
        this.test1 = "";
        this.test2 = "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public DummyObject( DummyObject other )
    {
        this.test1 = other.test1;
        this.test2 = other.test2;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty("test1")
    public String test1;

    @JsonProperty("test2")
    public String test2;

}
