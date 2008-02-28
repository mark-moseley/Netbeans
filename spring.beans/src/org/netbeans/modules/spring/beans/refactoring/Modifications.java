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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 * Class for holding all modifications.
 *
 * @author Dusan Balek et al.
 */
public final class Modifications {

    private final Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();

    public Modifications() {}

    public void addDifference(FileObject fileObject, Difference difference){
        List<Difference> differences = diffs.get(fileObject);
        if (differences == null){
            differences = new ArrayList<Difference>();
            differences.add(difference);
            diffs.put(fileObject, differences);
        } else {
            differences.add(difference);
        }
    }

    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }

    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files.
     */
    public void commit() throws IOException {
        for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
            commit(me.getKey(), me.getValue(), null);
        }
    }

    private void commit(final FileObject fileObject, final List<Difference> differences, Writer outWriter) throws IOException {
        DataObject dataObj = DataObject.find(fileObject);
        EditorCookie editorCookie = dataObj != null ? (EditorCookie) dataObj.getCookie(EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to
        // found document.
        if (editorCookie != null && outWriter == null) {
            Document doc = editorCookie.getDocument();
            if (doc != null) {
                if (doc instanceof BaseDocument)
                    ((BaseDocument)doc).atomicLock();
                try {
                    for (Difference diff : differences) {
                        if (diff.isExcluded())
                            continue;
                        try {
                            switch (diff.getKind()) {
                            case INSERT:
                                doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                break;
                            case REMOVE:
                                doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                break;
                            case CHANGE:
                                doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                break;
                            }
                        } catch (BadLocationException ex) {
                            IOException ioe = new IOException();
                            ioe.initCause(ex);
                            throw ioe;
                        }
                    }
                } finally {
                    if (doc instanceof BaseDocument)
                        ((BaseDocument)doc).atomicUnlock();
                }
                return;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;
        Reader inputReader = null;
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fileObject);
            ins = fileObject.getInputStream();
            baos = new ByteArrayOutputStream();
            FileUtil.copy(ins, baos);

            ins.close();
            ins = null;
            byte[] arr = baos.toByteArray();
            int arrLength = convertToLF(arr);
            baos.close();
            baos = null;
            inputReader = new InputStreamReader(new ByteArrayInputStream(arr, 0, arrLength), encoding);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            if (outWriter == null) {
                outWriter = new OutputStreamWriter(fileObject.getOutputStream(), encoding);
            }
            int offset = 0;

            // need to be sure, that the differences will be sorted acocrding
            // their start offset
            Difference aDifferences[] = differences.toArray(new Difference[differences.size()]);
            Arrays.sort(aDifferences, new Comparator<Difference>(){

                public int compare(Difference diff1, Difference diff2) {
                    int result = 0;
                    if (diff1.getStartPosition().getOffset() < diff2.getStartPosition().getOffset()){
                        result = -1;
                    }
                    else if (diff1.getStartPosition().getOffset() > diff2.getStartPosition().getOffset()){
                        result = 1;
                    }
                    return result;
                }

            });

            for (Difference diff : aDifferences) {
                if (diff.isExcluded())
                    continue;
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int length;
                int rcounter = 0;
                while ((length = inputReader.read(buff,0, toread - rcounter)) > 0 && rcounter < toread) {
                    outWriter.write(buff, 0, length);
                    rcounter+=length;
                    offset += length;
                }
                switch (diff.getKind()) {
                                                                                                                                case INSERT:
                    outWriter.write(diff.getNewText());
                    break;
                case REMOVE:
                    int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                    inputReader.skip(len);
                    offset += len;
                    break;
                case CHANGE:
                    len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                    inputReader.skip(len);
                    offset += len;
                    outWriter.write(diff.getNewText());
                    break;
                }
            }
            char[] buff = new char[1024];
            int count;
            while ((count = inputReader.read(buff)) > 0)
                outWriter.write(buff, 0, count);
        } finally {
            if (ins != null)
                ins.close();
            if (baos != null)
                baos.close();
            if (inputReader != null)
                inputReader.close();
            if (outWriter != null)
                outWriter.close();
        }
    }

    private int convertToLF(byte[] buff) {
        int index = 0;
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] != '\r') {
                buff[index++] = buff[i];
            }
        }
        return index;
    }

    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     *
     * @param   there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     */
    public String getResultingSource(FileObject fileObject) throws IOException {
        assert fileObject != null : "Provided fileObject is null";
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);

        return writer.toString();
    }

    public static final class Difference {

        private final Kind kind;
        private final PositionBounds position;
        private final String newText;
        private boolean excluded;

        public Difference(Kind kind, PositionBounds position, String newText) {
            this.kind = kind;
            this.position = position;
            this.newText = newText;
            this.excluded = false;
        }

        public Kind getKind() {
            return kind;
        }

        public PositionRef getStartPosition() {
            return position.getBegin();
        }

        public PositionRef getEndPosition() {
            return position.getEnd();
        }

        public String getNewText() {
            return newText;
        }

        public boolean isExcluded() {
            return excluded;
        }

        public void setExcluded(boolean excluded) {
            this.excluded = excluded;
        }

        @Override
        public String toString() {
            return kind + "<" + getStartPosition() + ", " + getEndPosition() + "> -> " + newText; //NOI18N
        }

        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE
        }
    }
}
