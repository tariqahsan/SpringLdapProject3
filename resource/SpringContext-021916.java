/**
 * This utility class bootstraps the Admin Portal application with the Spring database
 * framework specific configurable parameters.
 */
package gov.fema.adminportal.util;

import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.fema.adminportal.jdbc.dao.*;
import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.ldap.repository.LdapRepository;

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
        
/*        HCLookupDao ds = (HCLookupDao) appContext.getBean("hcLookupDao");
        System.out.println("HCLookupDao: " + ds);
        DisasterSecurityDao ds1 = (DisasterSecurityDao) appContext.getBean("disasterSecurityDao");
        System.out.println("DisasterSecurityDao: " + ds1);*/
        
        // LDAP related operations        
    	logger.info("Active Directory related LDAP test started at " + new Date(appContext.getStartupDate()));
    	LdapRepository ldapDao = (LdapRepository) appContext.getBean("ldapReposImpl");
    	{

			// Create Group
			Group group = new Group();
			group.setCn("Business_Objects_Disaster_Users_1455ha");
			group.setDisasterNumber("1455");
			group.setReportType("ha");
			//group.setDn("CN=Business_Objects_Disaster_Users_1454da,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net");
			group.setDistinguishedName("CN=Business_Objects_Disaster_Users_1455ha,OU=Groups,OU=CH,DC=femadev,DC=net");
			
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
			//logger.info("\n =>" + ldapDao.createGroup(group));
			// Remove a Group
			//logger.info("\n =>" + ldapDao.removeGroup(group));
			//logger.info("\n =>" + ldapDao.createUser(user));
			
			// Remove a member from a Group
			//logger.info("\n =>" + ldapDao.removeGroupMember(group, user));

			// Read
			//logger.info("\n =>" + ldapDao.getAllUsers());
			//logger.info("\n =>" + ldapDao.getAllUserNames());
			//logger.info("\n =>" + ldapDao.getUserbyGroup("Wall"));
			//logger.info("\n =>" + ldapDao.getUserDetail("test0001"));
			//logger.info("\n =>" + ldapDao.getUserDetail("DBA"));
			//logger.info("\n =>" + ldapDao.getUserDetails("test0001"));
			//logger.info("\n =>" + ldapDao.searchCnInGroups("Mark Somma"));
			//logger.info("\n =>" + ldapDao.getGroupList("Mark Somma"));
			//logger.info("\n =>" + ldapDao.getUserbyGroup("grodgers"));
			logger.info("\n => " + ldapDao.getDistinguishedName("grodgers"));
			//List<User> users = ldapDao.getMemberOfListFromUser("grodgers");
			/*for(User adUser : users) {
				
				logger.info(adUser.getDistinguishedName());
				for (String memberOf : adUser.getMemberOf()) {
					logger.info(memberOf);
				}
			}*/
			
			//logger.info("\n =>" + ldapDao.getMemberOfListFromUser());
			appContext.refresh();

		
    	}
    	
		//Register a shutdown hook with the JVM runtime, closing this context on JVM shutdown unless it has already been closed at that time.
    	appContext.registerShutdownHook();
    	appContext.close();
    	
    	
    }
}
