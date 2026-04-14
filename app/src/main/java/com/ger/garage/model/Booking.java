package com.ger.garage.model;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

public class Booking {

    private String id;
    private String firebaseId;
    private String date;
    private Timestamp createdAt;
    private String type;
    private String comments;

    // 🔥 FIX REAL AQUÍ
    private String vehicle;

    private Mechanic mechanic;
    private String status;
    private Cost cost;
    private User user;
    private ArrayList<Shift> shifts;

    private String userId;
    private String mechanicName;
    private String shift;

    private ArrayList<HashMap<String, Object>> pdfs;

    public Booking() {
    }

    public Booking(String id) {
        this.id = id;
    }

    private Boolean paid;

    public Boolean isPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Booking(String id, String date, Timestamp createdAt, String type,
                   String comments, String vehicle, Mechanic mechanic,
                   String status, Cost cost, User user, ArrayList<Shift> shifts) {

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

    // ================= GETTERS / SETTERS =================

    public String getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = String.valueOf(id);
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    // 🔥 FIX CLAVE
    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(ArrayList<Shift> shifts) {
        this.shifts = shifts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName) {
        this.mechanicName = mechanicName;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public ArrayList<HashMap<String, Object>> getPdfs() {
        return pdfs;
    }

    public void setPdfs(ArrayList<HashMap<String, Object>> pdfs) {
        this.pdfs = pdfs;
    }

    private Double lat;
    private Double lng;
    private String address;
    private Boolean isOnSite;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsOnSite() {
        return isOnSite;
    }

    public void setIsOnSite(Boolean isOnSite) {
        this.isOnSite = isOnSite;
    }

    public ArrayList<HashMap<String, Object>> getSafePdfs() {

        ArrayList<HashMap<String, Object>> safeList = new ArrayList<>();

        if (pdfs == null) return safeList;

        for (Object obj : pdfs) {
            if (obj instanceof HashMap) {
                safeList.add((HashMap<String, Object>) obj);
            }
        }

        return safeList;
    }

    // ================= UI =================

    public SpannableString toStringColor() {

        String booking = toString();

        SpannableString ss = new SpannableString(booking);

        int start = booking.indexOf("status:");
        if (start == -1) start = booking.indexOf("Status:");

        if (start != -1) {
            ForegroundColorSpan fcRed =
                    new ForegroundColorSpan(Color.parseColor("#95120a"));

            ss.setSpan(fcRed, start, ss.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ss;
    }

    private String phone;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @NonNull
    @Override
    public String toString() {

        String ubicacion = "No";

        if (isOnSite != null && isOnSite) {

            if (address != null && !address.isEmpty()) {
                ubicacion = address;
            } else if (lat != null && lng != null && lat != -1) {
                ubicacion = lat + ", " + lng;
            } else {
                ubicacion = "Sin ubicación";
            }
        }

        return "Booking Number: " + id + "\n"
                + "Booking date: " + date + "\n"
                + "Booking type: " + type + "\n"
                + "Vehicle: " + vehicle + "\n"
                + "Terreno: " + (isOnSite != null && isOnSite ? "Sí" : "No") + "\n"
                + "Ubicación: " + ubicacion + "\n"
                + "Teléfono: " + (phone != null ? phone : "N/A") + "\n"
                + "status: " + status;
    }

    public String toStringWithFullInformation() {

        String mechanicStr = mechanic != null ? mechanic.toString() : "No mechanic";
        String costStr = cost != null ? cost.toString() : "No cost";

        String shiftStr = "";

        if (shifts != null) {
            for (Shift s : shifts) {
                if (shiftStr.isEmpty())
                    shiftStr = s.toString();
                else
                    shiftStr += " / " + s.toString();
            }
        }

        return "Booking Number: " + id + "\n"
                + "Booking date: " + date + "\n"
                + "Booking type: " + type + "\n"
                + "Status: " + status + "\n"
                + "User: " + user + "\n"
                + "Vehicle: " + vehicle + "\n"
                + "Shift/s: " + shiftStr + "\n"
                + mechanicStr + "\n"
                + costStr;
    }
}