/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project;

/**
 * Represents one user-selectable configuration of a particular project.
 * For example, it might represent a choice of main class and arguments.
 * Besides the implementor, only the project UI infrastructure is expected to use this class.
 * <p>An instance of a configuration may be passed in the context argument for
 * an {@link ActionProvider} when called on a main-project-sensitive action.
 * For details see {@link ProjectConfigurationProvider#configurationsAffectAction}.
 *
 * @author Adam Sotona, Jesse Glick
 * @since org.netbeans.modules.projectapi/1 1.11
 * @see ProjectConfigurationProvider
 */
public interface ProjectConfiguration {

    /**
     * Provides a display name by which this configuration may be identified in the GUI.
     * @return a human-visible display name
     */
    String getDisplayName();

}
