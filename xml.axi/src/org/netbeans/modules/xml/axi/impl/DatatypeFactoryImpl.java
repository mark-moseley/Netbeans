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

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.datatype.*;
import org.netbeans.modules.xml.axi.datatype.Datatype.Facet;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;

/**
 *
 * @author Ayub Khan
 */
public class DatatypeFactoryImpl extends DatatypeFactory {
    
    private static HashMap<Datatype.Kind, List<Class<? extends SchemaComponent>>> asfCache =
            new HashMap<Datatype.Kind, List<Class<? extends SchemaComponent>>>();
    
    /** Creates a new instance of DatatypeFactory */
    public DatatypeFactoryImpl() {
    }
    
    public synchronized List<Class<? extends SchemaComponent>> getApplicableSchemaFacets(SimpleType st) {
        List<Class<? extends SchemaComponent>> facetClasses = Collections.emptyList();
        Datatype d = new DatatypeBuilder().getDatatype(st);
        if(d != null) {
            facetClasses = asfCache.get(d.getKind());
            if(facetClasses == null) {
                facetClasses = new ArrayList<Class<? extends SchemaComponent>>();
                List<Facet> afs = d.getApplicableFacets();
                for(Facet f:afs)
                    facetClasses.add(f.getComponentType());
                asfCache.put(d.getKind(), facetClasses);
            }
        }
        return facetClasses;
    }
    
    /**
     * Creates an AXI Datatype, given a typeName (built-in types
     * like "string" or custom types "SKU" etc).
     */
    public Datatype createPrimitive(String typeName) {
        Datatype returnType = null;
        SimpleType schemaType = getPrimitiveType(typeName);
        if(schemaType != null) {
            if(typeName.equals(Datatype.Kind.STRING.getName())) {
                returnType = new StringType();
            } else if(typeName.equals(Datatype.Kind.NORMALIZED_STRING.getName())) {
                returnType = new NormalizedStringType();
            } else if(typeName.equals(Datatype.Kind.TOKEN.getName())) {
                returnType = new TokenType();
            } else if(typeName.equals(Datatype.Kind.LANGUAGE.getName())) {
                returnType = new LanguageType();
            } else if(typeName.equals(Datatype.Kind.NAME.getName())) {
                returnType = new NameType();
            } else if(typeName.equals(Datatype.Kind.NMTOKEN.getName())) {
                returnType = new NmTokenType();
            } else if(typeName.equals(Datatype.Kind.NCNAME.getName())) {
                returnType = new NcNameType();
            } else if(typeName.equals(Datatype.Kind.NMTOKENS.getName())) {
                returnType = new NmTokensType();
            } else if(typeName.equals(Datatype.Kind.ID.getName())) {
                returnType = new IdType();
            } else if(typeName.equals(Datatype.Kind.IDREF.getName())) {
                returnType = new IdRefType();
            } else if(typeName.equals(Datatype.Kind.ENTITY.getName())) {
                returnType = new EntityType();
            } else if(typeName.equals(Datatype.Kind.IDREFS.getName())) {
                returnType = new IdRefsType();
            } else if(typeName.equals(Datatype.Kind.ENTITIES.getName())) {
                returnType = new EntitiesType();
            } else if(typeName.equals(Datatype.Kind.DECIMAL.getName())) {
                returnType = new DecimalType();
            } else if(typeName.equals(Datatype.Kind.INTEGER.getName())) {
                returnType = new IntegerType();
            } else if(typeName.equals(Datatype.Kind.NON_POSITIVE_INTEGER.getName())) {
                returnType = new NonPositiveIntegerType();
            } else if(typeName.equals(Datatype.Kind.LONG.getName())) {
                returnType = new LongType();
            } else if(typeName.equals(Datatype.Kind.NON_NEGATIVE_INTEGER.getName())) {
                returnType = new NonNegativeIntegerType();
            } else if(typeName.equals(Datatype.Kind.NEGATIVE_INTEGER.getName())) {
                returnType = new NegativeIntegerType();
            } else if(typeName.equals(Datatype.Kind.INT.getName())) {
                returnType = new IntType();
            } else if(typeName.equals(Datatype.Kind.SHORT.getName())) {
                returnType = new ShortType();
            } else if(typeName.equals(Datatype.Kind.BYTE.getName())) {
                returnType = new ByteType();
            } else if(typeName.equals(Datatype.Kind.UNSIGNED_LONG.getName())) {
                returnType = new UnsignedLongType();
            } else if(typeName.equals(Datatype.Kind.UNSIGNED_INT.getName())) {
                returnType = new UnsignedIntType();
            } else if(typeName.equals(Datatype.Kind.UNSIGNED_SHORT.getName())) {
                returnType = new UnsignedShortType();
            } else if(typeName.equals(Datatype.Kind.UNSIGNED_BYTE.getName())) {
                returnType = new UnsignedByteType();
            } else if(typeName.equals(Datatype.Kind.POSITIVE_INTEGER.getName())) {
                returnType = new PositiveIntegerType();
            } else if(typeName.equals(Datatype.Kind.DURATION.getName())) {
                returnType = new DurationType();
            } else if(typeName.equals(Datatype.Kind.DATE_TIME.getName())) {
                returnType = new DateTimeType();
            } else if(typeName.equals(Datatype.Kind.TIME.getName())) {
                returnType = new TimeType();
            } else if(typeName.equals(Datatype.Kind.DATE.getName())) {
                returnType = new DateType();
            } else if(typeName.equals(Datatype.Kind.G_YEAR_MONTH.getName())) {
                returnType = new GYearMonthType();
            } else if(typeName.equals(Datatype.Kind.G_YEAR.getName())) {
                returnType = new GYearType();
            } else if(typeName.equals(Datatype.Kind.G_MONTH_DAY.getName())) {
                returnType = new GMonthDayType();
            } else if(typeName.equals(Datatype.Kind.G_DAY.getName())) {
                returnType = new GDayType();
            } else if(typeName.equals(Datatype.Kind.G_MONTH.getName())) {
                returnType = new GMonthType();
            } else if(typeName.equals(Datatype.Kind.BOOLEAN.getName())) {
                returnType = new BooleanType();
            } else if(typeName.equals(Datatype.Kind.BASE64_BINARY.getName())) {
                returnType = new Base64BinaryType();
            } else if(typeName.equals(Datatype.Kind.HEX_BINARY.getName())) {
                returnType = new HexBinaryType();
            } else if(typeName.equals(Datatype.Kind.FLOAT.getName())) {
                returnType = new FloatType();
            } else if(typeName.equals(Datatype.Kind.DOUBLE.getName())) {
                returnType = new DoubleType();
            } else if(typeName.equals(Datatype.Kind.ANYURI.getName())) {
                returnType = new AnyURIType();
            } else if(typeName.equals(Datatype.Kind.ANYTYPE.getName())) {
                returnType = new AnyType();
            } else if(typeName.equals(Datatype.Kind.QNAME.getName())) {
                returnType = new QNameType();
            } else if(typeName.equals(Datatype.Kind.NOTATION.getName())) {
                returnType = new NotationType();
            }
        }
        return returnType;
    }
    
    /**
     * Creates an AXI Datatype, given a schema component.
     */
    public Datatype getDatatype(AXIModel axiModel, SchemaComponent component) {
        DatatypeBuilder builder = new DatatypeBuilder(axiModel);
        return builder.getDatatype(component);
    }
    
    static GlobalSimpleType getPrimitiveType(String typeName){
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for(GlobalSimpleType ptype: primitives){
            if(ptype.getName().equals(typeName)){
                return ptype;
            }
        }
        return null;
    }
}
