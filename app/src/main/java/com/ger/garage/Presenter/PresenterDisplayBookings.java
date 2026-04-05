package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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

        bookingDao.getAllBookings(new FirebaseListener() {

            @Override
            public void onSuccess(ArrayList<Booking> bookingsList) {

                HashMap<Integer, String> bookings = new HashMap<>();
                HashMap<Integer, String> status = new HashMap<>();

                int i = 0;

                for (Booking b : bookingsList) {

                    bookings.put(i, "Booking date: " + b.getDate());
                    status.put(i, b.getStatus());

                    i++;
                }

                if (view != null) {
                    view.showBookings(bookings, status);
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            // 🔥 NECESARIOS (aunque no se usen)

            @Override
            public void onSuccess(User user) { }

            @Override
            public void onSuccess(Map<Integer, Integer> quantityOfBookingsByShift) { }

            @Override
            public void onSuccess(Integer idBooking) { }

        });
    }
}