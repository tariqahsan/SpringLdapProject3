package gov.fema.adminportal.ldap.repository;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.util.Config;
import gov.fema.adminportal.util.ParameterConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.springframework.ldap.NameNotFoundException;
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
	@Qualifier(value = "ldapTemplate")
	private LdapTemplate ldapTemplate;

	public static final String BASE_DN = Config.getProperty(ParameterConstants.BASE_DN);
	public static final String BO_DN = Config.getProperty(ParameterConstants.BO_DN);

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
	public List<String> getAllUserNames(String baseDn) {
		log.info("executing {getAllUserNames}");
		LdapQuery query = query().base(baseDn); 
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
	/*@Override
	public List<String> getUserbyGroup(String lastName) {
		log.info("User name by group");
		LdapQuery query = query()
				.base(BASE_DN)
				.attributes("cn", "sn")
				.where("objectclass").is("person")
				.and("sn").is(lastName);

		return ldapTemplate.search(query, new AttributesMapper<String>() {
			public String mapFromAttributes(Attributes attrs)
					throws NamingException {
				return (String) attrs.get("cn").get();
			}
		});
	}*/
	
	public List<String> getUserbyGroup(String sAMAccountName) {
		log.info("\n =>" + sAMAccountName);
		LdapQuery query = query()
				.base(BASE_DN)
				.attributes("sAMAccountName", sAMAccountName)
				.where("objectclass").is("top")
				.and("objectclass").is("person");

		return ldapTemplate.search(query, new AttributesMapper<String>() {
			public String mapFromAttributes(Attributes attrs)
					throws NamingException {
				System.out.println((String) attrs.get("cn").get());
				return (String) attrs.get("cn").get();
				/*System.out.println((String) attrs.get("distinguishedName").get());
				return (String) attrs.get("distinguishedName").get();*/
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
	public User getUserDetails(String userName) {
		log.info("executing {getUserDetails}");
		//		List<User> list = ldapTemplate.search(query().base("ou=users").where("cn").is(userName), new UserAttributesMapper());
		//		List<User> list = ldapTemplate.search(query().base(BASE_DN).where("cn").is(userName), new UserAttributesMapper());
		List<User> list = ldapTemplate.search(query().base(BASE_DN).where("sAMAccountName").is(userName), new UserAttributesMapper());
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * This method is for getting user details
	 * @param userName String value passed
	 * @return String returns user details in string text format.
	 */

	public String getUserDetail(String userName) {
		log.info("executing {getUserDetails}");
		//List<String> results = ldapTemplate.search(query().base("ou=users").where("uid").is(userName), new MultipleAttributesMapper());
		//		List<String> results = ldapTemplate.search(query().base("ou=users,ou=system").where("cn").is(userName), new MultipleAttributesMapper());
		//List<String> results = ldapTemplate.search(query().base(BASE_DN).where("cn").is(userName), new MultipleAttributesMapper());
		List<String> results = ldapTemplate.search(query().base(BASE_DN).where("sAMAccountName").is(userName), new MultipleAttributesMapper());
		if (results != null && !results.isEmpty()) {
			return results.get(0);
		}
		return " userDetails for " + userName + " not found .";
	}

	/**
	 * This method is to get the DN value of a sAMAccountName
	 * @param sAMAccountName String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String getDistinguishedName(String sAMAccountName) {
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new EqualsFilter("sAMAccountName", sAMAccountName));
		List<String> searchList = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("distinguishedName").get();
			}
		});
		log.info("searchList.get(0) : " + searchList.get(0));
		return searchList.get(0);
	}
	
	/**
	 * This method is to get the DN value of a sAMAccountName
	 * @param sAMAccountName String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String findOuChildren(String ouName) {
		log.info("ouName : " + ouName);
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
		filter.and(new EqualsFilter("name", ouName));
		List<String> searchList = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("distinguishedName").get();
			}
		});
		log.info("searchList.size()" + searchList.size());
		log.info("searchList.get(0) : " + searchList.get(0));
		return searchList.get(0);
	}
	
	/**
	 * This method is to get the DN value of a sAMAccountName
	 * @param sAMAccountName String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String getNamefromGroupDn(String distinguishedName) {
		try {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		filter.and(new EqualsFilter("distinguishedName", distinguishedName));
		
		List<String> searchList = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("name").get();
			}
		});
		log.info("searchList.get(0) : " + searchList.get(0));
		return searchList.get(0);
		} catch (IndexOutOfBoundsException ex1) {
			log.error("Name of the group is not found");
			return null;
		} catch (Exception ex2) {
			log.error(ex2, ex2);
			return null;
		}
		
	}
	
	/**
	 * This method is to get the DN value of a name
	 * @param name String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String findOrganizationalUnit(String name) {
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
		filter.and(new EqualsFilter("name", name));
		
		List<String> searchList = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("distinguishedName").get();
			}
		});
		log.info("searchList.get(0) : " + searchList.get(0));
		return searchList.get(0);
	}

	/**
	 * This method is to get the DN value of a sAMAccountName
	 * and traverses the AD tree from the passed BASE DN root
	 * searching the user DN in all group objects.
	 * @param sAMAccountName String value passed
	 * @return String returns list of Group CN values in string 
	 * text format.
	 */
	public List<String> searchCnInGroups(String sAMAccountName) {
		log.info("Starting searchInGroups for sAMAccountName -> " + sAMAccountName);
		
		// Get the distinguishedName from it's sAMAccountName
		String distinguishedName = getDistinguishedName(sAMAccountName);
		log.info("distinguishedName : " + distinguishedName);
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		filter.and(new EqualsFilter("member", distinguishedName));
		List<String> searchList = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				log.info("group -> " + attrs.get("cn").get(0));
				return attrs.get("cn").get();
			}
		});
		return searchList;
	}
	
	/**
	 * This method uses the passed sAMAccountName to
	 * the method searchCnInGroups(String sAMAccountName)
	 * and gets the list of Group objects it belongs to.
	 * Basically the 'member' attribute of these listed
	 * groups contains the DN of the sAMAccountName.	 * 
	 * @param sAMAccountName String value passed
	 * @return String returns list of Group object.
	 */
	public List<Group> getGroupList(String sAMAccountName) {

		List<Group> groups = new ArrayList<Group>();
		List<String> groupList = searchCnInGroups(sAMAccountName);
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
	
	public List<Group> searchMemberInGroup(String sAMAccountName) {
		
		// Get the CN value of the user that should be part of the 'member' attribute of the group
		
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		//filter.and(new EqualsFilter("objectCategory", "CN=Group,CN=Schema,CN=Configuration,DC=femadev,DC=net"));
		//filter.and(new EqualsFilter("distinguishedName", "CN=Business_Objects_Disaster_Users_1454da,OU=Groups,OU=CH,DC=femadev,DC=net"));
		//		filter.and(new EqualsFilter("member","cn=" + cn + "," + Config.getProperty(ParameterConstants.BO_USER)));
		filter.and(new EqualsFilter("member", sAMAccountName));
		List<Group> groups = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper<Group>() { 
				
				public Group mapFromAttributes(Attributes attributes) throws NamingException { 
					
					LinkedList<String> members = new LinkedList<String>();
					
					String distinguishedName = attributes.get("distinguishedName") != null ? (String) attributes.get("distinguishedName").get() : ""; 
					String cn = attributes.get("cn") != null ? (String) attributes.get("cn").get() : ""; 
					String displayName = attributes.get("displayName") != null ? (String) attributes.get("displayName").get() : "";
					String sAMAccountName = attributes.get("sAMAccountName") != null ? (String) attributes.get("sAMAccountName").get() : "";

					// member attribute may contain multiple values
					Attribute memberVal = attributes.get("member"); 
					if (memberVal != null) { 
						NamingEnumeration<?> member = memberVal.getAll(); 
						while (member.hasMore()) { 
							String group = (String) member.next();
							log.info("member : " + group);
							members.add(group);
						} 
					} 

					return new Group(distinguishedName, null, null, cn, members, sAMAccountName, null, null); 
				} 
			}); 

			return groups; 		
	}
	
	public List<User> getMemberOfListFromUser(String sAMAccountName) {
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new EqualsFilter("sAMAccountName", sAMAccountName));

		List<User> users = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper<User>() {
			 
			public User mapFromAttributes(Attributes attributes) throws NamingException { 
				
				LinkedList<String> memberOfs = new LinkedList<String>();
				LinkedList<String> disasterNumbers = new LinkedList<String>();
				LinkedList<String> reportTypes = new LinkedList<String>();
				
				String distinguishedName = attributes.get("distinguishedName") != null ? (String) attributes.get("distinguishedName").get() : ""; 
				String cn = attributes.get("cn") != null ? (String) attributes.get("cn").get() : ""; 
				String displayName = attributes.get("displayName") != null ? (String) attributes.get("displayName").get() : "";
				String sAMAccountName = attributes.get("sAMAccountName") != null ? (String) attributes.get("sAMAccountName").get() : "";

				// memberOf attribute may contain multiple values
				Attribute memberOfVal = attributes.get("memberOf"); 
				if (memberOfVal != null) { 
					NamingEnumeration<?> memberOf = memberOfVal.getAll(); 
					while (memberOf.hasMore()) { 
						String groupDn = (String) memberOf.next();
						// Only store the groups that has prefix of CN=Business_Objects_Disaster_Users_
						if (groupDn.startsWith(Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX))) {
							log.info("memberOf : " + groupDn);
							String groupName = getNamefromGroupDn(groupDn);
							log.info(groupName);
							
							// Now parse the group name to extract the disasterNumber(the content after the last underscore)
							log.info(groupName);
							String varStr = groupName.substring(groupName.lastIndexOf('_') + 1);	
							String reportType =  StringUtils.right(varStr, 2 );
							String disasterNumber = StringUtils.removeEnd(varStr, reportType);
							log.info(reportType);
							log.info(disasterNumber);
							memberOfs.add(groupDn);
							disasterNumbers.add(disasterNumber);
							reportTypes.add(reportType);
						}
					} 
				} 

				return new User(distinguishedName, null, displayName, cn, memberOfs, sAMAccountName, null, null, null, disasterNumbers, reportTypes); 
			} 
		}); 

		return users; 
	}

	/**
	 * This method modifies value of an attribute of a group.
	 * @param userName String variable for name attribute
	 * @param value String of the new attribute value
	 * @param attributeName String value of the attribute name
	 * @return User an User class object is returned.
	 */
	/*@Override
	public User updateGroup(String userName, String value, String attributeName) {
		log.info("executing {updateGroup}");
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, value));
//		ldapTemplate.modifyAttributes("name=" + userName + ",ou=users", new ModificationItem[]{item});
		ldapTemplate.modifyAttributes("OU=1454,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net", new ModificationItem[]{item});
		
		//ldapTemplate.modifyAttributes("cn=" + userName + "," +  new ModificationItem[]{item});
		return getUserDetails(userName);
	}*/

	public void updateGroup(String userName, String value, String attributeName) {
		log.info("executing {updateGroup}");
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, value));
//		ldapTemplate.modifyAttributes("name=" + userName + ",ou=users", new ModificationItem[]{item});
		ldapTemplate.modifyAttributes("OU=1454,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net", new ModificationItem[]{item});
		
	}

	/**
	 * This method is used to remove a specified value in the 'member'
	 * attribute.
	 * @param User a User object is the first parameter
	 * @param Group a Group object is the second parameter
	 * @return User returns a User object.
	 */	
	public User removeGroupMember(Group group, User user) {
		log.info("executing {removeGroupMember}");
		//		log.info("user.getDn() : " + user.getDn() + " \n group.getDn() : "  + group.getDn());
		//		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("member", user.getDn()));
		//		ldapTemplate.modifyAttributes(group.getDn(), new ModificationItem[]{item});
		//CN=Tariq Ahsan,OU=Users,OU=CH,DC=femadev,DC=net
		log.info("group.getDn() : " + group.getDistinguishedName() + " \n\t\t user.getDn() : " + user.getDistinguishedName());
		ldapTemplate.modifyAttributes(group.getDistinguishedName(), new ModificationItem[] {
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
		filter.and(new EqualsFilter("member", "cn=" + cn + ",ou=User,ou=BusinessObjectsDisaster,ou=system"));
		return ldapTemplate.search("", filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("cn").get();
			}
		});
	}

	/**
	 * This method is used get all attribute values of an user.
	 * @param userName the user CN attribute value
	 * @return List<String> List of attribute values are returned.
	 */
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
	 * This method is used to authenticate a user against Active Directory.
	 * @param base the base DN
	 * @param userName CN attribute value of the user
	 * @param password password attribute value
	 * @return boolean boolean result is returned
	 */
	
	public boolean authenticate(String base, String userName, String password) {
		log.info("executing {authenticate}");
		return ldapTemplate.authenticate(base, "(cn=" + userName + ")", password);
	}
	
	/**
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return boolean returns a boolean result.
	 *//*
	
	public boolean createOrganizationalUnit(Group group) {

		// Construct the group name

		String groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_PREFIX) + group.getDisasterNumber() + group.getReportType();
		log.info("groupName : " + groupName);

		try {
			log.info("executing {createOrganizationalUnit}");
			
			// Construct the distinguishedName
			Attributes groupAttributes = new BasicAttributes();
			// Determine if it is an OrganizationalUnit or a Group to be created
			// if the ou variable is not null then an OU needs to be created
			if(!StringUtils.isBlank(group.getOu())) {
				Attribute objectClass = new BasicAttribute("objectClass");
				{
					objectClass.add("top");
					objectClass.add("organizationalUnit");
				}
//				Attributes groupAttributes = new BasicAttributes();
				groupAttributes.put(objectClass);
				groupAttributes.put("distinguishedName", group.getDistinguishedName()); // 
				groupAttributes.put("name", group.getOu());
				groupAttributes.put("ou", group.getOu());
				groupAttributes.put("instanceType", new String("4"));
			}
			//Attribute objectClass = new BasicAttribute("objectClass");
//			objectClass.add("top");
//			objectClass.add("organizationalUnit");
//			Attributes groupAttributes = new BasicAttributes();
//			groupAttributes.put(objectClass);

			
//			groupAttributes.put("distinguishedName", group.getDistinguishedName()); // 
//			groupAttributes.put("name", group.getOu());
//			groupAttributes.put("ou", group.getOu());
//			log.info("boGroup : " + Config.getProperty(ParameterConstants.BO_GROUP));
//			log.info("boGroupPrefix : " + Config.getProperty(ParameterConstants.BO_CN_GROUP_PREFIX));
//			log.info("boUser : " + Config.getProperty(ParameterConstants.BO_USER));
//			log.info("groupType : " + Config.getProperty(ParameterConstants.GROUP_TYPE));
//			groupAttributes.put("instanceType", Config.getProperty(ParameterConstants.GROUP_TYPE));
			//groupAttributes.put("instanceType", new String("4"));
			//groupAttributes.put("groupType", new String("4"));

			// NOTE : FEMA TDL AD do not accept 'member' attribute to be added during GROUP creation
			// FEMA EADIS DEV AD does accept 'member'. But does not allow a member attribute to be replaced.
			//groupAttributes.put("managedBy", "CN=Business_Objects_Disaster_Users_1454da,OU=1454,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net");
			//groupAttributes.put("member", "CN=Wadsworth\\, Anthony,OU=BRF,OU=Users,OU=VANPSC,DC=fema,DC=net"); //;CN=Gary Baldanza,OU=Users,OU=CH,DC=femadev,DC=net
			//groupAttributes.put("member", "CN=Tom Wall,OU=Users,OU=CH,DC=femadev,DC=net");
			//ldapTemplate.bind(bindDN(user.getUid()), null, groupAttributes);
			//		log.info("cn=" + group.getCn() + "," + ldapConfig.getBoGroup());

			log.info(groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("-------------------------------------------------------------");
			//log.info("distinguishedName : " + groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			//groupAttributes.put("distinguishedName", groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			//ldapTemplate.bind("cn=" + group.getCn() + "," + ldapConfig.getBoGroup(), null, groupAttributes);
			//ldapTemplate.bind(groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP), null, groupAttributes);
			ldapTemplate.bind(group.getDistinguishedName(), null, groupAttributes);

			return true;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error("Group already exists!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}
*/
	
	/**
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return boolean returns a boolean result.
	 */
	
	public boolean createOrganizationalUnit(Group group) {

		// Construct the ou name
		String ouName = "OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
		log.info("ouName : " + ouName);

		try {
			log.info("executing {createOrganizationalUnit}");
			
			// Construct the distinguishedName
			Attributes groupAttributes = new BasicAttributes();
			// Determine if it is an OrganizationalUnit or a Group to be created
			// if the ou variable is not null then an OU needs to be created
			if(!StringUtils.isBlank(group.getOu())) {
				Attribute objectClass = new BasicAttribute("objectClass");
				{
					objectClass.add("top");
					objectClass.add("organizationalUnit");
				}
//				Attributes groupAttributes = new BasicAttributes();
				groupAttributes.put(objectClass);
				groupAttributes.put("distinguishedName", ouName); // 
				groupAttributes.put("name", group.getOu());
				groupAttributes.put("ou", group.getOu());
				groupAttributes.put("instanceType", new String("4"));
			}

			log.info(ouName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("-------------------------------------------------------------");
			ldapTemplate.bind(ouName, null, groupAttributes);
			
			// Now create the corresponding admin ou based on the report type
			// Construct the group name
			String ouAdminName = "OU=" + group.getReportType() + "_Admins" + "," + ouName;
			log.info("-------------------------------------------------------------");
			log.info(ouAdminName);
			groupAttributes.put("distinguishedName", ouAdminName); // 
			groupAttributes.put("name", group.getReportType() + "_Admins");
			groupAttributes.put("ou", group.getReportType() + "_Admins");
			groupAttributes.put("instanceType", new String("4"));
			ldapTemplate.bind(ouAdminName, null, groupAttributes);

			return true;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error("OrganizationalUnit already exists!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}
	
	public boolean createAdminOrganizationalUnit(Group group) {

		// Now create the corresponding admin ou based on the report type
		// First construct the ou name
		// Construct the ou name
		String ouName = "OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
		String ouAdminName = "OU=" + group.getReportType() + "_Admins" + "," + ouName;
		log.info("---------------------------------------------------------------------------------");
		log.info(ouAdminName);

		try {
			log.info("executing {createAdminOrganizationalUnit}");
			
			// Construct the distinguishedName
			Attributes groupAttributes = new BasicAttributes();
			// Determine if it is an OrganizationalUnit or a Group to be created
			// if the ou variable is not null then an OU needs to be created
			if(!StringUtils.isBlank(group.getOu())) {
				Attribute objectClass = new BasicAttribute("objectClass");
				{
					objectClass.add("top");
					objectClass.add("organizationalUnit");
				}
				groupAttributes.put(objectClass);
				groupAttributes.put("distinguishedName", ouAdminName); 
				groupAttributes.put("name", group.getReportType() + "_Admins");
				groupAttributes.put("ou", group.getReportType() + "_Admins");
				groupAttributes.put("instanceType", new String("4"));
			}

			log.info(ouAdminName);
			log.info("-------------------------------------------------------------");
			ldapTemplate.bind(ouAdminName, null, groupAttributes);
			
			return true;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error("Admin OrganizationalUnit already exists!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}

	/**
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return boolean returns a boolean result.
	 * @throws IOException 
	 */
	
	public boolean createGroup(Group group)  {

		// Construct the group name

		String groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		String sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		log.info("groupName : " + groupName);
		
		
		try {
			run();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			log.info("executing {createGroup}");
			Attribute objectClass = new BasicAttribute("objectClass");
			{
				objectClass.add("top");
				objectClass.add("group");
			}
			Attributes groupAttributes = new BasicAttributes();
			groupAttributes.put(objectClass);
			groupAttributes.put("name", sAMAccountName);
			groupAttributes.put("sAMAccountName", sAMAccountName);
			log.info("boGroup : " + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("boCnGroupPrefix : " + Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX));
			log.info("boGroupPrefix : " + Config.getProperty(ParameterConstants.BO_GROUP_USERS_PREFIX));
			log.info("boUser : " + Config.getProperty(ParameterConstants.BO_USER));
			log.info("groupType : " + Config.getProperty(ParameterConstants.GROUP_TYPE));
			groupAttributes.put("groupType", Config.getProperty(ParameterConstants.GROUP_TYPE));
			//groupAttributes.put("groupType", new String("4"));

			//groupAttributes.put("managedBy", "OU=" + group.getReportType() + "_Admins,OU=" + group.getDisasterNumber() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
		
			log.info(groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("-------------------------------------------------------------");
			log.info("distinguishedName : " + groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			ldapTemplate.bind(groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP), null, groupAttributes);

			//Runtime.getRuntime().exec("set");
			
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
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return boolean returns a boolean result.
	 */
	
	public boolean createAdminGroup(Group group, String groupType) {

		// Construct the group name
		String groupName = new String();
		String sAMAccountName = new String();
		if (groupType.equalsIgnoreCase("DISASTER_NUMBER_ONLY")) {
			groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
			sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
		} else {
			groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		}
		log.info("groupName : " + groupName);

		try {
			log.info("executing {createAdminGroup}");
			Attribute objectClass = new BasicAttribute("objectClass");
			{
				objectClass.add("top");
				objectClass.add("group");
			}
			Attributes groupAttributes = new BasicAttributes();
			groupAttributes.put(objectClass);
			//log.info("group.getCn() : " + group.getCn());

			//groupAttributes.put("distinguishedName", group.getCn()); // 
			groupAttributes.put("name", sAMAccountName);
			groupAttributes.put("sAMAccountName", sAMAccountName);
			log.info("boGroup : " + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("boCnGroupPrefix : " + "OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX));
			log.info("boGroupPrefix : " + Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX));
			log.info("boUser : " + Config.getProperty(ParameterConstants.BO_USER));
			log.info("groupType : " + Config.getProperty(ParameterConstants.GROUP_TYPE));
			groupAttributes.put("groupType", Config.getProperty(ParameterConstants.GROUP_TYPE));

			//groupAttributes.put("managedBy", "OU=ha_Admins,OU=1455,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net");

			log.info(groupName + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("-------------------------------------------------------------");
//			log.info("distinguishedName : " + groupName + "," + "OU=" + group.getReportType() + "_Admins" + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
//			ldapTemplate.bind(groupName + "," + "OU=" + group.getReportType() + "_Admins" + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP), null, groupAttributes);
			log.info("distinguishedName : " + groupName + "," + "ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			ldapTemplate.bind(groupName + "," + "ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP), null, groupAttributes);


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
	
	public boolean removeGroup(Group group) {

		log.info("boDN : " + BO_DN);
		String groupDN = Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType() + ",OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
		log.info("groupDN : " + groupDN);
		ldapTemplate.unbind(groupDN);
		return true;       

	}
	
	public boolean removeAdminGroup(Group group, String groupType) {
		try {
			// Construct the group name
			String groupName = new String();
			String sAMAccountName = new String();
			if (groupType.equalsIgnoreCase("DISASTER_NUMBER_ONLY")) {
				groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
			} else {
				groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			}
			log.info("groupName : " + groupName);
	
			log.info("boDN : " + BO_DN);
			String groupDN = groupName + "," + "OU=" + group.getReportType() + "_Admins" + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
			log.info("groupDN : " + groupDN);
			ldapTemplate.unbind(groupDN);
			return true;
			
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exists!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 

	}
	
	
	public boolean removeAdminOrganizationalUnit(Group group) {
	 
		try {
		
			// Now create the corresponding admin ou based on the report type
			// First construct the ou name
			// Construct the ou name
			String ouName = "OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
			String ouAdminName = "OU=" + group.getReportType() + "_Admins" + "," + ouName;
			log.info("ouAdminName : " + ouAdminName);
			ldapTemplate.unbind(ouAdminName);
			return true;
		
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exists!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}
	
	/**
	 * This method removes a dn
	 * @param dn String value of a DN
	 * @return boolean return value.
	 */
	
	public boolean remove(String dn) {
		log.info(dn);
		ldapTemplate.unbind(dn);
		return true;
	}
	
	
	private void run() throws IOException, InterruptedException {
		Process p=Runtime.getRuntime().exec("cmd /c dir"); 
        p.waitFor(); 
        BufferedReader reader=new BufferedReader(
            new InputStreamReader(p.getInputStream())
        ); 
        String line; 
        while((line = reader.readLine()) != null) 
        { 
            System.out.println(line);
        } 
	}
	private void runTime() throws IOException {
		
		Runtime rt = Runtime.getRuntime();
		String[] commands = {"system.exe","-get t"};
		Process proc = rt.exec(commands);

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		System.out.println("Here is the standard output of the command:\n");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
		    System.out.println(s);
		}

		// read any errors from the attempted command
		System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
		    System.out.println(s);
		}
	}

	/**
	 * This class prepares User object after ldap search.
	 */
	private class UserAttributesMapper implements AttributesMapper<User> {

		//
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

		//
		public String mapFromAttributes(Attributes attrs) throws NamingException {
			Attribute cn = attrs.get("cn");
			return cn.toString();
		}
	}

	/**
	 * This class prints all the content in string format.
	 */
	private class MultipleAttributesMapper implements AttributesMapper<String> {

		//
		public String mapFromAttributes(Attributes attrs) throws NamingException {

			Map<String, Object> userHashMap = new HashMap<String, Object>();

			NamingEnumeration<? extends Attribute> all = attrs.getAll();
			StringBuffer result = new StringBuffer();
			result.append("\n Result { \n");
			while (all.hasMore()) {
				Attribute id = all.next();
				result.append(" \t |_  #" + id.getID() + "= [ " + id.get() + " ]  \n");
				log.info(id.getID() + "\t | " + id.get());
				userHashMap.put(id.getID(), id.get());
			}

			log.info("The \'distinguishedName\' attribute value is -> " + userHashMap.get("distinguishedName"));
			result.append("\n } ");
			return result.toString();
		}
	}

}
