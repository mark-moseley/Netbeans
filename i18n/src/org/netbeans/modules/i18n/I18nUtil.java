/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.JPanel;

import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.TopManager;
 // PENDING

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/**
 * Utilities class for I18N module.
 *
 * @author  Peter Zavadsky
 */
public final class I18nUtil {

    /** Help ID for i18n module in general. */
    public static final String HELP_ID_I18N = "internation.internation"; // NOI18N
    /** Help ID for I18N dialog. */
    public static final String HELP_ID_AUTOINSERT = "internation.autoinsert"; // NOI18N
    /** Help ID for Insert I18N dialog. */
    public static final String HELP_ID_MANINSERT = "internation.maninsert"; // NOI18N
    /** Help ID for I18N form property editor. You can see it in Component inspector. */
    public static final String HELP_ID_FORMED = "internation.formed"; // NOI18N
    /** Help ID for I18N test wizard. */
    public static final String HELP_ID_TESTING = "internation.testing"; // NOI18N
    /** Help ID for I18N wizard. */
    public static final String HELP_ID_WIZARD = "internation.wizard"; // NOI18N
    /** Help ID for I18N options. */
    public static final String HELP_ID_CUSTOM = "internation.custom"; // NOI18N
    /** Help ID for parameters dialog. */
    public static final String HELP_ID_ADDPARAMS = "internation.addparams"; // NOI18N
    /** Help ID for replacing format. */
    public static final String HELP_ID_REPLFORMAT = "internation.replformat"; // NOI18N
    /** Help ID for Locale execution. */
    public static final String HELP_ID_RUNLOCALE = "internation.runlocale"; // NOI18N
    
    /** Help ID for property editor */
    public static final String PE_REPLACE_CODE_HELP_ID = "i18n.pe.replacestring";
    /** Help ID for property editor */
    public static final String PE_I18N_REGEXP_HELP_ID = "i18n.pe.i18nregexp";
    /** Help ID for property editor */
    public static final String PE_BUNDLE_CODE_HELP_ID = "i18n.pe.bundlestring";
    /** Help ID for property editor */
    public static final String PE_TEST_REGEXP_HELP_ID = "i18n.pe.testregexp";
    
    /** Items for init format customizer. */
    private static List initFormatItems;

    /** Help description for init format customizer. */
    private static List initHelpItems;

    /** Items for replace format customizer. */
    private static List replaceFormatItems;

    /** Help description for replace format customizer. */
    private static List replaceHelpItems;

    /** Items for regular expression customizer. */
    private static List regExpItems;

    /** Help description for regular expression customizer. */
    private static List regExpHelpItems;
    
    /** Items for i18n regular expression customizer. */
    private static List i18nRegExpItems;

    /** Resource bundle used in i18n module. */
    private static ResourceBundle bundle;
    
    
    /** Gets <code>initFormatItems</code>. */
    public static List getInitFormatItems() { 
        if(initFormatItems == null) {
            initFormatItems = new ArrayList(2);
            initFormatItems.add("java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")"); // NOI18N
            initFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class)"); // NOI18N
        }
              
        return initFormatItems;
    }

    /** Gets <code>InitHelpFormats</code>. */
    public static List getInitHelpItems() {
        if(initHelpItems == null) {
            initHelpItems = new ArrayList(3);
            initHelpItems.add("{bundleNameSlashes} - "+ getBundle().getString("TXT_PackageNameSlashes")); // NOI18N
            initHelpItems.add("{bundleNameDots} - " + getBundle().getString("TXT_PackageNameDots")); // NOI18N
            initHelpItems.add("{sourceFileName} - " + getBundle().getString("TXT_SourceDataObjectName")); // NOI18N
        }
         
        return initHelpItems;
    }

    /** Gets <code>replaceFormatItems</code>. */
    public static List getReplaceFormatItems() {
        if(replaceFormatItems == null) {
            replaceFormatItems = new ArrayList(7);
            replaceFormatItems.add("{identifier}.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("Utilities.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class).getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("java.text.MessageFormat(java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\"), {arguments})"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\")"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\", {arguments})"); // NOI18N
        }
            
        return replaceFormatItems;
    }

    /** Gets <code>replaceHeplItems</code>.*/
    public static List getReplaceHelpItems() {
        if(replaceHelpItems == null) {
            replaceHelpItems = new ArrayList(5);
            replaceHelpItems.add("{identifier} - " + getBundle().getString("TXT_FieldIdentifier")); // NOI18N
            replaceHelpItems.add("{key} - " + getBundle().getString("TXT_KeyHelp")); // NOI18N
            replaceHelpItems.add("{bundleNameSlashes} - " + getBundle().getString("TXT_PackageNameSlashes")); // NOI18N
            replaceHelpItems.add("{bundleNameDots} - " + getBundle().getString("TXT_PackageNameDots")); // NOI18N
            replaceHelpItems.add("{sourceFileName} - " + getBundle().getString("TXT_SourceDataObjectName")); // NOI18N
            replaceHelpItems.add("{arguments} - " + getBundle().getString("TXT_Arguments")); // NOI18N
        }
            
        return replaceHelpItems;
    }

    /** Gets <code>regExpItems</code>. */
    public static List getRegExpItems() {
        if(regExpItems == null) {
            regExpItems = new ArrayList(4);
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}"); // NOI18N
            regExpItems.add("// NOI18N"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}|// NOI18N"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*{hardString}|// NOI18N"); // NOI18N
        }
            
        return regExpItems;
    }
    
    /** Gets <code>i18nRegExpItems</code>. */
    public static List getI18nRegExpItems() {
        if(i18nRegExpItems == null) {
            i18nRegExpItems = new ArrayList(2);
            i18nRegExpItems.add("getString[:space:]*\\([:space:]*{hardString}"); // NOI18N
            i18nRegExpItems.add("(getString[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*){hardString}"); // NOI18N
        }
            
        return i18nRegExpItems;
    }
    
    /** Gets <code>regExpHelpItems</code>. */
    public static List getRegExpHelpItems() {
        if(regExpHelpItems == null) {
            regExpHelpItems = new ArrayList(15);
            regExpHelpItems.add("{hardString} - " + getBundle().getString("TXT_HardString")); // NOI18N
            regExpHelpItems.add("[:alnum:] - " + getBundle().getString("TXT_Alnum")); // NOI18N
            regExpHelpItems.add("[:alpha:] - " + getBundle().getString("TXT_Alpha")); // NOI18N
            regExpHelpItems.add("[:blank:] - " + getBundle().getString("TXT_Blank")); // NOI18N
            regExpHelpItems.add("[:cntrl:] - " + getBundle().getString("TXT_Cntrl")); // NOI18N
            regExpHelpItems.add("[:digit:] - " + getBundle().getString("TXT_Digit")); // NOI18N
            regExpHelpItems.add("[:graph:] - " + getBundle().getString("TXT_Graph")); // NOI18N
            regExpHelpItems.add("[:lower:] - " + getBundle().getString("TXT_Lower")); // NOI18N
            regExpHelpItems.add("[:print:] - " + getBundle().getString("TXT_Print")); // NOI18N
            regExpHelpItems.add("[:punct:] - " + getBundle().getString("TXT_Punct")); // NOI18N
            regExpHelpItems.add("[:space:] - " + getBundle().getString("TXT_Space")); // NOI18N
            regExpHelpItems.add("[:upper:] - " + getBundle().getString("TXT_Upper")); // NOI18N
            regExpHelpItems.add("[:xdigit:] - " + getBundle().getString("TXT_Xdigit")); // NOI18N
            regExpHelpItems.add("[:javastart:] - " + getBundle().getString("TXT_Javastart")); // NOI18N
            regExpHelpItems.add("[:javapart:] - " + getBundle().getString("TXT_Javapart")); // NOI18N
        }
        
        return regExpHelpItems;
    }

    /** 
     * Indicates if folder or its subfolders contains data object
     * that is supported by any internationalization factory. 
     */
    public static boolean containsAcceptedDataObject(DataFolder folder) {
        DataObject[] children = folder.getChildren();

        for(int i = 0; i < children.length; i++) {
            if(children[i] instanceof DataFolder) {  
                if(containsAcceptedDataObject((DataFolder)children[i]))
                    return true;
            } else {
                if(FactoryRegistry.hasFactory(children[i].getClass()))
                    return true;
            }
        }

        return false;
    }
    
    /** 
     * Recursivelly get all accepted data objects starting from given folder. 
     */
    public static List getAcceptedDataObjects(DataFolder folder) {
        List accepted = new ArrayList();
        
        DataObject[] children = folder.getChildren();

        for(int i = 0; i < children.length; i++) {
            if(children[i] instanceof DataFolder) {  
                accepted.addAll(getAcceptedDataObjects((DataFolder)children[i]));
            } else {
                if(FactoryRegistry.hasFactory(children[i].getClass()))
                    accepted.add(children[i]);
            }
        }

        return accepted;
    }
    
    /** Gets resource bundle for i18n module. */
    public static ResourceBundle getBundle() {
        if(bundle == null)
            bundle = NbBundle.getBundle(I18nModule.class);
        
        return bundle;
    }
    
    /** Gets i18n options. */
    public static I18nOptions getOptions() {
        return (I18nOptions)SharedClassObject.findObject(I18nOptions.class, true);
    }
    
    /** Indicates debug mode. */
    public static boolean isDebug() {
        return Boolean.getBoolean("netbeans.debug.exceptions"); // NOI18N
    }

    

    /**
     * Create new topcomponent associated with I18N_MODE.
     */
    public static TopComponent createTopComponent(JPanel interior, String name, String title, URL icon) {
        TopComponent topComponent;
        
        // Actually create dialog, as non serializable top component.
        topComponent = new TopComponent() {
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            }

            public void writeExternal(ObjectOutput out) throws IOException {
            }

            protected Object writeReplace() throws ObjectStreamException {
                return null;
            }
        };
        topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
        topComponent.setLayout(new BorderLayout());
        topComponent.add(interior, BorderLayout.CENTER);
        topComponent.setName(name);
        topComponent.setToolTipText(title);

        // #24106
        topComponent.putClientProperty("TabPolicy", "HideWhenAlone");           // NOI18N

         // dock into I18N mode if possible
        Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
        for (int i = currentWs.length; --i >= 0; ) {
            Mode i18nMode = currentWs[i].findMode(I18nManager.I18N_MODE);
            if (i18nMode == null) {
                i18nMode = currentWs[i].createMode(
                    I18nManager.I18N_MODE,
                    title,
                    icon
                );
                // adjust mode size to sice of the first TopComponent(i18nPanel)
                Rectangle bounds = i18nMode.getBounds();
                Dimension size = interior.getPreferredSize();
                size.width += 50;
                bounds.setSize(size);
                i18nMode.setBounds(bounds);
            }

            i18nMode.dockInto(topComponent);
        }
        return topComponent;
    }
}
