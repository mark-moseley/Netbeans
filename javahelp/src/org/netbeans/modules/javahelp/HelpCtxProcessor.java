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

package org.netbeans.modules.javahelp;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xml.sax.*;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListener;

import org.netbeans.api.javahelp.Help;

/** XML processor for help context links.
 * The associated instance makes it suitable for
 * inclusion in a menu or toolbar.
 * @author Jesse Glick
 */
public final class HelpCtxProcessor implements XMLDataObject.Processor, InstanceCookie.Of {
    
    private static Help findHelp() {
        return (Help)Lookup.getDefault().lookup(Help.class);
    }
    
    /** the XML file being parsed
     */
    private XMLDataObject xml;
    
    /** the cached action
     */
    private Action p;
    
    /** Bind to an XML file.
     * @param xml the file to parse
     */
    public void attachTo(XMLDataObject xml) {
        this.xml = xml;
        Installer.err.log("processing help context ref: " + xml.getPrimaryFile());
    }
    
    /** Get the class produced.
     * @throws IOException doesn't
     * @throws ClassNotFoundException doesn't
     * @return the presenter class
     */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return ShortcutAction.class;
    }
    
    /** Get the name of the class produced.
     * @return the name of the presenter class
     */
    public String instanceName() {
        return "org.netbeans.modules.javahelp.HelpCtxProcessor$ShortcutAction"; // NOI18N
    }
    
    /** Test if this instance is of a suitable type.
     * @param type some superclass
     * @return true if it can be assigned to the desired superclass
     */
    public boolean instanceOf(Class type) {
        return type == Action.class;
    }
    
    /** Create the presenter.
     * @throws IOException doesn't
     * @throws ClassNotFoundException doesn't
     * @return the new presenter
     */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        if (p != null)
            return p;
        
        Installer.err.log("creating help context presenter from " + xml.getPrimaryFile());
        
        EntityResolver resolver = new EntityResolver() {
            public InputSource resolveEntity(String pubid, String sysid) {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        };
        
        HandlerBase handler = new HandlerBase() {
            public void startElement(String name, AttributeList amap) throws SAXException {
                if ("helpctx".equals(name)) { // NOI18N
                    String id = amap.getValue("id"); // NOI18N
                    String showmaster = amap.getValue("showmaster"); // NOI18N
                    if (id != null && !"".equals(id)) { // NOI18N
                        p = new ShortcutAction(xml, id, Boolean.valueOf(showmaster).booleanValue());
                    }
                }
            }
        };
        
        Parser parser = xml.createParser();
        parser.setEntityResolver(resolver);
        parser.setDocumentHandler(handler);
        
        try {
            parser.parse(new InputSource(xml.getPrimaryFile().getInputStream()));
        } catch (SAXException saxe) {
            IOException ioe = new IOException(saxe.toString());
            Installer.err.annotate(ioe, saxe);
            throw ioe;
        }
        
        if (p == null) {
            throw new IOException("No <helpctx> element in " + xml.getPrimaryFile()); // NOI18N
        }
        
        return p;
    }
    
    /** The presenter to be shown in a menu, e.g.
     */
    private static final class ShortcutAction extends AbstractAction implements HelpCtx.Provider, NodeListener, ChangeListener {
        
        /** associated XML file representing it
         */
        private final XMLDataObject obj;
        
        /** the cached help context
         */
        private String helpID;
        
        /** cached flag to show the master help set
         */
        private boolean showmaster;
        
        /** Create a new presenter.
         * @param obj XML file describing it
         */
        public ShortcutAction(XMLDataObject obj, String helpID, boolean showmaster) {
            this.obj = obj;
            this.helpID = helpID;
            this.showmaster = showmaster;
            Installer.err.log("new ShortcutAction: " + obj + " " + helpID + " showmaster=" + showmaster);
            updateText();
            updateIcon();
            updateEnabled();
            if (obj.isValid()) {
                Node n = obj.getNodeDelegate();
                n.addNodeListener(WeakListener.node(this, n));
            }
            Help h = findHelp();
            if (h != null) h.addChangeListener(WeakListener.change(this, h));
        }
        
        /** Show the help.
         * @param actionEvent ignored
         */
        public void actionPerformed(ActionEvent actionEvent) {
            Help h = findHelp();
            if (h != null) {
                Installer.err.log("ShortcutAction.actionPerformed: " + helpID + " showmaster=" + showmaster);
                h.showHelp(new HelpCtx(helpID), showmaster);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        
        /**
         * Help for the shortcut itself is generic.
         * @return a neutral help context - welcome page
         */
        public HelpCtx getHelpCtx() {
            // #23565:
            return new HelpCtx("ide.welcome"); // NOI18N
        }
        
        /** Help sets may have changed.
         * @param changeEvent ignore
         */
        public void stateChanged(ChangeEvent e) {
            updateEnabled();
        }
        
        /** Called when the node delegate changes somehow,
         * @param ev event indicating whether the change
         * was of display name, icon, or other
         */
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (!obj.isValid()) return;
            if (prop == null || prop.equals(Node.PROP_NAME) || prop.equals(Node.PROP_DISPLAY_NAME)) {
                updateText();
            }
            if (prop == null || prop.equals(Node.PROP_ICON)) {
                updateIcon();
            }
        }

        /** Update the text of the button according to node's
         * display name. Handle mnemonics sanely.
         */
        private void updateText() {
            String text;
            if (obj.isValid()) {
                text = obj.getNodeDelegate().getDisplayName();
            } else {
                // #16364
                text = "dead"; // NOI18N
            }
            putValue(Action.NAME, text);
        }

        /** Update the icon of the button according to the
         * node delegate.
         */
        private void updateIcon() {
            if (obj.isValid()) {
                putValue(Action.SMALL_ICON, new ImageIcon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            }
        }

        private void updateEnabled() {
            Help h = findHelp();
            Boolean valid = h == null ? Boolean.FALSE : h.isValidID(helpID, false);
            if (valid != null) {
                setEnabled(valid.booleanValue());
            }
            Installer.err.log("enabled: xml=" + obj.getPrimaryFile() + " id=" + helpID + " enabled=" + valid);
        }

        public void nodeDestroyed(NodeEvent ev) {
            setEnabled(false);
            updateText();
        }
        
        public void childrenAdded(NodeMemberEvent ev) {}
        public void childrenRemoved(NodeMemberEvent ev) {}
        public void childrenReordered(NodeReorderEvent ev) {}
        
    }
    
}
