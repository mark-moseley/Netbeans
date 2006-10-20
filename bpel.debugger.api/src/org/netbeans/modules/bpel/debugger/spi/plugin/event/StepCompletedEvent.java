/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.bpel.debugger.spi.plugin.event;

/**
 * Notification of step completion in the target BPEL Engine.
 * This event is generated immediately before the activity at its
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.def.Position}
 * is executed. When a next activity is about to start its execution and
 * there is an enabled
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.StepCompletedRequest},
 * an
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventSet}
 * containing an instance of this class will be added to the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngine}'s
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue}.
 *
 * @author Alexander Zgursky
 */
public interface StepCompletedEvent extends LocatableEvent {
    
}
