/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.client;

import java.io.*;
import java.util.*;
import org.netbeans.modules.subversion.config.KVFile;

/**
 * Implements properties access that is not supported
 * by svnClientAdapter library. It access <tt>.svn</tt>
 * metadata directly:
 *
 * <pre>
 *    trunk/
 *        .svn/
 *            dir-props            (KV file format)
 *            dir-props-base       (KV file format)
 *            props/
 *               filename.svn-base         (KV file format)
 *               filename_newprop.svn-base (KV file format)
 *            props-base/
 *               filename.svn-base         (KV file format)
 *        filename
 *        filename_newprop
 * </pre>
 *
 * <b>The implemetation should be moved into svnClientAdpater
 * library!</b>
 *
 * @author Petr Kuzel
 */
public final class PropertiesClient {

    private final File file;

    /** Creates a new instance of PropertiesClient */
    public PropertiesClient(File file) {
        assert file != null;
        this.file = file;
    }

    /**
     * Loads BASE properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getBaseProperties() throws IOException {
        File store;
        if (file.isDirectory()) {
            store = new File(file, ".svn/dir-props-base");  // NOI18N
        } else {
            store = new File(file.getParentFile(), ".svn/prop-base/" + file.getName() + ".svn-base");  // NOI18N
        }
        if (store.isFile()) {
            KVFile kv = new KVFile(store);
            return normalize(kv.getMap());
        } else {
            return new HashMap<String, byte[]>();
        }
    }

    /**
     * Loads (locally modified) properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getProperties() throws IOException {
        File store;
        if (file.isDirectory()) {
            store = new File(file, ".svn/dir-props");  // NOI18N
        } else {
            store = new File(file.getParentFile(), ".svn/props/" + file.getName() + ".svn-work");  // NOI18N
        }
        if (store.isFile()) {
            KVFile kv = new KVFile(store);
            return normalize(kv.getMap());
        } else {
            return new HashMap<String, byte[]>();
        }
    }

    private Map<String, byte[]> normalize(Map map) {
        Map<String, byte[]> ret = new HashMap<String, byte[]>(map.size());
        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry next = it.next();
            // getKey().toString() == the normalization
            ret.put(next.getKey().toString(), (byte[]) next.getValue());
        }
        return ret;
    }

    /** Not implemented. */
    public Map getProperties(int revision) throws IOException {
        throw new UnsupportedOperationException();
    }
}
