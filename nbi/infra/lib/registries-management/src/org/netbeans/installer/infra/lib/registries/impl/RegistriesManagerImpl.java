/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.lib.registries.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.installer.Installer;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.RegistriesManager;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Kirill Sorokin
 */
public class RegistriesManagerImpl implements RegistriesManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<File, ReentrantLock> locks =
            new HashMap<File, ReentrantLock>();
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // engine operations ////////////////////////////////////////////////////////////
    public File getEngine(
            final File root) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            
            return new File(root, ENGINE_JAR);
        } finally {
            lock.unlock();
        }
    }
    
    public void updateEngine(
            final File root,
            final File archive) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            deleteBundles(root);
            
            FileUtils.copyFile(archive, new File(root, ENGINE_JAR));
        } catch (IOException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    
    // component operations /////////////////////////////////////////////////////////
    public void addPackage(
            final File root,
            final File archive,
            final String parentUid,
            final String parentVersion,
            final String parentPlatforms) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            deleteBundles(root);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), false);
            
            final File registryXml =
                    new File(root, REGISTRY_XML);
            final File componentsDir =
                    new File(root, COMPONENTS);
            final File archiveDir =
                    FileUtils.createTempFile(new File(root, TEMP), false);
            final File archiveRegistryXml =
                    new File(archiveDir, REGISTRY_XML);
            
            FileUtils.unjar(archive, archiveDir);
            FileUtils.modifyFile(archiveRegistryXml,
                    "(\\>)resource:(.*?\\<\\/)",
                    "$1" + componentsDir.toURI() + "$2", true);
            
            final Registry registry = new Registry();
            registry.setLocalDirectory(tempDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.loadProductRegistry(registryXml);
            
            final Registry archiveRegistry = new Registry();
            archiveRegistry.setLocalDirectory(tempDir);
            archiveRegistry.setFinishHandler(DummyFinishHandler.INSTANCE);
            archiveRegistry.loadProductRegistry(archiveRegistryXml);
            
            final Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
            
            for (Product product: archiveRegistry.getProducts()) {
                final List<Product> existingProducts = registry.getProducts(
                        product.getUid(),
                        product.getVersion(),
                        product.getPlatforms());
                
                if (existingProducts.size() > 0) {
                    for (Product existingProduct: existingProducts) {
                        nodes.offer(existingProduct);
                    }
                }
            }
            
            for (Group group: archiveRegistry.getGroups()) {
                if (!group.equals(archiveRegistry.getRegistryRoot())) {
                    final Group existingGroup = registry.getGroup(
                            group.getUid());
                    
                    if (existingGroup != null) {
                        nodes.offer(existingGroup);
                    }
                }
            }
            
            if (nodes.size() > 0) {
                while (nodes.peek() != null) {
                    final RegistryNode node = nodes.poll();
                    
                    node.getParent().removeChild(node);
                    
                    if (node instanceof Product) {
                        final Product temp = (Product) node;
                        final String path = PRODUCTS + "/" +
                                temp.getUid() + "/" +
                                temp.getVersion() + "/" +
                                StringUtils.asString(temp.getPlatforms(), " ");
                        
                        FileUtils.deleteFile(new File(root, path), true);
                    }
                    
                    if (node instanceof Group) {
                        final Group temp = (Group) node;
                        final String path = GROUPS + "/" +
                                temp.getUid();
                        
                        FileUtils.deleteFile(new File(root, path), true);
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            final File productsDir = new File(root, PRODUCTS);
            FileUtils.mkdirs(productsDir);
            FileUtils.copyFile(
                    new File(archiveDir, "products"), // temp hack
                    productsDir,
                    true);
            
            final File groupsDir = new File(root, GROUPS);
            FileUtils.mkdirs(groupsDir);
            FileUtils.copyFile(
                    new File(archiveDir, "groups"), // temp hack
                    groupsDir,
                    true);
            
            RegistryNode parent;
            
            List<Product> parents = null;
            if ((parentVersion != null) &&
                    !parentVersion.equals("null") &&
                    (parentPlatforms != null) &&
                    !parentPlatforms.equals("null")) {
                parents = registry.getProducts(
                        parentUid,
                        Version.getVersion(parentVersion),
                        StringUtils.parsePlatforms(parentPlatforms));
            }
            if ((parents == null) || (parents.size() == 0)) {
                parent = registry.getGroup(parentUid);
                if (parent == null) {
                    parent = registry.getRegistryRoot();
                }
            } else {
                parent = parents.get(0);
            }
            
            parent.attachRegistry(archiveRegistry);
            
            registry.saveProductRegistry(
                    registryXml,
                    TrueFilter.INSTANCE,
                    true,
                    true,
                    true);
            
            FileUtils.deleteFile(archiveDir, true);
            FileUtils.deleteFile(tempDir, true);
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } catch (ParseException e) {
            throw new ManagerException(e);
        } catch (XMLException e) {
            throw new ManagerException(e);
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    
    public void removeProduct(
            final File root,
            final String uid,
            final String version,
            final String platforms) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            deleteBundles(root);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), false);
            
            final File registryXml =
                    new File(root,REGISTRY_XML);
            final File productsDir =
                    new File(root, PRODUCTS);
            final File groupsDir =
                    new File(root, GROUPS);
            
            final Registry registry = new Registry();
            
            registry.setLocalDirectory(tempDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.loadProductRegistry(registryXml);
            
            final List<Product> existing = registry.getProducts(
                    uid,
                    Version.getVersion(version),
                    StringUtils.parsePlatforms(platforms));
            
            if (existing != null) {
                existing.get(0).getParent().removeChild(existing.get(0));
                
                final Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing.get(0));
                
                while(nodes.peek() != null) {
                    final RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        final Product product = (Product) node;
                        
                        final File uidDir = new File(
                                productsDir,
                                product.getUid());
                        final File versionDir = new File(
                                uidDir,
                                product.getVersion().toString());
                        final File platformsDir = new File(
                                versionDir,
                                StringUtils.asString(product.getPlatforms(), " "));
                        
                        FileUtils.deleteFile(platformsDir, true);
                        if (FileUtils.isEmpty(versionDir)) {
                            FileUtils.deleteFile(versionDir, true);
                        }
                        if (FileUtils.isEmpty(uidDir)) {
                            FileUtils.deleteFile(uidDir, true);
                        }
                        
                    }
                    
                    if (node instanceof Group) {
                        final Group group = (Group) node;
                        
                        final File uidDir = new File(
                                groupsDir,
                                group.getUid());
                        
                        FileUtils.deleteFile(uidDir, true);
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(
                    registryXml,
                    TrueFilter.INSTANCE,
                    true,
                    true,
                    true);
            
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } catch (ParseException e) {
            throw new ManagerException(e);
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    
    public void removeGroup(
            final File root,
            final String uid) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            deleteBundles(root);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), false);
            
            final File registryXml =
                    new File(root,REGISTRY_XML);
            final File productsDir =
                    new File(root, PRODUCTS);
            final File groupsDir =
                    new File(root, GROUPS);
            
            final Registry registry = new Registry();
            
            registry.setLocalDirectory(tempDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.loadProductRegistry(registryXml);
            
            final Group existing = registry.getGroup(uid);
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    final RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        final Product product = (Product) node;
                        
                        final File uidDir = new File(
                                productsDir,
                                product.getUid());
                        final File versionDir = new File(
                                uidDir,
                                product.getVersion().toString());
                        final File platformsDir = new File(
                                versionDir,
                                StringUtils.asString(product.getPlatforms(), " "));
                        
                        FileUtils.deleteFile(platformsDir, true);
                        if (FileUtils.isEmpty(versionDir)) {
                            FileUtils.deleteFile(versionDir, true);
                        }
                        if (FileUtils.isEmpty(uidDir)) {
                            FileUtils.deleteFile(uidDir, true);
                        }
                        
                    }
                    
                    if (node instanceof Group) {
                        final Group group = (Group) node;
                        
                        final File uidDir = new File(
                                groupsDir,
                                group.getUid());
                        
                        FileUtils.deleteFile(uidDir, true);
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(
                    registryXml,
                    TrueFilter.INSTANCE,
                    true,
                    true,
                    true);
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    
    // bundles //////////////////////////////////////////////////////////////////////
    public File createBundle(
            final File root,
            final Platform platform,
            final String[] components,
            Properties props,
            Properties bundleProps) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            
            return createBundleNoLock(root, platform, components,props,bundleProps);
        } finally {
            lock.unlock();
        }
    }
    // bundles //////////////////////////////////////////////////////////////////////
    public File createBundle(
            final File root,
            final Platform platform,
            final String[] components) throws ManagerException {
        return createBundle(root,platform,components,new Properties(),new Properties());
    }
    public void generateBundles(
            final File root) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), false);
            
            final File registryXml =
                    new File(root,REGISTRY_XML);
            
            for (Platform platform: Platform.values()) {
                final Registry registry = new Registry();
                
                registry.setLocalDirectory(tempDir);
                registry.setFinishHandler(DummyFinishHandler.INSTANCE);
                registry.loadProductRegistry(registryXml);
                
                final List<Product> products = registry.getProducts(platform);
                for (int i = 1; i <= products.size(); i++) {
                    final Product[] combination = new Product[i];
                    
                    iterate(platform, root, registry, combination, 0, products, 0);
                }
            }
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    
    public void deleteBundles(
            final File root) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            
            deleteBudlesNoLock(root);
        } finally {
            lock.unlock();
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public void initializeRegistry(
            final File root) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
        } finally {
            lock.unlock();
        }
    }
    
    public File exportRegistry(
            final File root,
            final File destination,
            final String codebase) throws ManagerException {
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            initializeRegistryNoLock(root);
            
            final File registryXml =
                    new File(root, REGISTRY_XML);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), true, true);
            final File tempUserDir =
                    FileUtils.createTempFile(tempDir, false);
            final File tempRegistryXml =
                    FileUtils.createTempFile(tempDir, false);
            
            FileUtils.mkdirs(destination.getParentFile());
            
            final Registry registry = new Registry();
            registry.setLocalDirectory(tempUserDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.loadProductRegistry(registryXml);
            
            registry.saveProductRegistry(
                    tempRegistryXml,
                    TrueFilter.INSTANCE,
                    false,
                    true,
                    true);
            
            FileUtils.copyFile(
                    new File(root, COMPONENTS),
                    new File(destination, COMPONENTS),
                    true);
            
            final String replacement =
                    codebase.endsWith("/") ? codebase : codebase + "/";
            FileUtils.modifyFile(
                    tempRegistryXml,
                    "uri>" + root.toURI().toString(),
                    "uri>" + replacement);
            
            FileUtils.copyFile(
                    tempRegistryXml,
                    new File(destination, REGISTRY_XML));
            FileUtils.copyFile(
                    new File(root, ENGINE_JAR),
                    new File(destination, ENGINE_JAR));
            
            FileUtils.deleteFile(tempUserDir);
            FileUtils.deleteFile(tempRegistryXml);
            
            return destination;
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    public String generateComponentsJs(
            final File root) throws ManagerException {
        return generateComponentsJs(root, null, null);
    }
    public String generateComponentsJs(
            final File root, final File bundlesList) throws ManagerException {
        return generateComponentsJs(root, bundlesList, null);
    }
    
    public String generateComponentsJs(
            final File root, File bundlesList, final String localeString) throws ManagerException {
        
        Properties props = new Properties();
        try {
            if (bundlesList != null) {
                FileInputStream is = new FileInputStream(bundlesList);
                props.load(is);
                is.close();
            }            
        } catch (IOException e){
            throw new ManagerException(e);
        }
        
        final List <Pair <List<String>, String>> bundles = new LinkedList<Pair<List<String>, String>> ();
        for(Object key : props.keySet()) {
             Object value = props.get(key);
             List <String> list = StringUtils.asList(value.toString());
             bundles.add(new Pair(list,  key.toString()));
        }
        
        final Map<String, String> notes = new HashMap<String, String>();
        //notes.put("nb-javase", "for Java SE, includes GUI Builder, Profiler");
        
        Locale locale = StringUtils.parseLocale(localeString==null ? "" : localeString);
        
        final ReentrantLock lock = getLock(root);
        
        try {
            lock.lock();
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), true, true);
            final File tempUserDir =
                    FileUtils.createTempFile(tempDir, true, true);
            
            final StringBuilder out = new StringBuilder();
            
            final Registry registry = loadRegistry(
                    root,
                    tempUserDir,
                    Platform.GENERIC);
            
            final List<Product> products =
                    getProducts(registry.getRegistryRoot());
            final List<Group> groups =
                    getGroups(registry.getRegistryRoot());
            
            final Map<Integer, Integer> productMapping =
                    new HashMap<Integer, Integer>();
            
            final List<String> productUids =
                    new LinkedList<String>();
            final List<String> productVersions =
                    new LinkedList<String>();
            final List<String> productDisplayNames =
                    new LinkedList<String>();
            final List<String> productNotes =
                    new LinkedList<String>();
            final List<String> productDescriptions =
                    new LinkedList<String>();
            final List<String> productDownloadSizes =
                    new LinkedList<String>();
            final List<List<Platform>> productPlatforms =
                    new LinkedList<List<Platform>>();
            final List<String> productProperties =
                    new LinkedList<String>();
            
            final List<Integer> defaultGroupProducts =
                    new LinkedList<Integer>();
            final List<List<Integer>> groupProducts =
                    new LinkedList<List<Integer>>();
            final List<String> groupDisplayNames =
                    new LinkedList<String>();
            final List<String> groupDescriptions =
                    new LinkedList<String>();
            
            for (int i = 0; i < products.size(); i++) {
                final Product product = products.get(i);
                
                boolean existingFound = false;
                for (int j = 0; j < productUids.size(); j++) {
                    if (productUids.get(j).equals(product.getUid()) &&
                            productVersions.get(j).equals(product.getVersion().toString())) {
                        productPlatforms.get(j).addAll(product.getPlatforms());
                        productMapping.put(i, j);
                        existingFound = true;
                        break;
                    }
                }
                
                if (existingFound) {
                    continue;
                }
                
                long size = (long) Math.ceil(
                        ((double) product.getDownloadSize()) / 1024. );
                productUids.add(product.getUid());
                productVersions.add(product.getVersion().toString());
                productDisplayNames.add(product.getDisplayName(locale).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                productDescriptions.add(product.getDescription(locale).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                productDownloadSizes.add(Long.toString(size));
                productPlatforms.add(product.getPlatforms());
                
                if (notes.get(product.getUid()) != null) {
                    productNotes.add(notes.get(product.getUid()).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                } else {
                    productNotes.add("");
                }
                
                String properties = "PROPERTY_NONE";
                for(Pair <List<String>,String> pair : bundles) {
                    if(pair.getFirst().contains(product.getUid())) {
                        properties += " | PROPERTY_" + pair.getSecond();
                    }
                }                
                productProperties.add(properties);
                
                productMapping.put(i, productUids.size() - 1);
            }
            
            out.append("product_uids = new Array();").append("\n");
            for (int i = 0; i < productUids.size(); i++) {
                out.append("    product_uids[" + i + "] = \"" + productUids.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("product_versions = new Array();").append("\n");
            for (int i = 0; i < productVersions.size(); i++) {
                out.append("    product_versions[" + i + "] = \"" + productVersions.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("product_display_names = new Array();").append("\n");
            for (int i = 0; i < productDisplayNames.size(); i++) {
                out.append("    product_display_names[" + i + "] = \"" + productDisplayNames.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("product_notes = new Array();").append("\n");
            for (int i = 0; i < productNotes.size(); i++) {
                out.append("    product_notes[" + i + "] = \"" + productNotes.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("product_descriptions = new Array();").append("\n");
            for (int i = 0; i < productDescriptions.size(); i++) {
                out.append("    product_descriptions[" + i + "] = \"" + productDescriptions.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("product_download_sizes = new Array();").append("\n");
            for (int i = 0; i < productDownloadSizes.size(); i++) {
                out.append("    product_download_sizes[" + i + "] = " + productDownloadSizes.get(i) + ";").append("\n");
            }
            out.append("\n");

            
            out.append("product_platforms = new Array();").append("\n");
            for (int i = 0; i < productPlatforms.size(); i++) {
                out.append("    product_platforms[" + i + "] = new Array();").append("\n");
                for (int j = 0; j < productPlatforms.get(i).size(); j++) {
                    out.append("        product_platforms[" + i + "][" + j + "] = \"" + productPlatforms.get(i).get(j) + "\";").append("\n");
                }
            }
            out.append("\n");
            
            out.append("product_properties = new Array();").append("\n");
            for (int i = 0; i < productProperties.size(); i++) {
                out.append("    product_properties[" + i + "] = " + productProperties.get(i) + ";").append("\n");
            }
            out.append("\n");
            
            for (int i = 0; i < productUids.size(); i++) {
                defaultGroupProducts.add(Integer.valueOf(i));
            }
            
            for (Group group: groups) {
                List<Integer> components = new LinkedList<Integer>();
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getParent().equals(group)) {
                        Integer index = Integer.valueOf(productMapping.get(i));
                        if (!components.contains(index)) {
                            components.add(index);
                            defaultGroupProducts.remove(index);
                        }
                    }
                }
                
                groupProducts.add(components);
                groupDisplayNames.add(group.getDisplayName(locale).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                groupDescriptions.add(group.getDescription(locale).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
            }
            
            if (defaultGroupProducts.size() > 0) {
                groupProducts.add(0, defaultGroupProducts);
                groupDisplayNames.add(0, "");
                groupDescriptions.add(0, "");
            }
            
            out.append("group_products = new Array();").append("\n");
            for (int i = 0; i < groupProducts.size(); i++) {
                out.append("    group_products[" + i + "] = new Array();").append("\n");
                for (int j = 0; j < groupProducts.get(i).size(); j++) {
                    out.append("        group_products[" + i + "][" + j + "] = " + groupProducts.get(i).get(j) + ";").append("\n");
                }
            }
            out.append("\n");
            
            out.append("group_display_names = new Array();").append("\n");
            for (int i = 0; i < groupDisplayNames.size(); i++) {
                out.append("    group_display_names[" + i + "] = \"" + groupDisplayNames.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            out.append("group_descriptions = new Array();").append("\n");
            for (int i = 0; i < groupDescriptions.size(); i++) {
                out.append("    group_descriptions[" + i + "] = \"" + groupDescriptions.get(i) + "\";").append("\n");
            }
            out.append("\n");
            
            return out.toString();
        } catch (IOException e) {
            throw new ManagerException(e);
        } finally {
            lock.unlock();
        }
    }
    // private //////////////////////////////////////////////////////////////////////
    private synchronized ReentrantLock getLock(
            final File root) {
        if (locks.get(root) == null) {
            locks.put(root, new ReentrantLock());
        }
        
        return locks.get(root);
    }
    
    private File createBundleNoLock(
            final File root,
            final Platform platform,
            final String[] components,
            Properties props,
            Properties bundleProps) throws ManagerException {
        try {
            final String key = "" + platform.getCodeName() + ": " +
                    StringUtils.asString(components);
            
            final File bundlesListFile =
                    new File(root, BUNDLES_LIST);
            
            final List<String> bundlesList =
                    FileUtils.readStringList(bundlesListFile);
            if (bundlesList.contains(key)) {
                return new File(bundlesList.get(bundlesList.indexOf(key) + 1));
            }
            
            final File registryXml =
                    new File(root, REGISTRY_XML);
            
            final File tempDir =
                    FileUtils.createTempFile(new File(root, TEMP), true, true);
            final File tempUserDir =
                    FileUtils.createTempFile(tempDir, true, true);
            final File tempStatefile =
                    FileUtils.createTempFile(tempDir, false);
            
            File bundle = new File(
                    FileUtils.createTempFile(new File(root, BUNDLES), false),
                    "bundle.jar");
            
            final Registry registry = new Registry();
            registry.setLocalDirectory(tempUserDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.setTargetPlatform(platform);
            registry.loadProductRegistry(registryXml);
            
            List<Product> products = new LinkedList<Product>();
            for (String string: components) {
                final String[] parts = string.split(",");
                System.out.println("    processing: " + string);
                
                final Product product = registry.getProduct(
                        parts[0],
                        Version.getVersion(parts[1]));
                
                products.add(product);
                product.setStatus(Status.INSTALLED);
            }
            registry.saveStateFile(tempStatefile, new Progress());
            
            bundle.getParentFile().mkdirs();
            
            final File javaHome = new File(System.getProperty("java.home"));
            
            final File tempPropertiesFile = FileUtils.createTempFile(tempDir, false);            
            OutputStream os = new FileOutputStream(tempPropertiesFile);            
            props.store(os,null);
            os.close();
            
            final File tempBundlePropertiesFile = FileUtils.createTempFile(tempDir, false);            
            os = new FileOutputStream(tempBundlePropertiesFile);
            bundleProps.store(os,null);
            os.close();
            
            final ExecutionResults results = SystemUtils.executeCommand(
                    JavaUtils.getExecutable(javaHome).getAbsolutePath(),
                    "-Dnbi.product.remote.registries=" + registryXml.toURI(),
                    "-jar",
                    getEngine(root).getAbsolutePath(),
                    "--silent",
                    "--state",
                    tempStatefile.getAbsolutePath(),
                    "--create-bundle",
                    bundle.getAbsolutePath(),
                    "--ignore-lock",
                    "--platform",
                    platform.toString(),
                    "--userdir",
                    tempUserDir.getAbsolutePath(),
                    "--properties",
                    tempPropertiesFile.getAbsolutePath(),
                    "--bundle-properties",
                    tempBundlePropertiesFile.getAbsolutePath());
            
            if (results.getErrorCode() != 0) {
                throw new ManagerException("Could not create bundle - error in running the engine");
            }
            
            FileUtils.deleteFile(tempStatefile);
            FileUtils.deleteFile(tempPropertiesFile);
            FileUtils.deleteFile(tempBundlePropertiesFile);
            FileUtils.deleteFile(tempUserDir, true);
            
            if (platform.isCompatibleWith(Platform.WINDOWS)) {
                bundle = new File(
                        bundle.getAbsolutePath().replaceFirst("\\.jar$", ".exe"));
            } else if (platform.isCompatibleWith(Platform.MACOSX)) {
                bundle = new File(
                        bundle.getAbsolutePath().replaceFirst("\\.jar$", ".zip"));
            } else {
                bundle = new File(
                        bundle.getAbsolutePath().replaceFirst("\\.jar$", ".sh"));
            }
            
            bundlesList.add(key);
            bundlesList.add(bundle.getAbsolutePath());
            FileUtils.writeStringList(bundlesListFile, bundlesList);
            
            return bundle;
        } catch (IOException e) {
            throw new ManagerException(e);
        } catch (InitializationException e) {
            throw new ManagerException(e);
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        }
    }
    
    private void deleteBudlesNoLock(
            final File root) throws ManagerException {
        try {
            FileUtils.writeFile(new File(root, BUNDLES_LIST), "");
            FileUtils.deleteFile(new File(root, BUNDLES), true);
            FileUtils.mkdirs(new File(root, BUNDLES));
        } catch (IOException e) {
            throw new ManagerException(e);
        }
    }
    
    private void initializeRegistryNoLock(
            final File root) throws ManagerException {
        try {
            if (!root.exists()) {
                FileUtils.mkdirs(root);
            }
            
            final File temp = new File(root, TEMP);
            if (!temp.exists()) {
                FileUtils.mkdirs(temp);
            }
            
            Locale.setDefault(new Locale("en", "US"));
            
            DownloadManager.getInstance().setLocalDirectory(temp);
            DownloadManager.getInstance().setFinishHandler(DummyFinishHandler.INSTANCE);
            
            System.setProperty(
                    Installer.LOCAL_DIRECTORY_PATH_PROPERTY, temp.getAbsolutePath());
            System.setProperty(
                    Installer.IGNORE_LOCK_FILE_PROPERTY, "true");
            System.setProperty(
                    LogManager.LOG_TO_CONSOLE_PROPERTY, "false");
            System.setProperty(
                    Registry.LAZY_LOAD_ICONS_PROPERTY, "true");
            
            final File registryXml = new File(root, REGISTRY_XML);
            if (!registryXml.exists()) {
                new Registry().saveProductRegistry(
                        registryXml,
                        TrueFilter.INSTANCE,
                        true,
                        true,
                        true);
            }
            
            final File bundlesList = new File(root, BUNDLES_LIST);
            if (!bundlesList.exists()) {
                FileUtils.writeFile(bundlesList, "");
            }
            
            final File bundles = new File(root, BUNDLES);
            if (!bundles.exists()) {
                FileUtils.mkdirs(bundles);
            }
            
            final File components = new File(root, COMPONENTS);
            if (!components.exists()) {
                FileUtils.mkdirs(components);
            }
        } catch (FinalizationException e) {
            throw new ManagerException(e);
        } catch (IOException e) {
            throw new ManagerException(e);
        }
    }
    
    private Registry loadRegistry(
            final File root,
            final File tempUserDir,
            final Platform platform) throws ManagerException {
        try {
            final File registryXml =
                    new File(root, REGISTRY_XML);
            
            final Registry registry = new Registry();
            registry.setLocalDirectory(tempUserDir);
            registry.setFinishHandler(DummyFinishHandler.INSTANCE);
            registry.setTargetPlatform(platform);
            registry.loadProductRegistry(registryXml);
            
            return registry;
        } catch (InitializationException e) {
            throw new ManagerException(e);
        }
    }
    
    private void iterate(
            final Platform platform,
            final File root,
            final Registry registry,
            final Product[] combination,
            final int index,
            final List<Product> products,
            final int start) throws ManagerException {
        for (int i = start; i < products.size(); i++) {
            combination[index] = products.get(i);
            
            if (index == combination.length - 1) {
                for (Product product: products) {
                    product.setStatus(Status.NOT_INSTALLED);
                }
                for (Product product: combination) {
                    product.setStatus(Status.TO_BE_INSTALLED);
                }
                
                if (registry.getProductsToInstall().size() == combination.length) {
                    String[] components = new String[combination.length];
                    
                    for (int j = 0; j < combination.length; j++) {
                        components[j] = combination[j].getUid() + "," +
                                combination[j].getVersion().toString();
                    }
                    
                    createBundle(root, platform, components);
                }
            } else {
                iterate(platform, root, registry, combination, index + 1, products, i + 1);
            }
        }
    }
    
    private List<Product> getProducts(RegistryNode root) {
        final List<Product> list = new LinkedList<Product>();
        
        for (RegistryNode node: root.getChildren()) {
            if (node instanceof Product) {
                list.add((Product) node);
            }
            
            list.addAll(getProducts(node));
        }
        
        return list;
    }
    
    private List<Group> getGroups(RegistryNode root) {
        final List<Group> list = new LinkedList<Group>();
        
        for (RegistryNode node: root.getChildren()) {
            if (node instanceof Group) {
                list.add((Group) node);
            }
            
            list.addAll(getGroups(node));
        }
        
        return list;
    }
}
