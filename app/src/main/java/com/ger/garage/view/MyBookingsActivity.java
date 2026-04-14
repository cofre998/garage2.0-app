package com.ger.garage.view;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ger.garage.Presenter.PresenterMechanic;
import com.ger.garage.R;
import com.ger.garage.view.BookingAdapter;
import com.ger.garage.model.Booking;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MyBookingsActivity extends AppCompatActivity
        implements PresenterMechanic.MechanicView {

    private ListView listView;
    private PresenterMechanic presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        listView = findViewById(R.id.listMyBookings);
        presenter = new PresenterMechanic(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        presenter.getBookingsByUser(uid); // 🔥 usamos UID
    }



    @Override
    public void showBookings(ArrayList<Booking> bookings) {

        if (bookings == null || bookings.isEmpty()) {
            Toast.makeText(this, "No tienes bookings", Toast.LENGTH_SHORT).show();
            return;
        }

        BookingAdapter adapter = new BookingAdapter(this, bookings, "user");
        listView.setAdapter(adapter);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}