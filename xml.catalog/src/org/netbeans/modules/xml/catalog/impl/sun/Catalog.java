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

package org.netbeans.modules.xml.catalog.impl.sun;

import java.awt.Image;
import java.io.*;
import java.beans.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;


import org.netbeans.modules.xml.catalog.spi.*;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.CatalogManager;

/**
 * SPI implementation that bridges to Sun's Resolvers 1.1.
 * <p>
 * It uses heavily lazy initialization to eliminate differences between an
 * instance constructed by the contructor <b>or</b> by deserialization process.
 * The approach also speeds up setup time.
 *
 * @author  Petr Kuzel
 */
public final class Catalog 
    implements org.netbeans.modules.xml.catalog.spi.CatalogReader, CatalogDescriptor, Serializable, EntityResolver{

    private static final long serialVersionUID = 123659121L;
        
    private transient PropertyChangeSupport pchs;

    private transient EntityResolver peer;

    private transient String desc;
    
    // a catalog source location
    private String location;
    
    // a public preference
    private boolean preference = true;
    
    private static final String PROP_LOCATION = "cat-loc";
    
    private static final String PROP_PREF_PUBLIC = "cat-pref";

    private static final String PROP_DESC = CatalogDescriptor.PROP_CATALOG_DESC;
    
    /** Creates a new instance of Catalog */
    public Catalog() {
    }

    /**
     * Deserialization 'constructor'.
     */
    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // lazy init transient fields, see getPCHS() and getPeer() methods
        setShortDescription(Util.THIS.getString("MSG_prepared", location));
    }
    
    /**
     * Set Catalog source (a URL).
     */
    public synchronized void setLocation(String location) {
        String old = this.location;
        this.location = location;
        peer = null;  // lazy init       
        getPCHS().firePropertyChange(PROP_LOCATION, old, location);        
        updateDisplayName();
    }

    /**
     * Access the location value.
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Set public resolving preference.
     */
    public void setPreferPublic(boolean val) {
        boolean old = preference;
        this.preference = val;
        getPCHS().firePropertyChange(PROP_LOCATION, old, val);
    }

    /**
     * Access the public ID preference flag.
     */
    public boolean isPreferPublic() {
        return preference;
    }
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }
    
    /** Registers new listener.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPCHS().addPropertyChangeListener(l);
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        String src = location;
        if (src == null || "".equals(src.trim())) {
            return Util.THIS.getString("PROP_missing_location");
        } else {        
            return Util.THIS.getString("TITLE_catalog", location);
        }
    }

    public String getName() {
        return getClass() + location + preference;
    }
    
    /**
     * Notify listeners that display name have changed.
     */
    public void updateDisplayName() {
        String name = getDisplayName();
        getPCHS().firePropertyChange(CatalogDescriptor.PROP_CATALOG_NAME, null, name);
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public Image getIcon(int type) {
        return null;
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator getPublicIDs() {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.CatalogResolver) {
            org.apache.xml.resolver.Catalog cat = ((org.apache.xml.resolver.tools.CatalogResolver) p).getCatalog();
            return cat.getPublicIDs();
        }
        return null;
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return desc;
    }
    
    public void setShortDescription(String desc) {
        String old = this.desc;
        this.desc = desc;
        getPCHS().firePropertyChange(PROP_DESC, old, desc);
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.CatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.CatalogResolver) p).getCatalog().resolveSystem(publicId);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    public synchronized void refresh() {
        peer = createPeer(location, preference);
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
        throw new UnsupportedOperationException();
    }
    
    /** Unregister the listener.  */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPCHS().removePropertyChangeListener(l);
    }

    /**
     * Delegate entity resution process to peer if exists.
     */
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {        
        return getPeer().resolveEntity(publicID, systemID);
    }
    
/** We are a key and must retain equals immutability
    public boolean equals(Object obj) {
        if (obj instanceof Catalog) {
            Catalog cat = (Catalog) obj;
            if (this.location == null && cat.location != null) return false;
            if ((this.location != null && this.location.equals(cat.location)) == false) return false;
            return  (this.preference == cat.preference);
        }
        return false;
    }
    
    
    public int hashCode() {
        return (location != null ? location.hashCode() : 0) ^ (preference?13:7);
    }
*/    
    /**
     * Factory new peer and load data into it.
     * As a side effect set short description.
     * @return EntityResolver never <code>null</code>
     */
    private EntityResolver createPeer(String location, boolean pref) {
        try {
            CatalogManager manager = new CatalogManager(null);
            manager.setUseStaticCatalog(false);
            manager.setPreferPublic(pref);

            CatalogResolver catalogResolver = new CatalogResolver(manager);
            org.apache.xml.resolver.Catalog cat = catalogResolver.getCatalog();
            cat.parseCatalog(new URL(location));
            setShortDescription(Util.THIS.getString("DESC_loaded"));
            return catalogResolver;
        } catch (IOException ex) {
            setShortDescription(Util.THIS.getString("DESC_error_loading", ex.getLocalizedMessage()));
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("I/O error loading catalog " + location, ex);
        }
        
        // return dumb peer
        return new EntityResolver () {
            public InputSource resolveEntity(String p, String s) {
                return null;
            }
        };
    }
    
    /**
     * Lazy init PropertyChangeSupport and return it.
     */
    private synchronized PropertyChangeSupport getPCHS() {
        if (pchs == null) pchs = new PropertyChangeSupport(this);
        return pchs;
    }

    /**
     * Lazy init peer and return it.
     */
    private synchronized EntityResolver getPeer() {
        
        if (peer == null) peer = createPeer(location, preference);
        return peer;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.CatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.CatalogResolver) p).getCatalog().resolveURI(name);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    public String resolvePublic(String publicId) {
        Object p = getPeer();
        if (p instanceof org.apache.xml.resolver.tools.CatalogResolver)
            try {
                return ((org.apache.xml.resolver.tools.CatalogResolver) p).getCatalog().resolvePublic(publicId,null);
            } catch (java.net.MalformedURLException ex) {}
              catch (java.io.IOException ex) {}
        return null;
    }
    
}
