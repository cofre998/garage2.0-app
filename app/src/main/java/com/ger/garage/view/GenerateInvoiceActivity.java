package com.ger.garage.view;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.ger.garage.R;

import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.net.URL;
import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
public class GenerateInvoiceActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etCliente, etRut, etVehiculo, etPatente;
    private TextView txtTotal, txtIVA, txtFinal;
    private Button btnGenerate;

    private ArrayList<Map<String, Object>> costs = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private String bookingId;
    private double finalTotal = 0;

    private static final int PICK_IMAGE = 200;
    private Uri logoUri;

    private DocumentSnapshot bookingDoc;
    private String logoUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_invoice);

        listView = findViewById(R.id.listCosts);
        txtTotal = findViewById(R.id.txtTotal);
        txtIVA = findViewById(R.id.txtIVA);
        txtFinal = findViewById(R.id.txtFinal);
        btnGenerate = findViewById(R.id.btnGenerate);

        etCliente = findViewById(R.id.etCliente);
        etRut = findViewById(R.id.etRut);
        etVehiculo = findViewById(R.id.etVehiculo);
        etPatente = findViewById(R.id.etPatente);

        Button btnUploadLogo = findViewById(R.id.btnUploadLogo);
        btnUploadLogo.setOnClickListener(v -> openImagePicker());

        bookingId = getIntent().getStringExtra("bookingId");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, displayList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadCosts();
        loadLogo();

        btnGenerate.setOnClickListener(v -> calculateTotal());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            logoUri = data.getData();
            uploadLogo();
        }
    }

    private void uploadLogo() {

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("company/logo.png");

        ref.putFile(logoUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            Map<String, Object> data = new HashMap<>();
                            data.put("logo", uri.toString());

                            FirebaseFirestore.getInstance()
                                    .collection("config")
                                    .document("company")
                                    .set(data, SetOptions.merge());

                            Toast.makeText(this, "Logo guardado ✔", Toast.LENGTH_SHORT).show();
                        })
                );
    }

    private byte[] logoBytes = null;

    private void loadLogo() {

        FirebaseFirestore.getInstance()
                .collection("config")
                .document("company")
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.get("logo") != null) {

                        logoUrl = doc.getString("logo");

                        // 🔥 DESCARGAR DESDE FIREBASE STORAGE
                        StorageReference ref = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(logoUrl);

                        ref.getBytes(1024 * 1024) // 1MB
                                .addOnSuccessListener(bytes -> {

                                    logoBytes = bytes;
                                    Toast.makeText(this, "Logo listo ✔", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error descargando logo", Toast.LENGTH_SHORT).show()
                                );
                    }
                });
    }

    private void loadCosts() {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        bookingDoc = doc;

                        java.util.List<Map<String, Object>> list =
                                (java.util.List<Map<String, Object>>) doc.get("costs");

                        if (list == null) return;

                        costs.clear();
                        displayList.clear();

                        costs.addAll(list);

                        for (Map<String, Object> c : costs) {
                            displayList.add(c.get("name") + " - $" + c.get("price"));
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void calculateTotal() {

        double total = 0;

        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.isItemChecked(i)) {
                Object priceObj = costs.get(i).get("price");
                if (priceObj != null) {
                    total += Double.parseDouble(priceObj.toString());
                }
            }
        }

        double iva = total * 0.19;
        finalTotal = total + iva;

        txtTotal.setText("Total: $" + total);
        txtIVA.setText("IVA: $" + iva);
        txtFinal.setText("Final: $" + finalTotal);

        generatePDF(total, iva, finalTotal);
    }



    private void generatePDF(double total, double iva, double finalTotal) {

        try {

            String fileName = "invoice_" + System.currentTimeMillis() + ".pdf";
            File file = new File(getExternalFilesDir(null), fileName);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🔥 HEADER (LOGO + EMPRESA)
            Table header = new Table(new float[]{200F, 300F});

            // LOGO
            if (logoBytes != null) {
                ImageData imageData = ImageDataFactory.create(logoBytes);
                Image logo = new Image(imageData);
                logo.scaleToFit(100, 100);

                header.addCell(new Cell().add(logo).setBorder(null));
            } else {
                header.addCell(new Cell().add(new Paragraph("")).setBorder(null));
            }

            // DATOS EMPRESA
            header.addCell(new Cell()
                    .add(new Paragraph("PATO APARATO SPA").setBold().setFontSize(14))
                    .add(new Paragraph("RUT: 78.229.604-3"))
                    .add(new Paragraph("Giro: Reparación eléctrica vehicular"))
                    .add(new Paragraph("Dirección: Antofagasta"))
                    .add(new Paragraph("Tel: 991908345"))
                    .setBorder(null));

            document.add(header);

            document.add(new Paragraph(" "));

            // 🔴 CAJA FACTURA
            Table facturaBox = new Table(1);
            facturaBox.setWidth(150);

            facturaBox.addCell(new Cell()
                    .add(new Paragraph("FACTURA").setBold().setFontSize(14))
                    .add(new Paragraph("N° " + System.currentTimeMillis()))
                    .add(new Paragraph("Fecha: " + new Date()))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(facturaBox);

            document.add(new Paragraph(" "));

            // 👤 CLIENTE
            document.add(new Paragraph("DATOS DEL CLIENTE").setBold());

            String cliente = etCliente.getText().toString();
            String rutCliente = etRut.getText().toString();
            String vehiculo = etVehiculo.getText().toString();
            String patente = etPatente.getText().toString();

            document.add(new Paragraph("Nombre: " + cliente));
            document.add(new Paragraph("RUT: " + rutCliente));
            document.add(new Paragraph("Vehículo: " + vehiculo));
            document.add(new Paragraph("Patente: " + patente));

            document.add(new Paragraph(" "));

            // 📊 TABLA PROFESIONAL
            Table table = new Table(new float[]{300F, 100F});

            table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Precio").setBold()));

            for (int i = 0; i < listView.getCount(); i++) {
                if (listView.isItemChecked(i)) {

                    String name = costs.get(i).get("name").toString();
                    String price = costs.get(i).get("price").toString();

                    table.addCell(new Cell().add(new Paragraph(name)));
                    table.addCell(new Cell().add(new Paragraph("$" + price))
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
                }
            }

            document.add(table);

            document.add(new Paragraph(" "));

            // 💰 TOTALES (ALINEADOS DERECHA)
            Table totals = new Table(2);
            totals.setWidth(200);

            totals.addCell(new Cell().add(new Paragraph("NETO")).setBorder(null));
            totals.addCell(new Cell().add(new Paragraph("$" + total))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setBorder(null));

            totals.addCell(new Cell().add(new Paragraph("IVA 19%")).setBorder(null));
            totals.addCell(new Cell().add(new Paragraph("$" + iva))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setBorder(null));

            totals.addCell(new Cell().add(new Paragraph("TOTAL").setBold()).setBorder(null));
            totals.addCell(new Cell().add(new Paragraph("$" + finalTotal).setBold())
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setBorder(null));

            document.add(totals);

            document.add(new Paragraph(" "));

            // ✍️ FIRMA
            document.add(new Paragraph("________________________"));
            document.add(new Paragraph("Firma"));

            document.add(new Paragraph(" "));

            // 🔐 SII
            document.add(new Paragraph("Timbre Electrónico SII"));
            document.add(new Paragraph("Res. 99 de 2014 - Verifique en www.sii.cl"));

            document.close();

            uploadPdfToFirebase(file, fileName);

        } catch (Exception e) {
            Toast.makeText(this, "Error PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadPdfToFirebase(File file, String fileName) {

        Uri fileUri = Uri.fromFile(file);

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("invoices/" + bookingId + "/" + fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        String downloadUrl = uri.toString();

                        // ✅ GUARDAR COMO OBJETO (NO STRING)
                        Map<String, Object> pdfMap = new HashMap<>();
                        pdfMap.put("url", downloadUrl);
                        pdfMap.put("name", fileName);
                        pdfMap.put("type", "invoice");

                        FirebaseFirestore.getInstance()
                                .collection("garage")
                                .document("bookingInformation")
                                .collection("bookings")
                                .document(bookingId)
                                .update("pdfs", FieldValue.arrayUnion(pdfMap))
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(this, "Invoice guardado ✔", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error guardando invoice", Toast.LENGTH_SHORT).show()
                                );

                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error subiendo PDF", Toast.LENGTH_SHORT).show()
                );
    }
}