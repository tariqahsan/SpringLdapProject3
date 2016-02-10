package gov.fema.adminportal.ldap.repository;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

import gov.fema.adminportal.ldap.model.Group;
import gov.fema.adminportal.ldap.model.User;
import gov.fema.adminportal.ldap.model.LDAPConfig;

/**
 * This class implements the @see {@link UserRepository}.
 */
@Component
public class UserRepositoryImpl implements UserRepository {

	private static Logger log = Logger.getLogger(UserRepositoryImpl.class);

	public UserRepositoryImpl() {

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
	      .add("c", user.getCn())
	      .add("ou", user.getOu())
	      .build();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gov.fema.adminportal.ldap.repository.UserRepository#getAllUserNames()
	 */
	@Override
	public List<String> getAllUserNames() {
		log.info("executing {getAllUserNames}");
		//LdapQuery query = query().base("ou=users");
		LdapQuery query = query().base("ou=users,ou=system"); // Changed base="" from base="ou=system" in the context file 
		List<String> list = ldapTemplate.list(query.base());
		log.info("Users -> " + list);
		//return ldapTemplate.search(query().base("ou=users").where("objectClass").is("person"), new SingleAttributesMapper());
//		return ldapTemplate.search(query().base("ou=users,ou=system").where("objectClass").is("person"), new SingleAttributesMapper());
		return ldapTemplate.search(query().base("ou=system").where("objectClass").is("person"), new SingleAttributesMapper());
	}
	
	/*
	 * Let’s say that we want to perform a search starting at the base DN "ou=users,ou=system", 
	 * limiting the returned attributes to "cn" and "sn", with the filter (&(objectclass=person)(sn=?)), 
	 * where we want the ? to be replaced with the value of the parameter lastName. This is how we do it 
	 * using the LdapQueryBuilder:
	 */
	@Override
	public List<String> getUserbyGroup(String lastName) {
		System.out.println("User name by group");
		LdapQuery query = query()
	         .base("ou=users,ou=system")
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
	/*
	 * (non-Javadoc)
	 *
	 * @see gov.fema.adminportal.ldap.repository.UserRepository#getAllUsers()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<User> getAllUsers() {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, "(objectclass=person)", controls, new UserAttributesMapper());
	}

	@Override
	public User getUserDetails(String userName) {
		log.info("executing {getUserDetails}");
//		List<User> list = ldapTemplate.search(query().base("ou=users").where("uid").is(userName), new UserAttributesMapper());
		List<User> list = ldapTemplate.search(query().base("dc=femadev,dc=net").where("uid").is(userName), new UserAttributesMapper());
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public String getUserDetail(String userName) {
		log.info("executing {getUserDetails}");
		//List<String> results = ldapTemplate.search(query().base("ou=users").where("uid").is(userName), new MultipleAttributesMapper());
//		List<String> results = ldapTemplate.search(query().base("ou=users,ou=system").where("uid").is(userName), new MultipleAttributesMapper());
		List<String> results = ldapTemplate.search(query().base("dc=femadev,dc=net").where("uid").is(userName), new MultipleAttributesMapper());
		if (results != null && !results.isEmpty()) {
			return results.get(0);
		}
		return " userDetails for " + userName + " not found .";
	}

	 @Override
     public Object mapFromAttributes(Attributes attributes) throws NamingException {
         User user = new User();
         user.setCn((String)attributes.get("cn").get());
         List<String> memberOf = new ArrayList<String>();

         for(Enumeration vals = attributes.get("memberOf").getAll(); vals.hasMoreElements();){
             memberOf.add((String)vals.nextElement());
         }
         user.setMemberOf(memberOf);
         user.setsAMAccountName((String)attributes.get("sAMAccountName").get());
//         user.setMail((String)attributes.get("mail").get());
         return user;
     }
	@Override
	public boolean authenticate(String base, String userName, String password) {
		log.info("executing {authenticate}");
		return ldapTemplate.authenticate(base, "(uid=" + userName + ")", password);
	}

	@Override
	public User updateTelePhone(String userName, String newNumber) {
		log.info("executing {updateTelePhone}");
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telephoneNumber", newNumber));
		ldapTemplate.modifyAttributes("uid=" + userName + ",ou=users", new ModificationItem[]{item});
		//ldapTemplate.modifyAttributes("uid=" + userName + "," +  new ModificationItem[]{item});
		return getUserDetails(userName);
	}

	@Override
	public boolean createGroup(Group group) {
		
        // Construct the group name
        String groupName = ldapConfig.getBoGroupPrefix()+ group.getDisasterNumber() + group.getReportType();
        System.out.println("groupName : " + groupName);
		
		try {
			log.info("executing {createGroup}");
			Attribute objectClass = new BasicAttribute("objectClass");
		{
			objectClass.add("top");
			objectClass.add("groupOfNames");
			//objectClass.add("organizationalUnit");
		}
		Attributes groupAttributes = new BasicAttributes();
		groupAttributes.put(objectClass);
		System.out.println("group.getCn() : " + group.getCn());
		groupAttributes.put("cn", group.getCn());
		groupAttributes.put("member", "");
		//ldapTemplate.bind(bindDN(user.getUid()), null, groupAttributes);
//		System.out.println("cn=" + group.getCn() + "," + ldapConfig.getBoGroup());
		System.out.println(groupName + "," + ldapConfig.getBoGroup());
		System.out.println("-------------------------------------------------------------");
		
		//ldapTemplate.bind("cn=" + group.getCn() + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		ldapTemplate.bind(groupName + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		
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
	
	
	@Override
	public boolean createAdGroup(Group group) {
		
        // Construct the group name
        String groupName = ldapConfig.getBoGroupPrefix()+ group.getDisasterNumber() + group.getReportType();
        System.out.println("groupName : " + groupName);
		/*
		 * loAttrSet.add(new LDAPAttribute("objectclass", new String("Group")));
      loAttrSet.add(new LDAPAttribute("name", new String("Groupname2")));
      loAttrSet.add(new LDAPAttribute("sAMAccountname", new String("PO_ADMIN")));
      loAttrSet.add(new LDAPAttribute("description", new String("All Groupname2"))
		 */
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
		System.out.println("group.getCn() : " + group.getCn());
		
		groupAttributes.put("distinguishedName", group.getCn());
		groupAttributes.put("name", group.getCn());
		groupAttributes.put("sAMAccountName", group.getCn());
		groupAttributes.put("groupType", new String("4"));
		//ldapTemplate.bind(bindDN(user.getUid()), null, groupAttributes);
//		System.out.println("cn=" + group.getCn() + "," + ldapConfig.getBoGroup());
		System.out.println(groupName + "," + ldapConfig.getBoGroup());
		System.out.println("-------------------------------------------------------------");
		groupAttributes.put("distinguishedName", groupName + "," + ldapConfig.getBoGroup());
		//ldapTemplate.bind("cn=" + group.getCn() + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		ldapTemplate.bind(groupName + "," + ldapConfig.getBoGroup(), null, groupAttributes);
		
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
	
	@Override
	public boolean createUser(User user) {
		
		try {
			log.info("executing {createUser}");
			Attribute objectClass = new BasicAttribute("objectClass");
		{
			objectClass.add("top");
			//objectClass.add("uidObject");
			objectClass.add("inetOrgPerson");
			objectClass.add("person");
			objectClass.add("organizationalPerson");
		}
		Attributes userAttributes = new BasicAttributes();
		userAttributes.put(objectClass);
		userAttributes.put("cn", user.getCn());
		userAttributes.put("sn", user.getSn());
		userAttributes.put("uid", user.getUid());
		userAttributes.put("postalAddress", user.getPostalAddress());
		userAttributes.put("telephoneNumber", user.getTelephoneNumber());
		userAttributes.put("userPassword", user.getUserPassword().getBytes());
		System.out.println("user.getUid() :" + user.getUid());
		
		// We'll not use bindDN
//		System.out.println(bindDN(user.getUid()));
//		System.out.println("-----------------------------------------------");
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
	
	@Override
	public boolean removeGroup(Group group) {
		
        // Construct the group name
        String groupDN = ldapConfig.getBoGroupPrefix() + group.getDisasterNumber() + group.getReportType() + "," + ldapConfig.getBoGroup();
        System.out.println("groupDN : " + groupDN);
        ldapTemplate.unbind(groupDN);
		return true;       
     
	}
	
	@Override
//	public boolean remove(String uid) {
	public boolean remove(String dn) {
//		System.out.println(bindDN(uid));
//		System.out.println("--------------------------------------------------");
//		System.out.println(uid);
//		System.out.println("--------------------------------------------------");
		//ldapTemplate.unbind(bindDN(uid));
		//ldapTemplate.unbind(uid);
		System.out.println(dn);
		ldapTemplate.unbind(dn);
		return true;
	}

	public static javax.naming.Name bindDN(String _x){
		@SuppressWarnings("deprecation")
		javax.naming.Name name = new DistinguishedName("uid=" + _x + ",ou=users");
		return name;
	}

	/**
	 * This class prepares User object after ldap search.
	 */
	private class UserAttributesMapper implements AttributesMapper<User> {

		@Override
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
			if (attributes.get("uid") != null) {
				user.setUid(attributes.get("uid").get().toString());
			}
			if (attributes.get("uid") != null) {
				user.setUid(attributes.get("uid").get().toString());
			}
			if (attributes.get("sn") != null) {
				user.setSn(attributes.get("sn").get().toString());
			}
			if (attributes.get("postalAddress") != null) {
				user.setPostalAddress(attributes.get("postalAddress").get().toString());
			}
			if (attributes.get("telephoneNumber") != null) {
				user.setTelephoneNumber(attributes.get("telephoneNumber").get().toString());
			}
			return user;
		}
	}

	/**
	 * This class prints only cn .
	 */
	private class SingleAttributesMapper implements AttributesMapper<String> {

		@Override
		public String mapFromAttributes(Attributes attrs) throws NamingException {
			Attribute cn = attrs.get("cn");
			return cn.toString();
		}
	}

	/**
	 * This class prints all the content in string format.
	 */
	private class MultipleAttributesMapper implements AttributesMapper<String> {

		@Override
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
