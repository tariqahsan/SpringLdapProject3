/**
 * This class is used to display the disasterSecurity.jsp page
 */
package gov.fema.adminportal.presentation.usermgmt.action;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import gov.fema.adminportal.util.*;
import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.repository.LdapRepository;
import gov.fema.adminportal.presentation.common.action.BobjAction;
import gov.fema.adminportal.presentation.usermgmt.form.*;

import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.CeKind;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAlias;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAliases;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.IUserGroup;

public class DisasterSecurityAction extends BobjAction {

    public ActionForward processAction(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
                    throws Exception {
        String forward = WebConstants.LOGOUT;
        DisasterUserForm userForm = (DisasterUserForm) form;
        HttpSession session = request.getSession();
        IEnterpriseSession enterpriseSession = (IEnterpriseSession) session
                .getAttribute(WebSessionConstants.ENTERPRISE_SESSION);

        // Enterprise session needed to get here, else go back to Login/Logout page
        if (enterpriseSession != null) {
            String action = userForm.getAction();

            if (action == null || action.trim().length() == 0) {
                populatePage(session, request, enterpriseSession, userForm);
                forward = WebConstants.DISASTER_USER;
            } else {
                forward = action;
            }
        } else {
            forward = WebConstants.LOGOUT;
        }
        request.setAttribute(WebConstants.DISASTER_USER_FORM, userForm);
        return mapping.findForward(forward);
    }

    private void populatePage(HttpSession session, HttpServletRequest request, 
            IEnterpriseSession enterpriseSession, DisasterUserForm userForm) throws Exception {
        IInfoStore infostore = (IInfoStore) enterpriseSession.getService(WebConstants.INFOSTORE);       

        // Determine if user has permission to this utility
        String strAccessRights = Config.getProperty(ParameterConstants.AP_DISASTER_USER_SECURITY_AUTHORIZED);
        List userGroupCollection = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(strAccessRights, ",");
        while (tokenizer.hasMoreTokens()) {
            userGroupCollection.add(tokenizer.nextToken());
        }
        List groupList = (List) session.getAttribute(WebSessionConstants.GROUP_LIST);
        boolean isAuthenticated = false;
        for(int i = 0; i < groupList.size(); i++) {
            if (userGroupCollection.contains(groupList.get(i)))
                isAuthenticated = true;
        }
        if (isAuthenticated) {
            userForm.setAuthenticated(true);
        }

        String pageCounter = userForm.getPageCounter();
        String disasterNumber = userForm.getDisasterNumber();       
        if (NumberUtils.isNumber(disasterNumber)) {
            int disasterNumberConverted = Integer.parseInt(disasterNumber);
            if (disasterNumberConverted >= 1000 && disasterNumberConverted <= 9999) {
                if (pageCounter.equals("2")) {
                    // Fetch potential disaster groups that could be added
                    List<String> disasterGroups = new ArrayList<String>();
                    String iaPgmGroups = Config.getProperty(ParameterConstants.AP_DISASTER_USER_GROUPS);
                    StringTokenizer pgmAreaTokens = new StringTokenizer(iaPgmGroups, ",");
                    while (pgmAreaTokens.hasMoreTokens()) {
                        disasterGroups.add(pgmAreaTokens.nextToken().toString().toLowerCase());
                    }
                    userForm.setDisasterGroups(disasterGroups);
                }
                else if (pageCounter.equals("3")) {
                    populateDisasterSecurity((String) session.getAttribute(WebSessionConstants.LOGON_TOKEN), 
                            request, infostore, userForm, disasterNumber);
                }
            }
            else {
                userForm.setPageCounter("1m");
                userForm.setMessage("You entered a number outside the acceptable range. Please enter a number between 1000 and 9999.");
            }
        }
        else if (pageCounter.equals("2")){
            userForm.setPageCounter("1m");
            userForm.setMessage("You entered a non numeric character. Please enter a number between 1000 and 9999.");
        }
    }

    private void populateDisasterSecurity(String strLogin, HttpServletRequest request,
            IInfoStore infostore, DisasterUserForm userForm, String disasterNumber) throws Exception {

        String message = "";
        Enumeration parameters = request.getParameterNames();
        while(parameters.hasMoreElements()){
            String parameterName = (String)parameters.nextElement();
            String parameterValue = request.getParameter(parameterName);
            // Add service accounts in Business Objects and add to appropriate BO group if necessary
            if (parameterValue.equals("on")) {
                String userName = disasterNumber + parameterName;
                String boUserSQL = "SELECT SI_ID, SI_NAME, SI_ALIASES FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'User' AND SI_NAME = '" + userName + "'";
                IInfoObjects user = infostore.query(boUserSQL);
                if(user.size() == 0) {
                    String userGroup = parameterName.toUpperCase() + " Users";
                    String boGroupSQL = "SELECT SI_ID, SI_GROUP_MEMBERS FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'UserGroup' AND SI_NAME = '" + userGroup + "'";
                    IInfoObjects groups = infostore.query(boGroupSQL);
                    IUserGroup group = (IUserGroup) groups.get(0);
                    IInfoObjects newUsers = infostore.newInfoObjectCollection();
                    IUser newUser = (IUser) newUsers.add(CeKind.USER);
                    newUser.setTitle(userName);
                    newUser.setDescription("Disaster Service Account");
                    newUser.setPasswordExpiryAllowed(false);
                    newUser.setPasswordToChangeAtNextLogon(false);
                    newUser.setPasswordChangeAllowed(false);
                    long aRandomNumber = (long) (Math.random() * 100000000); // Set the password to a random number string
                    newUser.setNewPassword(aRandomNumber+"");
                    Set groupsOfNewUser = newUser.getGroups();
                    groupsOfNewUser.add(Integer.parseInt(group.properties().getString("SI_ID")));
                    infostore.commit(newUsers);
                    message = message + userName + " Business Objects Service Account created.<BR>";
                }
                else {
                    Iterator usrObjIter = user.iterator();
                    IUser existingUser = null;
                    IUserAliases userAliases;
                    while (usrObjIter.hasNext()) {
                        existingUser = (IUser) usrObjIter.next();
                        userAliases = existingUser.getAliases();

                        Iterator ialias = userAliases.iterator();     
                        while (ialias.hasNext()) {
                            IUserAlias userAlias;
                            userAlias = (IUserAlias) ialias.next();

                            if (userAlias.isDisabled()) {
                                userAlias.setDisabled(false);
                                infostore.commit(user);
                                message = message + userName + " Business Objects Service Account was unlocked.<BR>";
                            }
                            else {
                                message = message + userName + " Business Objects Service Account already exists and was not locked.<BR>";
                            }
                        }

                    }
                }
            }
            // Add Active Directory groups and assign appropriate administrative privileges as necessary
            if (parameterValue.equals("on")) {

                // Configure LdapRepository 
                LdapRepository ldapDao = (LdapRepository) SpringContext.getContext().getBean("ldapReposImpl");

                // LDAP related operations

                // Create Group object
                //Group group = new Group();
                //group.setCn("Business_Objects_Disaster_Users_1454da");
                //group.setDisasterNumber(disasterNumber);
                //group.setReportType(parameterName);

                // Create a Group in AD
                //ldapDao.createGroup(group);
            }
        }
        userForm.setPageCounter("1m");
        userForm.setMessage(message.substring(0, message.length() - 4));
    }
}
