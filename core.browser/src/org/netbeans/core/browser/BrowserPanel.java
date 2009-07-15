package org.netbeans.core.browser;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.browser.MozillaExecutor;
import org.mozilla.browser.MozillaKeyEvent;
import org.mozilla.browser.MozillaMouseEvent;
import org.mozilla.browser.MozillaPanel;
import org.mozilla.browser.MozillaRuntimeException;
import org.mozilla.browser.impl.ChromeAdapter;
import org.mozilla.browser.impl.DOMUtils;
import org.mozilla.dom.NodeFactory;
import org.mozilla.interfaces.nsIDOMRange;
import org.mozilla.interfaces.nsISelection;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebBrowserFocus;
import org.netbeans.core.browser.api.WebBrowserEvent;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static org.mozilla.browser.XPCOMUtils.qi;
import static org.mozilla.browser.MozillaExecutor.mozAsyncExec;

/**
 * Extends MozSwing's browser component to handle various browser events.
 *
 * @author S. Aubrecht
 */
public class BrowserPanel extends MozillaPanel {

    private final PropertyChangeSupport propSupport;
    private final BrowserCallback callback;
    private boolean backEnabled = false;
    private boolean forwardEnabled = false;
    private String statusText;
    private String url;
    private String title;
    private Document doc;

    public BrowserPanel(PropertyChangeSupport propSupport, BrowserCallback callback) {
        super(VisibilityMode.FORCED_HIDDEN, VisibilityMode.FORCED_HIDDEN);
        this.propSupport = propSupport;
        this.callback = callback;
        setUpdateTitle(false);
        setMinimumSize(new Dimension(10, 10));
    }

    @Override
    public void onEnableBackButton(boolean enabled) {
        super.onEnableBackButton(enabled);
        this.backEnabled = enabled;
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_BACKWARD, !enabled, enabled);
    }

    @Override
    public void onEnableForwardButton(boolean enabled) {
        super.onEnableForwardButton(enabled);
        this.forwardEnabled = enabled;
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_FORWARD, !enabled, enabled);
    }

    @Override
    public void onLoadingEnded() {
        super.onLoadingEnded();
//            doc = getDocument();
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_STATUS_MESSAGE, false, true);
        callback.fireBrowserEvent( WebBrowserEvent.WBE_LOADING_ENDED, null );
    }

    @Override
    public void onLoadingStarted() {
        super.onLoadingStarted();
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_STATUS_MESSAGE, false, true);
        callback.fireBrowserEvent( WebBrowserEvent.WBE_LOADING_STARTED, null );
    }

    @Override
    public boolean onLoadingStarting(String url) {
        if( callback.fireBrowserEvent( WebBrowserEvent.WBE_LOADING_STARTING, url ) )
            return true;
        return false;
    }

    @Override
    public void onSetStatus(String text) {
        super.onSetStatus(text);
        this.statusText = text;
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_STATUS_MESSAGE, false, true);
    }

    @Override
    public void onSetTitle(String title) {
        super.onSetTitle(title);
        this.title = title;
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_TITLE, false, true);
    }

    @Override
    public void onSetUrlbarText(String url) {
        super.onSetUrlbarText(url);
        this.url = url;
        propSupport.firePropertyChange(HtmlBrowser.Impl.PROP_URL, false, true);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @Override
    public void onDispatchEvent(AWTEvent e) {
        super.onDispatchEvent(e);
        if( e instanceof MozillaKeyEvent ) {
            MozillaKeyEvent mke = (MozillaKeyEvent) e;
            callback.fireBrowserEvent( WebBrowserEvent.WBE_KEY_EVENT, e, mke.getSourceNode() );
        } else if( e instanceof MozillaMouseEvent ) {
            MozillaMouseEvent mme = (MozillaMouseEvent) e;
            callback.fireBrowserEvent( WebBrowserEvent.WBE_KEY_EVENT, e, mme.getSourceNode() );
        }
    }

    public void dispose() {
        //TODO implement
    }

    private void updateCopyPaste() {
        MozillaExecutor.mozSyncExec(new Runnable() {

            public void run() {
                nsIWebBrowser brow = getChromeAdapter().getWebBrowser();
                nsISelection sel = brow.getContentDOMWindow().getSelection();
                //addWebBrowserListener(MyMozillaPanel.this, nsISelectionListener.NS_ISELECTIONLISTENER_IID);
                if( sel.getRangeCount() > 0 ) {
                    nsIDOMRange range = sel.getRangeAt(0);
                    Node n = NodeFactory.getNodeInstance(range.cloneContents());
                    System.out.println("selection: " + n.getTextContent());
                }
            }
        });
    }

    private void showSource() {
        Document source = getDocument();
        try {
            DOMUtils.writeDOMToStream(source, System.out, "UTF-8");
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    private URL getUrlFromNode(Node n) {
        if( null == n ) {
            return null;
        }
        NamedNodeMap attrs = n.getAttributes();
        if( null == attrs ) {
            return null;
        }
        Node href = attrs.getNamedItem("href");
        if( null == href ) {
            return null;
        }
        String urlText = href.getTextContent();
        String baseUrl = n.getBaseURI();
        if( baseUrl.indexOf("#") > 0 ) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("#"));
        }
        if( urlText.startsWith("#") ) {
            //todo filter out existing anchor (if any)
            urlText = baseUrl + urlText;
        }
        try {
            return new URL(urlText);
        } catch( MalformedURLException e ) {
            try {
                return new URL(baseUrl + "/" + urlText);
            } catch( MalformedURLException e2 ) {
            }
            return null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        try {
            if( null != doc ) {
                loadHTML(doc.getTextContent());
                doc = null;
            } else if( null != url ) {
                this.load(url);
            }
        } catch( MozillaRuntimeException e ) {
            Logger.getLogger(BrowserFactory.class.getName()).log(Level.FINE, null, e);
        }
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isForwardEnabled() {
        return forwardEnabled;
    }

    public boolean isBackwardEnabled() {
        return backEnabled;
    }

    public String getTitleText() {
        return title;
    }

    public void requestFocusInBrowser() {
        Runnable r = new Runnable() {
            public void run() {
                ChromeAdapter ca = getChromeAdapter();
                if( null == ca )
                    return;
                nsIWebBrowserFocus webBrowserFocus = qi(ca.getWebBrowser(), nsIWebBrowserFocus.class);
                webBrowserFocus.activate();
            }
        };
        mozAsyncExec(r);
    }
}
