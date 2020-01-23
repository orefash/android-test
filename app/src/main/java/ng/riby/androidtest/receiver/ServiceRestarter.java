package ng.riby.androidtest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import ng.riby.androidtest.service.BackgroundLocationService;

public class ServiceRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(ServiceRestarter.class.getSimpleName(), "Broadcast Received");
        Log.i(ServiceRestarter.class.getSimpleName(), "Restarting Service");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(new Intent(context, BackgroundLocationService.class));
        }else{
            context.startService(new Intent(context, BackgroundLocationService.class));
        }
    }

}
