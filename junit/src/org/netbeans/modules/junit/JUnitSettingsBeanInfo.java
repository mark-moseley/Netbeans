/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Enumeration;
import java.awt.Image;
import java.beans.*;

import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author vstejskal
 * @author  Marian Petras
 */
public class JUnitSettingsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor propMembersPublic = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PUBLIC, JUnitSettings.class);
            propMembersPublic.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_public"));
            propMembersPublic.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_public"));

            PropertyDescriptor propMembersProtected = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PROTECTED, JUnitSettings.class);
            propMembersProtected.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_protected"));
            propMembersProtected.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_protected"));

            PropertyDescriptor propMembersPackage = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PACKAGE, JUnitSettings.class);
            propMembersPackage.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_package"));
            propMembersPackage.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_package"));

            PropertyDescriptor propBodyComments = new PropertyDescriptor (JUnitSettings.PROP_BODY_COMMENTS, JUnitSettings.class);
            propBodyComments.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_comments"));
            propBodyComments.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_comments"));

            PropertyDescriptor propBodyContent = new PropertyDescriptor (JUnitSettings.PROP_BODY_CONTENT, JUnitSettings.class);
            propBodyContent.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_content"));
            propBodyContent.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_content"));

            PropertyDescriptor propJavaDoc = new PropertyDescriptor (JUnitSettings.PROP_JAVADOC, JUnitSettings.class);
            propJavaDoc.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_javadoc"));
            propJavaDoc.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_javadoc"));
            
            PropertyDescriptor propCfgConfigEnabled = new PropertyDescriptor (JUnitSettings.PROP_CFGCREATE_ENABLED, JUnitSettings.class);
            propCfgConfigEnabled.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_cfgcreate_enabled"));
            propCfgConfigEnabled.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_cfgcreate_enabled"));            
            
            
            PropertyDescriptor propGenerateExceptionClasses = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_EXCEPTION_CLASSES, JUnitSettings.class);
            propGenerateExceptionClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_exception_classes"));
            propGenerateExceptionClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_exception_classes"));
            
            PropertyDescriptor propGenerateAbstractImpl = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_ABSTRACT_IMPL, JUnitSettings.class);
            propGenerateAbstractImpl.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_abstract_impl"));
            propGenerateAbstractImpl.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_abstract_impl"));

            
            PropertyDescriptor propGenerateSuiteClasses = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_SUITE_CLASSES, JUnitSettings.class);
            propGenerateSuiteClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_suite_classes"));
            propGenerateSuiteClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_suite_classes"));
            
            PropertyDescriptor propIncludePackagePrivateClasses = new PropertyDescriptor (JUnitSettings.PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES, JUnitSettings.class);
            propIncludePackagePrivateClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_include_package_private_classes"));
            propIncludePackagePrivateClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_include_package_private_classes"));
            
            PropertyDescriptor propGenerateSetUp = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_SETUP, JUnitSettings.class);
            propGenerateSetUp.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_setUp"));
            propGenerateSetUp.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_setUp"));
            
            PropertyDescriptor propGenerateTearDown = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_TEARDOWN, JUnitSettings.class);
            propGenerateTearDown.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_tearDown"));
            propGenerateTearDown.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_tearDown"));
            

            
            // expert properties
            PropertyDescriptor propGenerateMainMethod = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_MAIN_METHOD, JUnitSettings.class);
            propGenerateMainMethod.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_main_method"));
            propGenerateMainMethod.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_main_method"));
            propGenerateMainMethod.setExpert(true);
            
            PropertyDescriptor propGenerateMainMethodBody = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_MAIN_METHOD_BODY, JUnitSettings.class);
            propGenerateMainMethodBody.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_main_method_body"));
            propGenerateMainMethodBody.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_main_method_body"));            
            propGenerateMainMethodBody.setExpert(true);

// XXX: is this really required to be customizable?
//            PropertyDescriptor propTestClassNamePrefix = new PropertyDescriptor (JUnitSettings.PROP_TEST_CLASSNAME_PREFIX, JUnitSettings.class);
//            propTestClassNamePrefix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_classname_prefix"));
//            propTestClassNamePrefix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_classname_prefix"));
//            propTestClassNamePrefix.setExpert(true);
//
//            PropertyDescriptor propTestClassNameSuffix = new PropertyDescriptor (JUnitSettings.PROP_TEST_CLASSNAME_SUFFIX, JUnitSettings.class);
//            propTestClassNameSuffix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_classname_suffix"));
//            propTestClassNameSuffix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_classname_suffix"));            
//            propTestClassNameSuffix.setExpert(true);
//            
//            PropertyDescriptor propSuiteClassNamePrefix = new PropertyDescriptor (JUnitSettings.PROP_SUITE_CLASSNAME_PREFIX, JUnitSettings.class);
//            propSuiteClassNamePrefix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_classname_prefix"));
//            propSuiteClassNamePrefix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_classname_prefix"));
//            propSuiteClassNamePrefix.setExpert(true);
//
//            PropertyDescriptor propSuiteClassNameSuffix = new PropertyDescriptor (JUnitSettings.PROP_SUITE_CLASSNAME_SUFFIX, JUnitSettings.class);
//            propSuiteClassNameSuffix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_classname_suffix"));
//            propSuiteClassNameSuffix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_classname_suffix"));            
//            propSuiteClassNameSuffix.setExpert(true);
            
            PropertyDescriptor propRootSuiteClassName = new PropertyDescriptor (JUnitSettings.PROP_ROOT_SUITE_CLASSNAME, JUnitSettings.class);
            propRootSuiteClassName.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_root_suite_classname"));
            propRootSuiteClassName.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_root_suite_classname"));            
            propRootSuiteClassName.setExpert(true);
                        

            return new PropertyDescriptor[] {
              propMembersPublic, propMembersProtected, propMembersPackage, propBodyComments, propBodyContent, 
              propJavaDoc, propCfgConfigEnabled,
              propGenerateExceptionClasses, propGenerateAbstractImpl, propIncludePackagePrivateClasses, 
              propGenerateSuiteClasses,
              propGenerateSetUp, propGenerateTearDown,
              propGenerateMainMethod, propGenerateMainMethodBody, 
              //propTestClassNamePrefix, propTestClassNameSuffix, propSuiteClassNamePrefix, propSuiteClassNameSuffix, 
              propRootSuiteClassName 
            };
        }
        catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("/org/netbeans/modules/junit/resources/JUnitSettingsIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/junit/resources/JUnitSettingsIcon32.gif");
            return icon32;
        }
    }

    public static class SortedListPropEd extends PropertyEditorSupport {
        private LinkedList  displays = new LinkedList();
        private LinkedList  values = new LinkedList();
        private String      defaultDisplay = NbBundle.getMessage(JUnitSettingsBeanInfo.class, "LBL_value_not_found");
        private String      defaultValue = "";

        public String[] getTags () {
            TreeSet t = new TreeSet(displays);
            if (displays.size() > 0) {
                return (String []) t.toArray(new String[displays.size() - 1]);
            } else {
                return new String[0];
            }
        }

        public String getAsText () {
            String      value = null;
            String      display = null;
            Iterator    iD = displays.iterator();
            Iterator    iV = values.iterator();
            while (iV.hasNext()) {
                value = (String) iV.next();
                display = (String) iD.next();
                if (value.equals(getValue()))
                    return display;
            }
            return defaultDisplay;
        }
        
        public void setAsText (String text) throws IllegalArgumentException {
            String      value = null;
            String      display = null;
            Iterator    iD = displays.iterator();
            Iterator    iV = values.iterator();
            while (iD.hasNext()) {
                value = (String) iV.next();
                display = (String) iD.next();
                if (display.equals(text)) {
                    setValue(value);
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }

        protected void put(String display, String value, int type) {
            if (SHOW_IN_LIST == (type & SHOW_IN_LIST)) {
                displays.add(display);
                values.add(value);
            }
            if (IS_DEFAULT == (type & IS_DEFAULT)) {
                defaultDisplay = display;
                defaultValue = value;
            }
        }
        
        protected static int SHOW_IN_LIST   = 1;
        protected static int IS_DEFAULT     = 2;
    }

}
