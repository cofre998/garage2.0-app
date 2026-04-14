package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import com.ger.garage.Presenter.MakeABookingContract;
import com.ger.garage.Presenter.PresenterMakeABooking;
import com.ger.garage.R;

import java.time.LocalDate;
import java.util.ArrayList;
import android.location.Location;
import android.location.LocationManager;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class MakeABookingActivity extends AppCompatActivity implements MakeABookingContract.View {

    private MakeABookingContract.Presenter presenter;

    private EditText vehiclesInput, inputAddress, inputPhone;
    private CalendarView calendar;
    private Spinner shiftSpinner;
    private AutoCompleteTextView typeOfBookingInput;
    private Button btnBook;
    private ProgressBar progressBar;
    private CheckBox checkOnSite;

    private double lat = 0, lng = 0;
    private boolean isOnSite = false;

    private String vehicleString = "";
    private String shiftString = "";
    private String typeOfBookingString = "";
    private LocalDate calendarDate = LocalDate.now();

    private final String chooseshift = "Book available time";


    @Override
    public void showVehicles(ArrayList<String> vehicles) {

        if (vehicles == null || vehicles.isEmpty()) return;

        // 🔥 Autocompleta con el primer vehículo
        vehiclesInput.setText(vehicles.get(0));

        // 🔥 Si quieres dropdown también:
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                vehicles
        );

        // Si cambias vehiclesInput a AutoCompleteTextView → mejor UX
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_a_booking);

        presenter = new PresenterMakeABooking(this);

        initViews();
        setupToolbar();
        setupTypeBooking();
        setupCalendar();
        setupShifts();
        setupCheckBox();
        setupButton();

        presenter.getTypeOfBooking();
    }

    // 🔥 Inicializa TODO en un solo lugar
    private void initViews() {
        vehiclesInput = findViewById(R.id.vehicles);
        shiftSpinner = findViewById(R.id.listViewShifts);
        typeOfBookingInput = findViewById(R.id.typeOfBookingInput);
        calendar = findViewById(R.id.calendarView);
        btnBook = findViewById(R.id.book);
        progressBar = findViewById(R.id.progressBar);
        checkOnSite = findViewById(R.id.checkOnSite);
        inputAddress = findViewById(R.id.inputAddress);
        inputPhone = findViewById(R.id.inputPhone);

        progressBar.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // 🔥 AUTOCOMPLETE LIMPIO
    private void setupTypeBooking() {

        ArrayList<String> types = new ArrayList<>();
        types.add("Chequeo básico");
        types.add("Chequeo avanzado");
        types.add("Cambio de aceite");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                types
        );

        typeOfBookingInput.setAdapter(adapter);

        typeOfBookingInput.setOnItemClickListener((parent, view, position, id) ->
                typeOfBookingString = parent.getItemAtPosition(position).toString()
        );

        typeOfBookingInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                typeOfBookingString = typeOfBookingInput.getText().toString().trim();
            }
        });
    }

    private void resetShifts() {
        ArrayList<String> shifts = new ArrayList<>();
        shifts.add(chooseshift);

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) shiftSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(shifts);
        adapter.notifyDataSetChanged();
    }

    private void setupCalendar() {
        calendar.setOnDateChangeListener((view, year, month, day) -> {
            calendarDate = LocalDate.of(year, month + 1, day);
            resetShifts();
        });
    }

    private void setupShifts() {

        ArrayList<String> shifts = new ArrayList<>();
        shifts.add(chooseshift);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                shifts
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shiftSpinner.setAdapter(adapter);

        shiftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shiftString = position != 0 ? parent.getItemAtPosition(position).toString() : "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        shiftSpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (typeOfBookingString.isEmpty()) {
                    Toast.makeText(this, "Selecciona tipo de booking primero", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    presenter.getShifts(typeOfBookingString, calendarDate);
                }
            }
            return false;
        });
    }

    @Override
    public void showSuccessMessage(String bookingId) {
        Toast.makeText(this, "Booking creado: " + bookingId, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showShiftsAvailable(ArrayList<String> shifts) {

        shifts.add(0, chooseshift);

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) shiftSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(shifts);
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showTypeOfBooking(ArrayList<String> types) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                types
        );

        typeOfBookingInput.setAdapter(adapter);
    }

    private void setupCheckBox() {
        checkOnSite.setOnCheckedChangeListener((buttonView, isChecked) ->
                inputAddress.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );
    }

    private void setupButton() {

        btnBook.setOnClickListener(v -> {

            typeOfBookingString = typeOfBookingInput.getText().toString().trim();
            vehicleString = vehiclesInput.getText().toString().toUpperCase().trim();
            isOnSite = checkOnSite.isChecked();

            String address = inputAddress.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();

            if (vehicleString.isEmpty()) {
                showToast("Ingrese patente");
                return;
            }

            if (!isValidChileanPlate(vehicleString)) {
                showToast("Formato inválido");
                return;
            }

            if (phone.isEmpty()) {
                showToast("Ingrese teléfono");
                return;
            }

            if (typeOfBookingString.isEmpty()) {
                showToast("Selecciona tipo de booking");
                return;
            }

            if (shiftString.isEmpty()) {
                showToast("Selecciona horario");
                return;
            }

                    if (isOnSite) {
                        if (address.isEmpty()) {
                            showToast("Ingrese dirección");
                            return;
                        }

                        getLocationFromAddress(address);
                    }

            progressBar.setVisibility(View.VISIBLE);

            presenter.book(
                    vehicleString,
                    typeOfBookingString,
                    calendarDate,
                    shiftString,
                    isOnSite,
                    lat,
                    lng,
                    address,
                    phone
            );
        });
    }

    private void getLocationFromAddress(String addressText) {

        try {
            android.location.Geocoder geocoder =
                    new android.location.Geocoder(this, java.util.Locale.getDefault());

            java.util.List<android.location.Address> addresses =
                    geocoder.getFromLocationName(addressText, 1);

            if (addresses != null && !addresses.isEmpty()) {

                android.location.Address location = addresses.get(0);

                lat = location.getLatitude();
                lng = location.getLongitude();

                Toast.makeText(this,
                        "📍 Dirección convertida correctamente",
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this,
                        "❌ Dirección no encontrada",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error al convertir dirección",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private boolean isValidChileanPlate(String plate) {
        return plate.matches("^[A-Z]{4}[0-9]{2}$") ||
                plate.matches("^[A-Z]{2}[0-9]{4}$");
    }
}