package it.divitech.nliticketapp.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionsManager
{
    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static void checkPermissions( Activity activity )
    {
        String androidVer = getAndroidVersion();

        if( androidVer.equals( "9" ) )
        {
            checkPermissionsAndroid9( activity );
        }
        else if( androidVer.equals( "10" ) )
        {
            checkPermissionsAndroid10( activity );
        }
        else if( androidVer.equals( "11" ) )
        {
            checkPermissionsAndroid11( activity );
        }
        else if( androidVer.equals( "12" ) )
        {
            checkPermissionsAndroid12( activity );
        }
        else if( androidVer.equals( "13" ) )
        {
            checkPermissionsAndroid13( activity );
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void checkPermissionsAndroid9( Activity activity )
    {
        // Specificare solo i permessi dichiarati nel Manifest
        String[] mandatoryPermissions =
        {
            // Permessi che richiedono esplicita approvazione per A9
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.CHANGE_WIFI_STATE
        };

        askPermissions( activity, mandatoryPermissions );

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void checkPermissionsAndroid10( Activity activity )
    {
        // Specificare solo i permessi dichiarati nel Manifest
        String[] mandatoryPermissions =
        {
            // Permessi che richiedono esplicita approvazione per A10
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
        };

        askPermissions( activity, mandatoryPermissions );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void checkPermissionsAndroid11( Activity activity )
    {
        // Specificare solo i permessi dichiarati nel Manifest
        String[] mandatoryPermissions =
        {
            // Permessi che richiedono esplicita approvazione per A11
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        };

        askPermissions( activity, mandatoryPermissions );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void checkPermissionsAndroid12( Activity activity )
    {
        // Specificare solo i permessi dichiarati nel Manifest
        String[] mandatoryPermissions =
        {
            // Permessi che richiedono esplicita approvazione per A12
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.READ_MEDIA_IMAGES,
            //Manifest.permission.READ_MEDIA_VIDEO,
            //Manifest.permission.READ_MEDIA_AUDIO,
            //Manifest.permission.RECORD_AUDIO,
            //Manifest.permission.CAMERA,
            //Manifest.permission.BODY_SENSORS,
        };

        askPermissions( activity, mandatoryPermissions );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void checkPermissionsAndroid13( Activity activity )
    {
        // Specificare solo i permessi dichiarati nel Manifest
        String[] mandatoryPermissions =
        {
            // Permessi che richiedono esplicita approvazione per A13
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.NEARBY_WIFI_DEVICES,
            //Manifest.permission.READ_MEDIA_IMAGES,
            //Manifest.permission.READ_MEDIA_VIDEO,
            //Manifest.permission.READ_MEDIA_AUDIO,
            //Manifest.permission.POST_NOTIFICATIONS,
        };

        askPermissions( activity, mandatoryPermissions );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static void askPermissions( Activity activity, String[] permissions )
    {
        boolean permissionsDenied = false;

        for( String permission : permissions )
        {
            if( ActivityCompat.checkSelfPermission( activity, permission ) != PackageManager.PERMISSION_GRANTED )
            {
                permissionsDenied = true;
                break;
            }
        }

        if( permissionsDenied )
        {
            ActivityCompat.requestPermissions( activity, permissions, PERMISSION_REQUEST_CODE );
        }
        else if( permissionsCallback != null )
        {
            permissionsCallback.onAllPermissionsGranted();
        }

    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static void handlePermissionResult( Activity activity, int requestCode, String[] permissions, int[] grantResults )
    {
        if( permissions == null || permissions.length == 0 )
            return;

        if( requestCode == PERMISSION_REQUEST_CODE )
        {
            boolean allGranted = true;
            String permission = "";

            for( int i = 0; i < grantResults.length; i++ )
            {
                if( grantResults[ i ] != PackageManager.PERMISSION_GRANTED )
                {
                    allGranted = false;
                    permission = permissions[ i ];
                    break;
                }
            }

            if( permissionsCallback != null )
            {
                if( allGranted )
                {
                    permissionsCallback.onAllPermissionsGranted();
                }
                else
                {
                    permissionsCallback.onPermissionDenied( permission );
                }
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static String getAndroidVersion()
    {
        switch( Build.VERSION.SDK_INT )
        {
            case Build.VERSION_CODES.P:
                return "9";

            case Build.VERSION_CODES.Q:
                return "10";

            case Build.VERSION_CODES.R:
                return "11";

            case Build.VERSION_CODES.S:
                return "12";

            case Build.VERSION_CODES.TIRAMISU:
                return "13";

            default:
                return "10"; // NLI only
        }

        //return "Unknown";
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public static void setPermissionsCallback( PermissionsCallback callback )
    {
        permissionsCallback = callback;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    public interface PermissionsCallback
    {
        void onAllPermissionsGranted();
        void onPermissionDenied( String permission );
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------

    private static final int PERMISSION_REQUEST_CODE = 144;

    private static PermissionsCallback permissionsCallback;

}
