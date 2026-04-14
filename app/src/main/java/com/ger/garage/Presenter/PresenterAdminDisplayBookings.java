package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.util.ArrayList;
import java.util.Collections;

public class PresenterAdminDisplayBookings implements AdminDisplayBookingsContract.Presenter {

    private BookingDao bookingDao;
    private AdminDisplayBookingsContract.View view;
    private String role;

    public PresenterAdminDisplayBookings(AdminDisplayBookingsContract.View view, String role) {
        this.view = view;
        this.role = role;
        this.bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        view = null;
        bookingDao = null;
    }



    @Override
    public void getBookings() {

        String uid = com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseListener listener = new FirebaseListener() {

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                java.util.Collections.sort(bookings, (b1, b2) -> {
                    try {
                        return b2.getDate().compareTo(b1.getDate());
                    } catch (Exception e) {
                        return 0;
                    }
                });

                if (view != null) {
                    view.showBookings(bookings);
                }
            }

            @Override public void onSuccessBookingsString(ArrayList<String> list) {}
            @Override public void onSuccessString(String message) {}
            @Override public void onSuccessInt(int value) {}
            @Override public void onSuccessUser(User user) {}

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        };

        // 🔥 FORZAMOS LÓGICA SEGURA
        if ("user".equals(role)) {
            bookingDao.getBookingsByUser(uid, listener); // SOLO LOS SUYOS
        } else {
            bookingDao.getAllBookings(listener); // admin / mechanic
        }
    }

    // 🔧 MECÁNICO
    public void assignMechanic(String bookingId, String mechanicName) {

        if (!"admin".equals(role) && !"mechanic".equals(role)) return;

        bookingDao.assignMechanic(bookingId, mechanicName, new FirebaseListener() {

            @Override public void onSuccessBookingsString(ArrayList<String> list) {}

            @Override
            public void onSuccessString(String message) {
                if (view != null) {
                    view.showSuccessMessage(message);
                    getBookings();
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            @Override public void onSuccessBookings(ArrayList<Booking> b) {}
            @Override public void onSuccessInt(int v) {}
            @Override public void onSuccessUser(User u) {}
        });
    }

    // 🔄 STATUS
    public void updateStatus(String bookingId, String status) {

        if (!"admin".equals(role) && !"mechanic".equals(role)) return;

        bookingDao.updateStatus(bookingId, status, new FirebaseListener() {

            @Override public void onSuccessBookingsString(ArrayList<String> list) {}

            @Override
            public void onSuccessString(String message) {
                if (view != null) {
                    view.showSuccessMessage(message);
                    getBookings();
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            @Override public void onSuccessBookings(ArrayList<Booking> b) {}
            @Override public void onSuccessInt(int v) {}
            @Override public void onSuccessUser(User u) {}
        });
    }
}