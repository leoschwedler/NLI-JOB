package it.divitech.nliticketapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.divitech.nliticketapp.data.login.User;

@Dao
public interface UsersDAO
{
    @Insert( onConflict = OnConflictStrategy.REPLACE )
    void insert( List<User> list );

    @Query("DELETE FROM users_table")
    void deleteAll();

    @Query( "SELECT COUNT(*) FROM users_table" )
    int getRecordCount();

    @Query( "SELECT * FROM users_table WHERE login_userid = :loginUserId" )
    User getUserByLoginId( String loginUserId );

}
