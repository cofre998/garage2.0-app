package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.BookingDao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class PresenterAllocateMechanic implements AllocateMechanicContract.Presenter {

    private AllocateMechanicContract.View view;
    private BookingDao bookingDao;

    public PresenterAllocateMechanic(AllocateMechanicContract.View view) {
        this.view = view;
        this.bookingDao = new BookingDao();
    }

    @Override
    public void detach() {
        view = null;
    }

    // ✔ MÉTODO DEL CONTRATO
    @Override
    public ArrayList<String> getMechanics() {

        ArrayList<String> mechanics = new ArrayList<>();
        mechanics.add("Juan");
        mechanics.add("Pedro");
        mechanics.add("Luis");

        return mechanics;
    }

    // ✔ MÉTODO DEL CONTRATO
    @Override
    public void allocateMechanic(HashMap<String, String> data) {

        // Aquí puedes usar bookingId o mechanic si quieres después
        // String bookingId = data.get("bookingId");

        // Por ahora no hacemos nada (solo evitar error)
    }

    // ✔ MÉTODO DEL CONTRATO (IMPORTANTE)
    @Override
    public void getBookings(LocalDate date) {

        bookingDao.getBookingsByDate(date, null, new FirebaseListener2() {

            @Override
            public void onSuccessUpdateMechanic(ArrayList<Booking> bookings) {

                if (view != null) {

                    ArrayList<String> list = new ArrayList<>();

                    for (Booking b : bookings) {
                        list.add("ID: " + b.getId() + " - Fecha: " + b.getDate());
                    }

                    view.showBookings(list);
                }
            }

            @Override public void onSuccess(ArrayList<Booking> bookings) {}
            @Override public void onSuccess(String result) {}
            @Override public void onFailure(FirebaseException e) {}
        });
    }
}