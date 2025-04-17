package it.divitech.nliticketapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

import androidx.room.Room;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usdk.apiservice.aidl.printer.PrinterError;
import com.usdk.apiservice.aidl.vectorprinter.UVectorPrinter;
import com.usdk.apiservice.aidl.vectorprinter.VectorPrinterData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import it.divitech.nliticketapp.data.carrello.ShoppingCart;
import it.divitech.nliticketapp.data.login.User;
import it.divitech.nliticketapp.data.session.InnerSession;
import it.divitech.nliticketapp.data.session.OperatorSession;
import it.divitech.nliticketapp.data.ticketing.Fee;
import it.divitech.nliticketapp.database.DeviceSettingsDAO;
import it.divitech.nliticketapp.database.FareDAO;
import it.divitech.nliticketapp.database.FeeDAO;
import it.divitech.nliticketapp.database.IssueDAO;
import it.divitech.nliticketapp.database.OperatorSessionDAO;
import it.divitech.nliticketapp.database.PaymentDAO;
import it.divitech.nliticketapp.database.StopDAO;
import it.divitech.nliticketapp.database.TecBusDB;
import it.divitech.nliticketapp.database.UsersDAO;
import it.divitech.nliticketapp.database.ValidationDAO;
import it.divitech.nliticketapp.database.ZoneDAO;
import it.divitech.nliticketapp.helpers.DeviceHelper;

import it.divitech.nliticketapp.helpers.PrinterHelper;
import it.divitech.nliticketapp.httpclients.TecBusApiClient;
import it.divitech.nliticketapp.httpclients.databasedownload.FaresResponse;
import it.divitech.nliticketapp.httpclients.databasedownload.FeesResponse;
import it.divitech.nliticketapp.httpclients.databasedownload.SettingsResponse;
import it.divitech.nliticketapp.httpclients.databasedownload.UsersResponse;
import it.divitech.nliticketapp.httpclients.databasedownload.ZonesResponse;

import retrofit2.Call;
import retrofit2.Response;

public class NLITicketApplication extends Application {
    //-----------------------------------------------------------------------------------------------------------------------------------------
    private PrinterHelper printerHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        setDBSyncPause(true);
        printerHelper = new PrinterHelper(this);
        printerHelper.bind();

        //---

        // Se non esiste una copia 'privata' della lista degli utenti, creala dagli assets.
        // La copia 'privata' serve per inizializzare il DB al primo avvio
        createUsersPrivateFile();

        initDB();

        DeviceHelper.getInstance().init(this);
        DeviceHelper.getInstance().bindService();

        // TEST LOGIN: userid "777", pwd "pippo" --> Hash = 7db1338373c48d9f5d03fa45912c140dcc6352aebd70ee1c61b4155f9aa47321
        // String tmp = generateLoginPasswordHash( "pippo", "777", "522e80e493112e1d" );

        TecBusApiClient.setBaseUrl(TECBUS_API_BASE_URL);

        setDBSyncPause(false);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public PrinterHelper getPrinterHelper() {
        return printerHelper;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getPaddedText(String label, String value, int maxLength) {
        String result = "";
        int spaceBetween = maxLength - label.length() - value.length();

        if (spaceBetween < 0) {
            //spaceBetween = 0;
            // result = label + "\n" + value + "\n";

            String valuePadded = getPaddedText("", value, maxLength);
            result = label + "\n" + valuePadded;
        } else {
            result = label + " ".repeat(spaceBetween) + value + "\n";
        }

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setTextToPrint(String text, int alignment, int size, boolean bold) {
        try {
            if (vectorPrinter.getStatus() == PrinterError.SUCCESS) {
                Bundle normalFormat = new Bundle();
                Bundle boldFormat = new Bundle();

                normalFormat.putInt(VectorPrinterData.ALIGNMENT, alignment);
                normalFormat.putInt(VectorPrinterData.TEXT_SIZE, size);
                normalFormat.putBoolean(VectorPrinterData.BOLD, false);

                boldFormat.putInt(VectorPrinterData.ALIGNMENT, alignment);
                boldFormat.putInt(VectorPrinterData.TEXT_SIZE, size);
                boldFormat.putBoolean(VectorPrinterData.BOLD, true);

                if (bold) {
                    vectorPrinter.addText(boldFormat, text);
                } else {
                    vectorPrinter.addText(normalFormat, text);
                }
            }

        } catch (RemoteException e) {

        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void initDB() {
        // Inizializza database (ASYNC)
        new Thread(() ->
        {
            tecbusDB = Room.databaseBuilder(this, TecBusDB.class, "tecbus_database")
                    .fallbackToDestructiveMigration()
                    .build();

            fares = tecbusDB.getFaresDAO();
            fees = tecbusDB.getFeesDAO();
            zones = tecbusDB.getZonesDAO();
            stops = tecbusDB.getStopsDAO();
            users = tecbusDB.getUsersDAO();
            sessions = tecbusDB.getSessionsDAO();
            payments = tecbusDB.getPaymentsDAO();
            issues = tecbusDB.getIssuesDAO();
            deviceSettings = tecbusDB.getDeviceSettingsDAO();
            validations = tecbusDB.getValidationsDAO();

            // Pre load data to save performance
            tipologiePrincipali = getFeesTable().getMainFees();

        }).start();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ValidationDAO getValidationsTable() {
        return validations;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getValidationsTableVersion() {
        return preferences.getString(PREFERENCES_VALIDATIONS_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setValidationsTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_VALIDATIONS_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public FareDAO getFaresTable() {
        return fares;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getFaresTableVersion() {
        return preferences.getString(PREFERENCES_FARES_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setFaresTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_FARES_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkFaresDatabaseUpdate() {
        // NOTA: Chiamare sempre in un thread separato
        Call<FaresResponse> call = TecBusApiClient.getDatabaseDownloadService().getFaresTableUpdate("Bearer " + getAccessToken(), null, getFaresTableVersion());

        try {
            Response response = call.execute();

            if (response.isSuccessful()) {
                FaresResponse faresResponse = (FaresResponse) response.body();

                if (faresResponse != null) {
                    // Salva versione corrente
                    setFaresTableVersion(faresResponse.version);

                    // Crea tabella DB
                    if (faresResponse.data != null) {
                        fares.deleteAll();
                        fares.insert(faresResponse.data);

                        int cnt = fares.getRecordCount();

                        return cnt > 0;
                    }
                }

            } else {
                // Not modified
                if (response.code() == 304)
                    return true;
            }
        } catch (IOException e) {
            int dippoIppo = 0;
            dippoIppo++;
        }

        return getFaresTable().getRecordCount() > 0;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onTerminate() {
        super.onTerminate();
        printerHelper.unbind();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public FeeDAO getFeesTable() {
        return fees;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getFeesTableVersion() {
        return preferences.getString(PREFERENCES_FEES_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setFeesTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_FEES_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkFeesDatabaseUpdate() {
        // NOTA: Chiamare sempre in un thread separato
        Call<FeesResponse> call = TecBusApiClient.getDatabaseDownloadService().getFeesTableUpdate("Bearer " + getAccessToken(), null, getFeesTableVersion());

        try {
            Response response = call.execute();

            if (response.isSuccessful()) {
                FeesResponse feesResponse = (FeesResponse) response.body();

                if (feesResponse != null) {
                    // Salva versione corrente
                    setFeesTableVersion(feesResponse.version);

                    // Crea tabella DB
                    if (feesResponse.data != null) {
                        fees.deleteAll();
                        fees.insert(feesResponse.data);

                        int cnt = fees.getRecordCount();

                        return cnt > 0;
                    }
                }

            } else {
                // Not modified
                if (response.code() == 304)
                    return true;
            }
        } catch (IOException e) {
            int dippoIppo = 0;
            dippoIppo++;
        }

        return getFeesTable().getRecordCount() > 0;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ZoneDAO getZonesTable() {
        return zones;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getZonesTableVersion() {
        return preferences.getString(PREFERENCES_ZONES_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setZonesTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_ZONES_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkZonesDatabaseUpdate() {
        // NOTA: Chiamare sempre in un thread separato
        Call<ZonesResponse> call = TecBusApiClient.getDatabaseDownloadService().getZonesTableUpdate("Bearer " + getAccessToken(), null, getZonesTableVersion());

        try {
            Response response = call.execute();

            if (response.isSuccessful()) {
                ZonesResponse zonesResponse = (ZonesResponse) response.body();

                if (zonesResponse != null) {
                    // Salva versione corrente
                    setZonesTableVersion(zonesResponse.version);

                    // Crea tabella DB
                    if (zonesResponse.data != null) {
                        zones.deleteAll();
                        zones.insert(zonesResponse.data);

                        int cnt = zones.getRecordCount();

                        return cnt > 0;
                    }
                }

            } else {
                // Not modified
                if (response.code() == 304)
                    return true;
            }
        } catch (IOException e) {
            int dippoIppo = 0;
            dippoIppo++;
        }

        return getZonesTable().getRecordCount() > 0;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public StopDAO getStopsTable() {
        return stops;
    } // DEPRECATED?

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public UsersDAO getUsersTable() {
        return users;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getUsersTableVersion() {
        return preferences.getString(PREFERENCES_USERS_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setUsersTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_USERS_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkUsersDatabaseUpdate() {
        // NOTA: Chiamare sempre in un thread separato
        Call<UsersResponse> call = TecBusApiClient.getDatabaseDownloadService().getUsersTableUpdate("Bearer " + getAccessToken(), null, getUsersTableVersion());

        try {
            Response response = call.execute();

            if (response.isSuccessful()) {
                UsersResponse usersResponse = (UsersResponse) response.body();

                if (usersResponse != null) {
                    // Salva versione corrente
                    setUsersTableVersion(usersResponse.version);

                    // Crea tabella DB
                    if (usersResponse.data != null) {
                        users.deleteAll();
                        users.insert(usersResponse.data);

                        int cnt = users.getRecordCount();

                        return cnt > 0;
                    }
                }
            } else {
                // Not modified
                if (response.code() == 304)
                    return true;
            }
        } catch (IOException e) {
            int dippoIppo = 0;
            dippoIppo++;
        }

        return getUsersTable().getRecordCount() > 0;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public DeviceSettingsDAO getDeviceSettingsTable() {
        return deviceSettings;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getSettingsTableVersion() {
        return preferences.getString(PREFERENCES_SETTINGS_VERSION, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setSettingsTableVersion(String version) {
        preferencesEditor.putString(PREFERENCES_SETTINGS_VERSION, version);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkSettingsDatabaseUpdate() {
        // NOTA: Chiamare sempre in un thread separato
        Call<SettingsResponse> call = TecBusApiClient.getDatabaseDownloadService().getSettingsTableUpdate("Bearer " + getAccessToken(), null, getSettingsTableVersion());

        try {
            Response response = call.execute();

            if (response.isSuccessful()) {
                SettingsResponse settingsResponse = (SettingsResponse) response.body();

                if (settingsResponse != null) {
                    // Salva versione corrente
                    setSettingsTableVersion(settingsResponse.version);

                    // Crea tabella DB
                    if (settingsResponse.data != null) {
                        deviceSettings.deleteAll();
                        deviceSettings.insert(settingsResponse.data);

                        int cnt = deviceSettings.getRecordCount();

                        return cnt > 0;
                    }
                }

            } else {
                // Not modified
                if (response.code() == 304)
                    return true;
            }
        } catch (IOException e) {
            int dippoIppo = 0;
            dippoIppo++;
        }

        return true;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public OperatorSessionDAO getSessionsTable() {
        return sessions;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public PaymentDAO getPaymentsTable() {
        return payments;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public IssueDAO getIssuesTable() {
        return issues;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public <T> List<T> loadDataFromJson(Context context, String filename, TypeReference<List<T>> typeReference) {
        try {
            InputStream inputStream = context.getAssets().open(filename);
            InputStreamReader reader = new InputStreamReader(inputStream);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return objectMapper.readValue(reader, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getSecretToken() {
        return preferences.getString(PREFERENCES_SECRET_TOKEN, "");
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void saveSecretToken(String token) {
        preferencesEditor.putString(PREFERENCES_SECRET_TOKEN, token);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getAccessToken() {
        return preferences.getString(PREFERENCES_ACCESS_TOKEN, "");
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void saveAccessToken(String token) {
        preferencesEditor.putString(PREFERENCES_ACCESS_TOKEN, token);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getUnitId() {
        return preferences.getInt(PREFERENCES_UNIT_ID, 0);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void saveUnitId(int unitId) {
        preferencesEditor.putInt(PREFERENCES_UNIT_ID, unitId);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getAndroidID() {
        // NOTA: Android ID è valido solo fino al Factory Reset del device
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getRegistrationToken() {
        return registrationToken;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void saveDeviceUUID(String newUUID) {
        preferencesEditor.putString(PREFERENCES_DEVICE_UUID, newUUID);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String getDeviceUUID() {
        return preferences.getString(PREFERENCES_DEVICE_UUID, "");
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public InnerSession getCurrentSessionInfo() {
        if (innerSession != null)
            return innerSession;

        /*
        String json = preferences.getString( PREFERENCES_SESSION_DATA, "" );

        if( json == null || json.isBlank() )
            return new InnerSession();

        ObjectMapper objMapper = new ObjectMapper();
        objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

        try
        {
            innerSession = objMapper.readValue( json, InnerSession.class );

        }
        catch( IOException e )
        {

        }

        return innerSession;*/

        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void SaveCurrentSessionInfo(InnerSession innerSessionInfo) {
        innerSession = innerSessionInfo;

        /*
        String json = "";
        ObjectMapper objMapper = new ObjectMapper();
        objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

        try
        {
            json = objMapper.writeValueAsString( innerSessionInfo );

        }
        catch( IOException e )
        {

        }

        preferencesEditor.putString( PREFERENCES_SESSION_DATA, json );
        preferencesEditor.commit();*/
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void saveLoginInfo(User newUser, OperatorSession newSession) {
        InnerSession innerSessionInfo = getCurrentSessionInfo();

        if (innerSessionInfo == null)
            innerSessionInfo = new InnerSession();

        if (innerSessionInfo != null) {
            innerSessionInfo.currentSession = newSession;
            innerSessionInfo.currentUser = newUser;

            SaveCurrentSessionInfo(innerSessionInfo);
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public boolean createUsersPrivateFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            File file = new File(getApplicationContext().getFilesDir(), loginUsersFileName);

            if (!file.exists()) {
                InputStream assetStream = getApplicationContext().getAssets().open(loginUsersFileName);
                FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(loginUsersFileName, Context.MODE_PRIVATE);

                // Copia il contenuto del file da assets al file privato
                byte[] buffer = new byte[1024];
                int length = 0;

                while ((length = assetStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                fileOutputStream.close();

                return true;
            }

        } catch (Exception err) {

        }

        return false;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String generateLoginPasswordHash(String password, String loginUserId, String salt) {
        String input = password + "." + loginUserId + "." + salt;
        MessageDigest digest = null;
        StringBuilder hexString = new StringBuilder();

        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

        } catch (NoSuchAlgorithmException e) {

        }

        return hexString.toString();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public List<User.UserTag> getUserListaTurni(String loginId) {
        User user = getUsersTable().getUserByLoginId(loginId);

        if (user != null) {
            if (user.tags != null && !user.tags.isEmpty())
                return user.tags;
        }

        return new ArrayList<>();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void clearCurrentLoggedUserData() {
        saveLoginInfo(null, null);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void hideKeyboard(Activity activity) {
        try {
            if (activity != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null)
                    imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

                activity.getCurrentFocus().clearFocus();
            }

        } catch (Exception err) {

        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static int calculateCRC32(byte[] data) {
        Checksum crc32 = new CRC32();
        crc32.update(data, 0, data.length);

        return (int) crc32.getValue();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static String convertTimestampToDate(long ts) {
        try {
            // Converti il timestamp da secondi a millisecondi
            Date date = new Date(ts * 1000);

            // Imposta il formato desiderato
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

            // Imposta il fuso orario al locale del dispositivo
            sdf.setTimeZone(TimeZone.getDefault());

            // Converte il timestamp in una data leggibile
            return sdf.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void clearCarrelloAcquisti() {
        carrelloAcquisti = null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCart getCarrelloAcquisti() {
        if (carrelloAcquisti == null) {
            carrelloAcquisti = new ShoppingCart();
        }

        /*
        if( carrelloAcquisti == null )
        {
            String json = preferences.getString( PREFERENCES_SHOPPING_CART, "" );

            if( json == null || json.isBlank() )
            {
                carrelloAcquisti = new ShoppingCart();
            }

            ObjectMapper objMapper = new ObjectMapper();
            objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

            try
            {
                carrelloAcquisti = objMapper.readValue( json, ShoppingCart.class );
            }
            catch( IOException e )
            {

            }

        }*/

        return carrelloAcquisti;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void SaveCarrelloAcquisti() {
        String json = "";

        ObjectMapper objMapper = new ObjectMapper();
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            json = objMapper.writeValueAsString(carrelloAcquisti);

        } catch (IOException e) {

        }

        preferencesEditor.putString(PREFERENCES_SHOPPING_CART, json);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public ShoppingCart getCarrelloAcquistiOneItem() {
        if (carrelloAcquistiOneItem == null)
            carrelloAcquistiOneItem = new ShoppingCart();

        return carrelloAcquistiOneItem;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void clearCarrelloAcquistiOneItem() {
        if (carrelloAcquistiOneItem != null)
            carrelloAcquistiOneItem.clear();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getMaxDaysToStoreDBData() {
        return MAX_DAYS_TO_STORE_DB_DATA;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int getGroupsCap() {
        return GROUPS_CAP;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setGroupsCap(int value) {
        GROUPS_CAP = value;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String convertCentesimiInEuro(int cents) {
        return String.format("%.2f€", cents / 100.0);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public String convertNumberToLetter(int number) {
        if (number >= 0 && number <= 25)
            return String.valueOf((char) ('A' + number));

        return "?";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void restartApp(Context context, Class<?> restartActivityClass) {
        Intent intent = new Intent(context, restartActivityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        System.exit(0);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public List<Fee> getTipologiePrincipali() {
        return tipologiePrincipali;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public int calcIvaScorporata(int prezzoCentesimi, int quantita, int aliquotaIVA) {
        double aliquotaIVADec = aliquotaIVA / 100.0;
        int totaleLordo = prezzoCentesimi * quantita;
        double imponibile = totaleLordo / (1.0 + aliquotaIVADec);
        double iva = totaleLordo - imponibile;

        return (int) Math.round(iva);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void setDBSyncPause(boolean pause) {
        preferencesEditor.putBoolean(PREFERENCES_DBSYNC_PAUSED, pause);
        preferencesEditor.commit();
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static String toIso8601Local(ZonedDateTime dateTime) {
        if (dateTime == null)
            return null;

        // ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        return dateTime.format(formatter);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static ZonedDateTime fromIso8601ToLocal(String iso8601String) {
        if (iso8601String == null)
            return null;

        // ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        ZonedDateTime zdt = ZonedDateTime.parse(iso8601String, formatter);
        ZonedDateTime zdtLocal = zdt.withZoneSameInstant(ZoneId.systemDefault());

        return zdtLocal;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public void closeCurrentOperatorSession(boolean forced) {
        OperatorSession lastOpenSession = getSessionsTable().getOpenSession();

        if (lastOpenSession != null) {
            // Force Close session
            lastOpenSession.status = forced ? "F" : "C";
            lastOpenSession.tsClose = toIso8601Local(ZonedDateTime.now());

            getSessionsTable().update(lastOpenSession);
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private ShoppingCart carrelloAcquistiOneItem = null;
    private ShoppingCart carrelloAcquisti = null;

    private TecBusDB tecbusDB = null;
    private FareDAO fares;
    private FeeDAO fees;
    private ZoneDAO zones;
    private UsersDAO users;
    private StopDAO stops;
    private OperatorSessionDAO sessions;
    private PaymentDAO payments;
    private IssueDAO issues;
    private DeviceSettingsDAO deviceSettings;
    private ValidationDAO validations;
    private List<Fee> tipologiePrincipali = new ArrayList<>();

    private UVectorPrinter vectorPrinter = null;

    private InnerSession innerSession = null;

    private String loginUsersFileName = "login_users.json";
    private String zoneFileName = "zones.json";
    private String feesFileName = "fees.json";
    private String faresFileName = "fare_table.json";
    private String stopsFileName = "stops.json";
    private String registrationToken = "Y29OdVVTV25yLTN6RlVRTk5MV2FZR21FeHcwMlNwSlA";
    private String TECBUS_API_BASE_URL = "https://nli.tecbus.dev/api/v1/";

    private int GROUPS_CAP = 10;
    private int MAX_DAYS_TO_STORE_DB_DATA = 3;

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor preferencesEditor = null;

    public static final String PREFERENCES_SESSION_DATA = "SESSION_DATA";
    public static final String PREFERENCES_DEVICE_UUID = "DEVICE_UUID";

    public static final String PREFERENCES_SECRET_TOKEN = "SECRET_TOKEN";
    public static final String PREFERENCES_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String PREFERENCES_UNIT_ID = "UNIT_ID";

    public static final String PREFERENCES_VALIDATIONS_VERSION = "VALIDATIONS_VERSION";
    public static final String PREFERENCES_USERS_VERSION = "USERS_VERSION";
    public static final String PREFERENCES_FEES_VERSION = "FEES_VERSION";
    public static final String PREFERENCES_FARES_VERSION = "FARES_VERSION";
    public static final String PREFERENCES_SETTINGS_VERSION = "SETTINGS_VERSION";
    public static final String PREFERENCES_ZONES_VERSION = "ZONES_VERSION";

    public static final String PREFERENCES_SHOPPING_CART = "SHOPPING_CART";

    public static final String PREFERENCES_DBSYNC_PAUSED = "PREFERENCES_DBSYNC_PAUSED";

    public static final String DEVICE_TYPE = "palm6k";
    public static final String PREFERENCES = "NLITicketApp";

    public static final String TAG = "NLITicketApp";

    // private static final String BASE45_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:;=?@^_";
    // private static final String INVALID_DATE = toIso8601Local( ZonedDateTime.of( 2000, 1, 1, 0, 0, 0, 0, TimeZone.getDefault().toZoneId() ) );
}
