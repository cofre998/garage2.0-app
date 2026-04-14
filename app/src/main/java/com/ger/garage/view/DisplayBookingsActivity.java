package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;
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

import com.ger.garage.Presenter.HomeAdminContract;
import com.ger.garage.Presenter.PresenterHomeAdmin;
import com.ger.garage.R;

import java.time.LocalDate;
import java.util.Calendar;

public class DisplayBookingsActivity extends AppCompatActivity
        implements HomeAdminContract.View, DatePickerDialog.OnDateSetListener {

    private Button btnMakeAAdminBooking;
    private Button btnDisplayAdminBooking;
    private Button btnAllocateMechanic;
    private Button btnAllocateCost;

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

        TextView title = findViewById(R.id.titleRole);

        role = getIntent().getStringExtra("role");

        if ("admin".equals(role)) {
            title.setText("Admin");
        } else if ("mechanic".equals(role)) {
            title.setText("Mecánico");
        }

        setListeners();
    }



    private void setListeners() {



        btnMakeAAdminBooking.setOnClickListener(v ->
                startActivity(new Intent(this, MakeABookingActivity.class))
        );

        btnDisplayAdminBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDisplayBookingsActivity.class);
            intent.putExtra("role", role); // 🔥 IMPORTANTE
            startActivity(intent);
        });

        btnAllocateMechanic.setOnClickListener(v -> {

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    this,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        btnAllocateCost.setOnClickListener(v ->
                startActivity(new Intent(this, AllocateCostActivity.class))
        );
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        presenter.detach();
        presenter = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Intent intent = new Intent(this, AllocateMechanicActivity.class);
        intent.putExtra("date", LocalDate.of(year, month + 1, dayOfMonth).toString());
        startActivity(intent);
    }
}