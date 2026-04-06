package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ger.garage.Presenter.DisplayBookingsContract;
import com.ger.garage.Presenter.PresenterDisplayBookings;
import com.ger.garage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DisplayBookingsActivity extends AppCompatActivity implements DisplayBookingsContract.View {

    private PresenterDisplayBookings presenter;
    @Override
    public void showMechanicAssignedError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMechanicAssignedSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void getBookings() {
        // No necesitas hacer nada aquí, ya que tu Presenter maneja la lógica
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_bookings);

        presenter = new PresenterDisplayBookings(this);

        presenter.getBookings();

    }


    @Override
    public void showBookings(HashMap<Integer, String> bookings, HashMap<Integer, String> status) {

        final String noBookings = "You have not booked a vehicle to fix/service yet";
        ListView listViewbookings = findViewById(R.id.bookings);

        if (!bookings.isEmpty()) {

            List<HashMap<String, String>> listItems = new ArrayList<>();

            SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                    new String[]{"First Line", "Second Line"},
                    new int[]{R.id.infoBooking, R.id.statusBooking});

            Iterator it = bookings.entrySet().iterator();

            while (it.hasNext()) {

                HashMap<String, String> resultMap = new HashMap<>();

                Map.Entry pair = (Map.Entry) it.next();

                resultMap.put("First Line", (String) pair.getValue());
                resultMap.put("Second Line", "Status: " + (String) status.get(pair.getKey()).toString());
                listItems.add(resultMap);

            }

            listViewbookings.setAdapter(adapter);

        } else
            Toast.makeText(DisplayBookingsActivity.this, noBookings, Toast.LENGTH_LONG).show();

    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        presenter.detach();
        presenter = null;

    }

    // call on destroy ad presenter check register adn test all together
}
