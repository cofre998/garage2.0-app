package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.User;

import java.util.ArrayList;

public interface FirebaseListener {

    void onSuccessBookings(ArrayList<Booking> bookings);

    void onSuccessString(String message);

    void onSuccessInt(int value);

    void onSuccessUser(User user);

    // ✅ AGREGA ESTE MÉTODO
    void onSuccessBookingsString(ArrayList<String> list);

    void onFailure(FirebaseException e);
}