package com.eszter.currentspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    Button sendButton;
    FloatingActionButton floatingActionButton;

    String SENT = "SENT";
    String DELIVERED = "DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver SmsSentReciever, SmsDeliveredReciever;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    String number;
    String globalLocation = "";
    int count = 0;
    int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        sendButton = findViewById(R.id.button);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        if(turnOnLocations()){
        getLocation();
        }
        else
            getLocation();

    }

    public void sendSMS(View view) {
        number = "03131299960";

        if (number.equals("")) {
            Toast.makeText(this, "Please put the number you want to send your location to!", Toast.LENGTH_SHORT).show();
        }
        if (!number.equals("")) {
            if (!globalLocation.equals("")) {
                Toast.makeText(this, "Ready to go!", Toast.LENGTH_SHORT).show();
                SmsManager smsManager = SmsManager.getDefault();
                //smsManager.sendTextMessage(number, null, globalLocation, sentPI, deliveredPI );
                //above code does not work because the length of the msg is too long.

                ArrayList<String> parts = smsManager.divideMessage(globalLocation + "Value of ...");

                ArrayList<PendingIntent> sendList = new ArrayList<>();
                sendList.add(sentPI);

                ArrayList<PendingIntent> deliverList = new ArrayList<>();
                deliverList.add(deliveredPI);

                smsManager.sendMultipartTextMessage(number, null, parts, sendList, deliverList);
            }
            if (globalLocation.equals("")) {
                Toast.makeText(MainActivity.this, "We are getting ready!", Toast.LENGTH_SHORT).show();
                getLocation();
            }
        }//number not equals null
    }//send sms

    @Override
    protected void onResume() {
        super.onResume();

        Intent startService = new Intent(getBaseContext(), SenderService.class);
        startService(startService);

        SmsSentReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //why i can't get "getResultCode() in Toast or anything else to get to know whats its value?"

                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                }
                if (getResultCode() == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
                    Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                }
                if (getResultCode() == SmsManager.RESULT_ERROR_NO_SERVICE) {
                    Toast.makeText(context, "Service not available", Toast.LENGTH_SHORT).show();
                }
                if (getResultCode() == SmsManager.RESULT_ERROR_NULL_PDU) {
                    Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                }
                if (getResultCode() == SmsManager.RESULT_ERROR_RADIO_OFF) {
                    Toast.makeText(context, "Radio Off", Toast.LENGTH_SHORT).show();
                }
            }
        };

        SmsDeliveredReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(context, "SMS delivered successfully", Toast.LENGTH_SHORT).show();
                }
                if (getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(SmsSentReciever, new IntentFilter(SENT));
        registerReceiver(SmsDeliveredReciever, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(SmsSentReciever);
        unregisterReceiver(SmsDeliveredReciever);
    }

    private LocationRequest getLocationRequest() {
        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
    private LocationRequest getLocationRequest2() {
        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(500000);
        locationRequest.setFastestInterval(50000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
    public void getLocation() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            Location location =locationResult.getLastLocation();
            globalLocation = String.valueOf(location.getLatitude() + "," + location.getLongitude());
            //count++;
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String []{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(count == 0) {
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
            Toast.makeText(this, "LocationRequest1", Toast.LENGTH_SHORT).show();
        }
        if(count >0) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest2(), locationCallback, null);
            Toast.makeText(this, "LocationRequest2", Toast.LENGTH_SHORT).show();
        }
    }

    /*        LocationManager LocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = String.valueOf(location.getLatitude() + "," + location.getLongitude());
                globalLocation = loc;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);*/


    public boolean turnOnLocations(){
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please turn on your Location services")
            .setCancelable(false).setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    return true;
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.config){
            //Intent i = new Intent(this, MapsActivity.class);
            //startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }*/

public void addAndDelete(View view){
    Intent intent = new Intent(MainActivity.this, Configure.class);
    startActivity(intent);
}

}
