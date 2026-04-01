package org.example;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Launch {
    private String id;
    private String name;

    @SerializedName("flight_number")
    private int flightNumber;

    @SerializedName("date_utc")
    private String dateUtc;

    private Boolean success;
    private boolean upcoming;
    private String details;

    private List<Failure> failures;
    private List<Core> cores;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public String getDateUtc() {
        return dateUtc;
    }

    public Boolean getSuccess() {
        return success;
    }

    public boolean isUpcoming() {
        return upcoming;
    }

    public String getDetails() {
        return details;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public List<Core> getCores() {
        return cores;
    }
}
