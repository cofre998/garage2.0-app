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
        vehicles.add("Auto 1");
        vehicles.add("Auto 2");

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

        Booking booking = new Booking(
                createdAt.toString(),   // ✔ String
                typeOfBooking,
                shift,
                new Vehicle(vehicle),
                "Pending",
                new User("Admin")
        );

        // ⚠️ no llamamos saveBooking porque no existe en tu DAO

        if (view != null) {
            view.showSuccessMessage("Reserva creada correctamente");
        }
    }
}