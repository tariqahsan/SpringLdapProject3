package gov.fema.adminportal.ldap.model;

public class LDAPConfig {
	
	
	private String boUser;
    private String boGroup;
    private String boGroupPrefix;
    
	public String getBoUser() {
		return boUser;
	}
	public void setBoUser(String boUser) {
		this.boUser = boUser;
	}
	public String getBoGroup() {
		return boGroup;
	}
	public void setBoGroup(String boGroup) {
		this.boGroup = boGroup;
	}
	public String getBoGroupPrefix() {
		return boGroupPrefix;
	}
	public void setBoGroupPrefix(String boGroupPrefix) {
		this.boGroupPrefix = boGroupPrefix;
	}
	
}
