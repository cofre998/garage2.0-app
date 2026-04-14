package com.ger.garage.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ger.garage.R;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ModoTerrenoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private Marker carMarker;
    private boolean isTrackingActive = false;

    private String currentBookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_terreno);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnCenter = findViewById(R.id.btnCenter);
        btnCenter.setOnClickListener(v -> moveToUser());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableUserLocation();
        loadBookingsOnMap();
        startLiveLocation();

        mMap.setOnMarkerClickListener(marker -> {

            Object tag = marker.getTag();

            if (tag instanceof Map) {

                Map<String, Object> data = (Map<String, Object>) tag;

                LatLng destino = (LatLng) data.get("latlng");
                String address = (String) data.get("address");
                String date = (String) data.get("date");
                String service = (String) data.get("service");
                String bookingId = (String) data.get("bookingId");

                String resumen =
                        "📍 Dirección: " + address + "\n\n" +
                                "📅 Fecha: " + date + "\n\n" +
                                "🔧 Servicio: " + service;

                new android.app.AlertDialog.Builder(this)
                        .setTitle("🚗 Detalle del servicio")
                        .setMessage(resumen)
                        .setPositiveButton("🚀 IR", (d, w) -> {

                            if (destino != null && bookingId != null) {

                                currentBookingId = bookingId;

                                FirebaseFirestore.getInstance()
                                        .collection("garage")
                                        .document("bookingInformation")
                                        .collection("bookings")
                                        .document(bookingId)
                                        .update("status", "en_camino");

                                startNavigation(destino);
                            }
                        })
                        .setNegativeButton("❌ Cancelar", null)
                        .show();
            }

            return true;
        });
    }

    // 🚗 ICONO AUTO
    private BitmapDescriptor getCarIcon() {
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        Bitmap scaled = Bitmap.createScaledBitmap(original, 120, 120, false);
        return BitmapDescriptorFactory.fromBitmap(scaled);
    }

    // 📍 MOVER A USUARIO
    private void moveToUser() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) return;

                    LatLng userLocation = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    if (carMarker == null) {
                        carMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .icon(getCarIcon())
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                        );
                    } else {
                        carMarker.setPosition(userLocation);
                        carMarker.setRotation(location.getBearing());
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f));
                });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        moveToUser();
    }

    // 🚀 NAVEGACIÓN + DETECCIÓN DE LLEGADA
    private void startNavigation(LatLng destino) {

        if (isTrackingActive) return;
        isTrackingActive = true;

        LocationRequest request = LocationRequest.create()
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                if (result == null) return;

                android.location.Location location = result.getLastLocation();

                LatLng userLocation = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                // 🔥 FIREBASE TRACKING
                Map<String, Object> tracking = new HashMap<>();
                tracking.put("lat", userLocation.latitude);
                tracking.put("lng", userLocation.longitude);

                FirebaseFirestore.getInstance()
                        .collection("tracking")
                        .document("active_service")
                        .set(tracking);

                // 🚗 MOVER AUTO
                if (carMarker != null) {
                    carMarker.setPosition(userLocation);
                    carMarker.setRotation(location.getBearing());
                }

                // 📍 DETECTAR LLEGADA
                if (currentBookingId != null) {

                    float distancia = distanceBetween(userLocation, destino);

                    if (distancia < 50) {

                        isTrackingActive = false;

                        // 🔥 CAMBIAR ESTADO
                        FirebaseFirestore.getInstance()
                                .collection("garage")
                                .document("bookingInformation")
                                .collection("bookings")
                                .document(currentBookingId)
                                .update("status", "llegando");

                        // 🚀 ABRIR HISTORIAL
                        Intent intent = new Intent(ModoTerrenoActivity.this, HistorialActivity.class);
                        intent.putExtra("bookingId", currentBookingId);
                        startActivity(intent);

                        Toast.makeText(ModoTerrenoActivity.this,
                                "Llegaste al cliente",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
    }

    // 📡 TRACKING SIEMPRE ACTIVO
    private void startLiveLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest request = LocationRequest.create()
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                if (result == null) return;

                android.location.Location location = result.getLastLocation();

                LatLng userLocation = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                if (carMarker == null) {
                    carMarker = mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .icon(getCarIcon()));
                } else {
                    carMarker.setPosition(userLocation);
                }
            }
        }, null);
    }

    // 📍 DISTANCIA
    private float distanceBetween(LatLng start, LatLng end) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                result
        );
        return result[0];
    }

    // 🗺 BOOKINGS EN MAPA
    private void loadBookingsOnMap() {

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(query -> {

                    for (QueryDocumentSnapshot doc : query) {

                        Boolean completed = doc.getBoolean("completed");
                        Boolean paid = doc.getBoolean("paid");

                        // 🔥 NO MOSTRAR SI YA TERMINÓ O PAGÓ
                        if ((completed != null && completed) || (paid != null && paid)) continue;

                        String date = doc.getString("date");
                        if (date == null || !date.equals(today)) continue;

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (lat == null || lng == null) continue;

                        LatLng destino = new LatLng(lat, lng);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(destino)
                                .title("Cliente")
                        );

                        Map<String, Object> data = new HashMap<>();
                        data.put("latlng", destino);
                        data.put("address", doc.getString("address"));
                        data.put("date", doc.getString("date"));
                        data.put("service", doc.getString("type"));
                        data.put("bookingId", doc.getId());

                        marker.setTag(data);
                    }
                });
    }
}