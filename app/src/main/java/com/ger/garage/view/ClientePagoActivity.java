package com.ger.garage.view;

import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ger.garage.R;
import java.util.HashMap;
public class ClientePagoActivity extends AppCompatActivity {

    private TextView txtTotal;
    private RadioGroup radioGroupPago;
    private Button btnPagar;

    private String bookingId;
    private double total = 0;
    private ListView listViewServicios;
    private Button btnVerPdf;

    private ArrayList<String> servicios = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private boolean isPaid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_pago);

        txtTotal = findViewById(R.id.txtTotal);
        radioGroupPago = findViewById(R.id.radioGroupPago);
        btnPagar = findViewById(R.id.btnPagar);

        bookingId = getIntent().getStringExtra("bookingId");

        loadBooking();
        listViewServicios = findViewById(R.id.listViewServicios);
        btnVerPdf = findViewById(R.id.btnVerPdf);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, servicios);
        listViewServicios.setAdapter(adapter);

        btnVerPdf.setOnClickListener(v -> abrirPdf());


        btnPagar.setOnClickListener(v -> procesarPago());
    }

    private void loadBooking() {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    if (isPaid) {
                        Toast.makeText(this, "Este servicio ya está pagado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 💰 TOTAL
                    Double t = doc.getDouble("total");
                    if (t != null) {
                        total = t;
                        txtTotal.setText("TOTAL: $" + total);
                    }

                    // 🔧 SERVICIOS
                    ArrayList<String> items = (ArrayList<String>) doc.get("items");

                    if (items != null) {
                        servicios.clear();
                        servicios.addAll(items);
                        adapter.notifyDataSetChanged();
                    }

                    // 💳 ESTADO DE PAGO
                    Boolean paid = doc.getBoolean("paid");

                    if (paid != null && paid) {

                        isPaid = true;

                        txtTotal.setText("PAGADO ✅ - $" + total);

                        btnPagar.setEnabled(false);
                        btnPagar.setText("Pagado");

                        for (int i = 0; i < radioGroupPago.getChildCount(); i++) {
                            radioGroupPago.getChildAt(i).setEnabled(false);
                        }
                    }
                });
    }

    private void iniciarPagoWebpay() {

        String url = "https://us-central1-garageapp-96323.cloudfunctions.net/crearPago";

        JSONObject json = new JSONObject();
        try {
            json.put("bookingId", bookingId);
            json.put("amount", total);
        } catch (Exception e) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {

                    try {
                        String token = response.getString("token");
                        String paymentUrl = response.getString("url");

                        String fullUrl = paymentUrl + "?token_ws=" + token;

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
                        startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(this, "Error iniciando pago", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> Toast.makeText(this, "Error conexión Webpay", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }



    private void abrirPdf() {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(document -> {

                    ArrayList<String> titles = new ArrayList<>();
                    ArrayList<String> urls = new ArrayList<>();

                    // 🔥 NUEVO FORMATO (pdfs)
                    ArrayList<Object> rawPdfs = (ArrayList<Object>) document.get("pdfs");

                    if (rawPdfs != null) {

                        for (Object obj : rawPdfs) {

                            if (obj instanceof HashMap) {

                                HashMap<String, Object> map = (HashMap<String, Object>) obj;

                                String url = map.get("url") != null ? map.get("url").toString() : "";
                                String type = map.get("type") != null ? map.get("type").toString() : "";

                                if (!url.isEmpty()) {

                                    if (type.equals("invoice")) {
                                        titles.add("🧾 Factura");
                                    } else {
                                        titles.add("📄 Documento");
                                    }

                                    urls.add(url);
                                }
                            }
                        }
                    }

                    // ❌ nada
                    if (urls.isEmpty()) {
                        Toast.makeText(this, "No hay documentos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] items = titles.toArray(new String[0]);

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Descargar documentos")
                            .setItems(items, (dialog, which) -> {

                                String url = urls.get(which);

                                // 🔥 ABRIR / DESCARGAR
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(android.net.Uri.parse(url));
                                startActivity(intent);
                            })
                            .show();
                });
    }

    private void procesarPago() {

        if (isPaid) {
            Toast.makeText(this, "Este servicio ya está pagado", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupPago.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Selecciona método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(selectedId);
        String metodo = selected.getText().toString();

        if (metodo.equalsIgnoreCase("Webpay")) {

            iniciarPagoWebpay(); // 🔥 REAL

        } else if (metodo.equalsIgnoreCase("Transferencia")) {

            mostrarDatosTransferencia();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooking(); // 🔥 recarga estado pago
    }


    private void mostrarDatosTransferencia() {

        String datos =
                "Banco: Chile\n" +
                        "Cuenta: 12345678\n" +
                        "Nombre: Garage App\n\n" +
                        "Enviar comprobante";

        new android.app.AlertDialog.Builder(this)
                .setTitle("Transferencia")
                .setMessage(datos)
                .setPositiveButton("Ya transferí", (d, w) -> {

                    FirebaseFirestore.getInstance()
                            .collection("garage")
                            .document("bookingInformation")
                            .collection("bookings")
                            .document(bookingId)
                            ;

                    Toast.makeText(this, "Pago registrado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}