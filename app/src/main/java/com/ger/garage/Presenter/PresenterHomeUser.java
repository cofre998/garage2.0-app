package com.ger.garage.Presenter;

import com.ger.garage.model.User;
import com.ger.garage.model.UserDao;

import java.util.ArrayList;


public class PresenterHomeUser implements HomeUserContract.Presenter, FirebaseListener {

    public void onSuccessBookingsString(ArrayList<String> list) {
        // no usado
    }

    private UserDao userDao;
    private HomeUserContract.View view;

    public PresenterHomeUser(HomeUserContract.View view) {
        this.view = view;
        userDao = new UserDao();
    }



    @Override
    public void logOut() {
        userDao.logOut();
    }

    @Override
    public void getUserDetails() {
        userDao.getUser(this);
    }

    @Override
    public void detach() {
        this.view = null;
        this.userDao = null;
    }

    // ✅ NUEVO CALLBACK CORRECTO
    @Override
    public void onSuccessUser(User user) {

        if (user != null) {
            view.showUserDetail(
                    user.getName(),
                    user.getEmail(),
                    user.getMobilePhoneNumber()
            );
        } else {
            //view.showErrorMessage("User not found in database");
        }
    }

    @Override
    public void onSuccessBookings(java.util.ArrayList<com.ger.garage.model.Booking> bookings) {
        // no usado aquí
    }

    @Override
    public void onSuccessString(String result) {
        // no usado aquí
    }

    @Override
    public void onSuccessInt(int value) {
        // no usado aquí
    }

    @Override
    public void onFailure(FirebaseException e) {
        if (view != null) {
            // opcional: mostrar error
        }
    }
}