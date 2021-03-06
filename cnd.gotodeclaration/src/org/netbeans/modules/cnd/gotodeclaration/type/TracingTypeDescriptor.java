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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.type;

import javax.swing.Icon;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;

/**
 * A wrapper used for tracin
 * @author Vladimir Kvashin
 */
/* package-local */
class TracingTypeDescriptor extends TypeDescriptor {
    
    private TypeDescriptor delegate;
    private String name;

    TracingTypeDescriptor(TypeDescriptor delegate) {
	this.delegate = delegate;
	name = delegate.getSimpleName();
    }
	    
    public String getContextName() {
	System.err.printf("TypeDescriptor.getContextName(%s)\n", name);
	return delegate.getContextName();
    }

    public FileObject getFileObject() {
	System.err.printf("TypeDescriptor.getFileObject(%s)\n", name);
	return delegate.getFileObject();
    }

    public Icon getIcon() {
	System.err.printf("TypeDescriptor.getIcon(%s)\n", name);
	return delegate.getIcon();
    }

    public int getOffset() {
	System.err.printf("TypeDescriptor.getOffset(%s)\n", name);
	return delegate.getOffset();
    }

    public String getOuterName() {
	System.err.printf("TypeDescriptor.(getOuterName)\n", name);
	return delegate.getOuterName();
    }

    public Icon getProjectIcon() {
	System.err.printf("TypeDescriptor.getProjectIcon(%s)\n", name);
	return delegate.getProjectIcon();
    }

    public String getProjectName() {
	System.err.printf("TypeDescriptor.getProjectName(%s)\n", name);
	return delegate.getProjectName();
    }

    public String getSimpleName() {
	System.err.printf("TypeDescriptor.getSimpleName(%s)\n", name);
	return delegate.getSimpleName();
    }

    public String getTypeName() {
	System.err.printf("TypeDescriptor.getTypeName(%s)\n", name);
	return delegate.getTypeName();
    }

    public void open() {
	System.err.printf("TypeDescriptor.open(%s)\n", name);
	delegate.open();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TracingTypeDescriptor other = (TracingTypeDescriptor) obj;
        if (this.delegate != other.delegate && (this.delegate == null || !this.delegate.equals(other.delegate))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.delegate != null ? this.delegate.hashCode() : 0);
        return hash;
    }
    
}
