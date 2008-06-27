
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

package org.netbeans.modules.cnd.repository.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author Sergey Grinev
 */
public abstract class AbstractObjectFactory {

    protected abstract int getHandler(Object object);
    protected abstract SelfPersistent createObject(int handler, DataInput stream) throws IOException;
    
    protected final void writeSelfPersistent(SelfPersistent object, DataOutput output) throws IOException
    {
        if (object == null) {
            output.writeShort(NULL_POINTER);
        } else {
            int handler = getHandler(object);
            assert handler != NULL_POINTER && handler <= Short.MAX_VALUE;
            output.writeShort(handler);
            object.write(output);
        }
    }
    
    protected final SelfPersistent readSelfPersistent(DataInput input) throws IOException
    {
        int handler = input.readShort();
        SelfPersistent object = null;
        if (handler != NULL_POINTER) {
            object = createObject(handler, input);
            assert object != null;
        }
        return object;
    }
    
    public static final int NULL_POINTER = -1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX = 0; 
}
