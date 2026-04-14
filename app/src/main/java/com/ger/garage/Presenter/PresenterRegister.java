package com.ger.garage.Presenter;

import androidx.annotation.NonNull;

import com.ger.garage.model.Make;
import com.ger.garage.model.SetUpDao;
import com.ger.garage.model.User;
import com.ger.garage.model.UserDao;
import com.ger.garage.model.UserType;
import com.ger.garage.model.Vehicle;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PresenterRegister implements OnCompleteListener<AuthResult>, OnSuccessListener<Void>, OnFailureListener {

    private UserDao userDao;
    private View view;
    private User user;
    private SetUpDao setUpDao;
    private String userType; // 🔥 NUEVO

    private String vehiclePlate;
    private String vehicleMake;
    private String vehicleEngineType;
    private String vehicleType;

    public PresenterRegister(View view) {
        this.view = view;
        userDao = new UserDao();
        setUpDao = new SetUpDao();
    }

    public boolean isLoggedIn() {
        return userDao.existCurrentUser();
    }

    // 🔥 AHORA RECIBE userType
    public void register(String email, String password, String mobilePhoneNumber, String name,
                         String vehiclePlate, String vehicleMake,
                         String vehicleEngineType, String vehicleType,
                         String userType) {

        this.userType = userType;

        Vehicle vehicle = new Vehicle(vehiclePlate);

        user = new User(name, mobilePhoneNumber, email, password, UserType.user);
        user.addVehicle(vehicle);

        // 🔥 GUARDAMOS TAMBIÉN LOS DATOS DEL VEHÍCULO EN VARIABLES
        this.vehiclePlate = vehiclePlate;
        this.vehicleMake = vehicleMake;
        this.vehicleEngineType = vehicleEngineType;
        this.vehicleType = vehicleType;

        userDao.createUser(user, this);
    }


    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {

        if (task.isSuccessful()) {

            String uid = userDao.getUid();
            user.setId(uid);

            Map<String, Object> map = new HashMap<>();

            // 🔥 DATOS USUARIO
            map.put("name", user.getName());
            map.put("email", user.getEmail());
            map.put("mobilePhone", user.getMobilePhoneNumber());
            map.put("userType", userType);

            // 🔥 DATOS VEHÍCULO (LO QUE TE FALTABA)
            map.put("vehiclePlate", vehiclePlate);
            map.put("vehicleMake", vehicleMake);
            map.put("vehicleType", vehicleType);
            map.put("engineType", vehicleEngineType);

            FirebaseFirestore.getInstance()
                    .collection("garage")
                    .document("userInformation")
                    .collection("users")
                    .document(uid)
                    .set(map)
                    .addOnSuccessListener(aVoid -> onSuccess(null))
                    .addOnFailureListener(this);

        } else {
            view.showRegistrationErrorMessage("Registration failed: " + task.getException().getMessage());
        }
    }

    @Override
    public void onSuccess(Void aVoid) {
        view.goToHomePage(userType); // 🔥 usa el real
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        userDao.deleteUser();
        view.showRegistrationErrorMessage(e.getMessage());
    }

    public ArrayList<String> getVehicleMakes(String vehicleType) {
        switch (vehicleType) {
            case "car": return setUpDao.getCarMake();
            case "motorbike": return setUpDao.getMotorbikeMake();
            case "small van": return setUpDao.getSmallVanMake();
            case "small bus": return setUpDao.getSmallBusMake();
            default: return new ArrayList<>();
        }
    }

    public ArrayList<String> getVehicleType() {
        return setUpDao.getVehicleType();
    }

    public ArrayList<String> getVehicleEngineType() {
        return setUpDao.getVehicleEngineType();
    }

    public void detach() {
        view = null;
        userDao = null;
        setUpDao = null;
        user = null;
    }

    public interface View {
        void showRegistrationErrorMessage(String errorMessage);
        void goToHomePage(String userType);
    }
}