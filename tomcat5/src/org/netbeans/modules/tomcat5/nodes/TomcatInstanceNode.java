/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.beans.PropertyEditor;
import java.util.*;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.nodes.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.tomcat5.nodes.actions.RefreshWebModulesAction;

import org.netbeans.modules.tomcat5.ide.MonitorSupport;
import org.openide.actions.PropertiesAction;


/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/tomcat5"; // NOI18N
    
    protected static final String PROPERTY_TOMCAT_HOME = "tomcat_home"; //NOI18N
    protected static final String PROPERTY_TOMCAT_BASE = "tomcat_base"; //NOI18N

    protected static final String DEBUGGER="debugger"; //NOI18N
    protected static final String DEBUGGER_PORT = "debugger_port"; //NOI18N
    protected static final String DEBUGGING_TYPE = "debugging_type"; //NOI18N
    protected static final String SERVER_PORT= "server_port";//NOI18N
    protected static final String ADMIN_PORT= "admin_port";//NOI18N
    protected static final String MONITOR_ENABLED= "monitor_enabled";//NOI18N
    protected static final String CLASSIC = "classic"; //NOI18N
    protected static final String USER_NAME = "user_name"; //NOI18N
    protected static final String PASSWORD = "password"; //NOI18N
    protected static final String NAME_FOR_SHARED_MEMORY_ACCESS = "name_for_shared_memory_access"; //NOI18N
    private static final String DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS = "tomcat_shared_memory_id"; //NOI18N
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        //this.getChildren().add(new Node[]{new WebModuleNode(new Children.Map())});
        lkp = lookup;
        setIconBase(ICON_BASE);
        getCookieSet().add(this);
    }
    
    private int iPort = 0;
    
    
    public String getDisplayName(){
        Integer port = getServerPort();
        String portStr = "";
        if (port != null) { 
            portStr = port.toString();
        }
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
            new Object []{portStr});
    }
    
    /** Returns the TomcatManager for this node, or null if TomcatManager was not found - which 
     * should never happen.
     */
    private TomcatManager getTomcatManager() {
        DeploymentManager m = getDeploymentManager();
        return (m instanceof TomcatManager) ? (TomcatManager)m : null;
    }
    
    private Integer getServerPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            Integer port = m.getServerPort();
            if (port != null && port.intValue() != iPort){
                iPort = port.intValue();
                setDisplayName(NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
                    new Object []{"" + iPort}));
            }
            return port;
        };
        return TomcatManager.DEFAULT_SERVER_PORT;
    }

    private Boolean getClassic() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getClassic();
        };
        return TomcatManager.DEFAULT_CLASSIC;
    }

    private String getDebugType() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getDebugType();
        };
        return null;
    }

    private String getSharedMemory() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getSharedMemory();
        };
        return DEFAULT_NAME_FOR_SHARED_MEMORY_ACCESS;
    }

    private Integer getDebugPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return  m.getDebugPort();
        };
        return TomcatManager.DEFAULT_DEBUG_PORT;
    }
    
    private Integer getAdminPort () {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return m.getAdminPort();
        };
        return TomcatManager.DEFAULT_ADMIN_PORT;
    }

    private void setClassic (Boolean classic) {
        TomcatManager m = getTomcatManager();
        if (m != null){
            m.setClassic(classic);
        };
    }

    private void setSharedMemory (String str) {
        TomcatManager m = getTomcatManager();
        if (m != null){
            m.setSharedMemory(str);
        };
    }

    private void setDebugType (String str) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setDebugType(str);
        };
    }

    private void setDebugPort (Integer port) {
        TomcatManager m = getTomcatManager();
        if (m != null){
            m.setDebugPort(port);
        };
    }

    public javax.swing.Action[] getActions(boolean context) {
        return new SystemAction[] {
                   null,
                   //SystemAction.get (AccessLogAction.class),
                   SystemAction.get (ContextLogAction.class),
                   null,
                   SystemAction.get(PropertiesAction.class),
                   null
               };        
    }

    DeploymentManager getDeploymentManager() {
        return (DeploymentManager)lkp.lookup(DeploymentManager.class);
    }

    private String getHome() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            return m.getCatalinaHomeDir().getAbsolutePath();
        }
        return "";
    }
    
    private String getBase() {
        TomcatManager m = getTomcatManager();
        if (m != null){
            if (m.getCatalinaBase() != null) {
                return m.getCatalinaBaseDir().getAbsolutePath();
            } else {
                return m.getCatalinaHomeDir().getAbsolutePath();
            }
        }
        return "";
    }

    /** Getter for property userName.
     * @return Value of property userName.
     *
     */
    private String getUserName() {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            return ((TomcatManager)m).getUsername();
        };
        return ""; //NOI18N
    }    
    
    /** Setter for property userName.
     * @param userName New value of property userName.
     *
     */
    private void setUserName(String userName) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setUsername(userName);
        };
    }
    
    /** Getter for property password.
     * @return Value of property password.
     *
     */
    private String getPassword() {
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_password");
    }
    
    /** Setter for property password.
     * @param password New value of property password.
     *
     */
    private void setPassword(String password) {
        DeploymentManager m = getDeploymentManager();
        if (m instanceof TomcatManager){
            ((TomcatManager)m).setPassword(password);
        };
    }
    
    // Create a property sheet:
    protected Sheet createSheet () {
	Sheet sheet = super.createSheet ();
        Sheet.Set ssProp = sheet.get (Sheet.PROPERTIES);
        if (ssProp == null) {
	    ssProp = Sheet.createPropertiesSet ();
            sheet.put (ssProp);
	}
                
        Node.Property p;
        p = new PropertySupport.ReadWrite (
                   SERVER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_serverPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_serverPort")   // NOI18N
               ) {
                   public Object getValue () {
                       return getServerPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning()) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               Integer newPort = (Integer)val;
                               if (setServerPort(newPort)) {
                                   mng.setServerPort(newPort);
                                   TomcatInstanceNode.this.setDisplayName(NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
                                    new Object []{"" + newPort}));
                               }
                           }
                       }
                   }
               };
        ssProp.put(p);  
        p = new PropertySupport.ReadWrite (
                   ADMIN_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_adminPort"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_adminPort")   // NOI18N
               ) {
                   public Object getValue () {
                       return getAdminPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning()) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               Integer newPort = (Integer)val;
                               if (setAdminPort(newPort)) mng.setAdminPort(newPort);
                           }
                       }
                   }
               };    
        ssProp.put(p);  
        p = new PropertySupport.ReadWrite (
                   USER_NAME,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_userName"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_userName")   // NOI18N
               ) {
                   public Object getValue () {
                       return getUserName();
                   }
                   
                   public void setValue (Object val){
                       setUserName((String)val);
                   }
               };    
        ssProp.put(p);  
        p = new PropertySupport.ReadWrite (
                   PASSWORD,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_password"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_password")   // NOI18N
               ) {
                   public Object getValue () {
                       return getPassword();
                   }
                   
                   public void setValue (Object val){
                       setPassword((String)val);
                   }
               };    
        ssProp.put(p);  
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_HOME,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatHome"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatHome")   // NOI18N
               ) {
                   public Object getValue () {
                       return getHome();
                   }
               };    
        ssProp.put(p);
        p = new PropertySupport.ReadOnly(
                   PROPERTY_TOMCAT_BASE,
                   String.class,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_tomcatBase"),   // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_tomcatBase")   // NOI18N
               ) {
                   public Object getValue () {
                       return getBase();
                   }
               };    
        ssProp.put(p);
        p = new PropertySupport.ReadWrite (
        MONITOR_ENABLED,
        Boolean.TYPE,
        NbBundle.getMessage (TomcatInstanceNode.class, "PROP_monitorEnabled"),   // NOI18N
        NbBundle.getMessage (TomcatInstanceNode.class, "HINT_monitorEnabled")   // NOI18N
        ) {
            public Object getValue () {
                TomcatManager tm = getTomcatManager();
                if (tm != null) {
                    return Boolean.valueOf(MonitorSupport.getMonitorFlag(tm));
                }
                else return null;
            }
            
            public void setValue (Object val){
                TomcatManager tm = getTomcatManager();
                if (tm != null) {
                    boolean b = ((Boolean)val).booleanValue();
                    MonitorSupport.setMonitorFlag(tm, b);
                }
            }
        };
        ssProp.put(p);
        
        
        Sheet.Set ssDebug = new Sheet.Set ();
        ssDebug.setName(DEBUGGER);
        ssDebug.setDisplayName(NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerSetName"));  // NOI18N
        ssDebug.setShortDescription(NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerSetName"));  // NOI18N
        
        ssDebug.setValue("helpID", "tomcat_node_ssProp");// NOI18N

        p = new PropertySupport.ReadWrite (
                   DEBUGGER_PORT,
                   Integer.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggerPort"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggerPort")  // NOI18N
               ) {
                   public Object getValue () {
                       return getDebugPort();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning()) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               setDebugPort((Integer)val);
                           }
                       }
                   }                   
               };      
        ssDebug.put(p);
        
        p = new PropertySupport.ReadWrite (
                   CLASSIC,
                   Boolean.TYPE,
                   NbBundle.getMessage (TomcatInstanceNode.class, "PROP_classic"),  // NOI18N
                   NbBundle.getMessage (TomcatInstanceNode.class, "HINT_classic")  // NOI18N
               ) {
                   public Object getValue () {
                       return getClassic();
                   }
                   
                   public void setValue (Object val){
                       TomcatManager mng = getTomcatManager();
                       if (mng!=null) {
                           if (mng.isRunning()) {
                               TomcatInstallUtil.notifyThatRunning(mng);
                           } else {
                               setClassic((Boolean)val);
                           }
                       }
                       
                   }                   
               };      
               
        ssDebug.put(p);
        
        if (org.openide.util.Utilities.isWindows()) {
            p = new PropertySupport.ReadWrite (
                       DEBUGGING_TYPE,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_debuggingType"),   // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_debuggingType")  // NOI18N
                   ) {
                       public Object getValue () {
                           return getDebugType();
                       }

                       public void setValue (Object val) {
                           TomcatManager mng = getTomcatManager();
                           if (mng!=null) {
                               if (mng.isRunning()) {
                                   TomcatInstallUtil.notifyThatRunning(mng);
                               } else {
                                    setDebugType((String)val);
                               }
                           }
                       }

                       public PropertyEditor getPropertyEditor(){
                           return new DebuggingTypeEditor();
                       }
                   };
            ssDebug.put(p);        
            p = new PropertySupport.ReadWrite (
                       NAME_FOR_SHARED_MEMORY_ACCESS,
                       String.class,
                       NbBundle.getMessage (TomcatInstanceNode.class, "PROP_nameForSharedMemoryAccess"),  // NOI18N
                       NbBundle.getMessage (TomcatInstanceNode.class, "HINT_nameForSharedMemoryAccess")  // NOI18N
                   ) {
                       public Object getValue () {
                           return getSharedMemory();
                       }

                       public void setValue (Object val){
                           TomcatManager mng = getTomcatManager();
                           if (mng!=null) {
                               if (mng.isRunning()) {
                                   TomcatInstallUtil.notifyThatRunning(mng);
                               } else {
                                   setSharedMemory((String)val);
                               }
                           }
                       }
                   };
            ssDebug.put(p);  
        }
        sheet.put(ssDebug);
        
        return sheet;
    }
    
    private boolean setServerPort(Integer port) {
        FileObject fo = getTomcatConf();
        if (fo==null) return false;
        boolean success=false;
        try {
            XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
            org.w3c.dom.Document doc = dobj.getDocument();
            org.w3c.dom.Element root = doc.getDocumentElement();
            org.w3c.dom.NodeList list = root.getElementsByTagName("Service"); //NOI18N
            int size=list.getLength();
            if (size>0) {
                org.w3c.dom.Element service=(org.w3c.dom.Element)list.item(0);
                org.w3c.dom.NodeList cons = service.getElementsByTagName("Connector"); //NOI18N
                for (int i=0;i<cons.getLength();i++) {
                    org.w3c.dom.Element con=(org.w3c.dom.Element)cons.item(i);
                    String protocol = con.getAttribute("protocol"); //NOI18N
                    if ((protocol == null) || protocol.length()==0 || (protocol.toLowerCase().indexOf("http") > -1)) { //NOI18N
                        con.setAttribute("port", String.valueOf(port)); //NOI18N
                        updateDocument(dobj,doc);
                        success=true;
                    }
                }
            }
        } catch(org.xml.sax.SAXException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(org.openide.loaders.DataObjectNotFoundException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(javax.swing.text.BadLocationException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(java.io.IOException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        }
        return success;
    }
        
    private boolean setAdminPort(Integer port) {
        FileObject fo = getTomcatConf();
        if (fo==null) return false;
        boolean success=false;
        try {
            XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
            org.w3c.dom.Document doc = dobj.getDocument();
            org.w3c.dom.Element root = doc.getDocumentElement();
            root.setAttribute("port", String.valueOf(port)); //NOI18N
            updateDocument(dobj,doc);
            success=true;
        } catch(org.xml.sax.SAXException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(org.openide.loaders.DataObjectNotFoundException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(javax.swing.text.BadLocationException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        } catch(java.io.IOException ex){
            org.openide.ErrorManager.getDefault ().notify(ex);
        }
        return success;
    }
    
    private FileObject getTomcatConf() {
        FileSystem fs = getTomcatManager().getCatalinaBaseFileSystem();
        if (fs != null) {
            return fs.findResource("conf/server.xml"); //NOI18N
        }
        return null;
    }
    
    private void updateDocument(DataObject dobj, org.w3c.dom.Document doc)
        throws javax.swing.text.BadLocationException, java.io.IOException {
        org.openide.cookies.EditorCookie editor = (EditorCookie)dobj.getCookie(EditorCookie.class);
        javax.swing.text.Document textDoc = editor.getDocument();
        if (textDoc==null) textDoc = editor.openDocument();
        TomcatInstallUtil.updateDocument(textDoc,TomcatInstallUtil.getDocumentText(doc),"<Server"); //NOI18N
        SaveCookie savec = (SaveCookie) dobj.getCookie(SaveCookie.class);
        if (savec!=null) savec.save();
    }
    
    
}
