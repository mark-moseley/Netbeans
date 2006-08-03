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
package org.openide.awt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.html.*;


/**
* Implementation of BrowserImpl in Swing.
*/
final class SwingBrowserImpl extends HtmlBrowser.Impl implements Runnable {
    /** state of history management */
    private static final int NO_NAVIGATION = 1;
    private static final int NAVIGATION_BACK = 2;
    private static final int NAVIGATION_FWD = 3;
    private static RequestProcessor rp = new RequestProcessor("Swing Browser"); //NOI18N

    /** Current URL. */
    private URL url;

    /** URL loaded by JEditorPane */
    private URL loadingURL;
    private PropertyChangeSupport pcs;
    private String statusMessage = ""; // NOI18N
    private SwingBrowser swingBrowser;

    /** list of accessed URLs for back/fwd navigation */
    private Vector historyList;

    /** current position in history */
    private int historyIndex;

    /** navigation indication */
    private int historyNavigating = NO_NAVIGATION;
    private String title = null;
    boolean fetchingTitle = false;

    private static Logger LOG = Logger.getLogger(SwingBrowserImpl.class.getName());

    SwingBrowserImpl() {
        pcs = new PropertyChangeSupport(this);
        swingBrowser = new SwingBrowser();
        historyList = new Vector(5, 3);
        historyIndex = -1;
        swingBrowser.addPropertyChangeListener(
            "page", // NOI18N
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getNewValue() instanceof URL) {
                        URL old = SwingBrowserImpl.this.url;
                        SwingBrowserImpl.this.url = (URL) evt.getNewValue();
                        SwingBrowserImpl.this.pcs.firePropertyChange(PROP_URL, old, url);

                        if (((URL) evt.getNewValue()).equals(loadingURL)) {
                            loadingURL = null;
                        }

                        // update history
                        if (historyNavigating == NAVIGATION_BACK) {
                            int idx = historyList.lastIndexOf(evt.getNewValue(), historyIndex - 1);

                            if (idx != -1) {
                                historyIndex = idx;
                            }
                        } else if (historyNavigating == NAVIGATION_FWD) {
                            int idx = historyList.indexOf(evt.getNewValue(), historyIndex + 1);

                            if (idx != -1) {
                                historyIndex = idx;
                            }
                        } else {
                            while (historyList.size() > (historyIndex + 1))
                                historyList.remove(historyList.size() - 1);

                            historyList.add(evt.getNewValue());
                            historyIndex = historyList.size() - 1;
                        }

                        historyNavigating = NO_NAVIGATION;
                        pcs.firePropertyChange(PROP_BACKWARD, null, null);
                        pcs.firePropertyChange(PROP_FORWARD, null, null);
                        SwingUtilities.invokeLater(SwingBrowserImpl.this);
                    }
                }
            }
        );
    }

    /**
    * Returns visual component of html browser.
    *
    * @return visual component of html browser.
    */
    public java.awt.Component getComponent() {
        return swingBrowser;
    }

    /**
    * Reloads current html page.
    */
    public synchronized void reloadDocument() {
        try {
            if ((url == null) || (loadingURL != null)) {
                return;
            }

            Document doc = swingBrowser.getDocument();
            loadingURL = url;

            if (doc instanceof AbstractDocument) {
                String protocol = url.getProtocol();

                if ("ftp".equalsIgnoreCase(protocol) // NOI18N
                         ||"http".equalsIgnoreCase(protocol) // NOI18N
                ) {
                    ((AbstractDocument) doc).setAsynchronousLoadPriority(Thread.NORM_PRIORITY);
                } else {
                    ((AbstractDocument) doc).setAsynchronousLoadPriority(-1);
                }
            }

            rp.post(this);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            pcs.firePropertyChange(PROP_STATUS_MESSAGE, null, statusMessage = "" + e); // NOI18N
        }
    }

    /**
    * Stops loading of current html page.
    */
    public void stopLoading() {
    }

    /**
    * Sets current URL.
    *
    * @param url URL to show in the browser.
    */
    public synchronized void setURL(URL url) {
        try {
            if (url == null) {
                return;
            }

            loadingURL = url;

            rp.post(this);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            pcs.firePropertyChange(PROP_STATUS_MESSAGE, null, statusMessage = "" + e); // NOI18N
        }
    }

    /**
    * Returns current URL.
    *
    * @return current URL.
    */
    public URL getURL() {
        return url;
    }

    /**
    * Returns status message representing status of html browser.
    *
    * @return status message.
    */
    public String getStatusMessage() {
        return statusMessage;
    }

    /** Returns title of the displayed page.
    * @return title
    */
    public String getTitle() {
        if (title == null) {
            Mutex.EVENT.readAccess(this);
        }

        return (title == null) ? NbBundle.getMessage(SwingBrowserImpl.class, "LBL_Loading") : title; //NOI18N
    }

    void updateTitle() {
        assert SwingUtilities.isEventDispatchThread();

        //        System.err.println("Update title");
        if (fetchingTitle) {
            //            System.err.println("  ...already updating");
            return;
        }

        fetchingTitle = true;

        String oldTitle = getTitle();

        try {
            Document d = swingBrowser.getDocument();
            title = (String) d.getProperty(HTMLDocument.TitleProperty);

            //            System.err.println("Title from document is " + title);
            if ((title == null) || (title.trim().length() == 0)) {
                //                System.err.println("No title from document, trying from url ");
                URL url = getURL();

                if (url != null) {
                    title = url.getFile();

                    if (title.length() == 0) {
                        title = NbBundle.getMessage(SwingBrowserImpl.class, "LBL_Untitled"); //NOI18N
                    } else {
                        //Trim any extraneous path info
                        int i = title.lastIndexOf("/"); //NOI18N

                        if ((i != -1) && (i != (title.length() - 1))) {
                            title = title.substring(i + 1);
                        }
                    }

                    //                 System.err.println("Using from url: " + title);
                }
            }

            if (title != null) {
                if (title.length() > 60) {
                    //Truncate to a reasonable tab length
                    title = NbBundle.getMessage(
                            SwingBrowserImpl.class, "LBL_Title", new Object[] { title.substring(0, 57) }
                        );
                }

                if (!oldTitle.equals(title)) {
                    //                    System.err.println("Firing prop change from " + oldTitle + " to " + title);
                    pcs.firePropertyChange(PROP_TITLE, oldTitle, title); //NOI18N
                }
            }
        } finally {
            fetchingTitle = false;
        }
    }

    public void run() {
        if (SwingUtilities.isEventDispatchThread()) {
            title = null;
            updateTitle();
        } else {
            synchronized (this) {
                try {
                    if ((this.url != null) && this.url.sameFile(url)) {
                        Document doc = swingBrowser.getDocument();

                        if (doc != null) {
                            //force reload
                            doc.putProperty(Document.StreamDescriptionProperty, null);
                        }
                    }

                    swingBrowser.setPage(loadingURL);
                    setStatusText(null);
                } catch (java.net.UnknownHostException uhe) {
                    setStatusText(
                        NbBundle.getMessage(SwingBrowserImpl.class, "FMT_UnknownHost", new Object[] { loadingURL })
                    ); // NOI18N
                } catch (java.net.NoRouteToHostException nrthe) {
                    setStatusText(
                        NbBundle.getMessage(SwingBrowserImpl.class, "FMT_NoRouteToHost", new Object[] { loadingURL })
                    ); // NOI18N
                } catch (IOException ioe) {
                    setStatusText(
                        NbBundle.getMessage(SwingBrowserImpl.class, "FMT_InvalidURL", new Object[] { loadingURL })
                    ); // NOI18N
                }

                SwingUtilities.invokeLater(this);
                loadingURL = null;
            }
        }
    }

    /**
     * Accessor to allow a message about bad urls to be displayed - see
     * HtmlBrowser.setURL().
     */
    void setStatusText(String s) {
        pcs.firePropertyChange(PROP_STATUS_MESSAGE, null, statusMessage = s); // NOI18N
    }

    /** Is forward button enabled?
    * @return true if it is
    */
    public boolean isForward() {
        return (historyIndex >= 0) && (historyIndex < (historyList.size() - 1)) &&
        (historyNavigating == NO_NAVIGATION);
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void forward() {
        if (isForward()) {
            historyNavigating = NAVIGATION_FWD;
            setURL((URL) historyList.elementAt(historyIndex + 1));
        }
    }

    /** Is backward button enabled?
    * @return true if it is
    */
    public boolean isBackward() {
        return (historyIndex > 0) && (historyIndex < historyList.size()) && (historyNavigating == NO_NAVIGATION);
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void backward() {
        if (isBackward()) {
            historyNavigating = NAVIGATION_BACK;
            setURL((URL) historyList.elementAt(historyIndex - 1));
        }
    }

    /** Is history button enabled?
    * @return true if it is
    */
    public boolean isHistory() {
        return false;
    }

    /** Invoked when the history button is pressed.
    */
    public void showHistory() {
    }

    /**
    * Adds PropertyChangeListener to this browser.
    *
    * @param l Listener to add.
    */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
    * Removes PropertyChangeListener from this browser.
    *
    * @param l Listener to remove.
    */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // encoding support; copied from html/HtmlEditorSupport
    private static String findEncodingFromURL(InputStream stream) {
        try {
            byte[] arr = new byte[4096];
            int len = stream.read(arr, 0, arr.length);
            String txt = new String(arr, 0, (len >= 0) ? len : 0).toUpperCase();

            // encoding
            return findEncoding(txt);
        } catch (Exception x) {
            x.printStackTrace();
        }

        return null;
    }

    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    private static String findEncoding(String txt) {
        int headLen = txt.indexOf("</HEAD>"); // NOI18N

        if (headLen == -1) {
            headLen = txt.length();
        }

        int content = txt.indexOf("CONTENT-TYPE"); // NOI18N

        if ((content == -1) || (content > headLen)) {
            return null;
        }

        int charset = txt.indexOf("CHARSET=", content); // NOI18N

        if (charset == -1) {
            return null;
        }

        int charend = txt.indexOf('"', charset);
        int charend2 = txt.indexOf('\'', charset);

        if ((charend == -1) && (charend2 == -1)) {
            return null;
        }

        if (charend2 != -1) {
            if ((charend == -1) || (charend > charend2)) {
                charend = charend2;
            }
        }

        return txt.substring(charset + "CHARSET=".length(), charend); // NOI18N
    }

    // innerclasses ..............................................................
    private class SwingBrowser extends JEditorPane {
        private boolean lastPaintException = false;

        private SwingBrowser() {
            setEditable(false);
            addHyperlinkListener(
                new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            if (e instanceof HTMLFrameHyperlinkEvent) {
                                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                                HTMLDocument doc = (HTMLDocument) getDocument();
                                URL old = getURL();
                                doc.processHTMLFrameHyperlinkEvent(evt);
                                pcs.firePropertyChange(PROP_URL, old, e.getURL());
                            } else {
                                try {
                                    SwingBrowserImpl.this.setURL(e.getURL());
                                } catch (Exception ex) {
                                    LOG.log(Level.WARNING, null, ex);
                                }
                            }
                        }
                    }
                }
            );

            //when up/down arrow keys are pressed, ensure the whole browser content 
            //scrolls up/down instead of moving the caret position only
            ActionMap actionMap = getActionMap();
            actionMap.put(DefaultEditorKit.upAction, new ScrollAction(-1));
            actionMap.put(DefaultEditorKit.downAction, new ScrollAction(1));
        }

        /**
         * Fetches a stream for the given URL, which is about to
         * be loaded by the <code>setPage</code> method.
         * This method is expected to have the the side effect of
         * establishing the content type, and therefore setting the
         * appropriate <code>EditorKit</code> to use for loading the stream.
         * <p>
         * If debugger is not running returns super implementation.
         * <p>
         * If debugger runs it will set content type to text/html.
         * Forwarding is not supported is that case.
         * <p>Control using sysprop org.openide.awt.SwingBrowserImpl.do-not-block-awt=true.
         *
         * @param page  the URL of the page
         */
        protected InputStream getStream(URL page) throws IOException {
            SwingUtilities.invokeLater(SwingBrowserImpl.this);

            // #53207: pre-read encoding from loaded URL
            String charset = findEncodingFromURL(page.openStream());
            LOG.log(Level.FINE, "Url " + page + " has charset " + charset); // NOI18N

            if (charset != null) {
                putClientProperty("charset", charset);
            }

            // XXX debugger ought to set this temporarily
            if (Boolean.getBoolean("org.openide.awt.SwingBrowserImpl.do-not-block-awt")) {
                // try to set contentType quickly and return (don't block AWT Thread)
                setContentType("text/html"); // NOI18N

                return new FilteredInputStream(page.openConnection(), SwingBrowserImpl.this);
            } else {
                return super.getStream(page);
            }
        }

        public Dimension getPreferredSize() {
            try {
                return super.getPreferredSize();
            } catch (RuntimeException e) {
                //Bug in javax.swing.text.html.BlockView
                return new Dimension(400, 600);
            }
        }

        public void paint(Graphics g) {
            try {
                super.paint(g);
                lastPaintException = false;
            } catch (RuntimeException e) {
                //Bug in javax.swing.text.html.BlockView
                //do nothing
                if (!lastPaintException) {
                    repaint();
                }

                lastPaintException = true;
            }
        }

        /**
         * An action to scroll the browser content up or down.
         */
        private class ScrollAction extends AbstractAction {
            int direction;

            public ScrollAction(int direction) {
                this.direction = direction;
            }

            public void actionPerformed(java.awt.event.ActionEvent e) {
                Rectangle r = getVisibleRect();
                int increment = getScrollableUnitIncrement(r, SwingConstants.VERTICAL, direction);
                r.y += (increment * direction);
                scrollRectToVisible(r);
            }
        }
    }

    /**
     * FilterInputStream that delays opening of stream.
     * The purpose is not to initialize the stream when it is created in getStream()
     * but to do it later when the content is asynchronously loaded in separate thread.
     */
    private static class FilteredInputStream extends FilterInputStream {
        private final URLConnection conn;
        private final SwingBrowserImpl browser;

        FilteredInputStream(URLConnection conn, SwingBrowserImpl browser) {
            super((FilterInputStream) null);
            this.conn = conn;
            this.browser = browser;
        }

        private synchronized void openStream() throws IOException {
            if (in == null) {
                in = conn.getInputStream();
            }
        }

        public int available() throws IOException {
            openStream();

            return super.available();
        }

        public long skip(long n) throws IOException {
            openStream();

            return super.skip(n);
        }

        public void reset() throws IOException {
            openStream();
            super.reset();
        }

        public void close() throws IOException {
            openStream();
            super.close();
            Mutex.EVENT.readAccess(browser);
        }

        public int read(byte[] b) throws IOException {
            openStream();

            return super.read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            openStream();

            return super.read(b, off, len);
        }

        public int read() throws IOException {
            openStream();

            return super.read();
        }
    }
}
