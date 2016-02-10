package gov.fema.adminportal.ldap.app;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.ldap.repository.LdapRepository;

public class LdapApp {

	private static Logger log = Logger.getLogger(LdapApp.class);

	static String username = "tahsan";

	public static void main(String[] args) {
		
//        String confFile = "applicationContext.xml";
//        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(confFile);
//        ldapConfig = (LDAPConfig) context.getBean("ldapConfig");
        
        
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-ldap-example.xml");
		//AbstractApplicationContext context = new ClassPathXmlApplicationContext("spring-ldap-example.xml");
		log.info("Test started at " + new Date(context.getStartupDate()));
		LdapRepository ldapDao = (LdapRepository) context.getBean("ldapReposImpl");
		{
			// Create Group
			Group group = new Group();
			group.setCn("Business_Objects_Disaster_Users_1452da");
			group.setDisasterNumber("1452");
			group.setReportType("da");
			
			// Create User
			User user = new User();
			{
				user.setCn("Arun Bhat");
				user.setSn("Bhat");
				user.setUid("abhat");
				user.setPostalAddress("arun bhat address");
				user.setTelephoneNumber("1111111111");
				user.setUserPassword("abhat");
			}
			
			// Create a Group
			//log.info("\n =>" + ldapDao.createGroup(group));
			// Remove a Group
			//System.out.println(bindDN("Business_Objects_Disaster_Users_1452da"));
			//log.info("\n =>" + ldapDao.remove("cn=Business_Objects_Disaster_Users_1452da,ou=Group,ou=BusinessObjectsDisaster,ou=system"));
			//log.info("\n =>" + ldapDao.removeGroup(group));
			//log.info("\n =>" + ldapDao.createUser(user));

			// Read
//			log.info("\n =>" + ldapDao.getAllUsers());
//			log.info("\n =>" + ldapDao.getAllUserNames());
			//log.info("\n =>" + ldapDao.getUserbyGroup("Wadsworth"));

			context.refresh();

//			log.info("\n =>" + ldapDao.getUserDetails("awadsworth"));
			//log.info("\n =>" + ldapDao.getUserDetail("awadsworth"));
			//log.info("\n =>" + ldapDao.getUserAttributes("Business_Objects_Disaster_Users_1450ha"));
//			List<String> list = ldapDao.getAllPersonGroups("awadsworth");
//			for(String str : list) {
//				System.out.println(str);
//			}
			//ldapDao.searchByFirstName("jsoule");
			List<Group> groups = ldapDao.getGroupList("tahsan");
			
			
			// Update
			//log.info("\n =>" + ldapDao.updateTelePhone("tahsan", "6307282091"));
			//log.info("\n =>" + ldapDao.updateTelePhone("spring_ldap_test", "(630)728-2091"));

			// Delete
			//log.info("\n =>" + ldapDao.remove("spring_ldap_test"));
		}
		context.registerShutdownHook();
		context.close();
	}

}
