package com.ger.garage.Presenter;

import com.ger.garage.model.Booking;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public interface AllocateMechanicContract {

    public interface View {
        void showBookings(ArrayList<String> bookings);
        void showBookingsUpdate(ArrayList<String> bookings);
        void showSuccessMessage(String message);
        void showErrorMessage(String message);

        // Métodos nuevos para asignación de mecánico
        void showMechanicAssignedSuccess(String message);
        void showMechanicAssignedError(String message);

        void getCheckBoxCheckedListener(int position, Boolean isChecked);
        void setBookingsObjects(ArrayList<Booking> bookings);
    }

    interface Presenter {

        void detach();
        ArrayList<String> getMechanics();
        void allocateMechanic(HashMap<String, String> data);
        void getBookings(LocalDate date);

    }
}
