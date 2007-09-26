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

package org.netbeans.api.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A class for clients that have an array of strings and want to process
 * it - e.g. parse it and also invoke registered {@link OptionProcessor}s.
 *
 * @author Jaroslav Tulach
 */
public final class CommandLine {
    /** internal errors of CommandLine start here and end here + 100 */
    private static final int ERROR_BASE = 50345;
    
    /** Use factory methods to create the line. */
    CommandLine() {
    }
    
    /** Getter for the default command line processor in the system. List
     * of {@link OptionProcessor}s is taken from default 
     * <a href="@org-openide-util@/org/openide/util/Lookup.html">Lookup.getDefault</code>.
     */
    public static CommandLine getDefault() {
        return new CommandLine();
    }
    
    /** Process the array of arguments and invoke associated {@link OptionProcessor}s.
     * 
     * @param args the array of strings to process
     * @exception CommandException if processing is not possible or failed
     */
    public void process(String[] args) throws CommandException {
        process(args, null, null, null, null);
    }
    
    /** Process the array of arguments and invoke associated {@link OptionProcessor}s.
     * 
     * @param args the array of strings to process
     * @param is the input stream that processors can read
     * @param os the output stream that processors can write to
     * @param err the output stream that processors can send error messages to
     * @param currentDir directory that processors should use as current user dir
     * @exception CommandException if processing is not possible or failed
     */
    public void process(String[] args, InputStream is, OutputStream os, OutputStream err, File currentDir) throws CommandException {
        if (is == null) {
            is = System.in;
        }
        if (os == null) {
            os = System.out;
        }
        if (err == null) {
            err = System.err;
        }
        if (currentDir == null) {
            currentDir = new File(System.getProperty("user.dir")); // NOI18N
        }
        Env env = OptionImpl.Trampoline.DEFAULT.create(is, os, err, currentDir);
        
        
        ArrayList<String> additionalParams = new ArrayList<String>();
        ArrayList<OptionImpl> opts = new ArrayList<OptionImpl>();
        OptionImpl acceptsAdons = null;
        
        OptionImpl[] mainOptions = getOptions();
        LinkedHashSet<OptionImpl> allOptions = new LinkedHashSet<OptionImpl>();
        for (int i = 0; i < mainOptions.length; i++) {
            mainOptions[i] = mainOptions[i].addWorkingCopy(allOptions);
        }
        OptionImpl[] arr = allOptions.toArray(new OptionImpl[0]);
        
        boolean optionMode = true;
        ARGS: for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue ARGS;
            }

            if (optionMode) {
                if (args[i].startsWith("--")) {
                    if (args[i].length() == 2) {
                        optionMode = false;
                        continue ARGS;
                    }

                    String text = args[i].substring(2);
                    String value = null;
                    int textEqual = text.indexOf('=');
                    if (textEqual >= 0) {
                        // strip the name of the option
                        value = text.substring(textEqual + 1);
                        text = text.substring(0, textEqual);
                    }
                    OptionImpl opt = findByLongName (text, arr);
                    if (opt == null) {
                        throw new CommandException(args[i], ERROR_BASE + 1);
                    }
                    if (opt.getArgumentType() == 1 && value == null) {
                        // read next value from the argument
                        for(;;) {
                            if (++i == args.length) {
                                throw new CommandException(NbBundle.getMessage(CommandLine.class, "MSG_MissingArgument", "--" + opt.getLongName()), ERROR_BASE + 2); // NOI18N
                            }
                            
                            if (args[i].equals("--")) {
                                optionMode = false;
                                continue;
                            }
                            
                            if (optionMode && args[i].startsWith("-")) {
                                throw new CommandException(NbBundle.getMessage(CommandLine.class, "MSG_MissingArgument", "--" + opt.getLongName()), ERROR_BASE + 2); // NOI18N
                            }

                            break;
                        }
                        
                        
                        
                        value = args[i];
                    }


                    if (value != null) {
                        if (opt.getArgumentType() != 1 && opt.getArgumentType() != 2) {
                            throw new CommandException("Option " + opt + " cannot have value " + value, ERROR_BASE + 2);
                        }

                        opt.associateValue(value);
                    }

                    if (opt.getArgumentType() == 3) {
                        if (acceptsAdons != null) {
                            String oName1 = findOptionName(acceptsAdons, args);
                            String oName2 = findOptionName(opt, args);
                            String msg = NbBundle.getMessage(CommandLine.class, "MSG_CannotTogether", oName1, oName2); // NOI18N
                            throw new CommandException(msg, ERROR_BASE + 3);
                        }
                        acceptsAdons = opt;
                    }

                    opts.add(opt);
                    continue ARGS;
                } else if (args[i].startsWith("-") && args[i].length() > 1) {
                    for (int j = 1; j < args[i].length(); j++) {
                        char ch = args[i].charAt(j);
                        OptionImpl opt = findByShortName(ch, arr);
                        if (opt == null) {
                            throw new CommandException("Unknown option " + args[i], ERROR_BASE + 1);
                        }
                        if (args[i].length() == j + 1 && opt.getArgumentType() == 1) {
                            throw new CommandException(NbBundle.getMessage(CommandLine.class, "MSG_MissingArgument", args[i]), ERROR_BASE + 2);
                        }

                        if (args[i].length() > j && (opt.getArgumentType() == 1 || opt.getArgumentType() == 2)) {
                            opt.associateValue(args[i].substring(j + 1));
                            j = args[i].length();
                        }
                        if (opt.getArgumentType() == 3) {
                            if (acceptsAdons != null) {
                                String oName1 = findOptionName(acceptsAdons, args);
                                String oName2 = findOptionName(opt, args);
                                String msg = NbBundle.getMessage(CommandLine.class, "MSG_CannotTogether", oName1, oName2); // NOI18N
                                throw new CommandException(msg, ERROR_BASE + 3);
                            }
                            acceptsAdons = opt;
                        }
                        opts.add(opt);
                    }
                    continue ARGS;
                }
            }
            
            additionalParams.add(args[i]);
        }
        
        if (acceptsAdons == null && !additionalParams.isEmpty()) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].getArgumentType() == 4) {
                    if (acceptsAdons != null) {
                        throw new CommandException("There cannot be two default options: " + acceptsAdons + " and " + arr[i], ERROR_BASE + 3);
                    }
                    acceptsAdons = arr[i];
                    opts.add(acceptsAdons);
                }
            }
            if (acceptsAdons == null) {
                throw new CommandException("There are params but noone wants to proces them: " + additionalParams, ERROR_BASE + 2);
            }
            
        }
        
        OptionImpl.Appearance[] postProcess = new OptionImpl.Appearance[mainOptions.length];
        {
            HashSet<OptionImpl> used = new HashSet<OptionImpl>(opts);
            for (int i = 0; i < mainOptions.length; i++) {
                OptionImpl.Appearance res = mainOptions[i].checkConsistent(used);
                postProcess[i] = res;
                if (res.isThere()) {
                    mainOptions[i].markConsistent(res);
                }
/*                
                if (res.isError()) {
                    throw new CommandException(res.errorMessage(args), ERROR_BASE + 4);
                }
 */
            }
        }
        
        
        {
            HashSet<OptionImpl> used = new HashSet<OptionImpl>(opts);
            for (int i = 0; i < mainOptions.length; i++) {
                if (postProcess[i].isError()) {
                    OptionImpl error = mainOptions[i].findNotUsedOption(used);
                    if (error != null) {
                        throw new CommandException(postProcess[i].errorMessage(args), ERROR_BASE + 4);    
                    }
                }
            }
        }

        Map<OptionProcessor,Map<Option,String[]>> providers = new LinkedHashMap<OptionProcessor,Map<Option,String[]>>();
        {
            for (int i = 0; i < mainOptions.length; i++) {
                if (postProcess[i].isThere()) {
                    Map<Option,String[]> param = providers.get(mainOptions[i].getProvider());
                    if (param == null) {
                        param = new HashMap<Option,String[]>();
                        providers.put(mainOptions[i].getProvider(), param);
                    }
                    mainOptions[i].process(additionalParams.toArray(new String[0]), param);
                }
            }
        }
        
        for (Map.Entry<OptionProcessor, Map<Option, String[]>> pair : providers.entrySet()) {
            OptionImpl.Trampoline.DEFAULT.process(pair.getKey(), env, pair.getValue());
        }
    }

    /** Prints the usage information about options provided by associated
     * {@link OptionProcessor}s.
     *
     * @param w the writer to output usage info to
     * @since 1.7
     */
    public void usage(PrintWriter w) {
        OptionImpl[] mainOptions = getOptions();
        LinkedHashSet<OptionImpl> allOptions = new LinkedHashSet<OptionImpl>();
        for (int i = 0; i < mainOptions.length; i++) {
            mainOptions[i].addWorkingCopy(allOptions);
        }
        OptionImpl[] arr = allOptions.toArray(new OptionImpl[0]);

        int max = 25;
        String[] prefixes = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            StringBuffer sb = new StringBuffer();
            
            String ownDisplay = OptionImpl.Trampoline.DEFAULT.getDisplayName(arr[i].getOption(), Locale.getDefault());
            if (ownDisplay != null) {
                sb.append(ownDisplay);
            } else {
                String sep = "";
                if (arr[i].getShortName() != -1) {
                    sb.append('-');
                    sb.append((char)arr[i].getShortName());
                    sep = ", ";
                }
                if (arr[i].getLongName() != null) {
                    sb.append(sep);
                    sb.append("--"); // NOI18N
                    sb.append(arr[i].getLongName());
                } else {
                    if (sep.length() == 0) {
                        continue;
                    }
                }

                switch (arr[i].getArgumentType()) {
                    case 0: break;
                    case 1:
                        sb.append(' ');
                        sb.append(NbBundle.getMessage(CommandLine.class, "MSG_OneArg")); // NOI18N
                        break;
                    case 2:
                        sb.append(' ');
                        sb.append(NbBundle.getMessage(CommandLine.class, "MSG_OptionalArg")); // NOI18N
                        break;
                    case 3:
                        sb.append(' ');
                        sb.append(NbBundle.getMessage(CommandLine.class, "MSG_AddionalArgs")); // NOI18N
                        break;
                    default:
                        assert false;
                }
            }

            if (sb.length() > max) {
                max = sb.length();
            }

            prefixes[i] = sb.toString();
        }

        for (int i = 0; i < arr.length; i++) {
            if (prefixes[i] != null) {
                w.print("  "); // NOI18N
                w.print(prefixes[i]);
                for (int j = prefixes[i].length(); j < max; j++) {
                    w.print(' ');
                }
                w.print(' ');
                arr[i].usage(w, max);
                w.println();
            }
        }

        w.flush();
    }

    private OptionImpl[] getOptions() {
        ArrayList<OptionImpl> arr = new ArrayList<OptionImpl>();
        
        for (OptionProcessor p : Lookup.getDefault().lookupAll(OptionProcessor.class)) {
            org.netbeans.spi.sendopts.Option[] all = OptionImpl.Trampoline.DEFAULT.getOptions(p);
            for (int i = 0; i < all.length; i++) {
                arr.add(OptionImpl.cloneImpl(OptionImpl.find(all[i]), all[i], p));
            }
        }
        
        return arr.toArray(new OptionImpl[0]);
    }
    
    private OptionImpl findByLongName(String lng, OptionImpl[] arr) {
        boolean abbrev = false;
        OptionImpl best = null;
        for (int i = 0; i < arr.length; i++) {
            String on = arr[i].getLongName();
            if (on == null) {
                continue;
            }
            if (lng.equals(on)) {
                return arr[i];
            }
            if (on.startsWith(lng)) {
                abbrev = best == null;
                best = arr[i];
            }
        }
        
        return abbrev ? best : null;
    }
    
    private OptionImpl findByShortName(char ch, OptionImpl[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (ch == arr[i].getShortName()) {
                return arr[i];
            }
        }
        return null;
    }

    private static String findOptionName(OptionImpl opt, String[] args) {
        for(int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                continue;
            }
            
            if (args[i].startsWith("--")) {
                String text = args[i].substring(2);
                int textEqual = text.indexOf('=');
                if (textEqual >= 0) {
                    // strip the name of the option
                    text = text.substring(0, textEqual);
                }
                if (text.startsWith(opt.getLongName())) {
                    return args[i];
                }
            } else {
                if (opt.getShortName() == args[i].charAt(1)) {
                    return "-" + (char)opt.getShortName();
                }
            }
        }
        
        return opt.toString();
    }

}
