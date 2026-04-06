package com.ger.garage.model;

import com.ger.garage.Presenter.FirebaseException;
import com.ger.garage.Presenter.FirebaseListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

/*

 Access direct to User Object: Class that connect with Firestore to get/delete/save information of the user

 */

public class UserDao {

    private FirebaseFirestore db; // database
    private FirebaseAuth mFirebaseAuth; // authentication
    private final String usersCollectionpath = "garage/userInformation/users"; // collection of users

    public UserDao() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    // Check if user exist
    public boolean existCurrentUser(){

        if (mFirebaseAuth.getCurrentUser() != null)
            return true;
        return false;

    }

    // Save user after login because login is made with firebase authentication
    // which save username and password. Rest of information is saved in Firestore
    // Anyway we can save more information about user in firebase Authentication (for example mobile phone number)
    public void saveUser(User user, OnFailureListener failureListener, OnSuccessListener<Void> successListener) {

        Map<String, Object> docDataUser = new HashMap<>();

        docDataUser.put("id", user.getId());
        docDataUser.put("name", user.getName());
        docDataUser.put("mobilePhoneNumber", user.getMobilePhoneNumber());
        docDataUser.put("email", user.getEmail());
        docDataUser.put("userType", user.getUserType());
        docDataUser.put("vehicles", user.getVehicles());

        db.collection(usersCollectionpath).document(getUid()).set(docDataUser)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    // Register User
    public void createUser(User user, OnCompleteListener<AuthResult> listener) {

        mFirebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(listener);

    }

    public String getUid() {

        return mFirebaseAuth.getUid();

    }

    public String getEmail() {

        return mFirebaseAuth.getCurrentUser().getEmail();

    }

    public void deleteUser() {

        mFirebaseAuth.getInstance().getCurrentUser().delete();
    }

    public void LogIn() {

        // Made in LoginActivity --> That class wasn't be refactored, in other words
        // I didn't create the presenter

    }

    public void logOut() {

        mFirebaseAuth.getInstance().signOut();

    }

    // get information of the user from Firestore
    public void getUser(final FirebaseListener listener) {

        DocumentReference docRef = db.collection(usersCollectionpath).document(getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {

                User user = documentSnapshot.toObject(User.class);

                if (user != null) {
                    listener.onSuccessUser(user);
                } else {
                    listener.onFailure(new FirebaseException("User parse error"));
                }

            } else {
                listener.onFailure(new FirebaseException("User does not exist"));
            }

        }).addOnFailureListener(e ->
                listener.onFailure(new FirebaseException(e.getMessage()))
        );
    }

}
