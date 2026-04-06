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

        bookingDao.getBookings(new FirebaseListener() {

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                if (view != null) {

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
            @Override public void onSuccessString(String result) {}
            @Override public void onSuccessInt(int value) {}
            @Override public void onFailure(FirebaseException e) {}

        });
    }
}