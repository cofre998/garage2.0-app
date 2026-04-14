package com.ger.garage.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

import com.ger.garage.R;
import com.ger.garage.model.Booking;

import java.util.ArrayList;

public class BookingAdapter extends ArrayAdapter<Booking> {

    private String role;

    public BookingAdapter(Context context, ArrayList<Booking> bookings, String role) {
        super(context, 0, bookings);
        this.role = role;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_booking, parent, false);
        }

        Booking booking = getItem(position);

        TextView txtEstadoPago = convertView.findViewById(R.id.txtEstadoPago);
        TextView txtVehicle = convertView.findViewById(R.id.txtVehicle);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        TextView txtType = convertView.findViewById(R.id.txtType);
        TextView txtStatus = convertView.findViewById(R.id.txtStatus);

        Button btnMap = convertView.findViewById(R.id.btnMap);
        Button btnCall = convertView.findViewById(R.id.btnCall);

        // 🔥 ESTADO PAGO
        Boolean paid = booking.isPaid();

        if (paid != null && paid) {
            txtEstadoPago.setText("PAGADO ✅");
            txtEstadoPago.setTextColor(0xFF4CAF50);
        } else {
            txtEstadoPago.setText("PENDIENTE");
            txtEstadoPago.setTextColor(0xFFFF0000);
        }

        String vehicle = (booking.getVehicle() != null) ? booking.getVehicle() : "Sin vehículo";
        String date = (booking.getDate() != null) ? booking.getDate() : "Sin fecha";
        String type = (booking.getType() != null) ? booking.getType() : "Sin tipo";
        String status = (booking.getStatus() != null) ? booking.getStatus() : "pendiente";

        txtVehicle.setText("🚗 " + vehicle);
        txtDate.setText("📅 " + date);
        txtType.setText("🔧 " + type);
        txtStatus.setText("Estado: " + status);

        // 🎨 COLORES (AJUSTADOS A TU APP)
        switch (status) {
            case "pendiente":
                txtStatus.setTextColor(Color.parseColor("#FFA000"));
                break;
            case "en_camino":
                txtStatus.setTextColor(Color.parseColor("#1976D2"));
                break;
            case "llegando":
                txtStatus.setTextColor(Color.parseColor("#0288D1"));
                break;
            case "finalizado":
                txtStatus.setTextColor(Color.parseColor("#388E3C"));
                break;
            default:
                txtStatus.setTextColor(Color.GRAY);
                break;
        }

        btnMap.setOnClickListener(v -> openLocation(booking, getContext()));
        btnCall.setOnClickListener(v -> callClient(booking, getContext()));

        return convertView;
    }

    private void openLocation(Booking booking, Context context) {

        if (booking.getIsOnSite() == null || !booking.getIsOnSite()) {
            Toast.makeText(context, "No es terreno", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri;

        if (booking.getAddress() != null && !booking.getAddress().isEmpty()) {
            uri = "geo:0,0?q=" + booking.getAddress();
        } else if (booking.getLat() != null && booking.getLng() != null) {
            uri = "geo:" + booking.getLat() + "," + booking.getLng();
        } else {
            Toast.makeText(context, "Sin ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        context.startActivity(intent);
    }

    private void callClient(Booking booking, Context context) {

        if (booking.getPhone() == null || booking.getPhone().isEmpty()) {

            new android.app.AlertDialog.Builder(context)
                    .setTitle("Teléfono no disponible")
                    .setMessage("Este cliente no tiene número registrado.\n\nPor favor, solicita que agregue su teléfono en su perfil.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(android.net.Uri.parse("tel:" + booking.getPhone()));
        context.startActivity(intent);
    }
}