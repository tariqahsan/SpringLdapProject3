package gov.fema.adminportal.ldap.model;

import java.util.List;

public class Group {
	
	private String cn;
	private List<String> member;
	private String disasterNumber;
	private String reportType;
	
	public String getCn() {
		return cn;
	}
	public void setCn(String cn) {
		this.cn = cn;
	}
	public List<String> getMember() {
		return member;
	}
	public void setMember(List<String> member) {
		this.member = member;
	}
	public String getDisasterNumber() {
		return disasterNumber;
	}
	public void setDisasterNumber(String disasterNumber) {
		this.disasterNumber = disasterNumber;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
}
