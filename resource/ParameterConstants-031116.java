/**
 * This class contains all the constants that will fetch configuration parameters
 * from a properties file or the spring context file when the web app boot straps
 * itself
 */
package gov.fema.adminportal.util;

public interface ParameterConstants {
    public static final String BO_USERNAME = "bo_username";
    public static final String BO_PASSWORD = "bo_password";
    public static final String BO_AP_USERNAME = "bo_ap_username";
    public static final String BO_AP_PASSWORD = "bo_ap_password";
    public static final String BO_CMS = "bo_cms";
    public static final String BO_TRUSTED_SHARED_SECRET= "bo_shared_secret";
    public static final String AP_REPORTS_AUTHORIZED = "ap_reports_authorized";
    public static final String AP_GROUP_RIGHTS_AUTHORIZED = "ap_user_group_rights_authorized";
    public static final String AP_REPORT_METRICS_ID = "ap_report_metrics_id";
    public static final String AP_DATABASE_AUTHORIZED = "ap_database_authorized";
    public static final String AP_LOGGING_ALERTS_ID = "ap_logging_alerts_id";
    public static final String AP_LOGGING_ERRORS_ID = "ap_logging_errors_id";
    public static final String AP_LOGGING_STATUS_ID = "ap_logging_status_id";
    public static final String AP_RIGHTS_AUTHORIZED = "ap_rights_authorized";
    public static final String AP_USER_GROUP_RIGHTS_GROUPS = "ap_user_group_rights_groups";
    public static final String AP_USER_GROUP_RIGHTS_PAGESIZE = "ap_user_group_rights_pagesize";
    public static final String AP_DISASTER_USER_SECURITY_AUTHORIZED = "ap_disaster_user_security_authorized";
    public static final String AP_DISASTER_USER_GROUPS = "ap_disaster_user_groups";
    public static final String AP_HUMAN_CAPITAL_LOOKUP_TABLES_AUTHORIZED = "ap_human_capital_lookup_tables_authorized";
    public static final String AP_REFRESHONDEMAND_SKIP_FOLDER_IDS = "ap_refreshondemand_skip_folder_ids";
    public static final String AP_REFRESHONDEMAND_PAGESIZE = "ap_refreshondemand_pagesize";
    public static final String AP_SCHEDULED_INFORMATION_PAGESIZE = "ap_scheduled_information_pagesize";
    public static final String ORACLE_DRIVER = "oracle_driver";
    public static final String EDW_USERNAME = "edw_username";
    public static final String EDW_PASSWORD = "edw_password";
    public static final String EDW_ROW_LEVEL_SECURITY_PACKAGE = "edw_row_level_security_package";
    public static final String EDW_CONNECTION = "edw_connection";
    public static final String ODS_USERNAME = "ods_username";
    public static final String ODS_PASSWORD = "ods_password";
    public static final String ODS_PACKAGE = "ods_package";
    public static final String ODS_CONNECTION = "ods_connection";
    public static final String AP_LOCKED_USER_ACCOUNTS_AUTHORIZED = "ap_locked_user_accounts_authorized";
    public static final String AP_LOCKED_USER_ACCOUNTS_PAGESIZE = "ap_locked_user_accounts_pagesize";
    public static final String LOCK_USER_ACCOUNTS_DAYS = "lock_user_accounts_days";
    public static final String LOCK_USER_ACCOUNTS_EXCEPTIONS = "lock_user_accounts_exceptions";
    public static final String LOCK_USER_ACCOUNTS_EXCEPTION_SUFFIX = "lock_user_accounts_exception_suffix";
    public static final String DATA_DICTIONARY_EXCEPTIONS = "data_dictionary_exceptions";

    // InfoStore Groups Highest Level root hierarchy names
    public static final String TOP_LVL_GRPS = "top_level_groups";
    public static final String TOP_LVL_GRPS_LOCKED_USERS = "top_level_groups_locked_users";

    // Open DOC Server URLS
    public static final String OPEN_DOC_SERVER_URL = "open_doc_url";
    public static final String OPEN_DOC_PORT = "open_doc_port";

    // Using Privacy page
    public static final String PRIVACY_PAGE_USED = "privacy_page_used";

    // ajax form variable
    public static final String PROGRAM_AREA_CD = "pgmAreaCd";
    public static final String AUTH_TYPE_CD = "authTypeCd";

    // authentication
    public static final String AUTH_SCHEME_WINAD = "secWinAD";
    public static final String AUTH_SCHEME_SECENTERPRISE = "secEnterprise";
    public static final String AUTH_TYPE = "auth_type";

    // Program area related property keys
    public static final String IA_PGM_AREA_RELATED_GROUPS = "ia_pgm_area_related_grps";
    public static final String MT_PGM_AREA_RELATED_GROUPS = "mt_pgm_area_related_grps";
    public static final String EDW_PGM_AREA_RELATED_GROUPS = "edw_pgm_area_related_grps";
    public static final String IMCAD_PGM_AREA_RELATED_GROUPS = "imcad_pgm_area_related_grps";
    public static final String FIN_PGM_AREA_RELATED_GROUPS = "fin_pgm_area_related_grps";
    public static final String PA_PGM_AREA_RELATED_GROUPS = "pa_pgm_area_related_grps";
    public static final String HC_PGM_AREA_RELATED_GROUPS = "hc_pgm_area_related_grps";
    public static final String NFIRS_PGM_AREA_RELATED_GROUPS = "nfirs_pgm_area_related_grps";
    public static final String EHP_PGM_AREA_RELATED_GROUPS = "ehp_pgm_area_related_grps";

    // Variables for Report Rescheduler Tool
    public static final String REPORT_RESCHEDULER_FOLDERS = "report_rescheduler_folders";
    public static final String REPORT_RESCHEDULER_USERNAME = "report_rescheduler_username";

    // Variables for Business Objects Account Auditing
    public static final String AUDIT_USER_LOCKED_TEXT = "audit_user_locked_text";
    public static final String AUDIT_USER_UNLOCKED_TEXT = "audit_user_unlocked_text";
    
    // Variables for Active Directory service related LDAP protocols
    public static final String BASE_DN = "baseDN";
    public static final String BO_DN = "boDN";
    public static final String GROUP_TYPE = "groupType";
    public static final String BO_LOOP_TIME = "BoLoopTime";
    public static final String BO_USER = "BoUser";
    public static final String BO_GROUP = "BoGroup";
    public static final String BO_CN_GROUP_USERS_PREFIX = "BoGroupCnUsersPrefix";
    public static final String BO_CN_GROUP_ADMINS_PREFIX = "BoGroupCnAdminsPrefix";
    public static final String BO_CN_GROUP_ABBR_ADMINS_PREFIX = "BoGroupCnAbbrAdminsPrefix";
    public static final String BO_GROUP_USERS_PREFIX = "BoGroupUsersPrefix";
    public static final String BO_GROUP_USERS_ADMIN_PREFIX = "BoGroupUsersAdminPrefix";
    public static final String BO_GROUP_ADMINS_PREFIX = "BoGroupAdminsPrefix";
    public static final String BO_GROUP_ABBR_ADMINS_PREFIX = "BoGroupAbbrAdminsPrefix";
    public static final String BO_DISASTER_NUMBER_ONLY = "BoDisasterNumberOnly";
    public static final String BO_DISASTER_USER_GROUP_ONLY = "BoDisasterUserGroupOnly";
    public static final String BO_DISASTER_ALL_GROUP = "BoDisasterAllGroup";
    public static final String BO_DISASTER_NUMBER_PROGRAM = "BoDisasterNumberProgram";
    public static final String BO_DOMAIN_NAME = "BoDomainName";
    public static final String BO_ACCESS_TYPES = "BoAccessTypes";
    public static final String BO_SEE_ALL_DISASTER_GROUPS = "BoSeeAllDisasterGroups";
    public static final String BO_CN_DISASTER_USERS_ADMIN = "BoDisasterCnUsersAdmin";
    
    
    public static final String FEMA_SMTP_GATEWAY = "fema_smtp_gateway";

    // Variables needed to email Disaster Users
    public static final String DISASTER_USER_FROM = "disaster_user_from";
    public static final String DISASTER_USER_CC = "disaster_user_cc";
    public static final String DISASTER_USER_EXPIRATION_DAY = "disaster_user_expiration_day";
    public static final String DISASTER_USER_EXPIRATION_WARNING_DAYS = "disaster_user_expiration_warning_days";
    public static final String DISASTER_USER_SUBJECT = "disaster_user_subject";
    public static final String DISASTER_USER_BODY_LINE1 = "disaster_user_body_line1";
    public static final String DISASTER_USER_BODY_LINE2 = "disaster_user_body_line2";
    public static final String DISASTER_USER_BODY_LINE3 = "disaster_user_body_line3";
    public static final String DISASTER_USER_SIGNATURE_LINE1 = "disaster_user_signature_line1";
    public static final String DISASTER_USER_SIGNATURE_LINE2 = "disaster_user_signature_line2";
    public static final String DISASTER_USER_SIGNATURE_LINE3 = "disaster_user_signature_line3";

    // Variables needed to email Disaster User Administrators
    public static final String DISASTER_ADMIN_USER_TO = "disaster_admin_user_to";
    public static final String DISASTER_ADMIN_USER_FROM = "disaster_admin_user_from";
    public static final String DISASTER_ADMIN_USER_CC = "disaster_admin_user_cc";
    public static final String DISASTER_ADMIN_USER_DAY = "disaster_admin_user_day";
    public static final String DISASTER_ADMIN_USER_WARNING_DAYS = "disaster_admin_user_warning_days";
    public static final String DISASTER_ADMIN_USER_SUBJECT = "disaster_admin_user_subject";
    public static final String DISASTER_ADMIN_USER_BODY_LINE1 = "disaster_admin_user_body_line1";
    public static final String DISASTER_ADMIN_USER_BODY_LINE2 = "disaster_admin_user_body_line2";
    public static final String DISASTER_ADMIN_USER_BODY_LINE3 = "disaster_admin_user_body_line3";
    public static final String DISASTER_ADMIN_USER_SIGNATURE_LINE1 = "disaster_admin_user_signature_line1";
    public static final String DISASTER_ADMIN_USER_SIGNATURE_LINE2 = "disaster_admin_user_signature_line2";
    public static final String DISASTER_ADMIN_USER_SIGNATURE_LINE3 = "disaster_admin_user_signature_line3";
	
}
