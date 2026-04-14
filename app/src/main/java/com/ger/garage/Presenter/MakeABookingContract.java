package com.ger.garage.Presenter;

import java.time.DayOfWeek;

import java.time.LocalDate;
import java.util.ArrayList;

public class MakeABookingContract {


    public interface Presenter {

        void detach();
        void logOut();
        void getVehicles();
        void getShifts(String typeOfBooking, LocalDate date);
        void getTypeOfBooking();
        boolean isWorkingDay(DayOfWeek day);
        void book(String vehicle, String type, LocalDate date, String shift,
                  boolean isOnSite, double lat, double lng, String address, String phone);
    }


    public interface View{

        void showVehicles(ArrayList<String> vehicles);
        void showTypeOfBooking(ArrayList<String> typeOfBookings);
        void showShiftsAvailable(ArrayList<String> shiftAvailable);
        void showErrorMessage(String errorMessage);
        void showSuccessMessage(String bookingId);
    }

}


