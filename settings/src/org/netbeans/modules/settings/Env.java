/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.util.Lookup;

/** A provider for .settings files of a certain DTD.
 *  It creates a suitable convertor according to {@link #EA_CONVERTOR}.
 *
 * @author Jan Pokorsky
 */
public final class Env implements Environment.Provider {
    /** file attribute containing convertor object. Usage 
     * <code>&lt;attr name="settings.convertor" methodvalue="org.netbeans.modules.settings.XMLPropertiesConvertor.create"/>
     * </code>
     */
    public final static String EA_CONVERTOR = "settings.convertor"; //NOI18N
    /** file attribute containing path to the provider. Used by
     * InstanceDataObject.create or upgrade algorithm. Usage 
     * <code>&lt;attr name="settings.providerPath" stringvalue="xml/lookups/NetBeans/DTD_XML_Properties_1_0.instance"/>
     * </code>
     */
    public final static String EA_PROVIDER_PATH = "settings.providerPath"; // NOI18N
    /** file attribute containing PUBLIC attribute of xml header. Usage
     * <code>&lt;attr name="hint.originalPublicID" stringvalue="-//NetBeans//DTD XML Properties 1.0//EN"/>
     * </code>
     */
    public final static String EA_PUBLICID = "hint.originalPublicID"; // NOI18N
    /** file attribute containnig class name of the setting object. Usage
     * <code>&lt;attr name="settings.instanceClass" stringvalue="org.netbeans.modules.foo.Foo"/>
     * </code>
     */
    public final static String EA_INSTANCE_CLASS_NAME = "settings.instanceClass"; //NOI18N
    /** file attribute containnig class name and subclass names of the setting object. Use the
     * attribute for performance reasons. Usage
     * <code>&lt;attr name="settings.instanceOf" stringvalue="org.netbeans.modules.foo.Foo[, ...]"/>
     * </code>
     */
    public final static String EA_INSTANCE_OF = "settings.instanceOf"; //NOI18N
    /** file attribute containnig the setting object. Usage
     * <code>&lt;attr name="settings.instanceCreate" newvalue="org.netbeans.modules.foo.Foo"/>
     * </code> or
     * <code>&lt;attr name="settings.instanceCreate" methodvalue="org.netbeans.modules.foo.Foo.create"/>
     * </code>
     */
    public final static String EA_INSTANCE_CREATE = "settings.instanceCreate"; //NOI18N
    
    private final FileObject providerFO;
    
    /** create Environment.Provider */
    public static Environment.Provider create(FileObject fo) {
        return new Env(fo);
    }
    
    private Env(FileObject fo) {
        providerFO = fo;
    }
    
    public Lookup getEnvironment(DataObject dobj) {
        if (!(dobj instanceof org.openide.loaders.InstanceDataObject)) return Lookup.EMPTY;
        InstanceProvider icp = new InstanceProvider(dobj, providerFO);
        return icp.getLookup();
    }
    
    /** parse file attribute
     * @param attr String value can be null; used delimiter is ","
     * @return set of items
     */
    public static java.util.Set parseAttribute(Object attr) {
        if (attr != null && attr instanceof String) {
            java.util.StringTokenizer s = 
                new java.util.StringTokenizer((String) attr, ","); //NOI18N
            java.util.Set set = new java.util.HashSet(10);
            while (s.hasMoreTokens()) {
                set.add(s.nextToken().trim());
            }
            return set;
        } else {
            return java.util.Collections.EMPTY_SET;
        }
    }
    
    /** look up appropriate provider according to clazz */
    public static FileObject findProvider(Class clazz) throws IOException {
        String prefix = "xml/memory/"; //NOI18N
        String name = clazz.getName().replace('.', '/');
        org.openide.filesystems.FileSystem sfs = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
        FileObject memContext = sfs.findResource(prefix);
        if (memContext == null) throw new java.io.FileNotFoundException("SFS/xml/memory/"); //NOI18N
        
        String convertorPath = new StringBuffer(200).append(prefix).
            append(name).toString(); // NOI18N
        FileObject fo = sfs.findResource(convertorPath);
        if (fo != null) {
            String providerPath = (String) fo.getAttribute(EA_PROVIDER_PATH);
            if (providerPath != null) {
                return sfs.findResource(providerPath);
            }
        }
        return null;
    }
    
}
