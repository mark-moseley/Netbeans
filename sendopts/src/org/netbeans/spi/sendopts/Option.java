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

package org.netbeans.spi.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.modules.sendopts.OptionImpl;
import org.openide.util.Lookup;

/** Represents possible option that can appear on {@link org.netbeans.api.sendopts.CommandLine}
 * and contains factory methods to create them.
 * <p>
 * An option can have letter short version, long name. It can
 * accept arguments, one argument or an array of additional arguments. 
 *
 * @author Jaroslav Tulach
 */
public final class Option {
    /** character of single command of (char)-1
     */
    private final int shortName;
    /** long name or null */
    private final String longName;
    /** implementation of this option */
    final OptionImpl impl;
    /** bundle with message*/
    private final String[] keys;
    private final String[] bundles;

    /** Constant that represents no short name indicator.
     */
    public static final char NO_SHORT_NAME = (char)-1;
    
    private static String[] EMPTY = new String[2];
    
    /** Use factory method */
    private Option(char shortName, String longName, int type) {
        this.shortName = shortName == NO_SHORT_NAME ? -1 : (int)shortName;
        this.longName = longName;
        switch (type) {
            case 0: this.impl = OptionImpl.createNoArg(this); break;
            case 1: this.impl = OptionImpl.createOneArg(this, false); break;
            case 2: this.impl = OptionImpl.createOneArg(this, true); break;
            case 3: this.impl = OptionImpl.createAdd(this, false); break;
            case 4: this.impl = OptionImpl.createAdd(this, true); break;
            case 5: this.impl = OptionImpl.createAlways(this); break;
            default: throw new IllegalArgumentException("Type: " + type); // NOI18N
        }
        this.keys = EMPTY;
        this.bundles = EMPTY;
    }

    /** Complex option */
    Option(int type, Option[] arr) {
        this.shortName = -1;
        this.longName = null;
        this.impl = OptionImpl.create(this, type, Arrays.asList(arr));
        this.keys = EMPTY;
        this.bundles = EMPTY;
    }
    
    /** clone with some description
     */
    private Option(Option old, int typeOfDescription, String bundle, String description) {
        this.shortName = old.shortName;
        this.longName = old.longName;
        this.impl = OptionImpl.cloneImpl(old.impl, this, null);
        this.keys = (String[])old.keys.clone();
        this.bundles = (String[])old.bundles.clone();
        
        this.keys[typeOfDescription] = description;
        this.bundles[typeOfDescription] = bundle;
        
    }
    
    /** Programmatic textual representation of the option. Format is subject to change 
     * in future.
     * @return textual description of the option
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this))); // NOI18N
        sb.append('[');
        sb.append(shortName);
        sb.append(',');
        sb.append(longName);
        sb.append(',');
        impl.append(sb);
        sb.append(']');
        return sb.toString();
    }
    
    /** Options with the same functionality, regardless of their descriptions
     * {@link Option#shortDescription} and {@link Option#displayName} are always the same.
     */
    public boolean equals(Object o) {
        if (o instanceof Option) {
            Option option = (Option)o;
            return impl.root == option.impl.root;
        }
        return false;
    }
    
    public int hashCode() {
        return System.identityHashCode(impl.root);
    }

    /** Factory method that creates an option without any arguments.
     * For example to create an option that handles <code>--help</code> or
     * <code>-h</code> one can create it using:<pre>
     * Option helpOption = Option.withoutArgument('h', "help");</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   protected void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     if (values.containsKey(helpOption)) {
     *       printHelp(env.getErrorStream());
     *     }
     *   }</pre>
     * The <code>values.get(helpOption)</code> is always <code>null</code> to signal
     * that this options does not have any associated value.
     *
     * @param shortName character code or {@link Option#NO_SHORT_NAME}
     * @param longName long name or null
     * @return option representing the created definition
     */
    public static Option withoutArgument(char shortName, String longName) {
        return new Option(shortName, longName, 0);
    }

    /** Factory method for option that may, but does not need to have an argument.
     * For example to have option that increments by one or by specified number
     * one could write:<pre>
     * Option incrementOption = Option.optionalArgument('i', "increment");</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   public void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     if (values.containsKey(incrementOption)) {
     *       String[] inc = values.get(incrementOption);
     *       int increment = inc == null ? 1 : Integer.parseInt(inc[0]);
     *       // do what is necessary
     *     }
     *   }</pre>
     * The <code>values</code> map always contains the <code>incrementOption</code>
     * if it appeared on the command line. If it had associated value, then
     * the <code>map.get(incrementOption)</code> returns array of length one,
     * with item on position 0 being the value of the option. However if the
     * option appeared without argument, then the value associated with the
     * option is <code>null</code>.
     * <p>
     * If registered into to system using {@link OptionProcessor} then users could
     * use command lines like <code>-i=5</code> or <code>--increment=5</code> to
     * increase the value by five or just <code>-i</code> and <code>--increment</code>
     * to increment by default - e.g. one.
     *
     * @param shortName the character to be used as a shortname or {@link Option#NO_SHORT_NAME}
     * @param longName the long name or null
     */
    public static Option optionalArgument(char shortName, String longName) {
        return new Option(shortName, longName, 1);
    }

    /** Factory method for option has to be followed by one argument.
     * For example to have option that opens a file
     * one could write:<pre>
     * Option openOption = Option.optionalArgument('o', "open");</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   public void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     if (values.containsKey(openOption)) {
     *       String fileName = values.get(openOption)[0];
     *       File file = new File({@link Env#getCurrentDirectory}, fileName);
     *       // do what is necessary
     *     }
     *   }</pre>
     * The <code>values</code> map always contains the <code>openOption</code>
     * if it appeared on the command line. Its value is then always string
     * array of length one and its 0 element contains the argument for the 
     * option.
     * <p>
     * If registered into to system using {@link OptionProcessor} then users could
     * use command lines like <code>-oX.java</code> or <code>--open Y.java</code> to
     * invoke the open functionality.
     *
     * @param shortName the character to be used as a shortname or {@link Option#NO_SHORT_NAME}
     * @param longName the long name or null
     */
    public static Option requiredArgument(char shortName, String longName) {
        return new Option(shortName, longName, 2);
    }

    /** Creates an option that can accept <q>additional arguments</q>.
     * For example to have option that opens few files
     * one could write:<pre>
     * Option openOption = Option.additionalArguments('o', "open");</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   public void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     if (values.containsKey(openOption)) {
     *       for (String fileName : values.get(openOption)) {
     *         File file = new File({@link Env#getCurrentDirectory}, fileName);
     *         // do what is necessary
     *       }
     *     }
     *   }</pre>
     * The <code>values</code> map always contains the <code>openOption</code>
     * if it appeared on the command line. Its value is then always string
     * array of arbitrary length containing all elements on the command line
     * that were not recognised as options (or their arguments). 
     * For example line <pre>
     * X.java -o Y.java Z.txt</pre>
     * will invoke the {@link OptionProcessor} with 
     * <code>{ "X.java", "Y.java", "Z.txt" }</code>.
     * <p>
     * Obviously only one such {@link Option#additionalArguments} can be
     * used at once on a command line. If there was not only the <q>open</q>
     * but also <q>edit</q> option 
     * taking the additional arguments, 
     * then command line like: <pre>
     * --edit X.java --open Y.java Z.txt</pre>
     * would be rejected. 
     *
     * @param shortName the character to be used as a shortname or {@link Option#NO_SHORT_NAME}
     * @param longName the long name or null
     */
    public static Option additionalArguments(char shortName, String longName) {
        return new Option(shortName, longName, 3);
    }
    /** Creates a default option that accepts <q>additional arguments</q>
     * not claimed by any other option. 
     * For example to have option that opens few files
     * one could write:<pre>
     * Option openOption = Option.defaultArguments();</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   public void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     if (values.containsKey(openOption)) {
     *       for (fileName : values.get(openOption)) {
     *         File file = new File({@link Env#getCurrentDirectory}, fileName);
     *         // do what is necessary
     *       }
     *     }
     *   }</pre>
     * The <code>values</code> map always contains the <code>openOption</code>
     * if there were some arguments on the command line that were not parsed
     * by any other option. Its value is then always string
     * array of arbitrary length containing all elements on the command line
     * that were not recognised as options (or their arguments). 
     * For example line <pre>
     * X.java Y.java Z.txt</pre>
     * will invoke the {@link OptionProcessor} with 
     * <code>{ "X.java", "Y.java", "Z.txt" }</code>.
     * <p>
     * Obviously only one such {@link Option#defaultArguments} can defined.
     * If there are two, then an error is reported when one tries to parse
     * any command line with arguments not claimed by any other option. 
     * That is why it is always good idea to not define just {@link Option#defaultArguments}
     * option, but also appropriate {@link Option#additionalArguments} one:<pre>
     * Option openOption1 = Option.defaultArguments();
     * Option openOption2 = Option.additionalArguments('o', "open");</pre> 
     * and handle both of them in the {@link OptionProcessor}. Then if the
     * command line: <pre>
     * X.java Y.java Z.txt</pre> is rejected due to ambiguities one can use <pre>
     * X.java Y.java --open Z.txt</pre> to invoke the same functionality.
     */
    public static Option defaultArguments() {
        return new Option(NO_SHORT_NAME, null, 4);
    }

    /** Creates an option that is always present. This can be useful for
     * processors that want to be notified everytime the command line
     * is successfuly parsed. 
     * 
     * Option always = Option.always();</pre> 
     * and inside of the {@link OptionProcessor} declaring this
     * option use:<pre>
     *   public void process(Env env, Map&lt;Option,String[]&gt; values) throws CommandException {
     *     assert values.contains(always);
     *   }</pre>
     * 
     * @return the option that always matches correct command line
     * @since 2.1
     */
    public static Option always() {
        return new Option(NO_SHORT_NAME, null, 5);
    }

    /** Associates a name with given option. By default 
     * the option display name is generated by the infrastructure from the
     * short and long name plus generic description of options arguments, this
     * method allows to completely replace the default behaviour.
     *
     * @param option the option to add description for
     * @param bundleName name of a bundle to create
     * @param key the bundle key to get the message from
     * @return option with same behaviour as the old one plus with associated display name
     */
    public static  Option displayName(Option option, String bundleName, String key) {
        return new Option(option, 0, bundleName, key);
    }
    
    /** Associates a short textual description with given option. This message
     * is going to be printed during {@link org.netbeans.api.sendopts.CommandLine#usage} next to the
     * option name. Usually should be one liner comment.
     *
     * @param option the option to add description for
     * @param bundleName name of a bundle to create
     * @param key the bundle key to get the message from
     * @return option with same behaviour as the old one plus with associated short description message
     */
    public static  Option shortDescription(Option option, String bundleName, String key) {
        return new Option(option, 1, bundleName, key);
    }
    
    
    static {
        OptionImpl.Trampoline.DEFAULT = new OptionImpl.Trampoline() {
            public OptionImpl impl(Option o) {
                return o.impl;
            }
            public Env create(InputStream is, OutputStream os, OutputStream err, File currentDir) {
                return new Env(is, os, err, currentDir);
            }

            public void usage(PrintWriter w, Option o, int max) {
                if (o.keys[1] != null) {
                    w.print(key(o.bundles[1], o.keys[1], Locale.getDefault()));
                }
            }
            public Option[] getOptions(OptionProcessor p) {
                return p.getOptions().toArray(new Option[0]);
            }
            public void process(OptionProcessor provider, Env env, Map<Option,String[]> options) throws CommandException {
                provider.process(env, Collections.unmodifiableMap(options));
            }
            public String getLongName(Option o) {
                return o.longName;
            }
            public int getShortName(Option o) {
                return o.shortName;
            }
            public String getDisplayName(Option o, Locale l) {
                return key(o.bundles[0], o.keys[0], l);
            }
            private String key(String bundle, String key, Locale l) {
                if (key == null) {
                    return null;
                }
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                if (loader == null) {
                    loader = Thread.currentThread().getContextClassLoader();
                }
                if (loader == null) {
                    loader = getClass().getClassLoader();
                }
                try {
                    ResourceBundle b = ResourceBundle.getBundle(bundle, l, loader);
                    return b.getString(key);
                } catch (MissingResourceException ex) {
                    OptionImpl.LOG.log(Level.WARNING, null, ex);
                    return key;
                }
                
            }
        };
    }
}
