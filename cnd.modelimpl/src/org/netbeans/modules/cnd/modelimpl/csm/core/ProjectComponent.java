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
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * A common ancestor for project components that 
 * 1) has key (most likely a project-based one);
 * 2) are able to put themselves into repository.
 *
 * It similar to Identifiable, but doesn't involve UIDs:
 * UIDs are unnecessary for such internal components as different project parts.
 *
 * @author Vladimir Kvashin
 */
public abstract class ProjectComponent implements Persistent, SelfPersistent {

    private final Key key;
    private final boolean hangInRepository;

    public ProjectComponent(Key key, boolean hangInRepository) {
        this.key = key;
        this.hangInRepository = hangInRepository;
    }

    public ProjectComponent(DataInput in) throws IOException {
        key = KeyFactory.getDefaultFactory().readKey(in);
        hangInRepository = in.readBoolean();
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("< ProjectComponent: Reading %s key %s\n", this, key);
        }
    }

    public Key getKey() {
        return key;
    }

    public void put() {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: Hanging %s by key %s\n", this, key);
        }
        if (hangInRepository) {
            RepositoryUtils.hang(key, this);
        } else {
            RepositoryUtils.put(key, this);
        }
    }

//    private void putImpl() {
//	if( TraceFlags.TRACE_PROJECT_COMPONENT_RW ) System.err.printf("> ProjectComponent: Putting %s by key %s\n", this, key);
//	RepositoryUtils.put(key, this);
//    }
    public void write(DataOutput out) throws IOException {
        if (TraceFlags.TRACE_PROJECT_COMPONENT_RW) {
            System.err.printf("> ProjectComponent: Writing %s by key %s\n", this, key);
        }
        writeKey(key, out);
        out.writeBoolean(hangInRepository);
    }

    public static Key readKey(DataInput in) throws IOException {
        return KeyFactory.getDefaultFactory().readKey(in);
    }

    public static void writeKey(Key key, DataOutput out) throws IOException {
        KeyFactory.getDefaultFactory().writeKey(key, out);
    }

    public static void setStable(Key key) {
        Persistent p = RepositoryUtils.tryGet(key);
        if (p != null) {
            assert p instanceof ProjectComponent;
            //ProjectComponent pc = (ProjectComponent) p;
            // A workaround for #131701
            //pc.putImpl();
        }
    }
}

