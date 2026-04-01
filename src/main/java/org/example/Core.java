package org.example;

import com.google.gson.annotations.SerializedName;

public class Core {
    private String core;
    private Integer flight;
    private Boolean gridfins;
    private Boolean legs;
    private Boolean reused;

    @SerializedName("landing_attempt")
    private Boolean landingAttempt;

    @SerializedName("landing_success")
    private Boolean landingSuccess;

    @SerializedName("landing_type")
    private String landingType;

    private String landpad;

    public String getCore() {
        return core;
    }

    public Integer getFlight() {
        return flight;
    }

    public Boolean getGridfins() {
        return gridfins;
    }

    public Boolean getLegs() {
        return legs;
    }

    public Boolean getReused() {
        return reused;
    }

    public Boolean getLandingAttempt() {
        return landingAttempt;
    }

    public Boolean getLandingSuccess() {
        return landingSuccess;
    }

    public String getLandingType() {
        return landingType;
    }

    public String getLandpad() {
        return landpad;
    }
}
