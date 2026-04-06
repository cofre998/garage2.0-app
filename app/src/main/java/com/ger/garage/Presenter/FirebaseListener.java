package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import com.ger.garage.model.User;

import java.util.ArrayList;

public interface FirebaseListener {

    void onSuccessBookings(ArrayList<Booking> bookings);

    void onSuccessUser(User user);

    void onSuccessString(String result);

    void onSuccessInt(int value);

    void onFailure(FirebaseException e);
}