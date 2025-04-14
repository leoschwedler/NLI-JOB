package it.divitech.nliticketapp.data.ticketing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Validity
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Validity()
    {
        this.trigger = "";
        this.unit = "";
        this.start = "";
        this.stop = "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public Validity( Validity other )
    {
        this.trigger = other.trigger;
        this.unit = other.unit;
        this.start = other.start;
        this.stop = other.stop;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @JsonProperty("trigger")
    public String trigger;

    @JsonProperty("unit")
    public String unit;

    @JsonProperty("start")
    public String start;

    @JsonProperty("stop")
    public String stop;
}
