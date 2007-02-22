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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceAliasImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * objects factory
 * @author Vladimir Voskresensky
 */
public final class CsmObjectFactory extends AbstractObjectFactory implements PersistentFactory {
    
    private static final CsmObjectFactory instance = new CsmObjectFactory();

    private CsmObjectFactory() {
    }
    
    public static CsmObjectFactory instance() {
        return instance;
    }

    public boolean canWrite(Persistent obj) {
        if (obj instanceof FileImpl) {
            return ((FileImpl)obj).getBuffer().isFileBased();
        } else {
            return true;
        }
    }

    protected int getHandler(Object object) {
        assert object != null;
        int aHandler;
        if (object instanceof LibProjectImpl) {
            aHandler = LIB_PROJECT_IMPL;
        } else if (object instanceof ProjectImpl) {
            aHandler = PROJECT_IMPL;
        } else if (object instanceof FileImpl) {
            aHandler = FILE_IMPL;
        } else if (object instanceof NamespaceImpl) {
            aHandler = NAMESPACE_IMPL;
        } else if (object instanceof NamespaceDefinitionImpl) {
            aHandler = NAMESPACE_DEF_IMPL;
        } else if (object instanceof NamespaceAliasImpl) {
            aHandler = NAMESPACE_ALIAS_IMPL;
        } else if (object instanceof UsingDeclarationImpl) {
            aHandler = USING_DECLARATION_IMPL;
        } else if (object instanceof UsingDirectiveImpl) {
            aHandler = USING_DIRECTIVE_IMPL;
        } else if (object instanceof ClassForwardDeclarationImpl) {
            aHandler = CLASS_FORWARD_DECLARATION_IMPL;
        } else if (object instanceof FunctionImpl) {
            // we have several FunctionImpl subclasses
            if (object instanceof FunctionDefinitionImpl) {
                // we have several FunctionDefinitionImpl subclasses
                if (object instanceof DestructorDefinitionImpl) {
                    aHandler = DESTRUCTOR_DEF_IMPL;
                } else if (object instanceof ConstructorDefinitionImpl) {
                    aHandler = CONSTRUCTOR_DEF_IMPL;
                } else {
                    aHandler = FUNCTION_DEF_IMPL;
                }
            } else if (object instanceof MethodImpl) {
                // we have several MethodImpl subclusses
                if (object instanceof MethodDDImpl) {
                    // we have two MethodDDImpl classses:
                    if (object instanceof DestructorDDImpl) {
                        aHandler = DESTRUCTOR_DEF_DECL_IMPL;
                    } else {
                        aHandler = METHOD_DEF_DECL_IMPL;
                    }
                } else if (object instanceof ConstructorImpl) {
                    aHandler = CONSTRUCTOR_IMPL;
                } else if (object instanceof DestructorImpl) {
                    aHandler = DESTRUCTOR_IMPL;
                } else {
                    aHandler = METHOD_IMPL;
                }                
            } else if (object instanceof FunctionDDImpl) {
                aHandler = FUNCTION_DEF_DECL_IMPL;
            } else {
                aHandler = FUNCTION_IMPL;
            }
        } else if (object instanceof VariableImpl) {
            // we have several VariableImpl subclasses
            if (false) {
                aHandler = -1;
            } else {
                aHandler = VARIABLE_IMPL;
            }
        } else if (object instanceof EnumeratorImpl) {
            aHandler = ENUMERATOR_IMPL;
        } else {
            throw new IllegalArgumentException("instance of unknown class" + object.getClass().getName());  //NOI18N
        }
        return aHandler;
    }

    protected SelfPersistent createObject(int handler, DataInput stream) throws IOException {
        SelfPersistent obj;
        
        switch (handler) {
            case PROJECT_IMPL:
                obj = new ProjectImpl(stream);
                break;
                
            case LIB_PROJECT_IMPL:
                obj = new LibProjectImpl(stream);
                break;
                
            case FILE_IMPL:
                obj = new FileImpl(stream);
                break;
                
            case NAMESPACE_IMPL:
                obj = new NamespaceImpl(stream);
                break;
                
            case NAMESPACE_DEF_IMPL:
                obj = new NamespaceDefinitionImpl(stream);
                break;
                
            case NAMESPACE_ALIAS_IMPL:
                obj = new NamespaceAliasImpl(stream);
                break;
                
            case USING_DECLARATION_IMPL:
                obj = new UsingDeclarationImpl(stream);
                break;
                
            case USING_DIRECTIVE_IMPL:
                obj = new UsingDirectiveImpl(stream);
                break;
                
            case CLASS_FORWARD_DECLARATION_IMPL:
                obj = new ClassForwardDeclarationImpl(stream);
                break;
                
            case FUNCTION_IMPL:
                obj = new FunctionImpl(stream);
                break;
                
            case DESTRUCTOR_DEF_IMPL:
                obj = new DestructorDefinitionImpl(stream);
                break;
                
            case CONSTRUCTOR_DEF_IMPL:
                obj = new ConstructorDefinitionImpl(stream);
                break;
                
            case FUNCTION_DEF_IMPL:
                obj = new FunctionDefinitionImpl(stream);
                break;
                
            case DESTRUCTOR_DEF_DECL_IMPL:
                obj = new DestructorDDImpl(stream);
                break;
                
            case METHOD_DEF_DECL_IMPL:
                obj = new MethodDDImpl(stream);
                break;
                
            case CONSTRUCTOR_IMPL:
                obj = new ConstructorImpl(stream);
                break;
                
            case DESTRUCTOR_IMPL:
                obj = new DestructorImpl(stream);
                break;
                
            case METHOD_IMPL:
                obj = new MethodImpl(stream);
                break;
                
            case FUNCTION_DEF_DECL_IMPL:
                obj = new FunctionDDImpl(stream);
                break;
                
            case VARIABLE_IMPL:
                obj = new VariableImpl(stream);
                break;
                
            case ENUMERATOR_IMPL:
                obj = new EnumeratorImpl(stream);
                break;
                
            default:
                throw new IllegalArgumentException("unknown handler" + handler);  //NOI18N
        }
        return obj;
    }

    public void write(DataOutput out, Persistent obj) throws IOException {
        SelfPersistent persistentObj = (SelfPersistent)obj;
        super.writeSelfPersistent(persistentObj, out);
    }

    public Persistent read(DataInput in) throws IOException {
        SelfPersistent persistentObj = super.readSelfPersistent(in);
        assert persistentObj == null || persistentObj instanceof Persistent;
        return (Persistent)persistentObj;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    // handlers to identify different classes of projects
    
    private static final int FIRST_INDEX                    = UIDObjectFactory.LAST_INDEX + 1;
    
    private static final int PROJECT_IMPL                   = FIRST_INDEX;
    private static final int LIB_PROJECT_IMPL               = PROJECT_IMPL + 1;    
    private static final int FILE_IMPL                      = LIB_PROJECT_IMPL + 1;
    private static final int NAMESPACE_IMPL                 = FILE_IMPL + 1;
    private static final int NAMESPACE_DEF_IMPL             = NAMESPACE_IMPL + 1;
    private static final int NAMESPACE_ALIAS_IMPL           = NAMESPACE_DEF_IMPL + 1;
    private static final int USING_DECLARATION_IMPL         = NAMESPACE_ALIAS_IMPL + 1;
    private static final int USING_DIRECTIVE_IMPL           = USING_DECLARATION_IMPL + 1;
    private static final int CLASS_FORWARD_DECLARATION_IMPL = USING_DIRECTIVE_IMPL + 1;
    
    // functions
    private static final int FUNCTION_IMPL                  = CLASS_FORWARD_DECLARATION_IMPL + 1;
    
    //// function definitons 
    private static final int DESTRUCTOR_DEF_IMPL            = FUNCTION_IMPL + 1;
    private static final int CONSTRUCTOR_DEF_IMPL           = DESTRUCTOR_DEF_IMPL + 1;
    private static final int FUNCTION_DEF_IMPL              = CONSTRUCTOR_DEF_IMPL + 1;
    
    //// methods
    private static final int DESTRUCTOR_DEF_DECL_IMPL       = FUNCTION_DEF_IMPL + 1;
    private static final int METHOD_DEF_DECL_IMPL           = DESTRUCTOR_DEF_DECL_IMPL + 1;
    private static final int CONSTRUCTOR_IMPL               = METHOD_DEF_DECL_IMPL + 1;
    private static final int DESTRUCTOR_IMPL                = CONSTRUCTOR_IMPL + 1;
    private static final int METHOD_IMPL                    = DESTRUCTOR_IMPL + 1;
    
    private static final int FUNCTION_DEF_DECL_IMPL         = METHOD_IMPL + 1;
    // end of functions
    
    // variables
    private static final int VARIABLE_IMPL                  = FUNCTION_DEF_DECL_IMPL + 1;
    
    private static final int ENUMERATOR_IMPL                = VARIABLE_IMPL + 1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX        
    public static final int LAST_INDEX = ENUMERATOR_IMPL;    
}
