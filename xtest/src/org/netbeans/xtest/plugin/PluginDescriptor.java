/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


/*
 * PluginDescriptor.java
 * Class wrapping a plugin configuration
 * !!! need to add chech of configuration validity
 * Created on July 18, 2003, 12:31 PM
 */

package org.netbeans.xtest.plugin;

import org.netbeans.xtest.xmlserializer.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author  mb115822
 */
public class PluginDescriptor implements XMLSerializable, Serializable {

    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.class);
    static {
        try {
            // load global registry
            GlobalMappingRegistry.registerClassForElementName("XTestPlugin", PluginDescriptor.class);
            // register this class
            classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
            classMappingRegistry.registerSimpleField("version",ClassMappingRegistry.ATTRIBUTE,"version");
            classMappingRegistry.registerSimpleField("parentPluginName",ClassMappingRegistry.ATTRIBUTE,"extends");
            classMappingRegistry.registerSimpleField("dependencies",ClassMappingRegistry.ELEMENT,"Dependencies");
            classMappingRegistry.registerContainerField("availableExecutors", "AvailableExecutors", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableExecutors", Executor.class,"Executor");
            classMappingRegistry.registerContainerField("availableCompilers", "AvailableCompilers", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableCompilers", Compiler.class,"Compiler");
            classMappingRegistry.registerContainerField("availablePackagers", "AvailablePackagers", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availablePackagers", Packager.class,"Packager");            
            classMappingRegistry.registerContainerField("availableResultProcessors", "AvailableResultProcessors", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableResultProcessors", ResultProcessor.class,"ResultProcessor");
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }            
    /** Creates a new instance of PluginDescriptor */
    public PluginDescriptor() {
    }
    
    // xmlized fields
    private String name;
    private String version;    
    private Executor[] availableExecutors;
    private Compiler[] availableCompilers;
    private Packager[] availablePackagers;
    private ResultProcessor[] availableResultProcessors;
    private Dependencies dependencies;
    private String parentPluginName;
    // dynamic variables - to be initialized later
    private PluginDescriptor parentPlugin;
    /* currently not requried 
    private PluginDescriptor[] childrenPlugins;
    */
    
    // nonxmlized field
    private File pluginHome;
    
    // get plugin home directory
    public File getPluginHomeDirectory() {
       return pluginHome; 
    }
    
    // set plugin home directory (package private is enough)
    void setPluginHomeDirectory(File dir) {
        pluginHome = dir;
    }
    
    // get Plugin name
    public String getName() {
        return name;
    }
    
    // return true if plugin is this plugin
    public boolean isPlugin(String pluginName) {
         return this.name.equalsIgnoreCase(pluginName);
    }
    
    // get plugin version
    public String getVersion() {
        return version;
    }
    
    // get XTest version
    public String getRequiredXTestVersion() {
        return dependencies.requiredXTestVersion;
    }
    
    // get plugin name which is being extended - naming analogy with superclass
    public String getParentPluginName() {
        return parentPluginName;
    }
    
    public PluginDescriptor getParentPlugin() {
        return parentPlugin;
    }
    
    public boolean hasParentPlugin() {
        return ( getParentPlugin() == null)? false : true;
    }
    
    /* currently not required 
    public PluginDescriptor[] getChildrenPlugins() {
        return childrenPlugins;
    }
     **/
       
    
    // is the supplied plugin descendant of this plugin?
    public boolean isAscendant(PluginDescriptor plugin) {
        if (plugin == null) {
            return false;
        }
        if (this == plugin.getParentPlugin()) {
            return true;
        } else {
            return isAscendant(plugin.getParentPlugin());
        }
    }

    
    // executors !!!
    
    // get all executors
    public Executor[] getExecutors() {
        return availableExecutors;
    }
    
    // get action for defined plugin with defined ID
    // returns null if the action hasn't been found
    private static Action getAction(Action[] availableActions, String actionID) {
        if (availableActions != null) {
            for (int i=0; i < availableActions.length; i++) {
                if (actionID == null) {
                    // default action
                    if (availableActions[i].isDefault()) {
                        return availableActions[i];
                    }                                        
                } else {
                    // non default action
                    if (actionID.equals(availableActions[i].actionID)) {
                        return availableActions[i];
                    }
                }
            }
        }
        return null;
    }
    
    // get the default action in a set
    // returns null if the default action hasn't been found
    /*
    private static Action getDefaultAction(Action[] availableActions) {
        return getAction(availableActions,null);
    }
     **/
    
    // has default action ...
    private static boolean hasDefaultAction(Action[] availableActions) {
        for (int i=0; i < availableActions.length; i++) {
            if (availableActions[i].isDefault()) {
                return true;
            }
        }
        return false;
    }
    
    
    // get the owner of this action
    private PluginDescriptor getActionOwner(Action action, Action[] availableActions) {
        if (availableActions != null) {
            for (int i=0; i < availableActions.length; i++) {
                if (action == availableActions[i]) {
                    return this;
                }
            }
        }
        return null;
    }
    
    // wondering what does this do ... 
    public PluginDescriptor getActionOwner(Action action) {
        PluginDescriptor result = null;
        if (getActionOwner(action,availableCompilers) != null) {
            return this;
        }
        if (getActionOwner(action,availablePackagers) != null) {
            return this;
        }        
        if (getActionOwner(action,availableExecutors) != null) {
            return this;
        }
        if (getActionOwner(action,availableResultProcessors) != null) {
            return this;
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getActionOwner(action);
        } else {
            return null;
        }
    }
    
    

    //  get executor for defined plugin with defined ID
    /**
     * @param executorID id of the executor to be found
     * if null, default executor will be returned (if available)
     *
     * @throws PluginResourceNotFoundException
     * @return
     */    
    public Executor getExecutor(String executorID) throws PluginResourceNotFoundException {
        Action action = getAction(availableExecutors, executorID);
        if (action != null) {
            return (Executor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getExecutor(executorID);
        }
        // throw exception - prepare appropriate message
        String exceptionSubMessage;        
        if (executorID == null) {
            exceptionSubMessage = "default executor";
        } else {
            exceptionSubMessage = "executor "+executorID;
        }
        throw new PluginResourceNotFoundException("Cannot find "+exceptionSubMessage+" for plugin "+getName());        
    }
    
    // get default plugin executor
    public Executor getDefaultExecutor() throws PluginResourceNotFoundException {
        return getExecutor(null);
    }
    
    public boolean hasDefaultExecutor() {
        if (hasDefaultAction(availableExecutors)) {
            return true;
        } else if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultExecutor();
        }
        return false;
    }
    
    
    // compilers !!!    
    public Compiler[] getCompilers() {
        return availableCompilers;
    }
    
    // get compiler for defined plugin with defined ID
    public Compiler getCompiler(String compilerID) throws PluginResourceNotFoundException {
        Action action = getAction(availableCompilers, compilerID);
        if (action != null) {
            return (Compiler)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getCompiler(compilerID);
        }
        // throw exception - prepare appropriate message
        String exceptionSubMessage;        
        if (compilerID == null) {
            exceptionSubMessage = "default compiler";
        } else {
            exceptionSubMessage = "compiler "+compilerID;
        }        
        throw new PluginResourceNotFoundException("Cannot find "+exceptionSubMessage+" for plugin "+getName());
    }
    
    // get default plugin compiler
    public Compiler getDefaultCompiler() throws PluginResourceNotFoundException {
        return getCompiler(null);
    }
    
    public boolean hasDefaultCompiler() {
        if (hasDefaultAction(availableCompilers)) {
            return true;
        } else if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultCompiler();
        }
        return false;
    }
    
    // packagers        
    public Packager[] getPackagers() {
        return availablePackagers;
    }
    
    // get packager for defined plugin with defined ID
    public Packager getPackager(String packagerID) throws PluginResourceNotFoundException {
        Action action = getAction(availablePackagers, packagerID);
        if (action != null) {
            return (Packager)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getPackager(packagerID);
        }
        // throw exception - prepare appropriate message
        String exceptionSubMessage;        
        if (packagerID == null) {
            exceptionSubMessage = "default packager";
        } else {
            exceptionSubMessage = "packager "+packagerID;
        }        
        throw new PluginResourceNotFoundException("Cannot find "+exceptionSubMessage+" for plugin "+getName());
    }
    
    // get default plugin packager
    public Packager getDefaultPackager() throws PluginResourceNotFoundException {
        return getPackager(null);
    }
    
    public boolean hasDefaultPackager() {
        if (hasDefaultAction(availablePackagers)) {
            return true;
        } else if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultPackager();
        }
        return false;
    }    
    
    // result processors
    public ResultProcessor[] getResultProcessors() {
        return availableResultProcessors;
    }
    
    
    // get RP for defined plugin with defined ID
    public ResultProcessor getResultProcessor(String rpID) throws PluginResourceNotFoundException {
        Action action = getAction(availableResultProcessors, rpID);
        if (action != null) {
            return (ResultProcessor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getResultProcessor(rpID);
        }   
        // throw exception - prepare appropriate message
        String exceptionSubMessage;        
        if (rpID == null) {
            exceptionSubMessage = "default result processor";
        } else {
            exceptionSubMessage = "result processor "+rpID;
        }        
        throw new PluginResourceNotFoundException("Cannot find "+exceptionSubMessage+" for plugin "+getName());        
    }
    
    
    // get default plugin RP
    public ResultProcessor getDefaultResultProcessor() throws PluginResourceNotFoundException {
        return getResultProcessor(null);
    }    
    
    // get result processor for corresponding executor
    public ResultProcessor getCorrespondingResultProcessor(String executorID) throws PluginResourceNotFoundException {        
        if ((executorID != null)&&(availableResultProcessors != null)) {
            for (int i=0; i < availableResultProcessors.length; i++) {
                    if (executorID.equals(availableResultProcessors[i].getCorrespondingExecutorID())) {
                        return availableResultProcessors[i];
                    }
                }
        }
        // no corresponding result processor was found for this target as defined, fallback to default rp
        return getDefaultResultProcessor();
    }
    
    public boolean hasDefaultResultProcessor() {
        if (hasDefaultAction(availableResultProcessors)) {
            return true;
        } else if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultResultProcessor();
        }
        return false;
    }    
    
    
    
    // get plugin dependency
    public PluginDependency[] getDependencies() {
        if (dependencies != null) {
            if (dependencies.pluginDependencies != null) {
                return dependencies.pluginDependencies;
            }
        }
        return new PluginDependency[0];
    }
    
    
    // static method for postInitialization things
    public static void performPostInitialization(PluginDescriptor[] descriptors) throws PluginConfigurationException {
        HashMap globalHashMap = new HashMap(descriptors.length);
        for (int i=0; i < descriptors.length; i++) {
            globalHashMap.put(descriptors[i].getName(), descriptors[i]);
        }
        // now set parent relationship and set plugins to dependencies
        for (int i=0; i < descriptors.length; i++) {
            PluginDescriptor descriptor = descriptors[i];
            // parent 
            if (descriptor.getParentPluginName() != null) {
                PluginDescriptor parentPlugin = (PluginDescriptor)globalHashMap.get(descriptor.getParentPluginName());
                if (parentPlugin != null) {
                    descriptor.parentPlugin = parentPlugin;
                } else {
                    // houston, we have a problem - plugin cannot extend a plugin, which is not installed
                    throw new PluginConfigurationException("Plugin "+descriptor.getName()+"cannot extend plugin "
                                    +descriptor.getParentPluginName()+", which is not installed");
                }
            }
            // dependencies
            PluginDescriptor.PluginDependency[] dependencies = descriptor.getDependencies();
            for (int j = 0; j < dependencies.length; j++) {
                String dependencyName = dependencies[j].name;                
                for (int k = 0; k < descriptors.length; k++) {                    
                    if (descriptors[k].getName().equals(dependencyName)) {
                        dependencies[j].pluginDescriptor = descriptors[k];
                        break;
                    }
                }
                // check whether descriptor was found - if not throw Exception
                if (dependencies[j].pluginDescriptor == null) {
                    throw new PluginConfigurationException("Cannot find dependency '"+dependencyName+"' for plugin "
                    + descriptor.getName());
                }
            }
            
        }
        
        
        
    }
    
    
    // public inner classes
    public static class Dependencies implements XMLSerializable, Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Dependencies.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("requiredXTestVersion",ClassMappingRegistry.ATTRIBUTE,"requiredXTestVersion");
                classMappingRegistry.registerContainerField("pluginDependencies","UsePlugin",ClassMappingRegistry.DIRECT);
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }        
        
        
        private String requiredXTestVersion;
        private PluginDependency[] pluginDependencies;
    }
    
    public static class PluginDependency implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.PluginDependency.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
                classMappingRegistry.registerSimpleField("version",ClassMappingRegistry.ATTRIBUTE,"version");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }   
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public PluginDescriptor getPluginDescriptor() {
            return pluginDescriptor;
        }
        
        private String name;
        private String version;
        private PluginDescriptor pluginDescriptor;
    }
    
    
    // abstract class as a base for executor/compiler/result processor
    // only have methods/fields - each implementation have to create classMapping registry
    // on its own
    public static abstract class Action implements java.io.Serializable {

        private String actionID;
        private String target;
        private String antFile;
        private boolean defaultAction = false;
        
        // return target of this executor
        public String getTarget() {
            return target;
        }
        
        // return ant file where this executor is stored
        public String getAntFile() {
            return antFile;
        }
        
        // is this Executor defalt?
        public boolean isDefault() {
            return defaultAction;
        }
        
        // returns executorID - package private, used only by PluginManager
        String getID() {
            return actionID;
        }        
    }
    
    
    public static class Executor extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Executor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }    
    }
    
    public static class Compiler extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Compiler.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }        
    }    
    
    public static class Packager extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Packager.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }        
    }
    
    
    public static class ResultProcessor extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.ResultProcessor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                classMappingRegistry.registerSimpleField("executorID", ClassMappingRegistry.ATTRIBUTE,"executor");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        private String executorID;
        
        public String getCorrespondingExecutorID() {
            return executorID;
        }
    }
}
