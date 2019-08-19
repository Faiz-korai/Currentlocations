package com.eszter.currentspot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class SenderService extends Service {

    OnSmsRecieve onSmsRecieve;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public int onStartCommand(Intent intent, int flag, int startID){

            if(intent == null){
                Toast.makeText(this, "null intent", Toast.LENGTH_SHORT).show();
            }
            else {

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    CharSequence channelName = "SMS Channel";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel notificationChannel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, channelName, importance);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                Notification notification = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle("CurrentSpot Service")
                        .setContentText("Running")
                        .setSmallIcon(R.drawable.ic_stat_name)
                        //.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle())
                        //icon, title, and text is necessary for notification to show.
                        //but since its a service notification, it will show.
                        //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .build();
                startForeground(1, notification);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                onSmsRecieve = new OnSmsRecieve();
                registerReceiver(onSmsRecieve, intentFilter);
            }

                return START_STICKY;
        }

        public void onDestroy(){
            super.onDestroy();
            unregisterReceiver(onSmsRecieve);
            //on app destroy end service.
        }
}