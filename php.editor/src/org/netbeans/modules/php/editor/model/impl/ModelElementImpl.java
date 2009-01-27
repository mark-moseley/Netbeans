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
package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.PHPElement;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
abstract class ModelElementImpl extends PHPElement implements ModelElement {

    private PhpKind kind;
    private String name;
    private OffsetRange offsetRange;
    private Union2<String/*url*/, FileObject> file;
    private PhpModifiers modifiers;
    private ScopeImpl inScope;

    //new contructors
    ModelElementImpl(ScopeImpl inScope, ASTNodeInfo info, PhpModifiers modifiers) {
        this(inScope, info.getName(),inScope.getFile(),info.getRange(),info.getPhpKind(),modifiers);
        inScope.addElement(this);
    }

    ModelElementImpl(ScopeImpl inScope, IndexedElement element, PhpKind kind) {
        this(inScope, element.getName(),Union2.<String, FileObject>createFirst(element.getFilenameUrl()),
                new OffsetRange(element.getOffset(), element.getOffset()+element.getName().length()),
                kind, new PhpModifiers(element.getFlags()));
        inScope.addElement(this);
    }

    //old contructors
    ModelElementImpl(ScopeImpl inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpKind kind) {
        this(inScope, name, file, offsetRange, kind, PhpModifiers.EMPTY);
    }

    ModelElementImpl(ScopeImpl inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, PhpKind kind,
            PhpModifiers modifiers) {
        if (name == null || file == null || kind == null || modifiers == null) {
            throw new IllegalArgumentException("null for name | fo | kind: " //NOI18N
                    + name + " | " + file + " | " + kind);//NOI18N
        }
        assert file.hasFirst() || file.hasSecond();
        this.inScope = inScope;
        this.name = name;
        this.offsetRange = offsetRange;
        this.kind = kind;
        this.file = file;
        this.modifiers = modifiers;
        //checkModifiersAssert();
    }


    @Override
    public final String getIn() {
        ScopeImpl retval = getInScope();
        return (retval != null) ? retval.getName() : null;
    }

    public final ScopeImpl getInScope() {
        return inScope;
    }

    @NonNull
    @Override
    public final String getMimeType() {
        return super.getMimeType();
    }

    @Override
    public final String getName() {
        return name;
    }

    public String getNormalizedName() {
        return getName().toLowerCase();
    }

    public final String getCamelCaseName() {
        return toCamelCase(getName());
    }

    static String toCamelCase(String plainName) {
        char[] retval = new char[plainName.length()];
        int retvalSize = 0;
        for (int i = 0; i < retval.length; i++) {
            char c = plainName.charAt(i);
            if (Character.isUpperCase(c)) {
                retval[retvalSize] = c;
                retvalSize++;
            }
        }
        return String.valueOf(String.valueOf(retval, 0, retvalSize));
    }

    static boolean nameKindMatch(Pattern p, String text) {
        return p.matcher(text).matches();
    }

    static boolean nameKindMatchForVariable(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(false, text, nameKind, queries);
    }

    static boolean nameKindMatch(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(true, text, nameKind, queries);
    }

    private static boolean nameKindMatch(boolean forceCaseInsensitivity, String text, QuerySupport.Kind nameKind, String... queries) {
        for (String query : queries) {
            switch (nameKind) {
                case CAMEL_CASE:
                    if (toCamelCase(text).startsWith(query)) {
                        return true;
                    }
                    break;
                case CASE_INSENSITIVE_PREFIX:
                    if (text.toLowerCase().startsWith(query.toLowerCase())) {
                        return true;
                    }
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    text = text.toLowerCase();
                case REGEXP:
                    //TODO: might be perf. problem if called for large collections
                    // and ever and ever again would be compiled still the same query
                    Pattern p = Pattern.compile(query);
                    if (nameKindMatch(p, text)) {
                        return true;
                    }
                    break;
                case EXACT:
                    boolean retval = (forceCaseInsensitivity) ? text.equalsIgnoreCase(query) : text.equals(query);
                    if (retval) {
                        return true;
                    }
                    break;
                case PREFIX:
                    if (text.startsWith(query)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public final ElementKind getKind() {
        switch (getPhpKind()) {
            case CLASS:
                return ElementKind.CLASS;
            case CLASS_CONSTANT:
                return ElementKind.CONSTANT;
            case CONSTANT:
                return ElementKind.CONSTANT;
            case FIELD:
                return ElementKind.FIELD;
            case FUNCTION:
                return ElementKind.METHOD;
            case IFACE:
                return ElementKind.CLASS;
            case METHOD:
                return ElementKind.METHOD;
            case VARIABLE:
                return ElementKind.VARIABLE;
        }
        return ElementKind.OTHER;
    }


    public PhpKind getPhpKind() {
        return kind;
    }

    public int getOffset() {
        return getNameRange().getStart();
    }

    @NonNull
    protected Union2<String, FileObject> getFile() {
        return file;
    }

    @CheckForNull
    @Override
    public FileObject getFileObject() {
        FileObject fileObject = null;
        synchronized (ModelElementImpl.class) {
            fileObject = file.hasSecond() ? file.second() : null;
        }
        if (fileObject == null) {
            assert file.hasFirst();
            String fileUrl = file.first();
            fileObject = PHPIndex.getFileObject(fileUrl);
            synchronized (ModelElementImpl.class) {
                file = Union2.createSecond(fileObject);
            }
        }
        return fileObject;
    }

    @Override
    public Set<Modifier> getModifiers() {
        assert modifiers != null;
        Set<Modifier> retval = new HashSet<Modifier>();
        if (modifiers.isPublic()) {
            retval.add(Modifier.PUBLIC);
        }
        if (modifiers.isProtected()) {
            retval.add(Modifier.PROTECTED);
        }
        if (modifiers.isPrivate()) {
            retval.add(Modifier.PRIVATE);
        }
        if (modifiers.isStatic()) {
            retval.add(Modifier.STATIC);
        }
        return retval;
    }

    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    public final boolean isScope() {
        return this instanceof ScopeImpl;
    }

    void checkModifiersAssert() {
        assert modifiers != null;
    }

    void checkScopeAssert() {
        assert inScope != null;
    }

    final StringBuilder golden() {
        return golden(0);
    }

    abstract StringBuilder golden(int indent);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpKind().toString()).append(" ").append(getName());
        return sb.toString();
    }

    public PHPElement getPHPElement() {
        return this;
    }

    /**
     * @return the offsetRange
     */
    public OffsetRange getNameRange() {
        return offsetRange;
    }
 
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelElementImpl other = (ModelElementImpl) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (!this.getNormalizedName().equals(other.getNormalizedName())) {
            return false;
        }
        //TODO: classscopes from different files are not the same, but be carefull about
        // perf. problems before uncommenting it
        /*if (!this.getFileObject().equals(other.getFileObject())) {
            return false;
        }*/
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        hash = 89 * hash + (this.getNormalizedName() != null ? this.getNormalizedName().hashCode() : 0);
        //hash = 89 * hash + (this.getInScope() != null ? this.getInScope().hashCode() : 0);
        //hash = 89 * hash + Integer.valueOf(this.getOffset()).hashCode();
        //hash = 89 * hash + this.file.hashCode();
        return hash;
    }

    public OffsetRange getOffsetRange(ParserResult result) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}