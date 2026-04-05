package com.ger.garage.model;

import java.time.DayOfWeek;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*

Class SetUpDao: Direct Access Object class, this class was implemented to access to customizable data
=

 */

public class SetUpDao {

    // I started to save everythin in firestore but i didnt have time to code that part, so I created this class
    // to save customizations
    // Vehicle make, Vehicle type and more
    private ArrayList<String> vehicleType = new ArrayList<>(Arrays.asList("car", "motorbike", "small van", "small bus"));
    private ArrayList<String> vehicleEngineType = new ArrayList<>(Arrays.asList("diesel", "petrol", "hybrid", "electric"));
    private ArrayList<String> carMake = new ArrayList<>(Arrays.asList("ford", "volkswagen", "chevrolet", "peugeot", "kia", "fiat","nissan", "audi"));
    private ArrayList<String> motorbikeMake = new ArrayList<>(Arrays.asList("harley davidson", "ducati", "bmw", "honda", "yamaha", "kawasaki", "triumph", "suzuki"));
    private ArrayList<String> smallBusMake = new ArrayList<>(Arrays.asList("toyota", "citroen", "ford", "mercedes", "chevrolet"));
    private ArrayList<String> smallVanMake = new ArrayList<>(Arrays.asList("citroen", "ford", "mercedes", "mitsubishi", "nissan", "renault","volkswagen", "toyota"));
    private ArrayList<String> statusTypeBooking = new ArrayList<>(Arrays.asList("booked","in service","fixed","unrepairable", "collected"));
    private ArrayList<String> typeOfBooking = new ArrayList<>(Arrays.asList("annual service","major service","repair","major repair"));
    // this arrayList must be ordered by startTime
    private ArrayList<Shift> shifts = new ArrayList<>();
    // Quantity of bookings by shifts
    private Integer quantityBookingsByShift;
    private ArrayList<DayOfWeek> workingDays = new ArrayList<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
    // Quantity of shifts for Annual Service
    private Integer quantityAnnualServiceShift;
    // Quantity of shifts for Mayor Service
    private Integer quantityMajorServiceShift;
    // Quantity of shifts for Repair
    private Integer quantityRepairShift;
    // Quantity of shifts for Mayor Repair
    private Integer quantityMajorRepairShift;
    private String emailAdmin = "jonathancofre998@gmail.com";

    public Mechanic getMechanic(Integer id) {

        for (Mechanic m: mechanics) {

            if (m.getId() == id)
                return m;

        }

        return null;

    }

    public ArrayList<Mechanic> getMechanics() {
        return mechanics;
    }

    private ArrayList<Mechanic> mechanics;

    public ArrayList<String> getListOfStatus(String status) {

        return statusGraph.get(status);
    }

    private HashMap<String, ArrayList<String>> statusGraph;

    public String getEmailAdmin() {
        return emailAdmin;
    }

    public String getFirstStatus() {

        return "booked";

    }

    public Integer getQuantityOfShiftsByTypeOfBooking(String typeOfBooking) {

        switch (typeOfBooking) {

            case "annual service":
                return quantityAnnualServiceShift;
            case "major service":
                return quantityMajorServiceShift;
            case "repair":
                return quantityRepairShift;
            case "major repair":
                return quantityMajorRepairShift;
            default:
                throw new IllegalStateException("Unexpected value: " + typeOfBooking);
        }


    }


    public ArrayList<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public Integer getQuantityBookingsByShift() {
        return quantityBookingsByShift;
    }

    public ArrayList<Shift> getShifts() {
        return shifts;
    }

    public SetUpDao() {

        Shift shift = new Shift(1, "Morning 1", LocalTime.of(8,0), LocalTime.of(10,0));
        shifts.add(shift);
        shift = new Shift(2, "Morning 2", LocalTime.of(10,0), LocalTime.of(12,0));
        shifts.add(shift);
        shift = new Shift(3, "Afternoon 1", LocalTime.of(13,0), LocalTime.of(15,0));
        shifts.add(shift);
        shift = new Shift(4, "Afternoon 2", LocalTime.of(15,0), LocalTime.of(17,0));
        shifts.add(shift);
        this.quantityBookingsByShift = 4;
        this.quantityAnnualServiceShift = 1;
        this.quantityMajorRepairShift = 2;
        this.quantityMajorServiceShift = 1;
        this.quantityRepairShift = 1;

        // I created a graph of status using one Hasmap and arrayLists
        // My plan is define methods related two status in this class
        // For example can allocate mechanic method or allocate cost method? could be implemented here
        // and uses this structure. Another option define this issue in Booking Clsss
        statusGraph = new HashMap<>();
        statusGraph.put("booked", new ArrayList<String>(Arrays.asList("in service")));
        statusGraph.put("in service", new ArrayList<String>(Arrays.asList("unrepairable", "fixed")));
        statusGraph.put("unrepairable", new ArrayList<String>());
        statusGraph.put("fixed", new ArrayList<String>(Arrays.asList("collected")));
        statusGraph.put("collected", new ArrayList<String>());

        mechanics = new ArrayList<>();

        Mechanic mechanic = new Mechanic(1, "John");
        mechanics.add(mechanic);
        mechanic = new Mechanic(2, "Bob");
        mechanics.add(mechanic);
        mechanic = new Mechanic(3, "Pauk");
        mechanics.add(mechanic);
        mechanic = new Mechanic(4, "Francisco");
        mechanics.add(mechanic);

    }



    public ArrayList<String> getVehicleType() {
        return vehicleType;
    }

    public ArrayList<String> getVehicleEngineType() {
        return vehicleEngineType;
    }

    public ArrayList<String> getCarMake() {
        return carMake;
    }

    public ArrayList<String> getMotorbikeMake() {
        return motorbikeMake;
    }

    public ArrayList<String> getSmallBusMake() {
        return smallBusMake;
    }

    public ArrayList<String> getSmallVanMake() {
        return smallVanMake;
    }

    public ArrayList<String> getStatusTypeBooking() {
        return statusTypeBooking;
    }

    public ArrayList<String> getTypeOfBooking() {
        return typeOfBooking;
    }

}
