package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.util.ArrayList;

public class PresenterMechanic {

    public interface MechanicView {
        void showBookings(ArrayList<Booking> bookings);
        void showErrorMessage(String message);
    }

    private MechanicView view;
    private BookingDao bookingDao;

    public PresenterMechanic(MechanicView view) {
        this.view = view;
        this.bookingDao = new BookingDao(); // 🔥 IMPORTANTE
    }

    public void getBookingsByUser(String uid) {

        bookingDao.getBookingsByUser(uid, new FirebaseListener() {

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                if (view != null) {
                    view.showBookings(bookings); // 🔥 SIN convertir a String
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            // 🔥 MÉTODOS OBLIGATORIOS
            @Override public void onSuccessBookingsString(ArrayList<String> list) {}
            @Override public void onSuccessUser(User user) {}
            @Override public void onSuccessString(String id) {}
            @Override public void onSuccessInt(int value) {}
        });
    }
}