/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.wsstack.api;

/** Provides WS Stack Version information. 
 * WSStackVersion consists of 4 numbers.
 * Example: Version "1.7.0.3-b32 is parsed in a following way :
 * major = 1, minor = 7, micro = 0, update = 3 (the last part is not important)
 *
 * @author mkuchtiak
 */
public final class WSStackVersion implements Comparable<WSStackVersion> {
    private final int major, minor, micro, update;
    
    /** Constructor for WSStackVersion.
     * The constructor is only be used by valueOf method.
     * 
     */
    private WSStackVersion(int major, int minor, int micro, int update) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.update = update;
    }
    
    /** Major version number of WS Stack. It's the first version number.
     * 
     * @return major(first) version part
     */

    public int getMajor() {
        return major;
    }
    
    /** Minor version number of WS Stack. It's the second version number.
     * 
     * @return minor(second) version part
     */
    public int getMinor() {
        return minor;
    }
    
    /** Micro version number of WS Stack. It's the third version number.
     * 
     * @return micro(third) version part
     */
    public int getMicro() {
        return micro;
    }
    
    /** Update version number of WS Stack. It's the fourth version number.
     * 
     * @return update(fourth) version part
     */
    public int getUpdate() {
        return update;
    }
    
    /** Get WSStackVersion from 4 numbers.
     * Usage: WSStackVersion version = WSStackVersion.valueOf(2,1,3,0); // version "2.1.3"
     * 
     * @param major "major" part of version
     * @param minor "minor" part of version
     * @param micro "micro" part of version
     * @param update "update" part of version
     * @return WSStackVersion object
     */
    public static WSStackVersion valueOf(int major, int minor, int micro, int update) {
        if (major < 0 || minor < 0 || micro  < 0 || update < 0) {
            throw new IllegalArgumentException("Negative version number");
        }
        return new WSStackVersion(major, minor, micro, update);
    }

    /** Compare two versions.
     *
     * if v1 == v2, return 0.
     * if v1 &lt; v2, return a negative number.
     * if v1 &gt; v2, return a positive number.
     * 
     * @param v2 version to compare
     * @return 0(if equals), 1(if greater). -1(if less) 
     */
    public int compareTo(WSStackVersion v2) {

        if (v2 == null) {
            throw new IllegalArgumentException("Cannot pass null as parameter of WSStackVersion.compareTo(WSStackVersion)"); //NOI18N
        }
        // Compare identity
        if (this == v2) return 0;
        
        // Compare major version
        int result = this.major - v2.major;
        if (result != 0) return result/Math.abs(result);
        
        // Compare minor version
        result = this.minor - v2.minor;
        if (result != 0) return result/Math.abs(result);
        
        // Compare micro version
        result = this.micro - v2.micro;
        if (result != 0) return result/Math.abs(result);
        
        // Compare update version
        result = this.update - v2.update;
        if (result != 0) return result/Math.abs(result);
        else return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WSStackVersion other = (WSStackVersion) obj;
        if (this.major == other.major && this.minor == other.minor && this.micro == other.micro && this.update == other.update) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.major;
        hash = 73 * hash + this.minor;
        hash = 73 * hash + this.micro;
        hash = 73 * hash + this.update;
        return hash;
    }

    @Override
    public String toString() {
        return String.valueOf(major)+"."+ //NOI18N
               String.valueOf(minor)+"."+ //NOI18N
               String.valueOf(micro)+"."+ //NOI18N
               String.valueOf(update); //NOI18N
    }

    
}
    