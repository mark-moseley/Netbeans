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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import gui.menu.*;
import gui.window.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure Profiler functionality
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureProfiler  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        // Menus
        suite.addTest(new MainMenu("testProfileMenu", "Profile main menu"));
        
        // Dialogs
        suite.addTest(new SelectProfilingTaskDialog("measureTime", "Select Profiling Task dialog open"));
        suite.addTest(new ProfilerAboutDialog("doMeasurement","Profiler About Dialog"));
        // Windows
        suite.addTest(new ProfilerWindows("testProfilerControlPanel","Open Profiler Control Panel Window"));
        suite.addTest(new ProfilerWindows("testProfilerTelemetryOverview","Open Profiler VM Telemetry Overview Window"));
        suite.addTest(new ProfilerWindows("testProfilerLiveResults","Open Profiler Live Results Window"));
        suite.addTest(new ProfilerWindows("testProfilerVMTelemetry","Open Profiler Profiler VM Telemetry Window Window"));
        suite.addTest(new ProfilerWindows("testProfilerThreads","Open Profiler Threads Window"));
        suite.addTest(new ProfilerWindows("testProfilerProfilingPoints","Open Profiler Profiling Pints Window"));
        
        return suite;
    }
    
}
