package com.ger.garage.model;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.util.ArrayList;

/*

 Class booking: Represent a booking in the system.

*/

public class Booking {

    private Integer id;
    private String firebaseId;
    private String date;
    private Timestamp createdAt;
    private String type;
    private String comments;
    private Vehicle vehicle;
    private Mechanic mechanic;
    private String status;
    private Cost cost;
    private User user;
    private ArrayList<Shift> shifts;

    public Booking() {
        // Required empty constructor for Firebase
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(ArrayList<Shift> shifts) {
        this.shifts = shifts;
    }

    public Booking(Integer id, String date, Timestamp createdAt, String type, String comments, Vehicle vehicle, Mechanic mechanic, String status, Cost cost, User user, ArrayList<Shift> shifts) {
        this.id = id;
        this.date = date;
        this.createdAt = createdAt;
        this.type = type;
        this.comments = comments;
        this.vehicle = vehicle;
        this.mechanic = mechanic;
        this.status = status;
        this.cost = cost;
        this.user = user;
        this.shifts = shifts;
    }

    public Booking(String date, String type, String comments, Vehicle vehicle, String status, User user) {
        this.date = date;
        this.type = type;
        this.comments = comments;
        this.vehicle = vehicle;
        this.status = status;
        this.user = user;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    // Generate a string with colors
    public SpannableString toStringColor() {

        int lastIndex;

        String booking = "Booking Number: " + id + System.lineSeparator()
                + "Booking date: " + date + System.lineSeparator()
                + "Booking type: " + type + System.lineSeparator()
                + "Vehicle: "  + vehicle + System.lineSeparator()
                + "status: " + status;

        SpannableString ss = new SpannableString(booking);

        lastIndex = status.lastIndexOf(booking);

        lastIndex =+ 7;

        ForegroundColorSpan fcRed = new ForegroundColorSpan(Color.parseColor("#95120a"));

        ss.setSpan(fcRed, lastIndex, ss.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    @NonNull
    @Override
    public String toString() {

        return "Booking Number: " + id + System.lineSeparator()
                + "Booking date: " + date + System.lineSeparator()
                + "Booking type: " + type + System.lineSeparator()
                + "Vehicle: "  + vehicle + System.lineSeparator()
                + "status: " + status;


    }

    public String toStringWithoutStatus() {

        return "Booking Number: " + id + System.lineSeparator()
                + "Booking date: " + date + System.lineSeparator()
                + "Booking type: " + type + System.lineSeparator()
                + "Vehicle: "  + vehicle;


    }

    public  String toStringWithFullInformation() {

        String mechanic = this.mechanic != null ? this.mechanic.toString() : "No mechanic assigned";
        String cost = this.cost != null ? this.cost.toString() : "No cost allocated";

        String shift = "";

        for (Shift s: this.shifts) {
            if (shift == "")
                shift = shift + s.toString();
            else
                shift = shift + " / " + s.toString();
        }

        return "Booking Number: " + id + System.lineSeparator()
                + "Booking date: " + date + System.lineSeparator()
                + "Booking type: " + type + System.lineSeparator()
                + "Status: " + status + System.lineSeparator()
                + "User: " + user  + System.lineSeparator()
                + "Vehicle: "  + vehicle  + System.lineSeparator()
                + "Shift/s: " + shift + System.lineSeparator()
                + mechanic + System.lineSeparator()
                + cost;


    }

    public Booking(Integer id) {
        this.id = id;
    }

    // This method is static and return a booking object using
    // a booking string generated by toStringWithFullInformation()
    // NO MODIFY THIS METHOD DEPENDS ON TOSTRINGWITHFULLINFORMATION
    public static Booking valueOfFullInformation(String booking) {

        String id = "";

        for (int i = 16; i < booking.length(); i++) {

            int ascii = (int) booking.charAt(i);

            if (ascii == 10)
                break;
            id = id + booking.charAt(i);
        }

        return new Booking(Integer.parseInt(id));

    }

    public String toStringToAllocateMechanic() {

        String mechanic = this.mechanic != null ? this.mechanic.toString() : "No mechanic assigned";
        String shift = "";

        for (Shift s: this.shifts) {
            if (shift == "")
                shift = shift + s.toString();
            else
                shift = shift + " / " + s.toString();
        }

        return "Booking Number: " + id + "(" + date + ")" + System.lineSeparator()
                + "Booking type: " + type + System.lineSeparator()
                + "Vehicle: "  + vehicle  + System.lineSeparator()
                + "Shift/s: " + shift + System.lineSeparator()
                + mechanic + System.lineSeparator();
    }

    // This method is static and return a booking object using
    // a booking string generated by toStringToAllocateMechanic()
    // NO MODIFY THIS METHOD DEPENDS ON TOSTRINGTOALLOCATEMECHANIC
    public static Booking valueOfToAllocateMechanic(String booking) {

        String id = "";

        for (int i = 16; i < booking.length(); i++) {

            if (booking.charAt(i) == '(')
                break;

            id = id + booking.charAt(i);
        }

        return new Booking(Integer.parseInt(id));

    }

    public Booking(Integer id, Mechanic mechanic, ArrayList<Shift> shifts) {
        this.id = id;
        this.mechanic = mechanic;
        this.shifts = shifts;
    }
}
