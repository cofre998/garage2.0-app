package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;

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
        bookingDao.getBookingsByDate(fDate, sDate, new FirebaseListener2() {
            @Override
            public void onSuccess(ArrayList<Booking> bookings) {
                ArrayList<String> bookingStrings = new ArrayList<>();
                for (Booking b : bookings) {
                    bookingStrings.add(b.toStringWithFullInformation());
                }
                if (view != null) view.showBookings(bookingStrings);
            }

            @Override
            public void onSuccess(String newStatus) {
                // No usado
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) view.showErrorMessage(e.getMessage());
            }

            @Override
            public void onSuccessUpdateMechanic(ArrayList<Booking> bookings) {
                // No usado
            }
        });
    }

    @Override
    public void changeStatus(String bookings, String newStatus) {
        // Si es necesario implementar
    }

    @Override
    public String[] getStatus(String booking) {
        // Implementar según necesidad
        return new String[]{};
    }
}