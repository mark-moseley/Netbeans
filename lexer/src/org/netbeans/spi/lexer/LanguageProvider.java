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

package org.netbeans.spi.lexer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;

/**
 * The <code>LanguageDescription</code> provider. This class is a hook into the
 * lexer framework allowing modules to provide a language resolution service.
 * Whenever a <code>LanguageDescription</code> is not explicitly known the
 * framework tries to determine it by asking <code>LanguageProvider</code>s registered
 * in the <code>Lookup.getDefault()</code>.
 * 
 * <code>LanguageDescription</code>s might be needed for a mime type or mime path
 * of a <code>Document</code> used as an input source or they might be needed for
 * some tokens that contain text in an another (embedded) language. In both cases
 * a <code>LanguageDescription</code> can either be explicitely provided by setting
 * the document's property or implementing the <code>LanguageHierarchy.embedded()</code>
 * method respectively or the framework will use <code>LanguageProvider</code>s to
 * create the appropriate <code>LanguageDescription</code>.
 * 
 * @author Vita Stejskal
 */
public abstract class LanguageProvider {
    
    /**
     * The name of the property, which should be fired when the mime paths to
     * <code>LanguageDescription</code> mapping changes.
     */
    public static final String PROP_LANGUAGE = "LanguageProvider.PROP_LANGUAGE"; //NOI18N

    /**
     * The name of the property, which should be fired when the embedded language to
     * <code>LanguageDescription</code> mapping changes.
     */
    public static final String PROP_EMBEDDED_LANGUAGE = "LanguageProvider.PROP_EMBEDDED_LANGUAGE"; //NOI18N
    
    /**
     * Finds <code>LanguageDescription</code> for a given mime path.
     * 
     * <p>The lexer framework uses this method to find a <code>LanguageDescription</code>
     * for <code>Document</code>s that are used as an input source. If the document
     * itself does not specify its <code>LanguageDescription</code> the framework
     * will consult registered <code>LanguageProvider</code>s to find out the
     * <code>LanguageDescription</code> appropriate for the document's mime type.
     * 
     * @param mimePath The mime path or mime type to get a <code>LanguageDescription</code>
     *   for. It is allowed to pass in an empty string or <code>null</code>.
     * 
     * @return The <code>LanguageDescription</code> registered for the given
     * mime path or <code>null</code> if no such <code>LanguageDescription</code> exists.
     */
    public abstract LanguageDescription findLanguage(String mimePath);
    
    /**
     * Finds <code>LanguageDescription</code> that will define language embedding
     * for a given token.
     * 
     * <p>If a <code>Token</code> contains text in a different language that could
     * further be used for lexing this <code>Token</code> the framework will try
     * to find out the <code>LanguageDescription</code> of that language by asking
     * the <code>Token</code>'s own <code>LanguageDescription</code> first and then
     * by consulting registered <code>LanguageProvider</code>s. The <code>LanguageProvider</code>s
     * are expected to return a <code>LanguageDescription</code> for tokens they
     * care about and <code>null</code> for the rest. The first non-null
     * <code>LanguageDescription</code> found will be used.
     * 
     * 
     * @param tokenLanguage The <code>LanguagePath</code> of the token, which
     *   embedded language should be returned.
     * @param token The <code>Token</code> to get the <code>LanguageDescription</code>
     *   for.
     * @param inputAttributes The attributes that could affect the creation of
     *   the embedded <code>LanguageDescription</code>. It may be <code>null</code>
     *   if there are no extra attributes.
     * 
     * @return The <code>LanguageDescription</code> for the given <code>Token</code>
     *   or <code>null</code> if there is no <code>LanguageDescription</code> available
     *   for this token or the token is unknown to this <code>LanguageProvider</code>.
     */
    public abstract LanguageDescription findEmbeddedLanguage(LanguagePath tokenLanguage, Token token, InputAttributes inputAttributes);
    
    /**
     * Add a listener for change notifications.
     * 
     * @param l The listener to add.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener.
     * 
     * @param l The listener to remove.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    protected final void firePropertyChange(String propertyName) {
        pcs.firePropertyChange(propertyName, null, null);
    }
    
    /**
     * The default constructor for subclasses.
     */
    protected LanguageProvider() {
        
    }
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
}
