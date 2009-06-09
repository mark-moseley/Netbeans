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

package org.netbeans.modules.j2ee.persistence.provider;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Properties;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider;
import org.netbeans.modules.j2ee.persistence.unit.*;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * A utility class for handling persistence units and providers. Provides means
 * for constructing a persistence unit and for getting/setting/changing 
 * properties of persistence units.
 * 
 * @author Martin Adamek, Erno Mononen
 */
public class ProviderUtil {
    
    // known providers
    public static final Provider HIBERNATE_PROVIDER = new HibernateProvider();
    public static final Provider TOPLINK_PROVIDER = ToplinkProvider.create();
    public static final Provider ECLIPSELINK_PROVIDER = new EclipseLinkProvider();
    public static final Provider KODO_PROVIDER = new KodoProvider();
    public static final Provider DATANUCLEUS_PROVIDER = new DataNucleusProvider();
    public static final Provider OPENJPA_PROVIDER = new OpenJPAProvider();
    public static final Provider DEFAULT_PROVIDER = new DefaultProvider();
    
    /**
     * TopLink provider using the provider class that was used in NetBeans 5.5. Needed
     * for maintaining backwards compatibility with persistence units created in 5.5.
     */ 
    private static final Provider TOPLINK_PROVIDER_55_COMPATIBLE = ToplinkProvider.create55Compatible();

    private ProviderUtil() {
    }
    
    /**
     * Gets the persistence provider identified by the given <code>providerClass</code>.
     * If the given class was empty or null, will return the default persistence provider
     * of the given project's target server, or null if a default provider is not supported
     * in the given project.
     *
     * @param providerClass the FQN of the class that specifies the persistence provider.
     *
     * @return the provider that the given providerClass represents or null if it was
     * an empty string and the project doesn't suppport a default (container managed)
     * persistence provider.
     */
    public static Provider getProvider(String providerClass, Project project){
        
        if (null == providerClass || "".equals(providerClass.trim())){
            return getContainerManagedProvider(project);
        }
        
        for (Provider each : getAllProviders()){
            if (each.getProviderClass().equals(providerClass.trim())){
                return each;
            }
        }
        // some unknown provider
        return DEFAULT_PROVIDER;
        
    }
    
    
    /*
     * Gets the default persistence provider of the target server
     * of the given <code>project</code>.
     *
     * @return the default container managed provider for the given project or <code>null</code>
     * no default provider could be resolved.
     *
     * @throws NullPointerException if the given project was null.
     */
    private static Provider getContainerManagedProvider(Project project){
        
        PersistenceProviderSupplier providerSupplier = project.getLookup().lookup(PersistenceProviderSupplier.class);
        
        if (providerSupplier == null
                || !providerSupplier.supportsDefaultProvider()
                || providerSupplier.getSupportedProviders().isEmpty()){
            
            return null;
        }
        
        return providerSupplier.getSupportedProviders().get(0);
    }
    
    /**
     * Gets the database connection specified in the given persistence
     * unit.
     * 
     * @param pu the persistence unit whose database connection is to 
     * be retrieved; must not be null.
     * 
     * @rerturn the connection specified in the given persistence unit or
     * <code>null</code> if it didn't specify a connectioh.
     * 
     */ 
    public static DatabaseConnection getConnection(PersistenceUnit pu) {
        
        Parameters.notNull("pu", pu); //NOI18N
        
        if (pu.getProperties() == null){
            return null;
        }
        
        String url = null;
        String driver = null;
        String username = null;
        Property[] properties = pu.getProperties().getProperty2();
        Provider provider = getProvider(pu);
        
        for (int i = 0; i < properties.length; i++) {
            String key = properties[i].getName();
            if (key == null){
                continue;
            }
            if (key.equals(provider.getJdbcUrl())) {
                url = properties[i].getValue();
            } else if (key.equals(provider.getJdbcDriver())) {
                driver = properties[i].getValue();
            } else if (key.equals(provider.getJdbcUsername())) {
                username = properties[i].getValue();
            }
        }
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        
        for (int i = 0; i < connections.length; i++) {
            DatabaseConnection c = connections[i];
            // password is problematic, when it is returned?
            if (c.getDatabaseURL().equals(url) &&
                    c.getDriverClass().equals(driver) &&
                    c.getUser().equals(username)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Sets the given table generation strategy for given persistence unit.
     * @param persistenceUnit
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code>
     * @project the project of the given persistence unit
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Project project) {
        String providerClass = persistenceUnit.getProvider();
        Provider provider = ProviderUtil.getProvider(providerClass, project);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }
    
    /**
     * Sets the given table generation strategy for the given persistence unit.
     *
     * @param persistenceUnit the persistenceUnit to which the given strategy is to be set.
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code> for
     * options.
     * @provider the provider whose table generation property will be used.
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Provider provider){
        // issue 123224. The user can have a persistence.xml in J2SE project without provider specified
        if(provider == null ) {
            return;
        }
        Property tableGenerationProperty = provider.getTableGenerationProperty(tableGenerationStrategy);
        Properties properties = persistenceUnit.getProperties();
        if (properties == null) {
            properties = persistenceUnit.newProperties();
            persistenceUnit.setProperties(properties);
        }
        
        Property existing = getProperty(properties.getProperty2(), provider.getTableGenerationPropertyName());
        
        if (existing != null && tableGenerationProperty == null){
            properties.removeProperty2(existing);
        } else if (existing != null && tableGenerationProperty != null){
            existing.setValue(tableGenerationProperty.getValue());
        } else if (tableGenerationProperty != null){
            properties.addProperty2(tableGenerationProperty);
        }
        
    }
    
    /**
     * Sets the given provider, connection and table generation strategy to the given persistence unit. Note
     * that if the given persistence unit already had an existing provider, its existing  properties are not preserved
     * with the exception of the database connection properties. In other words, you have to explicitly set for
     * example a table generation strategy for the persistence unit after changing the provider.
     *
     * @param persistenceUnit the persistence unit to which the other params are to be set; must not be null.
     * @param provider the provider to set; must not be null.
     * @connection the connection to set; must not be null.
     * @tableGenerationStrategy the table generation strategy to set.
     */
    public static void setProvider(PersistenceUnit persistenceUnit, Provider provider,
            DatabaseConnection connection, String tableGenerationStrategy){
        
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        // See issue 123224 desc 12 and desc 15 - connection can be null
        //Parameters.notNull("connection", connection); //NOI18N
        Parameters.notNull("provider", provider); //NOI18N
        
        removeProviderProperties(persistenceUnit);
        persistenceUnit.setProvider(provider.getProviderClass());
        setDatabaseConnection(persistenceUnit, connection);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }
    
    
    /**
     * Removes all provider specific properties from the given persistence unit.
     * Should be called before setting a new provider for persistence units.
     *
     * @param persistenceUnit the persistence unit whose provider specific
     * properties are to be removed; must not be null.
     */
    public static void removeProviderProperties(PersistenceUnit persistenceUnit){
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        
        Provider old = getProvider(persistenceUnit);
        Property[] properties = getProperties(persistenceUnit);
        
        if (old != null){
            for (int i = 0; i < properties.length; i++) {
                Property each = properties[i];
                if (old.getPropertyNames().contains(each.getName())){
                    persistenceUnit.getProperties().removeProperty2(each);
                }
            }
        }
        persistenceUnit.setProvider(null);
        
    }
    
    
    /**
     * Constructs a persistence unit based on the given paramaters. Takes care of
     * setting the default vendor specific properties (if any) to the created
     * persistence unit.
     *
     * @param name the name for the persistence unit; must not be null.
     * @param provider the provider for the persitence unit; must not be null.
     * @param connection the database connection for the persistence unit; must not be null.
     *
     * @return the created persistence unit.
     */
    public static PersistenceUnit buildPersistenceUnit(String name, Provider provider, DatabaseConnection connection) {
        
        Parameters.notNull("name", name);
        Parameters.notNull("provider", provider);
        Parameters.notNull("connection", connection);
        
        PersistenceUnit persistenceUnit = new PersistenceUnit();
        persistenceUnit.setName(name);
        persistenceUnit.setProvider(provider.getProviderClass());
        Properties properties = persistenceUnit.newProperties();
        Map connectionProperties = provider.getConnectionPropertiesMap(connection);
        for (Iterator it = connectionProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) connectionProperties.get(propertyName));
            properties.addProperty2(property);
        }
        
        Map defaultProperties = provider.getDefaultVendorSpecificProperties();
        for (Iterator it = defaultProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) defaultProperties.get(propertyName));
            properties.addProperty2(property);
        }
        
        persistenceUnit.setProperties(properties);
        return persistenceUnit;
    }
    
    
    /**
     * Sets the properties of the given connection to the given persistence unit.
     * 
     * @param persistenceUnit the persistence unit to which the connection properties
     * are to be set. Must not be null.
     * @param connection the database connections whose properties are to be set. Must
     * not be null.
     */
    public static void setDatabaseConnection(PersistenceUnit persistenceUnit, DatabaseConnection connection){
        
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        // See issue 123224 desc 12 and desc 15 - connection can be null
        //Parameters.notNull("connection", connection); //NOI18N
        
        
        Provider provider = getProvider(persistenceUnit);
        Property[] properties = getProperties(persistenceUnit);
        
        Map<String, String> propertiesMap = provider.getConnectionPropertiesMap(connection);
        
        for (String name : propertiesMap.keySet()) {
            Property property = getProperty(properties, name);
            if (property == null){
                
                if (persistenceUnit.getProperties() == null){
                    persistenceUnit.setProperties(persistenceUnit.newProperties());
                }
                
                property = persistenceUnit.getProperties().newProperty();
                property.setName(name);
                persistenceUnit.getProperties().addProperty2(property);
            }
            
            String value = propertiesMap.get(name);
            // value must be present (setting null would cause
            // value attribute to not be present)
            if (value == null){
                value = "";
            }
            property.setValue(value);
        }
    }
    
    /**
     * Gets the properties of the given persistence unit. If the properties of
     * given unit were null, will return an empty array.
     * 
     * @return array of properties, empty if the given unit's properties were null.
     */
    static Property[] getProperties(PersistenceUnit persistenceUnit){
        if (persistenceUnit.getProperties() != null){
            return persistenceUnit.getProperties().getProperty2();
        }
        return new Property[0];
    }
    
    /**
     * @return the property from the given properties whose name matches 
     * the given propertyName
     * or null if the given properties didn't contain property with a matching name.
     */
    private static Property getProperty(Property[] properties, String propertyName){
        
        if (null == properties){
            return null;
        }
        
        for (int i = 0; i < properties.length; i++) {
            Property each = properties[i];
            if (each.getName() != null && each.getName().equals(propertyName)){
                return each;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the property that matches the given <code>propertyName</code> from the
     * given <code>persistenceUnit</code>.
     *
     * @return the matching property or null if the given persistence unit didn't
     * contain a property with a matching name.
     */
    public static Property getProperty(PersistenceUnit persistenceUnit, String propertyName){
        if (persistenceUnit.getProperties() == null){
            return null;
        }
        return getProperty(persistenceUnit.getProperties().getProperty2(), propertyName);
    }
    
    /**
     * Gets the persistence provider of the given persistence unit.
     * 
     * @param persistenceUnit the persistence unit whose provider is to 
     * be get. Must not be null.
     * 
     * @return the provider of the given persistence unit. In case that no specific
     * provider can be resolved <code>DEFAULT_PROVIDER</code> will be returned.
     */
    public static Provider getProvider(PersistenceUnit persistenceUnit){
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        
        for (Provider each : getAllProviders()){
            if(each.getProviderClass().equals(persistenceUnit.getProvider())){
                return each;
            }
        }
        return DEFAULT_PROVIDER;
    }
    
    /**
     *@return true if the given puDataObject is not null and its document is
     * parseable, false otherwise.
     */
    public static boolean isValid(PUDataObject puDataObject){
        return null == puDataObject ? false : puDataObject.parseDocument();
    }
    
    /**
     * Gets the persistence units that are defined in the given <code>
     * puDataObject</code>.
     * 
     * @param puDataObject the PUDataObject whose persistence units are to be retrived.
     * 
     * @return the persistence units specified in the given <code>puDataObject</code>
     * or an empty array if there were no persistence units defined in it.
     */
    public static PersistenceUnit[] getPersistenceUnits(PUDataObject puDataObject){
        if (puDataObject.getPersistence() == null){
            return new PersistenceUnit[0];
        }
        return puDataObject.getPersistence().getPersistenceUnit();
    }
    
    /**
     * Renames given managed class in given persistence unit.
     * @param persistenceUnit the unit that contains the class to be renamed.
     * @param newName the new name of the class.
     * @param oldName the name of the class to be renamed.
     * @param dataObject
     *
     */
    public static void renameManagedClass(PersistenceUnit persistenceUnit, String newName,
            String oldName, PUDataObject dataObject){
        
        dataObject.removeClass(persistenceUnit, oldName);
        dataObject.addClass(persistenceUnit, newName);
        
    }
    
    /**
     * Removes given managed class from given persistence unit.
     * @param persistenceUnit the persistence unit from which the given class
     * is to be removed.
     * @param clazz fully qualified name of the class to be removed.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void removeManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject){
        
        dataObject.removeClass(persistenceUnit, clazz);
    }
    
    /**
     * Adds given managed class to given persistence unit.
     * @param persistenceUnit the persistence unit to which the given class
     * is to be added.
     * @param clazz fully qualified name of the class to be added.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void addManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject){
        
        dataObject.addClass(persistenceUnit, clazz);
    }
    
    
    /**
     * Adds the given <code>persistenceUnit</code> to the <code>PUDataObject<code>
     *  of the given <code>project</code> and saves it.
     * @param persistenceUnit the unit to be added
     * @param project the project to which the unit is to be added.
     * @throws InvalidPersistenceXmlException if the given project has an invalid persistence.xml file.
     *
     */
    public static void addPersistenceUnit(PersistenceUnit persistenceUnit, Project project) throws InvalidPersistenceXmlException{
        PUDataObject pud = getPUDataObject(project);
        pud.addPersistenceUnit(persistenceUnit);
        pud.save();
    }
    
    /**
     *Gets the <code>PUDataObject</code> associated with the given <code>fo</code>.
     * 
     *@param fo the file object thas has an associated <code>PUDataObject</code>. Must
     * not be null.
     * 
     *@return the <code>PUDataObject</code> associated with the given <code>fo</code>.
     * 
     *@throws IllegalArgumentException if the given <code>fo</code> is null.
     *@throws InvalidPersistenceXmlException if the given file object represents
     * an invalid persistence.xml file.
     */
    public static PUDataObject getPUDataObject(FileObject fo) throws InvalidPersistenceXmlException{
        Parameters.notNull("fo", fo); //NOI18N

        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!(dataObject instanceof PUDataObject)){
            throw new InvalidPersistenceXmlException(FileUtil.getFileDisplayName(fo));
        }
        return (PUDataObject) dataObject;
    }
    
    /**
     * Gets the PUDataObject associated with the given <code>project</code>. If there
     * was no PUDataObject (i.e. no persistence.xml) in the project, a new one
     * will be created. Use
     * {@link #getDDFile} for testing whether a project has a persistence.xml file.
     * 
     *@param project the project whose PUDataObject is to be get. Must not be null.
     * 
     *@return <code>PUDataObject</code> associated with the given project or null 
     * if there is no such <code>PUDataObject</code>.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>project</code> had an existing
     * invalid persitence.xml file.
     */
    public static synchronized PUDataObject getPUDataObject(Project project) throws InvalidPersistenceXmlException{
        Parameters.notNull("project", project); //NOI18N
        
        FileObject puFileObject = getDDFile(project);
        if (puFileObject == null) {
            try {
                puFileObject = createPersistenceDDFile(project);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        if (puFileObject == null) {
            return null;
        }
        return getPUDataObject(puFileObject);
    }
    
    /**
     * Creates a new FileObject representing file that defines
     * persistence units (<tt>persistence.xml</tt>). <i>Todo: move somewhere else?</i>
     * @return FileObject representing <tt>persistence.xml</tt>.
     */
    private static FileObject createPersistenceDDFile(Project project) throws IOException {
        final FileObject persistenceLocation = PersistenceLocation.createLocation(project);
        if (persistenceLocation == null) {
            return null;
        }
        final FileObject[] dd = new FileObject[1];
        // must create the file using AtomicAction, see #72058
        persistenceLocation.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                dd[0] = FileUtil.copyFile(FileUtil.getConfigFile(
                        "org-netbeans-modules-j2ee-persistence/persistence-1.0.xml"), persistenceLocation, "persistence"); //NOI18N
            }
        });
        return dd[0];
    }
    
    /**
     * Checks whether the given project has a persistence.xml that contains at least one
     * persistence unit.
     * 
     * @project the project; must not be null.
     * 
     * @return true if the given project has a persistence.xml containing
     * at least one persitence unit, false otherwise.
     * 
     * @throws InvalidPersistenceXmlException if the given <code>project</code> has an
     *  invalid persistence.xml file.
     */
    public static boolean persistenceExists(Project project) throws InvalidPersistenceXmlException{
       Parameters.notNull("project", project); //NOI18N
        
        if (getDDFile(project) == null){
            return false;
        }
        PUDataObject pud = getPUDataObject(project);
        return pud.getPersistence().getPersistenceUnit().length > 0;
    }
    
    /**
     * @return persistence.xml descriptor of first MetadataUnit found on project or null if none found
     */
    public static FileObject getDDFile(Project project){
        PersistenceScope[] persistenceScopes = PersistenceUtils.getPersistenceScopes(project);
        for (int i = 0; i < persistenceScopes.length; i++) {
            return persistenceScopes[i].getPersistenceXml();
        }
        return null;
    }
    
    
    /**
     * @return array of providers known to the IDE.
     */
    public static Provider[] getAllProviders() {
        return new Provider[]{
            TOPLINK_PROVIDER, ECLIPSELINK_PROVIDER, HIBERNATE_PROVIDER, 
            KODO_PROVIDER, DATANUCLEUS_PROVIDER, OPENJPA_PROVIDER, TOPLINK_PROVIDER_55_COMPATIBLE};
    }
    
    /**
     * Makes the given persistence unit portable if possible, i.e. removes the provider class from it.
     * A persistence unit may be made portable if it uses the default provider of the project's target
     * server, it doesn't specify any properties and it is not defined in Java SE environment.
     * 
     * @param project the project in which the given persistence unit is defined. Must not be null.
     * @param persistenceUnit the persistence unit to be made portable. Must not be null.
     * 
     * @return true if given persistence unit could be made portable, false otherwise.
     * 
     * @throws NullPointerException if either project or persistenceUnit was null.
     */
    public static boolean makePortableIfPossible(Project project, PersistenceUnit persistenceUnit){
        
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("persistenceUnit", persistenceUnit); //NOI18N
        
        if (Util.isJavaSE(project)){
            return false;
        }
        
        Provider defaultProvider = getContainerManagedProvider(project);
        
        if (defaultProvider == null){
            return false;
        }
        
        if (defaultProvider.getProviderClass().equals(persistenceUnit.getProvider())
                && persistenceUnit.getProperties().sizeProperty2() == 0){
            
            persistenceUnit.setProvider(null);
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks whether the given <code>project</code>'s target server is present.
     *
     * @param project the project whose target server's presence is checked; must not be null.
     * @return true if the given <code>project</code> has its target server present or
     *  if the project does not need a target server (i.e. it is not a J2EE project), false otherwise.
     * @throws NullPointerException if the given <code>project</code> was null.
     */
    public static boolean isValidServerInstanceOrNone(Project project){
        Parameters.notNull("project", project);
        ServerStatusProvider serverStatusProvider = project.getLookup().lookup(ServerStatusProvider.class);
        if (serverStatusProvider == null) {
            // not a J2EE project
            return true;
        }
        return serverStatusProvider.validServerInstancePresent();
    }
   
    /**
     * Help to migrate the Toplink properties to the corresponding Eclipselink ones and vice versa
     * 
     * @param prevProvider the provider class string 
     * @param curProvider the provider class string
     * @param persistenceUnit the persistence unit that is being modified on
     */
    public static void migrateProperties(String prevProvider, String curProvider, PersistenceUnit persistenceUnit) {
        if (prevProvider.equals("oracle.toplink.essentials.PersistenceProvider") && // NOI18N
                curProvider.equals("org.eclipse.persistence.jpa.PersistenceProvider")) { // NOI18N
            // Migrate TopLink properties to EclipseLink
            Property[] toplinkProps = persistenceUnit.getProperties().getProperty2();
            for (int i = 0; i < toplinkProps.length; i++) {
                if (toplinkProps[i].getName().contains("toplink")) { // NOI18N
                    String propName = toplinkProps[i].getName();
                    propName = propName.replace("toplink", "eclipselink"); // NOI18N

                    Property eclipselinkProp = persistenceUnit.getProperties().newProperty();
                    eclipselinkProp.setName(propName);
                    eclipselinkProp.setValue(toplinkProps[i].getValue());

                    persistenceUnit.getProperties().removeProperty2(toplinkProps[i]);
                    persistenceUnit.getProperties().addProperty2(eclipselinkProp);
                }
            }
        } else if (prevProvider.equals("org.eclipse.persistence.jpa.PersistenceProvider") && // NOI18N
                curProvider.equals("oracle.toplink.essentials.PersistenceProvider")) { // NOI18N
            // Change back to TopLink properties from EclipseLink
            Property[] eclipselinkProps = persistenceUnit.getProperties().getProperty2();
            for (int i = 0; i < eclipselinkProps.length; i++) {
                if (eclipselinkProps[i].getName().contains("eclipselink")) { // NOI18N
                    String propName = eclipselinkProps[i].getName();
                    propName = propName.replace("eclipselink", "toplink"); // NOI18N

                    Property toplinkProp = persistenceUnit.getProperties().newProperty();
                    toplinkProp.setName(propName);
                    toplinkProp.setValue(eclipselinkProps[i].getValue());

                    persistenceUnit.getProperties().removeProperty2(eclipselinkProps[i]);
                    persistenceUnit.getProperties().addProperty2(toplinkProp);
                }
            }
        }
    }
    
}
