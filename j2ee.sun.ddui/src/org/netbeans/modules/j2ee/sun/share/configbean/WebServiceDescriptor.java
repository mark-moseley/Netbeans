/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;

import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;

/** This DConfigBean is a child of WebServices.
 *
 * Property structures from sun-web-app.xml or sun-ejb-jar.xml handled by this bean
 *
 *		webservice-description : WebserviceDescription
 *			webservice-description-name : String
 *			wsdl-publish-location : String?
 *		webservice-endpoint : WebserviceEndpoint[0,n]
 *			port-component-name : String
 *			endpoint-address-uri : String?
 *			login-config : LoginConfig?
 *				auth-method : String
 *			transport-guarantee : String?
 *			service-qname : ServiceQname?	[not used - set by server]
 *				namespaceURI : String		[not used - set by server]
 *				localpart : String			[not used - set by server]
 *			tie-class : String?				[not used - set by server]
 *			servlet-impl-class : String?	[not used - set by server]
 *
 *
 * @author  Peter Williams
 */
public class WebServiceDescriptor extends Base {

	/** property event names
	 */
	public static final String WEBSERVICE_DESCRIPTION_NAME = "webserviceDescriptionName"; // NOI18N
	public static final String WEBSERVICE_ENDPOINT = "webserviceEndpoint"; // NOI18N
	public static final String COMPONENT_LINK_NAME = "componentLinkName"; // NOI18N


	/** DDBean that refers to "webservice-description-name" child of bound DDBean. */
	private DDBean webServiceDescriptionNameDD;

	/** Holds value of property wsdlPublishLocation. */
	private String wsdlPublishLocation;

    /** Hold a map of DDBean [portComponent] -> web service endpoints. */
    private Map webServiceEndpointMap;

    /** Differentiates Servlet vs Ejb webservice support */
    private EndpointHelper helper;


    /** Creates a new instance of WebServiceDescriptor */
	public WebServiceDescriptor() {
		setDescriptorElement(bundle.getString("BDN_WebServiceDescriptor")); // NOI18N
	}

	/** Override init to enable grouping support for this bean and load name
	 *  field from related DDBean.
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);

// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);

        BaseRoot masterRoot = getConfig().getMasterDCBRoot();
        if(masterRoot instanceof WebAppRoot) {
            helper = servletHelper;
        } else if(masterRoot instanceof EjbJarRoot) {
            helper = ejbHelper;
        } else {
            throw new ConfigurationException("Unexpected master DConfigBean type: " + masterRoot); // NOI18N
        }

        dDBean.addXpathListener(dDBean.getXpath(), this);
		webServiceDescriptionNameDD = getNameDD("webservice-description-name"); // NOI18N

		loadFromPlanFile(getConfig());
	}

	protected String getComponentName() {
		return getWebServiceDescriptionName();
	}

	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_WebServiceDescriptor";
	}

	/** Getter for property webservice-description-name.
	 * @return Value of property webservice-description-name.
	 *
	 */
	public String getWebServiceDescriptionName() {
		return cleanDDBeanText(webServiceDescriptionNameDD);
	}

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    Collection getSnippets() {
        Collection snippets = new ArrayList();

        Snippet snipOne = new DefaultSnippet() {
            public CommonDDBean getDDSnippet() {
                // Add web service description entry.
                WebserviceDescription wsDesc = StorageBeanFactory.getDefault().createWebserviceDescription();
                wsDesc.setWebserviceDescriptionName(getWebServiceDescriptionName());
                wsDesc.setWsdlPublishLocation(getWsdlPublishLocation());
                return wsDesc;
            }

            public boolean hasDDSnippet() {
				if(wsdlPublishLocation != null && wsdlPublishLocation.length() > 0) {
					return true;
				}

				return false;
            }

            public String getPropertyName() {
                return SunWebApp.WEBSERVICE_DESCRIPTION;
            }

            public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
                CommonDDBean newBean = getDDSnippet();
                if(newBean != null) {
                    if(ddParent != null) {
                        helper.addWebServiceDescription(ddParent, newBean);
                    }
                }
                return newBean;
            }

        };

        Snippet snipTwo = new DefaultSnippet() {
            public CommonDDBean getDDSnippet() {
                // The contract for getDDSnippet() is to NEVER return null.  In this
                // instance however, a true snippet is not created.  Instead, the
                // implementation of mergeIntoRovingDD() inserts the various endpoint
                // fragments into the correct places directly.  In fact, it is quite
                // possible this method is never called.  The result certainly should
                // never be used.
                return null;
            }

            public boolean hasDDSnippet() {
				if(webServiceEndpointMap != null && webServiceEndpointMap.size() > 0) {
					return true;
				}

				return false;
            }

            public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
                // For each endpoint, locate the host (servlet or ejb) it is bound
                // to and add it to that host's endpoint table.
                RootInterface root = (RootInterface) ddParent;
                Iterator iter = webServiceEndpointMap.entrySet().iterator();

                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    DDBean portComponent = (DDBean) entry.getKey();
                    String linkName = getComponentLinkName(portComponent);
                    WebserviceEndpoint endpoint = (WebserviceEndpoint) entry.getValue();

                    boolean endpointAdded = false;
                    CommonDDBean [] hosts = helper.getEndpointHosts(root);
                    for(int i = 0; i < hosts.length; i++) {
                        String hostName = (String) hosts[i].getValue(helper.getHostNameProperty());
                        if(hostName != null && hostName.equals(linkName)) {
                            hosts[i].addValue(helper.getEndpointProperty(), endpoint.clone());
                            endpointAdded = true;
                            break;
                        }
                    }

                    if(!endpointAdded) {
                        CommonDDBean newBean = helper.createNewHost();
                        newBean.setValue(helper.getHostNameProperty(), linkName);
                        newBean.addValue(helper.getEndpointProperty(), endpoint.clone());
                        helper.addEndpointHost(root, newBean);
                    }
                }

                return ddParent;
            }

            public String getPropertyName() {
                return helper.getEndpointProperty();
            }
        };

        snippets.add(snipOne);
        snippets.add(snipTwo);
        return snippets;
	}

	protected class WebServiceDescriptorFinder implements ConfigFinder {

		private String wsDescName;

		public WebServiceDescriptorFinder(String beanName) {
			this.wsDescName = beanName;
		}

		public Object find(Object obj) {
			Object result = null;
			CommonDDBean root = (CommonDDBean) obj;
            CommonDDBean [] descriptions = helper.getWebServiceDescriptions(root);

			for(int i = 0; i < descriptions.length; i++) {
                String name = (String) descriptions[i].getValue(WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME);
                if(wsDescName.equals(name)) {
					result = descriptions[i];
					break;
				}
			}

			return result;
		}
	}

	private class RootFinder implements ConfigFinder {
		public Object find(Object obj) {
			RootInterface result = null;

			if(obj instanceof SunWebApp || obj instanceof SunEjbJar) {
				result = (RootInterface) obj;
			}

			return result;
		}
	}

	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

        RootInterface root = (RootInterface) config.getBeans(uriText,
            constructFileName(), getParser(), new RootFinder());

		WebserviceDescription descGraph = (WebserviceDescription) config.getBeans(uriText,
            constructFileName(), getParser(), new WebServiceDescriptorFinder(getWebServiceDescriptionName()));

        clearProperties();

        // Default endpoint URI's are set in this call.
        Map tmpEndpointMap = getEndpointMap();

		if(descGraph != null) {
            wsdlPublishLocation = descGraph.getWsdlPublishLocation();
        }

        if(root != null) {
            // Load all endpoints that already have defined values.
            CommonDDBean [] hosts = helper.getEndpointHosts(root);
            if(hosts != null) {
                for(int i = 0; i < hosts.length; i++) {
                    String hostName = (String) hosts[i].getValue(helper.getHostNameProperty());
                    WebserviceEndpoint [] definedEndpoints = (WebserviceEndpoint []) hosts[i].getValues(helper.getEndpointProperty());

                    for(int j = 0; j < definedEndpoints.length; j++) {
                        DDBean key = findEndpointInMap(hostName, definedEndpoints[j].getPortComponentName(), tmpEndpointMap);
                        if(key != null) {
                            // This end point is still valid and has data that has been previously saved.
                            tmpEndpointMap.remove(key);
                            webServiceEndpointMap.put(key, definedEndpoints[j].clone());
                        }
                    }
                }
            }
        }
        
        // This is section is the equivalent of setDefaultProperties() in other
        // DConfigBean implementations.
        // Default URI's for any new, or otherwise undefined endpoints were initially
        // set above in getEndpointMap() and are transferred to the storage map here.
        //
        if(tmpEndpointMap.size() > 0) {
            // handle defaults for the remaining tmp endpoints.
            Iterator iter = tmpEndpointMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                webServiceEndpointMap.put(entry.getKey(), entry.getValue());
            }
            
            // Mark master DConfigBean as dirty so we force a save of the new default
            // values.  We can't just mark this bean dirty because it is still being
            // constructed and doesn't have any listeners attached yet.
            config.getMasterDCBRoot().setDirty();
        }
        
        
        // Do we want to do anything special for default values, or simply fill in
        // the entries + port component name all the time?

		return (descGraph != null || root != null);
	}

    /** Finds the first endpoint in the map, if any, with the given port name
     *  and servlet-link or ejb-link and returns the key that will allow this
     *  entry to be retrieved.  If this proves to be a performance bottleneck,
     *  we may have to have an additional index into the map.
     */
    private DDBean findEndpointInMap(String linkName, String portComponentName, Map endpointMap) {
        DDBean key = null;

        if(Utils.notEmpty(linkName) && Utils.notEmpty(portComponentName)) {
            Iterator iter = endpointMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                DDBean portComponent = (DDBean) entry.getKey();

                if(portComponentName.equals(getPortComponentName(portComponent)) &&
                        linkName.equals(getComponentLinkName(portComponent))) {
                    key = portComponent;
                    break;
                }
            }
        }

        return key;
    }

    /** Returns a map between servlet/port pairs and endpoints, with the port
     *  name in each endpoint pre-filled.
     */
    private Map getEndpointMap() {
        HashMap endpointMap = new HashMap();

        // The list of ports in this service
        DDBean [] portComponents = getDDBean().getChildBean("port-component"); // NOI18N
        for(int i = 0; i < portComponents.length; i++) {
            String portComponentName = getPortComponentName(portComponents[i]);
            WebserviceEndpoint endpoint = StorageBeanFactory.getDefault().createWebserviceEndpoint();

            if(Utils.notEmpty(portComponentName)) {
                endpoint.setPortComponentName(portComponentName);
                 // This where default endpoints get set.
                endpoint.setEndpointAddressUri(helper.getUriPrefix() + portComponentName);
            }

            endpointMap.put(portComponents[i], endpoint);
        }

        return endpointMap;
    }

    private String getPortComponentName(DDBean portComponent) {
        return getChildBeanText(portComponent, "port-component-name");
    }

    private String getComponentLinkName(DDBean portComponent) {
        return getChildBeanText(portComponent, "service-impl-bean/" + helper.getLinkXpath());
    }

    protected String getChildBeanText(DDBean parent, String childXpath) {
        String childText = null;

		DDBean[] beans = parent.getChildBean(childXpath);
		DDBean childDD = null;
		if(beans.length >= 1) {
			// Found the DDBean we want.
			childDD = beans[0];
		}

        if(childDD != null) {
            childText = childDD.getText();
            if(childText != null) {
                childText = childText.trim();
            }
        }

		return childText;
    }

	protected void clearProperties() {
		wsdlPublishLocation = null;
		webServiceEndpointMap = new HashMap();
	}

    protected String constructFileName() {
        // Delegate to parent, which in turn delegates to master DCB root.
        return getParent().constructFileName();
    }

	/* ------------------------------------------------------------------------
	 * Event handling support
	 */

	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);
//        dumpNotification("notifyDDChange", xpathEvent);

		DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

		if(eventBean == webServiceDescriptionNameDD) {
			// name changed...
			getPCS().firePropertyChange(WEBSERVICE_DESCRIPTION_NAME, GenericOldValue, getWebServiceDescriptionName());
			getPCS().firePropertyChange(DISPLAY_NAME, GenericOldValue, getDisplayName());
		} else if(xpath.endsWith("port-component-name")) {
            // port-component-name changed
            DDBean [] parents = eventBean.getChildBean("..");
            if(parents != null && parents.length == 1) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) webServiceEndpointMap.get(parents[0]);
                if(endpoint != null) {
                    updateEndpoint(endpoint, eventBean);
                    getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                }
            }
        } else if(xpath.endsWith(helper.getLinkXpath())) {
            DDBean [] parents = eventBean.getChildBean("../..");
            if(parents != null && parents.length == 1) {
                if(webServiceEndpointMap.get(parents[0]) != null) {
                    getPCS().firePropertyChange(COMPONENT_LINK_NAME, GenericOldValue, parents[0]);
                }
            }
        }
	}

    private void updateEndpoint(WebserviceEndpoint endpoint, DDBean portComponentNameDD) {
        String oldPortComponentName = endpoint.getPortComponentName();
        String newPortComponentName = portComponentNameDD.getText();
        if(newPortComponentName != null) {
            newPortComponentName = newPortComponentName.trim();
        }

        endpoint.setPortComponentName(newPortComponentName);

        String oldEndpointUri = endpoint.getEndpointAddressUri();
        if((oldPortComponentName != null && oldPortComponentName.equals(oldEndpointUri)) ||
                (oldPortComponentName == null && oldEndpointUri == null)) {
            endpoint.setEndpointAddressUri(newPortComponentName);
        }
    }

    /** A DDBean has been added or removed (or changed, but we handle change events
     *  in notifyDDChange()).
     */
    public void fireXpathEvent(XpathEvent xpathEvent) {
        super.fireXpathEvent(xpathEvent);
//        dumpNotification("fireXpathEvent", xpathEvent);

        DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

        if(xpath.endsWith("port-component")) {
            if(xpathEvent.isAddEvent()) {
                try {
                    addWebServiceEndpoint(eventBean);
                } catch(java.beans.PropertyVetoException ex) {
                    // suppress for now.
                }
            } else if(xpathEvent.isRemoveEvent()) {
                try {
                    removeWebServiceEndpoint(eventBean);
                } catch(java.beans.PropertyVetoException ex) {
                    // suppress for now.
                }
            }
        } else if(xpath.endsWith("port-component-name")) {
            DDBean [] parents = eventBean.getChildBean("..");
            if(parents != null && parents.length == 1) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) webServiceEndpointMap.get(parents[0]);
                if(endpoint != null) {
                    if(xpathEvent.isAddEvent()) {
                        updateEndpoint(endpoint, eventBean);
                        getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                    } else if(xpathEvent.isRemoveEvent()) {
                        endpoint.setPortComponentName("");
                        endpoint.setEndpointAddressUri("");
                        getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                    }
                }
            }
        }
    }

	/* ------------------------------------------------------------------------
	 * Property support
	 */

    /** Getter for property wsdlPublishLocation.
	 * @return Value of property wsdlPublishLocation.
	 */
	public String getWsdlPublishLocation() {
		return this.wsdlPublishLocation;
	}

	/** Setter for property wsdlPublishLocation.
	 * @param newWsdlPublishLocation New value of property wsdlPublishLocation.
	 *
	 * @throws PropertyVetoException
	 */
	public void setWsdlPublishLocation(String newWsdlPublishLocation) throws java.beans.PropertyVetoException {
		String oldWsdlPublishLocation = this.wsdlPublishLocation;
		getVCS().fireVetoableChange("wsdlPublishLocation", oldWsdlPublishLocation, newWsdlPublishLocation);
		this.wsdlPublishLocation = newWsdlPublishLocation;
		getPCS().firePropertyChange("wsdlPublishLocation", oldWsdlPublishLocation, wsdlPublishLocation);
	}

	/** Getter for property webServiceEndpoint.
	 * @return Value of property webServiceEndpoint.
	 *
	 */
	public List getWebServiceEndpoints() {
        List result = new ArrayList(webServiceEndpointMap.size());
        Iterator iter = webServiceEndpointMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.add(entry.getValue());
        }
        return result;
	}

	public WebserviceEndpoint getWebServiceEndpoint(int index) {
        List endpoints = getWebServiceEndpoints();
        return (WebserviceEndpoint) endpoints.get(index);
	}

	/** Setter for property webServiceEndpoint.
	 * @param webServiceEndpoint New value of property webServiceEndpoint.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setWebServiceEndpoints(List newWebServiceEndpoints) throws java.beans.PropertyVetoException {
        Map oldWebServiceEndpointMap = webServiceEndpointMap;
        getVCS().fireVetoableChange("webServiceEndpointMap", oldWebServiceEndpointMap, newWebServiceEndpoints);	// NOI18N
        webServiceEndpointMap = new HashMap();
        if(newWebServiceEndpoints != null) {
            Iterator iter = newWebServiceEndpoints.iterator();
            while(iter.hasNext()) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) iter.next();
                if(endpoint != null) {
                    DDBean key = createKey(endpoint.getPortComponentName());
                    if(key != null) {
                        webServiceEndpointMap.put(key, endpoint);
                    }
                }
            }
        }
        getPCS().firePropertyChange("webServiceEndpointMap", oldWebServiceEndpointMap, webServiceEndpointMap);	// NOI18N
    }

	public void addWebServiceEndpoint(WebserviceEndpoint newWebServiceEndpoint) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, null, newWebServiceEndpoint);
		if(webServiceEndpointMap == null) {
			webServiceEndpointMap = new HashMap();
		}
        DDBean key = createKey(newWebServiceEndpoint.getPortComponentName());
        if(key != null) {
            webServiceEndpointMap.put(key, newWebServiceEndpoint);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, null, newWebServiceEndpoint );
        }
	}

	public void removeWebServiceEndpoint(WebserviceEndpoint oldWebServiceEndpoint) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, oldWebServiceEndpoint, null);
        DDBean key = createKey(oldWebServiceEndpoint.getPortComponentName());
        if(key != null && webServiceEndpointMap.get(key) != null) {
            webServiceEndpointMap.remove(key);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, oldWebServiceEndpoint, null );
        }
	}

    public void addWebServiceEndpoint(DDBean portComponentDD) throws java.beans.PropertyVetoException {
        WebserviceEndpoint newWebserviceEndpoint = StorageBeanFactory.getDefault().createWebserviceEndpoint();
        String portComponentName = getChildBeanText(portComponentDD, "port-component-name"); // NOI81N
        newWebserviceEndpoint.setPortComponentName(portComponentName);
        newWebserviceEndpoint.setEndpointAddressUri(portComponentName);

        getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, null, newWebserviceEndpoint);
		if(webServiceEndpointMap == null) {
			webServiceEndpointMap = new HashMap();
		}
        webServiceEndpointMap.put(portComponentDD, newWebserviceEndpoint);
        getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, null, newWebserviceEndpoint);
    }

    public void removeWebServiceEndpoint(DDBean portComponentDD) throws java.beans.PropertyVetoException {
        WebserviceEndpoint oldWebserviceEndpoint = (WebserviceEndpoint) webServiceEndpointMap.get(portComponentDD);
        if(oldWebserviceEndpoint != null) {
            getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, oldWebserviceEndpoint, null);
            webServiceEndpointMap.remove(portComponentDD);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, oldWebserviceEndpoint, null );
        }
    }

    /** This method is the backbone of an attempt to interface the new Map based storage
     *  for endpoints with the existing List based API, which the existing UI depends
     *  on, even though the List provides less information.  This algorithm is dependent
     *  on each port component name specified for a servlet being unique, regardless
     *  of which servlet or ejb implements it.  This will generally be true (in fact
     *  it may be required.)
     */
    private DDBean createKey(String targetPortComponentName) {
        DDBean key = null;
        DDBean [] portComponentBeans = getDDBean().getChildBean("port-component"); // NOI18N
        for(int i = 0; i < portComponentBeans.length; i++) {
            String portComponentName = getPortComponentName(portComponentBeans[i]);
            if(Utils.notEmpty(portComponentName) && portComponentName.equals(targetPortComponentName)) {
                key = portComponentBeans[i];
                break;
            }
        }

        return key;
    }


    private static final String WEB_URI_PREFIX = ""; // NOI18N
    private static final String EJB_URI_PREFIX = "webservice/"; // NOI18N
    
    private static final EndpointHelper servletHelper = new ServletHelper();
    private static final EndpointHelper ejbHelper = new EjbHelper();

    private static abstract class EndpointHelper {

        private final String linkXpath;
        private final String hostNameProperty;
        private final String endpointProperty;
        private final String blueprintsUriPrefix;

        public EndpointHelper(String xpath, String hnp, String epp, String uriPrefix) {
            linkXpath = xpath;
            hostNameProperty = hnp;
            endpointProperty = epp;
            blueprintsUriPrefix = uriPrefix;
        }

        public String getLinkXpath() {
            return linkXpath;
        }

        public String getHostNameProperty() {
            return hostNameProperty;
        }

        public String getEndpointProperty() {
            return endpointProperty;
        }

        public String getUriPrefix() {
            return blueprintsUriPrefix;
        }

        public abstract CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent);

        public abstract void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean);

        public abstract CommonDDBean [] getEndpointHosts(RootInterface root);

        public abstract void addEndpointHost(RootInterface root, CommonDDBean bean);

        public abstract CommonDDBean createNewHost();
    }

    private static class ServletHelper extends EndpointHelper {
        public ServletHelper() {
            super("servlet-link", Servlet.SERVLET_NAME, Servlet.WEBSERVICE_ENDPOINT, WEB_URI_PREFIX);
        }

        public CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent) {
            CommonDDBean [] result = (CommonDDBean []) ddParent.getValues(SunWebApp.WEBSERVICE_DESCRIPTION);
            return result;
        }

        public void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean) {
            ddParent.addValue(SunWebApp.WEBSERVICE_DESCRIPTION, wsDescBean);
        }

        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = (CommonDDBean []) root.getValues(SunWebApp.SERVLET);
            return result;
        }

        public void addEndpointHost(RootInterface root, CommonDDBean bean) {
            root.addValue(SunWebApp.SERVLET, bean);
        }

        public CommonDDBean createNewHost() {
            return StorageBeanFactory.getDefault().createServlet();
        }
    }

    private static class EjbHelper extends EndpointHelper {
        public EjbHelper() {
            super("ejb-link", Ejb.EJB_NAME, Ejb.WEBSERVICE_ENDPOINT, EJB_URI_PREFIX);
        }

        public CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent) {
            CommonDDBean [] result = null;
            CommonDDBean enterpriseBeans = (CommonDDBean) ddParent.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans != null) {
                result = (CommonDDBean []) enterpriseBeans.getValues(EnterpriseBeans.WEBSERVICE_DESCRIPTION);
            }
            return result;
        }

        public void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean) {
            CommonDDBean enterpriseBeans = getEnterpriseBeans(ddParent);
            enterpriseBeans.addValue(EnterpriseBeans.WEBSERVICE_DESCRIPTION, wsDescBean);
        }

        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = null;
            CommonDDBean enterpriseBeans = (CommonDDBean) root.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans != null) {
                result = (CommonDDBean []) enterpriseBeans.getValues(EnterpriseBeans.EJB);
            }
            return result;
        }

        public void addEndpointHost(RootInterface root, CommonDDBean bean) {
            CommonDDBean enterpriseBeans = getEnterpriseBeans(root);
            enterpriseBeans.addValue(EnterpriseBeans.EJB, bean);
        }

        public CommonDDBean createNewHost() {
            return StorageBeanFactory.getDefault().createEjb();
        }

        private CommonDDBean getEnterpriseBeans(CommonDDBean ddParent) {
            assert ddParent instanceof SunEjbJar;
            CommonDDBean enterpriseBeans = (CommonDDBean) ddParent.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans == null) {
                enterpriseBeans = StorageBeanFactory.getDefault().createEnterpriseBeans();
                ddParent.addValue(SunEjbJar.ENTERPRISE_BEANS, enterpriseBeans);
            }
            return enterpriseBeans;
        }
    }
}
