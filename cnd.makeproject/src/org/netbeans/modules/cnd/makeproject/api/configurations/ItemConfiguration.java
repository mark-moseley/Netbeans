/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.configurations.ItemXMLCodec;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class ItemConfiguration implements ConfigurationAuxObject {
    private boolean needSave = false;
    
    private Configuration configuration;
    private Item item;
    
    // General
    private BooleanConfiguration excluded;
    private int tool;
    
    // Tools
    private CustomToolConfiguration customToolConfiguration;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    
    public ItemConfiguration(Configuration configuration, Item item) {
        // General
        this.configuration = configuration;
        setItem(item);
        excluded = new BooleanConfiguration(null, false);
        // Compilers
        customToolConfiguration = new CustomToolConfiguration();
        cCompilerConfiguration = new CCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), ((MakeConfiguration)configuration).getCCompilerConfiguration());
        ccCompilerConfiguration = new CCCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), ((MakeConfiguration)configuration).getCCCompilerConfiguration());
        fortranCompilerConfiguration = new FortranCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), ((MakeConfiguration)configuration).getFortranCompilerConfiguration());
        clearChanged();
    }
    
    public boolean isCompilerToolConfiguration() {
        return getTool() == Tool.CCompiler ||
                getTool() == Tool.CCCompiler ||
                getTool() == Tool.FortranCompiler;
    }
    
    public BasicCompilerConfiguration getCompilerConfiguration() {
        if (getTool() == Tool.CCompiler)
            return cCompilerConfiguration;
        else if (getTool() == Tool.CCCompiler)
            return ccCompilerConfiguration;
        else if (getTool() == Tool.FortranCompiler)
            return fortranCompilerConfiguration;
        else
            assert false;
        return null;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Item getItem() {
        return item;
    }
    
    public void setItem(Item item) {
        this.item = item;
        needSave = true;
        tool = item.getDefaultTool();
    }
    
    // General
    public BooleanConfiguration getExcluded() {
        return excluded;
    }
    
    public void setExcluded(BooleanConfiguration excluded) {
        this.excluded = excluded;
        needSave = true;
    }
    
    // Tool
    public void setTool(String genericName) {
        if (genericName != null) {
            CompilerSet set = CompilerSets.getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
            tool = set.getToolKind(genericName);
        }
    }
    public void setTool(int tool) {
        this.tool = tool;
    }
    public int getTool() {
        return tool;
    }
    protected String getToolName() {
        CompilerSet set = CompilerSets.getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
        return set.getTool(getTool()).getName();
    }
    protected String[] getToolNames() {
        CompilerSet set = CompilerSets.getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
        return set.getToolGenericNames();
    }
    
    // Custom Tool
    public void setCustomToolConfiguration(CustomToolConfiguration customToolConfiguration) {
        this.customToolConfiguration = customToolConfiguration;
    }
    
    public CustomToolConfiguration getCustomToolConfiguration() {
        return customToolConfiguration;
    }
    
    // C Compiler
    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.cCompilerConfiguration = cCompilerConfiguration;
    }
    
    public CCompilerConfiguration getCCompilerConfiguration() {
        return cCompilerConfiguration;
    }
    
    // CC Compiler
    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.ccCompilerConfiguration = ccCompilerConfiguration;
    }
    
    public CCCompilerConfiguration getCCCompilerConfiguration() {
        return ccCompilerConfiguration;
    }
    
    // Fortran Compiler
    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.fortranCompilerConfiguration = fortranCompilerConfiguration;
    }
    
    public FortranCompilerConfiguration getFortranCompilerConfiguration() {
        return fortranCompilerConfiguration;
    }
    
    // interface ConfigurationAuxObject
    public boolean shared() {
        return true;
    }
    
    // interface ConfigurationAuxObject
    public boolean hasChanged() {
        return needSave;
    }
    
    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }
    
    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     */
    public String getId() {
        return getId(getItem().getPath());
    }
    
    static public String getId(String path) {
        return "item-" + path;
    }
    
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.print("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration)profileAuxObject;
        setConfiguration(i.getConfiguration());
        setItem(i.getItem());
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        
        getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
        getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
        getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
    }
    
    public ItemConfiguration copy(MakeConfiguration makeConfiguration) {
        ItemConfiguration copy = new ItemConfiguration(makeConfiguration, getItem());
        copy.assign(this);
        return copy;
    }
    
    public Object clone() {
        ItemConfiguration i = new ItemConfiguration(getConfiguration(), getItem());
        
        i.setExcluded((BooleanConfiguration)getExcluded().clone());
        i.setTool(getTool());
        
        i.setCustomToolConfiguration((CustomToolConfiguration)getCustomToolConfiguration().clone());
        i.setCCompilerConfiguration((CCompilerConfiguration)getCCompilerConfiguration().clone());
        i.setCCCompilerConfiguration((CCCompilerConfiguration)getCCCompilerConfiguration().clone());
        i.setFortranCompilerConfiguration((FortranCompilerConfiguration)getFortranCompilerConfiguration().clone());
        return i;
    }
    
    //
    // XML codec support
    public XMLDecoder getXMLDecoder() {
        return new ItemXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new ItemXMLCodec(this);
    }
    
    public void initialize() {
        // FIXUP: this doesn't make sense...
    }
    
    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Item");
        set.setDisplayName("Item");
        set.setShortDescription("Item");
        set.put(new StringRONodeProp("Name", IpeUtils.getBaseName(item.getPath())));
        set.put(new StringRONodeProp("File Path", item.getPath()));
        String fullPath = IpeUtils.toAbsolutePath(((MakeConfiguration)configuration).getBaseDir(), item.getPath());
        String mdate = "";
        File itemFile = new File(fullPath);
        if (itemFile.exists()) {
            mdate = DateFormat.getDateInstance().format(new Date(itemFile.lastModified()));
            mdate += " " + DateFormat.getTimeInstance().format(new Date(itemFile.lastModified()));
        }
        set.put(new StringRONodeProp("Full File Path", fullPath));
        set.put(new StringRONodeProp("Last Modified", mdate));
        sheet.put(set);
        
        set = new Sheet.Set();
        set.setName("ItemConfiguration");
        set.setDisplayName("ItemConfiguration");
        set.setShortDescription("ItemConfiguration");
        set.put(new BooleanNodeProp(getExcluded(), true, "ExcludedFromBuild", "Excluded From Build", "Excluded From Build"));
        set.put(new ToolNodeProp());
        sheet.put(set);
        
        return sheet;
    }
    
    private class ToolNodeProp extends Node.Property {
        public ToolNodeProp() {
            super(Integer.class);
        }
        
        public String getName() {
            return "Tool";
        }
        
        public Object getValue() {
            return new Integer(getTool());
        }
        
        public void setValue(Object v) {
            String newTool = (String)v;
            setTool(newTool);
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public boolean canRead() {
            return true;
        }
        
        public PropertyEditor getPropertyEditor() {
            return new ToolEditor();
        }
    }
    
    private class ToolEditor extends PropertyEditorSupport {
        
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        public String getAsText() {
            int val = ((Integer)getValue()).intValue();
            CompilerSet set = CompilerSets.getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
            return set.getTool(val).getGenericName();
        }
        
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            setValue(text);
        }
        
        public String[] getTags() {
            return getToolNames();
        }
    }
    
    private class StringRONodeProp extends PropertySupport {
        String value;
        public StringRONodeProp(String name, String value) {
            super(name, String.class, name, name, true, false);
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object v) {
        }
    }
    
    
    public String toString() {
        return getItem().getPath();
    }
    
    private static ResourceBundle bundle = null;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ItemConfiguration.class);
        }
        return bundle.getString(s);
    }
}
