package com.ger.garage.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ger.garage.Presenter.HomeAdminContract;
import com.ger.garage.Presenter.PresenterHomeAdmin;
import com.ger.garage.R;

import java.time.LocalDate;
import java.util.Calendar;

public class AdminHomeActivity extends AppCompatActivity implements HomeAdminContract.View, DatePickerDialog.OnDateSetListener {

    private Button btnMakeAAdminBooking;
    private Button btnDisplayAdminBooking;
    private Button btnAllocateMechanic;
    private Button btnAllocateCost;
    private Button btnModoTerreno;

    private Button btnHistorial;

    private HomeAdminContract.Presenter presenter;
    private String role;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new PresenterHomeAdmin(this);

        btnMakeAAdminBooking = findViewById(R.id.btnMakeAAdminBooking);
        btnDisplayAdminBooking = findViewById(R.id.btndisplayAdminBooking);
        btnAllocateMechanic = findViewById(R.id.btnAllocateMechanic);
        btnAllocateCost = findViewById(R.id.btnAllocateCost);
        btnModoTerreno = findViewById(R.id.btnModoTerreno);

        TextView title = findViewById(R.id.titleRole);

        role = getIntent().getStringExtra("role");

        if ("mechanic".equals(role)) {
            title.setText("MECÁNICO");
        } else {
            title.setText("ADMIN");
        }

        // 🔥 SIEMPRE llamar esto
        setListeners();
    }



    private void setListeners() {

        btnMakeAAdminBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, MakeABookingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnDisplayAdminBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, DisplayBookingsActivity.class);
            intent.putExtra("role", role);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnAllocateMechanic.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    this,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        btnAllocateCost.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllocateCostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnModoTerreno.setOnClickListener(v -> {

            Intent intent = new Intent(this, ModoTerrenoActivity.class);
            intent.putExtra("role", role);

            startActivity(intent);

            finish(); // 🔥 ESTA ES LA CLAVE
        });

        btnHistorial.setOnClickListener(v -> {
            startActivity(new Intent(this, HistorialActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 🔥 REACTIVA TODO
        setListeners();

        btnModoTerreno.setEnabled(true);
        btnMakeAAdminBooking.setEnabled(true);
        btnDisplayAdminBooking.setEnabled(true);
        btnAllocateMechanic.setEnabled(true);
        btnAllocateCost.setEnabled(true);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Intent intent = new Intent(this, AllocateMechanicActivity.class);
        intent.putExtra("date", LocalDate.of(year, month + 1, dayOfMonth).toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.make_a_booking_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logout) {
            presenter.logOut();
            startActivity(new Intent(this, RegisterActivity.class));
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}