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

package org.netbeans.modules.java.guards;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.java.guards.GuardTag;
import org.netbeans.modules.java.guards.SectionDescriptor;

/**
 *
 * @author Jan Pokorsky
 */
final class JavaGuardedReader {

    /** The prefix of all magic strings */
    final static String MAGIC_PREFIX = "//GEN-"; // NOI18N
    
    Pattern magicsAsRE;
    
    private static final int LONGEST_ITEM = 10;
    
    /** The list of the SectionsDesc. */
    private final LinkedList<SectionDescriptor> list;

    private final JavaGuardedSectionsProvider provider;
    
    /** Creates a new instance of JavaGuardedReader */
    public JavaGuardedReader(JavaGuardedSectionsProvider provider) {
        list = new LinkedList<SectionDescriptor>();
        this.provider = provider;
    }
    
    public List<GuardedSection> getGuardedSections() {
        return fillSections(list);
    }
    
    public char[] translateToCharBuff(char[] readBuff) {
        char[] charBuff = new char[readBuff.length];
        // points to first unused cell in charBuff
        int charBuffPtr = 0;
        int stop = readBuff.length - 1;

        // read char
        int c;
        // ptr to first not processed char in readBuff
        int i = 0;
        // points to a character right after a newline
        int lastNewLine = 0;

        // final automata
        int fatpos = 0;
        final int MAGICLEN = MAGIC_PREFIX.length();


        //process newlines so only '\n' appears in the charBuff
        //count all kinds of newlines - most used will be used on save
        while (i < stop) {
            c = readBuff[i];
            if (c == '\n') {
                lastNewLine = charBuffPtr;
            }
            charBuff[charBuffPtr++] = readBuff[i++];

            switch (fatpos) {
            case 0:
                if (c == '/') {
                    fatpos++;
                } else {
                    fatpos = 0;
                }
                break;

            case 1:
                if (c == '/') {
                    fatpos++;
                } else {
                    fatpos = 0;
                }
                break;

            case 2:
                if (c == 'G') {
                    fatpos++;
		    } else if (c == '/') {
			fatpos = 2; // what if /////GEN-xxx?
                } else {
                    fatpos = 0;
                }
                break;

            case 3:
                if (c == 'E') {
                    fatpos++;
                } else {
                    fatpos = 0;
                }
                break;

            case 4:
                if (c == 'N') {
                    fatpos++;
                } else {
                    fatpos = 0;
                }
                break;

            case 5:
                if (c == '-') {
                    fatpos++;
                } else {
                    fatpos = 0;
                }
                break;

            default:
                fatpos = 0;
            }

            // "//GEN-" was reached at this time
            if (fatpos == MAGICLEN) {
                fatpos = 0;
                Pattern magics = getMagicsAsRE();
                int searchLen = Math.min(LONGEST_ITEM, readBuff.length - i);
                CharBuffer chi = CharBuffer.wrap(readBuff, i, searchLen);
                Matcher matcher = magics.matcher(chi);
                if (matcher.find()) {
                    String match = matcher.group();

                    charBuffPtr -= MAGICLEN;
                    i += match.length();
                    int toNl = toNewLine(i, readBuff);
                    int sectionSize = MAGICLEN+match.length()+toNl;
                    
//                    if (!justFilter) {
//                        System.out.println("## MATCH: '" + match.substring(0, match.length() - 1) + "'");
                        SectionDescriptor desc = new SectionDescriptor(
                                GuardTag.valueOf(match.substring(0, match.length() - 1)), //XXX catch IAE
                                String.valueOf(readBuff, i, toNl),
                                lastNewLine + 1,
                                charBuffPtr + sectionSize
                                );
//                                new SectionDescriptor(GuardTag.valueOf(match.substring(0, match.length() - 1))); //XXX catch IAE
//                        desc.begin = lastNewLine;
//                        desc.end = charBuffPtr + sectionSize + 1;
//                        desc.name = new String(readBuff, i, toNl);
                        list.add(desc);
//                    }
                    i += toNl;
                    Arrays.fill(charBuff,charBuffPtr,charBuffPtr+sectionSize,' ');
                    charBuffPtr+=sectionSize;
                }
            }
        }

        if (i == stop) {
//            c = readBuff[i];
//            switch(c) {
//                case (int) '\n':
//
//                    newLineTypes[NewLine.N.ordinal()]++;
//
//                    charBuff[charBuffPtr++] = '\n';
//
//                    break;
//                case (int) '\r':
//
//                    newLineTypes[NewLine.R.ordinal()]++;
//
//                    charBuff[charBuffPtr++] = '\n';
//
//                    break;
//                default:
//
                    charBuff[charBuffPtr++] = readBuff[i++];
//            }
        }

        // repair last SectionDesc
        if (/*!justFilter && */(list.size() > 0)) {
            SectionDescriptor desc = (SectionDescriptor) list.getLast();
            if (desc.getEnd() > charBuffPtr) {
                desc.setEnd(charBuffPtr);
            }
        }
        
        char[] res;
        if (charBuffPtr != charBuff.length) {
            res = new char[charBuffPtr];
            System.arraycopy(charBuff, 0, res, 0, charBuffPtr);
        } else {
            res = charBuff;
        }
        return charBuff;
    }

    /** @return searching engine for magics */
    final Pattern getMagicsAsRE() {
        if (magicsAsRE == null) {
            magicsAsRE = Pattern.compile(makeOrRegexp());
        }
        return magicsAsRE;
    }

    /** Makes or regular expression for magics */
    final String makeOrRegexp() {
        StringBuilder sb = new StringBuilder(100);
//        final int len = MAGIC_PREFIX.length();
        for (GuardTag t: GuardTag.values()) {
            sb.append(t.name() + ':');
            sb.append('|');
        }

        return sb.substring(0, sb.length() - 1);
    }

    /** Searches for newline from i */
    static int toNewLine(int i, char[] readBuff) {
        int c;
        int counter = i;
        final int len = readBuff.length;
        while (counter < len) {
            c = readBuff[counter++];
            if (c == '\r' || c == '\n') {
                counter--;
                break;
            }
        }

        return counter - i;
    }

    /** Takes the section descriptors from the GuardedReader and
    * fills the table 'sections', also marks as guarded all sections
    * in the given document.
    * @param is Where to take the guarded section descriptions.
    * @param doc Where to mark guarded.
    */
    List<GuardedSection> fillSections(List<SectionDescriptor> descs) {
        SectionDescriptor descBegin = null;
        List<GuardedSection> sections = new ArrayList<GuardedSection>(descs.size());
        
        for (SectionDescriptor descCurrent: descs) {
            try {
                GuardedSection sect = null;
                switch (descCurrent.getType()) {
                case LINE:
                    sect = provider.createSimpleSection(
                            descCurrent.getName(),
                            descCurrent.getBegin(),
                            descCurrent.getEnd()
                            );
                    break;

                case BEGIN:
                case HEADER:
                case FIRST:
                    descBegin = descCurrent;
                    break;

                case HEADEREND:
                    if ((descBegin != null) &&
                            ((descBegin.getType() == GuardTag.HEADER) || (descBegin.getType() == GuardTag.FIRST)) &&
                            (descCurrent.getName().equals(descBegin.getName()))
                       ) {
                        descBegin.setEnd(descCurrent.getEnd());
                    }
                    else {
                        //SYNTAX ERROR - ignore it.
                        descBegin = null;
                    }
                    break;

                case END:
                case LAST:
                    if ((descBegin != null) && (descBegin.getName().equals(descCurrent.getName()))) {
                        if ((descBegin.getType() == GuardTag.BEGIN) && (descCurrent.getType() == GuardTag.END)) {
                            // simple section
                            sect = provider.createSimpleSection(
                                    descCurrent.getName(),
                                    descBegin.getBegin(), descCurrent.getEnd()
                                    );
                            break;
                        }
                        if (((descBegin.getType() == GuardTag.FIRST) && (descCurrent.getType() == GuardTag.LAST)) ||
                                ((descBegin.getType() == GuardTag.HEADER) && (descCurrent.getType() == GuardTag.END))) {
                                // interior section
                                sect = provider.createInteriorSection(
                                        descCurrent.getName(),
                                        descBegin.getBegin(), descBegin.getEnd(),
                                        descCurrent.getBegin(), descCurrent.getEnd()
                                        );
                            break;
                        }
                    }
                    //SYNTAX ERROR - ignore it.
                    descBegin = null;
                    break;
                }

                if (sect != null) {
                    sections.add(sect);
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(JavaGuardedReader.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        
        return sections;
    }
    
//    static String magic(GuardTag tag) {
//        return MAGIC_PREFIX + tag.name() + ':';
//    }
}
