package gov.fema.adminportal.ldap.repository;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;

/**
 * <pre>
 * This interface is used for
 * 	 a) fetch all the user details as a list of String
 *   b) fetch all the user details as a list of User object
 *   c) fetch user details of particular user.
 * </pre>
 *
 */
public interface LdapRepository {

	/**
	 * This method is responsible to fetch all the user cn values as a list of
	 * String.
	 *
	 * @return list of String.
	 */
	public List<String> getAllUserNames(String baseDn);

	/**
	 * This method is responsible to create group.
	 */
	public Group createGroup(Group group);

	public List<String> getUserbyGroup(String lastName);

	public boolean removeGroup(Group group);

	public List<String> getUserAttributes(String userName);
	
	public List<User> getMemberOfListFromUser(String sAMAccountName);
	
	public String getDistinguishedName(String sAMAccountName);
	
	public List<Group> searchMemberInGroup(String sAMAccountName);
	
	public List<Group> searchMemberInAGroup(String sAMAccountName);

	public Group createOrganizationalUnit(Group group);
	
	public String getNamefromGroupDn(String distinguishedName);
	
	public Group createAdminGroup(Group group, String groupType);
	
	public boolean removeAdminGroup(Group group, String groupType);
	
	public boolean removeAdminOrganizationalUnit(Group group);
	
	public String getSAMAccountName(String distinguishedName);
	
	public User getUserDetailsFromDn(String distinguishedName);
	
	public List<User> getAllUserDetails();
	
	public List<User> getAllUserDetailsReportGroup();
	
	public boolean removeGroupMember(String userName, String groupName);

	public String findOuChildren(String ouName);
	
	public List<String> getAllMembers();
	
	public List<Group> getMemberList();
	
	public String getDistinguishedGroupName(String sAMAccountName);
	
	public Group addGroupPermission(Group group, String adminType);
	
	public List<User> getGroupNameList(String sAMAccountName);
	
	
	// Unused methods commented out in the implementation code
	
	//public void updateGroup(String userName, String value, String attributeName);
	//public User removeGroupMember(Group group, User user);
	//public List<String> searchCnInGroups(String cn);
	/**
	 * This method is responsible to create user.
	 */
	//public boolean createUser(User user);
//	/**
//	 * This method is responsible to fetch all the user details as a list of
//	 * User object
//	 *
//	 * @return list of {@link User}
//	 */
//	public List<User> getAllUsers();

//	/**
//	 * This method is responsible to fetch user details of particular user.
//	 *
//	 * @return user details {@link User}
//	 */
//	public User getUserDetails(String userName);

//	/**
//	 * This method is responsible to fetch user details of particular user as a string.
//	 *
//	 * @return user detail {@link User}
//	 */
//	public String getUserDetail(String userName);

	/**
	 * This method is responsible to authenticate user.
	 *
	 * @return boolean true|false
	 */
	//public boolean authenticate(String base,String userName, String password);
	//public List<String> getAllPersonGroups(String uid);
	//public String findOrganizationalUnit(String name);

}
