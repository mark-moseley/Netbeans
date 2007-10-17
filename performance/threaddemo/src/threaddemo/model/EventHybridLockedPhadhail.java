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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package threaddemo.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;
import threaddemo.locking.LockAction;
import threaddemo.locking.LockExceptionAction;
import threaddemo.locking.Locks;

/**
 * Similar to LockedPhadhail but using the "event hybrid" lock.
 * @author Jesse Glick
 */
final class EventHybridLockedPhadhail extends AbstractPhadhail {

    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new EventHybridLockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private EventHybridLockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        return Locks.eventHybrid().read(new LockAction<List<Phadhail>>() {
            public List<Phadhail> run() {
                return EventHybridLockedPhadhail.super.getChildren();
            }
        });
    }
    
    public String getName() {
        return Locks.eventHybrid().read(new LockAction<String>() {
            public String run() {
                return EventHybridLockedPhadhail.super.getName();
            }
        });
    }
    
    public String getPath() {
        return Locks.eventHybrid().read(new LockAction<String>() {
            public String run() {
                return EventHybridLockedPhadhail.super.getPath();
            }
        });
    }
    
    public boolean hasChildren() {
        return Locks.eventHybrid().read(new LockAction<Boolean>() {
            public Boolean run() {
                return EventHybridLockedPhadhail.super.hasChildren();
            }
        });
    }
    
    public void rename(final String nue) throws IOException {
        Locks.eventHybrid().write(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                EventHybridLockedPhadhail.super.rename(nue);
                return null;
            }
        });
    }
    
    public Phadhail createContainerPhadhail(final String name) throws IOException {
        return Locks.eventHybrid().write(new LockExceptionAction<Phadhail,IOException>() {
            public Phadhail run() throws IOException {
                return EventHybridLockedPhadhail.super.createContainerPhadhail(name);
            }
        });
    }
    
    public Phadhail createLeafPhadhail(final String name) throws IOException {
        return Locks.eventHybrid().write(new LockExceptionAction<Phadhail,IOException>() {
            public Phadhail run() throws IOException {
                return EventHybridLockedPhadhail.super.createLeafPhadhail(name);
            }
        });
    }
    
    public void delete() throws IOException {
        Locks.eventHybrid().write(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                EventHybridLockedPhadhail.super.delete();
                return null;
            }
        });
    }
    
    public InputStream getInputStream() throws IOException {
        return Locks.eventHybrid().read(new LockExceptionAction<InputStream,IOException>() {
            public InputStream run() throws IOException {
                return EventHybridLockedPhadhail.super.getInputStream();
            }
        });
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comments in AbstractPhadhail.getOutputStream.
        return Locks.eventHybrid().read(new LockExceptionAction<OutputStream,IOException>() {
            public OutputStream run() throws IOException {
                return EventHybridLockedPhadhail.super.getOutputStream();
            }
        });
    }
    
    public RWLock lock() {
        return Locks.eventHybrid();
    }
    
}
