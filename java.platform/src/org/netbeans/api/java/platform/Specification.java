/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.platform;

import org.openide.modules.SpecificationVersion;

/** Specification of the Java SDK
 */
public final class Specification {

    private String name;
    private SpecificationVersion version;
    private Profile[] profiles;


    /**
     * Creates new SDK Specification
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     */
    public Specification (String name, SpecificationVersion version) {
        this (name, version, null);
    }

    /**
     * Creates new SDK Specification
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     * @param profiles of the Java SDK
     */
    public Specification (String name, SpecificationVersion version, Profile[] profiles) {
        this.name = name;
        this.version = version;
        this.profiles = profiles;
    }

    /**
     * Returns the name of the Java specification e.g. J2SE
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the Java specification e.g 1.4
     * @return instance of SpecificationVersion
     */
    public final SpecificationVersion getVersion () {
        return this.version;
    }

    /**
     * Returns profiles supported by the Java platform.
     * @return list of profiles, or null if not applicable
     */
    public final Profile[] getProfiles () {
        return this.profiles;
    }

    public int hashCode () {
        int hc = 0;
        if (this.name != null)
            hc = this.name.hashCode() << 16;
        if (this.version != null)
            hc += this.version.hashCode();
        return hc;
    }

    public boolean equals (Object other) {
        if (other instanceof Specification) {
            Specification os = (Specification) other;
            boolean re = this.name == null ? os.name == null : this.name.equals(os.name) &&
                         this.version == null ? os.version == null : this.version.equals (os.version);
            if (!re || this.profiles == null)
                return re;
            if (os.profiles == null || this.profiles.length != os.profiles.length)
                return false;
            for (int i=0; i<os.profiles.length; i++)
                re &= this.profiles[i].equals(os.profiles[i]);
            return re;
        }
        else
            return false;
    }

    public String toString () {
        String str = this.name == null ? "" : this.name + " "; // NOI18N
        str += this.version == null ? "" : this.version + " "; // NOI18N
        if (this.profiles != null) {
            str+="["; // NOI18N
            for (int i = 0; i < profiles.length; i++) {
                str+= profiles[i]+ " "; // NOI18N
            }
            str+="]"; // NOI18N
        }
        return str;
    }

}
