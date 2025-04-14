package it.divitech.nliticketapp.database;

import androidx.room.TypeConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.divitech.nliticketapp.data.login.User;
import it.divitech.nliticketapp.data.ticketing.Fare;
import it.divitech.nliticketapp.data.ticketing.Fee;
import it.divitech.nliticketapp.data.ticketing.Options;
import it.divitech.nliticketapp.data.ticketing.Validation;

public class DBTypeConverter
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromUserTags( List<User.UserTag> userTags )
    {
        String result = "";

        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( userTags );
        }
        catch( Exception err )
        {

        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static List<User.UserTag> toUserTags( String Json )
    {
        List<User.UserTag> result = new ArrayList<>();

        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( Json, new TypeReference<List<User.UserTag>>() { } );
        }
        catch( Exception err )
        {

        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromFeeOptions( Fee.FeeOptions options )
    {
        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( options );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static Fee.FeeOptions toFeeOptions( String json )
    {
        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( json, new TypeReference<Fee.FeeOptions>()
            {
            } );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromFareOptions( Fare.FareOptions options )
    {
        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( options );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static Fare.FareOptions toFareOptions( String json )
    {
        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( json, new TypeReference<Fare.FareOptions>()
            {
            } );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromChildFees( List<Integer> childFees )
    {
        String result = "";

        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( childFees );
        }
        catch( Exception err )
        {

        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static List<Integer> toChildFees( String Json )
    {
        List<Integer> result = new ArrayList<>();

        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( Json, new TypeReference<List<Integer>>() { } );
        }
        catch( Exception err )
        {

        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromValidationDetails( Validation.Details validations )
    {
        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( validations );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static Validation.Details toValidationDetails( String json )
    {
        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( json, new TypeReference<Validation.Details>()
            {
            } );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    /*
    @TypeConverter
    public static String fromSrcMedia( List<String> srcMedia )
    {
        String result = "";

        try
        {
            // Converte in JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.writeValueAsString( srcMedia );
        }
        catch( Exception err )
        {

        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static List<String> toSrcMedia( String Json )
    {
        List<String> result = new ArrayList<>();

        try
        {
            // Deserializza il JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            return objectMapper.readValue( Json, new TypeReference<List<String>>() { } );
        }
        catch( Exception err )
        {

        }

        return result;
    }
    */

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static String fromList( List<String> list )
    {
       try
        {
            ObjectMapper objectMapper = new ObjectMapper();

            return list == null ? null : objectMapper.writeValueAsString( list );
        }
        catch( Exception err )
        {

        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @TypeConverter
    public static List<String> toList( String json )
    {
        if( json == null )
            return null;

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue( json, new TypeReference<List<String>>() {} );
        }
        catch( Exception e )
        {

        }

        return null;
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------

}
