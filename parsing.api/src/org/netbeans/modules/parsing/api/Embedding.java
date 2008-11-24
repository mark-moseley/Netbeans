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

package org.netbeans.modules.parsing.api;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents one block of code embedded in some other source. Performance 
 * is the only purpose of this class. You can obtain some basic information 
 * about embedded block of code before it is really created.
 *
 * Following example shows how to create compound Embedding from some snapshot:
 * 
 * <pre> 
 *         Embedding compoundEmbedding = Embedding.create (Arrays.asList (new Source[] {
 *             snapshot.create ("some prefix code", "text/x-java"),
 *             snapshot.create (10, 100, "text/x-java"),
 *             snapshot.create ("some postfix code", "text/x-java")
 *         })));
 * </pre>
 * 
 * @author Jan Jancura
 */
public final class Embedding {
    
    /**
     * Creates {@link Embedding} from a list of embeddings. All embeddings 
     * have to be created from one Smapshot. All embeddings must have the same 
     * mime type, but this mime type have to be different than current 
     * embedding mime types.
     * 
     * @param embeddings    A list of some embeddings created from one source.
     * @return              A new embedding compound from given pieces.
     * @throws IllegalArgumentException
     *                      if embeddings collection is empty, or
     *                      mime types of embeddings are not same.
     * @throws NullPointerException
     *                      embedding is null.
     */
    public static Embedding create (
        List<Embedding>        embeddings
    ) {
        if (embeddings.isEmpty ()) throw new IllegalArgumentException ();
        String mimeType = null;
        Source source = null;
        StringBuilder sb = new StringBuilder ();
        List<int[]> currentToOriginal = new ArrayList<int[]> ();
        List<int[]> originalToCurrent = new ArrayList<int[]> ();
        int offset = 0;
        for (Embedding embedding : embeddings) {
            Snapshot snapshot = embedding.getSnapshot ();
            if (mimeType != null) {
                if (!mimeType.equals (embedding.mimeType)) {
                    throw new IllegalArgumentException ();
                }
                if (source != snapshot.getSource ()) {
                    throw new IllegalArgumentException ();
                }
            } else {
                mimeType = embedding.mimeType;
                source = snapshot.getSource ();
            }
            sb.append (snapshot.getText ());
            int[][] p = snapshot.currentToOriginal;
            for (int i = 0; i < p.length; i++) {
                currentToOriginal.add (new int[] {p [i] [0] + offset, p [i] [1]});
                if (p [i] [1] >= 0) {
                    originalToCurrent.add (new int[] {p [i] [1], p [i] [0] + offset});
                } else if (!originalToCurrent.isEmpty ()) {
                    originalToCurrent.add (new int[] {
                        originalToCurrent.get (originalToCurrent.size () - 1) [0] + 
                            p [i] [0] + offset - 
                            originalToCurrent.get (originalToCurrent.size () - 1) [1], 
                        -1
                    });
                }
            }
            offset +=snapshot.getText ().length ();
        }
        if (originalToCurrent.size() > 0 && originalToCurrent.get (originalToCurrent.size () - 1) [1] >= 0) {
            originalToCurrent.add (new int[] {
                originalToCurrent.get (originalToCurrent.size () - 1) [0] + 
                    sb.length () - 
                    originalToCurrent.get (originalToCurrent.size () - 1) [1], 
                -1
            });
        }
        Snapshot snapshot = new Snapshot (
            sb,
            source,
            mimeType,
            currentToOriginal.toArray (new int [currentToOriginal.size ()] []),
            originalToCurrent.toArray (new int [originalToCurrent.size ()] [])
        );
        return new Embedding (
            snapshot, 
            mimeType
        );
    }
    
    private Snapshot        snapshot;
    private String          mimeType;
                
    Embedding (
        Snapshot            snapshot,
        String              mimeType
    ) {
        this.snapshot =     snapshot;
        this.mimeType =     mimeType;
    }
    
    /**
     * Returns {@link Snapshot} for embedded block of code.
     * 
     * @return              A {@link Snapshot} for embedded block of code..
     */
    public final Snapshot getSnapshot () {
        return snapshot;
    }
    
    /**
     * Returns mime type of embedded source.
     * 
     * @return              A mime type of embedded source.
     */
    public final String getMimeType () {
        return mimeType;
    }
    
    /**
     * Returns <code>true</code> if this embedding contains given offset related
     * to top level source.
     * 
     * @param originalOffset
     *                      A offset in original source.
     * @return              <code>true</code> if this embedding contains given offset
     */
    public final boolean containsOriginalOffset (int originalOffset) {
	return snapshot.getEmbeddedOffset (originalOffset) >= 0;
    }

    @Override
    public String toString () {
        return "Embedding (" + getMimeType () + ", " + getSnapshot () + ")";
    }
}




