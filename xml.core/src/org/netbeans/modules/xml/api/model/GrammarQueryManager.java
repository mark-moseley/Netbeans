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

package org.netbeans.modules.xml.api.model;

import java.beans.FeatureDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.enum.ArrayEnumeration;
import org.xml.sax.InputSource;

/**
 * GrammarQuery service provider definition. Manager methods
 * are interleated and client must invoke them in particular
 * sequence from single thread to get desired results:
 * <pre>
 *    enabledFor = manager.enabled(context);
 *    if (enabled != null) {
 *        grammar = manager.getGrammar(context);
 *        //... guard enableness and enjoy the grammar
 *    }
 * </pre>
 * 
 * @author  Petr Kuzel
 * @deprecated a draft
 */
public abstract class GrammarQueryManager {

    // default instance
    private static Reference instance;
    
    /**
     * Can this manager provide a grammar for given context?
     * @param ctx GrammarEnvironment describing grammar context.
     * @return <code>null</code> if a grammar cannot be provided for
     *         the context else return context items (subenum of
     *         <code>ctx.getDocumentChildren</code>) that defines
     *         grammar enableness context.
     */
    public abstract Enumeration enabled(GrammarEnvironment ctx);
    
    /**
     * Factory method providing a root grammar for given document.
     * @param ctx The same context that was passed to {@link #enabled}.
     * @return GrammarQuery being able to work in the context
     *         or <code>null</null> if {@link #enabled} returns
     *         for the same context false.
     */
    public abstract GrammarQuery getGrammar(GrammarEnvironment ctx);
    
    /**
     * @return detailed description.
     */
    public abstract FeatureDescriptor getDescriptor();
    
    /**
     * A factory method looking for subclasses registered in Lookup
     * under <code>Plugins/XML/GrammarQueryManagers</code>.
     * <p>
     * There are defined some ordering marks to which every registration
     * must express its position: 
     * <code>semantics-grammar-to-generic-grammar-separator</code>.
     * All generic grammars such as universal DTD and XML Schema grammar
     * must be behing this mark. Semantics grammars such as
     * XSLT only handling grammar must be placed before it.
     * <code>generic-grammar-to-universal-grammar-separator</code>
     * allows to distingwish between generic grammars and universal
     * ones (e.g. a grammar that scans well-formed document and
     * using heuritics methods it tries to guess actual grammar on fly).
     *
     * @return Best effort instance.
     */
    public static synchronized GrammarQueryManager getDefault() {
        Object cached = instance != null ? instance.get() : null;
        if (cached == null) {
            cached = new DefaultQueryManager();
            instance = new WeakReference(cached);
        }
        return (GrammarQueryManager) cached;        
    }

    /**
     * Delegating implementation.
     */
    private static class DefaultQueryManager extends GrammarQueryManager {

        private static final String FOLDER = "Plugins/XML/GrammarQueryManagers";// NOI18N
        
        private Lookup.Result registrations;
        
        private static ThreadLocal transaction = new ThreadLocal();
        
        public FeatureDescriptor getDescriptor() {
            FeatureDescriptor desc = new FeatureDescriptor();
            desc.setHidden(true);
            desc.setName(getClass().getName());
            return desc;            
        }
        
        public GrammarQuery getGrammar(GrammarEnvironment ctx) {
            try {
                GrammarQueryManager g = (GrammarQueryManager) transaction.get();
                if (g != null) {
                    GrammarQuery query = g.getGrammar(ctx);
                    if (query == null) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.log(err.WARNING, "Broken contract: " + g.getClass());
                    }
                    return query;
                } else {
                    ErrorManager err = ErrorManager.getDefault();
                    Exception ex = new IllegalStateException("Broken contract");
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter writer = new PrintWriter(stringWriter);
                    ex.printStackTrace(writer);
                    writer.flush();
                    err.log(err.WARNING, stringWriter.getBuffer().toString());
                    return null;
                }
            } finally {
                transaction.set(null);
            }
        }
        
        public Enumeration enabled(GrammarEnvironment ctx) {
            Iterator it = getRegistrations();
            transaction.set(null);
            ArrayList list = new ArrayList(5);
            Enumeration enum = ctx.getDocumentChildren();
            while (enum.hasMoreElements()) {
                list.add(enum.nextElement());
            }
            Object[] array = list.toArray();
            while (it.hasNext()) {
                GrammarQueryManager next = (GrammarQueryManager) it.next();
                GrammarEnvironment env = new GrammarEnvironment(
                    new ArrayEnumeration(array), 
                    ctx.getInputSource(),
                    ctx.getFileObject()
                );
                Enumeration en = next.enabled(env);
                if (en != null) {
                    transaction.set(next);
                    return en;
                }
            }
            return null;
        }
        
        private synchronized Iterator getRegistrations() {
            if (registrations != null) {
                return registrations.allInstances().iterator();
            }

            // try to initialize it
            
            try {
                FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                FileObject fo = fs.findResource(FOLDER);      
                DataObject df = DataObject.find(fo);
                if (df instanceof DataObject.Container) {
                    FolderLookup lookup =
                        new FolderLookup((DataObject.Container) df);
                    Lookup.Template template =
                        new Lookup.Template(GrammarQueryManager.class);
                    registrations = lookup.getLookup().lookup(template);
                    return registrations.allInstances().iterator();
                } else {
                    return new ArrayList(0).iterator();
                }
            } catch (DataObjectNotFoundException ex) {
                return new ArrayList(0).iterator();
            }
        }
    }
}
