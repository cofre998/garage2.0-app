package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class PresenterAllocateMechanic implements AllocateMechanicContract.Presenter {

    private final AllocateMechanicContract.View view;
    private final BookingDao bookingDao;

    public PresenterAllocateMechanic(AllocateMechanicContract.View view) {
        this.view = view;
        this.bookingDao = new BookingDao();
    }

    @Override
    public ArrayList<String> getMechanics() {
        ArrayList<String> mechanics = new ArrayList<>();
        mechanics.add("Juan");
        mechanics.add("Pedro");
        mechanics.add("Maria");
        return mechanics;
    }

    @Override
    public void getBookings(LocalDate date) {
        bookingDao.getBookingsByDate(date, date, new FirebaseListener() {

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                ArrayList<String> bookingStrings = new ArrayList<>();

                for (Booking b : bookings) {
                    String mechanicName = b.getMechanic() != null
                            ? b.getMechanic().getName()
                            : "No mechanic assigned";

                    bookingStrings.add("ID: " + b.getId()
                            + " | " + b.getType()
                            + " | Mechanic: " + mechanicName);
                }

                if (view != null) {
                    view.setBookingsObjects(bookings); // 🔥 GUARDAMOS OBJETOS
                    view.showBookings(bookingStrings); // 🔥 MOSTRAMOS STRINGS
                }
            }

            @Override
            public void onSuccessString(String message) {}

            @Override
            public void onSuccessInt(int value) {}

            @Override
            public void onSuccessUser(User user) {}

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }
        });
    }

    @Override
    public void allocateMechanic(HashMap<String, String> mechanicsToAllocate) {
        for (HashMap.Entry<String, String> entry : mechanicsToAllocate.entrySet()) {
            String bookingId = entry.getKey();
            String mechanicName = entry.getValue();

            bookingDao.allocateMechanic(bookingId, mechanicName, new FirebaseListener() {
                @Override
                public void onSuccessBookings(ArrayList<Booking> bookings) {
                    // No usado aquí
                }

                @Override
                public void onSuccessString(String message) {
                    if (view != null) {
                        view.showMechanicAssignedSuccess(message);
                    }
                }

                @Override
                public void onSuccessInt(int value) {
                    // No usado aquí
                }

                public void onSuccessUser(User user) {
                    // No usado aquí
                }

                @Override
                public void onFailure(FirebaseException e) {
                    if (view != null) {
                        view.showMechanicAssignedError(e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void detach() {
        // Limpieza si es necesaria
    }
}