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

package pty;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

interface PtyLibrary extends Library {

    public static class WinSize extends Structure {
        public short ws_row;
        public short ws_col;
        public short ws_xpixel;
        public short ws_ypixel;
    }

    PtyLibrary INSTANCE = (PtyLibrary) Native.loadLibrary("c", PtyLibrary.class);

    public int getpt();

    public int grantpt(int master_fd);

    public int unlockpt(int master_fd);

    public String ptsname(int master_fd);

    // generic unix support stuff
    public int close(int fd);
    public int open(String pathname, int flags);
    public int ioctl(int fd, int op, String str_arg);
    public int ioctl(int fd, int op, WinSize winsize);
    public String strerror(int errno);
    public int kill(int pid, int sig);

    // Linux: /bits/fcntl.h
    public static final int O_RDWR = 2;

    // Linux: /bits/stropts.h
    static final int __SID = ('S' << 8);
    public static final int I_PUSH = (__SID | 2);

    // Linux: /ioctls.h
    public static final int TIOCSWINSZ = 0x5414;

    public static final int SIGHUP = 1;
}
