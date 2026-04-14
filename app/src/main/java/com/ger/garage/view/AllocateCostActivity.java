package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ger.garage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllocateCostActivity extends AppCompatActivity {

    private EditText taskOItem, cost;
    private Button add;
    private ListView listOfCosts;

    private ArrayList<String> costList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private String bookingId;

    private String userId;
    private double totalCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_cost);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 🔥 PRIMERO recibir bookingId
        bookingId = getIntent().getStringExtra("bookingId");

        // 🔥 AHORA sí mostrar
        Toast.makeText(this, "bookingId: " + bookingId, Toast.LENGTH_LONG).show();

        // 🚨 VALIDACIÓN (MUY IMPORTANTE)
        if (bookingId == null) {
            Toast.makeText(this, "ERROR: bookingId es NULL", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        taskOItem = findViewById(R.id.taskOItem);
        cost = findViewById(R.id.cost);
        add = findViewById(R.id.add);
        listOfCosts = findViewById(R.id.listOfCosts);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, costList);
        listOfCosts.setAdapter(adapter);

        add.setOnClickListener(v -> addCost());
        userId = getIntent().getStringExtra("userId");

// 🔥 TRAER USER ID DESDE FIRESTORE SI NO VIENE
        if (userId == null) {
            FirebaseFirestore.getInstance()
                    .collection("garage")
                    .document("bookingInformation")
                    .collection("bookings")
                    .document(bookingId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            userId = doc.getString("userId");
                        }
                    });
        }
    }



    private void addCost() {

        String name = taskOItem.getText().toString().trim();
        String priceStr = cost.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        Map<String, Object> costMap = new HashMap<>();
        costMap.put("name", name);
        costMap.put("price", price);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔥 SUMAR AL TOTAL
        totalCost += price;

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update(
                        "costs", FieldValue.arrayUnion(costMap),
                        "price", totalCost, // 🔥 TOTAL
                        "status", "Cost Assigned" // 🔥 ESTADO
                )
                .addOnSuccessListener(unused -> {

                    costList.add(name + " - $" + priceStr);
                    adapter.notifyDataSetChanged();

                    taskOItem.setText("");
                    cost.setText("");

                    // 🔥 ENVIAR NOTIFICACIÓN
                    sendNotification(totalCost);

                    Toast.makeText(this, "✅ Costo agregado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void sendNotification(double totalCost) {

        if (userId == null) return;

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", "💰 Costo actualizado");
        notification.put("message", "Tu servicio ahora cuesta $" + totalCost);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notification);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.make_a_booking_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logout) {
            finish();
        }

        return true;
    }
}