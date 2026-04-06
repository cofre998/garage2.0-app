package com.ger.garage.view;

import com.ger.garage.model.Booking;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.ger.garage.Presenter.AllocateMechanicContract;
import com.ger.garage.Presenter.PresenterAllocateMechanic;
import com.ger.garage.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllocateMechanicActivity extends AppCompatActivity
        implements AllocateMechanicContract.View, ListViewAdapter.CheckBoxCheckedListener {

    private LocalDate date;
    private ListViewAdapter adapter;
    private ListView bookingsListView;
    private ArrayList<String> bookings;
    private ArrayList<Boolean> checkBoxes;
    private HashMap<Integer, String> positionsChecked;
    private HashMap<String, String> mechanicsToAllocate;
    private String selectedMechanic = "";

    private AllocateMechanicContract.Presenter presenter;
    private ProgressBar progressBar;

    private final String NO_BOOKINGS = "No bookings were found";
    private final String CHOOSE_MECHANIC = "Choose a mechanic";

    @Override
    public void setBookingsObjects(ArrayList<Booking> bookings) {
        this.bookingsObjects = bookings;
    }

    private ArrayList<Booking> bookingsObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_mechanic);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtener fecha desde intent
        Intent intent = getIntent();
        String dateAux = intent.getStringExtra("date");
        date = LocalDate.parse(dateAux);

        // Inicializar UI
        bookingsListView = findViewById(R.id.listViewBookings);
        progressBar = findViewById(R.id.progressBarListOfBookings);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Inicializar listas y mapas
        bookings = new ArrayList<>();
        checkBoxes = new ArrayList<>();
        positionsChecked = new HashMap<>();
        mechanicsToAllocate = new HashMap<>();

        // Instanciar Presenter
        presenter = new PresenterAllocateMechanic(this);

        loadMechanicsSpinner();
        getBookings();
    }

    private void loadMechanicsSpinner() {
        ArrayList<String> mechanics = presenter.getMechanics();
        mechanics.add(0, CHOOSE_MECHANIC);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, mechanics);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinnerMechanic = findViewById(R.id.spinnerMechanic);
        spinnerMechanic.setAdapter(dataAdapter);
        spinnerMechanic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedMechanic = (position != 0) ? parent.getItemAtPosition(position).toString() : "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMechanic = "";
            }
        });
    }

    private void getBookings() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        presenter.getBookings(date);
    }

    @Override
    public void showBookings(ArrayList<String> bookings) {
        this.bookings = bookings;

        checkBoxes.clear();
        for (int i = 0; i < bookings.size(); i++) checkBoxes.add(false);

        adapter = new ListViewAdapter(this.bookings, checkBoxes, this);
        bookingsListView.setAdapter(adapter);
        adapter.setCheckedListener(this);

        if (bookings.isEmpty()) Toast.makeText(this, NO_BOOKINGS, Toast.LENGTH_SHORT).show();

        progressBar.setVisibility(ProgressBar.GONE);
    }

    public void showBookingsUpdate(ArrayList<String> bookings) {
        this.bookings = bookings;

        checkBoxes.clear();
        for (int i = 0; i < bookings.size(); i++) checkBoxes.add(false);

        adapter.clear();
        adapter.addAll(bookings);
        adapter.notifyDataSetChanged();

        positionsChecked.clear();
        mechanicsToAllocate.clear();

        Toast.makeText(this, "Mechanic assigned successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getCheckBoxCheckedListener(int position, Boolean isChecked) {
        if (isChecked) {
            positionsChecked.put(position, bookings.get(position));
            checkBoxes.set(position, true);
        } else {
            positionsChecked.remove(position);
            checkBoxes.set(position, false);
        }
    }

    // Menú de tres puntitos
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.allocate_mechanic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.allocateMechanic) {
            if (selectedMechanic.isEmpty() || positionsChecked.isEmpty()) {
                Toast.makeText(this, "Select at least one booking and a mechanic", Toast.LENGTH_SHORT).show();
                return true;
            }

            mechanicsToAllocate.clear();

            for (Map.Entry<Integer, String> entry : positionsChecked.entrySet()) {

                int position = entry.getKey();
                Booking booking = bookingsObjects.get(position);

                mechanicsToAllocate.put(booking.getFirebaseId(), selectedMechanic);
            }

            // Guardar y refrescar
            presenter.allocateMechanic(mechanicsToAllocate);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showSuccessMessage(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(ProgressBar.GONE);
    }

    public void showMechanicAssignedError(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }


    public void showMechanicAssignedSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        getBookings();
    }
}