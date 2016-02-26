package gov.fema.adminportal.jdbc.valueobject;

import java.math.BigDecimal;

public class ManageDisasterUsersVO {
    private String userName;
    private BigDecimal numberOfDaysSinceAction;
    private String emailAddress;
    private String disasterProgram;
    private BigDecimal disasterNumber;
    private String action;

    public ManageDisasterUsersVO() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getNumberOfDaysSinceAction() {
        return numberOfDaysSinceAction;
    }

    public void setNumberOfDaysSinceAction(BigDecimal numberOfDaysSinceAction) {
        this.numberOfDaysSinceAction = numberOfDaysSinceAction;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDisasterProgram() {
        return disasterProgram;
    }

    public void setDisasterProgram(String disasterProgram) {
        this.disasterProgram = disasterProgram;
    }

    public BigDecimal getDisasterNumber() {
        return disasterNumber;
    }

    public void setDisasterNumber(BigDecimal disasterNumber) {
        this.disasterNumber = disasterNumber;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
