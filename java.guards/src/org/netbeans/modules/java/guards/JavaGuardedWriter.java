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

package org.netbeans.modules.java.guards;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
final class JavaGuardedWriter {

    private Iterator<SectionDescriptor> descs;
    
    private CharArrayWriter writer;
    
    /** Current section from the previous iterator. For filling this
    * field is used method nextSection.
    */
    private SectionDescriptor current;

    /** Current offset in the original document (NOT in the encapsulated
    * output stream.
    */
    private int offsetCounter;

    /** This flag is used during writing. It is complicated to explain. */
    boolean wasNewLine;

    /** number of consecutive spaces */
    int spaces;
    
    /** Creates a new instance of JavaGuardedWriter */
    public JavaGuardedWriter() {
    }

    public void setGuardedSection(List<GuardedSection> sections) {
        assert this.descs == null; // should be invoked just once
        this.descs = prepareSections(sections).iterator();
    }

    public char[] translate(char[] writeBuff) {
        if (this.descs == null || !this.descs.hasNext()) {
            return writeBuff;
        }
        this.writer = new CharArrayWriter(writeBuff.length);
        this.offsetCounter = 0;
        this.wasNewLine = false;
        
        nextSection();
        
        try {
            for (char c : writeBuff) {
                writeOneChar(c);
            }
            return this.writer.toCharArray();
        } catch (IOException ex) {
            // it hardly occurs since we write to CharArrayWriter, but for sure
            throw new IllegalStateException(ex);
        } finally {
            this.writer = null;
            this.current = null;
        }
        
    }

    /** Write one character. If there is a suitable place,
    * some special comments are written to the underlaying stream.
    * @param b char to write.
    */
    void writeOneChar(int b) throws IOException {
        if (b == '\r')
            return;

        if (current != null) {
            if (offsetCounter == current.getBegin()) {
                wasNewLine = false;
            }
            if ((b == '\n') && (current.getBegin() <= offsetCounter)) {
                switch(current.getType()) {
                    case LINE:

                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.LINE, current.getName());
                                nextSection();
                            }
                            else {
                                writeMagic(GuardTag.BEGIN, current.getName());
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.END, current.getName());
                                nextSection();
                            }
                        }

                        break;
                    case FIRST:
                    case HEADER:

                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.FIRST, current.getName());
                                nextSection();
                            }
                            else {
                                writeMagic(GuardTag.FIRST, current.getName());
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.HEADEREND, current.getName());
                                nextSection();
                            }
                        }

                        break;
                    case LAST:
                    case END:

                        writeMagic(GuardTag.LAST, current.getName());

                        nextSection();

                        break;
                }
            }
        }
        if (b==' ')
            spaces++;
        else {
            char[] sp = new char[spaces];
            
            Arrays.fill(sp,' ');
            writer.write(sp);
            writer.write(b);
            spaces=0;
        }
        offsetCounter++;
    }

    /** Try to get next sectionDesc from the 'sections'
    * If there is no more section the 'current' will be set to null.
    */
    private void nextSection() {
        current = descs.hasNext() ? descs.next() : null;
    }

    /** Writes the magic to the underlaying stream.
    * @param type The type of the magic section - T_XXX constant.
    * @param name name of the section.
    */
    private void writeMagic(GuardTag type, String name) throws IOException {
        // XXX see #73805 to resolve this hack
//        if (!shouldReload) {
//            shouldReload = spaces != SECTION_MAGICS[type].length()  + name.length();
//        }
        spaces = 0;
        String magic = JavaGuardedReader.MAGIC_PREFIX + type.name() + ':';
        writer.write(magic, 0, magic.length());
        writer.write(name, 0, name.length());
    }

    /** This method prepares the iterator of the SectionDesc classes
    * @param list The list of the GuardedSection classes.
    * @return iterator of the SectionDesc
    */
    private List<SectionDescriptor> prepareSections(List<? extends GuardedSection> list) {
        List<SectionDescriptor> dest = new ArrayList<SectionDescriptor>(list.size());
        Collections.sort(list, new GuardedPositionComparator());

        for (GuardedSection o: list) {
            if (o instanceof SimpleSection) {
                SectionDescriptor desc = new SectionDescriptor(
                        GuardTag.LINE,
                        o.getName(),
                        o.getStartPosition().getOffset(),
                        o.getEndPosition().getOffset()
                        );
                dest.add(desc);
            } else {
                SectionDescriptor desc = new SectionDescriptor(
                        GuardTag.HEADER,
                        o.getName(),
                        o.getStartPosition().getOffset(),
                        ((InteriorSection) o).getBodyStartPosition().getOffset()
                        );
                dest.add(desc);

                desc = new SectionDescriptor(
                        GuardTag.END,
                        o.getName(),
                        ((InteriorSection) o).getBodyEndPosition().getOffset(),
                        o.getEndPosition().getOffset()
                        );
                dest.add(desc);
            }
        }
        return dest;
    }
    
}
