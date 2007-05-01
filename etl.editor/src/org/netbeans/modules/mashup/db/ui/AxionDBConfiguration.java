/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mashup.db.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.nodes.BeanNode;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class AxionDBConfiguration {
    
    private static final AxionDBConfiguration DEFAULT = new AxionDBConfiguration();
    
    public static final String PROP_DB_LOC = "DBLocation";
    
    public static final String PROP_DRIVER_LOC = "DriverLocation";

    public static final String PROP_LOC = "location";
    
    public static final String PROP_DRV_LOC = "driver";
    
    public static AxionDBConfiguration getDefault() {
        return DEFAULT;
    }
    
    public String displayName() {
        return NbBundle.getMessage(AxionDBConfiguration.class, "LBL_AxionConf");
    }
    
    protected final String putProperty(String key, String value, boolean notify) {
        System.setProperty(key, value);
        return System.getProperty(key);
    }
    
    protected final String getProperty(String key) {
        return System.getProperty(key);
    }
    
    public String getDriver() {
        File conf = getConfigFile();
        if(conf != null) {
            Properties prop = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(conf);
                prop.load(in);
            } catch (FileNotFoundException ex) {
                //ignore
            } catch(IOException ioEx) {
                //ignore
            }
            String drv = prop.getProperty(PROP_DRIVER_LOC);
            if(drv != null)
                return drv;
        }
        return "";
    }
    
    public void setDriver(String driver) {
        driver = driver.trim();
        File conf = getConfigFile();
        try {
            FileInputStream in = new FileInputStream(conf);
            Properties oldProp = new Properties();
            oldProp.load(in);
            String dbLoc = oldProp.getProperty(PROP_DB_LOC);
            in.close();
            FileOutputStream out = new FileOutputStream(conf);
            Properties prop = new Properties();
            prop.setProperty(PROP_DRIVER_LOC, driver);
            prop.setProperty(PROP_DB_LOC, dbLoc);
            prop.store(out, "MashupDB Configurations");
            out.close();
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }        
    }
    
    /**
     * Returns the AXION location or an empty string if the AXION location
     * is not set. Never returns null.
     */
    public String getLocation() {
        File conf = getConfigFile();
        if(conf != null) {
            Properties prop = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(conf);
                prop.load(in);
            } catch (FileNotFoundException ex) {
                //ignore
            } catch(IOException ioEx) {
                //ignore
            }
            return prop.getProperty(PROP_DB_LOC);
        }
        return "";
    }
    
    /**
     * Sets the AXION location.
     *
     * @param location the AXION location. A null value is valid and
     *        will be returned by getLocation() as an empty
     *        string (meaning "not set"). An empty string is valid
     *        and has the meaning "set to the default location".
     */
    public void setLocation(String location) {
        location = location.trim();
        if(!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        File conf = getConfigFile();
        try {
            FileInputStream in = new FileInputStream(conf);
            Properties oldProp = new Properties();
            oldProp.load(in);            
            String drv = oldProp.getProperty(PROP_DRIVER_LOC);
            in.close();
            FileOutputStream out = new FileOutputStream(conf);
            Properties prop = new Properties();
            prop.setProperty(PROP_DB_LOC, location);
            prop.setProperty(PROP_DRIVER_LOC, drv);
            prop.store(out, "MashupDB Configurations");
            out.close();
            File db = new File(location);
            if(!db.exists()) {
                db.mkdir();
            }
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
    }
    
    
    public static File getConfigFile() {
        String nbUsrDir = System.getProperty("netbeans.user");
        String DEFAULT_DB_LOCATION = System.getProperty("netbeans.user");
        nbUsrDir = nbUsrDir + File.separator + "config" + File.separator + 
                "Databases" + File.separator + "MashupDB";
        File conf = new File(nbUsrDir);
        if(!conf.exists()) {
            conf.mkdir();
        }
        nbUsrDir = nbUsrDir + File.separator +"MashupDBConfig.properties";
        conf = new File(nbUsrDir);
        if(!conf.exists()) {
            try {
                conf.createNewFile();
                Properties prop = new Properties();
                prop.setProperty(PROP_DB_LOC, DEFAULT_DB_LOCATION);
                prop.setProperty(PROP_DRIVER_LOC, "");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(conf);
                    prop.store(out, "Mashup Database Location");
                    out.close();
                } catch (FileNotFoundException ex) {
                    //ignore
                } catch(IOException ioEx) {
                    //igonre
                }
            } catch (IOException ex) {
                conf = null;
            }
        }
        return conf;
    }    
    
    
    protected static BeanNode createViewNode() throws java.beans.IntrospectionException {
        BeanNode nd = new BeanNode(AxionDBConfiguration.getDefault());
        nd.setName("Mashup Database");
        return nd;
    }
}