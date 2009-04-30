/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler.State;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.FileContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileUtil;

/**
 * Storage for files and states. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ 
class FileContainer extends ProjectComponent implements Persistent, SelfPersistent {
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    private final Object lock = new Object();
    private Map<CharSequence, MyFile> myFiles = new ConcurrentHashMap<CharSequence, MyFile>();
    private Map<CharSequence, Object/*CharSequence or CharSequence[]*/> canonicFiles = new ConcurrentHashMap<CharSequence, Object/*CharSequence or CharSequence[]*/>();

    // empty stub
    private static final FileContainer EMPTY = new FileContainer() {

        @Override
        public void put() {
            // do nothing
        }

        @Override
        public void putFile(File file, FileImpl impl, State state) {
            // do nothing
        }

        @Override
        public void putPreprocState(File file, State state) {
            // do nothing
        }
    };

    /** Creates a new instance of FileContainer */
    public FileContainer(ProjectBase project) {
	super(new FileContainerKey(project.getUniqueName().toString()), false);
	put();
    }
    
    public FileContainer (DataInput input) throws IOException {
	super(input);
        readStringToMyFileMap(input, myFiles);
        readStringToStringsArrMap(input, canonicFiles);
	//trace(canonicFiles, "Read in ctor:");
    }

    // only for creating EMPTY stub
    private FileContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null, false);
    }

    /*package*/ static FileContainer empty() {
        return EMPTY;
    }

    private void trace(Map<CharSequence, Object/*String or CharSequence[]*/> map, String title) {
	System.err.printf("%s\n", title);
	for( Map.Entry<CharSequence, Object> entry : map.entrySet() ) {
	    System.err.printf("%s ->\n%s\n\n", entry.getKey(), entry.getValue());
	}
    }
    
    public void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        CharSequence path = getFileKey(file, true);
        MyFile newEntry;
        CsmUID<CsmFile> uid = RepositoryUtils.<CsmFile>put(impl);
        newEntry = new MyFile(uid, state, path);
        MyFile old;

        old = myFiles.put(path, newEntry);
        addAlternativeFileKey(path, newEntry.canonical);

        if (old != null){
            System.err.println("Replace file info for "+ old.fileNew + " with " + impl);
        }
	put();
    }
    
    public void removeFile(CharSequence file) {
        CharSequence path = getFileKey(file, false);
        MyFile f;

        f = myFiles.remove(path);
        if (f != null) {
            removeAlternativeFileKey(f.canonical, path);
        }
        
        if (f != null) {
            if (f.fileNew != null){
                // clean repository
                if (false) { RepositoryUtils.remove(f.fileNew) ;}
            }
        }
	put();
    }
    
    public FileImpl getFile(File file, boolean treatSymlinkAsSeparateFile) {
        MyFile f = getMyFile(file, treatSymlinkAsSeparateFile, false);
        if (f == null) {
            return null;
        }
        CsmUID<CsmFile> fileUID = f.fileNew;
        FileImpl impl = (FileImpl) UIDCsmConverter.UIDtoFile(f.fileNew);
        if( impl == null ) {
            DiagnosticExceptoins.register(new IllegalStateException("no file for UID " + fileUID)); // NOI18N
        }
        return impl;
    }


    /** 
     * This should only be called if we are sure this is the only correct state:
     * e.g., when creating new file, when invalidating state of a *source* (not a header) file, etc
     */
    public void putPreprocState(File file, APTPreprocHandler.State state) {
        MyFile f = getMyFile(file, false, true);
        f.setState(state, null);
        put();
    }

    public void invalidatePreprocState(File file) {
        MyFile f = getMyFile(file, false, false);
        if (f == null){
            return;
        }
        synchronized (f) {
            f.invalidateStates();
        }
        if (TRACE_PP_STATE_OUT) {
            CharSequence path = getFileKey(file, false);
            System.err.println("\nInvalidated state for file" + path + "\n");
        }
    }
    
    //@Deprecated
    public APTPreprocHandler.State getPreprocState(File file) {
        MyFile f = getMyFile(file, false, false);
        if (f == null){
            return null;
        }
        return f.getState();
    }
    
    public Collection<APTPreprocHandler.State> getPreprocStates(File file) {
        MyFile f = getMyFile(file, false, false);
        if (f == null){
            return Collections.<APTPreprocHandler.State>emptyList();
        }
        return f.getPrerocStates();
    }

    public Collection<StatePair> getStatePairs(File file) {
        MyFile f = getMyFile(file, false, false);
        if (f == null) {
            return Collections.<StatePair>emptyList();
        }
        return f.getStatePairs();
    }

    public Entry getEntry(File file) {
        return getMyFile(file, false, false);
    }

    public Object getLock(File file) {
        MyFile f = getMyFile(file, false, false);
        return f == null ? lock : f.getLock();
    }
    
    public void debugClearState(){
        List<MyFile> files;
        files = new ArrayList<MyFile>(myFiles.values());
        for (MyFile file : files){
            synchronized (file.getLock()) {
                file.debugClearState();
            }
        }
	put();
    }
    
    public Collection<CsmFile> getFiles() {
	List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>(myFiles.values().size());
	getFiles2(uids);
	return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    public Collection<CsmUID<CsmFile>> getFilesUID() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>(myFiles.values().size());
        getFiles2(uids);
        return uids;
    }
    
    public Collection<FileImpl> getFileImpls() {
	List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>(myFiles.values().size());
	getFiles2(uids);
	return new LazyCsmCollection<CsmFile, FileImpl>(uids, TraceFlags.SAFE_UID_ACCESS);
    }
    
    private void getFiles2(List<CsmUID<CsmFile>> res) {
        List<MyFile> files;
        files = new ArrayList<MyFile>(myFiles.values());
        for(MyFile f : files){
            res.add(f.fileNew);
        }
    }
    
    public void clear(){
        myFiles.clear();
	put();
    }
    
    public int getSize(){
        return myFiles.size();
    }
    
    @Override
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	// maps are concurrent, so we don't need synchronization here
        writeStringToMyFileMap(aStream, myFiles);
        writeStringToStringsArrMap(aStream, canonicFiles);
	//trace(canonicFiles, "Wrote in write()");
    }

    private static boolean checked = false;
    public static CharSequence getFileKey(File file, boolean sharedText) {
        if (CndUtils.isDebugMode()) {
            String path = file.getAbsolutePath();
            CndUtils.assertTrueInConsole(APTIncludeUtils.normalize(path).equals(path), "File not normalized " + file); // NOI18N
        }
        String key = null;
        if (TraceFlags.USE_CANONICAL_PATH) {
            try {
                key = file.getCanonicalPath();
            } catch (IOException ex) {
                key = file.getAbsolutePath();
            }
        } else {
            key = file.getAbsolutePath();
        }
        return sharedText ? FilePathCache.getManager().getString(key) : DefaultCache.getManager().getString(key);
    }
    
    public static CharSequence getFileKey(CharSequence file, boolean sharedText) {
        return sharedText ? FilePathCache.getManager().getString(file) : DefaultCache.getManager().getString(file);
    }

    private CharSequence getAlternativeFileKey(CharSequence primaryKey) {
        Object out = canonicFiles.get(primaryKey);
        if (out instanceof CharSequence) {
            return (CharSequence)out;
        } else if (out != null) {
            assert ((CharSequence[])out).length >= 2;
            return ((CharSequence[])out)[0];
        }
        return null;
    }
    
    private MyFile getMyFile(File file, boolean treatSymlinkAsSeparateFile, boolean sharedText) {
        CharSequence path = getFileKey(file, sharedText);
        MyFile f = myFiles.get(path);
        if (f == null && (!treatSymlinkAsSeparateFile || !TraceFlags.SYMLINK_AS_OWN_FILE)) {
            // check alternative expecting that 'path' is canonical path
            CharSequence path2 = getAlternativeFileKey(path);
            f = (path2 == null) ? null : myFiles.get(path2);
            if (f != null) {
                if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                    System.err.println("alternative for " + path + " is " + path2);
                }
            }
        }
        return f;
    }
    
    private void addAlternativeFileKey(CharSequence primaryKey, CharSequence canonicKey) {
        Object out = canonicFiles.get(canonicKey);
        Object newVal;
        if (out == null) {
            newVal = primaryKey;
        } else {
            if (out instanceof CharSequence) {
                if (out.equals(primaryKey)) {
                    return;
                }
                newVal = new CharSequence[] {(CharSequence)out, primaryKey};
            } else {
                CharSequence[] oldAr = (CharSequence[])out;
                for(CharSequence what:oldAr){
                    if (what.equals(primaryKey)){
                        return;
                    }
                }
                CharSequence[] newAr = new CharSequence[oldAr.length + 1];
                System.arraycopy(oldAr, 0, newAr, 0, oldAr.length);
                newAr[oldAr.length] = primaryKey;
                newVal = newAr;
            }
        }
        canonicFiles.put(canonicKey, newVal);
        if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
            if (newVal instanceof CharSequence[]) {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + Arrays.asList((CharSequence[])newVal).toString());
            } else {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + newVal);
            }
        }                
    }
    
    private void removeAlternativeFileKey(CharSequence canonicKey, CharSequence primaryKey) {
        Object out = canonicFiles.get(canonicKey);
        assert out != null : "no entry for " + canonicKey + " of " + primaryKey;
        Object newVal;
        if (out instanceof CharSequence) {
            newVal = null;
        } else {
            CharSequence[] oldAr = (CharSequence[])out;
            assert oldAr.length >= 2;
            if (oldAr.length == 2) {
                newVal = oldAr[0].equals(primaryKey) ? oldAr[1] : oldAr[0];
            } else {
                CharSequence[] newAr = new CharSequence[oldAr.length - 1];
                int k = 0;
                for(CharSequence cur : oldAr){
                    if (!cur.equals(primaryKey)){
                        newAr[k++]=cur;
                    }
                }
                newVal = newAr;
            }
        }
        if (newVal == null) {
            canonicFiles.remove(primaryKey);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("removed entry for " + canonicKey + " while removing " + primaryKey);
            }
        } else {
            canonicFiles.put(canonicKey, newVal);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("change entry for " + canonicKey + " while removing " + primaryKey + " to " + newVal);
            }
        }
    }
    
    private static void writeStringToMyFileMap (
            final DataOutput output, Map<CharSequence, MyFile> aMap) throws IOException {
        assert output != null;
        assert aMap != null;
        int size = aMap.size();
        
        //write size
        output.writeInt(size);
        
        // write the map
        final Set<Map.Entry<CharSequence, MyFile>> entrySet = aMap.entrySet();
        final Iterator <Map.Entry<CharSequence, MyFile>> setIterator = entrySet.iterator();
        while (setIterator.hasNext()) {
            final Map.Entry<CharSequence, MyFile> anEntry = setIterator.next();

            PersistentUtils.writeUTF(anEntry.getKey(), output);
            assert anEntry.getValue() != null;
            anEntry.getValue().write(output);
        }
    }
    
    private static void  readStringToMyFileMap(
            final DataInput input, Map<CharSequence, MyFile> aMap) throws IOException {
        
        assert input != null; 
        assert aMap != null;
        
        final APTStringManager pathManager = FilePathCache.getManager();
        
        aMap.clear();
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            CharSequence key = PersistentUtils.readUTF(input, pathManager);
            MyFile value = new MyFile(input);
            
            assert key != null;
            assert value != null;
            
            aMap.put(key, value);
        }
    }
    
    private static void writeStringToStringsArrMap (
            final DataOutput output, final Map<CharSequence, Object/*CharSequence or CharSequence[]*/> aMap) throws IOException {
        
        assert output != null;
        assert aMap != null;
        
        final int size = aMap.size();
        output.writeInt(size);
        
        final Set<Map.Entry<CharSequence, Object>> entrySet = aMap.entrySet();
        final Iterator<Map.Entry<CharSequence, Object>> setIterator = entrySet.iterator();
        
        while (setIterator.hasNext()) {
            final Map.Entry<CharSequence, Object> anEntry = setIterator.next();
            assert anEntry != null;
            
            final CharSequence key = anEntry.getKey();
            final Object value = anEntry.getValue();
            assert key != null;
            assert value != null;
            assert ((value instanceof CharSequence) || (value instanceof CharSequence[]));
            
            PersistentUtils.writeUTF(key, output);
            
            if (value instanceof CharSequence ) {
                output.writeInt(1);
                PersistentUtils.writeUTF((CharSequence)value, output);
            } else if (value instanceof CharSequence[]) {
                
                final CharSequence[] array = (CharSequence[]) value;
                
                output.writeInt(array.length);
                for (int j = 0; j < array.length; j++) {
                    PersistentUtils.writeUTF(array[j], output);
                }
            }
        }
    }
    
    private static void readStringToStringsArrMap(
            final DataInput input, Map<CharSequence, Object/*CharSequence or CharSequence[]*/> aMap) throws IOException {
        assert input != null;
        assert aMap != null;

        final APTStringManager pathManager = FilePathCache.getManager();

        aMap.clear();

        final int size = input.readInt();

        for (int i = 0; i < size; i++) {
            CharSequence key = PersistentUtils.readUTF(input, pathManager);
            assert key != null;

            final int arraySize = input.readInt();
            assert arraySize != 0;

            if (arraySize == 1) {
                aMap.put(key, PersistentUtils.readUTF(input, pathManager));
            } else {
                final CharSequence[] value = new CharSequence[arraySize];
                for (int j = 0; j < arraySize; j++) {
                    CharSequence path = PersistentUtils.readUTF(input, pathManager);
                    assert path != null;

                    value[j] = path;
                }
                aMap.put(key, value);
            }
        }
    }

    //for unit test only
    Map<CharSequence, MyFile> getFileStorage() {
        return new TreeMap<CharSequence, MyFile>(myFiles);
    }

    //for unit test only
    Map<CharSequence, Object/*CharSequence or CharSequence[]*/> getCanonicalNames(){
        return new TreeMap<CharSequence, Object>(canonicFiles);
    }

    public static class StatePair {
        
        public final APTPreprocHandler.State state;
        public final FilePreprocessorConditionState pcState;
        
        public StatePair(State ppState, FilePreprocessorConditionState pcState) {
            this.state = ppState;
            this.pcState = pcState;
        }

        @Override
        public String toString() {
            return "(" + pcState + "\n" + state + ')'; //NOI18N
        }
    }
            
    public static interface Entry {

        /** Gets the states collection */
        Collection<StatePair> getStatePairs();
        
        void setState(APTPreprocHandler.State ppState, FilePreprocessorConditionState pcState);
        //void setStates(Collection<StatePair> pairs);
        void setStates(Collection<StatePair> pairs, StatePair yetOneMore);

        public void invalidateStates();

        /**
         * Sets (replaces) new conditions state for the existent pair
         * @return true in the case of success, otherwise (if no ppState found) false
         */
        //boolean setPCState(APTPreprocHandler.State ppState, FilePreprocessorConditionState pcState);
        
//        int size();

        /**
         * Gets mod count; mod count allows to understand whether the entry was changed:
         * each modification changes mod count
         */
        int getModCount();
        
        public Object getLock();

        /**
         * Used to sync between ProjectBase.onFileIncluded concerning the same file 
         * that run simultaneously on different threads.
         * Set to true in ProjectBase.onFileIncluded in the case it removes some of 
         * the previous state => decides that the file should be REparsed
         * Should be used ONLY WHEN LOCKED (see getLock() method) !!!
         */
        public boolean isPendingReparse();

        /** See comments to isPendingReparse */
        public void setPendingReparse(boolean pendingReparse);
    }

    // package access for unit tests only
    static final class MyFile implements Persistent, SelfPersistent, Entry {

        private final CsmUID<CsmFile> fileNew;
        private final CharSequence canonical;
        private volatile Object data; // either StatePair or List<StatePair>
        private volatile int modCount;
        private volatile boolean pendingReparse = false; // "transient"

        @SuppressWarnings("unchecked")
        private MyFile (final DataInput input) throws IOException {
            fileNew = UIDObjectFactory.getDefaultFactory().readUID(input);
            canonical = PersistentUtils.readUTF(input, FilePathCache.getManager());
            modCount = input.readInt();
            if (input.readBoolean()) {
                int cnt = input.readInt();
                if (cnt == 1) {
                    data = readStatePair(input);
                } else {
                    data = new ArrayList<StatePair>(cnt);
                    for (int i = 0; i < cnt; i++) {
                        ((List<StatePair>) data).add(readStatePair(input));
                    }
                }
            }
        }

        public boolean isPendingReparse() {
            return pendingReparse;
        }

        public void setPendingReparse(boolean pendingReparse) {
            this.pendingReparse = pendingReparse;
        }
        
        private MyFile(CsmUID<CsmFile> fileNew, APTPreprocHandler.State state, CharSequence fileKey) {
            this.fileNew = fileNew;
            this.data = new StatePair(state, null);
            this.canonical = getCanonicalKey(fileKey);
            this.modCount = 0;
        }
        
        public void write(final DataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(fileNew, output);
            PersistentUtils.writeUTF(canonical, output);
            output.writeInt(modCount);
            Object aData = data;
            output.writeBoolean(aData != null);
            if (aData != null) {
                if(aData instanceof StatePair) {
                    output.writeInt(1);
                    writeStatePair(output, (StatePair) aData);
                } else {
                    @SuppressWarnings("unchecked")
                    Collection<StatePair> pairs = (Collection<StatePair>)aData;
                    output.writeInt(pairs.size());
                    for (StatePair pair : pairs) {
                        writeStatePair(output, pair);
                    }
                }
            }
        }
        
        private static StatePair readStatePair(DataInput input) throws IOException {
            if (input.readBoolean()) {
                APTPreprocHandler.State state = null;
                if (input.readBoolean()){
                    state = PersistentUtils.readPreprocState(input);
                }
                FilePreprocessorConditionState pcState = null;
                if (input.readBoolean()){
                    pcState = new FilePreprocessorConditionState(input);
                }
                return new StatePair(state, pcState);
            }
            return null;
            
            
        }

        private static void writeStatePair(DataOutput output, StatePair pair) throws IOException {
            output.writeBoolean(pair != null);
            if (pair != null) {
                output.writeBoolean(pair.state != null);
                if (pair.state != null) {
                    PersistentUtils.writePreprocState(pair.state, output);
                }
                output.writeBoolean(pair.pcState != null);
                if (pair.pcState != null) {
                    pair.pcState.write(output);
                }
            }
        }

        public final synchronized int getModCount() {
            return modCount;
        }

        /**
         * @return lock under which all sequances read-decide-modify should be done
         * get* and replace* methods are synchronize on the same lock 
         */
        public Object getLock() {
            return this;
        }
        
//        public synchronized int size() {
//            return (data instanceof Collection) ? ((Collection) data).size() : 1;
//        }

        //@Deprecated
        private final synchronized APTPreprocHandler.State getState() {
            return getStatePairs().iterator().next().state;
        }

        private synchronized void debugClearState() {
            data = null;
        }

        /** 
         * This should only be called if we are sure this is THE ONLY correct state:
         * e.g., when creating new file, when invalidating state of a *source* (not a header) file, etc
         */
        public final synchronized void setState(APTPreprocHandler.State state, FilePreprocessorConditionState pcState) {
            State oldState = null;
            if( state != null && ! state.isCleaned() ) {
                state = APTHandlersSupport.createCleanPreprocState(state);
            }
            if ((data instanceof Collection<?>)) {
                @SuppressWarnings("unchecked")
                Collection<StatePair> states = (Collection<StatePair>) data;
                // check how many good old states are there
                // and pick a valid one to check that we aren't replacing better state by worse one 
                int oldGoodStatesCount = 0;
                for (StatePair pair : states) {
                    if (pair.state != null && pair.state.isValid()) {
                        oldGoodStatesCount++;
                        if (oldState == null || state.isCompileContext()) {
                            oldState = state;
                        }
                    }
                }
                if (oldGoodStatesCount > 1) {
                    if (CndUtils.isDebugMode()) {
                        StringBuilder sb = new StringBuilder("Attempt to set state while there are multiple states: " + canonical); // NOI18N
                        for (StatePair pair : states) {
                            sb.append(String.format("\nvalid: %b context: %b %s", pair.state.isValid(), pair.state.isCompileContext(), pair.pcState)); //NOI18N
                        }
                        Utils.LOG.log(Level.SEVERE, sb.toString(), new Exception(sb.toString()));
                    }
                    //return;
                }
            } else if(data instanceof StatePair) {
                oldState = ((StatePair) data).state;
            }
            
            incrementModCount();
            
            if (oldState == null || !oldState.isValid()) {
                data = state;
            } else {
                if (oldState.isCompileContext()) {
                    if (state.isCompileContext()) {
                        data = state;
                    } else {
                        if (CndUtils.isDebugMode()) {
                            String message = "Replacing correct state to incorrect " + canonical; // NOI18N
                            Utils.LOG.log(Level.SEVERE, message, new Exception());
                        }
                        return;
                    }
                } else {
                    data = state;
                }
            }
            if (TRACE_PP_STATE_OUT) {
                System.err.println("\nPut state for file" + canonical + "\n");
                System.err.println(state);
            }
            
            data = new StatePair(state, pcState);
        }
        
//        private static int countValidStates(Collection<StatePair> states) {
//            int cnt = 0;
//            for (StatePair pair : states) {
//                if (pair.state != null && pair.state.isValid()) {
//                    cnt++;
//                }
//            }
//            return cnt;
//        }
        
        /**
         * Sets (replaces) new conditions state for the existent pair
         * @return true in the case of success, otherwise (if no ppState found) false
         */
//        public synchronized boolean setPCState(APTPreprocHandler.State state, FilePreprocessorConditionState pcState) {
//            assert state != null : "state should not be null"; //NOI18N
//            if (state == null) {
//                return false;
//            }
//            if( state != null && ! state.isCleaned() ) {
//                state = APTHandlersSupport.createCleanPreprocState(state);
//            }
//            if (data instanceof StatePair) {
//                StatePair pair = (StatePair) data;
//                if (state.equals(pair.state)) {
//                    data = new StatePair(state, new FilePreprocessorConditionState(pcState));
//                    return true;
//                } else {
//                    return false;
//                }
//
//            } else {
//                List<StatePair> list = (List<StatePair>) data;
//                for (int i = 0; i < list.size(); i++) {
//                    StatePair pair = list.get(i);
//                    if (state.equals(pair.state)) {
//                        list.set(i, new StatePair(state, new FilePreprocessorConditionState(pcState)));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        }
        
//        public synchronized void setStates(Collection<StatePair> pairs) {
//            incrementModCount();
//            if (pairs.size() == 1) {
//                data = pairs.iterator().next();
//            } else {
//                data = new ArrayList<StatePair>(pairs);
//            }
//            if (CndUtils.isDebugMode()) {
//                checkConsistency();
//            }
//        }

        public synchronized void setStates(Collection<StatePair> pairs, StatePair yetOneMore) {
            incrementModCount();
            if (yetOneMore != null && yetOneMore.state != null && !yetOneMore.state.isCleaned()) {
                yetOneMore = new StatePair(APTHandlersSupport.createCleanPreprocState(yetOneMore.state), yetOneMore.pcState);
            }
            if (pairs.size() == 0) {
                data = yetOneMore;
            } else {
                ArrayList<StatePair> newData = new ArrayList<StatePair>(pairs.size()+1);
                newData.addAll(pairs);
                newData.add(yetOneMore);
                data = newData;
            }
            if (CndUtils.isDebugMode()) {
                checkConsistency();
            }
        }

        private void checkConsistency() {
            Collection<StatePair> pairs = getStatePairs();
            if (!pairs.isEmpty()) {
                boolean alarm = false;

                State firstState = null;
                boolean first = true;
                for (StatePair pair : getStatePairs()) {
                    if (first) {
                        first = false;
                        firstState = pair.state;
                    } else {
                        if ((firstState == null) != (pair.state == null)) {
                            alarm = true;
                            break;
                        }
                        if (firstState != null) {
                            if ((firstState.isValid() != pair.state.isValid()) ||
                                (firstState.isCompileContext() != pair.state.isCompileContext())) {
                                alarm = true;
                                break;
                            }
                        }
                    }
                }
                if (alarm) {
                    StringBuilder sb = new StringBuilder("Mixed preprocessor states: " + canonical); // NOI18N
                    for (StatePair pair : getStatePairs()) {
                        if (pair.state == null) {
                            sb.append(String.format(" (null, %s)", pair.pcState)); //NOI18N
                        } else {
                            sb.append(String.format(" (valid: %b, context: %b, %s) ", //NOI18N
                                    pair.state.isValid(), pair.state.isCompileContext(), pair.pcState));
                        }
                    }
                    Utils.LOG.log(Level.SEVERE, sb.toString(), new Exception());                
                }
            }
        }
        
        private synchronized void incrementModCount() {
            modCount = (modCount == Integer.MAX_VALUE) ? 0 : modCount+1;
        }

        @SuppressWarnings("unchecked")
        public synchronized void invalidateStates() {
            incrementModCount();
            if (data != null) {
                if (data instanceof StatePair) {
                    data = createInvalidState((StatePair) data);
                } else {
                    Collection<StatePair> newData = new ArrayList<StatePair>();
                    for (StatePair pair : (Collection<StatePair>) data) {
                        newData.add(createInvalidState(pair));
                    }
                    data = newData;
                }
            }
        }
        
        private static StatePair createInvalidState(StatePair pair) {
            if (pair == null) {
                return pair;
            } else {
                if (pair.state == null) {
                    return pair;
                } else {
                    return new StatePair(APTHandlersSupport.createInvalidPreprocState(pair.state), pair.pcState);
                }
            }
        }
            
        public synchronized Collection<StatePair> getStatePairs() {
            if (data == null) {
                return Collections.singleton(new StatePair(null, null));
            } else if(data instanceof StatePair) {
                return Collections.singleton((StatePair) data);
            } else {
                @SuppressWarnings("unchecked")
                Collection<StatePair> array = (Collection<StatePair>) data;
                return new ArrayList<StatePair>(array);
            }
        }
        
        public synchronized Collection<APTPreprocHandler.State> getPrerocStates() {
            if (data == null) {
                return Collections.emptyList();
            } else if(data instanceof StatePair) {
                return Collections.singleton(((StatePair) data).state);
            } else {
                @SuppressWarnings("unchecked")
                Collection<StatePair> pairs = (Collection<StatePair>) data;
                Collection<APTPreprocHandler.State> result = new ArrayList<State>(pairs.size());
                for (StatePair pair : pairs) {
                    result.add(pair.state);
                }
                return result;
            }
        }

        //for unit test only
        CsmUID<CsmFile> getFileUID(){
            return fileNew;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(fileNew);
            sb.append("states:\n"); //NOI18N
            for (StatePair pair : getStatePairs()) {
                sb.append(pair);
                sb.append('\n'); //NOI18N
            }
            return sb.toString();
        }

    }
    
    private static final CharSequence getCanonicalKey(CharSequence fileKey) {
        try {
            CharSequence res = new File(fileKey.toString()).getCanonicalPath();
            res = FilePathCache.getManager().getString(res);
            if (fileKey.equals(res)) {
                return fileKey;
            }
            return res;
        } catch (IOException e) {
            // skip exception
            return fileKey;
        }
    }
}