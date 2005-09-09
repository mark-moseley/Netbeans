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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.turbo.TurboProvider;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.ErrorManager;

import java.io.*;
import java.util.*;

/**
 * Storage of file attributes with shortcut to retrieve all stored values.
 *
 * @author Maros Sandor
 */
class DiskMapTurboProvider implements TurboProvider {

    static final String ATTR_STATUS_MAP = "org.netbeans.modules.versioning.system.cvss.DiskMapTurboProvider.STATUS_MAP";  // NOI18N

    private File    cacheStore;
    private int     storeSerial;

    private int     cachedStoreSerial = -1;
    private Map     cachedValues;
    
    private static final int STATUS_VALUABLE = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_VERSIONED_UPTODATE;

    DiskMapTurboProvider() {
        initCacheStore();
    }

    synchronized Map getAllModifiedValues() {
        if (cachedStoreSerial != storeSerial || cachedValues == null) {
            cachedValues = new HashMap();
            File [] files = cacheStore.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    for (;;) {
                        int pathLen = dis.readInt();
                        dis.readInt();
                        String path = readChars(dis, pathLen);
                        Map value = readValue(dis, path);
                        for (Iterator j = value.keySet().iterator(); j.hasNext();) {
                            File f = (File) j.next();
                            FileInformation info = (FileInformation) value.get(f);
                            if ((info.getStatus() & STATUS_VALUABLE) != 0) {
                                cachedValues.put(f, info);
                            }
                        }
                    }
                } catch (EOFException e) {
                    // reached EOF, no entry for this key
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (dis != null) try { dis.close(); } catch (IOException e) {}
                }
            }
            cachedStoreSerial = storeSerial;
            cachedValues = Collections.unmodifiableMap(cachedValues);
        }
        return cachedValues;
    }

    public boolean recognizesAttribute(String name) {
        return ATTR_STATUS_MAP.equals(name);
    }

    public boolean recognizesEntity(Object key) {
        return key instanceof File;
    }

    public synchronized Object readEntry(Object key, String name, MemoryCache memoryCache) {
        assert key instanceof File;
        assert name != null;

        boolean readFailed = false;
        File dir = (File) key;
        File store = getStore(dir);
        if (!store.isFile()) {
            return null;
        }

        String dirPath = dir.getAbsolutePath();
        int dirPathLen = dirPath.length();
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(store)));
            for (;;) {
                int pathLen = dis.readInt();
                int mapLen = dis.readInt();
                if (pathLen != dirPathLen) {
                    skip(dis, pathLen * 2 + mapLen);
                } else {
                    String path = readChars(dis, pathLen);
                    if (dirPath.equals(path)) {
                        return readValue(dis, path);
                    } else {
                        skip(dis, mapLen);
                    }
                }
            }
        } catch (EOFException e) {
            // reached EOF, no entry for this key
        } catch (Exception e) {
            e.printStackTrace();
            readFailed = true;
        } finally {
            if (dis != null) try { dis.close(); } catch (IOException e) {}
        }
        if (readFailed) store.delete();
        return null;
    }

    public synchronized boolean writeEntry(Object key, String name, Object value) {
        assert key instanceof File;
        assert name != null;

        if (value != null) {
            if (!(value instanceof Map)) return false;
            if (!isValuable(value)) value = null;
        }

        File dir = (File) key;
        String dirPath = dir.getAbsolutePath();
        int dirPathLen = dirPath.length();
        File store = getStore(dir);

        if (value == null && !store.exists()) return true;

        File storeNew = new File(store.getParentFile(), store.getName() + ".new");

        DataOutputStream oos = null;
        DataInputStream dis = null;
        try {
            oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(storeNew)));
            if (value != null) {
                writeEntry(oos, dirPath, value);
            }
            if (store.exists()) {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(store)));
                for (;;) {
                    int pathLen;
                    try {
                        pathLen = dis.readInt();
                    } catch (EOFException e) {
                        break;
                    }
                    int mapLen = dis.readInt();
                    if (pathLen == dirPathLen) {
                        String path = readChars(dis, pathLen);
                        if (dirPath.equals(path)) {
                            skip(dis, mapLen);
                        } else {
                            oos.writeInt(pathLen);
                            oos.writeInt(mapLen);
                            oos.writeChars(path);
                            copyStreams(oos, dis, mapLen);
                        }
                    } else {
                        oos.writeInt(pathLen);
                        oos.writeInt(mapLen);
                        copyStreams(oos, dis, mapLen + pathLen * 2);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().annotate(e, "Copy: " + store.getAbsolutePath() + " to: " + storeNew.getAbsolutePath());
            ErrorManager.getDefault().notify(e);
            return true;
        } finally {
            if (oos != null) try { oos.close(); } catch (IOException e) {}
            if (dis != null) try { dis.close(); } catch (IOException e) {}
        }
        storeSerial++;
        store.delete();
        storeNew.renameTo(store);
        return true;
    }

    private void skip(InputStream is, long len) throws IOException {
        while (len > 0) {
            long n = is.skip(len);
            if (n < 0) throw new EOFException("Missing " + len + " bytes.");  // NOI18N
            len -= n;
        }
    }

    private String readChars(DataInputStream dis, int len) throws IOException {
        StringBuffer sb = new StringBuffer(len);
        while (len-- > 0) {
            sb.append(dis.readChar());
        }
        return sb.toString();
    }

    private Map readValue(DataInputStream dis, String dirPath) throws IOException {
        Map map = new HashMap();
        int len = dis.readInt();
        while (len-- > 0) {
            int nameLen = dis.readInt();
            String name = readChars(dis, nameLen);
            File file = new File(dirPath, name);
            int status = dis.readInt();
            FileInformation info = new FileInformation(status & 65535, status > 65535);
            map.put(file, info);
        }
        return map;
    }

    private void writeEntry(DataOutputStream dos, String dirPath, Object value) throws IOException {

        Map map = (Map) value;
        Set set = map.keySet();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(set.size() * 50);
        DataOutputStream temp = new DataOutputStream(baos);

        temp.writeInt(set.size());
        for (Iterator i = set.iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) map.get(file);
            temp.writeInt(file.getName().length());
            temp.writeChars(file.getName());
            temp.writeInt(info.getStatus() + (info.isDirectory() ? 65536 : 0));
        }
        temp.close();
        byte [] valueBytes = baos.toByteArray();

        dos.writeInt(dirPath.length());
        dos.writeInt(valueBytes.length);
        dos.writeChars(dirPath);
        dos.write(valueBytes);
    }

    private boolean isValuable(Object value) {
        Map map = (Map) value;
        for (Iterator i = map.values().iterator(); i.hasNext();) {
            FileInformation info = (FileInformation) i.next();
            if ((info.getStatus() & STATUS_VALUABLE) != 0) return true;
        }
        return false;
    }

    private File getStore(File dir) {
        String dirPath = dir.getAbsolutePath();
        int dirHash = dirPath.hashCode();
        return new File(cacheStore, Integer.toString(dirHash % 173 + 172) + ".bin");
    }

    private void initCacheStore() {
        String userDir = System.getProperty("netbeans.user");
        if (userDir != null) {
            cacheStore = new File(new File(new File (userDir, "var"), "cache"), "cvscache");
        } else {
            File cachedir = FileUtil.toFile(Repository.getDefault().getDefaultFileSystem().getRoot());
            cacheStore = new File(cachedir, "cvscache");
        }
        cacheStore.mkdirs();
    }

    private static void copyStreams(OutputStream out, InputStream in, int len) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = (len <= 4096) ? len : 4096;
            n = in.read(buffer, 0, n);
            if (n < 0) throw new EOFException("Missing " + len + " bytes.");  // NOI18N
            out.write(buffer, 0, n);
            if ((len -= n) == 0) break;
        }
        out.flush();
    }
}
