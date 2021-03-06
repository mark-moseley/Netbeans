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

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.netbeans.modules.ruby.platform.DebuggerPreferences;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.platform.spi.RubyDebuggerImplementation;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.RubyDebuggerFactory;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

/**
 * Implementation of {@link RubyDebuggerImplementation} SPI, providing an entry
 * to point to the Ruby debugging.
 *
 * @author Martin Krauskopf
 */
public final class RubyDebugger implements RubyDebuggerImplementation {
    
    private static final String PATH_TO_CLASSIC_DEBUG_DIR;
    
    static {
        String path = "ruby/debug-commons-0.9.5/classic-debug.rb"; // NOI18N
        File classicDebug = InstalledFileLocator.getDefault().locate(
                path, "org.netbeans.modules.ruby.debugger", false); // NOI18N
        if (classicDebug == null || !classicDebug.isFile()) {
            throw new IllegalStateException("Cannot locate classic debugger in NetBeans Ruby cluster (" + path + ')'); // NOI18N
        }
        PATH_TO_CLASSIC_DEBUG_DIR = classicDebug.getParentFile().getAbsolutePath();
    }
    
    /** @see RubyDebuggerImplementation#debug */
    public Process debug(final ExecutionDescriptor descriptor) {
        Process p = null;
        try {
            p = startDebugging(descriptor);
        } catch (IOException e) {
            problemOccurred(e);
        } catch (RubyDebuggerException e) {
            problemOccurred(e);
        }
        return p;
    }
    
    private static void problemOccurred(final Exception e) {
        Util.showWarning(NbBundle.getMessage(RubyDebugger.class, "RubyDebugger.startup.problem", e.getMessage()));
    }
    
    /**
     * Starts debugging of the given script using the given interpreter.
     *
     * @param descriptor description of the process to be debugged
     * @return debugger {@link java.lang.Process process}. Might be
     *         <tt>null</tt> if debugging cannot be started for some reason.
     *         E.g. interpreter cannot be obtained from preferences.
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    static Process startDebugging(final ExecutionDescriptor descriptor)
            throws IOException, RubyDebuggerException {
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        final RubyPlatform platform = descriptor.getPlatform();
        boolean jrubySet = platform.isJRuby();

        if (!checkAndTuneSettings(descriptor)) {
            return null;
        }
        
        RubyDebuggerFactory.Descriptor debugDesc = new RubyDebuggerFactory.Descriptor();
        debugDesc.useDefaultPort(false);
        debugDesc.setJRuby(descriptor.getPlatform().isJRuby());
        debugDesc.setScriptPath(descriptor.getScript());
        if (descriptor.getInitialArgs() != null) {
            List<String> additionalOptions = new ArrayList<String>();
            additionalOptions.addAll(Arrays.asList(descriptor.getInitialArgs()));
            debugDesc.setAdditionalOptions(additionalOptions);
        }
//        List<String> additionalOptions = new ArrayList<String>();
//        additionalOptions.add("-J-Djruby.compile.mode=OFF");
//        additionalOptions.add("-d");
//        additionalOptions.add("-J-Xdebug");
//        additionalOptions.add("-J-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y");
//        debugDesc.setAdditionalOptions(additionalOptions);
        debugDesc.setScriptArguments(descriptor.getAdditionalArgs());
        debugDesc.setSynchronizedOutput(true);
        if (descriptor.getPwd() != null) {
            debugDesc.setBaseDirectory(descriptor.getPwd());
        }
        Map<String, String> env = new  HashMap<String, String>();
        GemManager.adjustEnvironment(platform, env);
        if (jrubySet) {
            env.putAll(getJRubyEnvironment(descriptor));
        }
        debugDesc.setEnvironment(env);
        RubyDebuggerProxy proxy;
        int timeout = Integer.getInteger("org.netbeans.modules.ruby.debugger.timeout", 15); // NOI18N
        Util.finest("Using timeout: " + timeout + 's'); // NOI18N
        String interpreter = platform.getInterpreter();
        boolean forceRubyDebug = Boolean.getBoolean("org.netbeans.modules.ruby.debugger.force.rdebug");
        if (!forceRubyDebug && (jrubySet || prefs.isUseClassicDebugger(platform))) {
            Util.LOGGER.fine("Running classic(slow) debugger...");
            proxy = RubyDebuggerFactory.startClassicDebugger(debugDesc,
                    PATH_TO_CLASSIC_DEBUG_DIR, interpreter, timeout);
        } else { // ruby-debug
            Util.LOGGER.fine("Running fast debugger...");
            File rDebugF = new File(Util.findRDebugExecutable(platform));
            proxy = RubyDebuggerFactory.startRubyDebug(debugDesc,
                    rDebugF.getAbsolutePath(), interpreter, timeout);
        }
        
        intializeIDEDebuggerEngine(proxy, descriptor.getFileLocator());
        proxy.startDebugging(RubyBreakpointManager.getBreakpoints());
        return proxy.getDebugTarged().getProcess();
    }

    private static Map<String, String> getJRubyEnvironment(final ExecutionDescriptor descriptor) {
        Map<String, String> env = new HashMap<String, String>();
        env.put("JAVA_HOME", RubyExecution.getJavaHome()); // NOI18N
        if (descriptor.getClassPath() != null) {
            env.put("CLASSPATH", descriptor.getClassPath()); // NOI18N
        }
        return env;
    }

    /** Package private for unit test. */
    static boolean checkAndTuneSettings(final ExecutionDescriptor descriptor) {
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        final RubyPlatform platform = descriptor.getPlatform();

        boolean jrubySet = platform.isJRuby();
        
        // TODO stop to support the property when jruby-debug is available
        boolean fastDebuggerRequired = descriptor.isFastDebugRequired() 
                && !Boolean.getBoolean("org.netbeans.modules.ruby.debugger.fast.not.required");

        // See issue #114183
        if (!jrubySet && prefs.isFirstTime()) {
            prefs.setFirstTime(false);
            Util.offerToInstallFastDebugger(platform);
        }
        
        // JRuby vs. ruby-debug-ide
        if (jrubySet) {
            if (fastDebuggerRequired) {
                Util.showMessage(getMessage("RubyDebugger.jruby.cannot.be.used"));
                return false;
            }
            if (!prefs.isUseClassicDebugger(platform) && !shouldContinueWithCD(platform)) {
                return false;
            }
        }
        
        if (fastDebuggerRequired && prefs.isUseClassicDebugger(platform)
                && !Util.ensureRubyDebuggerIsPresent(platform, true, "RubyDebugger.wrong.fast.debugger.required")) {
            return false;
        }

        if (!jrubySet && !platform.hasFastDebuggerInstalled() && !Util.offerToInstallFastDebugger(platform)) {
            // user really wants classic debugger, ensure it
            prefs.setUseClassicDebugger(platform, true);
        }

        if (jrubySet || prefs.isUseClassicDebugger(platform)) {
            if (!platform.isValidRuby(true)) {
                return false;
            }
        } else { // ruby-debug
            if (!Util.ensureRubyDebuggerIsPresent(platform, true, "RubyDebugger.requiredMessage")) { // NOI18N
                return false;
            }
            String rDebugPath = Util.findRDebugExecutable(platform);
            if (rDebugPath == null) {
                Util.showMessage(NbBundle.getMessage(RubyDebugger.class,
                        "RubyDebugger.wrong.rdebug-ide", // NOI18N
                        platform.getInterpreter(), 
                        Util.rdebugPattern()));
                return false;
            }
        }
        return true;
    }
    
    private static boolean shouldContinueWithCD(final RubyPlatform platform) {
        Confirmation confirmation = new Confirmation(getMessage("RubyDebugger.jruby.vs.fast.debugger"),
                Confirmation.OK_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(confirmation);
        boolean continueWithCD = confirmation.getValue() != Confirmation.CANCEL_OPTION;
        if (continueWithCD) {
            DebuggerPreferences.getInstance().setUseClassicDebugger(platform, true);
        }
        return continueWithCD;
    }
    
    private static void intializeIDEDebuggerEngine(final RubyDebuggerProxy proxy, final FileLocator fileLocator) {
        RubySession rubySession = new RubySession(proxy, fileLocator);
        SessionProvider sp = rubySession.createSessionProvider();
        DebuggerInfo di = DebuggerInfo.create(
                "RubyDebuggerInfo", new Object[] { sp, rubySession }); // NOI18N
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        DebuggerEngine[] es = dm.startDebugging(di);
        
        RubyDebuggerActionProvider provider =
                (RubyDebuggerActionProvider) es[0].lookupFirst(null, RubyDebuggerActionProvider.class);
        assert provider != null;
        proxy.addRubyDebugEventListener(provider);
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getMessage(RubyDebugger.class, key);
    }
    
}
