/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.APTStringManager;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NoType;
import org.netbeans.modules.cnd.modelimpl.csm.TypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.csm.deep.EmptyCompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.LazyCompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTPreprocStateImpl;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;

/**
 *
 * @author Vladimir Voskresensky
 */
public class PersistentUtils { 
    
    private PersistentUtils() {
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // support file buffers
    
    public static void writeBuffer(FileBuffer buffer, DataOutput output) throws IOException {
        assert buffer != null;
        if (buffer instanceof FileBufferFile) {
            output.writeInt(FILE_BUFFER_FILE);
            ((FileBufferFile)buffer).write(output);
        } else {
            throw new IllegalArgumentException("instance of unknown FileBuffer " + buffer);  //NOI18N
        }        
    }
    
    public static FileBuffer readBuffer(DataInput input) throws IOException {
        FileBuffer buffer;
        int handler = input.readInt();
        assert handler == FILE_BUFFER_FILE;
        buffer = new FileBufferFile(input);
        return buffer;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support string (arrays)
    
    public static void writeStrings(String[] arr, DataOutput output) throws IOException {
        if (arr == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int len = arr.length;
            output.writeInt(len);
            for (int i = 0; i < len; i++) {
                assert arr[i] != null;
                output.writeUTF(arr[i]);
            }
        }
    }
    
    public static String[] readStrings(DataInput input, APTStringManager manager) throws IOException {
        String[] arr = null;
        int len = input.readInt();
        if (len != AbstractObjectFactory.NULL_POINTER) {
            arr = new String[len];
            for (int i = 0; i < len; i++) {
                String str = input.readUTF();
                assert str != null;
                arr[i] = manager.getString(str);
            }
        }
        return arr;
    }   
    
    public static void writeUTF(String st, DataOutput aStream) throws IOException
    {
        if (st != null) {
            aStream.writeBoolean(true);
            aStream.writeUTF(st);
        } else {
            aStream.writeBoolean(false);
        }
    }
    
    public static String readUTF(DataInput aStream) throws IOException
    {
        return aStream.readBoolean() ? aStream.readUTF() : null;
    }
    ////////////////////////////////////////////////////////////////////////////
    // support CsmExpression

    public static void writeExpression(CsmExpression expr, DataOutput output) throws IOException {
        if (expr == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            if (expr instanceof ExpressionBase) {
                output.writeInt(EXPRESSION_BASE);
                ((ExpressionBase)expr).write(output);            
            } else {
                throw new IllegalArgumentException("instance of unknown CsmExpression " + expr);  //NOI18N
            }
        }
    }
    
    public static CsmExpression readExpression(DataInput input) throws IOException {
        int handler = input.readInt();
        CsmExpression expr;
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            expr = null;
        } else {
            assert handler == EXPRESSION_BASE;
            expr = new ExpressionBase(input);
        }
        return expr;
    }
    
    public static void writeExpressions(Collection<CsmExpression> exprs, DataOutput output) throws IOException {
        if (exprs == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int collSize = exprs.size();
            output.writeInt(collSize);

            for (CsmExpression expr: exprs) {
                assert expr != null;
                writeExpression(expr, output);
            }            
        }
    }
    
    public static <T extends Collection<CsmExpression>> T readExpressions(T collection, DataInput input) throws IOException {
        int collSize = input.readInt();
        if (collSize == AbstractObjectFactory.NULL_POINTER) {
            collection = null;
        } else {
            for (int i = 0; i < collSize; ++i) {
                CsmExpression expr = readExpression(input);
                assert expr != null;
                collection.add(expr);
            }
            return collection;
        }
        return collection;
    }
    
    public static void writeExpressionKind(CsmExpression.Kind kind, DataOutput output) throws IOException {
        if (kind == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            throw new UnsupportedOperationException("Not yet implemented"); //NOI18N
        }
    }

    public static CsmExpression.Kind readExpressionKind(DataInput input) throws IOException {
        int handler = input.readInt();
        CsmExpression.Kind kind;
        if (handler == AbstractObjectFactory.NULL_POINTER) {
            kind = null;
        } else {
            throw new UnsupportedOperationException("Not yet implemented"); //NOI18N
        }
        return kind;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support types
    
    public static CsmType readType(DataInput stream) throws IOException {
        CsmType obj;
        int handler = stream.readInt();
        switch (handler) {
        case AbstractObjectFactory.NULL_POINTER:
            obj = null;
            break;
            
        case NO_TYPE:
            obj = NoType.instance();
            break;

        case TYPE_IMPL:
            obj = new TypeImpl(stream);
            break;        
            
        default:
            throw new IllegalArgumentException("unknown type handler" + handler);  //NOI18N
        }
        return obj;
    }
    
    public static void writeType(CsmType type, DataOutput stream) throws IOException {
        if (type == null) {
            stream.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else if (type instanceof NoType) {
            stream.writeInt(NO_TYPE);
        } else if (type instanceof TypeImpl) {
            stream.writeInt(TYPE_IMPL);
            ((TypeImpl)type).write(stream);
        } else {
            throw new IllegalArgumentException("instance of unknown class " + type.getClass().getName());  //NOI18N
        }       
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support inheritance 
    
    private static void writeInheritance(CsmInheritance inheritance, DataOutput output) throws IOException {
        assert inheritance != null;
        if (inheritance instanceof InheritanceImpl) {
            ((InheritanceImpl)inheritance).write(output);            
        } else {
            throw new IllegalArgumentException("instance of unknown CsmInheritance " + inheritance);  //NOI18N
        }
    }
    
    private static CsmInheritance readInheritance(DataInput input) throws IOException {
        CsmInheritance inheritance = new InheritanceImpl(input);
        return inheritance;
    }
        
    public static <T extends Collection<CsmInheritance>> void readInheritances(T collection, DataInput input) throws IOException {
        int collSize = input.readInt();
        assert collSize >= 0;
        for (int i = 0; i < collSize; ++i) {
            CsmInheritance inheritance = readInheritance(input);
            assert inheritance != null;
            collection.add(inheritance);
        }
    }
    
    public static void writeInheritances(Collection<? extends CsmInheritance> inhs, DataOutput output) throws IOException {
        assert inhs != null;
        int collSize = inhs.size();
        output.writeInt(collSize);

        for (CsmInheritance elem: inhs) {
            assert elem != null;
            writeInheritance(elem, output);
        }            
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support visibility
    
    public static void writeVisibility(CsmVisibility visibility, DataOutput output) throws IOException {
        assert visibility != null;
        int handler = -1;
        if (visibility == CsmVisibility.PUBLIC) {
            handler = VISIBILITY_PUBLIC;
        } else if (visibility == CsmVisibility.PROTECTED) {
            handler = VISIBILITY_PROTECTED;
        } else if (visibility == CsmVisibility.PRIVATE) {
            handler = VISIBILITY_PRIVATE;
        } else if (visibility == CsmVisibility.NONE) {
            handler = VISIBILITY_NONE;
        } else {
            throw new IllegalArgumentException("instance of unknown visibility " + visibility);  //NOI18N
        }
        output.writeInt(handler);
    }
    
    public static CsmVisibility readVisibility(DataInput input) throws IOException {
        CsmVisibility visibility = null;
        int handler = input.readInt();
        switch (handler) {
            case VISIBILITY_PUBLIC:
                visibility = CsmVisibility.PUBLIC;
                break;
                
            case VISIBILITY_PROTECTED:
                visibility = CsmVisibility.PROTECTED;
                break;
                
            case VISIBILITY_PRIVATE:
                visibility = CsmVisibility.PRIVATE;
                break;
                
            case VISIBILITY_NONE:
                visibility = CsmVisibility.NONE;
                break;                
            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N
        }       
        return visibility;
    }     

    ////////////////////////////////////////////////////////////////////////////
    // compound statements
    
    public static void writeCompoundStatement(CsmCompoundStatement body, DataOutput output) throws IOException {
        assert body != null;
        if (body instanceof LazyCompoundStatementImpl) {
            output.writeInt(LAZY_COMPOUND_STATEMENT_IMPL);
            ((LazyCompoundStatementImpl)body).write(output);
        } else if (body instanceof EmptyCompoundStatementImpl) {
            output.writeInt(EMPTY_COMPOUND_STATEMENT_IMPL);
            ((EmptyCompoundStatementImpl)body).write(output);
        } else {
            throw new IllegalArgumentException("unknown compound statement " + body);  //NOI18N
        }
    }

    public static CsmCompoundStatement readCompoundStatement(DataInput input) throws IOException {
        int handler = input.readInt();
        CsmCompoundStatement body;
        switch (handler) {
            case LAZY_COMPOUND_STATEMENT_IMPL:
                body = new LazyCompoundStatementImpl(input);
                break;
            case EMPTY_COMPOUND_STATEMENT_IMPL:
                body = new EmptyCompoundStatementImpl(input);
                break;
            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N                
        }
        return body;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // support preprocessor states
    

    public static void writeStringToStateMap(Map<String, APTPreprocState.State> filesHandlers, DataOutput output) throws IOException {
        assert filesHandlers != null;
        int collSize = filesHandlers.size();
        output.writeInt(collSize);

        for (Entry<String, APTPreprocState.State> entry: filesHandlers.entrySet()) {
            assert entry != null;
            String key = entry.getKey();
            output.writeUTF(key);
            assert key != null;
            APTPreprocState.State state = entry.getValue();
            writePreprocStateState(state, output);
        }         
    }

    public static void readStringToStateMap(Map<String, APTPreprocState.State> filesHandlers, DataInput input) throws IOException {
        assert filesHandlers != null;
        int collSize = input.readInt();
        
        for (int i = 0; i < collSize; i++) {
            String key = FilePathCache.getString(input.readUTF());
            assert key != null;
            APTPreprocState.State state = readPreprocStateState(input);
            assert state != null;
            filesHandlers.put(key, state);
        }
    }
    
    private static void writePreprocStateState(APTPreprocState.State state, DataOutput output) throws IOException {
        assert state != null;
        if (state instanceof APTPreprocStateImpl.StateImpl) {
            output.writeInt(PREPROC_STATE_STATE_IMPL);
            ((APTPreprocStateImpl.StateImpl)state).write(output);
        } else {
            throw new IllegalArgumentException("unknown preprocessor state" + state);  //NOI18N
        }        
    }
    
    private static APTPreprocState.State readPreprocStateState(DataInput input) throws IOException {
        int handler = input.readInt();
        APTPreprocState.State out;
        switch (handler) {
            case PREPROC_STATE_STATE_IMPL:
                out = new APTPreprocStateImpl.StateImpl(input);
                break;
            default:
                throw new IllegalArgumentException("unknown preprocessor state handler" + handler);  //NOI18N
        }
        return out;
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // indices
    
    
    private static final int FIRST_INDEX            = CsmObjectFactory.LAST_INDEX + 1;
    
    private static final int VISIBILITY_PUBLIC      = FIRST_INDEX;
    private static final int VISIBILITY_PROTECTED   = VISIBILITY_PUBLIC + 1;
    private static final int VISIBILITY_PRIVATE     = VISIBILITY_PROTECTED + 1;
    private static final int VISIBILITY_NONE        = VISIBILITY_PRIVATE + 1;    
    
    private static final int EXPRESSION_BASE        = VISIBILITY_NONE + 1;
    
    private static final int FILE_BUFFER_FILE       = EXPRESSION_BASE + 1;
    // types
    private static final int NO_TYPE                = FILE_BUFFER_FILE + 1;
    private static final int TYPE_IMPL              = NO_TYPE + 1;
    
    // state 
    private static final int PREPROC_STATE_STATE_IMPL = TYPE_IMPL + 1;
    
    // compound statements
    private static final int LAZY_COMPOUND_STATEMENT_IMPL = PREPROC_STATE_STATE_IMPL + 1;
    private static final int EMPTY_COMPOUND_STATEMENT_IMPL = LAZY_COMPOUND_STATEMENT_IMPL + 1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX        
    public static final int LAST_INDEX              = EMPTY_COMPOUND_STATEMENT_IMPL;
}
