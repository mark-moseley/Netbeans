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

package org.netbeans.modules.languages.yaml;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.jruby.util.ByteList;
import org.jvyamlb.Composer;
import org.jvyamlb.PositioningComposerImpl;
import org.jvyamlb.PositioningParserImpl;
import org.jvyamlb.PositioningScannerImpl;
import org.jvyamlb.ResolverImpl;
import org.jvyamlb.exceptions.PositionedParserException;
import org.jvyamlb.nodes.Node;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.util.NbBundle;

/**
 * Parser for YAML. Delegates to the YAML parser shipped with JRuby (jvyamlb)
 * @author Tor Norbye
 */
public class YamlParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(YamlParser.class.getName());
    /**
     * The max length for files we will try to parse (to avoid OOMEs).
     */
    private static final int MAX_LENGTH = 512*1024;

    private YamlParserResult lastResult;

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // FIXME parsing API
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // FIXME parsing API
    }
    
    @Override
    public void cancel() {
        // FIXME parsing API
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        assert lastResult != null : "getResult() called prior parse()"; //NOI18N
        return lastResult;
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

    private boolean isTooLarge(String source) {
        return source.length() > MAX_LENGTH;
    }

    private YamlParserResult resultForTooLargeFile(Snapshot snapshot) {
        YamlParserResult result = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false, null, null);
        // FIXME this can violate contract of DefaultError (null fo)
        DefaultError error = new DefaultError(null, NbBundle.getMessage(YamlParser.class, "TooLarge"), null,
                snapshot.getSource().getFileObject(), 0, 0, Severity.WARNING);
        result.addError(error);
        return result;
    }

    // for test package private
    YamlParserResult parse(String source, Snapshot snapshot) {
        try {
            if (isTooLarge(source)) {
                return resultForTooLargeFile(snapshot);
            }
            ByteList byteList = null;
            int[] byteToUtf8 = null;
            int[] utf8toByte = null;

            byte[] bytes = source.getBytes("UTF-8"); // NOI18N
            if (bytes.length == source.length()) {
                // No position translations necessary - this should be fast
                byteList = new ByteList(bytes);
            } else {
                // There's some encoding happening of unicode characters.
                // I need to produce functions to translate between a byte offset
                // and a unicode offset.
                // I couldn't find an API for this in the various Charset functions.
                // So for now, here is a fantastically lame but functional way to do it:
                // I'm encoding the string, one character at a time, flushing after
                // each operation to compute the current byte offset. I then build
                // up an array of these offsets such that I can do quick translations.
                ByteArrayOutputStream out = new ByteArrayOutputStream(2*source.length());
                OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8"); // NOI18N
                utf8toByte = new int[source.length()];
                int currentPos = 0;
                for (int i = 0, n = source.length(); i < n; i++) {
                    writer.write(source.charAt(i));
                    writer.flush(); // flush because otherwise we don't know the correct offset
                    utf8toByte[i] = currentPos;
                    currentPos = out.size();
                }

                if (currentPos > 0) {
                    byteToUtf8 = new int[currentPos];
                    for (int i = 0, n = utf8toByte.length; i < n; i++) {
                        byteToUtf8[utf8toByte[i]] = i;
                    }
                    // Fill in holes - these are the middles of unicode encodings.
                    int last = 0;
                    for (int i = 0, n = byteToUtf8.length; i < n; i++) {
                        int p = byteToUtf8[i];
                        if (p == 0) {
                            byteToUtf8[i] = last;
                        } else {
                            last = p;
                        }
                    }
                } else {
                    byteToUtf8 = new int[0];
                }

                byteList = new ByteList(out.toByteArray());
            }

            Composer composer = new PositioningComposerImpl(new PositioningParserImpl(new PositioningScannerImpl(byteList)), new ResolverImpl());
            List<Node> nodes = new ArrayList<Node>();
            Iterator iterator = composer.eachNode();
            while (iterator.hasNext()) {
                Node node = (Node) iterator.next();
                if (node == null) {
                    break;
                }
                nodes.add(node);
            }

            //Object yaml = YAML.load(stream);
            return new YamlParserResult(nodes, this, snapshot, true, byteToUtf8, utf8toByte);
        } catch (Exception ex) {
            int pos = 0;
            if (ex instanceof PositionedParserException) {
                PositionedParserException ppe = (PositionedParserException)ex;
                pos = ppe.getPosition().offset;
            }

            YamlParserResult result = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false, null, null);
            String message = ex.getMessage();
            if (message != null && message.length() > 0) {
                // Strip off useless prefixes to make errors more readable
                if (message.startsWith("ScannerException null ")) { // NOI18N
                    message = message.substring(22);
                } else if (message.startsWith("ParserException ")) { // NOI18N
                    message = message.substring(16);
                }
                // Capitalize sentences
                char firstChar = message.charAt(0);
                char upcasedChar = Character.toUpperCase(firstChar);
                if (firstChar != upcasedChar) {
                    message = upcasedChar + message.substring(1);
                }

                // FIXME this can violate contract of DefaultError (null fo)
                DefaultError error = new DefaultError(null, message, null, snapshot.getSource().getFileObject(),
                        pos, pos, Severity.ERROR);
                result.addError(error);
            }
            
            return result;
        }
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {

        String source = asString(snapshot.getText());
        if (isTooLarge(source)) {
            LOGGER.log(Level.FINE,
                    "Skipping {0}, too large to parse (length: {1})",
                    new Object[]{snapshot.getSource().getFileObject(), source.length()});
            lastResult = resultForTooLargeFile(snapshot);
            return;
        }

        try {
//                int caretOffset = reader.getCaretOffset(file);
//                if (caretOffset != -1 && job.translatedSource != null) {
//                    caretOffset = job.translatedSource.getAstOffset(caretOffset);
//                }

            // Construct source by removing <% %> tokens etc.
            StringBuilder sb = new StringBuilder();
            TokenHierarchy hi = TokenHierarchy.create(source, YamlTokenId.language());

            TokenSequence ts = hi.tokenSequence();

            // If necessary move ts to the requested offset
            int offset = 0;
            ts.move(offset);

//                int adjustedOffset = 0;
//                int adjustedCaretOffset = -1;
            while (ts.moveNext()) {
                Token t = ts.token();
                TokenId id = t.id();

                if (id == YamlTokenId.RUBY_EXPR) {
                    String marker = "__"; // NOI18N
                    // Marker
                    sb.append(marker);
                    // Replace with spaces to preserve offsets
                    for (int i = 0, n = t.length()-marker.length(); i < n; i++) { // -2: account for the __
                        sb.append(' ');
                    }
                } else if (id == YamlTokenId.RUBY || id == YamlTokenId.RUBYCOMMENT || id == YamlTokenId.DELIMITER) {
                    // Replace with spaces to preserve offsets
                    for (int i = 0; i < t.length(); i++) {
                        sb.append(' ');
                    }
                } else {
                    sb.append(t.text().toString());
                }

//                    adjustedOffset += t.length();
            }

            source = sb.toString();

            lastResult = parse(source, snapshot);
        } catch (Exception ioe) {
            lastResult = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false, null, null);
        }
    }
 
}
