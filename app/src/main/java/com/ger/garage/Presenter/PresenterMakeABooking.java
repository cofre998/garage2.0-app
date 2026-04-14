package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import com.google.firebase.firestore.FirebaseFirestore;

public class PresenterMakeABooking implements MakeABookingContract.Presenter {

    private MakeABookingContract.View view;
    private BookingDao bookingDao;

    public PresenterMakeABooking(MakeABookingContract.View view) {
        this.view = view;
        this.bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        view = null;
    }

    @Override
    public void logOut() {}

    @Override
    public void getVehicles() {
        ArrayList<String> vehicles = new ArrayList<>();
        vehicles.add("Ford");
        vehicles.add("Toyota");
        vehicles.add("Nissan");
        vehicles.add("Chevrolet");

        if (view != null) {
            view.showVehicles(vehicles);
        }
    }

    @Override
    public void getShifts(String typeOfBooking, LocalDate date) {

        ArrayList<String> shifts = new ArrayList<>();
        shifts.add("10:00 - 11:00");
        shifts.add("12:00 - 15:00");
        shifts.add("16:00 - 18:00");

        if (typeOfBooking.equals("Premium")) {
            shifts.add("18:00 - 19:00");
        }

        if (view != null) {
            view.showShiftsAvailable(shifts);
        }
    }

    @Override
    public void getTypeOfBooking() {
        bookingDao.getBookingTypes(new FirebaseListener() {

            public void onSuccessBookingsString(ArrayList<String> list) {
                if (view != null) {
                    view.showTypeOfBooking(list);
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            public void onSuccessBookings(ArrayList<Booking> b) {}
            public void onSuccessString(String s) {}
            public void onSuccessInt(int v) {}
            public void onSuccessUser(User u) {}
        });
    }

    @Override
    public boolean isWorkingDay(DayOfWeek day) {
        return day != DayOfWeek.SUNDAY;
    }

    // 🔥 BOOK CON TELÉFONO + LÍMITE SEMANAL

    @Override
    public void book(String vehicle, String type, LocalDate date, String shift,
                     boolean isOnSite, double lat, double lng, String address, String phone) {

        // 🔥 PRIMERO validar límite semanal
        bookingDao.checkWeeklyLimit(date, new FirebaseListener() {

            @Override
            public void onSuccessInt(int currentCount) {

                // ✅ SI HAY CUPO → CREAR BOOKING
                bookingDao.book(vehicle, type, date, shift,
                        isOnSite, lat, lng, address, phone,
                        new FirebaseListener() {

                            @Override
                            public void onSuccessString(String message) {
                                if (view != null) {
                                    view.showSuccessMessage(message);
                                }
                            }

                            @Override
                            public void onFailure(FirebaseException e) {
                                if (view != null) {
                                    view.showErrorMessage(e.getMessage());
                                }
                            }

                            @Override public void onSuccessBookings(ArrayList<Booking> b) {}
                            @Override public void onSuccessBookingsString(ArrayList<String> l) {}
                            @Override public void onSuccessInt(int v) {}
                            @Override public void onSuccessUser(User u) {}
                        });
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage()); // 🔥 "Semana llena"
                }
            }

            @Override public void onSuccessBookings(ArrayList<Booking> b) {}
            @Override public void onSuccessBookingsString(ArrayList<String> l) {}
            @Override public void onSuccessString(String s) {}
            @Override public void onSuccessUser(User u) {}
        });
    }
}