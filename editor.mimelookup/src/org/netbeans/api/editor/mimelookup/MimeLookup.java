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

package org.netbeans.api.editor.mimelookup;

import org.netbeans.modules.editor.mimelookup.MimePathLookup;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

/**
 * Mime Lookup provides lookup mechanism for mime type specific objects.
 * It can be used for example for retrieving mime type specific
 * editor settings, actions, etc.
 *
 * <p>The static method {@link #getLookup(MimePath) getLookup(MimePath mimePath)}
 * can be used to obtain a mime type specific lookup implementation. This lookup
 * can then be searched for objects bound to the specific mime type using the
 * standard <code>Lookup</code> methods.
 *
 * <p>Please look at the description of the {@link MimePath} class to learn more
 * about embedded mime types and how they are represented by the mime path. By
 * using the <code>MimePath</code> the <code>MimeLookup</code>
 * class allows to have different <code>Lookup</code>s for a mime type
 * embedded in different other mime types. So, for example there can be different
 * editor settings for the 'text/x-java' mime type and for the 'text/x-java' mime
 * type embedded in the 'text/x-jsp' mime type.
 *
 * <p>The <code>Lookup</code> instance returned from the <code>getLookup</code>
 * method can be used in the same way as any other <code>Lookup</code>. It is
 * possible to look up class instances, listen on changes in the lookup restults,
 * etc. The following code snippet shows a typical usage of this class for getting
 * instances of the <code>YourSetting</code> class from the 'text/x-java' mime
 * type specific <code>Lookup</code>.
 *
 * <pre>
 *     Lookup lookup = MimeLookup.getLookup(MimePath.get("text/x-java"));
 *     Lookup.Result result = lookup.lookup(new Lookup.Template(YourSetting.class));
 *     Collection instances = result.allInstances();
 * </pre>
 *
 * <p><b>Lifecycle:</b> The lifecycle of <code>Lookup</code> instances created
 * by calling the <code>getLookup</code> or <code>getMimeLookup</code> methods is
 * tied to the lifecycle of the <code>MimePath</code> instances they were created
 * for and vice versa. Therefore it is enough to keep reference to either the
 * <code>MimePath</code> or the <code>Lookup</code> created for that mime path
 * or even to the <code>Lookup.Result</code> in order to preserve the other instances
 * in the memory. Clients are strongly encouraged to keep refernce to the
 * <code>Lookup</code>s they obtain from this class throughout the whole lifecycle
 * of their component, especially when they need to use that <code>Lookup</code>
 * several times.
 *
 * @author Miloslav Metelka, Martin Roskanin, Vita Stejskal
 * @see MimePath
 */
public final class MimeLookup extends Lookup {
    
    private MimePathLookup mimePathLookup;
    
    /**
     * Gets a <code>Lookup</code> implementation that exposes objects specific
     * for the given <code>MimePath</code>.
     *
     * @param mimePath The mime path to get the <code>Lookup</code> for.
     *
     * @return The <code>Lookup</code> containing instances for the <code>MimePath</code>
     * passed in as a parameter.
     */
    public static Lookup getLookup(MimePath mimePath) {
        if (mimePath == null) {
            throw new NullPointerException("The mimePath parameter must not be null."); //NOI18N
        }
        
        return mimePath.getLookup();
    }

    /**
     * Gets mime-type specific lookup.
     *
     * @param mimeType non-null mime-type string representation, e.g. "text/x-java"
     * @return non-null mime-type specific lookup
     * @deprecated Use {@link #getLookup(MimePath) getLookup(MimePath.get(mimeType))} instead.
     */
    public static MimeLookup getMimeLookup(String mimeType) {
        if (mimeType == null) {
            throw new NullPointerException("The mimeType parameter must not be null."); //NOI18N
        }
        
        return new MimeLookup(MimePath.get(mimeType).getLookup());
    }

    /**
     * Creates a new instance of MimeLookup.
     *
     * @param parent   parent of this MimeLookup. Can be null in case of constructing 
     *                 the root MimeLookup
     * @param mimeType non-null mime-type string representation, e.g. "text/x-java"
     */
    private MimeLookup(MimePathLookup lookup) {
        this.mimePathLookup = lookup;
    }
    
    /** 
     * Gets mime-type specific child (embeded) lookup. Child mime-type content can be embeded into parent
     * mime-type content in various embeded languages. In this case mime-type lookup child is 
     * specified as subelement of parent lookup i.e.: MimeLookup("text/x-jsp") can have
     * a child MimeLookup("text/x-java") in a case of a jsp scriplet.
     *
     * @param mimeType non-null mime-type string representation
     *
     * @return non-null mime-type specific child (embeded) lookup
     * @deprecated Use {@link #getLookup(MimePath)} with the <code>MimePath</code>
     * of the embedded mime type instead.
     */
    public MimeLookup childLookup(String mimeType) {
        if (mimeType == null) {
            throw new NullPointerException("The mimeType parameter must not be null."); //NOI18N
        }
        
        MimePath mimePath = MimePath.get(mimePathLookup.getMimePath(), mimeType);
        return new MimeLookup(mimePath.getLookup());
    }

    /**
     * Looks up an object in this mime path lookup.
     *
     * @param clazz The class of the object to search for.
     *
     * @return An object implementing the given class or <code>null</code> if no such
     *         implementation is found.
     * @deprecated Use {@link #getLookup(MimePath)} and the methods provided by
     * the ordinary <code>Lookup</code> instance returned.
     */
    public Object lookup(Class clazz){
        return mimePathLookup.lookup(clazz);
    }

    /**
     * Looks up objects in this mime path lookup.
     *
     * @param template The template describing the objects to look for.
     *
     * @return The search result.
     * @deprecated Use {@link #getLookup(MimePath)} and the methods provided by
     * the ordinary <code>Lookup</code> instance returned.
     */
    public Result lookup(Template template) {
        return mimePathLookup.lookup(template);
    }

}
