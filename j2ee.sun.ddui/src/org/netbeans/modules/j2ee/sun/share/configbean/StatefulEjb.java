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

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;

/**
 *
 * @author  vkraemer
 */
public class StatefulEjb extends SessionEjb {

    /** Creates a new instance of SunONEStatelessEjbDConfigBean */	
    public StatefulEjb() {
    }

    /** Holds value of property availabilityEnabled. */
    private String availabilityEnabled;

    /** Holds value of property checkpointAtEndOfMethod. */
    private CheckpointAtEndOfMethod checkpointAtEndOfMethod;
    
    
    /** -----------------------------------------------------------------------
     *  Validation implementation
     */

    // relative xpaths (double as field id's)
    public static final String FIELD_STATEFUL_AVAILABILITY = "availability-enabled";
    
    protected void updateValidationFieldList() {
        super.updateValidationFieldList();

        validationFieldList.add(FIELD_STATEFUL_AVAILABILITY);
    }

    public boolean validateField(String fieldId) {
        boolean result = super.validateField(fieldId);
        
        Collection/*ValidationError*/ errors = new ArrayList();

        // !PW use visitor pattern to get rid of switch/if statement for validation
        //     field -- data member mapping.
        //
        // ValidationSupport can return multiple errors for a single field.  We only want
        // to display one error per field, so we'll pick the first error rather than adding
        // them all.  As the user fixes each error, the remainder will display until all of
        // them are handled.  (Hopefully the errors are generated in a nice order, e.g. 
        // check blank first, then content, etc.  If not, we may have to reconsider this.)
        //
        String absoluteFieldXpath = getAbsoluteXpath(fieldId);
        if(fieldId.equals(FIELD_STATEFUL_AVAILABILITY)) {
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    availabilityEnabled, absoluteFieldXpath, bundle.getString("LBL_Availability_Enabled"))); // NOI18N
        }

        boolean noErrors = true;
        Iterator errorIter = errors.iterator();

        while(errorIter.hasNext()) {
            ValidationError error = (ValidationError) errorIter.next();
            getMessageDB().updateError(error);

            if(Utils.notEmpty(error.getMessage())) {
                noErrors = false;
            }
        }

        // return true if there was no error added
        return noErrors || result;
    } 
    
    
    
    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
/*
	private HashMap statefulEjbFactoryMap;
	
	protected Map getXPathToFactoryMap() {
        if(statefulEjbFactoryMap == null) {
                statefulEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

                // add child DCB's specific to Stateful Session Beans
        }

        return statefulEjbFactoryMap;
    }
 */

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class StatefulEjbSnippet extends BaseEjb.BaseEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();
            String version = getAppServerVersion().getEjbJarVersionAsString();

            if(Utils.notEmpty(availabilityEnabled)) {
                try {
                    ejb.setAvailabilityEnabled(availabilityEnabled);
                } catch (VersionNotSupportedException ex) {
                }
            }

            if(checkpointAtEndOfMethod != null && checkpointAtEndOfMethod.sizeMethod() > 0) {
                try {
                    ejb.setCheckpointAtEndOfMethod((CheckpointAtEndOfMethod)checkpointAtEndOfMethod.cloneVersion(version));
                } catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e) {
                }
            }

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()) {
                return true;
            }

            if(Utils.notEmpty(availabilityEnabled)) {
                return true;
            }

            if(checkpointAtEndOfMethod != null && checkpointAtEndOfMethod.sizeMethod() > 0) {
                return true;
            }
            
            return false;
        }
    }


    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new StatefulEjbSnippet());
        return snippets;
    }


    protected void loadEjbProperties(Ejb savedEjb) {
        super.loadEjbProperties(savedEjb);

        try {
            availabilityEnabled =  savedEjb.getAvailabilityEnabled();
        } catch(VersionNotSupportedException e) {
        }

        try {
            CheckpointAtEndOfMethod cpem = savedEjb.getCheckpointAtEndOfMethod();
            if(cpem != null) {
                checkpointAtEndOfMethod = (CheckpointAtEndOfMethod) cpem.clone();
            }
        } catch(VersionNotSupportedException e) {
        }
    }

    protected void clearProperties() {
        super.clearProperties();
        
        availabilityEnabled = null;
        checkpointAtEndOfMethod = getConfig().getStorageFactory().createCheckpointAtEndOfMethod();
    }
    

    public String getAvailabilityEnabled() {
        return this.availabilityEnabled;
    }


    public void setAvailabilityEnabled(String availabilityEnabled) throws java.beans.PropertyVetoException {
        String oldAvailabilityEnabled = this.availabilityEnabled;                                           //NOI18N
        getVCS().fireVetoableChange("availabilityEnabled", oldAvailabilityEnabled, availabilityEnabled);    //NOI18N
        this.availabilityEnabled = availabilityEnabled;                                                     //NOI18N
        getPCS().firePropertyChange("availabilityEnabled", oldAvailabilityEnabled, availabilityEnabled);    //NOI18N
    }


    public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() {
            return this.checkpointAtEndOfMethod;
    }


    public void setCheckpointAtEndOfMethod(CheckpointAtEndOfMethod checkpointAtEndOfMethod) throws java.beans.PropertyVetoException {
            CheckpointAtEndOfMethod oldCheckpointAtEndOfMethod = this.checkpointAtEndOfMethod;
            getVCS().fireVetoableChange("checkpointAtEndOfMethod", oldCheckpointAtEndOfMethod, checkpointAtEndOfMethod);       //NOI18N
            this.checkpointAtEndOfMethod = checkpointAtEndOfMethod;
            getPCS().firePropertyChange("checkpointAtEndOfMethod", oldCheckpointAtEndOfMethod, checkpointAtEndOfMethod);   //NOI18N
    }


    public void addMethod(Method method){
        if(null == checkpointAtEndOfMethod){
            checkpointAtEndOfMethod = getConfig().getStorageFactory().createCheckpointAtEndOfMethod();
        }
        checkpointAtEndOfMethod.addMethod(method);
    }


    public void removeMethod(Method method){
        if(null != checkpointAtEndOfMethod){
            checkpointAtEndOfMethod.removeMethod(method);
        }
    }


    public String getHelpId() {
        return "AS_CFG_StatefulEjb";                                    //NOI18N
    }
}
