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
        mechanics.add("Alex");
        mechanics.add("Jonathan");
        mechanics.add("Patricio");
        return mechanics;
    }
    public String getRandomMechanic() {
        ArrayList<String> mechanics = getMechanics();
        return mechanics.get(new java.util.Random().nextInt(mechanics.size()));
    }

    @Override
    public void getBookings(LocalDate date) {
        bookingDao.getBookingsByDate(date, date, new FirebaseListener() {

            public void onSuccessBookingsString(ArrayList<String> list) {
                // no usado
            }

            @Override
            public void onSuccessBookings(ArrayList<Booking> bookings) {

                ArrayList<String> bookingStrings = new ArrayList<>();

                for (Booking b : bookings) {

                    String mechanicName;

                    if (b.getMechanic() == null) {
                        mechanicName = "No mechanic";
                    } else {
                        mechanicName = b.getMechanic().toString();
                    }

                    String status = (b.getStatus() != null)
                            ? b.getStatus()
                            : "Pending";

                    String vehicle = (b.getVehicle() != null)
                            ? b.getVehicle()
                            : "No plate";

                    String phone = (b.getUser() != null && b.getUser().getMobilePhoneNumber() != null)
                            ? b.getUser().getMobilePhoneNumber()
                            : "No phone";

                    bookingStrings.add(
                            "ID: " + b.getId()
                                    + " | " + b.getType()
                                    + "\nVehicle: " + vehicle
                                    + " | Phone: " + phone
                                    + "\nMechanic: " + mechanicName
                                    + " | Status: " + status
                    );
                }

                if (view != null) {
                    view.showBookings(bookingStrings);
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

            if (mechanicName == null || mechanicName.isEmpty()) {
                mechanicName = getRandomMechanic();
            }

            bookingDao.allocateMechanic(bookingId, mechanicName, new FirebaseListener() {

                public void onSuccessBookingsString(ArrayList<String> list) {
                    // no usado
                }

                @Override
                public void onSuccessBookings(ArrayList<Booking> bookings) {}

                @Override
                public void onSuccessString(String message) {
                    if (view != null) {
                        view.showMechanicAssignedSuccess(message);
                    }
                }

                @Override
                public void onSuccessInt(int value) {}

                @Override
                public void onSuccessUser(User user) {}

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
    public void detach() {}
}