package gov.fema.adminportal.ldap.repository;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.LDAPConfig;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.ldap.util.CommonConstants;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;


/**
 * This class implements the @see {@link LdapRepository}.
 */
@Component
public class LdapRepositoryImpl implements LdapRepository {

	private static Logger log = Logger.getLogger(LdapRepositoryImpl.class);

	public LdapRepositoryImpl() {

	}

	@Autowired(required = true)
	@Qualifier(value = "ldapConfig")
	private LDAPConfig ldapConfig;

	@Autowired(required = true)
	@Qualifier(value = "ldapTemplate")
	private LdapTemplate ldapTemplate;

	public static final String BASE_DN = "dc=fema,dc=net";

	protected Name buildDn(User user) {
		return LdapNameBuilder.newInstance(BASE_DN)
				.add("cn", user.getCn())
				.add("distinguishedName", user.getDistinguishedName())
				.build();
	}

	/**
	 * This method will only return 'cn'of the list of users
	 * underlying the base dn
	 * @param baseDn Base DN String value
	 * @return List<String> This returns sum of numA and numB.
	 */
	@Override
	public List<String> getAllUserNames(String baseDn) {
		log.info("executing {getAllUserNames}");
		//LdapQuery query = query().base("ou=users");
		//LdapQuery query = query().base("dc=femadev,dc=net"); // Changed base="" from base="ou=system" in the context file
		LdapQuery query = query().base(baseDn); // Changed base="" from base="ou=system" in the context file 
		List<String> list = ldapTemplate.list(query.base());
		log.info("Users -> " + list);
		//return ldapTemplate.search(query().base("ou=users").where("objectClass").is("person"), new SingleAttributesMapper());
		//		return ldapTemplate.search(query().base("ou=users,ou=system").where("objectClass").is("person"), new SingleAttributesMapper());
//		return ldapTemplate.search(query().base("dc=femadev,dc=net").where("objectClass").is("person"), new SingleAttributesMapper());
		return ldapTemplate.search(query().base("OU=ServiceAccounts,OU=MW,DC=fema,DC=net").where("objectClass").is("person"), new SingleAttributesMapper());
	}

	/*
	 * Say we want to perform a search starting at the base DN "ou=users,ou=system", 
	 * limiting the returned attributes to "cn" and "sn", with the filter (&(objectclass=person)(sn=?)), 
	 * where we want the ? to be replaced with the value of the parameter lastName. This is how we do it 
	 * using the LdapQueryBuilder:
	 * This method will get User detail
	 * @param lastName String value
	 * @return List<String> This returns sum of numA and numB.
	 */
	@Override
	public List<String> getUserbyGroup(String lastName) {
		log.info("User name by group");
		LdapQuery query = query()
				.base("dc=femadev,dc=net")
				.attributes("cn", "sn")
				.where("objectclass").is("person")
				.and("sn").is(lastName);

		return ldapTemplate.search(query, new AttributesMapper<String>() {
			public String mapFromAttributes(Attributes attrs)
					throws NamingException {
				return (String) attrs.get("cn").get();
			}
		});
	}

	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<User> getAllUsers() {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, "(objectclass=person)", controls, new UserAttributesMapper());
		return ldapTemplate.search("", "(objectclass=person)", controls, new UserAttributesMapper());
	}
	

	/**
	 * This method returns the user details
	 * @param userName is a String value
	 * @return User This returns User object.
	 */
	@Override
	public User getUserDetails(String userName) {
		log.info("executing {getUserDetails}");
		//		List<User> list = ldapTemplate.search(query().base("ou=users").where("cn").is(userName), new UserAttributesMapper());
		List<User> list = ldapTemplate.search(query().base("dc=femadev,dc=net").where("cn").is(userName), new UserAttributesMapper());
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	
	@Override
	public String getUserDetail(String userName) {
		log.info("executing {getUserDetails}");
		//List<String> results = ldapTemplate.search(query().base("ou=users").where("uid").is(userName), new MultipleAttributesMapper());
//		List<String> results = ldapTemplate.search(query().base("ou=users,ou=system").where("cn").is(userName), new MultipleAttributesMapper());
		List<String> results = ldapTemplate.search(query().base("dc=fema,dc=net").where("cn").is(userName), new MultipleAttributesMapper());
		if (results != null && !results.isEmpty()) {
			return results.get(0);
		}
		return " userDetails for " + userName + " not found .";
	}

	
	public List<String> searchCnInGroups(String cn) {

		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter("objectclass", "groupOfNames"));searchCnByGroup
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		//filter.and(new EqualsFilter("member","cn=" + cn + "," + ldapConfig.getBoUser()));
		filter.and(new EqualsFilter("member","CN=Shoup\\, Christopher,OU=BRF,OU=Users,OU=VANPSC,DC=fema,DC=net"));
		
		log.info("cn=" + cn + "," + ldapConfig.getBoUser());
		//log.info("CN=Shoup\\, Christopher,OU=BRF,OU=Users,OU=VANPSC,DC=fema,DC=net");
		List<String> searchList = ldapTemplate.search("", filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("cn").get();
			}
		});
		return searchList;
	}
	
	public List<Group> getGroupList(String cn) {
		
		List<Group> groups = new ArrayList<Group>();
		List<String> groupList = searchCnInGroups(cn);
		for(String str : groupList) {			
			Group group = new Group();
			log.info(str);
			String varStr = str.substring(str.lastIndexOf('_') + 1);	
			String reportType =  StringUtils.right(varStr, 2 );
			String disasterNumber = StringUtils.removeEnd(varStr, reportType);
			log.info(reportType);
			log.info(disasterNumber);
			group.setReportType(reportType);
			group.setDisasterNumber(disasterNumber);
			groups.add(group);
		}
		
		return groups;
	}
	
	/**
	 * This method updates an attribute of a group.
	 * @param userName String variable for name attribute
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@Override
	public User updateGroup(String userName, String value, String attributeName) {
		log.info("executing {updateTelePhone}");
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, value));
		ldapTemplate.modifyAttributes("name=" + userName + ",ou=users", new ModificationItem[]{item});
		//ldapTemplate.modifyAttributes("cn=" + userName + "," +  new ModificationItem[]{item});
		return getUserDetails(userName);
	}

	
	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@Override
	public User removeGroupMember(Group group, User user) {
		log.info("executing {removeGroupMember}");
//		log.info("user.getDn() : " + user.getDn() + " \n group.getDn() : "  + group.getDn());
//		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("member", user.getDn()));
//		ldapTemplate.modifyAttributes(group.getDn(), new ModificationItem[]{item});
		
		log.info("group.getDn() : " + group.getDn() + " \n\t\t user.getDn() : " + user.getDistinguishedName());
		ldapTemplate.modifyAttributes(group.getDn(), new ModificationItem[] {
	            new ModificationItem(
	                DirContext.REMOVE_ATTRIBUTE,
	                new BasicAttribute("member", user.getDistinguishedName()))
	        });
//	
		return user;
	}

	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllPersonGroups(String cn) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "group"));
		//		filter.and(new EqualsFilter("member", "CN=userAlias,OU=Accounts,OU=XXX,OU=YYYUsers,OU=ZZZ,DC=ttt,DC=company,DC=com"));
		log.info("member" + "," + "cn=" + cn +",ou=User,ou=BusinessObjectsDisaster,ou=system");
		filter.and(new EqualsFilter("member", "cn="+cn+",ou=User,ou=BusinessObjectsDisaster,ou=system"));
		return ldapTemplate.search("",
				filter.encode(),
				new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs)
					throws NamingException {
				return attrs.get("cn").get();
			}
		});
	}

	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@Override
	public List<String> getUserAttributes(String userName) {
		ldapTemplate.search(
				LdapQueryBuilder.query().where("cn").is(userName),
				new AttributesMapper<Void>() {

					public Void mapFromAttributes(Attributes attrs) throws NamingException {

						NamingEnumeration<String> attrIdEnum = attrs.getIDs();
						while (attrIdEnum.hasMoreElements()) {
							String attrId = attrIdEnum.next();
							Attribute attr = attrs.get(attrId);

							log.info(attrId + ":");
							NamingEnumeration<?> nameEnum = attr.getAll();
							while (nameEnum.hasMore()) {
								Object valueObj = nameEnum.next();
								System.out.printf("        %s%n",  valueObj);
							}
						}
						return null;
					}
				});
		return null;
	}

	/**
	 * This method is used to add two integers. This is
	 * a the simplest form of a class method, just to
	 * show the usage of various javadoc Tags.
	 * @param numA This is the first paramter to addNum method
	 * @param numB  This is the second parameter to addNum method
	 * @return int This returns sum of numA and numB.
	 */
	@Override
	public boolean authenticate(String base, String userName, String password) {
		log.info("executing {authenticate}");
		return ldapTemplate.authenticate(base, "(cn=" + userName + ")", password);
	}

	@Override
	public boolean createGroup(Group group) {
		
        // Construct the group name
        String groupName = ldapConfig.getBoGroupPrefix()+ group.getDisasterNumber() + group.getReportType();
        log.info("groupName : " + groupName);
		
		try {
			log.info("executing {createGroup}");
			Attribute objectClass = new BasicAttribute("objectClass");
		{
			objectClass.add("top");
			objectClass.add("group");
			//objectClass.add("organizationalUnit");
		}
		Attributes groupAttributes = new BasicAttributes();
		groupAttributes.put(objectClass);
		log.info("group.getCn() : " + group.getCn());
		
		//groupAttributes.put("distinguishedName", group.getCn()); // 
		groupAttributes.put("name", group.getCn());
		groupAttributes.put("sAMAccountName", group.getCn());
		log.info("groupType : " + CommonConstants.groupType);
		groupAttributes.put("groupType", CommonConstants.groupType);
		//groupAttributes.put("groupType", new String("4"));
		//groupAttributes.put("member", "CN=Tom Wall,OU=Users,OU=CH,DC=femadev,DC=net"); //;CN=Gary Baldanza,OU=Users,OU=CH,DC=femadev,DC=net
		//groupAttributes.put("member", "CN=Gary Baldanza,OU=Users,OU=CH,DC=femadev,DC=net");
		//ldapTemplate.bind(bindDN(user.getUid()), null, groupAttributes);
//		log.info("cn=" + group.getCn() + "," + ldapConfig.getBoGroup());
		log.info(groupName + "," + ldapConfig.getBoGroup());
		log.info("-------------------------------------------------------------");
		log.info("distinguishedName : " + groupName + "," + ldapConfig.getBoGroup());
		groupAttributes.put("distinguishedName", groupName + "," + ldapConfig.getBoGroup());
		//ldapTemplate.bind("cn=" + group.getCn() + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		ldapTemplate.bind(groupName + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		
		return true;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error("Group already exists!");
			return false;
        } catch (Exception e) {
            log.error(e, e);
            return false;
        } 
	}

	/**
	 * This method creates a User
	 * @param User passed as parameter a User bean object 
	 * @return boolean returns a boolean value.
	 */
	@Override
	public boolean createUser(User user) {

		try {
			log.info("executing {createUser}");
			Attribute objectClass = new BasicAttribute("objectClass");
			{
				objectClass.add("top");
				objectClass.add("user");
				objectClass.add("person");
				objectClass.add("organizationalPerson");
			}
			Attributes userAttributes = new BasicAttributes();
			userAttributes.put(objectClass);
			userAttributes.put("cn", user.getCn());
			userAttributes.put("sAMAccountName", user.getsAMAccountName());
			userAttributes.put("name", user.getName());
			userAttributes.put("givenName", user.getGivenName());
			userAttributes.put("principalName", user.getUserPrincipalName());
			//userAttributes.put("userPassword", user.getUserPassword().getBytes());
			log.info("user.getCn() :" + user.getCn());

			// We'll not use bindDN
			//		log.info(bindDN(user.getUid()));
			//		log.info("-----------------------------------------------");
			//		ldapTemplate.bind(bindDN(user.getUid()), null, userAttributes);
			//ldapTemplate.bind(ldapConfig.getBoUser())

			return true;
			//		} catch (NameAlreadyBoundException nabex) {
			//        	
			//			log.error("Group already exists!");
			//			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}

	/**
	 * This method removes a Group
	 * @param group parameter is a Group bean object 
	 * @return boolean value.
	 */
	@Override
	public boolean removeGroup(Group group) {

		// Construct the group name
		//CN=BusinessObjectsDisasterUsers1452da,OU=Groups,OU=CH,DC=femadev,DC=net
		String groupDN = ldapConfig.getBoGroupPrefix() + group.getDisasterNumber() + group.getReportType() + "," + ldapConfig.getBoGroup();
		log.info("groupDN : " + groupDN);
		ldapTemplate.unbind(groupDN);
		return true;       

	}

	/**
	 * This method removes a dn
	 * @param dn String value of a DN
	 * @return boolean return value.
	 */
	@Override
	public boolean remove(String dn) {
		log.info(dn);
		ldapTemplate.unbind(dn);
		return true;
	}


	/**
	 * This class prepares User object after ldap search.
	 */
	private class UserAttributesMapper implements AttributesMapper<User> {

		//@Override
		public User mapFromAttributes(Attributes attributes) throws NamingException {
			User user;
			if (attributes == null) {
				return null;
			}
			user = new User();
			user.setCn(attributes.get("cn").get().toString());

			if (attributes.get("userPassword") != null) {
				String userPassword = null;
				try {
					userPassword = new String((byte[]) attributes.get("userPassword").get(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					log.error("unable to process", e);
				}
				user.setUserPassword(userPassword);
			}
			if (attributes.get("cn") != null) {
				user.setCn(attributes.get("cn").get().toString());
			}
			if (attributes.get("name") != null) {
				user.setName(attributes.get("name").get().toString());
			}
			if (attributes.get("sAMAccountName") != null) {
				user.setsAMAccountName(attributes.get("sAMAccountName").get().toString());
			}
			if (attributes.get("distinguishedName") != null) {
				user.setDistinguishedName(attributes.get("distinguishedName").get().toString());
			}
			
			return user;
		}
	}

	/**
	 * This class prints only cn .
	 */
	private class SingleAttributesMapper implements AttributesMapper<String> {

		//@Override
		public String mapFromAttributes(Attributes attrs) throws NamingException {
			Attribute cn = attrs.get("cn");
			return cn.toString();
		}
	}

	/**
	 * This class prints all the content in string format.
	 */
	private class MultipleAttributesMapper implements AttributesMapper<String> {

		//@Override
		public String mapFromAttributes(Attributes attrs) throws NamingException {
			NamingEnumeration<? extends Attribute> all = attrs.getAll();
			StringBuffer result = new StringBuffer();
			result.append("\n Result { \n");
			while (all.hasMore()) {
				Attribute id = all.next();
				result.append(" \t |_  #" + id.getID() + "= [ " + id.get() + " ]  \n");
				log.info(id.getID() + "\t | " + id.get());
			}
			result.append("\n } ");
			return result.toString();
		}
	}

}
