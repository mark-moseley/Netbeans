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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.app.Web;


/** This bean is contained by an AppRoot and represents a reference to a web
 *  module contained in that application
 *
 *  @auther Peter Williams
 */
public class WebAppRef extends BaseModuleRef {

	/** -----------------------------------------------------------------------
	 * Initialization
	 */
	
	/** Creates new WebAppRef 
	 */
	public WebAppRef() {
	}
	
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);

		contextRootDD = getNameDD("context-root");
		loadFromPlanFile(getConfig());
	}
	
	/** Called from BaseModuleRef.init() to get the correct module URI field
	 *  for the reference object.
	 */
	protected void initModuleUri(DDBean dDBean) {
		DDBean[] uriBeans = dDBean.getChildBean("web-uri");
		if(uriBeans.length > 0) {
			setModuleUri(uriBeans[0]);
		} else {
			setModuleUri(null);
		}
	}
	
    /** Getter for customizer title fragment property
     * @return String fragment for use in customizer title
     *
     */
    public String getTitleFragment() {
        return bundle.getString("LBL_WebTitleFragment"); // NOI18N
    }
    
	/** -----------------------------------------------------------------------
	 * JSR-88 Interface support
	 */
	
	// All JSR-88 methods inherited from base
	
	/** -----------------------------------------------------------------------
	 * Properties
	 */

	/** Holds value of property webUri. */
//	private DDBean webUri = null;
	
	/** Holds value of property contextRoot. */
	private DDBean contextRootDD = null;
	private String contextRoot;
	
	/** Getter for property webURI.
	 * @return Value of property webURI.
	 *
	 */
	public String getWebUri() {
		return getModuleUri();
	}
	
	/** Getter for property contextRoot.
	 * @return Value of property contextRoot.
	 *
	 */
	public String getContextRoot() {
		String result = null;
		
		/* if a contextRoot for this module has not been set, get the context
		 * root from the application.xml, otherwise, use the explicit setting
		 */
		if(contextRoot == null || contextRoot.length() == 0 && contextRootDD != null) {
			result = contextRootDD.getText();
		} else {
			result = contextRoot;
		}
		
		return result;
	}
	
    /** Setter for property contextRoot.
     * @param newContextRoot New value of property contextRoot.
     * @throws PropertyVetoException if the property change is vetoed
     */
    public void setContextRoot(String newContextRoot) throws java.beans.PropertyVetoException {
        if (newContextRoot!=null){
            newContextRoot = newContextRoot.replace (' ', '_'); //NOI18N
        }
        if (newContextRoot!=null){ //see bug 56280
            try{
                StringBuilder resultBuilder = new StringBuilder(newContextRoot.length()+25);
                String s[] = newContextRoot.split("/");
                for (int i=0;i<s.length;i++){
                    resultBuilder.append(java.net.URLEncoder.encode(s[i], "UTF-8"));
                    if (i!=s.length -1)
                        resultBuilder.append('/');
                }
                newContextRoot= resultBuilder.toString();
            }
            catch (Exception e){

            }
        }
        String oldContextRoot = contextRoot;
        getVCS().fireVetoableChange("contextRoot", oldContextRoot, newContextRoot);
        contextRoot = newContextRoot;
        getPCS().firePropertyChange("contextRoot", oldContextRoot, contextRoot);		 
    }


        public String getHelpId() {
            return "AS_CFG_WebAppRef";                                  //NOI18N
        }


	/** -----------------------------------------------------------------------
	 * Persistance support
	 */
	 Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			
			public CommonDDBean getDDSnippet() {
				Web web = getConfig().getStorageFactory().createWeb();
				
				web.setWebUri(getWebUri());
				web.setContextRoot(getContextRoot());
				
				return web;
			}
			
            public boolean hasDDSnippet() {
                if(Utils.notEmpty(contextRoot)) {
                    return true;
                }

                return false;
            }
             
			public String getPropertyName() {
				return SunApplication.WEB;
			}			
		};
		
		snippets.add(snipOne);
		return snippets;
	}

	private class WebAppRefFinder implements ConfigFinder {
		public Object find(Object obj) {
			Web result = null;
			String webUri = getWebUri();
			
			if(obj instanceof SunApplication && webUri != null) {
				SunApplication sa = (SunApplication) obj;
				Web [] webModules = sa.getWeb();
				
				for(int i = 0; i < webModules.length; i++) {
					if(webUri.compareTo(webModules[i].getWebUri()) == 0) {
						result = webModules[i];
						break;
					}
				}
			}
			
			return result;
		}
	}
		
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		
		Web beanGraph = (Web) config.getBeans(
			uriText, constructFileName(), getParser(), new WebAppRefFinder());
		
		clearProperties();
		
		if(beanGraph != null) {
			String cr = beanGraph.getContextRoot();
			if(Utils.notEmpty(cr) && cr.compareTo(getContextRoot()) != 0) {
				contextRoot = cr;
			}
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}	
	
	protected void clearProperties() {
		contextRoot = null;
	}
	
	protected void setDefaultProperties() {
		// no defaults
	}	
}
