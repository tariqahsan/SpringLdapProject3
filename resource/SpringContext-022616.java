/**
 * This utility class bootstraps the Admin Portal application with the Spring database
 * framework specific configurable parameters.
 */
package gov.fema.adminportal.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.ldap.repository.LdapRepository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SpringContext {
	
    private static Logger logger = Logger.getLogger(SpringContext.class.getName());
    private static ClassPathXmlApplicationContext appContext = null;
    
    static {
        loadSpringContext();
    }

    protected static void loadSpringContext() {
        // InputStream stream = null;
        try {
            logger.debug("Loading Application context");
            // stream =
            // Thread.currentThread().getContextClassLoader().getResourceAsStream("spring-config.xml");
            appContext = new ClassPathXmlApplicationContext("spring-config.xml");
            // Resource resource = new InputStreamResource(stream);
            // GenericXmlApplicationContext appContext = new
            // GenericXmlApplicationContext();
            // appContext.load(resource);
            logger.debug("Loaded Application context");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error loading spring config file please check classpath");
        }
    }

    public static ClassPathXmlApplicationContext getContext() {
    	
        if (appContext == null) {
            // logger.debug("appContext was null, so populating it");
            loadSpringContext();
        } else {
            logger.debug("App context is not null");
        }
        return appContext;
    }

    public static void main(String[] args) {
        // try fetching the ClassPath context
    	logger.info("Running the main method ...");
        appContext = getContext();
        
        // LDAP related operations        
    	logger.info("Active Directory related LDAP test started at " + new Date(appContext.getStartupDate()));
    	LdapRepository ldapDao = (LdapRepository) appContext.getBean("ldapReposImpl");
    	{

			// Create Group
//			Group group = new Group();
//			group.setCn("Business_Objects_Disaster_Users_1455ha");
//			group.setDisasterNumber("1455");
//			group.setReportType("ha");
//			//group.setDn("CN=Business_Objects_Disaster_Users_1454da,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net");
//			group.setDistinguishedName("CN=Business_Objects_Disaster_Users_1455ha,OU=Groups,OU=CH,DC=femadev,DC=net");
			
			// Create a Organizational Unit
			Group group = new Group();
			//group.setCn("Business_Objects_Disaster_Users_1454da");
			group.setDisasterNumber("1454");
			group.setReportType("da");
			//group.setDistinguishedName("OU=1450,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net");
			group.setOu("1454");
		
			
			// Create User
			User user = new User();
			{
				user.setCn("Tom Wall");
				user.setsAMAccountName("twall");
				user.setDisplayName("Tom Wall");
				user.setGivenName("Tom Wall");
				user.setName("Tom Wall");
				user.setUserPrincipalName("twall@fema.net");
				user.setUserPassword("twall");
				user.setDistinguishedName("CN=Tom Wall,OU=Users,OU=CH,DC=femadev,DC=net");
				//user.setDn("CN=Tom Wall,OU=Users,OU=CH");
			}
			
			// Create a Group
//			logger.info("\n =>" + ldapDao.createOrganizationalUnit(group));
//			//logger.info("\n =>" + ldapDao.createAdminOrganizationalUnit(group));
//			logger.info("\n =>" + ldapDao.createGroup(group));
//			logger.info("\n =>" + ldapDao.createAdminGroup(group, "DISASTER_NUMBER_ONLY"));
//			logger.info("\n =>" + ldapDao.createAdminGroup(group, "BOTH"));
//			
			//logger.info("\n =>" + ldapDao.updateGroup(null, "", attributeName);
			
			// Find OrganizationalUnit
			//logger.info("\n =>" + ldapDao.findOrganizationalUnit("1454"));
			
			// Remove a Group
//			logger.info("\n =>" + ldapDao.removeGroup(group));
//			logger.info("\n =>" + ldapDao.removeAdminGroup(group, "DISASTER_NUMBER_ONLY"));
//			logger.info("\n =>" + ldapDao.removeAdminGroup(group, "BOTH"));
//			logger.info("\n =>" + ldapDao.removeAdminOrganizationalUnit(group));
			//logger.info("\n =>" + ldapDao.findOuChildren("1454"));
			
			
			//logger.info("\n =>" + ldapDao.createUser(user));
			
			// Remove a member from a Group
			//logger.info("\n =>" + ldapDao.removeGroupMember(group, user));
			//logger.info("\n =>" + ldapDao.removeGroupMember("awadswo1", "Business_Objects_Disaster_Users_1454ha"));

			// Read
			//logger.info("\n =>" + ldapDao.getAllUsers());
//			List<User> users = ldapDao.getAllUsers();
//			for (User adUser : users) {
//				
//				logger.info(adUser.getsAMAccountName());
//				
//			}
			//logger.info("\n =>" + ldapDao.getAllUserNames());
			//logger.info("\n =>" + ldapDao.getUserbyGroup("Wall"));
			//logger.info("\n =>" + ldapDao.getUserDetail("test0001"));
			//logger.info("\n =>" + ldapDao.getUserDetail("awadswo1"));
			//logger.info("\n =>" + ldapDao.getUserDetails("awadswo1"));
//			User adUser = ldapDao.getUserDetails("awadswo1");
//			logger.info(adUser.getCn());
//			logger.info(adUser.getMail());
			
			//logger.info("\n =>" + ldapDao.searchCnInGroups("Mark Somma"));
//			logger.info("\n =>" + ldapDao.getGroupList("msomma"));
			//logger.info("\n =>" + ldapDao.getGroupList("awadswo1"));
			//logger.info("\n =>" + ldapDao.getUserbyGroup("grodgers"));
			//logger.info("\n => " + ldapDao.getDistinguishedName("grodgers"));
			//logger.info("\n => " + ldapDao.searchMemberInGroup("grodgers"));
			//logger.info("\n => " + ldapDao.getNamefromGroupDn("CN=Business_Objects_Disaster_Users_1450ha,OU=1450,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net"));
			
			List<User> users = ldapDao.getMemberOfListFromUser("awadswo1");
			for(User adUser : users) {
				
				logger.info(adUser.getDistinguishedName());
				for (String memberOf : adUser.getMemberOf()) {
					logger.info(memberOf);
				}
				for (String disasterNumber : adUser.getDisasterNumbers()) {
					logger.info(disasterNumber);
				}
				for (String reportType : adUser.getReportTypes()) {
					logger.info(reportType);
				}
				
				for(String disasterNumberReportType : adUser.getDisasterNumberReportTypes()) {
					logger.info(disasterNumberReportType);
				}
			}
			/*List<Group> groups = ldapDao.getMemberList();
			List<User> adUsers = new ArrayList<User>();
			for (Group adGroup : groups) {
				logger.info("-------------------------------------------------------------------------------------------------------------------------------");
				logger.info("sAMAccountName : " + adGroup.getsAMAccountName());
				logger.info("DN : " + adGroup.getDistinguishedName());
				for (String member : adGroup.getMembers()) {
					//logger.info("Member : " + member);
					User adUser = ldapDao.getUserDetailsFromDn(member);
					//logger.info("User sAMAccountName :" + adUser.getsAMAccountName());
					//logger.info("User Email :" + adUser.getMail());
					String varStr = adGroup.getsAMAccountName().substring(adGroup.getsAMAccountName().lastIndexOf('_') + 1);	
					String reportType =  StringUtils.right(varStr, 2 );
					String disasterNumber = StringUtils.removeEnd(varStr, reportType);
					adUser.setDisasterNumber(disasterNumber);
					adUser.setReportType(reportType);
					//logger.info(adUser.getDisasterNumber());
					//logger.info(adUser.getReportType());
					adUsers.add(adUser);
				}
			}*/
			
//			List<User> adUsers = ldapDao.getAllUserDetails();
//			for(User user1 : adUsers) {
//				
//				logger.info("=====================================================================");
//				logger.info(user1.getsAMAccountName());
//				logger.info(user1.getMail());
//				logger.info(user1.getDisasterNumber());
//				logger.info(user1.getReportType());
//				logger.info(user1.getDisasterNumberReportType());
//			}
		
			
//			List<String> users = ldapDao.getAllMembers();
//			for (String adUser : users) {
//				logger.info(adUser);
//			}
			
			//logger.info("\n =>" + ldapDao.getMemberOfListFromUser());
			appContext.refresh();

		
    	}
    	
		//Register a shutdown hook with the JVM runtime, closing this context on JVM shutdown unless it has already been closed at that time.
    	appContext.registerShutdownHook();
    	appContext.close();
    	
    	
    }
}
