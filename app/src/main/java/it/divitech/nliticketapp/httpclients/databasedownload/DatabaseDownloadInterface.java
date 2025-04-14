package it.divitech.nliticketapp.httpclients.databasedownload;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface DatabaseDownloadInterface
{
    @GET( "download/unitloginusers/" )
    Call<UsersResponse> getUsersTableUpdate( @Header("Authorization") String auth, @Query( "user_id" ) Integer companyId, @Query( "client_version" ) String version );

    @GET( "download/fees/" )
    Call<FeesResponse> getFeesTableUpdate( @Header("Authorization") String auth, @Query( "user_id" ) Integer companyId, @Query( "client_version" ) String version );

    @GET( "download/faretable/" )
    Call<FaresResponse> getFaresTableUpdate( @Header("Authorization") String auth, @Query( "user_id" ) Integer companyId, @Query( "client_version" ) String version );

    @GET( "download/zones/" )
    Call<ZonesResponse> getZonesTableUpdate( @Header("Authorization") String auth, @Query( "user_id" ) Integer companyId, @Query( "client_version" ) String version );

    @GET( "download/unitsettings/" )
    Call<SettingsResponse> getSettingsTableUpdate( @Header("Authorization") String auth, @Query( "user_id" ) Integer companyId, @Query( "client_version" ) String version );
}
