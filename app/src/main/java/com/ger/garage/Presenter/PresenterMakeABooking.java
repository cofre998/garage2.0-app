package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;
import com.ger.garage.model.User;
import com.ger.garage.model.Vehicle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

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
    public void logOut() {
        // opcional
    }

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

    // 🔴 NO usamos showShifts porque no existe en tu View
    @Override
    public void getShifts(String typeOfBooking, LocalDate date) {

        ArrayList<String> shifts = new ArrayList<>();

        shifts.add("10:00 - 12:00");
        shifts.add("14:00 - 16:00");
        shifts.add("16:00 - 18:00");

        if (typeOfBooking.equals("Premium")) {
            shifts.add("18:00 - 20:00");
        }

        if (view != null) {
            view.showShiftsAvailable(shifts); // 🔥 ESTE ES EL CORRECTO
        }
    }

    @Override
    public void getTypeOfBooking() {

        ArrayList<String> types = new ArrayList<>();
        types.add("Standard");
        types.add("Premium");

        if (view != null) {
            view.showTypeOfBooking(types);
        }
    }

    @Override
    public boolean isWorkingDay(DayOfWeek day) {
        return day != DayOfWeek.SUNDAY;
    }

    // 🔥 AQUÍ ESTABA EL ERROR GRANDE



    @Override
    public void book(String vehicle, String typeOfBooking, LocalDate createdAt, String shift) {

        Booking booking = new Booking();

        booking.setDate(createdAt.toString());
        booking.setType(typeOfBooking);
        booking.setComments(shift);
        booking.setStatus("Pending");

        Vehicle v = new Vehicle();
        v.setNumberPlate(vehicle);
        booking.setVehicle(v);

        User user = new User();
        user.setName("Cliente");
        booking.setUser(user);

        bookingDao.createBooking(booking, new FirebaseListener() {

            @Override
            public void onSuccessString(String result) {
                if (view != null) {
                    view.showSuccessMessage(result);
                }
            }

            @Override
            public void onFailure(FirebaseException e) {
                if (view != null) {
                    view.showErrorMessage(e.getMessage());
                }
            }

            @Override public void onSuccessBookings(ArrayList<Booking> bookings) {}
            @Override public void onSuccessUser(User user) {}
            @Override public void onSuccessInt(int value) {}
        });
    }
}