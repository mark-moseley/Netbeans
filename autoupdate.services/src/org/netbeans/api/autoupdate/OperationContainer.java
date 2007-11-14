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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.autoupdate;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;

/**
 * An object that keeps requests for operations upon instances of <code>UpdateEelement</code>
 * (like install, uninstall, update, enable, disable), provides checks whether 
 * chosen operation is allowed (e.g. already installed plugin cannot be scheduled for install again), 
 * provides information which additonal plugins are
 * required and so on.
 * <p>
 * Typical scenario how to use:
 * <ul>
 * <li>use one of factory methods for creating instance of <code>OperationContainer</code> 
 * for chosen operation: {@link #createForInstall}, {@link #createForUninstall}, 
 * {@link #createForUpdate}, {@link #createForEnable},{@link #createForDisable}</li>
 * <li>add instances of <code>UpdateElement</code> (see {@link OperationContainer#add})</li>
 * <li>check if additional requiered instances of <code>UpdateElement</code> are needed 
 * ({@link OperationInfo#getRequiredElements}), 
 * if so then these requiered instances should be also added</li>
 * <li>next can be tested for broken dependencies ({@link OperationInfo#getBrokenDependencies}) </li>
 * <li>call method {@link #getSupport} to get either {@link InstallSupport} or {@link OperationSupport} 
 * that can be used for performing operation</li>
 * 
 * </ul>
 * Code example:
 * <pre style="background-color: rgb(255, 255, 153);"> 
 * UpdateElement element = ...;
 * OperationContainer&lt;OperationSupport&gt; container = createForDirectInstall();
 * OperationInfo&lt;Support&gt; info = container.add(element);
 * Set&lt;UpdateElement&gt; required = info.getRequiredElements();
 * container.add(required);
 * OperationSupport support = container.getSupport();
 * support.doOperation(null);
 * </pre>
 * </p>
 * @param <Support> the type of support for performing chosen operation like 
 * {@link OperationSupport} or {@link InstallSupport}
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationContainer<Support> {
    /**
     * The factory method to construct instance of <code>OperationContainer</code> for install operation
     * @return newly constructed instance of <code>OperationContainer</code> for install operation
     */
    public static OperationContainer<InstallSupport> createForInstall() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForInstall(), new InstallSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for install operation
     * @return newly constructed instance of <code>OperationContainer</code> for install operation
     */
    public static OperationContainer<OperationSupport> createForDirectInstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForDirectInstall(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }    
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for update operation
     * @return newly constructed instance of <code>OperationContainer</code> for update operation
     */    
    public static OperationContainer<InstallSupport> createForUpdate() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForUpdate(), new InstallSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for update operation
     * @return newly constructed instance of <code>OperationContainer</code> for update operation
     */    
    public static OperationContainer<OperationSupport> createForDirectUpdate() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectUpdate(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }    
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstall operation
     * @return newly constructed instance of <code>OperationContainer</code> for uninstall operation
     */        
    public static OperationContainer<OperationSupport> createForUninstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForUninstall(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstall operation
     * @return newly constructed instance of <code>OperationContainer</code> for uninstall operation
     */            
    public static OperationContainer<OperationSupport> createForDirectUninstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectUninstall(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for enable operation
     * @return newly constructed instance of <code>OperationContainer</code> for enable operation
     */            
    public static OperationContainer<OperationSupport> createForEnable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForEnable(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;        
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for disable operation
     * @return newly constructed instance of <code>OperationContainer</code> for disable operation
     */                
    public static OperationContainer<OperationSupport> createForDisable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDisable(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for disable operation
     * @return newly constructed instance of <code>OperationContainer</code> for disable operation
     */                    
    public static OperationContainer<OperationSupport> createForDirectDisable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectDisable(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for installation of custom compomnent
     * @return newly constructed instance of <code>OperationContainer</code> for installation of custom compomnent
     */                    
    public static OperationContainer<OperationSupport> createForCustomInstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForInstallNativeComponent (), new OperationSupport());
        retval.getSupport ().setContainer (retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstallation of custom compomnent
     * @return newly constructed instance of <code>OperationContainer</code> for uninstallation of custom compomnent
     */                        
    public static OperationContainer<OperationSupport> createForCustomUninstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForUninstallNativeComponent (), new OperationSupport());
        retval.getSupport ().setContainer (retval);
        return retval;
    }
    
    /**
     * @return either {@link OperationSupport} or {@link InstallSupport} depending on type parameter of <code>OperationContainer&lt;Support&gt;</code>      
     * <br><p>See the difference between {@link #createForInstall} and {@link #createForDirectInstall} for example</p>
     */                        
    public Support getSupport() {
        if (!init) {
            init = true;
            return support;
        }
        return (listAll().size() > 0 && listInvalid().size() == 0) ? support : null;
    }
    
    /**
     * Check if <code>updateElement</code> can be added ({@link #add})
     * @param updateUnit
     * @param updateElement to be inserted.
     * @return <tt>true</tt> if chosen operation upon <code>updateElement</code> is allowed
     */
    public boolean canBeAdded(UpdateUnit updateUnit, UpdateElement updateElement) {
        return impl.isValid(updateUnit, updateElement);
    }
    
    /**
     * Adds all <code>elems</code>
     * @param elems to be inserted.
     */
    public void add(Collection<UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException("Cannot add null value.");
        for (UpdateElement el : elems) {
            add(el);
        }
    }
    
    /**
     * Adds all <code>elems</code>
     * @param elems to be inserted.
     */
    public void add(Map<UpdateUnit, UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (Map.Entry<UpdateUnit, UpdateElement> entry : elems.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
    
    
    /**
     * Adds <code>updateElement</code>
     * @param updateUnit
     * @param updateElement
     * @return instance of {@link OperationInfo}&lt;Support&gt; or
     * <code>null</code> if the <code>UpdateElement</code> is already present in the container
     */
    public OperationInfo<Support> add(UpdateUnit updateUnit,UpdateElement updateElement) {
        //UpdateUnit updateUnit = UpdateManagerImpl.getInstance().getUpdateUnit(updateElement.getCodeName());
        return impl.add (updateUnit, updateElement);
    }
    
    /**
     * Adds <code>updateElement</code>
     * @param updateElement
     * @return instance of {@link OperationInfo}&lt;Support&gt; or
     * <code>null</code> if the <code>UpdateElement</code> is already present in the container
     */
    public OperationInfo<Support> add(UpdateElement updateElement) {
        UpdateUnit updateUnit = updateElement.getUpdateUnit ();
        return impl.add (updateUnit, updateElement);
    }
    
    
    /**
     * Removes all <code>elems</code>
     * @param elems
     */
    public void remove(Collection<UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (UpdateElement el : elems) {
            remove (el);
        }
    }        
    
    /**
     * Removes <code>updateElement</code>
     * @param updateElement
     * @return <tt>true</tt> if succesfully added
     */
    public boolean remove(UpdateElement updateElement) {
        return impl.remove(updateElement);
    }
    
    
    /**
     * @param updateElement
     * @return <tt>true</tt> if this instance of <code>OperationContainer</code> 
     * contains the specified <code>updateElement</code>.     
     */
    public boolean contains(UpdateElement updateElement) {
        return impl.contains(updateElement);
    }

    /**
     * @return all instances of {@link OperationInfo}&lt;Support&gt; from this 
     * instance of <code>OperationContainer</code>
     */
    public List<OperationInfo<Support>> listAll() {
        return impl.listAllWithPossibleEager ();
    }
    
    /**
     * @return all invalid instances of {@link OperationInfo}&lt;Support&gt; from this 
     * instance of <code>OperationContainer</code>    
     */
    public List<OperationInfo<Support>> listInvalid() {
        return impl.listInvalid ();
    }

    
    /**
     * Removes <code>op</code>
     * @param op
     */
    public void remove(OperationInfo<Support> op) {
        impl.remove (op);
    }
    
    
    /**
     * Removes all content
     */
    public void removeAll() {
        impl.removeAll ();
    }
    
    /**
     * Provides additional information
     * @param Support the type of support for performing chosen operation like 
     */
    public static final class OperationInfo<Support> {
        OperationContainerImpl<Support>.OperationInfoImpl<Support> impl;
        
        OperationInfo (OperationContainerImpl<Support>.OperationInfoImpl<Support> impl) {
            this.impl = impl;
        }
        
        public UpdateElement getUpdateElement() {return impl.getUpdateElement();}
        public UpdateUnit getUpdateUnit() {return impl.getUpdateUnit();}        
        /**
         * @return all requiered elements. Each of them represented by instance of <code>UpdateElement</code>
         */
        public Set<UpdateElement> getRequiredElements(){return new LinkedHashSet<UpdateElement> (impl.getRequiredElements());}
        
        /**
         * @return all broken dependencies. Each of them represented by the code name of the module 
         * @see ModuleInfo#getCodeNameBase()
         */
        public Set<String> getBrokenDependencies(){return impl.getBrokenDependencies();}
    }

    //end of API - next just impl details
    /** Creates a new instance of OperationContainer */
    private  OperationContainer(OperationContainerImpl<Support> impl, Support t) {
        this.impl = impl;
        this.support = t;
        impl.setOperationContainer (this);
    }
    
    OperationContainerImpl<Support> impl;
    private Support support;
    private boolean init = false;
}