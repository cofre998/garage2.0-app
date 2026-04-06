package com.ger.garage.Presenter;
import com.ger.garage.model.User;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.time.LocalDate;
import java.util.ArrayList;

public class PresenterListOfBookings implements ListOfBookingsContract.Presenter {

    private ListOfBookingsContract.View view;
    private BookingDao bookingDao;

    public PresenterListOfBookings(ListOfBookingsContract.View view) {
        this.view = view;
        this.bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        view = null;
        bookingDao.removeListenerBookingsByRef();
    }

    @Override
    public void getBookings(LocalDate fDate, LocalDate sDate) {

        bookingDao.getBookingsByDate(fDate, sDate, new FirebaseListener() {

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                if (view != null) {

                    ArrayList<String> list = new ArrayList<>();

                    for (Booking b : bookings) {
                        list.add(b.toStringWithFullInformation());
                    }

                    view.showBookings(list);
                }
            }

            @Override
            public void onSuccessUser(User user) {}

            @Override
            public void onSuccessString(String result) {}

            @Override
            public void onSuccessInt(int value) {}

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        });
    }

    @Override
    public void changeStatus(String bookings, String newStatus) {
        // Si es necesario implementar
    }

    @Override
    public String[] getStatus(String booking) {
        return new String[]{};
    }
}