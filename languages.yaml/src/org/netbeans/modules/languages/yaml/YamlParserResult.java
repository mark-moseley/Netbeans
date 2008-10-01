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

import java.util.List;
import org.jvyamlb.Position.Range;
import org.jvyamlb.Positionable;
import org.jvyamlb.nodes.Node;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.StructureItem;

/**
 * A result from Parsing YAML
 * 
 * @author Tor Norbye
 */
public class YamlParserResult extends ParserResult {

    private Node node;
    private List<? extends StructureItem> items;
    private int[] byteToUtf8;
    private int[] utf8ToByte;

    public YamlParserResult(Node node, YamlParser parser, ParserFile file, boolean valid, int[] byteToUtf8, int[] utf8ToByte) {
        super(parser, file, YamlTokenId.YAML_MIME_TYPE, valid);
        this.node = node;
        this.byteToUtf8 = byteToUtf8;
        this.utf8ToByte = utf8ToByte;
    }

    public Node getObject() {
        return node;
    }

    @Override
    public AstTreeNode getAst() {
        return null;
    }

    public synchronized List<? extends StructureItem> getItems() {
        if (items == null) {
            items = new YamlScanner().scanStructure(this);
        }

        return items;
    }

    public void setItems(List<? extends StructureItem> items) {
        this.items = items;
    }

    public int convertUtf8ToByte(int utf8Pos) {
        if (utf8ToByte == null) {
            return utf8Pos;
        }
        if (utf8Pos < utf8ToByte.length) {
            return utf8ToByte[utf8Pos];
        } else {
            return utf8ToByte.length;
        }
    }

    public int convertByteToUtf8(int bytePos) {
        if (byteToUtf8 == null) {
            return bytePos;
        }
        if (bytePos < byteToUtf8.length) {
            return byteToUtf8[bytePos];
        } else {
            return byteToUtf8.length;
        }
    }

    public OffsetRange getAstRange(Range range) {
        int start = range.start.offset;
        int end = range.end.offset;
        if (byteToUtf8 == null) {
            return new OffsetRange(start, end);
        } else {
            int s,e;
            if (start >= byteToUtf8.length) {
                s = byteToUtf8.length;
            } else {
                s = byteToUtf8[start];
            }
            if (end >= byteToUtf8.length) {
                e = byteToUtf8.length;
            } else {
                e = byteToUtf8[end];
            }

            return new OffsetRange(s, e);
        }
    }
    
    public OffsetRange getAstRange(Node node) {
        return getAstRange(((Positionable)node).getRange());
    }
}
