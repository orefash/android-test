package ng.riby.androidtest.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ng.riby.androidtest.database.DatabaseClient;
import ng.riby.androidtest.database.UserLocation;
import ng.riby.androidtest.receiver.ServiceRestarter;


public class BackgroundLocationService extends Service {

    public static final String TAG = BackgroundLocationService.class.getSimpleName();
    Context context = this;
    int stop_flag=0;

    public BackgroundLocationService(Context applicationContext) {
        super();
        Log.i(TAG, "here I am!");
    }

    public BackgroundLocationService() {
    }


    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 60000;
    private static final float LOCATION_DISTANCE = 0;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            storeLocation(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }


    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.PASSIVE_PROVIDER),
                    new LocationListener(LocationManager.GPS_PROVIDER),
                    new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        startForeground(1,new Notification());
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        }catch (Exception ex) {
            Log.d(TAG, "Location fetch error " + ex.getMessage());
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        // send new broadcast when service is destroyed.
        // this broadcast restarts the service.

//        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//        int stopFlag = prefs.getInt("stop_flag", 0);

        if (mLocationManager != null) {
            try {
                for ( LocationListener listener: mLocationListeners
                     ) {
                    mLocationManager.removeUpdates(listener);
                }
                mLocationManager =null;

            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }
        }

        if(stop_flag==0){
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, ServiceRestarter.class);
            this.sendBroadcast(broadcastIntent);
        }else{
            Log.i(TAG, "service stopped");
        }


    }

    public void stopLocationService(){
        stop_flag = 1;
        this.onDestroy();
    }


    List<UserLocation> locationList = null;

    private void storeLocation(final double latitude, final double longitude) {
//        List<UserLocation> locationList = null;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                UserLocation userLocation = new UserLocation();
                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);
                userLocation.setTimestamp(dateFormat.format(date));


                //adding location data to database
                DatabaseClient.getInstance(context).getAppDatabase()
                        .userLocationDao()
                        .insert(userLocation);

//                locationList = DatabaseClient
//                        .getInstance(context)
//                        .getAppDatabase()
//                        .userLocationDao()
//                        .getAll();

                return null;
            }

            @Override
            protected void onPostExecute( final Void result ) {
//                Log.v("Locations: ",locationList.get(0).getTimestamp());
//                Log.v("Locations: size: ",locationList.size()+"");
            }
        }.execute();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
