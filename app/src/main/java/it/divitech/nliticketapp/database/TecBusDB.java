package it.divitech.nliticketapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.divitech.nliticketapp.data.login.User;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.DeviceSettings;
import it.divitech.nliticketapp.data.ticketing.Fare;
import it.divitech.nliticketapp.data.ticketing.Fee;
import it.divitech.nliticketapp.data.ticketing.Issue;
import it.divitech.nliticketapp.data.ticketing.Payment;
import it.divitech.nliticketapp.data.ticketing.Stop;
import it.divitech.nliticketapp.data.ticketing.Validation;
import it.divitech.nliticketapp.data.ticketing.Zone;

@Database( entities = { Fare.class, Fee.class, Zone.class, Stop.class, User.class, OperatorSession.class, Payment.class, Issue.class, DeviceSettings.class, Validation.class }, version = 1, exportSchema = false )
@TypeConverters( DBTypeConverter.class )
public abstract class TecBusDB extends RoomDatabase
{
    public abstract FareDAO getFaresDAO();
    public abstract FeeDAO getFeesDAO();
    public abstract ZoneDAO getZonesDAO();
    public abstract StopDAO getStopsDAO();
    public abstract UsersDAO getUsersDAO();
    public abstract OperatorSessionDAO getSessionsDAO();
    public abstract PaymentDAO getPaymentsDAO();
    public abstract IssueDAO getIssuesDAO();
    public abstract DeviceSettingsDAO getDeviceSettingsDAO();
    public abstract ValidationDAO getValidationsDAO();

}
