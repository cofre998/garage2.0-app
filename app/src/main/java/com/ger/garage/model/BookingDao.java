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

    public void getAllBookings(FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Booking> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Booking b = doc.toObject(Booking.class);

                        if (b != null) {
                            b.setFirebaseId(doc.getId());
                            b.setId(doc.getId());

                            // 🔥 LIMPIAR PDFs ROTOS
                            ArrayList<Object> rawPdfs = (ArrayList<Object>) doc.get("pdfs");

                            ArrayList<HashMap<String, Object>> cleanPdfs = new ArrayList<>();

                            if (rawPdfs != null) {
                                for (Object obj     : rawPdfs) {

                                    if (obj instanceof HashMap) {
                                        cleanPdfs.add((HashMap<String, Object>) obj);
                                    }

                                    // 🔥 SOPORTE PARA PDFs ANTIGUOS (STRING)
                                    else if (obj instanceof String) {
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("url", obj);
                                        map.put("name", "PDF antiguo");
                                        map.put("type", "unknown");
                                        cleanPdfs.add(map);
                                    }
                                }
                            }

                            b.setPdfs(cleanPdfs);

                            if (rawPdfs != null) {
                                for (Object obj : rawPdfs) {
                                    if (obj instanceof HashMap) {
                                        cleanPdfs.add((HashMap<String, Object>) obj);
                                    }
                                }
                            }

                            b.setPdfs(cleanPdfs);
                        }

                        if (b != null) {
                            b.setFirebaseId(doc.getId());
                            b.setId(doc.getId()); // 🔥 CLAVE
                            list.add(b);
                        }
                    }

                    listener.onSuccessBookings(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    public void getBookingsByMechanic(String mechanicName, FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .whereEqualTo("mechanicName", mechanicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Booking> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking b = doc.toObject(Booking.class);
                        if (b != null) {
                            b.setFirebaseId(doc.getId());
                            b.setId(doc.getId()); // 🔥 AÑADIR
                            list.add(b);
                        }
                    }

                    listener.onSuccessBookings(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
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

    public void book(String vehicle, String type, LocalDate date, String shift,
                     boolean isOnSite, double lat, double lng, String address, String phone,
                     FirebaseListener listener) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            listener.onFailure(new FirebaseException("Usuario no logueado"));
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference ref = db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document();

        String id = ref.getId();

        Map<String, Object> booking = new HashMap<>();
        booking.put("id", id);
        booking.put("vehicle", vehicle);
        booking.put("type", type);
        booking.put("date", date.toString());
        booking.put("shift", shift);
        booking.put("userId", uid);
        booking.put("status", "Pending");

        // 🔥 UBICACIÓN
        booking.put("isOnSite", isOnSite);
        booking.put("lat", lat);
        booking.put("lng", lng);
        booking.put("address", address);

        // 🔥 TELÉFONO CORRECTO
        booking.put("phone", phone);

        ref.set(booking)
                .addOnSuccessListener(unused ->
                        listener.onSuccessString(id))
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage())));
    }


    private void executeQueryByADate(LocalDate date, final FirebaseListener listener) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String dateStr = date.format(formatter);

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
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

                                Booking booking = new Booking(bookingId); // 🔥 FIX

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

    public void checkWeeklyLimit(LocalDate date, FirebaseListener listener) {

        LocalDate startOfWeek = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(java.time.DayOfWeek.SUNDAY);

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(snapshot -> {

                    final int[] count = {0}; // 🔥 FIX

                    for (DocumentSnapshot doc : snapshot) {
                        String bookingDate = doc.getString("date");

                        if (bookingDate != null) {

                            LocalDate bDate = LocalDate.parse(bookingDate);

                            if ((bDate.isEqual(startOfWeek) || bDate.isAfter(startOfWeek)) &&
                                    (bDate.isEqual(endOfWeek) || bDate.isBefore(endOfWeek))) {
                                count[0]++; // 🔥 FIX
                            }
                        }
                    }

                    db.collection("garage")
                            .document("config")
                            .get()
                            .addOnSuccessListener(configDoc -> {

                                Long limit = configDoc.getLong("weeklyLimit");
                                int max = (limit != null) ? limit.intValue() : 5;

                                if (count[0] >= max) {
                                    listener.onFailure(new FirebaseException("Semana llena (" + max + " bookings)"));
                                } else {
                                    listener.onSuccessInt(count[0]);
                                }

                            });

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

        String id = document.getId();

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
                .document(booking.getFirebaseId());

        Map<String, Object> mechanicMap = new HashMap<>();
        mechanicMap.put("id", mechanic.getId());
        mechanicMap.put("name", mechanic.getName());

        bookingRef.update("mechanic", mechanicMap)
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessString(booking.getId())
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
                .document(booking.getFirebaseId());

        bookingRef.update("status", newStatus)
                .addOnSuccessListener(aVoid ->
                        listener.onSuccessString(newStatus)
                )
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }


    public void assignMechanic(String bookingId, String mechanicName, FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update(
                        "mechanicName", mechanicName,
                        "mechanicEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail()
                ) // 🔥 SOLO ESTO
                .addOnSuccessListener(unused -> {
                    listener.onSuccessString("Mechanic assigned");
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(new FirebaseException(e.getMessage()));
                });
    }

    // ---------- GET ALL BOOKINGS ----------
    public void getBookings(FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Booking> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Booking b = new Booking();

                        b.setFirebaseId(doc.getId());
                        b.setId(doc.getId());

                        b.setDate(doc.getString("date"));
                        b.setType(doc.getString("type"));
                        b.setStatus(doc.getString("status"));
                        b.setShift(doc.getString("shift"));
                        b.setUserId(doc.getString("userId"));

                        // 🔥 FIX VEHICLE
                        b.setVehicle(doc.getString("vehicle"));

                        // 🔥 GPS
                        b.setIsOnSite(doc.getBoolean("isOnSite"));
                        b.setLat(doc.getDouble("lat"));
                        b.setLng(doc.getDouble("lng"));
                        b.setAddress(doc.getString("address"));

                        list.add(b);
                    }

                    listener.onSuccessBookings(list);
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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            listener.onFailure(new FirebaseException("Usuario no logueado"));
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference ref = db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document();

        String firebaseId = ref.getId();

        booking.setFirebaseId(firebaseId);
        booking.setUserId(uid);
        booking.setId(firebaseId); // 🔥 AQUÍ EL FIX

        ref.set(booking)
                .addOnSuccessListener(unused ->
                        listener.onSuccessString(firebaseId))
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage())));
    }

    public void getBookingsByUser(String userId, FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Booking> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Booking b = new Booking();

                        b.setFirebaseId(doc.getId());
                        b.setId(doc.getId());

                        b.setDate(doc.getString("date"));
                        b.setType(doc.getString("type"));
                        b.setStatus(doc.getString("status"));
                        b.setShift(doc.getString("shift"));

                        // 🔥 FIX VEHICLE
                        b.setVehicle(doc.getString("vehicle"));

                        list.add(b);
                    }

                    listener.onSuccessBookings(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }

    public void allocateMechanic(String bookingId, String mechanicName, FirebaseListener listener) {

        Map<String, Object> mechanicMap = new HashMap<>();
        mechanicMap.put("id", bookingId + "_m"); // simple id
        mechanicMap.put("name", mechanicName);
        mechanicMap.put("email", mechanicName.toLowerCase() + "@garage.com");

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update("mechanic", mechanicMap) // 🔥 ahora objeto
                .addOnSuccessListener(unused -> {
                    listener.onSuccessString("Mechanic assigned");
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(new FirebaseException(e.getMessage()));
                });
    }

    public void updateStatus(String bookingId, String status, FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookings")
                .document(bookingId)
                .update("status", status)
                .addOnSuccessListener(unused ->
                        listener.onSuccessString("Status updated"))
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage())));
    }

    public void getBookingTypes(FirebaseListener listener) {

        db.collection("garage")
                .document("bookingInformation")
                .collection("bookingTypes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<String> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String type = doc.getString("name"); // 🔥 campo dentro del doc

                        if (type != null) {
                            list.add(type);
                        }
                    }

                    listener.onSuccessBookingsString(list);
                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }


}