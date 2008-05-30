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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;

/**
 * The class allowing the client to query the instance identified by
 * the instance ID. Because corresponding instance can be removed anytime
 * each method can throw {@link InstanceRemovedException}.
 *
 * @author Petr Hejl
 * @since 1.45
 */
public final class ServerInstance {

    /*
     * There is bit wider synchronization on ServerRegistry in following
     * methods. This is because even getters on instance implementation
     * are backed by other objects that can disappear asynchronously.
     * To avoid this we are doing this synchronization (remove is using registry).
     */

    private final String serverInstanceId;

    ServerInstance(String serverInstanceId) {
        assert serverInstanceId != null : "Server instance id is null"; // NOI18N
        this.serverInstanceId = serverInstanceId;
    }

    /**
     * Returns the display name of this instance.
     *
     * @return the display name of this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getDisplayName() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                    getInstanceFromRegistry(registry);
            return inst.getDisplayName();
        }
    }

    /**
     * Returns the display name of the server.
     *
     * @return the display name of the server
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getServerDisplayName() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
            return inst.getServer().getDisplayName();
        }
    }

    /**
     * Returns the ID of the server associated with this instance.
     *
     * @return the ID of the server associated with this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public String getServerID() throws InstanceRemovedException {
        final ServerRegistry registry = ServerRegistry.getInstance();
        // see comment at the beginning of the class
        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = getInstanceFromRegistry(registry);
            return inst.getServer().getShortName();
        }
    }

    /**
     * Returns <code>true</code> if this instance is running, <code>false</code>
     * otherwise.
     *
     * @return <code>true</code> if this instance is running, <code>false</code>
     *             otherwise
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public boolean isRunning() throws InstanceRemovedException {
        final org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                getInstanceFromRegistry(ServerRegistry.getInstance());

        return (inst.isReallyRunning() || inst.isSuspended());
    }

    /**
     * Returns the platform for this instance.
     *
     * @return the platform for this instance
     * @throws InstanceRemovedException if the instance is not available anymore
     */
    public J2eePlatform getJ2eePlatform() throws InstanceRemovedException {
         return J2eePlatform.create(getInstanceFromRegistry(ServerRegistry.getInstance()));
    }

    /**
     * Returns descriptor providing extra information about the instance. May
     * return <code>null</code> if the server does not support this.
     *
     * @return descriptor providing extra information about the instance or <code>null</code>
     * @throws InstanceRemovedException if the instance is not available anymore
     * @since 1.46
     */
    public Descriptor getDescriptor() throws InstanceRemovedException {
        if (getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor() != null) {
            return new Descriptor();
        }
        return null;
    }

    private org.netbeans.modules.j2ee.deployment.impl.ServerInstance getInstanceFromRegistry(ServerRegistry registry)
            throws InstanceRemovedException {

        synchronized (registry) {
            org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst =
                    registry.getServerInstance(serverInstanceId);
            if (inst == null) {
                throw new InstanceRemovedException(serverInstanceId);
            }
            return inst;
        }
    }

    /**
     * Descriptor providing extra (and optional) information about the server instance.
     * @since 1.46
     */
    public final class Descriptor {

        /**
         * Returns the HTTP port of the server.
         *
         * @return the HTTP port of the server
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public int getHttpPort() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().getHttpPort();
        }

        /**
         * Returns the hostname of the server. Returned name is usable to reach
         * the server from the computer where IDE runs.
         *
         * @return the hostname of the server
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public String getHostname() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().getHostname();
        }

        /**
         * Returns <code>true</code> if the server is installed locally,
         * <code>false</code> otherwise.
         *
         * @return <code>true</code> if the server is installed locally
         * @throws InstanceRemovedException if the instance is not available anymore
         */
        public boolean isLocal() throws InstanceRemovedException {
            return getInstanceFromRegistry(ServerRegistry.getInstance()).getServerInstanceDescriptor().isLocal();
        }
    }
}
