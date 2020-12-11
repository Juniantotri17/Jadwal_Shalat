package com.example.jadwal_shalat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    TextView Subuh,Dhuhur,Ashar,Maghrib,Isha;
    private float Latitude,Longitude;
    ProgressDialog progressDialog;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    //tombol back
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tombol back
        getSupportActionBar().setTitle("Jadwal Shalat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Subuh = findViewById(R.id.subuh_value);
        Dhuhur = findViewById(R.id.dhuhur_value);
        Ashar = findViewById(R.id.ashar_value);
        Maghrib = findViewById(R.id.maghrib_value);
        Isha = findViewById(R.id.isha_value);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mohon Tunggu");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        CheckinternetGPS();
    }
    private void CheckinternetGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, "Nyalakan GPS anda", Toast.LENGTH_SHORT).show();
                } else {
                    GetLocation();
                }
            }
        }else{
            Toast.makeText(this,"No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void GetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
            } else {
            JadwalShalat();
        //atas masih error
        }
        progressDialog.show();
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location !=null){
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),location.getLongitude(),1
                        );

                        Longitude = (float) addresses.get(0).getLongitude();
                        Latitude = (float) addresses.get(0).getLatitude();

                        JadwalShalat();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void JadwalShalat() {

        final DateComponents dateComponents = DateComponents.from(new Date());
        final Coordinates coordinates = new Coordinates(Latitude,Longitude);
        final CalculationParameters parameters = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getDefault());

        PrayerTimes prayerTimes = new PrayerTimes(coordinates,dateComponents,parameters);

        Subuh.setText(Html.fromHtml("Pukul " + formatter.format(prayerTimes.fajr)));
        Dhuhur.setText(Html.fromHtml("Pukul " + formatter.format(prayerTimes.dhuhr)));
        Ashar.setText(Html.fromHtml("Pukul " + formatter.format(prayerTimes.asr)));
        Maghrib.setText(Html.fromHtml("Pukul " + formatter.format(prayerTimes.maghrib)));
        Isha.setText(Html.fromHtml("Pukul" + formatter.format(prayerTimes.isha)));
        progressDialog.dismiss();
    }
}
