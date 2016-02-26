/**
 * This class contains a method to remove disaster users after
 * a time period specified in the adminportal.properties file.
 * It will also email the users after a time period also
 * specified in the adminportal.properties file.
 */

package gov.fema.handler;

import gov.fema.adminportal.jdbc.valueobject.ManageDisasterUsersVO;
import gov.fema.adminportal.jdbc.dao.ManageDisasterUsersDao;
import gov.fema.adminportal.util.Config;
import gov.fema.adminportal.util.ParameterConstants;
import gov.fema.adminportal.util.SpringContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ManageDisasterUsers {
    /**
     * This method will be called by the scheduler and will remove, email or update users  
     * when their accounts are about to be removed from a disaster AD group.
     */

    public void manageDisasterUsers() throws Exception {      
        // Tariq's code will need to pull all the disaster users and iterate through them.
        String userName = "awadswo1";
        String emailAddress = "anthony.wadsworth@associates.dhs.gov";
        String disasterProgram = "ha";
        String disasterNumber = "1651";

        ManageDisasterUsersDao audit = (ManageDisasterUsersDao) SpringContext.getContext().getBean("manageDisasterUsersDao");
        List<ManageDisasterUsersVO> boDisasterLogonAuditVOs = audit.selectDisasterUsers(userName, disasterProgram, disasterNumber);
        if (boDisasterLogonAuditVOs.size() > 0) {
            String emailHost = Config.getProperty(ParameterConstants.FEMA_SMTP_GATEWAY);
            String emailFrom = Config.getProperty(ParameterConstants.DISASTER_USER_FROM);
            String emailCc = Config.getProperty(ParameterConstants.DISASTER_USER_CC);
            String emailSubject = "";
            String emailBody = "";
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", emailHost);
            Session session = Session.getDefaultInstance(properties);
            BigDecimal numberOfDaysSinceAction = null;
            String action = "";
            for(ManageDisasterUsersVO boDisasterLogonAuditVO : boDisasterLogonAuditVOs) {
                //userName = boDisasterLogonAuditVO.getUserName();
                numberOfDaysSinceAction = boDisasterLogonAuditVO.getNumberOfDaysSinceAction();
                //emailAddress = boDisasterLogonAuditVO.getEmailAddress();
                //disasterProgram = boDisasterLogonAuditVO.getDisasterProgram().toLowerCase();
                //disasterNumber = boDisasterLogonAuditVO.getDisasterNumber().toString();
                action = boDisasterLogonAuditVO.getAction().toString();
            }

            Boolean sendEmail = false;
            BigDecimal expirationDay = new BigDecimal(Config.getProperty(ParameterConstants.DISASTER_USER_EXPIRATION_DAY));
            String expirationWarningDays = Config.getProperty(ParameterConstants.DISASTER_USER_EXPIRATION_WARNING_DAYS);
            for (String retval: expirationWarningDays.split(",")){
                if (numberOfDaysSinceAction.compareTo(expirationDay.subtract(new BigDecimal(retval))) == 0) {
                    sendEmail = true;
                }
            }

            if (sendEmail) {
                // User is about to be locked and an email is sent to warn them
                emailSubject = Config.getProperty(ParameterConstants.DISASTER_USER_SUBJECT) + disasterNumber + disasterProgram + " " + userName;
                String sep = System.getProperty("line.separator");
                emailBody = Config.getProperty(ParameterConstants.DISASTER_USER_BODY_LINE1) + sep + sep;
                emailBody = emailBody + "Due to account inactivity, your [" + userName +"]"
                        + " access to the [" + disasterNumber + disasterProgram +"]"
                        + " Disaster account will be removed in "
                        + expirationDay.subtract(numberOfDaysSinceAction) + " days." + sep + sep;
                emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_USER_BODY_LINE2) + sep + sep;
                emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_USER_BODY_LINE3) + sep + sep;
                emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_USER_SIGNATURE_LINE1) + sep;
                emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_USER_SIGNATURE_LINE2) + sep;
                emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_USER_SIGNATURE_LINE3);

                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(emailFrom));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
                    if (emailCc.isEmpty() == false) {
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailCc));
                    }
                    message.setSubject(emailSubject);
                    message.setText(emailBody);
                    //Transport.send(message);   
                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }
                //System.out.println("To: " + emailAddress);
                //System.out.println("Cc: " + emailCc);
                //System.out.println("From: " + emailFrom);
                //System.out.println("Subject: " + emailSubject);
                //System.out.println("Body: " + emailBody);
                System.out.println("Manage Disaster Users - " + userName + " sent email regarding "
                        + "being removed from the " + disasterNumber + disasterProgram + " group.");
                sendEmail = false;
            } else if (expirationDay.compareTo(numberOfDaysSinceAction) == 0) {
                // User is being removed from the group
                // Tariq will insert code to remove the user from the appropriate group
                audit.insertDisasterUsers(userName, "Removed", emailAddress, disasterProgram, disasterNumber);
                System.out.println("Manage Disaster Users - " + userName + " removed from " 
                        + disasterNumber + disasterProgram + " group.");
            } else if ((action.equals("Removed")) || (expirationDay.compareTo(numberOfDaysSinceAction) == -1)) {
                // User was previously removed from the group and has now been re-added to it
                audit.insertDisasterUsers(userName, "Readded", emailAddress, disasterProgram, disasterNumber);
                System.out.println("Manage Disaster Users - " + userName + " readded to the " 
                        + disasterNumber + disasterProgram + " group in the audit table.");
            }
        } else {
            // User was added to the group today and has not yet logged on
            audit.insertDisasterUsers(userName, "Added", emailAddress, disasterProgram, disasterNumber);
            System.out.println("Manage Disaster Users - " + userName + " added to the " 
                    + disasterNumber + disasterProgram + " group in the audit table.");
        }
    }
}
