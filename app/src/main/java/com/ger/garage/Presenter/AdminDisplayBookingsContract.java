package com.ger.garage.Presenter;

import java.util.ArrayList;
import com.ger.garage.model.Booking;

public class AdminDisplayBookingsContract {

    public interface View {


        void showBookings(ArrayList<Booking> bookings); // Recibe lista de bookings ya formateada
        void showErrorMessage(String message);          // Para errores de Firebase
        void showSuccessMessage(String message);
    }

    public interface Presenter {
        void detach();
        void getBookings();  // Método para obtener bookings
    }

}