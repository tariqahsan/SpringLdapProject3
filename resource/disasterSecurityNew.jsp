<%@ page import="com.crystaldecisions.sdk.exception.SDKException,
                 com.crystaldecisions.sdk.framework.*,
                 com.crystaldecisions.sdk.properties.*,
                 com.crystaldecisions.sdk.occa.infostore.*,
                 com.crystaldecisions.sdk.plugin.CeKind,
                 com.crystaldecisions.sdk.plugin.desktop.user.*,
                 com.crystaldecisions.sdk.plugin.desktop.usergroup.*,
                 gov.fema.adminportal.util.*,
                 gov.fema.adminportal.jdbc.valueobject.*,
                 gov.fema.adminportal.jdbc.dao.*,
		 gov.fema.adminportal.presentation.usermgmt.form.*,
                 org.springframework.jdbc.core.*,
                 org.springframework.context.support.*,
                 oracle.jdbc.driver.*,
                  javax.sql.*,
                 java.sql.*,
                 java.lang.*,
                 java.util.*"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<jsp:include page="../tiles/headerNew.jsp"/>
<%
DisasterUserForm disasterUserForm = (DisasterUserForm)request.getAttribute(WebConstants.DISASTER_USER_FORM);
IEnterpriseSession enterpriseSession = (IEnterpriseSession)session.getAttribute(WebSessionConstants.ENTERPRISE_SESSION);
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

if(isAuthenticated) {
  String strAccountType;
  if((disasterUserForm.getAccountType() == null) || (disasterUserForm.getAccountType().trim().length() == 0)) 
    strAccountType = "HA";
  else
    strAccountType = disasterUserForm.getAccountType();

  // Fetch the previous selected disaster number
  String selectId = disasterUserForm.getDisasterNumber();
  if (selectId == null) {
	selectId = "";
  }

  // EDW Database Connection Variables
  String strEDWRowLevelPackage = Config.getProperty(ParameterConstants.EDW_ROW_LEVEL_SECURITY_PACKAGE);

%>
<!--html-->
  <head>
    <script type="text/javascript">
      function SubmitForm() {
          disasterUserForm.submit();
      }

      function LogOut() {
          disasterUserForm.action.value = "logout";
          SubmitForm();
      }

      function AdminHome() {
          disasterUserForm.action.value = "adminportal";
          SubmitForm();
      }

      function AssociateUsers() {
          if (disasterUserForm.associateUser.selectedIndex !=  - 1)
              SubmitForm();
          else 
              alert("Please select user(s) to associate first.");
      }
    </script>
  </head>
  <!--body-->
    <html:form action="/disasterSecurity">
      <table>
        <tr>
          <td width="500" align="left"><h2>Disaster User Security</h2></td>
        </tr>
        <tr>
          <td colspan="2">&nbsp;</td>
        </tr>
      </table>
      <table>
        <tr>
          <td width="120">
            <b>Group:</b>
          </td>
          <td width="880">
            <select name="AccountType" onchange="SubmitForm()">
              <option value="DA"<%if(strAccountType.equals("DA")) out.print(" selected");%>>DA - DARAC</option>
              <option value="HA"<%if(strAccountType.equals("HA")) out.print(" selected");%>>HA - Housing Assistance</option>
              <option value="ON"<%if(strAccountType.equals("ON")) out.print(" selected");%>>ON - Other Needs</option>
            </select>

          </td>
        </tr>
         
        <tr>
          <td>
            <b>Disaster:</b>
          </td>
          <td>
            <select name="disasterNumber" onchange="SubmitForm()">
            <%// *** BEGIN GET DISASTER INFORMATION ***
		    String strDisasterNumber = "";
            DisasterSecurityDao sproc = (DisasterSecurityDao)SpringContext.getContext().getBean("disasterSecurityDao");
		    if(request.getParameter("disasterNumber") == null)
			  strDisasterNumber = "";
		    else
			  strDisasterNumber = request.getParameter("disasterNumber");

            try {
                List disasters = sproc.getDisasters("AI");

                if (!disasters.isEmpty()) {
                    LabelValueBean bean = null;
                    boolean selected = false;
                    Iterator iter = disasters.iterator();
                    while (iter.hasNext()) {
                      bean = (LabelValueBean)iter.next();
					  if (strDisasterNumber == "") {
						  strDisasterNumber = bean.getValue();
					  }
                      if (bean.getValue().equals(selectId)) {
                              selected = true;
                      }%>
              <option <% if(selected) {%>selected<% }%> value='<%=bean.getValue()%>'><%=bean.getLabel()%></option>
              <%      selected = false;
                    }
                } 
            } catch (Exception ioe) {
                out.print(ioe.getMessage());
                ioe.printStackTrace();
            }
            // *** END GET DISASTER INFORMATION ***%>
            </select>
          </td>
        </tr>
      </table>
      <br></br>
      <%// *** BEGIN CREATE OR UPDATE USER GROUP ***
      String[] strAssociateUser = request.getParameterValues("associateUser"); 

      if(strAssociateUser != null) {
        String strUserGroup = strDisasterNumber + strAccountType.toLowerCase();
        String strBOGroupSQL = "SELECT SI_ID, SI_GROUP_MEMBERS FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'UserGroup' AND SI_NAME = '" + strUserGroup + "'";
        IInfoObjects groups1 = infostore.query(strBOGroupSQL);
        if(groups1.size() == 0){
          IInfoObjects newUserGroups = infostore.newInfoObjectCollection();
          IUserGroup newUserGroup = (IUserGroup) newUserGroups.add(CeKind.USERGROUP);
          newUserGroup.setTitle(strUserGroup);
          infostore.commit(newUserGroups);

          // Add new group as member of appropriate parent group
          Integer intNewGroupID = newUserGroup.getID();
          IInfoObjects rUserGroups = infostore.query("SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='UserGroup' AND SI_NAME='" + strAccountType + " Users'"); 
          IInfoObject rUserGroup = (IInfoObject) rUserGroups.get(0); 
          ((IUserGroup) rUserGroup).getSubGroups().add(intNewGroupID); 
          infostore.commit(rUserGroups);

          groups1 = infostore.query(strBOGroupSQL);
        }
        if(groups1.size() > 0){
          IUserGroup group = (IUserGroup) groups1.get(0);
          for (int i = 0; i < strAssociateUser.length; i++) {
            if(strAssociateUser[i].substring(0,2).equals("0_")) { // Create new user and assign to appropriate group
              IInfoObjects newUsers = infostore.newInfoObjectCollection();
              IUser newUser = (IUser) newUsers.add(CeKind.USER);
              String strTitle = strDisasterNumber + strAssociateUser[i].substring(2,strAssociateUser[i].length());
              newUser.setTitle(strTitle);
              newUser.setPasswordExpiryAllowed(false);
              newUser.setPasswordToChangeAtNextLogon(false);
              newUser.setPasswordChangeAllowed(false);
              long aRandomNumber = (long) (Math.random() * 100000000); // Set the password to a random number string
              newUser.setNewPassword(aRandomNumber+"");
              Set groupsOfNewUser = newUser.getGroups();
              groupsOfNewUser.add(Integer.parseInt(group.properties().getString("SI_ID")));
              infostore.commit(newUsers);
            }
            else {
              String strBOUserSQL1 = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'User' AND SI_ID = '" + strAssociateUser[i] + "'";
              IInfoObjects existingUsers = infostore.query(strBOUserSQL1);
              IInfoObject BOUsersObject = (IInfoObject) existingUsers.get(0);
              IUser existingUser = (IUser) BOUsersObject;

              List grpsToBeRemoved = new ArrayList();

              // Remove user from all existing groups except EVERYONE and groups with same Disaster Number
              Integer intGroupID = Integer.parseInt(group.properties().getString("SI_ID"));
              for (Iterator iter2 = existingUser.getGroups().iterator(); iter2.hasNext();) {
                Integer intRemoveGroupID = (Integer)iter2.next();
                if (!intRemoveGroupID.equals(1) && !intRemoveGroupID.equals(intGroupID)) {
                  String strBOUserSQL2 = "SELECT SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'UserGroup' AND SI_ID = '" + intRemoveGroupID + "'";
                  IInfoObjects groups2 = infostore.query(strBOUserSQL2);
                  IInfoObject group2 = (IInfoObject) groups2.get(0); 
                  String strOldGroup = group2.properties().getString("SI_NAME").substring(0,4);
                  if(!strOldGroup.equals(strDisasterNumber)) {
                    grpsToBeRemoved.add(intRemoveGroupID);
                  }
                }
              }

              if (grpsToBeRemoved != null && grpsToBeRemoved.size() > 0) {
                // Iterate and remove it from existingUser.getGroups
                Iterator grpsRmvIter = grpsToBeRemoved.iterator();
                while (grpsRmvIter.hasNext()) {
                    existingUser.getGroups().remove((Integer)grpsRmvIter.next());
                    infostore.commit(existingUsers);
                }
              }

              String strTitle = strDisasterNumber + existingUser.properties().getString("SI_NAME").substring(4,existingUser.properties().getString("SI_NAME").length());
              existingUser.setTitle(strTitle);
              Set groupsOfExistingUser = existingUser.getGroups();
              groupsOfExistingUser.add(intGroupID);
              infostore.commit(existingUsers);
            }
          }
        }
      }
      // *** END CREATE OR UPDATE USER GROUP ***%>
      <table border="0">
        <tr>
          <th width="0" align="left">
            Available
            <%out.print(strAccountType.toUpperCase());%>
            Users
          </th>
          <th></th>
          <th align="left">
            Current
            <%out.print(strAccountType.toUpperCase());%>
            Users Associated To
            <%out.print(strDisasterNumber);%>
          </th>
        </tr>
         
        <tr>
          <td>
            <select name="associateUser" size="20" multiple="multiple" style="width: 250px">
            <%
            String strUsers = "";
            if(strAccountType.equals("HA"))
              strUsers = "cmumaw,mbirchen,rreed2";
            else if(strAccountType.equals("ON"))
              strUsers = "mbirchen,redwar10";
            else if(strAccountType.equals("DA"))
              strUsers = "mbirchen";
            String[] userlist = strUsers.split(",");
            if(strUsers != "") {
              for (int i = 0; i < userlist.length; i++) {
                String strBOGroupSQL1 = "SELECT SI_ID, SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'User' AND SI_NAME LIKE '____" + userlist[i] + "'";
                IInfoObjects user = infostore.query(strBOGroupSQL1);
                Iterator usrObjIter = user.iterator();
                if(user.size() > 0) {
                  while (usrObjIter.hasNext()) {
                    IUser displayUser = (IUser)usrObjIter.next();
                    String strDisplayUser = displayUser.properties().getString("SI_NAME");
                    try {
                      int aInt = Integer.parseInt(strDisplayUser.substring(0,4));
                      out.println("<option value='" + displayUser.properties().getString("SI_ID") + "'>" + strDisplayUser.substring(4,strDisplayUser.length()) + "</option>");
                    }
                    catch (Exception e) {};
                  }
                }
                else {
                  out.println("<option value='0_" + userlist[i] + "'>" + userlist[i] + "</option>");
                }
              }
            }
            %>
            </select>
          </td>
          <td>
            <input type="button" value="Associate Users >>" onclick="AssociateUsers()" style="width: 145px"></input>
          </td>
          <td valign="top">
            <select size='20' style="width: 250px" disabled>
            <%String strBOGroupSQL2 = "SELECT SI_ID, SI_GROUP_MEMBERS FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'UserGroup' AND SI_NAME = '" + strDisasterNumber + strAccountType.toLowerCase() + "'"+ " ORDER BY SI_GROUP_MEMBERS";

            IInfoObjects groups = infostore.query(strBOGroupSQL2);
            if(groups.size() > 0){
              IUserGroup group = (IUserGroup)groups.get(0);
              Set assignedUsers = group.getUsers();
              Iterator it2 = assignedUsers.iterator();

              String strBOUserSQL2;
              String strGroupIDs = "";
              while(it2.hasNext()) {
                Object gid2 = it2.next();
                strGroupIDs = strGroupIDs + gid2;
                if(it2.hasNext())
                  strGroupIDs = strGroupIDs + ",";
              }

              strBOUserSQL2 = "SELECT SI_NAME, SI_USERFULLNAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'User' AND SI_ID IN (" + strGroupIDs + ") ORDER BY SI_NAME";
              if( strGroupIDs != "") {
                IInfoObjects BOUsers = infostore.query(strBOUserSQL2);
                if(BOUsers.size() > 0){
                  for (int i = 0; i < BOUsers.size(); i++) {
                    IInfoObject BOUsersObject = (IInfoObject) BOUsers.get(i);
                    String strStrippedName = BOUsersObject.getTitle().substring(4,BOUsersObject.getTitle().length());
                    out.println("<option>" + strStrippedName + "</option>");
                    //if(BOUsersObject.properties().getProperty("SI_USERFULLNAME").toString().equals("") == false)
                      //out.print(" - " + BOUsersObject.properties().getProperty("SI_USERFULLNAME"));
                  }
                }
              }
            }%>
			</select>
          </td>
        </tr>
      </table>
    </html:form>
  <!--/body>
</html-->
<%
} // is authenticated
else {
    out.println("You do not have access to this page");
}
%>
<jsp:include page="../tiles/footerNew.jsp"/>

