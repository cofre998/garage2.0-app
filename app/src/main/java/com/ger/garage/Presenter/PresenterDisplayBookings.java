package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class PresenterDisplayBookings implements DisplayBookingsContract.Presenter {

    private DisplayBookingsContract.View view;
    private BookingDao bookingDao;

    public PresenterDisplayBookings(DisplayBookingsContract.View view) {
        this.view = view;
        bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        view = null;
    }





    @Override
    public void getBookings() {

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        bookingDao.getBookingsByUser(uid, new FirebaseListener() {

            @Override
            public void onSuccessBookingsString(ArrayList<String> list) {
                // no usado
            }


            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                if (view != null) {

                    if (bookings == null || bookings.isEmpty()) {
                        view.showErrorMessage("No tienes bookings aún"); // 👈 CLAVE
                        return;
                    }

                    HashMap<Integer, String> mapBookings = new HashMap<>();
                    HashMap<Integer, String> mapStatus = new HashMap<>();

                    int i = 0;

                    for (Booking b : bookings) {

                        mapBookings.put(i, "Fecha: " + b.getDate());
                        mapStatus.put(i, b.getStatus());

                        i++;
                    }

                    view.showBookings(mapBookings, mapStatus);
                }
            }

            @Override public void onSuccessUser(User user) {}
            @Override
            public void onSuccessString(String id) {

                // 🔥 lista de mecánicos
                String[] mechanics = {"Alex", "Jonathan", "Patricio", "Diego"};

                String randomMechanic = mechanics[new java.util.Random().nextInt(mechanics.length)];

                // 🔥 asignar mecánico en Firebase
                bookingDao.allocateMechanic(id, randomMechanic, new FirebaseListener() {

                    @Override
                    public void onSuccessString(String message) {
                        if (view != null) {
                            view.showSuccessMessage("Booking creado y mecánico asignado: " + randomMechanic);
                        }
                    }

                    @Override
                    public void onFailure(FirebaseException e) {
                        if (view != null) {
                            view.showErrorMessage(e.getMessage());
                        }
                    }

                    // no usados
                    public void onSuccessBookingsString(ArrayList<String> list) {}
                    public void onSuccessBookings(ArrayList<Booking> b) {}
                    public void onSuccessInt(int v) {}
                    public void onSuccessUser(User u) {}
                });
            }
            @Override public void onSuccessInt(int value) {}
            @Override public void onFailure(FirebaseException e) {}

        });
    }
}