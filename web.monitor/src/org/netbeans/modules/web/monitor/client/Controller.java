/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * Controller.java
 *
 *
 * Created: Fri Jan 19 16:21:06 2001
 *
 * @author Ana von Klopp
 * @version
 */

/* 
 * PENDING: 

 * This class currently holds a hashtable full of beans corresponding
 * to the transaction data. I'm not sure what the best thing to do is
 * w.r.t. keeping those beans in memory or not - that might be
 * huge. Need to consort with somebody that's good at that sort of
 * thing. Perhaps like the last five or so that the user has been
 * looking at are a good idea to keep. 
 *
 * The reason for doing that was to have a quick fix w.r.t. reading in 
 * files. Once I have figured out a way to parse the XML file quickly
 * for just the monitor attributes that should be unnecessary. 
 * 
 */

package  org.netbeans.modules.web.monitor.client;

import java.util.*;
import java.io.*;
import java.net.*;

import java.text.MessageFormat; 

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedArray;
import org.openide.options.*;
import org.openide.util.NbBundle;

// Note dependency on other module! 
import org.netbeans.modules.httpserver.*;

import org.netbeans.modules.web.monitor.server.Constants;
import org.netbeans.modules.web.monitor.data.*;


public class Controller  {

    static java.util.ResourceBundle resBundle =
	NbBundle.getBundle(TransactionView.class);

    public static final boolean debug = false;
    //private transient static boolean starting = true;

    // Test server location and port
    // Should use InetAddress.getLocalhost() instead
    private transient static String server = "localhost"; //NOI18N
    private transient static int port = 8080;

    // Location of the files
    private static FileObject monDir = null;
    private static FileObject currDir = null;
    private static FileObject saveDir = null;
    private static FileObject replayDir = null;

    public final static String monDirStr = "HTTPMonitor"; // NOI18N
    public final static String currDirStr = "current"; // NOI18N
    public final static String saveDirStr = "save"; // NOI18N
    public final static String replayDirStr = "replay"; // NOI18N

    // Constant nodes etc we need to know about
    private transient  NavigateNode root = null;
    private Children.SortedArray currTrans = null;
    private Children.SortedArray  savedTrans = null;

    // These are the ones that should go. 
    private Hashtable currBeans = null;
    private Hashtable saveBeans = null;
    
    //private Node[] transactionNodes = null;
    //private Node[] savedNodes = null;

    private transient Comparator comp = null;

    private boolean useBrowserCookie = true;
    
    public Controller() {

	currBeans = new Hashtable();
	saveBeans = new Hashtable();
	
	createNodeStructure();
	if(debug) log("Creating port server"); // NOI18N
	PortServer ps = new PortServer();
	if(debug) log("Created port server, about to run it"); // NOI18N
	ps.start();
	if(debug) log("Running port server"); // NOI18N
    }

    /**
     * Invoked at startup, creates the root folder and the folder for
     * current and saved transactions (and their children arrays).
     */
    private void createNodeStructure() {

	comp = new CompTime(true);
	currTrans = new Children.SortedArray();
	currTrans.setComparator(comp);
	savedTrans = new Children.SortedArray();
	savedTrans.setComparator(comp);

	CurrNode currNode = new CurrNode(currTrans);
	SavedNode savedNode = new SavedNode(savedTrans);

	Node[] kids = new Node[2];
	kids[0] = currNode;
	kids[1] = savedNode;

	Children children = new Children.Array();
	children.add(kids);
	root = new NavigateNode(children);
    }

    public void cleanup() {
	deleteDirectory(currDirStr);
    }

    /**
     * Adds a transaction to the list of current transactions.
     */
    void addTransaction(String str) {
	TransactionNode[] nodes = new TransactionNode[1];
	nodes[0] = new TransactionNode(str);
	currTrans.add(nodes);
    }

    /**
     * Adds a transaction to the list of current transactions.
     */
    protected NavigateNode getRoot() {
	return root;
    }


    protected static FileObject getMonDir() throws FileNotFoundException {
	 
	if(monDir == null || !monDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return monDir;
    }
    

    protected static FileObject getCurrDir() throws FileNotFoundException {
	 
	if(currDir == null || !currDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return currDir;
    }

    protected static FileObject getReplayDir() throws FileNotFoundException {
	 
	if(replayDir == null || !replayDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return replayDir;
    }
    

    protected static FileObject getSaveDir() throws FileNotFoundException {
	 
	if(saveDir == null || !saveDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return saveDir;
    }

    public boolean haveDirectories() {
	if(currDir == null) {
	    try {
		currDir = getCurrDir();
	    }
	    catch(Exception ex) {
		return false;
	    }
	}
	
	if(saveDir == null) {
	    try {
		saveDir = getSaveDir();
	    }
	    catch(Exception ex) {
		return false;
	    }
	}
	return true;
    }
    

    private static void createDirectories() throws FileNotFoundException {

	if(debug) log("Now in createDirectories()"); // NOI18N

	FileLock lock = null;

	TopManager tm = TopManager.getDefault();
	if(debug) log("TopManager: " + tm.toString()); // NOI18N
	
	FileObject rootdir = 
	    tm.getRepository().getDefaultFileSystem().getRoot();
	if(debug) {
	    log("Root directory is " +  rootdir.getName()); // NOI18N
	    File rootF = NbClassPath.toFile(rootdir);
	    log("Root directory abs path " + // NOI18N
		rootF.getAbsolutePath());
	}

	if(monDir == null || !monDir.isFolder()) {

	    try {
		monDir = rootdir.getFileObject(monDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(monDir == null || !monDir.isFolder()) {
		if(monDir != null) {
		    try {
			lock = monDir.lock();
			monDir.delete(lock);
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    lock.releaseLock();
			
		}
		try {
		    monDir = rootdir.createFolder(monDirStr);
		}
		catch(Exception ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(monDir == null || !monDir.isFolder()) 
		throw new FileNotFoundException();
	}

	if(debug) 
	    log("monitor directory is " + monDir.getName());// NOI18N

	// Current directory

	if(currDir == null || !currDir.isFolder()) {

	    try {
		currDir = monDir.getFileObject(currDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(currDir == null || !currDir.isFolder()) {
		if(currDir != null) {
		    try {
			lock = currDir.lock();
			currDir.delete(lock);
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    lock.releaseLock();
		}
		try {
		    currDir = monDir.createFolder(currDirStr);
		}
		catch(Exception ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(currDir == null || !currDir.isFolder()) 
		throw new FileNotFoundException();
	}
	
	if(debug) log("curr directory is " + currDir.getName()); // NOI18N



	// Save Directory

	if(saveDir == null || !saveDir.isFolder()) {

	    try {
		saveDir = monDir.getFileObject(saveDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(saveDir == null || !saveDir.isFolder()) {
		if(saveDir != null) {
		    try {
			lock = saveDir.lock();
			saveDir.delete(lock);
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    lock.releaseLock();
		}
		try {
		    saveDir = monDir.createFolder(saveDirStr);
		}
		catch(Exception ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(saveDir == null || !saveDir.isFolder()) 
		throw new FileNotFoundException();

	    if(debug) 
		log("save directory is " + saveDir.getName()); // NOI18N
	}

	// Replay Directory

	if(replayDir == null || !replayDir.isFolder()) {

	    try {
		replayDir = monDir.getFileObject(replayDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(replayDir == null || !replayDir.isFolder()) {
		if(replayDir != null) {
		    try {
			lock = replayDir.lock();
			replayDir.delete(lock);
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    lock.releaseLock();
		}
		try {
		    replayDir = monDir.createFolder(replayDirStr);
		}
		catch(Exception ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(replayDir == null || !replayDir.isFolder()) 
		throw new FileNotFoundException();

	    if(debug) 
		log("replay directory is " + replayDir.getName());// NOI18N
	}
    }


    /**
     * Invoked by ReplayAction. Replays the transaction corresponding to
     * the selected node.
     *
     * PENDING - it would be better if the nodes know which server
     * they were processed on. This would be the case if we listed the 
     * nodes separately depending on the server that collected the
     * data. In that case we wouldn't have to get the MonitorData
     * object at all, and the ReplaySendXML servlet could just 
     * pass on the data. 
     *
     */
    public void replayTransaction(Node node) {

	if(debug) 
	    log("Replay transaction from node " + node.getName()); // NOI18N
		
	if(!checkServer(true)) return;
	TransactionNode tn = (TransactionNode)node;
	MonitorData md = getMonitorData(tn);
	if(!useBrowserCookie) 
	    md.getRequestData().setReplaceSessionCookie(true);

	if(debug) { 
	    log("Replace is " +  // NOI18N
		String.valueOf(md.getRequestData().getReplaceSessionCookie()));
    
	    try {
		StringBuffer buf = new StringBuffer
		    (System.getProperty("java.io.tmpdir")); // NOI18N
		buf.append(System.getProperty("file.separator")); // NOI18N
		buf.append("control-replay.xml"); // NOI18N
		File file = new File(buf.toString()); 
		FileOutputStream fout = new FileOutputStream(file);
		PrintWriter pw2 = new PrintWriter(fout);
		md.write(pw2);
		pw2.close();
		fout.close();
		log("Wrote replay data to " + // NOI18N 
		    file.getAbsolutePath()); 
		
	    }
	    catch(Throwable t) {
	    }
	}



	String status;
	if(tn.isCurrent()) status = currDirStr; 
	else status = saveDirStr; 
	try {
	    replayTransaction(md, status);
	}
	catch(UnknownHostException uhe) {
	    // Notify the user that there is no host

	    Object[] options = {
		resBundle.getString("MON_OK"),
	    };

	    Object[] args = {
		md.getServerName(),
	    };
	    
	    MessageFormat msgFormat = new MessageFormat
		(resBundle.getString("MON_Exec_server_no_host"));  
	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(msgFormat.format(args),
				     resBundle.getString("MON_Exec_server"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    TopManager.getDefault().notify(noServerDialog);

	}
	catch(IOException ioe) {

	    // Notify the user that the server is not running
	    Object[] options = {
		resBundle.getString("MON_OK"),
	    };

	    Object[] args = {
		md.getServerAndPort(),
	    };

	    MessageFormat msgFormat = new MessageFormat
		(resBundle.getString("MON_Exec_server_start")); 

	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(msgFormat.format(args), 
				     resBundle.getString("MON_Exec_server"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    TopManager.getDefault().notify(noServerDialog);
	}
    }

    /**
     * Invoked by EditPanel. Replays the transaction corresponding to
     * the selected node. 
     */
    public void replayTransaction(MonitorData md) 
	throws UnknownHostException, IOException  {
	
	if(debug)
	    log("Controller::replayTransaction(MD)"); //NOI18N
	
	FileLock lock = null;
	OutputStream out = null;
	PrintWriter pw = null;

	try {
	    
	    if(debug) 
		log("Creating record for replay"); //NOI18N
	    String id = md.getAttributeValue("id"); // NOI18N
	    FileObject fo = getReplayDir().createData(id, "xml"); //NOI18N
	    lock = fo.lock();
	    out = fo.getOutputStream(lock);
	    pw = new PrintWriter(out);
	    md.write(pw);	    
	    if(debug) log("...record complete"); //NOI18N

	    if(debug) {
		try {

		    StringBuffer buf = new StringBuffer
			(System.getProperty("java.io.tmpdir")); // NOI18N
		    buf.append(System.getProperty("file.separator")); // NOI18N
		    buf.append("control-record.xml"); // NOI18N
		    File file = new File(buf.toString()); 
		    FileOutputStream fout = new FileOutputStream(file);
		    PrintWriter pw2 = new PrintWriter(fout);
		    md.write(pw2);
		    pw2.close();
		    fout.close();
		    log("Wrote replay data to " + file.getAbsolutePath()); // NOI18N
		}
		catch(Throwable t) {
		}
	    }
	}
	catch(IOException ioex) {
	    // PENDING WARN THE USER
	}
	finally {
	    try {
		pw.close();
	    }
	    catch(Throwable t) {
	    }  
	    try {
		out.close();
	    }
	    catch(Throwable t) {
	    }  
	    try {
		lock.releaseLock();
		if(debug) 
		    log("Released lock on replay file"); // NOI18N
		
	    }
	    catch(Throwable t) {
		if(debug) 
		    log("Failed to release lock on replay file"); // NOI18N
		
	    }  
	}
	
	try {
	    replayTransaction(md, replayDirStr);
	}
	catch(UnknownHostException uhe) {
	    throw uhe;
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }
    
    /**
     *
     */
    public void replayTransaction(MonitorData md, String status)
	throws UnknownHostException, IOException  {
	if(debug) 
	    log("Replay transaction from transaction file "); //NOI18N 
	URL url = null;
	try {
	    String name = md.getServerName();
	    int port = md.getServerPort();
	    
	    StringBuffer uriBuf = new StringBuffer(128);
	    uriBuf.append(md.getRequestData().getAttributeValue("uri")); //NOI18N 
	    uriBuf.append("?ffj_resend="); //NOI18N 
	    uriBuf.append(md.getAttributeValue("id")); //NOI18N 
	    uriBuf.append("&ffj_status="); //NOI18N 
	    uriBuf.append(status);

	    if(md.getRequestData().getReplaceSessionCookie()) { 
		uriBuf.append("&netbeans_session=");
		uriBuf.append("12434");
	    }
	    url = new URL("http", name, port, uriBuf.toString()); //NOI18N 
	}
	catch(MalformedURLException me) { 
	    if(debug) log(me.getMessage());
	}
	catch(NumberFormatException ne) { 
	    if(debug) log(ne.getMessage());
	}

	// Send the url to the browser.
	try {
	    showReplay(url);
	}
	catch(UnknownHostException uhe) {
	    throw uhe;
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }

    public void saveTransaction(Node[] nodes) {

	if(!haveDirectories()) {
	    // PENDING - report the error properly
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N
	    return;
	}

	Node[] newNodes = new Node[nodes.length];
	TransactionNode oldNode; 
	String id;
	 
	for(int i=0; i < nodes.length; ++i) {
	    
	    oldNode = (TransactionNode)nodes[i];
	    id = oldNode.getID();
	    
	    if(debug) log("Controller: Saving " + id); //NOI18N 

	    if(currBeans.containsKey(id)) 
		saveBeans.put(id, currBeans.remove(id)); 
	    
	    // Note I didn't load the bean here yet. Will only do that 
	    // if the data is displayed. 
			      
	    try {
		FileObject fold = 
		    currDir.getFileObject(id, "xml"); //NOI18N
		FileLock lock = fold.lock();
		fold.copy(saveDir, id, "xml"); //NOI18N
		if(debug) log(fold.getName());
		fold.delete(lock);
		lock.releaseLock();
					      
		newNodes[i] = new TransactionNode(id, oldNode.getMethod(), 
						  oldNode.getURI(), false);
	    }
	    catch(Exception ex) {
		// PENDING report properly
	    }
	    
	}
	currTrans.remove(nodes);
	savedTrans.add(newNodes);
    }
  
    /**
     * Invoked by DeleteAction.  Deletes a saved transaction 
     */

    public void deleteTransaction(Node[] nodes) {

	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	// PENDING
	if((nodes == null) || (nodes.length == 0)) return;

	TransactionNode n = null;
	FileObject fold = null;
	FileLock lock = null;
	 
	for(int i=0; i < nodes.length; ++i) {
	    
	    n = (TransactionNode)nodes[i];
	    if(debug) 
		log("Deleting :" + n.toString()); //NOI18N 
	    
	    try {
		if(n.isCurrent()) {
		    if(debug) log("Deleting current"); //NOI18N 
		    fold = currDir.getFileObject(n.getID(), "xml"); //NOI18N
		    // PENDING
		    Node[] ns = { n };
		    currTrans.remove(ns);
		    currBeans.remove(n.getID());
		}
		
		else {
		    if(debug) log("Deleting saved"); //NOI18N 
		    fold = saveDir.getFileObject(n.getID(), "xml");//NOI18N
		    // PENDING
		    Node[] ns = { n };
		    savedTrans.remove(ns);
		    saveBeans.remove(n.getID());
		}
		lock = fold.lock();
		if(debug) 
		    log("Deleting: " + fold.getName()); //NOI18N 
			
		
		fold.delete(lock); 
		lock.releaseLock();
	    }
	    catch(Exception ex) {
		// PENDING report properly
		if(debug) log("Failed :" + n.getID()); //NOI18N 
					     
	    }
	}
    }
    
    void deleteDirectory(String dir) {

	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	FileObject directory = null;
	if(dir.equals(saveDirStr)) {
	    directory = saveDir;
	    savedTrans.remove(savedTrans.getNodes());
	    saveBeans.clear();
	}
	
	else {   
	    directory = currDir;
	    currTrans.remove(currTrans.getNodes());
	    currBeans.clear();
	}
	
	FileLock lock = null;
	Enumeration e = directory.getData(false);
	while(e.hasMoreElements()) {
	    FileObject fo = (FileObject) e.nextElement();
	    try {
		lock = fo.lock();
		fo.delete(lock);
		lock.releaseLock();
	    }
	    catch(Exception ex) {
		// PENDING report properly
	    }
	}
    }

    void deleteTransactions() {
	deleteDirectory(Constants.Files.save);
	deleteDirectory(Constants.Files.current);
	savedTrans.remove(savedTrans.getNodes());
	currTrans.remove(currTrans.getNodes());
    }


    void getTransactions() {

	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	FileLock lock = null;
	Enumeration e = null;
	Vector nodes = null; 
	InputStreamReader in = null;
	 

	currTrans.remove(currTrans.getNodes());
	nodes = new Vector();

	e = currDir.getData(false);
	while(e.hasMoreElements()) {

	    FileObject fo = (FileObject) e.nextElement();
	    String id = fo.getName();
	    if(debug) 
		log("getting current transaction: " + id); //NOI18N 
		    
	    
	    if(currBeans.containsKey(id)) {
		MonitorData md = (MonitorData)(currBeans.get(id));
		nodes.add(md.createTransactionNode(true)); 
	    }
	    else {
		try {
		    lock = fo.lock();
		    in = new InputStreamReader(fo.getInputStream());
		    MonitorData md = MonitorData.createGraph(in);
		    currBeans.put(id, md);
		    nodes.add(md.createTransactionNode(true));
		}
		catch(IOException ioe) {
		    String message = ioe.getMessage();
		    if(message == null || message.equals("")) //NOI18N
			message = resBundle.getString("MON_Bad_input");
		    log(message);
		    if (debug) ioe.printStackTrace();
		}
		catch(Exception ex) {
		    log(resBundle.getString("MON_Bad_input"));
		    if(debug) ex.printStackTrace();
		}
		finally {
		    try {
			in.close();
		    }
		    catch(Throwable t) {}

		    try {
			lock.releaseLock();
		    }
		    catch(Throwable t) {}
		}
	    }
	}
	    
	int numtns = nodes.size();
	TransactionNode[] tns = new TransactionNode[numtns]; 
	for(int i=0;i<numtns;++i) 
	    tns[i] = (TransactionNode)nodes.elementAt(i);
	currTrans.add(tns);
	// end of region

	// Get the saved transactions
	savedTrans.remove(savedTrans.getNodes());
	nodes = new Vector();

	e = saveDir.getData(false);
	while(e.hasMoreElements()) {

	    FileObject fo = (FileObject) e.nextElement();
	    String id = fo.getName();
	    if(debug) 
		log("getting saved transaction: " + id); //NOI18N 
		    
	    
	    if(saveBeans.containsKey(id)) {
		MonitorData md = (MonitorData)(saveBeans.get(id));
		nodes.add(md.createTransactionNode(false)); 
	    }
	    else {
		try {
		    lock = fo.lock();
		    in = new InputStreamReader(fo.getInputStream());
		    MonitorData md = MonitorData.createGraph(in);
		    saveBeans.put(id, md);
		    nodes.add(md.createTransactionNode(false));
		}
		catch(Exception ex) {
		    // PENDING report properly
		}
		finally {
		    try {
			in.close();
		    }
		    catch(Throwable t) {}
		    
		    try {
			lock.releaseLock();
		    }
		    catch(Throwable t) {}
		}
	    }
	}
	numtns = nodes.size();
	tns = new TransactionNode[numtns]; 
	for(int i=0;i<numtns;++i) {
	    tns[i] = (TransactionNode)nodes.elementAt(i);
	    if(debug) 
		log("Adding saved node" + tns[i].toString()); //NOI18N 
		    
	}
	savedTrans.add(tns);
    }
    
	    
    /**
     * Sets the machine name and port of the web server. Not used in
     * this version, we do not support remote debugging.
     */
    public static void setServer(String loc, int p) {
	port = p;
	server = loc;
	return;
    }

    public void setComparator(Comparator comp) {
	currTrans.setComparator(comp);
	savedTrans.setComparator(comp);
    }

    public void setUseBrowserCookie(boolean value) { 
	useBrowserCookie = value;
	if(debug) 
	    log("Setting useBrowserCookie to " + //NOI18N
		String.valueOf(useBrowserCookie));
    }
    
    // PENDING - should just pass the ID to replay, would be more
    // efficient. 

    public MonitorData getMonitorData(TransactionNode node) {
	return getMonitorData(node, true);
    }
    
    public MonitorData getMonitorData(TransactionNode node, boolean cached) {

	if(debug) log("Entered getMonitorData()"); //NOI18N 
	
	String id = node.getID();
	Hashtable ht = null;
	FileObject dir = null;
	 
	if(node.isCurrent()) {
	    ht = currBeans;
	    dir = currDir;
	    if(debug) log("node is current"); //NOI18N 
	}
	else {
	    ht = saveBeans;
	    dir = saveDir;
	}
	
	if(debug) {
	    log("node id is " + node.getID()); //NOI18N 
	    log("using directory " + dir.getName()); //NOI18N 
	}

	if (!cached) {
	    return retrieveMonitorData(id, dir);
	}
	
	if(!ht.containsKey(id)) {
	    if(debug) 
		log("Node is not in the hashtable yet"); //NOI18N 
	    MonitorData md = retrieveMonitorData(id, dir);
	    ht.put(id, md);


	}
        
	return (MonitorData)ht.get(id);
    }

    MonitorData retrieveMonitorData(String id, String dirS) {

	if(debug) 
	    log("Controller::retrieveMonitorData(String, String)"); //NOI18N 
	if(!haveDirectories()) {
	    // PENDING - report the error property
	    log("Couldn't get the directory"); //NOI18N 
	    return null;
	}
	
	FileObject dir = null;
	
	if (dirS.equalsIgnoreCase(currDirStr))  dir = currDir;
	else if (dirS.equalsIgnoreCase(saveDirStr)) dir = saveDir;
	else if (dirS.equalsIgnoreCase(replayDirStr)) dir = replayDir;

	if(debug) log("Directory = " + dir.getName()); //NOI18N 
	return retrieveMonitorData(id, dir);
    }
    

    MonitorData retrieveMonitorData(String id, FileObject dir) {

	if(debug)
	    log("Controller::retrieveMonitorData(String, FileObject)"); //NOI18N 
	if(!haveDirectories()) {
	    // PENDING - report the error property
	    log("Couldn't get the directory"); //NOI18N 
	    return null;
	}
	
	MonitorData md = null;

	FileObject fo = dir.getFileObject(id, "xml"); // NOI18N
	if(debug) log("From file: " + fo.getName()); //NOI18N 

	FileLock lock = null; 
	InputStreamReader in = null;
	
	try {
	    if(debug) log("Locking " + fo.getName()); //NOI18N 
	    lock = fo.lock();
	    if(debug) log("Getting InputStreamReader"); //NOI18N 
	    in = new InputStreamReader(fo.getInputStream()); 
	    if(debug) log("Creating monitordata"); //NOI18N 
	    md = MonitorData.createGraph(in);
	} 
	catch(Exception ex) {
	    log("Controller couldn't read the file..."); //NOI18N 
	    ex.printStackTrace();
	}
	finally {
	    try {
		if(dir == replayDir) 
		    fo.delete(lock);
	    }
	    catch(Exception ex) {
	    }
	    
	    try {
		in.close();
	    }
	    catch(Throwable t) {}
	    try {
		lock.releaseLock();
	    }
	    catch(Throwable t) {}
	    fo = null;
	}
	if(debug) log("We're done!"); //NOI18N 
	return md;
    }

    boolean checkServer(boolean replay) {

	boolean serverRunning = true;

	try {
	    if(debug) 
		log("Getting the server setting"); //NOI18N 
	    
	    HttpServerSettings setting = 
		(HttpServerSettings)SystemOption.findObject 
		(HttpServerSettings.class);
	    if(setting == null) {
		if(debug) 
		    log("No server setting object"); //NOI18N 
		serverRunning = false; 
	    }
	    else if(!setting.isRunning()) {
		if(debug) 
		    log("Server is not running"); //NOI18N 
		serverRunning = false;
	    }
	}
	catch(Exception ex) {
	    serverRunning = false;
	}
	if(!serverRunning) {

	    Object[] options = {
		resBundle.getString("MON_OK"),
	    };
	    String msg = null;
	    if(replay) msg = resBundle.getString("MON_CantReplay"); 
	    else { 
		msg = resBundle.getString("MON_NoServer");
	    }
	    
	    msg = msg.concat(" ");
	    msg = msg.concat(resBundle.getString("MON_Start_server"));
		
	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(msg,
				     resBundle.getString("MON_NoServerTitle"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    TopManager.getDefault().notify(noServerDialog);
	}
	return serverRunning;
    }

    private void showReplay(URL url) throws UnknownHostException,
	                                    IOException {
	
	if(debug) log("Controller::showReplay()"); // NOI18N
	if(debug) log("Controller::showReplay() url is " + url.toString()); // NOI18N
	// First we check that we can find a host of the name that's
	// specified 
	ServerCheck sc = new ServerCheck(url.getHost());
	Thread t = new Thread(sc);
	t.start();
	try {
	    t.join(2000);
	}
	catch(InterruptedException ie) {
	    t.destroy();
	}
	t.stop();
	if(!sc.isServerGood()) {
	    if(debug) 
		log("Controller::showReplay(): No host"); // NOI18N
	    throw new UnknownHostException();
	}
	
	if(debug) log("Controller::performed server check"); // NOI18N

	// Next we see if we can connect to the server
	try {
	    Socket server = new Socket(url.getHost(), url.getPort());
	    server.close();
	    server = null;
	}
	catch(UnknownHostException uhe) {
	    if(debug) log("Controller::showReplay(): uhe2"); // NOI18N
	    throw uhe;
	}
	catch(IOException ioe) {
	    if(debug) 
		log("Controller::showReplay(): No service"); // NOI18N
	    throw ioe;
	}
	
	if(debug) log("Controller::showReplay(): reaching the end..."); // NOI18N
	// Finally we ask the browser to show it
	org.netbeans.modules.web.core.WebExecUtil.showInBrowser(url,"text/html"); // NOI18N
	 
    }

    // PENDING - use the logger instead
    private static void log(final String s) {
	System.out.println(s);
    }
    

    
    /**
     * Does the server we try to replay on exist? 
     */
    class ServerCheck implements Runnable {	 

	boolean serverGood = false;
	String serverName = null;
	
	public ServerCheck(String name) {
	    serverName = name;
	}
	
	public void run() {
	    try {
		InetAddress.getByName(serverName);
		serverGood = true;
		
	    }
	    catch (UnknownHostException e) {
		serverGood = false; 
	    }	 
	}
	
	public boolean isServerGood() {
	    return serverGood;
	}
	
    }

    /**
     * Sort by time
     */
    class CompTime implements Comparator {

	boolean descend = true;

	CompTime(boolean descend) {
	    this.descend = descend;
	}

	public int compare(Object o1, Object o2) {

	    if(debug) log("In compareTime"); //NOI18N
	    TransactionNode n1 = (TransactionNode)o1;
	    TransactionNode n2 = (TransactionNode)o2;

	    if(debug) log("Cast the nodes"); //NOI18N
	    if(debug) {
		log("Comparing " + String.valueOf(o1) + //NOI18N
		    " and " + String.valueOf(o2)); //NOI18N
		try {
		    log(n1.getID());
		    log(n2.getID());
		}
		catch(Exception ex) {};
	    }
	    int result;
	    if(descend)
		result = n1.getID().compareTo(n2.getID());
	    else result = n2.getID().compareTo(n1.getID());
	    if(debug) log("End of compareTime"); //NOI18N
	    return result;
	}
    }

    // Really dumb way of forcing this, but I couldn't get the tree to 
    // repaint... Will remove this method when that works. 
    public void updateNodeNames() {
	
	TransactionNode tn;
	
	Node[] nodes = currTrans.getNodes();
	int size = nodes.length;
	for(int i=0; i<size; ++i) {
	    tn = (TransactionNode)nodes[i];
	    tn.setNameString();
	}
	
	nodes = savedTrans.getNodes();
	size = nodes.length;
	for(int i=0; i<size; ++i) {
	    tn = (TransactionNode)nodes[i];
	    tn.setNameString();
	}
    }
    
    /**
     * Sort alphabetically
     */
    class CompAlpha implements Comparator {

	public int compare(Object o1, Object o2) {
	    if(debug) log("In compareAlpha"); //NOI18N
	    TransactionNode n1 = (TransactionNode)o1;
	    TransactionNode n2 = (TransactionNode)o2;
	    if(debug) log("cast the nodes"); //NOI18N
	    if(debug) {
		log("Comparing " + String.valueOf(o1) + //NOI18N
		    " and " + String.valueOf(o2)); //NOI18N
		try {
		    log("names"); //NOI18N
		    log(n1.getName());
		    log(n2.getName());
		    log("IDs");  //NOI18N
		    log(n1.getID());
		    log(n2.getID());
		}
		catch(Exception ex) {};
	    }
	    int diff = n1.getName().compareTo(n2.getName());
	    if(diff == 0)
		return n1.getID().compareTo(n2.getID());
	    else
		return diff;
	}
    }
} // Controller
