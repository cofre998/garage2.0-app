package com.ger.garage.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ger.garage.Presenter.PresenterRegister;
import com.ger.garage.R;
import java.util.ArrayList;

/*

 View Class allow users to interact with the program to be registered

 */

public class RegisterActivity extends AppCompatActivity implements PresenterRegister.View {

    // Screen Elements
    private EditText email, password;
    private EditText mobilePhoneNumber;
    private EditText name;
    private EditText other;
    private EditText vehiclePlate;
    private Spinner vehicleType;
    private Spinner vehicleMake;
    private Spinner vehicleEngineType;

    // global variables with values selected
    private String vehicleTypeString = "";
    private String vehicleMakeString = "";
    private String vehicleEngineTypeString = "";

    // More elements
    private Button btnRegister;
    private TextView haveAccount;
    private Spinner typeOfVehicles;
    private Spinner makeOfVehicles;
    private Spinner engineTypeOfVehicles;

    // Presenter
    private PresenterRegister presenterRegister;

    // message
    private final String chooseVehicleType = "Choose a type of vehicle *";
    private final String chooseVehicleMake = "Choose a make of your vehicle *";
    private final String chooseVehicleEngineType = "Choose a engine type *";
    private final String otherMake = "Other";
    private final String admin = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // create elements
        presenterRegister = new PresenterRegister(this);
        haveAccount = findViewById(R.id.haveAccount);
        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLogin =  new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intentLogin);
            }
        });
        addEngineTypeVehicleOnSpinner();
        addVehicleMakeOnSpinner();
        addVehicleTypeOnSpinner();
        register();
    }

    // add engine type spinner with an array adapter
    private void addEngineTypeVehicleOnSpinner() {
        engineTypeOfVehicles = findViewById(R.id.vehicleEngineType);
        // get engine types
        ArrayList<String> engineTypes = presenterRegister.getVehicleEngineType();
        engineTypes.add(0, chooseVehicleEngineType);
        // create adapter
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, engineTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        engineTypeOfVehicles.setAdapter(dataAdapter);
        engineTypeOfVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // when user select a item
                if (position != 0)
                    vehicleEngineTypeString = parent.getItemAtPosition(position).toString();
                else
                    vehicleEngineTypeString = "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    // add make vehicle spinner with an array adapter
    private void addVehicleMakeOnSpinner() {

        makeOfVehicles = findViewById(R.id.vehicleMake);
        // this list start empty because depends on the type of vehicle
        ArrayList<String> makes = new ArrayList<>();
        makes.add(0, chooseVehicleMake);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, makes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        makeOfVehicles.setAdapter(dataAdapter);
        makeOfVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // when user select a item
                if (position != 0) {
                    vehicleMakeString = parent.getItemAtPosition(position).toString();
                    if (vehicleMakeString.equals(otherMake)) {
                        vehicleMakeString = other.getText().toString();
                    }
                    else
                        other.setEnabled(false);
                }
                else {
                    vehicleMakeString = "";
                    other.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // check if the user first chose the type of vehicle
        makeOfVehicles.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (vehicleTypeString == "") {

                        Toast.makeText(RegisterActivity.this, "First, the type of vehicle must be selected", Toast.LENGTH_LONG).show();

                    }


                }

                return false;
            }

        });

    }


    // type of vehicle
    private void addVehicleTypeOnSpinner() {

        typeOfVehicles = findViewById(R.id.vehicleType);
        // get types from preseneter
        ArrayList<String> types = presenterRegister.getVehicleType();
        types.add(0, chooseVehicleType);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, types);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeOfVehicles.setAdapter(dataAdapter);
        typeOfVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // chose a vehicle
                vehicleMakeString = "";

                if (position != 0) {  //  if choose a type, update make vehicle
                    vehicleTypeString = parent.getItemAtPosition(position).toString();
                    ArrayList<String> makes = presenterRegister.getVehicleMakes(vehicleTypeString);
                    makes.add(0, chooseVehicleMake);
                    makes.add(otherMake);
                    ArrayAdapter<String> dataAdapter = (ArrayAdapter<String>) makeOfVehicles.getAdapter();
                    dataAdapter.clear();
                    dataAdapter.addAll(makes);
                    dataAdapter.notifyDataSetChanged();
                    makeOfVehicles.setSelection(0);
                }
                else { // if dont choose, make spinner is clean
                    vehicleTypeString = "";
                    ArrayList<String> makes = new ArrayList<>();
                    makes.add(0, chooseVehicleMake);
                    ArrayAdapter<String> dataAdapter = (ArrayAdapter<String>) makeOfVehicles.getAdapter();
                    dataAdapter.clear();
                    dataAdapter.addAll(makes);
                    dataAdapter.notifyDataSetChanged();
                    makeOfVehicles.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    // validation of editText for empty
    private boolean isEditTextEmpty(EditText editText, String fieldName) {

        if (editText.getText().toString().isEmpty()) {

            editText.setError("Please enter " + fieldName);
            editText.requestFocus();
            return true;
        }
        return false;

    }

    // creation of handler for register button
    private void register() {

        email = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        mobilePhoneNumber = findViewById(R.id.phoneNumber);
        name = findViewById(R.id.name);
        other = findViewById(R.id.other);
        vehiclePlate = findViewById(R.id.vehiclePlate);
        vehicleType = findViewById(R.id.vehicleType);
        vehicleMake = findViewById(R.id.vehicleMake);
        vehicleEngineType = findViewById(R.id.vehicleEngineType);


        btnRegister = findViewById(R.id.register);


        btnRegister.setOnClickListener(v -> {

            if (isEditTextEmpty(email, "email")) return;
            if (isEditTextEmpty(password, "password")) return;
            if (isEditTextEmpty(name, "Name")) return;
            if (isEditTextEmpty(mobilePhoneNumber, "Mobile Phone Number")) return;
            if (isEditTextEmpty(vehiclePlate, "Number Plate")) return;

            if (vehicleMakeString.isEmpty()) {
                Toast.makeText(this, "Seleccione marca", Toast.LENGTH_SHORT).show();
                return;
            }

            if (vehicleEngineTypeString.isEmpty()) {
                Toast.makeText(this, "Seleccione motor", Toast.LENGTH_SHORT).show();
                return;
            }

            if (vehicleTypeString.isEmpty()) {
                Toast.makeText(this, "Seleccione tipo", Toast.LENGTH_SHORT).show();
                return;
            }

            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            // 🔥 ROLE AUTOMÁTICO
            String userType = "user";

            if (emailText.contains("admin")) {
                userType = "admin";
            } else if (emailText.contains("mechanic")) {
                userType = "mechanic";
            }

            presenterRegister.register(
                    emailText,
                    passwordText,
                    mobilePhoneNumber.getText().toString(),
                    name.getText().toString(),
                    vehiclePlate.getText().toString(),
                    vehicleMakeString,
                    vehicleEngineTypeString,
                    vehicleTypeString,
                    userType
            );
        });

    }

    // show error message on the screen
    public void showRegistrationErrorMessage(String errorMessage) {

        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();

    }





    // go to adminHomeScreen (owner of the garage) or userHomeScreen (customer)
    public void goToHomePage(String userType) {

        if (userType.equals(admin))
            startActivity(new Intent(RegisterActivity.this, AdminHomeActivity.class));
        else
            startActivity(new Intent(RegisterActivity.this, UserHomeActivity.class));
    }


    @Override
    public void onStart() { // check if the user is logged in redirect to home screen
        super.onStart();

        if (presenterRegister.isLoggedIn())
            ;
    }

    @Override
    public void onDestroy() { // clean everything in the object before be destroyed
        super.onDestroy();
        btnRegister.setOnClickListener(null);
        haveAccount.setOnClickListener(null);
        typeOfVehicles.setOnItemSelectedListener(null);
        makeOfVehicles.setOnItemSelectedListener(null);;
        engineTypeOfVehicles.setOnItemSelectedListener(null);;
        presenterRegister.detach();
        presenterRegister = null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { // close up

        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

