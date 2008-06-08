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

package org.netbeans.modules.extexecution.api.print;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.extexecution.print.FindFileListener;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Factory methods for {@link LineConvertor} classes.
 *
 * @author Petr Hejl
 */
public final class LineConvertors {

    private static final Logger LOGGER = Logger.getLogger(LineConvertors.class.getName());

    private LineConvertors() {
        super();
    }

    public static LineConvertor filePattern(LineConvertor chain, FileLocator fileLocator,
            Pattern linePattern, Pattern extPattern) {

        return new FilePatternConvertor(chain, fileLocator, linePattern, extPattern);
    }

    public static LineConvertor filePattern(LineConvertor chain, FileLocator fileLocator,
            Pattern linePattern, Pattern extPattern, int fileGroup, int lineGroup) {

        return new FilePatternConvertor(chain, fileLocator, linePattern, extPattern, fileGroup, lineGroup);
    }

    /**
     * Returns the convertor parsing the line and searching for
     * <code>http</code> or <code>https</code> URL.
     * <p>
     * Converted line returned from the processor consist from the original
     * line and listener opening browser with recognized url on click.
     * <p>
     * If line is not recognized as <code>http</code> or <code>https</code>
     * URL it is passed to the given chaining convertor. If chaining convertor
     * does not exist only the line containing the original text is returned.
     *
     * @param chain the converter to which the line will be passed when it is
     *             not recognized as URL; may be <code>null</code>
     * @return the convertor parsing the line and searching for
     *             <code>http</code> or <code>https</code> URL
     */
    public static LineConvertor httpUrl(LineConvertor chain) {
        return new HttpUrlConvertor(chain);
    }

    /**
     * Locates the file for the given path, file or part of the path.
     *
     * @see LineConvertors#filePattern(org.netbeans.modules.extexecution.api.print.LineConvertor, org.netbeans.modules.extexecution.api.print.LineConvertors.FileLocator, java.util.regex.Pattern, java.util.regex.Pattern)
     * @see LineConvertors#filePattern(org.netbeans.modules.extexecution.api.print.LineConvertor, org.netbeans.modules.extexecution.api.print.LineConvertors.FileLocator, java.util.regex.Pattern, java.util.regex.Pattern, int, int)
     */
    public interface FileLocator {

        /**
         * Returns the file corresponding to the filename (or path) or
         * <code>null</code> if locater can't find the file.
         *
         * @param filename name of the file
         * @return the file corresponding to the filename (or path) or
         *             <code>null</code> if locater can't find the file
         */
        FileObject find(String filename);

    }

    private static List<ConvertedLine> chain(LineConvertor chain, String line) {
        if (chain != null) {
            return chain.convert(line);
        }
        return Collections.<ConvertedLine>singletonList(new SimpleConvertedLine(line));
    }

    private static class FilePatternConvertor implements LineConvertor {

        private final LineConvertor chain;

        private final FileLocator locator;

        private final Pattern linePattern;

        private final Pattern extPattern;

        private final int fileGroup;

        private final int lineGroup;

        public FilePatternConvertor(LineConvertor chain, FileLocator locator,
                Pattern linePattern, Pattern extPattern) {
            this(chain, locator, linePattern, extPattern, -1, -1);
        }

        public FilePatternConvertor(LineConvertor chain, FileLocator locator,
                Pattern linePattern, Pattern extPattern, int fileGroup, int lineGroup) {

            this.chain = chain;
            this.locator = locator;
            this.linePattern = linePattern;
            this.fileGroup = fileGroup;
            this.lineGroup = lineGroup;
            this.extPattern = extPattern;
        }

        public List<ConvertedLine> convert(final String line) {
            // Don't try to match lines that are too long - the java.util.regex library
            // throws stack exceptions (101234)
            if (line.length() > 400) {
                return chain(chain, line);
            }

            Matcher match = linePattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup >= 0) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (extPattern != null && !(extPattern.matcher(file).matches() || new File(file).isFile())) {
                        return chain(chain, line);
                    }
                }

                if (lineGroup >= 0) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        LOGGER.log(Level.INFO, null, nfe);
                        lineno = 0;
                    }
                }

                return Collections.<ConvertedLine>singletonList(
                        new SimpleConvertedLine(line, new FindFileListener(file, lineno, locator)));
            }

            return chain(chain, line);
        }
    }

    private static class HttpUrlConvertor implements LineConvertor {

        private final LineConvertor chain;

        private final Pattern pattern = Pattern.compile(".*(((http)|(https))://\\S+)(\\s.*|$)"); // NOI18N

        public HttpUrlConvertor(LineConvertor chain) {
            this.chain = chain;
        }

        public List<ConvertedLine> convert(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String stringUrl = matcher.group(1);
                try {
                    final URL url = new URL(stringUrl);

                    return Collections.<ConvertedLine>singletonList(new SimpleConvertedLine(line, new OutputListener() {

                        public void outputLineSelected(OutputEvent ev) {
                        }

                        public void outputLineAction(OutputEvent ev) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        }

                        public void outputLineCleared(OutputEvent ev) {
                        }
                    }));
                } catch (MalformedURLException ex) {
                    // retur chain
                }
            }

            return chain(chain, line);
        }

    }

    private static class SimpleConvertedLine implements ConvertedLine {

        private final String text;

        private final OutputListener listner;

        public SimpleConvertedLine(String text) {
            this(text, null);
        }

        public SimpleConvertedLine(String text, OutputListener listner) {
            this.text = text;
            this.listner = listner;
        }

        public OutputListener getListener() {
            return listner;
        }

        public String getText() {
            return text;
        }

    }
}
