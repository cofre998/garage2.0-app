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

import java.util.HashMap;
import java.util.Map;

public class UserDao {

    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;

    // ✅ RUTA CORRECTA
    private final String usersCollectionpath = "users";

    public UserDao() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ✅ CHECK USER
    public boolean existCurrentUser(){
        return mFirebaseAuth.getCurrentUser() != null;
    }

    // ✅ GET UID
    public String getUid() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            return mFirebaseAuth.getCurrentUser().getUid();
        }
        return null;
    }

    // ✅ GET EMAIL
    public String getEmail() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            return mFirebaseAuth.getCurrentUser().getEmail();
        }
        return null;
    }

    // ✅ SAVE USER
    public void saveUser(User user,
                         OnFailureListener failureListener,
                         OnSuccessListener<Void> successListener) {

        if (getUid() == null) {
            failureListener.onFailure(new Exception("User not logged"));
            return;
        }

        Map<String, Object> docDataUser = new HashMap<>();

        docDataUser.put("id", user.getId());
        docDataUser.put("name", user.getName());
        docDataUser.put("mobilePhoneNumber", user.getMobilePhoneNumber());
        docDataUser.put("email", user.getEmail());
        docDataUser.put("userType", user.getUserType());
        docDataUser.put("vehicles", user.getVehicles());

        db.collection(usersCollectionpath)
                .document(getUid())
                .set(docDataUser)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // ✅ CREATE USER (AUTH)
    public void createUser(User user, OnCompleteListener<AuthResult> listener) {
        mFirebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(listener);
    }

    // ✅ DELETE USER
    public void deleteUser() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirebaseAuth.getCurrentUser().delete();
        }
    }

    public void logOut() {
        mFirebaseAuth.signOut();
    }

    // ✅ GET USER FROM FIRESTORE
    public void getUser(final FirebaseListener listener) {

        if (getUid() == null) {
            listener.onFailure(new FirebaseException("User not logged"));
            return;
        }

        DocumentReference docRef = db.collection(usersCollectionpath).document(getUid());

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {

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

                })
                .addOnFailureListener(e ->
                        listener.onFailure(new FirebaseException(e.getMessage()))
                );
    }
}