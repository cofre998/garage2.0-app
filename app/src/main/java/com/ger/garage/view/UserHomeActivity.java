package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ger.garage.Presenter.HomeUserContract;
import com.ger.garage.Presenter.PresenterHomeUser;
import com.ger.garage.R;

public class UserHomeActivity extends AppCompatActivity implements HomeUserContract.View {

    // presenter
    private HomeUserContract.Presenter presenter;

    // element on the screen
    private Button btnMakeABooking;
    private Button btnDisplayBookings;

    private Button btnTracking;
    // message
    private final String welcomeMessage1 = "Welcome ";
    private final String welcomeMessage2 = "!";
    private final String mobilePhoneMessage = "Mobile phone number: ";
    private final String emailMessage = "Email: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        presenter = new PresenterHomeUser(UserHomeActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter.getUserDetails();
        makeABooking();
        displayBookings();
        trackingButton();


    }

    private void trackingButton() {

        btnTracking = findViewById(R.id.btnTracking);

        btnTracking.setOnClickListener(v -> {

            Intent intent = new Intent(UserHomeActivity.this, TrackingClienteActivity.class);
            startActivity(intent);

        });
    }

    // button display bookings - listener
    private void displayBookings() {

        btnDisplayBookings = findViewById(R.id.displayBookings);
        btnDisplayBookings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UserHomeActivity.this, AdminDisplayBookingsActivity.class);

                intent.putExtra("role", "user"); // 🔥 ESTO ES LO IMPORTANTE

                startActivity(intent);
            }
        });
    }

    // button make a booking listener
    private void makeABooking() {


        btnMakeABooking = findViewById(R.id.makeABooking);
        btnMakeABooking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intentMakeABooking = new Intent(UserHomeActivity.this, MakeABookingActivity.class);
                startActivity(intentMakeABooking);
            }
        });
    }

    // create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_home_menu, menu);
        return true;
    }

    // logic for functionalities on the menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                presenter.logOut();
                Intent intentRegister = new Intent(UserHomeActivity.this, RegisterActivity.class);
                startActivity(intentRegister);
                break;
            case R.id.changeProfile:
                // Optional functionality
                break;
            default:
                break;
        }

        return true;

    }

    // onDestroy, clean listener for buttons and detach presenter
    @Override
    protected void onDestroy() {
        super.onDestroy();

        btnDisplayBookings.setOnClickListener(null);
        btnMakeABooking.setOnClickListener(null);
        presenter.detach();
        presenter = null;
        btnTracking.setOnClickListener(null);

    }

    // put the app down
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // receive information from presenter to show on the screen
    // this information comes from database
    @Override
    public void showUserDetail(String name, String email, String mobilePhone) {

        TextView nameTextView = findViewById(R.id.userName);
        TextView emailTextView = findViewById(R.id.email);
        TextView mobPhoneNumberTextView = findViewById(R.id.mobPhoneNumber);

        nameTextView.setText(welcomeMessage1 + name + welcomeMessage2);
        emailTextView.setText(emailMessage + email);
        mobPhoneNumberTextView.setText(mobilePhoneMessage + mobilePhone);

    }


}
