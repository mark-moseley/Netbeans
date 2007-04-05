/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.modules.exceptions.ReportPanel;
import org.netbeans.modules.exceptions.ExceptionsSettings;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall {
    /**
     *
     */
    static final String USER_CONFIGURATION = "UI_USER_CONFIGURATION";   // NOI18N
    private static UIHandler ui = new UIHandler(false);
    private static UIHandler handler = new UIHandler(true);
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
    static final RequestProcessor RP = new RequestProcessor("UI Gestures"); // NOI18N
    private static final Preferences prefs = NbPreferences.forModule(Installer.class);
    private static OutputStream logStream;
    private static int logsSize;
    
    @Override
    public void restored() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        logsSize = prefs.getInt("count", 0);
        
        for (Activated a : Lookup.getDefault().lookupAll(Activated.class)) {
            a.activated(log);
        }
        /*
        Enumeration<String> en = LogManager.getLogManager().getLoggerNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            if (name.startsWith("org.netbeans.ui")) {
                Logger l = Logger.getLogger(name);
                l.setLevel(Level.FINEST);
            }
        }
         */
    }
    
    @Override
    public void uninstalled() {
        close();
    }
    
    @Override
    public void close() {
        Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
        log.removeHandler(ui);
        Logger all = Logger.getLogger(""); // NOI18N
        all.removeHandler(handler);
        
        closeLogStream();
    }
    
    static void writeOut(LogRecord r) {
        try {
            LogRecords.write(logStream(), r);
            if (logsSize >= UIHandler.MAX_LOGS) {
                prefs.putInt("count", UIHandler.MAX_LOGS);
                closeLogStream();
                File f = logFile(0);
                f.renameTo(new File(f.getParentFile(), f.getName() + ".1"));
                logsSize = 0;
            } else {
                logsSize++;
                if (prefs.getInt("count", 0) < logsSize) {
                    prefs.putInt("count", logsSize);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    public static int getLogsSize() {
        return prefs.getInt("count", 0); // NOI18N
    }
    
    public static List<LogRecord> getLogs() {
        File f = logFile(0);
        if (f == null || !f.exists()) {
            return Collections.emptyList();
        }
        closeLogStream();
        
        class H extends Handler {
            List<LogRecord> logs = new LinkedList<LogRecord>();
            
            public void publish(LogRecord r) {
                logs.add(r);
                if (logs.size() > UIHandler.MAX_LOGS) {
                    logs.remove(0);
                }
            }
            
            public void flush() {
            }
            
            public void close() throws SecurityException {
            }
        }
        H handler = new H();
        
        
        InputStream is = null;
        File f1 = logFile(1);
        if (logsSize < UIHandler.MAX_LOGS && f1 != null && f1.exists()) {
            try {
                is = new FileInputStream(f1);
                LogRecords.scan(is, handler);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        try {
            is = new FileInputStream(f);
            LogRecords.scan(is, handler);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return handler.logs;
    }
    
    private static File logFile(int revision) {
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return null;
        }
        
        String suffix = revision == 0 ? "" : "." + revision;
        
        File userDir = new File(ud); // NOI18N
        File logFile = new File(new File(new File(userDir, "var"), "log"), "uigestures" + suffix);
        return logFile;
    }
    
    private static OutputStream logStream() throws FileNotFoundException {
        synchronized (Installer.class) {
            if (logStream != null) {
                return logStream;
            }
        }
        
        OutputStream os;
        File logFile = logFile(0);
        if (logFile != null) {
            logFile.getParentFile().mkdirs();
            os = new BufferedOutputStream(new FileOutputStream(logFile, true));
        } else {
            os = new NullOutputStream();
        }
        
        synchronized (Installer.class) {
            logStream = os;
        }
        
        return os;
    }
    
    private static void closeLogStream() {
        OutputStream os;
        synchronized (Installer.class) {
            os = logStream;
            logStream = null;
        }
        if (os == null) {
            return;
        }
        
        try {
            os.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    static void clearLogs() {
        closeLogStream();
        
        for (int i = 0; ; i++) {
            File f = logFile(i);
            if (f == null || !f.exists()) {
                break;
            }
            f.delete();
        }
        
        prefs.putInt("count", 0);
        UIHandler.SUPPORT.firePropertyChange(null, null, null);
    }
    
    public boolean closing() {
        if (getLogsSize() == 0) {
            return true;
        }
        
        return displaySummary("EXIT_URL", false); // NOI18N
    }
    
    private static AtomicReference<String> DISPLAYING = new AtomicReference<String>();
    static boolean displaySummary(String msg, boolean explicit) {
        if (!DISPLAYING.compareAndSet(null, msg)) {
            return true;
        }
        
        boolean v = true;
        try {
            if (!explicit) {
                boolean dontAsk = NbPreferences.forModule(Installer.class).getBoolean("ask.never.again." + msg, false); // NOI18N
                if (dontAsk) {
                    LOG.log(Level.INFO, "UI Gesture Collector's ask.never.again.{0} is true, exiting", msg); // NOI18N
                    return true;
                }
            }
            
            v = doDisplaySummary(msg);
        } finally {
            DISPLAYING.set(null);
        }
        return v;
    }
    
    protected static Throwable getThrown(){
        List<LogRecord> list = getLogs();
        ListIterator<LogRecord> it = list.listIterator(list.size());
        while (it.hasPrevious()){
            Throwable t = it.previous().getThrown();
            // find first exception from end
            if (t != null) return t;
        }
        return null;// no throwable found
    }
    
    private static boolean doDisplaySummary(String msg) {
        Submit submit = new Submit(msg);
        submit.doShow();
        return submit.okToExit;
    }
    
    
    private static boolean isChild(org.w3c.dom.Node child, org.w3c.dom.Node parent) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            child = child.getParentNode();
        }
        return false;
    }
    
    private static String attrValue(org.w3c.dom.Node in, String attrName) {
        org.w3c.dom.Node n = in.getAttributes().getNamedItem(attrName);
        return n == null ? null : n.getNodeValue();
    }
    
    /** Tries to parse a list of buttons provided by given page.
     * @param u the url to read the page from
     * @param defaultButton the button to add always to the list
     */
    static void parseButtons(InputStream is, Object defaultButton, DialogDescriptor dd)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        
        List<Object> buttons = new ArrayList<Object>();
        List<Object> left = new ArrayList<Object>();
        
        NodeList forms = doc.getElementsByTagName("form");
        for (int i = 0; i < forms.getLength(); i++) {
            Form f = new Form(forms.item(i).getAttributes().getNamedItem("action").getNodeValue());
            NodeList inputs = doc.getElementsByTagName("input");
            for (int j = 0; j < inputs.getLength(); j++) {
                if (isChild(inputs.item(j), forms.item(i))) {
                    org.w3c.dom.Node in = inputs.item(j);
                    String type = attrValue(in, "type");
                    String name = attrValue(in, "name");
                    String value = attrValue(in, "value");
                    String align = attrValue(in, "align");
                    String alt = attrValue(in, "alt");
                    
                    List<Object> addTo = "left".equals(align) ? left : buttons;
                    
                    if ("hidden".equals(type) && "submit".equals(name)) { // NOI18N
                        f.submitValue = value;
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, f.submitValue);
                        b.setActionCommand("submit"); // NOI18N
                        b.putClientProperty("url", f.url); // NOI18N
                        b.setDefaultCapable(addTo.isEmpty() && addTo == buttons);
                        b.putClientProperty("alt", alt); // NOI18N
                        b.putClientProperty("now", f.submitValue); // NOI18N
                        addTo.add(b);
                        continue;
                    }
                    

                    if ("hidden".equals(type)) { // NOI18N
                        JButton b = new JButton();
                        Mnemonics.setLocalizedText(b, value);
                        b.setActionCommand(name);
                        b.setDefaultCapable(addTo.isEmpty() && addTo == buttons);
                        b.putClientProperty("alt", alt); // NOI18N
                        b.putClientProperty("now", value); // NOI18N
                        addTo.add(b);
                        if ("exit".equals(name)) { // NOI18N
                            defaultButton = null;
                        }else if ("redirect".equals(name)){
                            b.putClientProperty("url", f.url);
                        }
                    }
                }
            }
        }
        if (defaultButton != null) {
            buttons.add(defaultButton);
        }
        dd.setOptions(buttons.toArray());
        dd.setAdditionalOptions(left.toArray());
    }
    
    static String decodeButtons(Object res, URL[] url) {
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                String replace = System.getProperty("org.netbeans.modules.uihandler.Submit"); // NOI18N
                if (replace != null) {
                    post = replace;
                }
                try {
                    url[0] = new URL((String) post);
                } catch (MalformedURLException ex) {
                    url[0] = null;
                }
            }
            return b.getActionCommand();
        }
        return res instanceof String ? (String)res : null;
    }
    
    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs) throws IOException {
        ProgressHandle h = ProgressHandleFactory.createHandle(NbBundle.getMessage(Installer.class, "MSG_UploadProgressHandle"));
        try {
            return uLogs(h, postURL, id, attrs, recs);
        } finally {
            h.finish();
        }
    }
    
    private static URL uLogs(ProgressHandle h, URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs) throws IOException {
        h.start(100 + recs.size());
        h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadConnecting")); // NOI18N
        
        URLConnection conn = postURL.openConnection();
        
        h.progress(50);
        
        conn.setReadTimeout(20000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=--------konec<>bloku");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-control", "no-cache");
        
        h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadSending"), 60);
        
        PrintStream os = new PrintStream(conn.getOutputStream());
        /*
        os.println("POST " + postURL.getPath() + " HTTP/1.1");
        os.println("Pragma: no-cache");
        os.println("Cache-control: no-cache");
        os.println("Content-Type: multipart/form-data; boundary=--------konec<>bloku");
        os.println();
         */
        for (Map.Entry<String, String> en : attrs.entrySet()) {
            os.println("----------konec<>bloku");
            os.println("Content-Disposition: form-data; name=\"" + en.getKey() + "\"");
            os.println();
            os.println(en.getValue().getBytes());
        }
        
        h.progress(70);
        
        os.println("----------konec<>bloku");
        
        if (id == null) {
            id = "uigestures"; // NOI18N
        }
        
        os.println("Content-Disposition: form-data; name=\"logs\"; filename=\"" + id + "\"");
        os.println("Content-Type: x-application/gzip");
        os.println();
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        DataOutputStream data = new DataOutputStream(gzip);
        data.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("utf-8")); // NOI18N
        data.write("<uigestures version='1.0'>\n".getBytes("utf-8")); // NOI18N
        
        int cnt = 80;
        for (LogRecord r : recs) {
            h.progress(cnt++);
            LogRecords.write(data, r);
        }
        data.write("</uigestures>\n".getBytes("utf-8")); // NOI18N
        data.flush();
        gzip.finish();
        os.println("----------konec<>bloku--");
        os.close();
        
        h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadReading"), cnt + 10);
        
        InputStream is = conn.getInputStream();
        StringBuffer redir = new StringBuffer();
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            redir.append((char)ch);
        }
        is.close();
        
        h.progress(cnt + 20);
        
        LOG.info("Reply from uploadLogs:");
        LOG.info(redir.toString());
        
        Pattern p = Pattern.compile("<meta\\s*http-equiv=.Refresh.\\s*content.*url=['\"]?([^'\" ]*)\\s*['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(redir);
        
        
        if (m.find()) {
            return new URL(m.group(1));
        } else {
            File f = File.createTempFile("uipage", "html");
            FileWriter w = new FileWriter(f);
            w.write(redir.toString());
            w.close();
            return f.toURI().toURL();
        }
    }
    
    private static String findIdentity() {
        Preferences p = NbPreferences.root().node("org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get("ideIdentity", null);
        LOG.log(Level.INFO, "findIdentity: {0}", id);
        return id;
    }
    
    static final class Form extends Object {
        final String url;
        String submitValue;
        
        public Form(String u) {
            url = u;
        }
    }
    
    private static final class Submit implements ActionListener, Runnable {
        private String msg;
        boolean okToExit;
        private DialogDescriptor dd;
        private Dialog d;
        private SubmitPanel panel;
        private HtmlBrowser browser;
        private URL url;
        private String exitMsg;
        private boolean report;//property tells me wheather I'm in report mode
        private ReportPanel reportPanel;
        
        public Submit(String msg) {
            this.msg = msg;
            if ("ERROR_URL".equals(msg)) report = true; // NOI18N
            else report = false;
        }
        
        private LogRecord getUserData(){
            LogRecord userData;
            ExceptionsSettings settings = new ExceptionsSettings();
            ArrayList<String> params = new ArrayList<String>(6);
            params.add(getOS());
            params.add(getVM());
            params.add(getVersion());
            if (reportPanel != null) reportPanel.saveUserName();
            params.add(settings.getUserName());
            if (reportPanel != null){
                params.add(reportPanel.getSummary());
                params.add(reportPanel.getComment());
            }
            userData = new LogRecord(Level.CONFIG, USER_CONFIGURATION);
            userData.setResourceBundle(NbBundle.getBundle(Installer.class));
            userData.setResourceBundleName(Installer.class.getPackage().getName()+".Bundle");
            userData.setParameters(params.toArray());
            return userData;
        }
        
        private String getOS(){
            String unknown = "unknown";                                   // NOI18N
            String str = System.getProperty("os.name", unknown)+", "+     // NOI18N
                    System.getProperty("os.version", unknown)+", "+       // NOI18N
                    System.getProperty("os.arch", unknown);               // NOI18N
            return str;
        }
        
        private String getVersion(){
            String str = MessageFormat.format(
                    NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                    new Object[] {System.getProperty("netbeans.buildnumber")});                         // NOI18N
            return str;
        }
        
        private String getVM(){
            return System.getProperty("java.vm.name", "unknown") + ", " + System.getProperty("java.vm.version", ""); // NOI18N
        }
        
        public void doShow() {
            Logger log = Logger.getLogger("org.netbeans.ui"); // NOI18N
            for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
                a.deactivated(log);
            }
            if (report) {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "ErrorDialogTitle"));
            } else {
                dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
            }
            
            exitMsg = NbBundle.getMessage(Installer.class, "MSG_" + msg + "_EXIT"); // NOI18N
            
            String defaultURI = NbBundle.getMessage(Installer.class, msg);
            if (defaultURI == null || defaultURI.length() == 0) {
                okToExit = true;
                return;
            }
            
            synchronized (this) {
                RequestProcessor.getDefault().post(this);
                while (d == null) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
            
            for (;;) {
                try {
                    if (url == null) {
                        url = new URL(defaultURI); // NOI18N
                    }
                    
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(5000);
                    File tmp = File.createTempFile("uigesture", ".html");
                    tmp.deleteOnExit();
                    FileOutputStream os = new FileOutputStream(tmp);
                    FileUtil.copy(conn.getInputStream(), os);
                    os.close();
                    conn.getInputStream().close();
                    InputStream is = new FileInputStream(tmp);
                    parseButtons(is, exitMsg, dd);
                    if ("ERROR_URL".equals(msg)&(dd.getOptions().length > 1)){
                        Object obj = dd.getOptions()[0];
                        AbstractButton abut = null;
                        String report=null;
                        if (obj instanceof AbstractButton ) abut = (AbstractButton)obj;
                        if (abut != null)report = (String) abut.getClientProperty("alt");
                        if ("reportDialog".equals(report)) dd.setMessage(reportPanel);
                    }
                    is.close();
                    url = tmp.toURI().toURL();
                } catch (ParserConfigurationException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (SAXException ex) {
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                } catch (java.net.SocketTimeoutException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getUnknownHostExceptionURL();
                    msg = null;
                    continue;
                } catch (UnknownHostException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getUnknownHostExceptionURL();
                    msg = null;
                    continue;
                } catch (NoRouteToHostException ex) {
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getUnknownHostExceptionURL();
                    msg = null;
                    continue;
                }catch (ConnectException ex){
                    LOG.log(Level.INFO, url.toExternalForm(), ex);
                    url = getUnknownHostExceptionURL();
                    msg = null;
                    continue;
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, url.toExternalForm(), ex);
                }
                break;
            }
            
            if (browser != null) {
                browser.setURL(url);
            }
            
            synchronized (this) {
                while (d != null) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
        }
        
        private URL getUnknownHostExceptionURL() {
            String resource = NbBundle.getMessage(Installer.class, "URL_UnknownHostException");
            return getClass().getResource(resource);
        }
        
        public void run() {
            if (reportPanel==null) reportPanel = new ReportPanel();
            Throwable t = getThrown();
            if ((t != null)&&(reportPanel !=null)){
                String summary = t.getClass().getName();
                String[] pieces = summary.split("\\.");
                if (pieces.length > 0) summary = pieces[pieces.length-1];//posledni piece
                if (t.getMessage()!= null)summary = summary.concat(" : " + t.getMessage()); //NOI18N
                reportPanel.setSummary(summary);
            }
            browser = new HtmlBrowser();
            String resource = NbBundle.getMessage(Installer.class, "URL_Connecting"); // NOI18N
            browser.setURL(Installer.class.getResource("Connecting.html")); // NOI18N
            browser.setEnableLocation(false);
            browser.setEnableHome(false);
            browser.setStatusLineVisible(false);
            browser.setToolbarVisible(false);
            browser.setPreferredSize(new Dimension(640, 480));
            dd.setMessage(browser);
            
            //        AbstractNode root = new AbstractNode(new Children.Array());
            //        root.setName("root"); // NOI18N
            //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
            //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
            //        for (LogRecord r : recs) {
            //            root.getChildren().add(new Node[] { UINode.create(r) });
            //        }
            //
            //        panel.getExplorerManager().setRootContext(root);
            
            Object[] arr = new Object[] { exitMsg };
            dd.setOptions(arr);
            dd.setClosingOptions(arr);
            dd.setButtonListener(this);
            dd.setModal(true);
            d = DialogDisplayer.getDefault().createDialog(dd);
            synchronized (this) {
                // dialog created let the code go on
                notify();
            }
            d.setVisible(true);
            
            Object res = dd.getValue();
            
            if (res == exitMsg) {
                okToExit = true;
            }
            synchronized (this) {
                d = null;
                notifyAll();
            }
        }
        
        private void uploadAndPost(List<LogRecord> recs, URL u) {
            URL nextURL = null;
            try {
                nextURL = uploadLogs(u, findIdentity(), Collections.<String,String>emptyMap(), recs);
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
                String txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailed", u.getHost(), u.toExternalForm());
                NotifyDescriptor dd = new NotifyDescriptor.Message(txt, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(dd);
            }
            if (nextURL != null) {
                clearLogs();
                HtmlBrowser.URLDisplayer.getDefault().showURL(nextURL);
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            final URL[] url = new URL[1];
            String actionURL = decodeButtons(e.getSource(), url);
            
            if ("submit".equals(e.getActionCommand())) { // NOI18N
                final List<LogRecord> recs = getLogs();
                if (report) reportPanel.saveUserName();
                recs.add(getUserData());
                RP.post (new Runnable() {
                    public void run() {
                        uploadAndPost(recs, url[0]);
                    }
                });
                okToExit = false;
                // this should close the descriptor
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                closeDialog();
                return;
            }
            
            if ("redirect".equals(e.getActionCommand())){
                if (url[0] != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url[0]);
                }
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                closeDialog();
                return ;
            }
            
            if ("view-data".equals(e.getActionCommand())) { // NOI18N
                if (panel == null) {
                    panel = new SubmitPanel();
                    AbstractNode root = new AbstractNode(new Children.Array());
                    root.setName("root"); // NOI18N
                    List<LogRecord> recs = getLogs();
                    recs.add(getUserData());
                    root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                    root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                    LinkedList<Node> reverted = new LinkedList<Node>();
                    for (LogRecord r : recs) {
                        reverted.addFirst(UINode.create(r));
                        panel.addRecord(r);
                    }
                    root.getChildren().add(reverted.toArray(new Node[0]));
                    panel.getExplorerManager().setRootContext(root);
                }
                
                if (report) {
                    if (dd.getMessage() == reportPanel) {
                        dd.setMessage(panel);
                    } else {
                        dd.setMessage(reportPanel);
                    }
                } else {
                    if (dd.getMessage() == browser) {
                        dd.setMessage(panel);
                    } else {
                        dd.setMessage(browser);
                    }
                }
                if (e.getSource() instanceof AbstractButton) {
                    AbstractButton abut = (AbstractButton)e.getSource();
                    String alt = (String) abut.getClientProperty("alt"); // NOI18N
                    if (alt != null) {
                        String now = (String)abut.getClientProperty("now"); // NOI18N
                        Mnemonics.setLocalizedText(abut, alt);
                        abut.putClientProperty("alt", now); // NOI18N
                        abut.putClientProperty("now", alt); // NOI18N
                    }
                }
                return;
            }
            
            if ("never-again".equals(e.getActionCommand())) { // NOI18N
                LOG.log(Level.FINE, "Assigning ask.never.again.{0} to true", msg); // NOI18N
                NbPreferences.forModule(Installer.class).putBoolean("ask.never.again." + msg, true); // NOI18N
                okToExit = true;
                // this should close the descriptor
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                closeDialog();
                return;
            }
            
            if ("exit".equals(e.getActionCommand())) {
                // this should close the descriptor
                dd.setValue(DialogDescriptor.CLOSED_OPTION);
                closeDialog();
                return;
            }
        }
        
        private void closeDialog() {
            d.setVisible(false);
            synchronized (this) {
                d = null;
                notifyAll();
            }
        }
    } // end Submit
}
