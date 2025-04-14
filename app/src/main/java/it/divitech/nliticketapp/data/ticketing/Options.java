package it.divitech.nliticketapp.data.ticketing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Options
{
    @JsonProperty("validity")
    public Validity validity;

    // Questo è solo un test, dato che Options può contenere tipologie eterogene di oggetti
    @JsonProperty("dummy")
    public DummyObject dummy;
}
