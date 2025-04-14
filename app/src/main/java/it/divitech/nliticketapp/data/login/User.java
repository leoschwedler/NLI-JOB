package it.divitech.nliticketapp.data.login;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Entity( tableName = "users_table" )
public class User
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public User()
    {
        this.id = -1;
        this.loginUserId = "";
        this.name = "";
        this.agencyId = 0;
        this.userClass = "";
        this.password = "";
        this.salt = "";
        this.groups = "";
        this.tags = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.status = "";
        this.userId = -1;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public User( User other )
    {
        this.id = other.id;
        this.loginUserId = other.loginUserId;
        this.name = other.name;
        this.agencyId = other.agencyId;
        this.userClass = other.userClass;
        this.password = other.password;
        this.salt = other.salt;
        this.groups = other.groups;
        this.tags = other.tags;
        this.permissions = other.permissions;
        this.status = other.status;
        this.userId = other.userId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    // Inner class
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static class UserTag
    {
        @JsonProperty( "id" )
        public String id;

        @JsonProperty( "label" )
        public String label;

        @JsonProperty( "default_zone_id" )
        public int defaultZoneId;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @PrimaryKey
    @JsonProperty( "id" )
    public int id;

    @ColumnInfo( name = "login_userid" )
    @JsonProperty( "login_userid" )
    public String loginUserId;

    @ColumnInfo( name = "name" )
    @JsonProperty( "name" )
    public String name;

    @ColumnInfo( name = "agencyId" )
    @JsonProperty( "agency_id" )
    public int agencyId;

    @ColumnInfo( name = "userClass" )
    @JsonProperty( "class" )
    public String userClass;

    @ColumnInfo( name = "password" )
    @JsonProperty( "password" )
    public String password;

    @ColumnInfo( name = "salt" )
    @JsonProperty( "salt" )
    public String salt;

    @ColumnInfo( name = "groups" )
    @JsonProperty( "groups" )
    public String groups;

    @ColumnInfo( name = "tags" )
    @JsonProperty( "tags" )
    public List<UserTag> tags;

    @ColumnInfo( name = "permissions" )
    @JsonProperty( "permissions" )
    public List<String> permissions;

    @ColumnInfo( name = "status" )
    @JsonProperty( "status" )
    public String status;

    @ColumnInfo( name = "user_id" )
    @JsonProperty( "user_id" )
    public int userId;
}
