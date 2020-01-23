package ng.riby.androidtest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import ng.riby.androidtest.database.DatabaseClient;
import ng.riby.androidtest.database.UserLocation;
import ng.riby.androidtest.service.BackgroundLocationService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    Button startBtn, stopBtn;
    TextView statusTV;

    private final int PERMISSION_REQUEST_CODE = 200;

    private final int PERMISSION_REQUEST_CODE2 = 205;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    Intent mServiceIntent;
    private BackgroundLocationService locationService;
    Context ctx;

    public Context getCtx() {
        return ctx;
    }

    List<UserLocation> locationList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        initViews();

        locationService = new BackgroundLocationService(getCtx());
        mServiceIntent = new Intent(getCtx(), locationService.getClass());
        if (!isMyServiceRunning(locationService.getClass())) {
//            startService(mServiceIntent);
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            statusTV.setText("Ready to Capture");
//            showLocation();
            Log.v("Service", "Not running");
        }else{
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            statusTV.setText("Capturing Location");
        }

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i ("isMyServiceRunning?", true+"");
                    return true;
                }
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationCapture();
            }
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showLocation() {
//        List<UserLocation> locationList = null;

        Log.v("Locations: ","Fetching");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {
                // something you know that will take a few seconds


                locationList = DatabaseClient
                        .getInstance(MainActivity.this)
                        .getAppDatabase()
                        .userLocationDao()
                        .getAll();

                return null;
            }

            @Override
            protected void onPostExecute( final Void result ) {
                // continue what you are doing...
                Log.v("Locations: ",locationList.get(0).getTimestamp());
                Log.v("Locations: size: ",locationList.size()+"");
            }
        }.execute();
    }

    public void initViews() {
        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        statusTV = findViewById(R.id.statusTV);



        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationCapture();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationCapture();
            }
        });
    }

    public void startLocationCapture() {
        Log.e("Start", "Location");
        if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            statusTV.setText("Capturing Location");
            startBtn.setEnabled(false);

            stopBtn.setEnabled(true);
//            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
//            editor.putInt("stop_flag", 0);
//            editor.apply();
            startService(mServiceIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }

    }

    public void stopLocationCapture() {
        //flag to stop service without restart
//        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
//        editor.putInt("stop_flag", 1);
//        editor.apply();

//        stopService(mServiceIntent);
        locationService.stopLocationService();
        statusTV.setText("Ready to Capture");

        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }
}
