package com.eszter.currentspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;


public class OnSmsRecieve extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)){
                String message = smsMessage.getMessageBody();
                    if (message.length()<=4) {
                        //here in if condition we'll be checking/comparing the message with saved code.
                        //if its a code and it matched then this will execute.
                        //send back the address of this device.
                        Intent i =new Intent(context, SendingLocation.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                    if(message.contains("Value of ...")){
                        //otherwise this will execute.
                        Intent mapAct = new Intent(context, Map_Activity.class);
                        mapAct.putExtra("LatLang", message);
                        mapAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(mapAct);
                    }
            }
        }
    }
}
