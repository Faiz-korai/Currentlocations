package com.eszter.currentspot;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SendingLocation extends AppCompatActivity {

    PendingIntent sentPI, deliveredPI;
    String globalLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_location);

        Intent i =new Intent(SendingLocation.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(turnOnLocations()){
            getLocation();
            startActivity(i);
        }
        else{
            getLocation();
            startActivity(i);
    }

    }//end of onCreate.

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

    public void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String []{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                    if (location != null) {
                        globalLocation = String.valueOf(location.getLatitude() +","+ location.getLongitude());

                        SmsManager smsManager = SmsManager.getDefault();
                        String number = "03131299960";
                        ArrayList<String> parts = smsManager.divideMessage(String.valueOf(globalLocation +"Value of ..."));

                        ArrayList<PendingIntent> sendList = new ArrayList<>();
                        sendList.add(sentPI);

                        ArrayList<PendingIntent> deliverList = new ArrayList<>();
                        deliverList.add(deliveredPI);

                        smsManager.sendMultipartTextMessage(number, null, parts, sendList, deliverList);
                    }
                }
        });

        locationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendingLocation.this, "Turn on Locations", Toast.LENGTH_SHORT).show();
            }
        });
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
            Intent i = new Intent(this, Configure.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    protected void onResume(){
        super.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();
    }
}
