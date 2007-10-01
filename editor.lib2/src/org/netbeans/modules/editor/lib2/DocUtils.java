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

package org.netbeans.modules.editor.lib2;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.openide.util.NbBundle;

/**
 * This class contains useful methods for working with documents.
 * 
 * @author Vita Stejskal
 */
public final class DocUtils {

    private static final Logger LOG = Logger.getLogger(DocUtils.class.getName());
    
    public static int getRowStart(Document doc, int offset, int lineShift)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        if (lineShift != 0) {
            Element lineRoot = doc.getDefaultRootElement();
            int line = lineRoot.getElementIndex(offset);
            line += lineShift;
            if (line < 0 || line >= lineRoot.getElementCount()) {
                return -1; // invalid line shift
            }
            return lineRoot.getElement(line).getStartOffset();

        } else { // no shift
            return doc.getDefaultRootElement().getElement(
                   doc.getDefaultRootElement().getElementIndex(offset)).getStartOffset();
        }
    }

    public static int getRowEnd(Document doc, int offset)
    throws BadLocationException {
        checkOffsetValid(doc, offset);

        return doc.getDefaultRootElement().getElement(
               doc.getDefaultRootElement().getElementIndex(offset)).getEndOffset() - 1;
    }
    
    /** 
     * Return line offset (line number - 1) for some position in the document.
     * 
     * @param doc document to operate on
     * @param offset position in document where to start searching
     */
    public static int getLineOffset(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength() + 1);

        Element lineRoot = doc.getDefaultRootElement();
        return lineRoot.getElementIndex(offset);
    }

    public static String debugPosition(Document doc, int offset) {
        String ret;

        if (offset >= 0) {
            try {
                int line = getLineOffset(doc, offset) + 1;
                int col = getVisualColumn(doc, offset) + 1;
                ret = String.valueOf(line) + ":" + String.valueOf(col); // NOI18N
            } catch (BadLocationException e) {
                ret = NbBundle.getBundle(DocUtils.class).getString("wrong_position")
                      + ' ' + offset + " > " + doc.getLength(); // NOI18N
            }
        } else {
            ret = String.valueOf(offset);
        }

        return ret;
    }

    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(Document doc, int offset) throws BadLocationException {
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        // TODO: fix this, do not use reflection
        try {
            Method m = findDeclaredMethod(doc.getClass(), "getVisColFromPos", Integer.TYPE); //NOI18N
            m.setAccessible(true);
            int col = (Integer) m.invoke(doc, offset);
            return col;
//            return doc.getVisColFromPos(offset);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    private static Method findDeclaredMethod(Class clazz, String name, Class... parameters) throws NoSuchMethodException {
        while(clazz != null) {
            try {
                return clazz.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method: " + name); //NOI18N
    }
    
    public static boolean isIdentifierPart(Document doc, char ch) {
        // TODO: make this configurable
        return AcceptorFactory.LETTER_DIGIT.accept(ch);
    }
    
    public static boolean isWhitespace(char ch) {
        // TODO: make this configurable
        return AcceptorFactory.WHITESPACE.accept(ch);
    }
    
    public static void atomicLock(Document doc) {
        // TODO: fix this, do not use reflection
        try {
            Method lockMethod = doc.getClass().getMethod("atomicLock");
            lockMethod.invoke(doc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    public static void atomicUnlock(Document doc) {
        // TODO: fix this, do not use reflection
        try {
            Method unlockMethod = doc.getClass().getMethod("atomicUnlock");
            unlockMethod.invoke(doc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength());
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }
    
    /** Creates a new instance of DocUtils */
    private DocUtils() {
    }
    
}
