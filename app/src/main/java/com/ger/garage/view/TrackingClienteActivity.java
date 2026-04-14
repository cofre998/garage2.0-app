package com.ger.garage.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ger.garage.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class TrackingClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker techMarker;
    private Polyline routeLine;
    private Polyline glowLine;

    private LatLng lastLocation = null;

    // 🔥 ESTE DEBERÍA VENIR DESDE INTENT
    private String bookingId;

    private String lastStatus = ""; // 🔥 evita spam de notificaciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_cliente);

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            bookingId = "active_service"; // fallback
        }

        listenBookingStatus();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startTrackingTecnico();
    }

    private BitmapDescriptor getCarIcon() {
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        Bitmap scaled = Bitmap.createScaledBitmap(original, 120, 120, false);
        return BitmapDescriptorFactory.fromBitmap(scaled);
    }

    private void startTrackingTecnico() {

        FirebaseFirestore.getInstance()
                .collection("tracking")
                .document("active_service")
                .addSnapshotListener((doc, error) -> {

                    if (error != null || doc == null || !doc.exists()) return;

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat == null || lng == null) return;

                    LatLng techLocation = new LatLng(lat, lng);

                    if (techMarker == null) {
                        techMarker = mMap.addMarker(new MarkerOptions()
                                .position(techLocation)
                                .title("🚗 Técnico en camino")
                                .icon(getCarIcon())
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                        );
                    } else {
                        techMarker.setPosition(techLocation);
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(techLocation, 17f));

                    drawSimpleRoute(techLocation);
                });
    }

    // 🔥 ESCUCHA SOLO ESTE BOOKING
    private void listenBookingStatus() {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .addSnapshotListener((doc, error) -> {

                    if (doc == null || !doc.exists()) return;

                    String status = doc.getString("status");
                    if (status == null) return;

                    // 🔥 EVITA SPAM
                    if (status.equals(lastStatus)) return;

                    lastStatus = status;

                    switch (status) {

                        case "aceptado":
                            notifyUser("🟢 Técnico aceptó tu servicio");
                            break;

                        case "en_camino":
                            notifyUser("🟡 Técnico en camino");
                            break;

                        case "llegando":
                            notifyUser("🔵 Técnico está llegando");
                            break;

                        case "finalizado":
                            notifyUser("🔴 Servicio finalizado");
                            break;
                    }
                });
    }

    private void notifyUser(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // 🔊 sonido real
        MediaPlayer mp = MediaPlayer.create(this,
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        mp.start();

        // 📳 vibración
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    private void drawSimpleRoute(LatLng techLocation) {

        if (routeLine != null) routeLine.remove();
        if (glowLine != null) glowLine.remove();

        List<LatLng> points = new ArrayList<>();
        points.add(techLocation);

        glowLine = mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(25f)
                .color(0xFFFF6D00));

        routeLine = mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(0xFFFFFF00));
    }
}