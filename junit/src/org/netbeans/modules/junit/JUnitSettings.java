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
/*
 * CreateTestAction.java
 *
 * Created on January 29, 2001, 7:08 PM
 */

package org.netbeans.modules.junit;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;




/** Options for JUnit module, control behavior of test creation and execution.
 *
 * @author  vstejskal
 * @version 1.0
 */
public class JUnitSettings extends SystemOption {

    // static final long serialVersionUID = ...;
    static final long serialVersionUID = 372745543035969452L;

    // XXX this property has to go too - will not work any longer, need some src -> test query
    private static final String PROP_FILE_SYSTEM         = "fileSystem";
    public static final String PROP_SUITE_TEMPLATE      = "suiteTemplate";
    public static final String PROP_CLASS_TEMPLATE      = "classTemplate";
    public static final String PROP_MEMBERS_PUBLIC      = "membersPublic";
    public static final String PROP_MEMBERS_PROTECTED   = "membersProtected";
    public static final String PROP_MEMBERS_PACKAGE     = "membersPackage";
    public static final String PROP_BODY_COMMENTS       = "bodyComments";
    public static final String PROP_BODY_CONTENT        = "bodyContent";
    public static final String PROP_JAVADOC             = "javaDoc";
    public static final String PROP_CFGCREATE_ENABLED   = "cfgCreateEnabled";
    public static final String PROP_GENERATE_EXCEPTION_CLASSES = "generateExceptionClasses";
    public static final String PROP_GENERATE_ABSTRACT_IMPL = "generateAbstractImpl";
    public static final String PROP_GENERATE_SUITE_CLASSES   = "generateSuiteClasses";
    
    public static final String PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES = "includePackagePrivateClasses";
    public static final String PROP_GENERATE_TESTS_FROM_TEST_CLASSES = "generateTestsFromTestClasses";    
    public static final String PROP_GENERATE_MAIN_METHOD = "generateMainMethod";
    public static final String PROP_GENERATE_MAIN_METHOD_BODY = "generateMainMethodBody";
    public static final String PROP_TEST_CLASSNAME_PREFIX = "testClassNamePrefix";
    public static final String PROP_TEST_CLASSNAME_SUFFIX = "testClassNameSuffix";
    public static final String PROP_SUITE_CLASSNAME_PREFIX = "suiteClassNamePrefix";
    public static final String PROP_SUITE_CLASSNAME_SUFFIX = "suiteClassNameSuffix";
    public static final String PROP_ROOT_SUITE_CLASSNAME = "rootSuiteClassName";
    
    
    
    public static final String PROP_VERSION = "version";    
    
    public static final Integer CURRENT_VERSION = new Integer(30);
    
    // No constructor please!

    protected void initialize () {
        // If you have more complex default values which might require
        // other parts of the module to already be installed, do not
        // put them here; e.g. make the getter return them as a
        // default if getProperty returns null. (The class might be
        // initialized partway through module installation.)
        
        super.initialize();
        
        putProperty(PROP_VERSION, CURRENT_VERSION, true);
        putProperty(PROP_FILE_SYSTEM, "", true);
        putProperty(PROP_SUITE_TEMPLATE, "Templates/JUnit/SimpleJUnitTest.java", true);
        putProperty(PROP_CLASS_TEMPLATE, "Templates/JUnit/SimpleJUnitTest.java", true);
        putProperty(PROP_MEMBERS_PUBLIC, Boolean.TRUE, true);
        putProperty(PROP_MEMBERS_PROTECTED, Boolean.TRUE, true);
        putProperty(PROP_MEMBERS_PACKAGE, Boolean.TRUE, true);
        putProperty(PROP_BODY_COMMENTS, Boolean.TRUE, true);
        putProperty(PROP_BODY_CONTENT, Boolean.TRUE, true);
        putProperty(PROP_JAVADOC, Boolean.TRUE, true);
        putProperty(PROP_CFGCREATE_ENABLED, Boolean.TRUE, true);
        putProperty(PROP_GENERATE_ABSTRACT_IMPL, Boolean.TRUE, true);
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, Boolean.FALSE, true);
        putProperty(PROP_GENERATE_SUITE_CLASSES, Boolean.TRUE, true);
        putProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES, Boolean.FALSE, true);
        putProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES, Boolean.FALSE, false);        
        putProperty(PROP_GENERATE_MAIN_METHOD, Boolean.FALSE, true);
        putProperty(PROP_GENERATE_MAIN_METHOD_BODY, NbBundle.getMessage(JUnitSettings.class, "PROP_generate_main_method_body_default_value"), true);
        putProperty(PROP_TEST_CLASSNAME_PREFIX, NbBundle.getMessage(JUnitSettings.class, "PROP_test_classname_prefix_default_value"), true);
        putProperty(PROP_TEST_CLASSNAME_SUFFIX, NbBundle.getMessage(JUnitSettings.class, "PROP_test_classname_suffix_default_value"), true);
        putProperty(PROP_SUITE_CLASSNAME_PREFIX, NbBundle.getMessage(JUnitSettings.class, "PROP_suite_classname_prefix_default_value"), true);
        putProperty(PROP_SUITE_CLASSNAME_SUFFIX, NbBundle.getMessage(JUnitSettings.class, "PROP_suite_classname_suffix_default_value"), true);        
        putProperty(PROP_ROOT_SUITE_CLASSNAME, NbBundle.getMessage(JUnitSettings.class, "PROP_root_suite_classname_default_value"), true);        
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeObject(getProperty(PROP_VERSION));
        out.writeObject(getProperty(PROP_FILE_SYSTEM));
        out.writeObject(getProperty(PROP_SUITE_TEMPLATE));
        out.writeObject(getProperty(PROP_CLASS_TEMPLATE));
        out.writeObject(getProperty(PROP_MEMBERS_PUBLIC));
        out.writeObject(getProperty(PROP_MEMBERS_PROTECTED));
        out.writeObject(getProperty(PROP_MEMBERS_PACKAGE));
        out.writeObject(getProperty(PROP_BODY_COMMENTS));
        out.writeObject(getProperty(PROP_BODY_CONTENT));
        out.writeObject(getProperty(PROP_JAVADOC));
        out.writeObject(getProperty(PROP_CFGCREATE_ENABLED));
        out.writeObject(getProperty(PROP_GENERATE_ABSTRACT_IMPL));
        out.writeObject(getProperty(PROP_GENERATE_EXCEPTION_CLASSES));
        out.writeObject(getProperty(PROP_GENERATE_SUITE_CLASSES));
        out.writeObject(getProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES));
        out.writeObject(getProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES));        
        out.writeObject(getProperty(PROP_GENERATE_MAIN_METHOD));
        out.writeObject(getProperty(PROP_GENERATE_MAIN_METHOD_BODY));
        out.writeObject(getProperty(PROP_TEST_CLASSNAME_PREFIX));
        out.writeObject(getProperty(PROP_TEST_CLASSNAME_SUFFIX));
        out.writeObject(getProperty(PROP_SUITE_CLASSNAME_PREFIX));
        out.writeObject(getProperty(PROP_SUITE_CLASSNAME_SUFFIX));
        out.writeObject(getProperty(PROP_ROOT_SUITE_CLASSNAME));
    }
    
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        Object firstProperty = in.readObject();
        if (firstProperty instanceof String) {
            // here goes old (version pre 2.9 settings file);
            readPre29VersionOptions(in, firstProperty);
        } else if (firstProperty instanceof Integer) {
            int version = ((Integer)firstProperty).intValue();
            readVersionedOptions(in, version);
        } else {
            // something went wrong
            //System.err.println("Unkonwn options?");
            // Notification should be added
        }
    }
    
    
    private void readVersionedOptions(ObjectInput in, int version) throws IOException, ClassNotFoundException {
        switch (version) {
            case 30:
                readVersion30Options(in);
                break;
            case 29:
                readVersion29Options(in);
                break;
            default:
                // weird stuff
                // System.err.println("Unkonwn options? - version"+version);
                // Notification should be added
        }
    }
    
    private void readVersion30Options(ObjectInput in) throws IOException, ClassNotFoundException {
        putProperty(PROP_FILE_SYSTEM, in.readObject(), true);
        putProperty(PROP_SUITE_TEMPLATE, in.readObject(), true);
        putProperty(PROP_CLASS_TEMPLATE, in.readObject(), true);
        putProperty(PROP_MEMBERS_PUBLIC, in.readObject(), true);
        putProperty(PROP_MEMBERS_PROTECTED, in.readObject(), true);
        putProperty(PROP_MEMBERS_PACKAGE, in.readObject(), true);
        putProperty(PROP_BODY_COMMENTS, in.readObject(), true);
        putProperty(PROP_BODY_CONTENT, in.readObject(), true);
        putProperty(PROP_JAVADOC, in.readObject(), true);
        putProperty(PROP_CFGCREATE_ENABLED, in.readObject(), true);
        putProperty(PROP_GENERATE_ABSTRACT_IMPL, in.readObject(), true);
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, in.readObject(), true);
        putProperty(PROP_GENERATE_SUITE_CLASSES,in.readObject(), true);
        putProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,in.readObject(), true);
        putProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES,in.readObject(), true);
        putProperty(PROP_GENERATE_MAIN_METHOD,in.readObject(), true);
        putProperty(PROP_GENERATE_MAIN_METHOD_BODY,in.readObject(), true);
        putProperty(PROP_TEST_CLASSNAME_PREFIX,in.readObject(), true);
        putProperty(PROP_TEST_CLASSNAME_SUFFIX,in.readObject(), true);
        putProperty(PROP_SUITE_CLASSNAME_PREFIX,in.readObject(), true);
        putProperty(PROP_SUITE_CLASSNAME_SUFFIX,in.readObject(), true);
        putProperty(PROP_ROOT_SUITE_CLASSNAME,in.readObject(), true);
    }
    
    private void readVersion29Options(ObjectInput in) throws IOException, ClassNotFoundException {
        putProperty(PROP_FILE_SYSTEM, in.readObject(), true);
        putProperty(PROP_SUITE_TEMPLATE, in.readObject(), true);
        putProperty(PROP_CLASS_TEMPLATE, in.readObject(), true);
        putProperty(PROP_MEMBERS_PUBLIC, in.readObject(), true);
        putProperty(PROP_MEMBERS_PROTECTED, in.readObject(), true);
        putProperty(PROP_MEMBERS_PACKAGE, in.readObject(), true);
        putProperty(PROP_BODY_COMMENTS, in.readObject(), true);
        putProperty(PROP_BODY_CONTENT, in.readObject(), true);
        putProperty(PROP_JAVADOC, in.readObject(), true);
        putProperty(PROP_CFGCREATE_ENABLED, in.readObject(), true);
        in.readObject(); // was PROP_CFGEXEC_ENABLED
        in.readObject(); // was PROP_EXECUTOR_TYPE
        putProperty(PROP_GENERATE_ABSTRACT_IMPL, in.readObject(), true);
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, in.readObject(), true);
        in.readObject(); // was PROP_TEST_RUNNER
        in.readObject(); // was PROP_PROPERTIES
        putProperty(PROP_GENERATE_SUITE_CLASSES,in.readObject(), true);
        putProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,in.readObject(), true);
        putProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES,in.readObject(), true);
        putProperty(PROP_GENERATE_MAIN_METHOD,in.readObject(), true);
        putProperty(PROP_GENERATE_MAIN_METHOD_BODY,in.readObject(), true);
        putProperty(PROP_TEST_CLASSNAME_PREFIX,in.readObject(), true);
        putProperty(PROP_TEST_CLASSNAME_SUFFIX,in.readObject(), true);
        putProperty(PROP_SUITE_CLASSNAME_PREFIX,in.readObject(), true);
        putProperty(PROP_SUITE_CLASSNAME_SUFFIX,in.readObject(), true);
        putProperty(PROP_ROOT_SUITE_CLASSNAME,in.readObject(), true);
    }
    
    private void readPre29VersionOptions(ObjectInput in, Object firstProperty) throws IOException, ClassNotFoundException {
        try {            
            putProperty(PROP_FILE_SYSTEM, firstProperty, true);
            putProperty(PROP_SUITE_TEMPLATE, in.readObject(), true);
            putProperty(PROP_CLASS_TEMPLATE, in.readObject(), true);
            putProperty(PROP_MEMBERS_PUBLIC, in.readObject(), true);
            putProperty(PROP_MEMBERS_PROTECTED, in.readObject(), true);
            putProperty(PROP_MEMBERS_PACKAGE, in.readObject(), true);
            putProperty(PROP_BODY_COMMENTS, in.readObject(), true);
            putProperty(PROP_BODY_CONTENT, in.readObject(), true);
            putProperty(PROP_JAVADOC, in.readObject(), true);
            putProperty(PROP_CFGCREATE_ENABLED, in.readObject(), true);
            in.readObject(); // was PROP_CFGEXEC_ENABLED
            in.readObject(); // was PROP_EXECUTOR_TYPE
            putProperty(PROP_GENERATE_ABSTRACT_IMPL, in.readObject(), true);
            putProperty(PROP_GENERATE_EXCEPTION_CLASSES, in.readObject(), true);
            in.readObject(); // was PROP_TEST_RUNNER
            in.readObject(); // was PROP_PROPERTIES
            // dummy read object (Generate NBJUnit poperty) 
            in.readObject();
            // dummy end
            putProperty(PROP_GENERATE_SUITE_CLASSES,in.readObject(), true);
        } catch (OptionalDataException ode) {
            // deserialization failed - just swallow it
            // probably a very old version of JUNit (pre 2.5)
        }
    }

    public String displayName () {
        return NbBundle.getMessage (JUnitSettings.class, "LBL_junit_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(JUnitSettings.class); 
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    public static JUnitSettings getDefault () {
        return (JUnitSettings) findObject (JUnitSettings.class, true);
    }

    public String getSuiteTemplate() {
        return (String) getProperty(PROP_SUITE_TEMPLATE);
    }

    public void setSuiteTemplate(String newVal) {
        putProperty(PROP_SUITE_TEMPLATE, newVal, true);
    }

    public String getClassTemplate() {
        return (String) getProperty(PROP_CLASS_TEMPLATE);
    }

    public void setClassTemplate(String newVal) {
        putProperty(PROP_CLASS_TEMPLATE, newVal, true);
    }

    public boolean isMembersPublic() {
        return ((Boolean) getProperty(PROP_MEMBERS_PUBLIC)).booleanValue();
    }

    public void setMembersPublic(boolean newVal) {
        putProperty(PROP_MEMBERS_PUBLIC, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isMembersProtected() {
        return ((Boolean) getProperty(PROP_MEMBERS_PROTECTED)).booleanValue();
    }

    public void setMembersProtected(boolean newVal) {
        putProperty(PROP_MEMBERS_PROTECTED, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isMembersPackage() {
        return ((Boolean) getProperty(PROP_MEMBERS_PACKAGE)).booleanValue();
    }

    public void setMembersPackage(boolean newVal) {
        putProperty(PROP_MEMBERS_PACKAGE, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isBodyComments() {
        return ((Boolean) getProperty(PROP_BODY_COMMENTS)).booleanValue();
    }

    public void setBodyComments(boolean newVal) {
        putProperty(PROP_BODY_COMMENTS, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isBodyContent() {
        return ((Boolean) getProperty(PROP_BODY_CONTENT)).booleanValue();
    }

    public void setBodyContent(boolean newVal) {
        putProperty(PROP_BODY_CONTENT, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isJavaDoc() {
        return ((Boolean) getProperty(PROP_JAVADOC)).booleanValue();
    }

    public void setJavaDoc(boolean newVal) {
        putProperty(PROP_JAVADOC, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }
   
    public boolean isCfgCreateEnabled() {
        return ((Boolean) getProperty(PROP_CFGCREATE_ENABLED)).booleanValue();
    }
    
    public void setCfgCreateEnabled(boolean newVal) {
        putProperty(PROP_CFGCREATE_ENABLED, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isGenerateExceptionClasses() {
        return ((Boolean) getProperty(PROP_GENERATE_EXCEPTION_CLASSES)).booleanValue();
    }

    public void setGenerateExceptionClasses(boolean newVal) {
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }
    
   
    public boolean isGenerateAbstractImpl() {
     return ((Boolean) getProperty(PROP_GENERATE_ABSTRACT_IMPL)).booleanValue();
    }

    public void setGenerateAbstractImpl(boolean newVal) {
     putProperty(PROP_GENERATE_ABSTRACT_IMPL, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean isGlobal() {
        return false;
    }
    
    public boolean isGenerateSuiteClasses() {
        return ((Boolean) getProperty(PROP_GENERATE_SUITE_CLASSES)).booleanValue();
    }

    public void setGenerateSuiteClasses(boolean newVal) {
        putProperty(PROP_GENERATE_SUITE_CLASSES, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }

    
    public boolean isIncludePackagePrivateClasses() {
        return ((Boolean) getProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES)).booleanValue();
    }

    public void setIncludePackagePrivateClasses(boolean newVal) {
        putProperty(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }    
    
    public boolean isGenerateTestsFromTestClasses() {
        return ((Boolean) getProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES)).booleanValue();
    }

    public void setGenerateTestsFromTestClasses(boolean newVal) {
        putProperty(PROP_GENERATE_TESTS_FROM_TEST_CLASSES, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }    
    
    public boolean isGenerateMainMethod() {
        return ((Boolean) getProperty(PROP_GENERATE_MAIN_METHOD)).booleanValue();
    }

    public void setGenerateMainMethod(boolean newVal) {
        putProperty(PROP_GENERATE_MAIN_METHOD, newVal ? Boolean.TRUE : Boolean.FALSE, true);
    }
    
    public String getGenerateMainMethodBody() {
        return (String) getProperty(PROP_GENERATE_MAIN_METHOD_BODY);
    }

    public void setGenerateMainMethodBody(String newVal) {
        putProperty(PROP_GENERATE_MAIN_METHOD_BODY, newVal, true);
    }
    
    public String getTestClassNamePrefix() {
        return (String) getProperty(PROP_TEST_CLASSNAME_PREFIX);
    }

    public void setTestClassNamePrefix(String newVal) {
        putProperty(PROP_TEST_CLASSNAME_PREFIX, newVal, true);
    }    

    public String getTestClassNameSuffix() {
        return (String) getProperty(PROP_TEST_CLASSNAME_SUFFIX);
    }

    public void setTestClassNameSuffix(String newVal) {
        putProperty(PROP_TEST_CLASSNAME_SUFFIX, newVal, true);
    }        
    
    public String getSuiteClassNamePrefix() {
        return (String) getProperty(PROP_SUITE_CLASSNAME_PREFIX);
    }

    public void setSuiteClassNamePrefix(String newVal) {
        putProperty(PROP_SUITE_CLASSNAME_PREFIX, newVal, true);
    }    

    public String getSuiteClassNameSuffix() {
        return (String) getProperty(PROP_SUITE_CLASSNAME_SUFFIX);
    }

    public void setSuiteClassNameSuffix(String newVal) {
        putProperty(PROP_SUITE_CLASSNAME_SUFFIX, newVal, true);
    }

    public String getRootSuiteClassName() {
        return (String) getProperty(PROP_ROOT_SUITE_CLASSNAME);
    }

    public void setRootSuiteClassName(String newVal) {
        putProperty(PROP_ROOT_SUITE_CLASSNAME, newVal, true);
    }    
}
