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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 * This class represents QNameType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 *
 *
 * @author Ayub Khan
 */
public class QNameType extends StringBase {
    
    protected boolean hasFacets;
    
    private List<Integer> lengths;
    
    private List<Integer> minLengths;
    
    private List<Integer> maxLengths;
    
    private List<String> patterns;
    
    protected List<String> enumerations;
    
    private List<Whitespace.Treatment> whitespaces;
    
    /**
     * Creates a new instance of QNameType
     */
    public QNameType() {
        super(Datatype.Kind.QNAME);
    }
    
    /*
     * returns length (this corresponds to the value of length facet in schema)
     *
     * @return length
     */
    public List<Integer> getLengths() {
        return lengths;
    }
    
    /*
     * returns minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @return minLength
     */
    public List<Integer> getMinLengths() {
        return minLengths;
    }
    
    /*
     * returns maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @return maxLength
     */
    public List<Integer> getMaxLengths() {
        return maxLengths;
    }
    
    /*
     * returns pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @return pattern
     */
    public List<String> getPatterns() {
        return patterns;
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @return enumeration
     */
    public List<String> getEnumerations() {
        return enumerations;
    }
    
    /*
     * returns whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @return whitespaces
     */
    public List<Whitespace.Treatment> getWhiteSpaces() {
        return whitespaces;
    }
    
    /*
     * set length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void addLength(int length) {
        if(lengths == null) {
            lengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.lengths.add(new Integer(length));
    }
    
    /*
     * set minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void addMinLength(int minLength) {
        if(minLengths == null) {
            minLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.minLengths.add(new Integer(minLength));
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void addMaxLength(int maxLength) {
        if(maxLengths == null) {
            maxLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.maxLengths.add(new Integer(maxLength));
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void addPattern(String pattern) {
        if(patterns == null) {
            patterns = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.patterns.add(pattern);
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(String enumeration) {
        if(enumerations == null) {
            enumerations = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.enumerations.add(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void addWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces == null) {
            whitespaces = new ArrayList<Whitespace.Treatment>(1);
            hasFacets = true;
        }
        this.whitespaces.add(whitespace);
    }
    
    /*
     * set length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void removeLength(int length) {
        if(lengths == null) {
            lengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.lengths.remove(new Integer(length));
    }
    
    /*
     * set minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void removeMinLength(int minLength) {
        if(minLengths == null) {
            minLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.minLengths.remove(new Integer(minLength));
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void removeMaxLength(int maxLength) {
        if(maxLengths == null) {
            maxLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.maxLengths.remove(new Integer(maxLength));
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
        if(patterns == null) {
            patterns = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.patterns.remove(pattern);
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void removeEnumeration(String enumeration) {
        if(enumerations == null) {
            enumerations = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.enumerations.remove(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void removeWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces == null) {
            whitespaces = new ArrayList<Whitespace.Treatment>(1);
            hasFacets = true;
        }
        this.whitespaces.remove(whitespace);
    }
}
