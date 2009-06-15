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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.repository.GraphContainerKey;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Storage for include graph.
 * @author Alexander Simon
 */
public class GraphContainer extends ProjectComponent implements Persistent, SelfPersistent {
    
    // empty stub
    private static final GraphContainer EMPTY = new GraphContainer() {

        @Override
        public void put() {
        }

        @Override
        public void putFile(CsmFile master) {
        }
    };

    /** Creates a new instance of GraphContainer */
    public GraphContainer(ProjectBase project) {
        super(new GraphContainerKey(project.getUniqueName().toString()), false);
        put();
    }

    public GraphContainer(final DataInput input) throws IOException {
        super(input);
        assert input != null;
        readUIDToNodeLinkMap(input, graph);
    }

    // only for EMPTY static field
    private GraphContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null, true);
    }

    /*package*/ static GraphContainer empty() {
        return EMPTY;
    }
    
    /**
     * save file graph.
     * called after (re)parse.
     */
    public void putFile(CsmFile master){
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            graphLock.writeLock().lock();
            try {
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                } else {
                    node = new NodeLink();
                    graph.put(key,node);
                }
                for (CsmInclude include : master.getIncludes()){
                    CsmFile to = include.getIncludeFile();
                    if (to != null) {
                        CsmUID<CsmFile> out = UIDCsmConverter.fileToUID(to);
                        NodeLink pair = graph.get(out);
                        if (pair == null){
                            pair = new NodeLink();
                            graph.put(out,pair);
                        }
                        node.addOutLink(out);
                        pair.addInLink(key);
                    }
                }
            } finally {
                graphLock.writeLock().unlock();
            }
        }
        put();
    }
    
    /**
     * remove file graph.
     * called after remove, delelete.
     */
    public void removeFile(CsmFile master){
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            graphLock.writeLock().lock();
            try {
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> inLink = node.getInLinks();
                    for (CsmUID<CsmFile> in : inLink){
                        NodeLink pair = graph.get(in);
                        if (pair != null){
                            pair.removeOutLink(key);
                        }
                    }
                    inLink.clear();
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                    graph.remove(key);
                }
            } finally {
                graphLock.writeLock().unlock();
            }
        }
    	put();
    }
    
    /**
     * gets all direct or indirect included files into the  referenced file.
     */
    public Set<CsmFile> getIncludedFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(referencedFile);
        if (keyFrom != null) {
            graphLock.readLock().lock();
            try {
                getIncludedFiles(res, keyFrom);
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }

    public boolean isFileIncluded(CsmFile sourceFile, CsmFile headerFile) {
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(sourceFile);
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(headerFile);
        if (keyFrom != null && keyTo != null) {
            Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
            Map<Integer, GraphContainer> map = new HashMap<Integer, GraphContainer>();
            try {
                return isFileIncluded(map, res, keyFrom, keyTo);
            } finally {
                for(GraphContainer current : map.values()){
                    current.graphLock.readLock().unlock();
                }
            }
        }
        return false;
    }

    private boolean isFileIncluded(Map<Integer, GraphContainer> map, Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom, CsmUID<CsmFile> keyTo) {
        GraphContainer current = map.get(UIDUtilities.getProjectID(keyFrom));
        if (current == null) {
            CsmFile file = UIDCsmConverter.UIDtoFile(keyFrom);
            if (file == null) {
                return false;
            }
            current = ((ProjectBase)file.getProject()).getGraphStorage();
            if (current == null) {
                return false;
            }
            map.put(UIDUtilities.getProjectID(keyFrom), current);
            current.graphLock.readLock().lock();
        }
        NodeLink node = current.graph.get(keyFrom);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getOutLinks()){
                if (uid.equals(keyTo)) {
                    return true;
                }
                if (!res.contains(uid)){
                    res.add(uid);
                    if (isFileIncluded(map, res, uid, keyTo)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * gets all files that direct or indirect include the referenced file.
     */
    public Set<CsmFile> getParentFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(res, keyTo);
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }
    
    /**
     * gets all files that direct or indirect include the referenced file.
     * return files that not included into other files.
     * If set empty then return set with the referenced file.
     */
    public Set<CsmFile> getTopParentFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(res, keyTo);
                if (res.size()==0) {
                    res.add(keyTo);
                }
                List<CsmUID<CsmFile>> list = new ArrayList<CsmUID<CsmFile>>(res);
                res.clear();
                for(CsmUID<CsmFile> uid : list){
                    NodeLink link = graph.get(uid);
                    if (link != null && link.getInLinks().size()==0){
                        res.add(uid);
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }
    
    /**
     * gets all files that direct or indirect include referenced file.
     * If set empty then return set with the referenced file.
     */
    public Set<CsmFile> getCoherenceFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(res, keyTo);
                if (res.size()==0) {
                    res.add(keyTo);
                }
                for(CsmUID<CsmFile> uid : new ArrayList<CsmUID<CsmFile>>(res)){
                    getIncludedFiles(res, uid);
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }
    
    /**
     *  Returns set files that direct include referenced file.
     */
    public Set<CsmFile> getInLinks(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                NodeLink node = graph.get(keyTo);
                if (node != null) {
                    for(CsmUID<CsmFile> uid : node.getInLinks()){
                        if (!res.contains(uid)){
                            res.add(uid);
                        }
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }
    
    /**
     *  Returns set of direct included files in the referenced file.
     */
    public Set<CsmFile> getOutLinks(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                NodeLink node = graph.get(keyTo);
                if (node != null) {
                    for(CsmUID<CsmFile> uid : node.getOutLinks()){
                        if (!res.contains(uid)){
                            res.add(uid);
                        }
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return convertToFiles(res);
    }

    private Set<CsmFile> convertToFiles(Set<CsmUID<CsmFile>> res) {
        Set<CsmFile> res2= new HashSet<CsmFile>();
        for(CsmUID<CsmFile> uid : res) {
            CsmFile file = UIDCsmConverter.UIDtoFile(uid);
            if (file != null) {
                res2.add(file);
            }
        }
        return res2;
    }
    
    /*
     * method called in synchronized block
     */
    private void getIncludedFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom){
        NodeLink node = graph.get(keyFrom);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getOutLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getIncludedFiles(res, uid);
                }
            }
        }
    }
    
    /*
     * method called in synchronized block
     */
    private void getParentFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyTo){
        NodeLink node = graph.get(keyTo);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getInLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getParentFiles(res, uid);
                }
            }
        }
    }
    
    public void clear() {
        graphLock.writeLock().lock();
        try {
            graph.clear();
        } finally {
            graphLock.writeLock().unlock();
        }
        put();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        // need a write lock
        graphLock.writeLock().lock();
        try {
            writeUIDToNodeLinkMap(output, graph);
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    private static void writeUIDToNodeLinkMap (
            final DataOutput output, final Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {
        
        assert output != null;
        assert aMap != null;
        
        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;
        
        output.writeInt(aMap.size());
        
        final Set<Entry<CsmUID<CsmFile>,NodeLink>> entrySet = aMap.entrySet();
        final Iterator<Entry<CsmUID<CsmFile>,NodeLink>> setIterator = entrySet.iterator();
        
        while (setIterator.hasNext()) {
            final Entry<CsmUID<CsmFile>,NodeLink> anEntry = setIterator.next();
            assert anEntry != null;
            
            uidFactory.writeUID(anEntry.getKey(), output);
            anEntry.getValue().write(output);
        }
    }    
    
    private static void readUIDToNodeLinkMap (
            final DataInput input, Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {
        
        assert input != null;
        assert aMap != null;
        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;
        
        aMap.clear();
        
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            final CsmUID<CsmFile> uid = uidFactory.readUID(input);
            final NodeLink        link = new NodeLink(input);
            
            assert uid != null;
            assert link != null;
            
            aMap.put(uid, link);
        }
        
    }
    
    private final Map<CsmUID<CsmFile>,NodeLink> graph = new HashMap<CsmUID<CsmFile>, NodeLink>();
    private ReadWriteLock graphLock = new ReentrantReadWriteLock();
    
    private static class NodeLink implements SelfPersistent, Persistent {
        
        final Set<CsmUID<CsmFile>> in;
        final Set<CsmUID<CsmFile>> out;
        
        private NodeLink(){
            in = new HashSet<CsmUID<CsmFile>>();
            out = new HashSet<CsmUID<CsmFile>>();
        }
        
        private NodeLink(final DataInput input) throws IOException {
            assert input != null;

            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;
            int collSize = input.readInt();
            if (collSize < 0) {
                in = new HashSet<CsmUID<CsmFile>>(0);
            } else {
                in = new HashSet<CsmUID<CsmFile>>(collSize);
            }
            factory.readUIDCollection(in, input, collSize);
            collSize = input.readInt();
            if (collSize < 0) {
                out = new HashSet<CsmUID<CsmFile>>(0);
            } else {
                out = new HashSet<CsmUID<CsmFile>>(collSize);
            }
            factory.readUIDCollection(out, input, collSize);
        }
        
        private void addInLink(CsmUID<CsmFile> inLink){
            in.add(inLink);
        }
        private void removeInLink(CsmUID<CsmFile> inLink){
            in.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getInLinks(){
            return in;
        }
        private void addOutLink(CsmUID<CsmFile> inLink){
            out.add(inLink);
        }
        private void removeOutLink(CsmUID<CsmFile> inLink){
            out.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getOutLinks(){
            return out;
        }

        public void write(final DataOutput output) throws IOException {
            assert output != null;
            assert in != null;
            assert out != null;
            
            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;
            
            factory.writeUIDCollection(in, output, false);
            factory.writeUIDCollection(out, output, false);
        }
    }
    
}
