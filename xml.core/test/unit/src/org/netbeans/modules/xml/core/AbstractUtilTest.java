package org.netbeans.modules.xml.core;

import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Collection;
import java.util.TreeMap;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import junit.framework.*;

public class AbstractUtilTest extends TestCase {
    
    public AbstractUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testGetCallerPackage() {
        System.out.println("testGetCallerPackage");

        try {
            String pack = getClass().getPackage().getName();

            assertTrue("Class package detection failed! " + testPackage(), testPackage().equals(pack));
            assertTrue("Inner class package detection failed! " + Inner.testPackage(), Inner.testPackage().equals(pack));
        } catch (Exception ex) {
            ex.printStackTrace(new PrintWriter(System.out));
        }
    }
    
    private String testPackage() {
        return AbstractUtilImpl.getCallerPackage();
    }
    
    private class AbstractUtilImpl extends AbstractUtil {
        
    }
    
    private static class Inner {
        static String testPackage() {
            return AbstractUtilImpl.getCallerPackage();
        }
    }
}
