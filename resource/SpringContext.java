/**
 * This utility class bootstraps the Admin Portal application with the Spring database
 * framework specific configurable parameters.
 */
package gov.fema.adminportal.util;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import gov.fema.adminportal.jdbc.dao.*;
import org.apache.log4j.Logger;

public class SpringContext {
    private static Logger logger = Logger.getLogger(SpringContext.class
            .getName());
    private static ClassPathXmlApplicationContext appContext = null;

    static {
        loadSpringContext();
    }

    protected static void loadSpringContext() {
        // InputStream stream = null;
        try {
            logger.debug("Loading Application context");
            // stream =
            // Thread.currentThread().getContextClassLoader().getResourceAsStream("spring-config.xml");
            appContext = new ClassPathXmlApplicationContext("spring-config.xml");
            // Resource resource = new InputStreamResource(stream);
            // GenericXmlApplicationContext appContext = new
            // GenericXmlApplicationContext();
            // appContext.load(resource);
            logger.debug("Loaded Application context");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error loading spring config file please check classpath");
        }
    }

    public static ClassPathXmlApplicationContext getContext() {
        if (appContext == null) {
            // logger.debug("appContext was null, so populating it");
            loadSpringContext();
        } else {
            logger.debug("App context is not null");
        }
        return appContext;
    }

    public static void main(String[] args) {
        // try fetching the ClassPath context
        appContext = getContext();
        HCLookupDao ds = (HCLookupDao) appContext.getBean("hcLookupDao");
        System.out.println("HCLookupDao: " + ds);
        DisasterSecurityDao ds1 = (DisasterSecurityDao) appContext
                .getBean("disasterSecurityDao");
        System.out.println("DisasterSecurityDao: " + ds1);
    }
}
