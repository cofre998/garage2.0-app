package com.ger.garage.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.ger.garage.Presenter.AdminDisplayBookingsContract;
import com.ger.garage.Presenter.PresenterAdminDisplayBookings;
import com.ger.garage.R;
import com.ger.garage.model.Booking;

import java.util.*;
import com.google.firebase.auth.FirebaseAuth;
public class AdminDisplayBookingsActivity extends AppCompatActivity implements AdminDisplayBookingsContract.View {

    private PresenterAdminDisplayBookings presenter;
    private ListView listViewBookingsAdmin;
    private ProgressBar progressBar;

    private String role;
    private ArrayList<Booking> bookingsList = new ArrayList<>();

    private String currentBookingId;
    private String startDateRange = null;

    // ===================== ON CREATE =====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_display_bookings);

        listViewBookingsAdmin = findViewById(R.id.listViewBookingsAdmin);
        progressBar = findViewById(R.id.progressBar);

        role = getIntent().getStringExtra("role");
        if (role == null) role = "user";

        LinearLayout containerFilters = findViewById(R.id.containerFilters);
        if ("user".equals(role)) {
            containerFilters.setVisibility(View.GONE);
        }

        presenter = new PresenterAdminDisplayBookings(this, role);

        progressBar.setVisibility(View.VISIBLE);
        presenter.getBookings();

        setupFilters();
        setupChooseDate();
        setupRangeDates();
        setupSearchById();
        setupSearchByEmail();
    }

    // ===================== FILTERS =====================
    private void setupFilters() {
        findViewById(R.id.btnToday).setOnClickListener(v ->
                filterByDateRange(getToday(), getToday()));

        findViewById(R.id.btnTomorrow).setOnClickListener(v ->
                filterByDateRange(addDays(1), addDays(1)));

        findViewById(R.id.btnThisWeek).setOnClickListener(v ->
                filterByDateRange(getToday(), addDays(6)));

        findViewById(R.id.btnNextWeek).setOnClickListener(v ->
                filterByDateRange(addDays(7), addDays(13)));
    }

    private void setupChooseDate() {
        findViewById(R.id.btnDate).setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();

            new DatePickerDialog(this, (view, year, month, day) -> {

                String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                filterByDateRange(date, date);

            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupRangeDates() {
        findViewById(R.id.btnRangeOfDates).setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();

            new DatePickerDialog(this, (view, year, month, day) -> {

                String selected = String.format("%04d-%02d-%02d", year, month + 1, day);

                if (startDateRange == null) {
                    startDateRange = selected;
                    Toast.makeText(this, "Selecciona fecha final", Toast.LENGTH_SHORT).show();
                } else {
                    filterByDateRange(startDateRange, selected);
                    startDateRange = null;
                }

            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupSearchById() {
        findViewById(R.id.btnIdBooking).setOnClickListener(v -> {

            EditText input = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Buscar Booking ID")
                    .setView(input)
                    .setPositiveButton("Buscar", (d, w) -> {

                        String id = input.getText().toString().trim();
                        ArrayList<Booking> filtered = new ArrayList<>();

                        for (Booking b : bookingsList) {
                            if (b.getId() != null && b.getId().contains(id)) {
                                filtered.add(b);
                            }
                        }

                        listViewBookingsAdmin.setAdapter(new BookingAdapter(this, filtered, role));
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void setupSearchByEmail() {
        findViewById(R.id.btnCustomerEmail).setOnClickListener(v -> {

            EditText input = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Buscar Email")
                    .setView(input)
                    .setPositiveButton("Buscar", (d, w) -> {

                        String email = input.getText().toString().toLowerCase();
                        ArrayList<Booking> filtered = new ArrayList<>();

                        for (Booking b : bookingsList) {
                            if (b.getUser() != null &&
                                    b.getUser().getEmail() != null &&
                                    b.getUser().getEmail().toLowerCase().contains(email)) {
                                filtered.add(b);
                            }
                        }

                        listViewBookingsAdmin.setAdapter(new BookingAdapter(this, filtered, role));
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void filterByDateRange(String start, String end) {
        ArrayList<Booking> filtered = new ArrayList<>();

        for (Booking b : bookingsList) {
            if (b.getDate() == null) continue;

            if (b.getDate().compareTo(start) >= 0 &&
                    b.getDate().compareTo(end) <= 0) {
                filtered.add(b);
            }
        }

        listViewBookingsAdmin.setAdapter(new BookingAdapter(this, filtered, role));
        Toast.makeText(this, "Resultados: " + filtered.size(), Toast.LENGTH_SHORT).show();
    }

    private String getToday() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private String addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    // ===================== DATA =====================

    @Override
    public void showBookings(ArrayList<Booking> bookings) {
        progressBar.setVisibility(View.GONE);

        bookingsList = bookings;

        BookingAdapter adapter = new BookingAdapter(this, bookings, role);
        listViewBookingsAdmin.setAdapter(adapter);

        listViewBookingsAdmin.setOnItemClickListener((parent, view, position, id) -> {
            Booking booking = bookingsList.get(position);

            if (booking != null && booking.getId() != null) {
                showOptionsDialog(booking.getId());
            } else {
                Toast.makeText(this, "Error con booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            Uri fileUri = data.getData();

            if (fileUri != null && currentBookingId != null) {
                uploadPdfToFirebase(fileUri, currentBookingId);
            }
        }
    }

    private void uploadPdfToFirebase(Uri fileUri, String bookingId) {

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("pdfs/" + bookingId + "/" + System.currentTimeMillis() + ".pdf");

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        String downloadUrl = uri.toString();

                        FirebaseFirestore.getInstance()
                                .collection("garage")
                                .document("bookingInformation")
                                .collection("bookings")
                                .document(bookingId)
                                .update("pdfUrls", FieldValue.arrayUnion(downloadUrl))
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(this, "PDF subido", Toast.LENGTH_SHORT).show()
                                );
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir PDF", Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditDialog(String bookingId) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText inputPhone = new EditText(this);
        inputPhone.setHint("Teléfono");

        EditText inputAddress = new EditText(this);
        inputAddress.setHint("Dirección");

        layout.addView(inputPhone);
        layout.addView(inputAddress);

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String ownerId = doc.getString("userId");

                        // 🔒 SOLO USER puede editar SU booking
                        if ("user".equals(role)) {

                            if (ownerId == null || !ownerId.equals(currentUserId)) {
                                Toast.makeText(this, "No puedes editar este booking", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 🔥 AUTOCOMPLETAR SOLO USER
                            String phone = doc.getString("phone");
                            String address = doc.getString("address");

                            if (phone != null) inputPhone.setText(phone);
                            if (address != null) inputAddress.setText(address);
                        }

                        // 👑 ADMIN / MECHANIC
                        else {
                            // 👉 pueden editar, pero SIN autocompletar
                        }
                    }
                });

        new AlertDialog.Builder(this)
                .setTitle("Editar datos")
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {

                    String phone = inputPhone.getText().toString().trim();
                    String address = inputAddress.getText().toString().trim();

                    FirebaseFirestore.getInstance()
                            .collection("garage")
                            .document("bookingInformation")
                            .collection("bookings")
                            .document(bookingId)
                            .update(
                                    "phone", phone,
                                    "address", address
                            )
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                                presenter.getBookings();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }



    // ===================== OPTIONS =====================
    private void showOptionsDialog(String bookingId) {

        String[] options;

        if ("admin".equals(role) || "mechanic".equals(role)) {
            options = new String[]{
                    "Assign Mechanic",
                    "Change Status",
                    "Add PDF",
                    "View PDFs",
                    "Allocate Cost",
                    "Generate Invoice",
                    "View GPS",
                    "Delete Booking"
            };
        } else {
            options = new String[]{
                    "Pagar servicio",
                    "View PDFs",
                    "Editar datos",
                    "Eliminar booking"
            };
        }

        new AlertDialog.Builder(this)
                .setTitle("Options")
                .setItems(options, (dialog, which) -> {

                    if ("user".equals(role)) {

                        switch (which) {
                            case 0:
                                Intent intent = new Intent(this, ClientePagoActivity.class);
                                intent.putExtra("bookingId", bookingId);
                                startActivity(intent);
                                break;

                            case 1:
                                openPdf(bookingId);
                                break;

                            case 2:
                                showEditDialog(bookingId);
                                break;

                            case 3:
                                deleteBooking(bookingId);
                                break;
                        }
                        return;
                    }

                    switch (which) {
                        case 0:
                            showMechanicDialog(bookingId);
                            break;

                        case 1:
                            showStatusDialog(bookingId);
                            break;

                        case 2:
                            currentBookingId = bookingId;
                            openFilePicker();
                            break;

                        case 3:
                            openPdf(bookingId);
                            break;

                        case 4:
                            startActivity(new Intent(this, AllocateCostActivity.class)
                                    .putExtra("bookingId", bookingId));
                            break;

                        case 5:
                            startActivity(new Intent(this, GenerateInvoiceActivity.class)
                                    .putExtra("bookingId", bookingId));
                            break;

                        case 6:
                            Intent intent = new Intent(this, TrackingClienteActivity.class);
                            intent.putExtra("bookingId", bookingId);
                            startActivity(intent);
                            break;

                        case 7:
                            deleteBooking(bookingId);
                            break;
                    }
                })
                .show();
    }

    private void openMap(String bookingId) {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {

                    Boolean isOnSite = doc.getBoolean("isOnSite");

                    if (isOnSite == null || !isOnSite) {
                        Toast.makeText(this, "No es terreno", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat != null && lng != null) {
                        String uri = "https://www.google.com/maps?q=" + lat + "," + lng;
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    }
                });
    }

    private void deleteBooking(String bookingId) {
        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                    presenter.getBookings();
                });
    }

    private void openPdf(String bookingId) {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(document -> {

                    ArrayList<String> invoiceUrls = new ArrayList<>();
                    ArrayList<String> otherUrls = new ArrayList<>();

                    // 🔹 PDFs antiguos
                    ArrayList<String> pdfUrls = (ArrayList<String>) document.get("pdfUrls");
                    if (pdfUrls != null) {
                        otherUrls.addAll(pdfUrls);
                    }

                    // 🔹 PDFs nuevos
                    ArrayList<Object> rawPdfs = (ArrayList<Object>) document.get("pdfs");

                    if (rawPdfs != null) {
                        for (Object obj : rawPdfs) {

                            if (obj instanceof HashMap) {

                                HashMap<String, Object> map = (HashMap<String, Object>) obj;

                                String url = map.get("url") != null ? map.get("url").toString() : "";
                                String type = map.get("type") != null ? map.get("type").toString() : "";

                                if (type.equals("invoice")) {
                                    invoiceUrls.add(url);
                                } else {
                                    otherUrls.add(url);
                                }
                            }
                        }
                    }

                    // ❌ nada
                    if (invoiceUrls.isEmpty() && otherUrls.isEmpty()) {
                        Toast.makeText(this, "No hay PDFs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 🔥 construir lista visual
                    ArrayList<String> titles = new ArrayList<>();
                    ArrayList<String> finalUrls = new ArrayList<>();

                    // 🧾 INVOICES
                    if (!invoiceUrls.isEmpty()) {
                        titles.add("🧾 FACTURAS ");
                        finalUrls.add(""); // placeholder

                        for (int i = 0; i < invoiceUrls.size(); i++) {
                            titles.add("Invoice " + (i + 1));
                            finalUrls.add(invoiceUrls.get(i));
                        }
                    }

                    // 📄 OTROS PDFS
                    if (!otherUrls.isEmpty()) {
                        titles.add("📄 OTROS PDFs(Scanner)");
                        finalUrls.add(""); // placeholder

                        for (int i = 0; i < otherUrls.size(); i++) {
                            titles.add("PDF " + (i + 1));
                            finalUrls.add(otherUrls.get(i));
                        }
                    }

                    String[] items = titles.toArray(new String[0]);

                    new AlertDialog.Builder(this)
                            .setTitle("Documentos")
                            .setItems(items, (dialog, which) -> {

                                String url = finalUrls.get(which);

                                // 🔥 evitar click en títulos
                                if (url == null || url.isEmpty()) return;

                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            })
                            .show();
                });
    }

    private void showMechanicDialog(String bookingId) {

        String[] mechanics = {"Alex", "Jonathan", "Patricio", "Diego"};

        new AlertDialog.Builder(this)
                .setTitle("Assign Mechanic")
                .setItems(mechanics, (dialog, which) ->
                        presenter.assignMechanic(bookingId, mechanics[which])
                )
                .show();
    }

    private void showStatusDialog(String bookingId) {

        String[] options = {"pendiente", "en_camino", "llegando", "finalizado"};

        new AlertDialog.Builder(this)
                .setTitle("Estado del servicio")
                .setItems(options, (dialog, which) -> {

                    String selectedStatus = options[which];

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", selectedStatus);

                    // 🔥 SI ES FINALIZADO
                    if (selectedStatus.equals("finalizado")) {
                        updates.put("completed", true);
                    }

                    FirebaseFirestore.getInstance()
                            .collection("garage")
                            .document("bookingInformation")
                            .collection("bookings")
                            .document(bookingId)
                            .update(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                                presenter.getBookings();
                            });
                })
                .show();
    }

    private void showHistorial() {

        ArrayList<Booking> historial = new ArrayList<>();

        for (Booking b : bookingsList) {

            if (b != null && b.isPaid() != null && b.isPaid()) {
                historial.add(b);
            }
        }

        listViewBookingsAdmin.setAdapter(
                new BookingAdapter(this, historial, role)
        );

        Toast.makeText(this, "Historial: " + historial.size(), Toast.LENGTH_SHORT).show();
    }

    private void openFilePicker() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Seleccionar PDF"), 100);
    }

    // ===================== REQUIRED =====================
    @Override
    public void showErrorMessage(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}