package com.ger.garage.model;

import com.ger.garage.Presenter.FirebaseException;
import com.ger.garage.Presenter.FirebaseListener;
import com.ger.garage.Presenter.FirebaseListener2;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingDao {

    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private final String bookingsCollectionPath = "garage/bookingInformation/bookings";

    public BookingDao() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ---------- Obtener reservas por fecha ----------
    public void getBookingsByDate(LocalDate fDate, LocalDate sDate, final FirebaseListener2 listener2) {
        if (sDate == null) {
            executeQueryByADate(fDate, listener2);
        } else {
            executeQueryByARangeOfDates(fDate, sDate, listener2);
        }
    }

    private void executeQueryByADate(LocalDate date, final FirebaseListener2 listener2) {

        String dateStr = date.toString();

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookingByDate")
                .document(dateStr)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    ArrayList<Booking> bookings = new ArrayList<>();

                    if (documentSnapshot.exists()) {

                        Map<String, Object> data = documentSnapshot.getData();

                        for (Map.Entry<String, Object> entry : data.entrySet()) {

                            String bookingId = entry.getKey();

                            Map<String, Object> bookingMap = (Map<String, Object>) entry.getValue();

                            Booking booking = new Booking(Integer.parseInt(bookingId));

                            booking.setStatus((String) bookingMap.get("status"));
                            booking.setDate(dateStr);

                            bookings.add(booking);
                        }
                    }

                    listener2.onSuccessUpdateMechanic(bookings);
                })
                .addOnFailureListener(e ->
                        listener2.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    public void getAllBookings(final FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookingByDate")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<Booking> bookings = new ArrayList<>();

                    int counter = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String date = doc.getId();

                        // 👇 creamos booking simple
                        Booking booking = new Booking(counter);
                        booking.setStatus("Pending");
                        booking.setDate(date);

                        bookings.add(booking);

                        counter++;
                    }

                    listener.onSuccess(bookings); // ✔ AHORA SÍ FUNCIONA

                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    private void executeQueryByARangeOfDates(LocalDate fDate, LocalDate sDate, final FirebaseListener2 listener2) {

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

                        listener2.onSuccessUpdateMechanic(allBookings);
                    });

            current = current.plusDays(1);
        }
    }

    // ---------- Construir objeto Booking ----------
    private Booking buildBooking(QueryDocumentSnapshot document) {

        Integer id = Integer.parseInt(document.getId());

        // 👇 CORRECTO
        String date = document.getString("date");

        String status = document.getString("status");

        Map<String, Object> mechMap = (Map<String, Object>) document.get("mechanic");
        Mechanic mechanic = null;
        if (mechMap != null) {
            mechanic = new Mechanic(
                    ((Long) mechMap.get("id")).intValue(),
                    (String) mechMap.get("name")
            );
        }

        ArrayList<Map<String, Object>> shiftsMap =
                (ArrayList<Map<String, Object>>) document.get("shifts");

        ArrayList<Shift> shifts = new ArrayList<>();

        if (shiftsMap != null) {
            for (Map<String, Object> s : shiftsMap) {

                int idShift = Integer.parseInt(s.get("id").toString());
                String description = (String) s.get("description");

                Map<String, Object> start = (Map<String, Object>) s.get("timeStart");
                Map<String, Object> end = (Map<String, Object>) s.get("timeEnd");

                LocalTime timeStart = LocalTime.of(
                        Integer.parseInt(start.get("hour").toString()),
                        Integer.parseInt(start.get("minute").toString())
                );

                LocalTime timeEnd = LocalTime.of(
                        Integer.parseInt(end.get("hour").toString()),
                        Integer.parseInt(end.get("minute").toString())
                );

                shifts.add(new Shift(idShift, description, timeStart, timeEnd));
            }
        }

        return new Booking(id, date, null, null, null, null, mechanic, status, null, null, shifts);
    }

    // ---------- Actualizar mecánico ----------
    public void updateBookingMechanics(final Booking booking, final Mechanic mechanic, final FirebaseListener listener) {

        DocumentReference bookingRef = db.collection(bookingsCollectionPath)
                .document(booking.getId().toString());

        Map<String, Object> mechanicMap = new HashMap<>();
        mechanicMap.put("id", mechanic.getId());
        mechanicMap.put("name", mechanic.getName());

        bookingRef.update("mechanic", mechanicMap)
                .addOnSuccessListener(aVoid -> listener.onSuccess(booking.getId()))
                .addOnFailureListener(e -> listener.onFailure(new FirebaseException(e.getMessage())));
    }

    // ---------- Cambiar estado ----------
    public void changeStatus(Booking booking, String newStatus, FirebaseListener2 listener) {

        DocumentReference bookingRef = db.collection(bookingsCollectionPath)
                .document(booking.getId().toString());

        bookingRef.update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onSuccess(newStatus))
                .addOnFailureListener(e -> listener.onFailure(new FirebaseException(e.getMessage())));
    }

    public void removeListenerBookingsByRef() {
        // opcional
    }
}