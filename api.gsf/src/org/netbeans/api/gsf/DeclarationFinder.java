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
package org.netbeans.api.gsf;

import java.net.URL;

import javax.swing.text.Document;

import org.netbeans.api.gsf.annotations.NonNull;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Tor Norbye
 */
public interface DeclarationFinder {
    /**
     * Find the declaration for the program element that is under the caretOffset
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     *
     * Return {@link Declaration.NONE} if the declaration can not be found, otherwise return
     *   a valid DeclarationLocation.
     */
    @NonNull
    DeclarationLocation findDeclaration(@NonNull CompilationInfo info, int caretOffset);

    /**
     * Check the caret offset in the document and determine if it is over a span
     * of text that should be hyperlinkable ("Go To Declaration" - in other words,
     * locate the reference and return it. When the user drags the mouse with a modifier
     * key held this will be hyperlinked, and so on.
     * <p>
     * Remember that when looking up tokens in the token hiearchy, you will get the token
     * to the right of the caret offset, so check for these conditions
     * {@code (sequence.move(offset); sequence.offset() == offset)} and check both
     * sides such that placing the caret between two tokens will match either side.
     *
     * @return {@link OffsetRange.NONE} if the caret is not over a valid reference span,
     *   otherwise return the character range for the given hyperlink tokens
     */
    @NonNull
    public OffsetRange getReferenceSpan(@NonNull Document doc, int caretOffset);

    /**
     * Holder object for return values from the DeclarationFinder#findDeclaration method.
     * The constant NONE object should be returned when finding a declaration failed.
     */
    public final class DeclarationLocation {
        /** DeclarationLocation representing no match or failure to find declaration */
        public static final DeclarationLocation NONE = new DeclarationLocation(null, -1);
        private final FileObject fileObject;
        private final int offset;
        private final URL url;
        /** Associated element, if any */
        private Element element;

        public DeclarationLocation(final FileObject fileObject, final int offset) {
            this.fileObject = fileObject;
            this.offset = offset;
            this.url = null;
        }

        public DeclarationLocation(final FileObject fileObject, final int offset, Element element) {
            this(fileObject, offset);
            this.element = element;
        }

        public DeclarationLocation(final URL url) {
            this.url = url;
            this.fileObject = null;
            this.offset = -1;
        }
        
        public URL getUrl() {
            return url;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public int getOffset() {
            return offset;
        }
        
        public Element getElement() {
            return element;
        }

        public String toString() {
            if (this == NONE) {
                return "NONE";
            }

            if (url != null) {
                return url.toExternalForm();
            }

            return fileObject.getNameExt() + ":" + offset;
        }
    }
}
