package org.netbeans.modules.web.jsf.xdm.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.util.Exceptions;

public class SyncUpdateTest extends NbTestCase {
    
    public SyncUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testSyncRuleElement() throws Exception {
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        
        NavigationRule rule = model.getRootComponent().getNavigationRules().get(0);
        
        assertEquals("afaa", rule.getFromViewId());
        
        Util.setDocumentContentTo(model, "faces-config-04.xml");
        
        assertEquals("newafaa", rule.getFromViewId());
    }
    
    public boolean propertyChangeCalled = false;
    public void testSyncNotWellFormedElement() throws Exception {
        propertyChangeCalled = false;
        List<NavigationRule> navRules;
        
        JSFConfigModel model = Util.loadRegistryModel("faces-config-05.xml");
        
        navRules = model.getRootComponent().getNavigationRules();
        
        assertEquals("index.jsp", navRules.get(0).getFromViewId());
        
        Document document;
        //An Excpetion should be thrown.
        try {
            Util.setDocumentContentTo(model,"faces-config-notwellformed.xml");
        } catch (IOException ioe ){
            assertEquals("java.io.IOException: Invalid token '<' found in document: Please use the text editor to resolve the issues...", ioe.toString());
            //            System.out.println(ioe);
        }
        
        //An Exception Should be thrown
        try {
            navRules = model.getRootComponent().getNavigationRules();
        } catch ( IllegalStateException ise ) {
            assertEquals("java.lang.IllegalStateException: The model is not initialized or is broken.", ise.toString());
        }
        
        
        model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
            }
        });
        Util.setDocumentContentTo(model,"faces-config-wellformed.xml");
        
        assertTrue(propertyChangeCalled);
    }
    
    public void testSyncRemoveRule() throws Exception {
        
        propertyChangeCalled = false;
        
        /* Load a file that has a simple rule*/
        JSFConfigModel model = Util.loadRegistryModel("faces-config-03.xml");
        
         model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
            }
        });       

        try {
            Util.setDocumentContentTo(model,"faces-config-empty.xml");
        } catch (IOException ioe ){
            ioe.printStackTrace();
            assertTrue(false);
        }
        
        assertTrue(propertyChangeCalled);
    }
    
    public void testSyncRemoveCase() throws Exception {
        
        propertyChangeCalled = false;
        
        /* Load a file that has a simple rule*/
        JSFConfigModel model = Util.loadRegistryModel("faces-config-99325.xml");
        
         model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
                System.out.println("event prisla : " + arg0);
            }
        });       

        try {
            Util.setDocumentContentTo(model,"faces-config-99325-end.xml");
        } catch (IOException ioe ){
            ioe.printStackTrace();
            assertTrue(false);
        }
        
        assertTrue(propertyChangeCalled);
    }
    
    public void testSynceRenamePageInMode_l100321() throws Exception {
        
        propertyChangeCalled = false;
        
        /* Load a file that has a simple rule*/
        JSFConfigModel configModel = Util.loadRegistryModel("faces-config-100321.xml");
        
         configModel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                propertyChangeCalled = true;
                System.out.println("event prisla : " + arg0);
            }
        });       
        String oldDisplayName = "OLDFILENMAME.jsp";
        String newDisplayName = "NEWFILENAME.jsp";
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            if ( navRule.getFromViewId().equals(oldDisplayName) ){
                navRule.setFromViewId(newDisplayName);
            }
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                if ( navCase.getToViewId().equals(oldDisplayName) ) {
                    navCase.setToViewId(newDisplayName);
                }
            }
        }
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }        
        assertTrue(propertyChangeCalled);
    }
    
}
