package com.ger.garage.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ger.garage.R;

import java.util.*;

public class HistorialActivity extends AppCompatActivity {

    private EditText inputItem, inputPrecio;
    private Button btnAgregar, btnFinalizar, btnFactura;
    private ListView listView;
    private TextView txtTotal;

    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<Double> precios = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private double total = 0;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        inputItem = findViewById(R.id.inputItem);
        inputPrecio = findViewById(R.id.inputPrecio);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnFactura = findViewById(R.id.btnFactura);
        listView = findViewById(R.id.listView);
        txtTotal = findViewById(R.id.txtTotal);

        bookingId = getIntent().getStringExtra("bookingId");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        // ➕ agregar servicio
        btnAgregar.setOnClickListener(v -> agregarItem());

        // 🧾 generar factura
        btnFactura.setOnClickListener(v -> {
            if (bookingId == null) {
                Toast.makeText(this, "Error: booking vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, GenerateInvoiceActivity.class);
            intent.putExtra("bookingId", bookingId);
            startActivity(intent);
        });

        // ✅ finalizar → guardar + ir a pago
        btnFinalizar.setOnClickListener(v -> finalizarServicio());
    }

    private void agregarItem() {

        String item = inputItem.getText().toString().trim();
        String precioStr = inputPrecio.getText().toString().trim();

        if (item.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioStr);

        items.add(item + " - $" + precio);
        precios.add(precio);

        total += precio;

        txtTotal.setText("TOTAL: $" + total);

        adapter.notifyDataSetChanged();

        inputItem.setText("");
        inputPrecio.setText("");
    }

    private void finalizarServicio() {

        if (bookingId == null) {
            Toast.makeText(this, "Error booking", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("total", total);
        data.put("status", "finalizado");
        data.put("completed", true);

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update(data)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Servicio finalizado ✔", Toast.LENGTH_SHORT).show();

                    // 🚀 IR AUTOMÁTICO A PAGO
                    Intent intent = new Intent(this, ClientePagoActivity.class);
                    intent.putExtra("bookingId", bookingId);
                    startActivity(intent);

                    finish();
                });
    }
}