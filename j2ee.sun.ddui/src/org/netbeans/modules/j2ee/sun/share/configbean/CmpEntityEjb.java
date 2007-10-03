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
import java.util.List;

import javax.enterprise.deploy.model.DDBean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod;

import org.netbeans.modules.j2ee.sun.share.configbean.ConfigQuery;


/**
 *
 * @author  vkraemer
 */
public class CmpEntityEjb extends EntityEjb {
    
	/** Holds value of property cmp. */
	private Cmp cmp;

    /** Holds value of property flush-at-end-of-method. */
    private FlushAtEndOfMethod flushAtEndOfMethod;

    /** Holds value of property schema. */
    private String schema;
    
    /** Holds value of property tableName. */
    private String tableName;
    
    /** Holds value of property consistency. */
    private String consistency;
    
    /** Holds value of property secondaryTables. */

    /** Holds value of property beanName. */
    //private String beanName;
    
    /** Creates a new instance of CmpEntityEjb */
    public CmpEntityEjb() {
    }

    /** -----------------------------------------------------------------------
     *  Validation implementation
     */

    // relative xpaths (double as field id's)
    public static final String FIELD_CMP_MAPPINGPROPERTIES = "cmp/mapping-properties"; // NOI18N
    
    protected void updateValidationFieldList() {
        super.updateValidationFieldList();

        validationFieldList.add(FIELD_CMP_MAPPINGPROPERTIES);
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
        if(fieldId.equals(FIELD_CMP_MAPPINGPROPERTIES)) {
            String value = (cmp != null) ? cmp.getMappingProperties() : null;
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    value, absoluteFieldXpath, bundle.getString("LBL_Mapping_Properties"))); // NOI18N
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
    
    /** Getter for property schema.
     * @return Value of property schema.
     *
     */
    public String getSchema() {
        return this.schema;
    }
    
    /** Setter for property schema.
     * @param schema New value of property schema.
     *
     * @throws PropertyVetoException
     *
     */
    public void setSchema(String schema) throws java.beans.PropertyVetoException {
        String oldSchema = this.schema;
        getVCS().fireVetoableChange("schema", oldSchema, schema);
        this.schema = schema;
        getPCS().firePropertyChange("schema", oldSchema, schema);
    }
    
    /** Getter for property tableName.
     * @return Value of property tableName.
     *
     */
    public String getTableName() {
        return this.tableName;
    }
    
    /** Setter for property tableName.
     * @param tableName New value of property tableName.
     *
     * @throws PropertyVetoException
     *
     */
    public void setTableName(String tableName) throws java.beans.PropertyVetoException {
        String oldTableName = this.tableName;
        getVCS().fireVetoableChange("tableName", oldTableName, tableName);
        this.tableName = tableName;
        getPCS().firePropertyChange("tableName", oldTableName, tableName);
    }
    
    /** Getter for property consistency.
     * @return Value of property consistency.
     *
     */
    public String getConsistency() {
        return this.consistency;
    }
    
    /** Setter for property consistency.
     * @param consistency New value of property consistency.
     *
     * @throws PropertyVetoException
     *
     */
    public void setConsistency(String consistency) throws java.beans.PropertyVetoException {
        String oldConsistency = this.consistency;
        getVCS().fireVetoableChange("consistency", oldConsistency, consistency);
        this.consistency = consistency;
        getPCS().firePropertyChange("consistency", oldConsistency, consistency);
    }

    /** Getter for property beanName.
     * @return Value of property beanName.
     *
     */
    public String getBeanName() {
        return cleanDDBeanText(getDDBean());
    }

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class CmpEntityEjbSnippet extends EntityEjb.EntityEjbSnippet {
        
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();
            String version = getAppServerVersion().getEjbJarVersionAsString();

            Cmp c = getCmp();
            if(hasContent(c)) {
                ejb.setCmp((Cmp) c.cloneVersion(version));
            }

            try {
                FlushAtEndOfMethod feom = getFlushAtEndOfMethod();
                if(feom != null && feom.sizeMethod() > 0) {
                    ejb.setFlushAtEndOfMethod((FlushAtEndOfMethod) feom.cloneVersion(version));
                }            
            } catch (VersionNotSupportedException ex) {
            }            

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()) {
                return true;
            }

            if(hasContent(getCmp())) {
                return true;
            }

            FlushAtEndOfMethod feom = getFlushAtEndOfMethod();
            if(feom != null && feom.sizeMethod() > 0) {
                return true;
            }

            return false;
        }
    
        private boolean hasContent(Cmp c) {
            if(c == null) {
                return false;
            }

            if(Utils.notEmpty(c.getIsOneOneCmp()) ||
                    Utils.notEmpty(c.getMappingProperties())) {
                return true;
            }

            OneOneFinders finders = c.getOneOneFinders();
            if(finders != null && finders.sizeFinder() > 0) {
                return true;
            }

            try {
                PrefetchDisabled pd = c.getPrefetchDisabled();
                if(pd != null && pd.sizeQueryMethod() > 0) {
                    return true;
                }
            } catch (VersionNotSupportedException ex) {
            }

            return false;
        }    
    }
    
    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new CmpEntityEjbSnippet());

        // FIXME create snippet for sun-cmp-mappings.xml here as well.

        return snippets;
    }


    protected void loadEjbProperties(Ejb savedEjb) {
            super.loadEjbProperties(savedEjb);
        Cmp cmp = savedEjb.getCmp();
        if(null != cmp){
            this.cmp = cmp;
        }

        FlushAtEndOfMethod flushAtEndOfMethod = null;
        try{
            flushAtEndOfMethod = savedEjb.getFlushAtEndOfMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }

        if(null != flushAtEndOfMethod){
            this.flushAtEndOfMethod = flushAtEndOfMethod;
        }
    }
    
    protected void clearProperties() {
        super.clearProperties();
        StorageBeanFactory beanFactory = getConfig().getStorageFactory();        
        
        cmp = beanFactory.createCmp();
        flushAtEndOfMethod = beanFactory.createFlushAtEndOfMethod();
        schema = null;
        tableName = null;
        consistency = null;
    }

	
	/** Getter for property cmp.
	 * @return Value of property cmp.
	 *
	 */
	public Cmp getCmp() {
		return this.cmp;
	}


	/** Setter for property cmp.
	 * @param cmp New value of property cmp.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCmp(Cmp cmp) throws java.beans.PropertyVetoException {
		Cmp oldCmp = this.cmp;
		getVCS().fireVetoableChange("cmp", oldCmp, cmp);
		this.cmp = cmp;
		getPCS().firePropertyChange("cmp", oldCmp, cmp);
	}

        
    /** Getter for property flushAtEndOfMethod.
	 * @return Value of property flushAtEndOfMethod.
	 */
	public FlushAtEndOfMethod getFlushAtEndOfMethod() {
		return this.flushAtEndOfMethod;
	}


	/** Setter for property flushAtEndOfMethod.
	 * @param flushAtEndOfMethod New value of property flushAtEndOfMethod.
	 *
	 * @throws PropertyVetoException
	 */
	public void setFlushAtEndOfMethod(FlushAtEndOfMethod flushAtEndOfMethod) throws java.beans.PropertyVetoException {
		FlushAtEndOfMethod oldFlushAtEndOfMethod = this.flushAtEndOfMethod;
		getVCS().fireVetoableChange("flushAtEndOfMethod", oldFlushAtEndOfMethod, flushAtEndOfMethod);        //NOI18N
		this.flushAtEndOfMethod = flushAtEndOfMethod;
		getPCS().firePropertyChange("flushAtEndOfMethod", oldFlushAtEndOfMethod, flushAtEndOfMethod);   //NOI18N
	}


	//methods called by the customizer model
	public void addFinder(Finder finder){
		if(null == cmp){
			cmp = getConfig().getStorageFactory().createCmp();
		}
		OneOneFinders oneOneFinders = cmp.getOneOneFinders();
		if(null == oneOneFinders){
			oneOneFinders = cmp.newOneOneFinders();
			cmp.setOneOneFinders(oneOneFinders);
		}
		oneOneFinders.addFinder(finder);
	}


        public void addMethod(Method method){
/*            
            System.out.println("CmpEntityEjb addMethod ddMethod:" + method);                             //NOI18N
            System.out.println("CmpEntityEjb addMethod name :" + method.getMethodName() );               //NOI18N
            System.out.println("CmpEntityEjb addMethod interface :" + method.getMethodIntf() );          //NOI18N   
            System.out.println("CmpEntityEjb addMethod ejb name :" + method.getEjbName() );              //NOI18N
            System.out.println("CmpEntityEjb addMethod params :" + method.getMethodParams() );           //NOI18N   
*/
            if(null == flushAtEndOfMethod){
                flushAtEndOfMethod = getConfig().getStorageFactory().createFlushAtEndOfMethod();
            }
            flushAtEndOfMethod.addMethod(method);
	}


        public void addQueryMethod(QueryMethod queryMethod){
            try{
                if(null == cmp){
                    cmp = getConfig().getStorageFactory().createCmp();
                }

                PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                if(null == prefetchDisabled){
                    prefetchDisabled = cmp.newPrefetchDisabled();
                    cmp.setPrefetchDisabled(prefetchDisabled);
                }
                prefetchDisabled.addQueryMethod(queryMethod);
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
	}


	public void removeFinder(Finder finder){
		if(null != cmp){
			OneOneFinders oneOneFinders = cmp.getOneOneFinders();
			if(null != oneOneFinders){
				oneOneFinders.removeFinder(finder);
				try {
					if(oneOneFinders.sizeFinder() < 1){
						setCmp(null);
					}
				}catch(java.beans.PropertyVetoException ex){
				}
			}
		}
	}


	public void removeMethod(Method method){
		if(null != flushAtEndOfMethod){
                        flushAtEndOfMethod.removeMethod(method);
		}
	}


	public void removeQueryMethod(QueryMethod queryMethod){
            try{
                if(null != cmp){
                    PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                    if(null != prefetchDisabled){
                        prefetchDisabled.removeQueryMethod(queryMethod);
                    }
                }
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
	}


        //List of all the finder methods of cmp bean
        public List getFinderMethods(){
            ArrayList methods = new ArrayList();
            DDBean ddBean = getDDBean();

            //xpath - ejb-jar/enterprise-beans/entity
            DDBean[] childBeans = ddBean.getChildBean("./query");            //NOI18N
            ConfigQuery.MethodData methodData = null;
            DDBean queryMethods[];
            DDBean queryMethod;
            DDBean methodNameBean;
            String methodName;
            DDBean methodParams;
            DDBean methodParam[];
            for(int i=0; i<childBeans.length; i++){
                queryMethods = childBeans[i].getChildBean("./query-method"); //NOI18N
                if(queryMethods.length > 0){
                    queryMethod = queryMethods[0]; 
                    methodNameBean = queryMethod.getChildBean("./method-name")[0]; //NOI18N
                    methodName = methodNameBean.getText();
                    if((methodName != null) && (methodName.length() > 0)){
                        methodParams = queryMethod.getChildBean("./method-params")[0]; //NOI18N
                        methodParam = methodParams.getChildBean("./method-param"); //NOI18N
                        ArrayList params = new ArrayList();
                        if(methodParam != null){
                           for(int j=0; j<methodParam.length; j++){
                               params.add(methodParam[j].getText());
                           } 
                        }
                        methodData = new ConfigQuery.MethodData(methodName, params);
                    }
                }
                methods.add((Object)methodData);
            }
            return methods;
        }


        //List of all the QueryMethod elements(elements from DD)
        public List getPrefetchedMethods(){
            List prefetchedMethodList = new ArrayList();

            try{
                if(cmp != null){
                    PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                    if(prefetchDisabled != null){
                        QueryMethod[] queryMethods = prefetchDisabled.getQueryMethod();
                        if(queryMethods != null){
                            for(int i=0; i<queryMethods.length; i++){
                                prefetchedMethodList.add(queryMethods[i]);
                            }
                        }
                    }
                }
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
            return prefetchedMethodList;
        }


        public String getHelpId() {
		return "AS_CFG_CmpEntityEjb";                           //NOI18N
	}
}

