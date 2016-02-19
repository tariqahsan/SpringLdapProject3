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
	 * This method is responsible to fetch all the user details as a list of
	 * User object
	 *
	 * @return list of {@link User}
	 */
	public List<User> getAllUsers();

	/**
	 * This method is responsible to fetch user details of particular user.
	 *
	 * @return user details {@link User}
	 */
	public User getUserDetails(String userName);

	/**
	 * This method is responsible to fetch user details of particular user as a string.
	 *
	 * @return user detail {@link User}
	 */
	public String getUserDetail(String userName);

	/**
	 * This method is responsible to authenticate user.
	 *
	 * @return boolean true|false
	 */
	public boolean authenticate(String base,String userName, String password);

	/**
	 * This method is responsible to create group.
	 */
	public boolean createGroup(Group group);
	
	/**
	 * This method is responsible to create user.
	 */
	public boolean createUser(User user);

	/**
	 * This method is responsible to delete user.
	 */
	public boolean remove(String uid);

	List<String> getUserbyGroup(String lastName);

	boolean removeGroup(Group group);

	public List<String> getAllPersonGroups(String uid);
	
	public List<String> getUserAttributes(String userName);
	
	public List<String> searchCnInGroups(String cn);

	public List<Group> getGroupList(String string);
	
	public List<User> getMemberOfListFromUser(String sAMAccountName);
	
	public List<String> getDistinguishedName(String sAMAccountName);

	public User removeGroupMember(Group group, User user);

	public User updateGroup(String userName, String value, String attributeName);

}
