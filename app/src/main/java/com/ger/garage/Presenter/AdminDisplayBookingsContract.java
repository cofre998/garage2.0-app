package com.ger.garage.Presenter;

import java.util.ArrayList;

public class AdminDisplayBookingsContract {

    public interface View {
        void showBookings(ArrayList<String> bookings);  // Recibe lista de bookings ya formateada
        void showErrorMessage(String message);          // Para errores de Firebase
    }

    public interface Presenter {
        void detach();
        void getBookings();  // Método para obtener bookings
    }

}