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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.TinyCharSequence;

/**
 * implementation of include handler responsible for preventing recursive inclusion
 * @author Vladimir Voskresensky
 */
public class APTIncludeHandlerImpl implements APTIncludeHandler {
    private List<CharSequence> systemIncludePaths;
    private List<CharSequence> userIncludePaths;
    
    private Map<CharSequence, Integer> recurseIncludes = null;
    private static final int MAX_INCLUDE_DEEP = 5;    
    private Stack<IncludeInfo> inclStack = null;
    private StartEntry startFile;
    
    /*package*/ APTIncludeHandlerImpl(StartEntry startFile) {
        this(startFile, new ArrayList<CharSequence>(), new ArrayList<CharSequence>());
    }
    
    public APTIncludeHandlerImpl(StartEntry startFile,
                                    List<CharSequence> systemIncludePaths,
                                    List<CharSequence> userIncludePaths) {
        this.startFile =startFile;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;        
    }

    public boolean pushInclude(CharSequence path, APTInclude aptInclude, int resolvedDirIndex) {
        return pushIncludeImpl(path, aptInclude.getToken().getLine(), aptInclude.getToken().getOffset(), resolvedDirIndex);
    }

    public CharSequence popInclude() {
        return popIncludeImpl();
    }
    
    public APTIncludeResolver getResolver(CharSequence path) {
        return new APTIncludeResolverImpl(path, getCurDirIndex(),
                systemIncludePaths, userIncludePaths);
    }
    
    public StartEntry getStartEntry() {
        return startFile;
    }

    private CharSequence getCurPath() {
        assert (inclStack != null);
        IncludeInfo info = inclStack.peek();
        return info.getIncludedPath();
    }
    
    private int getCurDirIndex() {
        if (inclStack != null && !inclStack.empty()) {
            IncludeInfo info = inclStack.peek();
            return info.getIncludedDirIndex();
        } else {
            return 0;
        }
    }    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    public State getState() {
        return createStateImpl();
    }
    
    public void setState(State state) {
        if (state instanceof StateImpl) {
	    StateImpl stateImpl = ((StateImpl)state);
	    assert ! stateImpl.isCleaned();
            stateImpl.restoreTo(this);
        }
    }
    
    protected StateImpl createStateImpl() {
        return new StateImpl(this);
    }
    
    /** immutable state object of include handler */ 
    public final static class StateImpl implements State, Persistent, SelfPersistent {
        // for now just remember lists
        private final List<CharSequence> systemIncludePaths;
        private final List<CharSequence> userIncludePaths;
        private final StartEntry   startFile;
        
        private final Map<CharSequence, Integer> recurseIncludes;
        private final Stack<IncludeInfo> inclStack;
        
        protected StateImpl(APTIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.startFile = handler.startFile;
            
            if (handler.recurseIncludes != null && !handler.recurseIncludes.isEmpty()) {
                assert (handler.inclStack != null && !handler.inclStack.empty()) : "must be in sync with inclStack";
                this.recurseIncludes = new HashMap<CharSequence, Integer>();
                this.recurseIncludes.putAll(handler.recurseIncludes);
            } else {
                this.recurseIncludes = null;
            }
            if (handler.inclStack != null && !handler.inclStack.empty()) {
                assert (handler.recurseIncludes != null && !handler.recurseIncludes.isEmpty()) : "must be in sync with recurseIncludes";
                this.inclStack = new Stack<IncludeInfo>();
                this.inclStack.addAll(handler.inclStack);
            } else {
                this.inclStack = null;
            }
        }
        
        private StateImpl(StateImpl other, boolean cleanState) {
            // shared information
            this.startFile = other.startFile;
            
            // state object is immutable => safe to share stacks
            this.inclStack = other.inclStack;
	    
            if (cleanState) {
                this.systemIncludePaths = Collections.emptyList();
                this.userIncludePaths = Collections.emptyList();
                this.recurseIncludes = null;
            } else {
                this.systemIncludePaths = other.systemIncludePaths;
                this.userIncludePaths = other.userIncludePaths;
                this.recurseIncludes = other.recurseIncludes;
            }
        }

        int getIncludeStackDepth() {
            return inclStack == null ? 0 : inclStack.size();
        }
        
        private void restoreTo(APTIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            handler.startFile = this.startFile;
            
            // do not restore include info if state is cleaned
            if (!isCleaned()) {
                if (this.recurseIncludes != null) {
                    handler.recurseIncludes = new HashMap<CharSequence, Integer>();
                    handler.recurseIncludes.putAll(this.recurseIncludes);
                }
                if (this.inclStack != null) {
                    handler.inclStack = new Stack<IncludeInfo>();
                    handler.inclStack.addAll(this.inclStack);
                }
            }
        }

        @Override
        public String toString() {
            return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
        }
        
        public void write(DataOutput output) throws IOException {
            assert output != null;
            startFile.write(output);
            
            assert systemIncludePaths != null;
            assert userIncludePaths != null;
            
            int size = systemIncludePaths.size();
            output.writeInt(size);
            for (int i = 0; i < size; i++) {
                output.writeUTF(systemIncludePaths.get(i).toString());
            }
            
            size = userIncludePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                output.writeUTF(userIncludePaths.get(i).toString());
            }
            
            if (recurseIncludes == null) {
                output.writeInt(-1);
            } else {
                final Set<Entry<CharSequence, Integer>> entrySet = recurseIncludes.entrySet();
                assert entrySet != null;
                final Iterator<Entry<CharSequence, Integer>> setIterator = entrySet.iterator();
                assert setIterator != null;
                output.writeInt(entrySet.size());
                
                while (setIterator.hasNext()) {
                    final Entry<CharSequence, Integer> entry = setIterator.next();
                    assert entry != null;
                    
                    output.writeUTF(entry.getKey().toString());
                    output.writeInt(entry.getValue().intValue());
                }
            }
            
            if (inclStack == null) {
                output.writeInt(-1);
            } else {
                size = inclStack.size();
                output.writeInt(size);
                
                for (int i = 0; i < size; i++ ) {
                    final IncludeInfo inclInfo = inclStack.get(i);
                    assert inclInfo != null;
                    
                    final IncludeInfoImpl inclInfoImpl = new IncludeInfoImpl(
                            inclInfo.getIncludedPath(), 
                            inclInfo.getIncludeDirectiveLine(), 
                            inclInfo.getIncludeDirectiveOffset(),
                            inclInfo.getIncludedDirIndex());
                    assert inclInfoImpl != null;
                    
                    inclInfoImpl.write(output);
                    
                }
            }
        }
        
        public StateImpl(final DataInput input) throws IOException {
            assert input != null;
            final APTStringManager pathManager = FilePathCache.getManager();
            
            startFile = new StartEntry(input);

            systemIncludePaths = new ArrayList<CharSequence>();
            int size = input.readInt();
            for (int i = 0; i < size; i++) {
                CharSequence path = pathManager.getString(input.readUTF());
                systemIncludePaths.add(i, path);
            }
            
            userIncludePaths = new ArrayList<CharSequence>();
            size = input.readInt();
            for (int i = 0; i < size; i++) {
                CharSequence path = pathManager.getString(input.readUTF());
                userIncludePaths.add(i, path);                
            }
            
            size = input.readInt();
            if (size == -1) {
                recurseIncludes = null;
            } else {
                recurseIncludes = new HashMap<CharSequence, Integer>();
                
                for (int i = 0; i < size; i++) {
                    CharSequence key = pathManager.getString(input.readUTF());
                    final Integer value = Integer.valueOf(input.readInt());
                    
                    recurseIncludes.put(key, value);
                }
            }
            
            size = input.readInt();
            
            if (size == -1) {
                inclStack = null;
            } else {
                inclStack = new Stack<IncludeInfo>();
                
                for (int i = 0; i < size; i++) {
                    final IncludeInfoImpl impl = new IncludeInfoImpl(input);
                    assert impl != null;
                    
                    inclStack.add(i, impl);
                }
            }
        }        
	
	/* package */ final StartEntry getStartEntry() {
	    return startFile;
	}

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.startFile.equals(other.startFile) &&
                    compareStacks(this.inclStack, other.inclStack);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
            hash = 67 * hash + (this.inclStack != null ? this.inclStack.hashCode() : 0);
            return hash;
        }
        
        private boolean compareStacks(Stack<IncludeInfo> inclStack1, Stack<IncludeInfo> inclStack2) {
            if (inclStack1 != null) {
                int size = inclStack1.size();
                if (inclStack2 == null || inclStack2.size() != size) {
                    return false;
                }
                for (int i = 0; i < size; i++) {
                    IncludeInfo cur1 = inclStack1.get(i);
                    IncludeInfo cur2 = inclStack2.get(i);
                    if (!cur1.equals(cur2)) {
                        return false;
                    }
                }
                return true;
            } else {
                return inclStack2 == null;
            }
        }        

        /*package*/ List<IncludeInfo> getIncludeStack() {
            return this.inclStack;
        }
        
        /*package*/ boolean isCleaned() {
            return this.recurseIncludes == null && this.inclStack != null; // was created as clean state
        }
        
        /*package*/ APTIncludeHandler.State copy(boolean cleanState) {
            return new StateImpl(this, cleanState);
        }
        
        /*package*/ List<CharSequence> getSysIncludePaths() {
            return this.systemIncludePaths;
        }
        
        /*package*/ List<CharSequence> getUserIncludePaths() {
            return this.userIncludePaths;
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private boolean pushIncludeImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
        if (recurseIncludes == null) {
            assert (inclStack == null): inclStack.toString() + " started on " + startFile;
            inclStack = new Stack<IncludeInfo>();
            recurseIncludes = new HashMap<CharSequence, Integer>();
        }
        assert path instanceof TinyCharSequence : "must be char sequence key " + path; // NOI18N
        Integer counter = recurseIncludes.get(path);
        counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue()+1);
        if (counter.intValue() < MAX_INCLUDE_DEEP) {
            recurseIncludes.put(path, counter);
            inclStack.push(new IncludeInfoImpl(path, directiveLine, directiveOffset, resolvedDirIndex));
            return true;
        } else {
            assert (recurseIncludes.get(path) != null) : "included file must be in map"; // NOI18N
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[] { path , getCurPath() }); // NOI18N
            return false;
        }
    }    
    
    private static final class IncludeInfoImpl implements IncludeInfo, SelfPersistent, Persistent {
        private final CharSequence path;
        private final int directiveLine;
        private final int directiveOffset;
        private final int resolvedDirIndex;
        
        public IncludeInfoImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
            assert path != null;
            this.path = path;
            assert directiveLine >= 0;
            this.directiveLine = directiveLine;
            this.directiveOffset = directiveOffset;
            this.resolvedDirIndex = resolvedDirIndex;
        }
        
        public IncludeInfoImpl(final DataInput input) throws IOException {
            assert input != null;
            this.path = FilePathCache.getManager().getString(input.readUTF());
            directiveLine = input.readInt();
            directiveOffset = input.readInt();
            resolvedDirIndex = input.readInt();
        }

        public CharSequence getIncludedPath() {
            return path;
        }

        public int getIncludeDirectiveLine() {
            return directiveLine;
        }

        public int getIncludeDirectiveOffset() {
            return directiveOffset;
        }

        @Override
        public String toString() {
            String retValue;
            
            retValue = "(" + getIncludeDirectiveLine() + "/" + getIncludeDirectiveOffset() + ": " + // NOI18N
                    getIncludedPath() + ":" + getIncludedDirIndex() + ")"; // NOI18N
            return retValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            IncludeInfoImpl other = (IncludeInfoImpl)obj;
            return this.directiveLine == other.directiveLine && this.directiveOffset == other.directiveOffset &&
                    this.path.equals(other.path) && (resolvedDirIndex == other.resolvedDirIndex);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 73 * hash + this.directiveLine;
            hash = 73 * hash + this.directiveOffset;
            hash = 73 * hash + this.resolvedDirIndex;
            return hash;
        }

        public void write(final DataOutput output) throws IOException {
            assert output != null;
            
            output.writeUTF(path.toString());
            output.writeInt(directiveLine);
            output.writeInt(directiveOffset);
            output.writeInt(resolvedDirIndex);
        }

        public int getIncludedDirIndex() {
            return this.resolvedDirIndex;
        }
    }
      
    private CharSequence popIncludeImpl() {
        assert (inclStack != null);
        assert (!inclStack.isEmpty());
        assert (recurseIncludes != null);
        IncludeInfo inclInfo = inclStack.pop();
        CharSequence path = inclInfo.getIncludedPath();
        Integer counter = recurseIncludes.remove(path);
        assert (counter != null) : "must be added before"; // NOI18N
        // decrease include counter
        counter = Integer.valueOf(counter.intValue()-1);
        assert (counter.intValue() >= 0) : "can't be negative"; // NOI18N
        if (counter.intValue() != 0) {
            recurseIncludes.put(path, counter);
        }
        return path;
    }
    
    @Override
    public String toString() {
        return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, recurseIncludes, inclStack);
    }    
    
    private static String toString(CharSequence startFile,
                                    List<CharSequence> systemIncludePaths,
                                    List<CharSequence> userIncludePaths,
                                    Map<CharSequence, Integer> recurseIncludes,
                                    Stack<IncludeInfo> inclStack) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("User includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(userIncludePaths));
        retValue.append("\nSys includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(systemIncludePaths));
        retValue.append("\nInclude Stack starting from:\n"); // NOI18N
        retValue.append(startFile).append("\n"); // NOI18N
        retValue.append(includesStack2String(inclStack));
        return retValue.toString();
    }

    private static String includesStack2String(Stack<IncludeInfo> inclStack) {
        StringBuilder retValue = new StringBuilder();
        if (inclStack == null) {
            retValue.append("<not from #include>"); // NOI18N
        } else {
            for (Iterator<IncludeInfo>  it = inclStack.iterator(); it.hasNext();) {
                IncludeInfo info = it.next();
                retValue.append(info);
                if (it.hasNext()) {
                    retValue.append("->\n"); // NOI18N
                }
            }
        }
        return retValue.toString();
    }
}
