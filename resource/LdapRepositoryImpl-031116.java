package gov.fema.adminportal.ldap.repository;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.util.Config;
import gov.fema.adminportal.util.ParameterConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.ContextNotEmptyException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

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
		return ldapTemplate.search(query().base("OU=ServiceAccounts,OU=MW,DC=fema,DC=net").where("objectClass").is("person"), new SingleAttributesMapper());
	}

	
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
			}
		});
	}

	
	/**
	 * This method is to get the DN value of a sAMAccountName
	 * @param sAMAccountName String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String getDistinguishedName(String sAMAccountName) {
		try {
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
		} catch (IndexOutOfBoundsException ex1) {
			log.error("Name of the group is not found");
			return null;
		} catch (Exception ex2) {
			log.error(ex2, ex2);
			return null;
		}
	}
	
	/**
	 * This method is to get the DN value of a sAMAccountName
	 * @param sAMAccountName String value passed
	 * @return String returns user DN value in string text format.
	 */
	public String getDistinguishedGroupName(String sAMAccountName) {
		try {
			AndFilter filter = new AndFilter();
			filter.and(new EqualsFilter("objectclass", "top"));
			filter.and(new EqualsFilter("objectclass", "group"));
			filter.and(new EqualsFilter("sAMAccountName", sAMAccountName));
			List<String> searchList = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper() {
				public Object mapFromAttributes(Attributes attrs) throws NamingException {
					return attrs.get("distinguishedName").get();
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
	 * This method is to get the sAMAccountName from it's corresponding DN value
	 * @param  String distinguishedName value passed
	 * @return String returns user sAMAccountName value in string text format.
	 */
	public String getSAMAccountName(String distinguishedName) {
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new EqualsFilter("distinguishedName", distinguishedName));
		List<String> searchList = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("sAMAccountName").get();
			}
		});
		//log.info("searchList.get(0) : " + searchList.get(0));
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
	 * This method is to get the DN value of a sAMAccountName
	 * and traverses the AD tree from the passed BASE DN root
	 * searching the user DN in all group objects.
	 * @param sAMAccountName String value passed
	 * @return String returns list of Group CN values in string 
	 * text format.
	 */
	public List<String> getAllMembers() {
		log.info("Starting getAllMembers() ...");
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		List<String> searchList = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				log.info("group -> " + attrs.get("cn").get(0));
				return attrs.get("cn").get();
			}
		});
		return searchList;
	}
	
	
	public List<Group> searchMemberInGroup(String sAMAccountName) {
		
		// Get the CN value of the user that should be part of the 'member' attribute of the group
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		//filter.and(new EqualsFilter("objectCategory", "CN=Group,CN=Schema,CN=Configuration,DC=femadev,DC=net"));
		filter.and(new EqualsFilter("distinguishedName", Config.getProperty(ParameterConstants.BO_USER)));
		filter.and(new EqualsFilter("member", getDistinguishedName(sAMAccountName)));
		List<Group> groups = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper<Group>() { 
				
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
	
	public List<Group> searchMemberInAGroup(String sAMAccountName) {

		log.info("Running searchMemberInGroup(" + sAMAccountName + ")");
		log.info(getDistinguishedName(sAMAccountName));
		log.info(Config.getProperty(ParameterConstants.BO_SEE_ALL_DISASTER_GROUPS));
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		filter.and(new EqualsFilter("distinguishedName", Config.getProperty(ParameterConstants.BO_SEE_ALL_DISASTER_GROUPS)));
		filter.and(new EqualsFilter("member", getDistinguishedName(sAMAccountName)));
		List<Group> groups = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper<Group>() { 

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
	
	public List<User> getGroupNameList(String sAMAccountName) {
				
		try {
			// Create the return type list of Users
			List<User> users = new ArrayList<User>();
			// To make it consistent with the getMemberOfListFromUser method will be storing the concatenated
			// value of the disasterNumber and reportType in the User.disasterNumberReportTypes list variable
			LinkedList<String> disasterNumberReportTypes = new LinkedList<String>();
			
			// First check if the user is a member of the BO_SEE_ALL_DISASTER_GROUPS
			List<Group> groups = searchMemberInAGroup(sAMAccountName);

			if (groups != null && groups.size() > 0) {
				
				List<Group> groupList = getMemberList();		
				for (Group adGroup : groupList) {

					// Create an User object and set the group's sAMAccountName to it
					User user = new User();
					
					// Only store the user groups excluding the admin groups in the list
					if (adGroup.getsAMAccountName().startsWith(Config.getProperty(ParameterConstants.BO_GROUP_USERS_PREFIX))) {
						
						String groupName = adGroup.getsAMAccountName();
						log.info(groupName);
						
						// Now parse the group name to extract the disasterNumber(the content after the last underscore)
						String varStr = groupName.substring(groupName.lastIndexOf('_') + 1);	
						String reportType =  StringUtils.right(varStr, 2 );
						String disasterNumber = StringUtils.removeEnd(varStr, reportType);
						// Concatenate disasterNumber with reportType
						String disasterNumberReportType = disasterNumber.concat(reportType);
						user.setDisasterNumberReportType(disasterNumberReportType);
						disasterNumberReportTypes.add(disasterNumberReportType);
						user.setDisasterNumberReportTypes(disasterNumberReportTypes);

						// Store this in the User list
						users.add(user);
					}
				}
				
				return users;
				
			} else {
				
				return users = getMemberOfListFromUser(sAMAccountName);			
			}
		}  catch (IndexOutOfBoundsException ex1) {
			log.error("Empty group list");
			return null;
		} catch (Exception ex2) {
			log.error(ex2, ex2);
			return null;
		}
	}
	
	public List<Group> getMemberList() {
		
		// Get the CN value of the user that should be part of the 'member' attribute of the group
				
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "top"));
		filter.and(new EqualsFilter("objectclass", "group"));
		
		List<Group> groups = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper<Group>() { 
				
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
				LinkedList<String> disasterNumberReportTypes = new LinkedList<String>();
				
				String distinguishedName = attributes.get("distinguishedName") != null ? (String) attributes.get("distinguishedName").get() : ""; 
				String cn = attributes.get("cn") != null ? (String) attributes.get("cn").get() : ""; 
				String displayName = attributes.get("displayName") != null ? (String) attributes.get("displayName").get() : "";
				String sAMAccountName = attributes.get("sAMAccountName") != null ? (String) attributes.get("sAMAccountName").get() : "";
				String mail = attributes.get("mail") != null ? (String) attributes.get("mail").get() : "";

				// memberOf attribute may contain multiple values
				Attribute memberOfVal = attributes.get("memberOf"); 
				if (memberOfVal != null) { 
					NamingEnumeration<?> memberOf = memberOfVal.getAll(); 
					while (memberOf.hasMore()) { 
						String groupDn = (String) memberOf.next();
						// Only store the groups that has prefix of CN=Business_Objects_Disaster_Users_ and exclude the group - CN=Business_Objects_Disaster_Users_Admin
						if (groupDn.startsWith(Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX)) && !groupDn.startsWith(Config.getProperty(ParameterConstants.BO_CN_DISASTER_USERS_ADMIN))) {
							
							String groupName = getNamefromGroupDn(groupDn);
							
							// Now parse the group name to extract the disasterNumber(the content after the last underscore)
							String varStr = groupName.substring(groupName.lastIndexOf('_') + 1);	
							String reportType =  StringUtils.right(varStr, 2 );
							String disasterNumber = StringUtils.removeEnd(varStr, reportType);
							// Concatenate disasterNumber with reportType
							String disasterNumberReportType = disasterNumber.concat(reportType);
							memberOfs.add(groupDn);
							disasterNumbers.add(disasterNumber);
							reportTypes.add(reportType);
							disasterNumberReportTypes.add(disasterNumberReportType);
							
						}
					} 
				} 

				return new User(distinguishedName, null, displayName, cn, memberOfs, sAMAccountName, null, null, null, mail, disasterNumbers, reportTypes, disasterNumberReportTypes); 
			} 
		}); 

		return users; 
	}
		
	/**
	 * This method is used to remove a specified value in the 'member'
	 * attribute.
	 * @param User a User object is the first parameter
	 * @param Group a Group object is the second parameter
	 * @return User returns a User object.
	 */	
	public boolean removeGroupMember(String userName, String groupName) {
		
		log.info("executing {removeGroupMember}");
		try {	
			// Get the DN value of the userName
			String userDn = getDistinguishedName(userName);

			// Get the DN value of the groupName
			String groupDn = getDistinguishedGroupName(groupName);

			ldapTemplate.modifyAttributes(groupDn, new ModificationItem[] {
				new ModificationItem(
						DirContext.REMOVE_ATTRIBUTE,
						new BasicAttribute("member", userDn))
			});
		
			return true;
			
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exist!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 

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
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return Group returns a Group object result.
	 */
	public Group createOrganizationalUnit(Group group) {

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

			ldapTemplate.bind(ouName, null, groupAttributes);
			group.setMessage(group.getOu().concat(" Organizational Unit created") );
			return group;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error("OrganizationalUnit already exist!");
			group.setMessage(group.getOu().concat(" Organizational Unit already exists"));
			return group;
		} catch (Exception e) {
			log.error(e, e);
			group.setMessage("Error in creating " + group.getOu().concat(" Organizational Unit"));
			return group;
		} 
	}
	
	/**
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return Group returns a Group object result.
	 * @throws IOException 
	 */
	
	public Group createGroup(Group group)  {

		// Construct the group name
		String groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		String sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		log.info("groupName : " + groupName);
		
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
			groupAttributes.put("groupType", Config.getProperty(ParameterConstants.GROUP_TYPE));
			String distinguishedName = groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);

			ldapTemplate.bind(distinguishedName, null, groupAttributes);
			group.setsAMAccountName(sAMAccountName);
			group.setDistinguishedName(groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			group.setMessage(sAMAccountName.concat(" Active Directory Group created"));
			return group;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error(sAMAccountName.concat(" Active Directory Group already exists"));
			group.setMessage(sAMAccountName.concat(" Active Directory Group already exists"));
			return group;
		} catch (Exception e) {
			log.error(e, e);
			group.setMessage("Error in creating " + sAMAccountName.concat(" Active Directory Group"));
			return group;
		} 
	}
	
	/**
	 * This method is used to create a MS Active Directory group object.
	 * @param Group a Group bean is passed as an argument
	 * @return Group returns a Group object result.
	 */
	
	public Group createAdminGroup(Group group, String groupType) {

		// Construct the group name
		String groupName = new String();
		String sAMAccountName = new String();
		if (groupType.equalsIgnoreCase(Config.getProperty(ParameterConstants.BO_DISASTER_NUMBER_ONLY))) {
			groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
			sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
		} else {
			groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
		}

		try {

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

			ldapTemplate.bind(groupName + "," + "ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP), null, groupAttributes);
			group.setMessage(sAMAccountName.concat(" Active Directory Group created"));
			return group;
		} catch (NameAlreadyBoundException nabex) {        	
			log.error(sAMAccountName.concat(" Active Directory Group already exists"));
			group.setMessage(sAMAccountName.concat(" Active Directory Group already exists"));
			return group;
		} catch (Exception e) {
			log.error(e, e);
			group.setMessage("Error in creating " + sAMAccountName.concat(" Active Directory Group"));
			return group;
		} 
	}

	/**
	 * This method removes a Group
	 * @param group parameter is a Group bean object 
	 * @return boolean value.
	 */
	
	public boolean removeGroup(Group group) {
		try {

			String groupDN = Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType() + ",OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
			ldapTemplate.unbind(groupDN);
			return true;
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exist!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 

	}
	
	public boolean removeAdminGroup(Group group, String groupType) {
		
		try {
			
			// Construct the group name
			String groupName = new String();
			String sAMAccountName = new String();
			
			if (groupType.equalsIgnoreCase(Config.getProperty(ParameterConstants.BO_DISASTER_NUMBER_ONLY))) {
				groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
				sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
			} else {
				groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
				sAMAccountName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			}

			String groupDN = groupName + "," + "OU=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
			ldapTemplate.unbind(groupDN);
			return true;
			
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exist!");
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

			ldapTemplate.unbind(ouName);
			return true;
		} catch (ContextNotEmptyException cnee) {
			log.error("OrganizationalUnit is not empty!");
			return false;
		
		} catch (NameNotFoundException nnfex) {        	
			log.error("Group does not exist!");
			return false;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		} 
	}

	/**
	 * This method calls the method getMemberList() which gets the
	 * list of DNs of a member attribute of MS Active Directory group objects
	 * that is in the path of a specified DN.
	 * @return List<User> returns list of User objects.
	 */
	public List<User> getAllUserDetails() {
		
		List<Group> groups = getMemberList();
		List<User> adUsers = new ArrayList<User>();
		for (Group adGroup : groups) {
			
			for (String member : adGroup.getMembers()) {
				User adUser = getUserDetailsFromDn(member);
				String varStr = adGroup.getsAMAccountName().substring(adGroup.getsAMAccountName().lastIndexOf('_') + 1);	
				String reportType =  StringUtils.right(varStr, 2 );
				String disasterNumber = StringUtils.removeEnd(varStr, reportType);
				adUser.setDisasterNumber(disasterNumber);
				adUser.setReportType(reportType);
				
				// Concatenating disasterNumber + reportType
				String disasterNumberReportType = disasterNumber + reportType;
				adUser.setDisasterNumberReportType(disasterNumberReportType);

				adUsers.add(adUser);
			}
		}
		
		return adUsers;
	}
	
	/**
	 * This method calls the method getMemberList() which gets the
	 * list of DNs of a member attribute of MS Active Directory group objects
	 * that is in the path of a specified DN.
	 * @return List<User> returns list of User objects.
	 */
	public List<User> getAllUserDetailsReportGroup() {
		
		List<Group> groups = getMemberList();
		List<User> adUsers = new ArrayList<User>();
		for (Group adGroup : groups) {

			if (adGroup.getDistinguishedName().startsWith(Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX))) {	
				
				for (String member : adGroup.getMembers()) {

					User adUser = getUserDetailsFromDn(member);
					String varStr = adGroup.getsAMAccountName().substring(adGroup.getsAMAccountName().lastIndexOf('_') + 1);	
					String reportType =  StringUtils.right(varStr, 2 );
					String disasterNumber = StringUtils.removeEnd(varStr, reportType);
					adUser.setDisasterNumber(disasterNumber);
					adUser.setReportType(reportType);
					
					// Concatenating disasterNumber + reportType
					String disasterNumberReportType = disasterNumber + reportType;
					adUser.setDisasterNumberReportType(disasterNumberReportType);
	
					adUsers.add(adUser);
				}
			
			}
		}
		
		return adUsers;
	}
	
	/**
	 * This method returns the user details
	 * @param distinguishedName is a String value
	 * @return User This returns User object.
	 */
	public User getUserDetailsFromDn(String distinguishedName) {
		
		List<User> list = ldapTemplate.search(query().base(BASE_DN).where("distinguishedName").is(distinguishedName), new UserAttributesMapper());
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
	public Group addGroupPermission(Group group, String adminType) {
		
		Process process = null;
		String adminGroupDn = null;
		String adminGroupName = null;
		
		try {
					
			log.info("Running addGroupPermission ..." );
			
			String groupName = Config.getProperty(ParameterConstants.BO_CN_GROUP_USERS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			String userGroupDn = groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP);
			String domainName = Config.getProperty(ParameterConstants.BO_DOMAIN_NAME);
			String accessTypes = Config.getProperty(ParameterConstants.BO_ACCESS_TYPES);
			
			if (adminType.equalsIgnoreCase(Config.getProperty(ParameterConstants.BO_DISASTER_NUMBER_ONLY))) {
				adminGroupName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber();
			} else {
				adminGroupName = Config.getProperty(ParameterConstants.BO_GROUP_ABBR_ADMINS_PREFIX) + group.getDisasterNumber() + group.getReportType();
			}
			
			adminGroupDn = getDistinguishedGroupName(adminGroupName);
			
			// To break out from infinite while loop
			long startTime = System.currentTimeMillis();
			long waitTime = Long.parseLong(Config.getProperty(ParameterConstants.BO_LOOP_TIME)) * 60 * 1000; // Convert minutes to milliseconds
			long endTime = startTime + waitTime;
			//long sleepTime = Long.parseLong(Config.getProperty(ParameterConstants.SLEEP_TIME)) * 1000; // Convert seconds to milliseconds
			// return code from the dcsacls command
			int processExitValue = 0;
			
			log.info("Wait Time : " + waitTime);
			log.info("End Time : " + endTime);
			//log.info("Sleep Time : " + sleepTime);
			
			boolean flag = true;
			// Check if the groups exist in AD. Also prevent looping indefinitely 
			while (flag && System.currentTimeMillis() < endTime) {
				if (getNamefromGroupDn(adminGroupDn) != null && getNamefromGroupDn(userGroupDn) != null) {
					log.info("Groups does exist");
					flag = false;
					break;
				}
				log.info("Groups do not exist");
			}
			
			// Sleep for a pre-determined time before kicking off the dcacls command
			//TimeUnit.SECONDS.toSeconds(Long.parseLong(Config.getProperty(ParameterConstants.SLEEP_TIME)));
			//Thread.sleep(Integer.parseInt(Config.getProperty(ParameterConstants.SLEEP_TIME)) * 1000);
			
			log.info("Adding " + groupName + ",ou=" + group.getOu() + "," + Config.getProperty(ParameterConstants.BO_GROUP));
			log.info("for group -> " + adminGroupName);		
			
			log.info("Adding permission to group");
			
			// Loop until the return code from the dcacls command return code is 0 and exceeded the time limit
			do  {
				log.info("In the dcacls command execution loop");
				String dcaclsCommand = "cmd /C dsacls \"" + userGroupDn +  "\" /G " + domainName + "\\" + adminGroupName + ":" + accessTypes;
				log.info("dcaclsCommand :" + dcaclsCommand);
				process = Runtime.getRuntime().exec(dcaclsCommand);
				ReadStream s1 = new ReadStream("stdin", process.getInputStream ());
				ReadStream s2 = new ReadStream("stderr", process.getErrorStream ());
				s1.start ();
				s2.start ();
				process.waitFor();        
				processExitValue = process.exitValue();
				log.info("processExitValue -> <" + processExitValue + ">");  
				log.info(dcaclsCommand + " command run completed");
				if (processExitValue == 0) {
					group.setMessage(adminGroupName + " Active Directory Group permissions added");
					log.info("Breaking out from the command execution loop");
					break;
				} else {
					group.setMessage(adminGroupName + " failed to add Active Directory Group permissions as Group does not exist yet");
				}
			} while (processExitValue != 0 && System.currentTimeMillis() < endTime);
			
			return group;
		} catch (IOException ioEx) {
			log.error("dsacls executable is not found!");
			group.setMessage(adminGroupName + " failed to add Active Directory Group permissions");
			return group;
		/*} catch (InterruptedException iEx) {        	
			log.error("Process interrupted!");
			return false;*/
		} catch (Exception e) {
			log.error(e, e);
			group.setMessage(adminGroupName + " failed to add Active Directory Group permissions");
			return group;
		} finally {
		    if(process != null)
		        process.destroy();
		}
	}
	
	public class ReadStream implements Runnable {
	    String name;
	    InputStream is;
	    Thread thread;      
	    public ReadStream(String name, InputStream is) {
	        this.name = name;
	        this.is = is;
	    }       
	    public void start () {
	        thread = new Thread (this);
	        thread.start ();
	    }       
	    public void run () {
	        try {
	            InputStreamReader isr = new InputStreamReader (is);
	            BufferedReader br = new BufferedReader (isr);   
	            while (true) {
	                String s = br.readLine ();
	                if (s == null) break;
	                log.info("[" + name + "] " + s);
	            }
	            is.close ();    
	        } catch (Exception ex) {
	            log.error("Problem reading stream " + name + "... :" + ex);
	            ex.printStackTrace ();
	        }
	    }
	}
	
	/*****************************************************************
	 * Commented out unused methods that might become useful in future
	 * 
	 *****************************************************************/
	
//	/**
//	 * This method is used to authenticate a user against Active Directory.
//	 * @param base the base DN
//	 * @param userName CN attribute value of the user
//	 * @param password password attribute value
//	 * @return boolean boolean result is returned
//	 */
//	
//	public boolean authenticate(String base, String userName, String password) {
//		log.info("executing {authenticate}");
//		return ldapTemplate.authenticate(base, "(cn=" + userName + ")", password);
//	}
	
//	/**
//	 * This method creates a User
//	 * @param User passed as parameter a User bean object 
//	 * @return boolean returns a boolean value.
//	 */
//	
//	public boolean createUser(User user) {
//
//		try {
//			log.info("executing {createUser}");
//			Attribute objectClass = new BasicAttribute("objectClass");
//			{
//				objectClass.add("top");
//				objectClass.add("user");
//				objectClass.add("person");
//				objectClass.add("organizationalPerson");
//			}
//			Attributes userAttributes = new BasicAttributes();
//			userAttributes.put(objectClass);
//			userAttributes.put("cn", user.getCn());
//			userAttributes.put("sAMAccountName", user.getsAMAccountName());
//			userAttributes.put("name", user.getName());
//			userAttributes.put("givenName", user.getGivenName());
//			userAttributes.put("principalName", user.getUserPrincipalName());
//			//userAttributes.put("userPassword", user.getUserPassword().getBytes());
//			log.info("user.getCn() :" + user.getCn());
//
//			return true;
//			
//		} catch (Exception e) {
//			log.error(e, e);
//			return false;
//		} 
//	}
	
//	/**
//	 * This method is used to find all users traversing from the 
//	 * root of the specified DN
//	 * @param numA This is the first paramter to addNum method
//	 * @param numB  This is the second parameter to addNum method
//	 * @return int This returns sum of numA and numB.
//	 */
//	@SuppressWarnings("deprecation")
//	public List<User> getAllUsers() {
//		SearchControls controls = new SearchControls();
//		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//		//		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, "(objectclass=person)", controls, new UserAttributesMapper());
//		List<User> list = ldapTemplate.search(BASE_DN, "(objectclass=person)", controls, new UserAttributesMapper());
//		if (list != null && !list.isEmpty()) {
//			return list;
//		}
//		return null;
//		
//	}
//
//
//	/**
//	 * This method returns the user details
//	 * @param userName is a String value
//	 * @return User This returns User object.
//	 */
//	public User getUserDetails(String sAMAccountName) {
//		log.info("executing {getUserDetails}");
//		//		List<User> list = ldapTemplate.search(query().base("ou=users").where("cn").is(userName), new UserAttributesMapper());
//		//		List<User> list = ldapTemplate.search(query().base(BASE_DN).where("cn").is(userName), new UserAttributesMapper());
//		List<User> list = ldapTemplate.search(query().base(BASE_DN).where("sAMAccountName").is(sAMAccountName), new UserAttributesMapper());
//		if (list != null && !list.isEmpty()) {
//			return list.get(0);
//		}
//		return null;
//	}
//	
//	
//
//	/**
//	 * This method is for getting user details
//	 * @param userName String value passed
//	 * @return String returns user details in string text format.
//	 */
//
//	public String getUserDetail(String userName) {
//		log.info("executing {getUserDetail}");
//		//List<String> results = ldapTemplate.search(query().base("ou=users").where("uid").is(userName), new MultipleAttributesMapper());
//		//		List<String> results = ldapTemplate.search(query().base("ou=users,ou=system").where("cn").is(userName), new MultipleAttributesMapper());
//		//List<String> results = ldapTemplate.search(query().base(BASE_DN).where("cn").is(userName), new MultipleAttributesMapper());
//		List<String> results = ldapTemplate.search(query().base(BASE_DN).where("sAMAccountName").is(userName), new MultipleAttributesMapper());
//		if (results != null && !results.isEmpty()) {
//			return results.get(0);
//		}
//		log.info("userDetails for " + userName + " not found");
//		return null;
//	}
	
//	/**
//	 * This method is used to remove a specified value in the 'member'
//	 * attribute.
//	 * @param User a User object is the first parameter
//	 * @param Group a Group object is the second parameter
//	 * @return User returns a User object.
//	 */	
//	public User removeGroupMember(Group group, User user) {
//		log.info("executing {removeGroupMember}");
//		log.info("group.getDn() : " + group.getDistinguishedName() + " \n\t\t user.getDn() : " + user.getDistinguishedName());
//		ldapTemplate.modifyAttributes(group.getDistinguishedName(), new ModificationItem[] {
//			new ModificationItem(
//					DirContext.REMOVE_ATTRIBUTE,
//					new BasicAttribute("member", user.getDistinguishedName()))
//		});
//		//	
//		return user;
//	}
	
//	/**
//	 * This method is to get the DN value of a sAMAccountName
//	 * and traverses the AD tree from the passed BASE DN root
//	 * searching the user DN in all group objects.
//	 * @param sAMAccountName String value passed
//	 * @return String returns list of Group CN values in string 
//	 * text format.
//	 */
//	public List<String> searchCnInGroups(String sAMAccountName) {
//		log.info("Starting searchInGroups for sAMAccountName -> " + sAMAccountName);
//		
//		// Get the distinguishedName from it's sAMAccountName
//		String distinguishedName = getDistinguishedName(sAMAccountName);
//		log.info("distinguishedName : " + distinguishedName);
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter("objectclass", "top"));
//		filter.and(new EqualsFilter("objectclass", "group"));
//		filter.and(new EqualsFilter("member", distinguishedName));
//		List<String> searchList = ldapTemplate.search(BO_DN, filter.encode(), new AttributesMapper() {
//			public Object mapFromAttributes(Attributes attrs) throws NamingException {
//				log.info("group -> " + attrs.get("cn").get(0));
//				return attrs.get("cn").get();
//			}
//		});
//		return searchList;
//	}
	
//	/**
//	 * This method modifies value of an attribute of a group.
//	 * @param userName String variable for name attribute
//	 * @param value String of the new attribute value
//	 * @param attributeName String value of the attribute name
//	 * @return User an User class object is returned.
//	 */
//
//	public void updateGroup(String userName, String value, String attributeName) {
//		log.info("executing {updateGroup}");
//		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, value));
////		ldapTemplate.modifyAttributes("name=" + userName + ",ou=users", new ModificationItem[]{item});
//		ldapTemplate.modifyAttributes("OU=1454,OU=DR,OU=Business_Objects,OU=EnterpriseGroups,OU=EnterpriseServers,DC=fema,DC=net", new ModificationItem[]{item});
//		
//	}
	
//	private void dirList() throws IOException, InterruptedException {
//		//Process p=Runtime.getRuntime().exec("cmd /c dir"); 
//		Process p=Runtime.getRuntime().exec("cmd /c whoami"); 
//		//p.waitFor(); 
//		BufferedReader reader=new BufferedReader(
//				new InputStreamReader(p.getInputStream())
//				); 
//		String line; 
//		while((line = reader.readLine()) != null) 
//		{ 
//			log.info(line);
//		} 
//	}
	
//	/**
//	 * This method is to get the DN value of a name
//	 * @param name String value passed
//	 * @return String returns user DN value in string text format.
//	 */
//	public String findOrganizationalUnit(String name) {
//		
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter("objectclass", "top"));
//		filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
//		filter.and(new EqualsFilter("name", name));
//		
//		List<String> searchList = ldapTemplate.search(BASE_DN, filter.encode(), new AttributesMapper() {
//			public Object mapFromAttributes(Attributes attrs) throws NamingException {
//				return attrs.get("distinguishedName").get();
//			}
//		});
//		log.info("searchList.get(0) : " + searchList.get(0));
//		return searchList.get(0);
//	}
	
//	@SuppressWarnings("unchecked")
//	public List<String> getAllPersonGroups(String cn) {
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter("objectclass", "group"));
//		log.info("member" + "," + "cn=" + cn +",ou=User,ou=BusinessObjectsDisaster,ou=system");
//		filter.and(new EqualsFilter("member", "cn=" + cn + ",ou=User,ou=BusinessObjectsDisaster,ou=system"));
//		return ldapTemplate.search("", filter.encode(), new AttributesMapper() {
//			public Object mapFromAttributes(Attributes attrs) throws NamingException {
//				return attrs.get("cn").get();
//			}
//		});
//	}


	
	/******************
	 * 
	 * Inner classes
	 *
	 ******************
	 */
	
	/**
	 * This class prepares User object after ldap search.
	 */
	private class UserAttributesMapper implements AttributesMapper<User> {

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
			if (attributes.get("mail") != null) {
				user.setMail(attributes.get("mail").get().toString());
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
