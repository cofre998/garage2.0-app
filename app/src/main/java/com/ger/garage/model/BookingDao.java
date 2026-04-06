package com.ger.garage.model;

import com.ger.garage.Presenter.FirebaseException;
import com.ger.garage.Presenter.FirebaseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingDao {

    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;

    public BookingDao() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ---------- POR FECHA ----------
    public void getBookingsByDate(LocalDate fDate, LocalDate sDate, FirebaseListener listener) {

        FirebaseFirestore.getInstance()
                .collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<Booking> list = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot) {

                        Booking b = doc.toObject(Booking.class);

                        if (b != null) {

                            // 🔥 GUARDAR FIREBASE ID
                            b.setFirebaseId(doc.getId());

                            // 🔥 FILTRAR POR FECHA
                            if (b.getDate() != null &&
                                    b.getDate().equals(fDate.toString())) {

                                list.add(b);
                            }
                        }
                    }

                    listener.onSuccessBookings(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    private void executeQueryByADate(LocalDate date, final FirebaseListener listener) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String dateStr = date.format(formatter);

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookingByDate")
                .document(dateStr)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    ArrayList<Booking> bookings = new ArrayList<>();

                    if (documentSnapshot.exists()) {

                        Map<String, Object> data = documentSnapshot.getData();

                        if (data != null) {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {

                                String bookingId = entry.getKey();
                                Map<String, Object> bookingMap =
                                        (Map<String, Object>) entry.getValue();

                                Booking booking = new Booking(Integer.parseInt(bookingId));

                                booking.setStatus((String) bookingMap.get("status"));
                                booking.setDate(dateStr);

                                bookings.add(booking);
                            }
                        }
                    }

                    listener.onSuccessBookings(bookings);

                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    // ---------- RANGO DE FECHAS ----------
    private void executeQueryByARangeOfDates(LocalDate fDate, LocalDate sDate, final FirebaseListener listener) {

        ArrayList<Booking> allBookings = new ArrayList<>();

        LocalDate current = fDate;

        while (!current.isAfter(sDate)) {

            String dateStr = current.toString();

            db.collection("garage")
                    .document("bookingInformation")
                    .collection("bookingByDate")
                    .document(dateStr)
                    .collection("bookings")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            allBookings.add(buildBooking(document));
                        }

                        listener.onSuccessBookings(allBookings);

                    })
                    .addOnFailureListener(e ->
                            listener.onFailure(new FirebaseException(e.getMessage()))
                    );

            current = current.plusDays(1);
        }
    }

    // ---------- BUILD BOOKING ----------
    private Booking buildBooking(QueryDocumentSnapshot document) {

        Integer id = Integer.parseInt(document.getId());

        String date = document.getString("date");
        String status = document.getString("status");

        return new Booking(id, date, null, null, null, null,
                null, status, null, null, null);
    }

    // ---------- UPDATE MECANICO ----------
    public void updateBookingMechanics(final Booking booking,
                                       final Mechanic mechanic,
                                       final FirebaseListener listener) {

        DocumentReference bookingRef = db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(booking.getId().toString());

        Map<String, Object> mechanicMap = new HashMap<>();
        mechanicMap.put("id", mechanic.getId());
        mechanicMap.put("name", mechanic.getName());

        bookingRef.update("mechanic", mechanicMap)
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessInt(booking.getId())
                )
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    // ---------- CAMBIAR STATUS ----------
    public void changeStatus(Booking booking, String newStatus, FirebaseListener listener) {

        DocumentReference bookingRef = db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(booking.getId().toString());

        bookingRef.update("status", newStatus)
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessString(newStatus)
                )
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    public void assignMechanic(Booking booking, Mechanic mechanic, FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(String.valueOf(booking.getId()))
                .update(
                        "mechanicId", mechanic.getId(),
                        "mechanicName", mechanic.getName()
                )
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessString("mechanic_assigned")
                )
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }
    // ---------- GET ALL BOOKINGS ----------
    public void getBookings(FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Booking> bookings = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        bookings.add(booking);
                    }

                    listener.onSuccessBookings(bookings);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    // ---------- REMOVE LISTENER ----------
    public void removeListenerBookingsByRef() {
        // No listener activo por ahora
    }

    public void createBooking(Booking booking, FirebaseListener listener) {

        DocumentReference ref = db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(); // genera ID

        String firebaseId = ref.getId();
        booking.setFirebaseId(firebaseId); // 👈 importante

// si quieres mantener id numérico:
        booking.setId(Integer.parseInt(firebaseId.hashCode() + ""));

        ref.set(booking)
                .addOnSuccessListener(unused ->
                        listener.onSuccessString(firebaseId))
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage())));
    }

    public void allocateMechanic(String bookingId, String mechanic, FirebaseListener listener) {

        Map<String, Object> mechanicMap = new HashMap<>();
        mechanicMap.put("name", mechanic);

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update("mechanic", mechanicMap,
                        "status", "Assigned")
                .addOnSuccessListener(unused ->
                        listener.onSuccessString("Mechanic assigned"))
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage())));
    }



}