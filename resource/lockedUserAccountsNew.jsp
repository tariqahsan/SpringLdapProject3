<%@ page import="gov.fema.adminportal.util.*,
gov.fema.adminportal.presentation.usermgmt.form.*,
gov.fema.adminportal.wrapper.*,
java.text.*,
java.util.*"
%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/ajaxtags.tld" prefix="ajax"%>
<%@ taglib uri="/WEB-INF/displaytag.tld" prefix="display" %>
<jsp:include page="../tiles/headerNew.jsp"/>
<%
LockedUsrAccntsForm userForm = (LockedUsrAccntsForm)request.getAttribute(WebConstants.LCK_USR_ACCT_FORM);

int intPageSize = Integer.parseInt(Config.getProperty(ParameterConstants.AP_LOCKED_USER_ACCOUNTS_PAGESIZE));

if(userForm.isAuthenticated()) {
    String selectId = userForm.getProgramArea();
    if (selectId == null) {
        selectId = "";
    }
    //Get the Authentication Objects
    List<LabelValueBean> authenticationObjs = userForm.getAuthenticationObjs();

    String strAuthType = userForm.getAuthenticationType();
    if (strAuthType == null) {
        strAuthType = "";
    }

    // get the group name
    String subGroupName = userForm.getGroupName();
    List groupObjs = userForm.getGroupObjs();
    List<LabelValueBean> subGroupObjs = userForm.getSubGroupObjs();
    %>
    <!--HTML-->
    <HEAD>
    <SCRIPT type="text/javascript">
    function SubmitForm() {
        userForm.action.value = "update";
        userForm.submit();
    }
    </SCRIPT>
    <STYLE type="text/css">
    <%@include file="../style.css" %>
    </STYLE>
    </HEAD>
    <!--BODY-->
    <html:form action="/lockedUserAccounts">
    <html:hidden property="userid"/>
    <html:hidden property="method"/>
    <html:hidden property="action"/>
    <BR>
    <TABLE border="0">
    <tr width="100%">
    <td align="center"><h2>Locked User Accounts</h2></td>
    </tr>
    <tr>
    <td>&nbsp;</td>
    </tr>
    </TABLE>
    <TABLE>
    <tr>
    <td>
    <b>Program Area:</b>
    </td>
    <td>  
    <%Iterator grpOptionsIter = null;
    if (groupObjs != null) {
        grpOptionsIter = groupObjs.iterator();
    }
    LabelValueBean valueBean = null;%>
    <select id="programArea" name="programArea" size="1" style="width: 250px">
    <%if(request.getParameter("method") == null || userForm.getMethod() == null){%>
    <option>Please Select Program Area</option>
    <%}
    if (grpOptionsIter != null) {
        boolean selected = false;
        while (grpOptionsIter.hasNext()) {
            valueBean = (LabelValueBean)grpOptionsIter.next();
            if (selectId.equals(valueBean.getValue())) {
                selected = true;
            }
            if(selected) {%>
            <OPTION selected value='<%=valueBean.getValue()%>'><%=valueBean.getLabel()%></OPTION>
            <%} else {%>
            <OPTION value='<%=valueBean.getValue()%>'><%=valueBean.getLabel()%></OPTION>
            <%}
            selected = false;
        }
    }%>
    </select>
    </td>
    </tr> 
    <tr>
    <td><B>Authentication:&nbsp;&nbsp;</B></td>
    <td>
    <select id="authenticationType" name="authenticationType" size="1" style="width: 250px">
    <option value="">Select Authentication Type</option>
    <%
    if (strAuthType != null && strAuthType.trim().length() > 0) {
        boolean selected = false; 
        for (LabelValueBean labelValue:authenticationObjs) {
            if (strAuthType.equals(labelValue.getValue())) {
                selected = true;
            }
            if(selected) {
                //out.print("Selected: "+labelValue.getValue());%>
                <option selected value='<%=labelValue.getValue()%>'><%=labelValue.getLabel()%></option>
                <%} else {%>
                <option value='<%=labelValue.getValue()%>'><%=labelValue.getLabel()%></option>
                <%
                }
            selected = false;
        }
    }  
    %>
    </select>
    </td>
    </tr>
    </select>
    </td>
    </tr>
    <tr>
    <td><b>Group:</b></td>
    <td>
    <select id="groupName" name="groupName" style="width: 250px" onchange="SubmitForm()">
    <option value="">Select Group</option>
    <%
    if (subGroupName != null && subGroupName.trim().length() > 0) {
        boolean selected = false; 
        for (LabelValueBean labelValue:subGroupObjs) {
            if (subGroupName.equals(labelValue.getValue())) {
                selected = true;
            }
            if(selected) {%>
            <option selected value='<%=labelValue.getValue()%>'><%=labelValue.getLabel()%></option>
            <%} else {%>
            <option value='<%=labelValue.getValue()%>'><%=labelValue.getLabel()%></option>
            <%}
            selected = false;
        }
    }  
    %>
    </select>
    </td>
    </tr>
    <tr>
    <td><b>User Name:</b></td>
    <td><html:text property="accountName" size='30' style='width: 247px'/>&nbsp;&nbsp;</td>
    <td><html:submit value="Search User" onclick="searchUser()"/></td>
    </tr>
    </TABLE>
    <BR>
    <!-- CR 26723 The code updated to use display tag and allow user to sort by authentication type - Active Directory, Enterprise or both -->
    <display:table export="true" id="user" name="sessionScope.sessionUsers" class="adminportal" requestURI="" pagesize="<%=intPageSize%>">
    <!-- Using struts multibox code to allow multiple checkboxes to be selected -->
    <display:column title="<a style='color:white;' href='javascript:SubmitForm();'>Unlock</a>" media="html">     <html:multibox 

    property="selectedUserCheckboxes"  value='<%= ((UserDetailVO)user).getUserId() %>' />  </display:column>
    <display:column property="accountName" title="User Account Name" sortable="true" defaultorder="descending"/>
    <display:column property="authenticationType" title="Authentication" sortable="true" defaultorder="descending"/>
    <display:column property="fullName" title="User Full Name" sortable="true"/>
    <display:column property="email" title="Email" sortable="true"/>
    <display:column property="createdDateTime" title="Creation Date" sortable="true"/>
    <display:column property="lastLogonTime" title="Last Login Date" sortable="true"/>
    <display:column property="lockedDate" title="Locked Date" sortable="true"/>
    <display:column property="groups" title="Groups" sortable="true"/>
    </display:table>
    <TABLE border="0">
    <tr width="100%">
    <td align="left"><html:submit value="Submit to Unlock" onclick="SubmitForm()"/></td>
    </tr>
    </TABLE>
    </html:form>

    <ajax:select
    baseUrl="LockedUsrAuthTypeSublist.do"
    source="programArea"
    target="authenticationType"
    parameters="pgmAreaCd={programArea}" />
    <ajax:select
    baseUrl="programAreaSublist.do"
    source="authenticationType"
    target="groupName"
    parameters="pgmAreaCd={programArea},authTypeCd={authenticationType}" />
    <!--/body>
    </html-->
    <%} // is authenticated
else {
    out.println("You do not have access to this page");
}

%>
<jsp:include page="../tiles/footerNew.jsp"/>
