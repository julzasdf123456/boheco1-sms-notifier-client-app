package com.lopez.julz.textrequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.lopez.julz.textrequest.api.RequestPlaceHolder;
import com.lopez.julz.textrequest.api.RetrofitBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MessageListener {

    public PulsatorLayout pulsatorLayout;
    public String URL = "http://192.168.10.15/text-request-api/agma-pre-reg.php?message="; // OG
    //    public String URL = "http://192.168.254.109/text-request-api/agma-pre-reg.php?message=";
    public Toolbar toolbar;
    public TextView status;

    public RetrofitBuilder retrofitBuilder;
    private RequestPlaceHolder requestPlaceHolder;

    public SMSNotifications smsNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitBuilder = new RetrofitBuilder();
        requestPlaceHolder = retrofitBuilder.getRetrofit().create(RequestPlaceHolder.class);

        startService(new Intent(this, BackgroundService.class));

        String SENT = "SMS_SENT";

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        status = findViewById(R.id.status);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Pulsating Effect
        pulsatorLayout = (PulsatorLayout) findViewById(R.id.pulsator);
        //pulsatorLayout.setCount(4);
        pulsatorLayout.start();

        sendNotifications();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                int resultCode = getResultCode();
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // UPDATE SMS IN DATABASE
                        status.setText("SMS Sent!");
                        if (smsNotification != null) {
                            Log.e("SMS_SENT", smsNotification.getId());
                            updateSMS(smsNotification.getId());
                        }

                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        sendNotifications();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        sendNotifications();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        sendNotifications();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        sendNotifications();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // Listener
        Receiver.bindListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

            final EditText passwordField = new EditText(MainActivity.this);

            alertDialogBuilder.setView(passwordField);

            // set dialog message
            alertDialogBuilder
                    .setTitle("ADMIN PIN REQUIRED")
                    .setCancelable(false)
                    .setPositiveButton("SUBMIT",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (passwordField.getText().toString().equals("2419")) {
                                        MainActivity.super.onBackPressed();
                                    } else {
                                        Snackbar.make(pulsatorLayout, "Password incorrect!", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } catch (Exception e) {
            Log.e("ERR", e.getMessage());
            TextLogger.appendLog("MANUAL_SNT_ERR: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resend) {
            resendUnsent();
        } else if (item.getItemId() == android.R.id.home) {

        }
        return false;
    }

    @Override
    public void messageReceived(String message) {
        try {
            String splitMessage[] = message.split("---");
            String contactNo = splitMessage[1];
            String origMessage = splitMessage[0];
            Snackbar.make(pulsatorLayout, message, Snackbar.LENGTH_LONG).show();

            new ValidateRequest().execute(textToUrlParam(origMessage), contactNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String textToUrlParam(String message) {
        try {
            message = message.replaceAll("\n", "");
            message = message.replace("<", "");
            message = message.replace(">", "");
            message = message.replace("#", "");
//            message = message.replace(".", "");
            message = message.replace("~", "");
            message = message.replace("|", "");
            message = message.replace("{", "");
            message = message.replace("}", "");
            message = message.replace("[", "");
            message = message.replace("]", "");
            message = message.replace("!", "");
//            message = message.replace("@", "");
            message = message.replace("%", "");
            message = message.replace("^", "");
            message = message.replace("&", "");
            message = message.replace("*", "");
            message = message.replace("(", "");
            message = message.replace(")", "");
//            message = message.replace("_", "");
            message = message.replace(",", "");
            message = message.replace("=", "");
            message = message.replace("?", "");
            message = message.replace("/", "");
            message = message.replace(":", "");
            message = message.replace(";", "");
            String[] split = message.split(" ");
            String value = "";
            for (int i = 0; i < split.length; i++) {
                if (i == split.length - 1) {
                    value += split[i];
                } else {
                    value += split[i] + "+";
                }
            }

            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    class ValidateRequest extends AsyncTask<String, Void, String> {

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                HttpGet httpget = new HttpGet(URL + strings[0] + "&contactNo=" + strings[1]);
                response = httpclient.execute(httpget);

                if (response.getStatusLine().getStatusCode() == 200) {
                    String server_response = null;
                    try {
                        server_response = EntityUtils.toString(response.getEntity());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Log.i("Server response", server_response );

                    // PARSE JSON OBJECT
                    JSONObject response = new JSONObject(server_response);
                    Log.i("RES", response.getString("msg"));
                    TextLogger.appendLog("SRV_RSPNSE: OK (see in Log.i)");
                    try {
                        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(MainActivity.this);
                        SmsManager sms = SmsManager.getDefault(); // using android SmsManager
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return "TEST";
                        }
                        List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        SubscriptionInfo info2 = (SubscriptionInfo) localList.get(1);
                        //sms.sendTextMessage(strings[1], null, response.getString("msg"), null, null); // adding number and text
                        ArrayList<String> parts = sms.divideMessage(response.getString("msg"));
//                        sms.sendMultipartTextMessage(strings[1], null, parts, null, null);
                        SmsManager.getSmsManagerForSubscriptionId(info2.getSubscriptionId()).sendMultipartTextMessage(strings[1], null, parts, null, null);
                        TextLogger.appendLog("RPLY: " + "SENDING..." + "\n\tTO: " + strings[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        TextLogger.appendLog("RPLY_ERR: " + e.getMessage());
                    }
                } else {
                    Log.i("Server response", "Failed to get server response");
                }
            } catch (IOException e) {
                e.printStackTrace();
                TextLogger.appendLog("IO_ERROR: " + e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                TextLogger.appendLog("JSON_ERROR: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void resendUnsent() {
        try {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.resend_form, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);

            alertDialogBuilder.setView(promptsView);

            final EditText textMsg = (EditText) promptsView
                    .findViewById(R.id.textMsg);
            final EditText contactNo = (EditText) promptsView
                    .findViewById(R.id.contactNo);

            // set dialog message
            alertDialogBuilder
                    .setTitle("Input the Unsent Message")
                    .setCancelable(false)
                    .setPositiveButton("GO BE A HERO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    TextLogger.appendLog("MANUAL_SNT: " + id + "\n\tTO: " + contactNo.getText().toString().trim().replace(" ", ""));
                                    new ValidateRequest().execute(textToUrlParam(textMsg.getText().toString()), contactNo.getText().toString().trim().replace(" ", ""));
                                }
                            })
                    .setNegativeButton("I DONT GIVE A SHIT",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } catch (Exception e) {
            Log.e("ERR", e.getMessage());
            TextLogger.appendLog("MANUAL_SNT_ERR: " + e.getMessage());
        }
    }

    public void sendNotifications() {
        try {
            status.setText("Fetching...");
            Call<SMSNotifications> smsNotificationsCall = requestPlaceHolder.getRandom();

            smsNotificationsCall.enqueue(new Callback<SMSNotifications>() {
                @Override
                public void onResponse(Call<SMSNotifications> call, Response<SMSNotifications> response) {
                    if (response.isSuccessful()) {
                        smsNotification = response.body();

                        if (smsNotification != null) {
                            status.setText("Sending...");
                            sendSMS(smsNotification);
                        } else {
                            status.setText("No SMS");
                            sendNotifications();
                        }
                    } else {
                        status.setText("No SMS");
                        sendNotifications();
                    }
                }

                @Override
                public void onFailure(Call<SMSNotifications> call, Throwable t) {
                    status.setText("Fetching Error");
                    Log.e("ERR_SND_SMS_FLR", t.getMessage());
                    sendNotifications();
                }
            });
        } catch (Exception e) {
            status.setText("Fetching Error");
            Log.e("ERR_SND_SMS", e.getMessage());
            sendNotifications();
        }
    }

    public void sendSMS(SMSNotifications smsNotifications) {
        try {
            String SENT = "SMS_SENT";
            PendingIntent sentApi = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(SENT), 0);

            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            sentIntents.add(sentApi);

            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(smsNotifications.getMessage());
            sms.sendMultipartTextMessage(smsNotifications.getContactNumber(), null, parts, sentIntents, null);
            Log.e("SMS_SENDING", "SMS Sending with ID " + smsNotifications.getId());
        } catch (Exception e) {
            Log.e("ERR_SMS_SNT", e.getMessage());
        }
    }

    public void updateSMS(String id) {
        try {
            status.setText("Updating DB Data...");
            Call<Void> updateCall = requestPlaceHolder.updateSMS(id);

            updateCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        status.setText("Update done!");
                        sendNotifications();
                    } else {
                        status.setText("Updating Error");
                        sendNotifications();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ERR_UPDT_SMS_FLR", t.getMessage());
                    status.setText("Updating Error");
                    sendNotifications();
                }
            });
        } catch (Exception e) {
            Log.e("ERR_UPDT_SMS", e.getMessage());
            status.setText("Updating Error");
            sendNotifications();
        }
    }
}
