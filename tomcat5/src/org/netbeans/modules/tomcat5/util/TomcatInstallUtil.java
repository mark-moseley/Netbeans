/*
 * TomcatInstallUtil.java
 *
 * Created on December 9, 2003, 11:14 AM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;

import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;

import org.w3c.dom.Document;
import org.apache.xml.serialize.*;

/**
 *
 * @author  snajper
 */
public class TomcatInstallUtil {
    
    static private final String TOMCAT_TEMP_DIR = "temp";                       //NOI18N
    static private final String SERVER_XML_NAME_EXT = "server.xml";             //NOI18N
    static private final String TOMCAT_CONF_DIR = "conf";//NOI18N

    /** Creates a new instance of TomcatInstallUtil */
    public TomcatInstallUtil() {
    }    
    
    public static boolean noTempDir(File homeDir) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().equals(TOMCAT_TEMP_DIR); 
            }
        };
        File[] subFolders = homeDir.listFiles( filter );
        if ( subFolders == null || subFolders.length == 0 )
            return true;

        return false;        
    }
    
    public static boolean noServerXML(File homeDir, File baseDir) {
        File testDir = ( baseDir != null ) ? baseDir : homeDir;
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().equals(TOMCAT_CONF_DIR);
            }
        };
        File[] subFolders = testDir.listFiles( filter );
        if ( subFolders == null || subFolders.length == 0 )
            return true;
        
        File[] serverFiles = subFolders[0].listFiles( new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().equals( SERVER_XML_NAME_EXT );
            }
        } );
        if ( serverFiles == null || serverFiles.length == 0 )
            return true;

        return false;
    }
    
    public static boolean noBootStrapJar(File homeDir, File baseDir) {
        File[] subFolders = homeDir.listFiles();
        if (subFolders==null) return true;
        for (int i=0; i<subFolders.length; i++) {
            if (subFolders[i].getName().equals("bin")&&subFolders[i].isDirectory()) {//NOI18N
                File[] subBinFolders = subFolders[i].listFiles();
                if (subBinFolders==null) return true;
                for (int ii=0; ii<subBinFolders.length; ii++) {
                    if (subBinFolders[ii].getName().equals("bootstrap.jar")) {  //NOI18N
                        return false;
                   }
                }
            }
        }
        return true;
    }    

    public static String getAdminPort(Server server) {
        String port;
        
        port = server.getAttributeValue("port");
                
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getAdminPort: " + port);             // NOI18N
        }
        return port;
    }
    
    public static String getPort(Server server) {

        Service service = server.getService(0);

        int defCon = -1;
        boolean[] connectors = service.getConnector();
        String port;
                
        for (int i=0; i<service.sizeConnector(); i++) {
            String protocol = service.getAttributeValue("Connector",i,"protocol"); // NOI18N
            if ((protocol == null) || (protocol.toLowerCase().indexOf("http") > -1)) { // NOI18N
                defCon = i;
            }
        }
        
        if (defCon==-1 && service.sizeConnector() > 0) {
            defCon=0;
        }
        
        port = service.getAttributeValue("Connector",defCon,"port");            //NOI18N

        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getPort: " + port);             // NOI18N
        }
        return port;
    }
    
    public static String getHost(Server server) {
        String host = null;
        Service service = server.getService(0);
        if (service != null) {
            host = service.getAttributeValue("Engine",0,"defaultHost");
        }
       
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getHost: " + host);             // NOI18N
        }
        return host;
    }
    
    /** @return text (suitable for printing to XML file) for a given XML document.
     * this method uses org.apache.xml.serialize.XMLSerializer class for printing XML file
     */
    public static String getDocumentText(Document doc) {
        OutputFormat format = new OutputFormat ();
        format.setPreserveSpace (true);
        StringWriter sw = new StringWriter();
        org.w3c.dom.Element rootElement = doc.getDocumentElement();
        if (rootElement==null) return null;
        try {
            XMLSerializer ser = new XMLSerializer (sw, format);
            ser.serialize (rootElement);
            // Apache serializer also fails to include trailing newline, sigh.
            sw.write('\n');
            return sw.toString();
        }catch(IOException ex) {
            System.out.println("ex="+ex);
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return rootElement.toString();
        }
        finally {
            try {
                sw.close();
            } catch(IOException ex) {
                System.out.println("ex="+ex);
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    public static void updateDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origDoc=doc.getText(prefixInd,origLen-prefixInd);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
                newDoc=newDoc.substring(prefixIndNewDoc);
        }
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }
        
        doc.remove(prefixInd, origLen - prefixInd);
        doc.insertString(prefixInd, newDoc, null);
    }
    /** The method is useful to notify the user that Tomcat must be restarted 
     *
    */
    public static void notifyToRestart(final TomcatManager mng) {
        org.openide.util.RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                if (mng.getStartTomcat().isRunning()) {
                    DialogDisplayer disp = DialogDisplayer.getDefault();
                    disp.notify(new NotifyDescriptor.Message(
                     org.openide.util.NbBundle.getMessage(TomcatInstallUtil.class,"MSG_TomcatRestart")));
                }
            }
        });
    }
    
    /** The method is useful to notify the user that changes cannot be performed 
     *
    */
    public static void notifyThatRunning(final TomcatManager mng) {
        DialogDisplayer disp = DialogDisplayer.getDefault();
        disp.notify(new NotifyDescriptor.Message(
         org.openide.util.NbBundle.getMessage(TomcatInstallUtil.class,"MSG_TomcatIsRunning")));
    }
    
    public static boolean setServerPort(Integer port, FileObject tomcatConf) {
        FileObject fo = tomcatConf;
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
    
    public static boolean setAdminPort(Integer port, FileObject tomcatConf) {
        FileObject fo = tomcatConf;
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
    
    public static void updateDocument(DataObject dobj, org.w3c.dom.Document doc)
        throws javax.swing.text.BadLocationException, java.io.IOException {
        org.openide.cookies.EditorCookie editor = (EditorCookie)dobj.getCookie(EditorCookie.class);
        javax.swing.text.Document textDoc = editor.getDocument();
        if (textDoc==null) textDoc = editor.openDocument();
        TomcatInstallUtil.updateDocument(textDoc,TomcatInstallUtil.getDocumentText(doc),"<Server"); //NOI18N
        SaveCookie savec = (SaveCookie) dobj.getCookie(SaveCookie.class);
        if (savec!=null) savec.save();
    }
    
    public static String generatePassword(int length) {
	int ran2 = 0;
	String pwd = "";
	for (int i = 0; i < length; i++) {
            ran2 = (int)(Math.random()*61);
            if (ran2 < 10) {
                ran2 += 48;
            } else {
                if (ran2 < 35) {
                    ran2 += 55;
                } else {
                    ran2 += 62;
                }
            }
            char c = (char) ran2;
            pwd += c;
	}
        return pwd;
    }
    
}
