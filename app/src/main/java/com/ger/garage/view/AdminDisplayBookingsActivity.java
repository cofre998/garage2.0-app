package com.ger.garage.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ger.garage.Presenter.AdminDisplayBookingsContract;
import com.ger.garage.Presenter.FirebaseException;
import com.ger.garage.Presenter.PresenterAdminDisplayBookings;
import com.ger.garage.R;

import java.util.ArrayList;

public class AdminDisplayBookingsActivity extends AppCompatActivity implements AdminDisplayBookingsContract.View {

    private PresenterAdminDisplayBookings presenterAdminDisplayBookings;
    private ListView listViewBookingsAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_display_bookings);

        listViewBookingsAdmin = findViewById(R.id.listViewBookingsAdmin);
        presenterAdminDisplayBookings = new PresenterAdminDisplayBookings(this);

        // Obtener los bookings al iniciar
        presenterAdminDisplayBookings.getBookings();
    }

    @Override
    public void showBookings(ArrayList<String> bookings) {
        if (bookings.isEmpty()) {
            Toast.makeText(this, "No bookings found", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, bookings);
        listViewBookingsAdmin.setAdapter(adapter);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenterAdminDisplayBookings != null) {
            presenterAdminDisplayBookings.detach();
        }
    }
}