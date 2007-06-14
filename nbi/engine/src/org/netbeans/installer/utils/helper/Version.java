/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper;

/**
 *
 * @author Kirill Sorokin
 */
public class Version {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static Version getVersion(
            final String string) {
        if (string.matches("([0-9]+[\\._\\-]+)*[0-9]+")) {
            return new Version(string);
        } else {
            return null;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private long major;
    private long minor;
    private long micro;
    private long update;
    private long build;
    
    private Version(
            final String string) {
        String[] split = string.split("[\\._\\-]+"); //NOI18N
        
        if (split.length > 0) {
            major = new Long(split[0]);
        }
        if (split.length > 1) {
            minor = new Long(split[1]);
        }
        if (split.length > 2) {
            micro = new Long(split[2]);
        }
        if (split.length > 3) {
            update = new Long(split[3]);
        }
        if (split.length > 4) {
            build = new Long(split[4]);
        }
    }
    
    public boolean equals(
            final Version version) {
        return ((major == version.major) &&
                (minor == version.minor) &&
                (micro == version.micro) &&
                (update == version.update) &&
                (build == version.build)) ? true : false;
    }
    
    public boolean newerThan(
            final Version version) {
        if (major > version.major) {
            return true;
        } else if (major == version.major) {
            if (minor > version.minor) {
                return true;
            } else if (minor == version.minor) {
                if (micro > version.micro) {
                    return true;
                } else if (micro == version.micro) {
                    if (update > version.update) {
                        return true;
                    } else if (update == version.update) {
                        if (build > version.build) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean newerOrEquals(
            final Version version) {
        if (newerThan(version) || equals(version)) {
            return true;
        }
        
        return false;
    }
    
    public boolean olderThan(
            final Version version) {
        if (!newerOrEquals(version)) {
            return true;
        }
        
        return false;
    }
    
    public boolean olderOrEquals(
            final Version version) {
        if (!newerThan(version)) {
            return true;
        }
        
        return false;
    }
    
    public VersionDistance getDistance(
            final Version version) {
        return new VersionDistance(this, version);
    }
    
    public long getMajor() {
        return major;
    }
    
    public long getMinor() {
        return minor;
    }
    
    public long getMicro() {
        return micro;
    }
    
    public long getUpdate() {
        return update;
    }
    
    public long getBuild() {
        return build;
    }
    
    @Override
    public String toString() {
        return "" + major + "." + minor + "." + micro + "." + update + "." + build;
    }
    
    public String toMajor() {
        return "" + major;
    }
    
    public String toMinor() {
        return "" + major + "." + minor;
    }
    
    public String toMicro() {
        return "" + major + "." + minor + "." + micro;
    }
    
    public String toJdkStyle() {
        return "" + major +
                "." + minor +
                "." + micro +
                (update != 0 ? "_" + (update < 10 ? "0" + update : update) : "");
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class VersionDistance {
        private long majorDistance;
        private long minorDistance;
        private long microDistance;
        private long updateDistance;
        private long buildDistance;
        
        private VersionDistance(
                final Version version1,
                final Version version2) {
            majorDistance = Math.abs(version1.getMajor() - version2.getMajor());
            minorDistance = Math.abs(version1.getMinor() - version2.getMinor());
            microDistance = Math.abs(version1.getMicro() - version2.getMicro());
            updateDistance = Math.abs(version1.getUpdate() - version2.getUpdate());
            buildDistance = Math.abs(version1.getBuild() - version2.getBuild());
        }
        
        public boolean equals(
                final VersionDistance distance) {
            return ((majorDistance == distance.majorDistance) &&
                    (minorDistance == distance.minorDistance) &&
                    (microDistance == distance.microDistance) &&
                    (updateDistance == distance.updateDistance) &&
                    (buildDistance == distance.buildDistance)) ? true : false;
        }
        
        public boolean greaterThan(
                final VersionDistance distance) {
            if (majorDistance > distance.majorDistance) {
                return true;
            } else if (majorDistance == distance.majorDistance) {
                if (minorDistance > distance.minorDistance) {
                    return true;
                } else if (minorDistance == distance.minorDistance) {
                    if (microDistance > distance.microDistance) {
                        return true;
                    } else if (microDistance == distance.microDistance) {
                        if (updateDistance > distance.updateDistance) {
                            return true;
                        } else if (updateDistance == distance.updateDistance) {
                            if (buildDistance > distance.buildDistance) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            return false;
        }
        
        public boolean greaterOrEquals(
                final VersionDistance version) {
            if (greaterThan(version) || equals(version)) {
                return true;
            }
            
            return false;
        }
        
        public boolean lessThan(
                final VersionDistance version) {
            if (!greaterOrEquals(version)) {
                return true;
            }
            
            return false;
        }
        
        public boolean lessOrEquals(
                final VersionDistance distance) {
            if (!greaterThan(distance)) {
                return true;
            }
            
            return false;
        }
    }
}
