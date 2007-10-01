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

package org.netbeans.lib.editor.util.swing;

import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * A pair of positions delimiting a text region in a swing document.
 * <br/>
 * At all times it should be satisfied that
 * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public class MutablePositionRegion extends PositionRegion {

    /**
     * Construct new mutable position region.
     *
     * @param startPosition non-null start position of the region &lt;= end position.
     * @param endPosition non-null end position of the region &gt;= start position.
     */
    public MutablePositionRegion(Position startPosition, Position endPosition) {
        super(startPosition, endPosition);
    }
    
    /**
     * Construct new mutable position region based on the knowledge
     * of the document and starting and ending offset.
     */
    public MutablePositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }

    /**
     * Set a new start and end positions of this region.
     * <br/>
     * They should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param startPosition non-null new start position of this region.
     * @since 1.10
     */
    public final void reset(Position startPosition, Position endPosition) {
        resetImpl(startPosition, endPosition);
    }
    
    /**
     * Set a new start position of this region.
     * <br/>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param startPosition non-null new start position of this region.
     */
    public final void setStartPosition(Position startPosition) {
        setStartPositionImpl(startPosition);
    }

    /**
     * Set a new end position of this region.
     * <br/>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param endPosition non-null new start position of this region.
     */
    public final void setEndPosition(Position endPosition) {
        setEndPositionImpl(endPosition);
    }

}
