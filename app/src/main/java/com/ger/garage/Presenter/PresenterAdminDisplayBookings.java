package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.util.ArrayList;

public class PresenterAdminDisplayBookings implements AdminDisplayBookingsContract.Presenter {

    private BookingDao bookingDao;
    private AdminDisplayBookingsContract.View view;

    public PresenterAdminDisplayBookings(AdminDisplayBookingsContract.View view) {
        this.view = view;
        this.bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        this.view = null;
        this.bookingDao = null;
    }

    @Override
    public void getBookings() {
        bookingDao.getBookings(new FirebaseListener() {
            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {
                ArrayList<String> list = new ArrayList<>();
                for (Booking b : bookings) {
                    String mechanicName = (b.getMechanic() != null) ? b.getMechanic().getName() : "No mechanic assigned";
                    list.add("ID: " + b.getId() + " | " + b.getType() + " | Mechanic: " + mechanicName);
                }
                if (view != null) {
                    view.showBookings(list);
                }
            }

            @Override
            public void onSuccessString(String message) {
                // No usamos este
            }

            @Override
            public void onSuccessInt(int value) {
                // No usamos este
            }

            @Override
            public void onSuccessUser(User user) {
                // No usamos este
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        });
    }
}