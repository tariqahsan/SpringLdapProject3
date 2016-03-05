/**
 * This class contains method to manage user accounts.
 */

package gov.fema.handler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.repository.LdapRepository;
import gov.fema.adminportal.util.*;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

public class DeleteDisasterGroups {
	
	private static Logger log = Logger.getLogger(DeleteDisasterGroups.class);

    /**
     * This method will be called by the scheduler and will delete inactive 
     * disaster AD groups and their corresponding BO service account users.
     */
    public void deleteGroups() throws Exception {
        // Instantiate BO credential variables with appropriate user information
        String strUserName = Config.getProperty(ParameterConstants.BO_USERNAME);
        String strUserPass = Config.getProperty(ParameterConstants.BO_PASSWORD);
        String strCMSName = Config.getProperty(ParameterConstants.BO_CMS);
        String strAuthType = "secEnterprise";

        // Configure Initial BO Enterprise Session
        IEnterpriseSession enterpriseSession = null;
        ISessionMgr sm = CrystalEnterprise.getSessionMgr();
        enterpriseSession = sm.logon(strUserName, strUserPass, strCMSName,strAuthType);
        IInfoStore infostore = (IInfoStore) enterpriseSession.getService("","InfoStore");

        String strUserSQL = "SELECT TOP 10000 si_id, si_name, si_lastlogontime, si_update_ts, si_aliases "
                + "FROM ci_systemobjects WHERE si_kind = 'User'";
        IInfoObjects users = infostore.query(strUserSQL);
        Iterator usrObjIter = users.iterator();
        IUser user = null;
        IUserAliases userAliases;
        String userName = "";
        String disasterProgram = "";
        String disasterNumber = "";
        String iaPgmGroups = Config.getProperty(ParameterConstants.AP_DISASTER_USER_GROUPS).toLowerCase();
        while (usrObjIter.hasNext()) {
            user = (IUser) usrObjIter.next();
            userAliases = user.getAliases();
            for (Iterator ialias = userAliases.iterator(); ialias.hasNext();) {
                userName = user.properties().getProperty("si_name").toString();

                // Disaster accounts have to be exactly 6 characters in length
                if(userName.length() == 6) {
                    disasterProgram = userName.substring(4,6);
                    disasterNumber = userName.substring(0,4);

                    // Confirm that the account is a disaster account and whether or not it should be deleted
                    if (NumberUtils.isNumber(disasterNumber) && iaPgmGroups.matches(".*" + disasterProgram + ".*")) {
                        SimpleDateFormat formater = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
                        Date tempDate = null;
                        Date dtLastLoginDate = new Date(0, 0, 0);
                        if (user.properties().getProperty("si_lastlogontime") != null) {
                            tempDate = user.properties().getDate("si_lastlogontime");
                            if (tempDate != null) {
                                dtLastLoginDate = tempDate;
                            }
                        }
                        tempDate = user.properties().getDate("si_update_ts");
                        Date dtLastActivityDate = new Date(0, 0, 0);
                        if (user.properties().getProperty("si_update_ts") != null) {
                            if (tempDate != null) {
                                dtLastActivityDate = tempDate;
                            }
                        }
                        Calendar dtToday = Calendar.getInstance();
                        int intExpirationDays = Integer.parseInt(Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_DAY)) * -1;
                        dtToday.add(Calendar.DATE, intExpirationDays);
                        Date dtDeleteDate = new Date(dtToday.getTimeInMillis());

                        // If the account has been inactive for a certain number of days, delete it
                        if (dtLastLoginDate.before(dtDeleteDate) && dtLastActivityDate.before(dtDeleteDate)) {
                            // Remove Business Objects Service Account
                            user.deleteNow();
                            infostore.commit(users);
                            log.info("Delete Disaster Groups - Deleted the " + userName + " account.");

                            // Remove Active Directory group and corresponding admin group(s) if necessary
                			                           
                            // Configure LdapRepository 
                            LdapRepository ldapDao = (LdapRepository) SpringContext.getContext().getBean("ldapReposImpl");

                            log.info("disasterNumber :" + disasterNumber);
                            log.info("disasterProgram :" + disasterProgram); 
                            Group group = new Group();
                			group.setDisasterNumber(disasterNumber);
                			group.setReportType(disasterProgram);
                			group.setOu(disasterNumber);
                			
                			log.info("=== Group Deletion Started ==="); 
                			ldapDao.removeGroup(group);
                            log.info("=== Admin Group Deletion Started ==="); 
                            ldapDao.removeAdminGroup(group, Config.getProperty(ParameterConstants.BO_DISASTER_NUMBER_ONLY));
                            ldapDao.removeAdminGroup(group, Config.getProperty(ParameterConstants.BO_DISASTER_NUMBER_PROGRAM));  
                            ldapDao.removeAdminOrganizationalUnit(group);
                            log.info("=== Group Deletion Ended ===");
                            
                        } else {
                            // Check if an email needs to be sent to warn about upcoming account deletion
                            String emailHost = Config.getProperty(ParameterConstants.FEMA_SMTP_GATEWAY);
                            String emailTo = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_TO);
                            String emailFrom = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_FROM);
                            String emailCc = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_CC);
                            String emailSubject = "";
                            String emailBody = "";
                            Properties properties = System.getProperties();
                            properties.setProperty("mail.smtp.host", emailHost);
                            Session session = Session.getDefaultInstance(properties);
                            Integer expirationDay = intExpirationDays * -1;
                            String expirationWarningDays = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_WARNING_DAYS);                                                        
                            Date today = Calendar.getInstance().getTime();
                            long activityDateDifference = TimeUnit.DAYS.convert(Math.abs(today.getTime() - dtLastActivityDate.getTime()), TimeUnit.MILLISECONDS);
                            long lastLoginDateDifference = TimeUnit.DAYS.convert(Math.abs(today.getTime() - dtLastLoginDate.getTime()), TimeUnit.MILLISECONDS);
                            long numberOfDaysSinceAction;
                            if (lastLoginDateDifference < activityDateDifference) {
                                numberOfDaysSinceAction = lastLoginDateDifference;
                            } else {
                                numberOfDaysSinceAction = activityDateDifference;
                            }
                            for (String retval: expirationWarningDays.split(",")){
                                if (numberOfDaysSinceAction == (expirationDay - new Integer(retval))) {
                                    emailSubject = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_SUBJECT) + disasterNumber + disasterProgram;
                                    String sep = System.getProperty("line.separator");
                                    emailBody = Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_BODY_LINE1) + sep + sep;
                                    emailBody = emailBody + "Due to account inactivity, the [" + userName +"]"
                                            + " Disaster account will be removed in "
                                            + (expirationDay - numberOfDaysSinceAction) + " days." + sep + sep;                                   
                                    emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_BODY_LINE2) + sep + sep;
                                    emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_BODY_LINE3) + sep + sep;
                                    emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_SIGNATURE_LINE1) + sep;
                                    emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_SIGNATURE_LINE2) + sep;
                                    emailBody = emailBody + Config.getProperty(ParameterConstants.DISASTER_ADMIN_USER_SIGNATURE_LINE3);

                                    try {
                                        MimeMessage message = new MimeMessage(session);
                                        message.setFrom(new InternetAddress(emailFrom));
                                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
                                        if (emailCc.isEmpty() == false) {
                                            message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailCc));
                                        }
                                        message.setSubject(emailSubject);
                                        message.setText(emailBody);
                                        Transport.send(message);
                                        log.info("Delete Disaster Groups - Sent email regarding the " + userName + " account.");
                                    } catch (MessagingException mex) {
                                        mex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                ialias.next();
            }
        }

        // Close Administrator BO Enterprise Session and clear authenticated session variable
        enterpriseSession.logoff();
    }
    
    // Send recipient with one email listing all required content instead of sending individual emails.
    
}
