package com.ger.garage.view;

import com.ger.garage.model.Booking;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.SparseBooleanArray;

import com.ger.garage.Presenter.AllocateMechanicContract;
import com.ger.garage.Presenter.PresenterAllocateMechanic;
import com.ger.garage.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AllocateMechanicActivity extends AppCompatActivity
        implements AllocateMechanicContract.View {

    private LocalDate date;
    private ListView bookingsListView;
    private ArrayList<Booking> bookingsObjects;
    private HashMap<String, String> mechanicsToAllocate = new HashMap<>();
    private String selectedMechanic = "";

    private AllocateMechanicContract.Presenter presenter;
    private ProgressBar progressBar;

    private final String CHOOSE_MECHANIC = "Choose a mechanic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_mechanic);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 🔥 SPINNER MECÁNICOS
        Spinner spinnerMechanic = findViewById(R.id.spinnerMechanic);

        ArrayList<String> mechanics = new ArrayList<>();
        mechanics.add(CHOOSE_MECHANIC);
        mechanics.add("Alex");
        mechanics.add("Juan");
        mechanics.add("Pedro");

        ArrayAdapter<String> adapterMechanic = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                mechanics
        );

        adapterMechanic.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMechanic.setAdapter(adapterMechanic);

        spinnerMechanic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMechanic = (position != 0)
                        ? parent.getItemAtPosition(position).toString()
                        : "";
            }

            public void onNothingSelected(AdapterView<?> parent) {
                selectedMechanic = "";
            }
        });

        // 🔥 FECHA
        Intent intent = getIntent();
        String dateAux = intent.getStringExtra("date");
        date = LocalDate.parse(dateAux);

        // 🔥 UI
        bookingsListView = findViewById(R.id.listViewBookings);
        progressBar = findViewById(R.id.progressBarListOfBookings);
        progressBar.setVisibility(View.VISIBLE);

        presenter = new PresenterAllocateMechanic(this);

        getBookings();
    }

    private void getBookings() {
        progressBar.setVisibility(View.VISIBLE);
        presenter.getBookings(date);
    }

    @Override
    public void showBookings(ArrayList<String> bookings) {

        if (bookings.isEmpty()) {
            Toast.makeText(this, "No bookings", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                bookings
        );

        bookingsListView.setAdapter(adapter);
        bookingsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setBookingsObjects(ArrayList<Booking> bookings) {
        this.bookingsObjects = bookings;
    }

    // 🔥 ESTE MÉTODO FALTABA (POR ESO EL ERROR)
    @Override
    public void showBookingsUpdate(ArrayList<String> bookings) {
        Toast.makeText(this, "Bookings updated", Toast.LENGTH_SHORT).show();
    }

    // 🔥 ESTE TAMBIÉN FALTABA
    @Override
    public void getCheckBoxCheckedListener(int position, Boolean isChecked) {
        // No lo usamos ahora, pero es obligatorio implementarlo
    }

    // 🔥 MENÚ
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.allocate_mechanic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (selectedMechanic.isEmpty()) {
            Toast.makeText(this, "Select a mechanic", Toast.LENGTH_SHORT).show();
            return true;
        }

        mechanicsToAllocate.clear();

        SparseBooleanArray checked = bookingsListView.getCheckedItemPositions();

        for (int i = 0; i < bookingsListView.getCount(); i++) {
            if (checked.get(i)) {

                Booking booking = bookingsObjects.get(i);

                mechanicsToAllocate.put(booking.getFirebaseId(), selectedMechanic);
            }
        }

        if (mechanicsToAllocate.isEmpty()) {
            Toast.makeText(this, "Select at least one booking", Toast.LENGTH_SHORT).show();
            return true;
        }

        presenter.allocateMechanic(mechanicsToAllocate);

        return true;
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        getBookings();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showMechanicAssignedError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMechanicAssignedSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        getBookings();
    }
}