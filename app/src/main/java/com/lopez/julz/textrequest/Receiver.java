package com.lopez.julz.textrequest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class Receiver extends BroadcastReceiver {

    private static MessageListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle data = intent.getExtras();
            Object[] pdus = (Object[]) data.get("pdus");
            for(int i=0; i<pdus.length; i++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String message = smsMessage.getMessageBody() + "---" + smsMessage.getDisplayOriginatingAddress();
                TextLogger.appendLog("RCVD_SMS: " + smsMessage.getTimestampMillis() + "\n\tFROM: " + smsMessage.getDisplayOriginatingAddress() + "\n\tBODY: " + smsMessage.getMessageBody());
                mListener.messageReceived(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TextLogger.appendLog("ERR: " + e.getMessage());
        }

    }

    public static void bindListener(MessageListener listener){
        mListener = listener;
    }
}
