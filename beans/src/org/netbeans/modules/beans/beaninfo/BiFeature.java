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

package org.netbeans.modules.beans.beaninfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.beans.EventSetPattern;
import org.netbeans.modules.beans.GenerateBeanException;
import org.netbeans.modules.beans.IdxPropertyPattern;
import org.netbeans.modules.beans.Pattern;
import org.netbeans.modules.beans.PatternAnalyser;
import org.netbeans.modules.beans.PropertyPattern;
import org.openide.nodes.Node;

/** The basic class representing features included in BeanInfo.
*
* @author Petr Hrebejk
*/
public abstract class BiFeature implements IconBases, Node.Cookie, Comparable {

    /** generated Serialized Version UID */
    //static final long serialVersionUID = -8680621542479107034L;

    // Function names for code generation and reconition
    private static final String TEXT_EXPERT = "setExpert"; // NOI18N
    private static final String TEXT_HIDDEN = "setHidden"; // NOI18N
    private static final String TEXT_PREFERRED = "setPreferred"; // NOI18N
    private static final String TEXT_DISPLAY_NAME = "setDisplayName"; // NOI18N
    private static final String TEXT_SHORT_DESCRIPTION = "setShortDescription"; // NOI18N


    // variables ..........................................................................
    private String displayName = null;
    private boolean expert = false;
    private boolean hidden = false;
    private String name = null;
    private boolean preferred  = false;
    private String shortDescription = null;
    private boolean included = true;

    private String brackets = "]"; // NOI18N
    private final BiAnalyser bia;
    /**
    * Creates empty BiFeature.
    */
    public BiFeature( Pattern pattern, BiAnalyser bia ) {
        this(pattern.getName(), bia);
    }

    BiFeature(String name, String displayName, BiAnalyser bia) {
        this.name = name;
        this.displayName = displayName;
        this.bia = bia;
    }

    protected BiFeature(BiAnalyser bia) {        
        this("beanDescriptor", bia);//NOI18N GenerateBeanInfoAction.getString("CTL_NODE_DescriptorDisplayName");
    }
    
    private BiFeature(String name, BiAnalyser bia) {
        this(name, null, bia);
    }

    abstract String getCreationString();

    protected final void setModified() {
        bia.setModified();
    }

    // Definition of properties ....................................................................

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        setModified();
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
        setModified();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        setModified();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
        setModified();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        setModified();
    }

    abstract String getBracketedName();

    String getBrackets(){
        return brackets;
    }
    
    void setBrackets(String brackets){
        this.brackets = brackets;
        setModified();
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
        setModified();
    }
    
    public String getToolTip() {
        return this.getName();
    }

    /** Generates collection of strings which customize the feature */
    List<String> getCustomizationStrings () {
        List<String> col = new ArrayList<String>();
        StringBuffer sb = new StringBuffer( 100 );

        if ( expert ) {
            sb.setLength( 0 );
            sb.append( TEXT_EXPERT ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( hidden ) {
            sb.setLength( 0 );
            sb.append( TEXT_HIDDEN ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( preferred ) {
            sb.setLength( 0 );
            sb.append( TEXT_PREFERRED ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( displayName != null && displayName.trim().length() > 0) {
            sb.setLength( 0 );
            sb.append( TEXT_DISPLAY_NAME ).append( " ( "); // NOI18N
            sb.append( displayName ).append( " )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( shortDescription != null && shortDescription.trim().length() > 0 ) {
            sb.setLength( 0 );
            sb.append( TEXT_SHORT_DESCRIPTION ).append( " ( "); // NOI18N
            sb.append( shortDescription ).append( " )" ); // NOI18N
            col.add( sb.toString() );
        }

        return col;
    }


    /** Analyzes the bean info code for all customizations */
    void analyzeCustomization ( Collection<String> code ) throws GenerateBeanException {
        setIncluded( false );
        
        Iterator<String> it = code.iterator();
        String n = getBracketedName();

        String stNew = n + "=new"; // NOI18N
        String stExpert = n + "." + TEXT_EXPERT; // NOI18N
        String stHidden = n + "." + TEXT_HIDDEN; // NOI18N
        String stPreferred = n + "." + TEXT_PREFERRED; // NOI18N
        String stDisplayName = n + "." + TEXT_DISPLAY_NAME; // NOI18N
        String stShortDescription = n + "." + TEXT_SHORT_DESCRIPTION; // NOI18N
        while( it.hasNext() ) {
            String statement = it.next();

            if ( statement.indexOf( stNew ) != -1 ) {
                setIncluded( true );
                analyzeCreationString( statement ); // Implemented in descendants
                continue;
            }
            if ( statement.indexOf( stExpert ) != -1 ) {
                this.setExpert( true );
                continue;
            }
            if ( statement.indexOf( stHidden ) != -1 ) {
                this.setHidden( true );
                continue;
            }
            if ( statement.indexOf( stPreferred ) != -1 ) {
                this.setPreferred( true );
                continue;
            }
            if ( statement.indexOf( stDisplayName ) != -1 ) {
                String param = BiAnalyser.getArgumentParameter( statement );
                
                if ( param != null )
                    this.setDisplayName( param );
                continue;
            }
            if ( statement.indexOf( stShortDescription ) != -1 ) {
                String param = BiAnalyser.getArgumentParameter( statement );
                
                if ( param != null )
                    this.setShortDescription( param );
                continue;
            }
            analyzeCustomizationString( statement ); // Implemented in descendants
        }
    }

    /** gets the current icon base for the feature */
    abstract String getIconBase( boolean defaultIcon );

    abstract void analyzeCreationString( String statement );
    abstract void analyzeCustomizationString( String statement );

    public static final class Descriptor extends BiFeature {
        ElementHandle<TypeElement> element;
        String customizer;
        private String beanName;

        Descriptor( TypeElement ce, BiAnalyser bia ) throws GenerateBeanException {
            super(bia);
            this.element = ElementHandle.create(ce);
            this.beanName = ce.getQualifiedName().toString();
        }

        /** Returns the call to constructor of PropertyDescriptor */
        String getCreationString () {
            StringBuilder sb = new StringBuilder( 100 );

            sb.append( "new BeanDescriptor  ( " ); // NOI18N
            sb.append( getBeanName() + ".class , " ); // NOI18N
            sb.append( String.valueOf(getCustomizer()) + " )"); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            //now there be no icon !!!
            //if( defaultIcon )
            //    return null;
            //else                
            //    return null;    // NOI18N
            return BIF_DESCRIPTOR; // NOI18N
        }
        
        void analyzeCustomizationString( String statement ) {
        }

        void analyzeCreationString( String statement ) {
            int beg = statement.indexOf( ',' );
            int end = statement.lastIndexOf( ')' );

            if ( beg != -1 && end != -1 && ( ++beg < end ) )
                setCustomizer( statement.substring( beg, end ) );
            else
                setCustomizer( null );
        }

        String getBracketedName() {
            return getName();
        }

        @Override
        String getBrackets(){
            return ""; // NOI18N
        }
        
        public String getCustomizer(){
            return customizer;
        }

        public void setCustomizer(String customizer){
            this.customizer = customizer;
            setModified();
        }
        
        //overrides BiFeature.isIncluded(), this property is always included ( disabled by setting get from Introspection )
        @Override
        public boolean isIncluded() {
            return true;
        }
        
        public String getBeanName() {
            return this.beanName;
        }
    }
    
    public static class Property extends BiFeature {

        private PropertyPattern pattern;

        private static final String TEXT_BOUND = "setBound"; // NOI18N
        private static final String TEXT_CONSTRAINED = "setConstrained"; // NOI18N
        private static final String TEXT_PROPERTY_EDITOR = "setPropertyEditorClass"; // NOI18N

        private boolean bound;
        private boolean constrained;
        private int mode;
        private String propertyEditorClass;

        private String declaringClassName;
        private String getterName;
        private String setterName;

        Property( PropertyPattern pp, CompilationInfo javac, BiAnalyser bia ) throws GenerateBeanException {
            super( pp, bia );
            mode = pp.getMode();
            pattern = pp;

            TypeElement declaringClass = pattern.getDeclaringClass().resolve(javac);
            declaringClassName = declaringClass.getQualifiedName().toString();
            ElementHandle<ExecutableElement> getterHandle = pattern.getGetterMethod();
            getterName = getterHandle == null? null: getterHandle.resolve(javac).getSimpleName().toString();
            ElementHandle<ExecutableElement> setterHandle = pattern.getSetterMethod();
            setterName = setterHandle == null? null: setterHandle.resolve(javac).getSimpleName().toString();
        }

        protected final String getDeclaringClassName() {
            return declaringClassName;
        }

        protected final String getGetterName() {
            return getterName;
        }

        protected final String getSetterName() {
            return setterName;
        }

        public boolean isBound() {
            return bound;
        }

        String getBracketedName() {
            return "[PROPERTY_" + getName() + "]"; // NOI18N
        }

        public void setBound(boolean bound) {
            this.bound = bound;
            setModified();
        }

        public boolean isConstrained() {
            return constrained;
        }

        public void setConstrained(boolean constrained) {
            this.constrained = constrained;
            setModified();
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
            setModified();
        }

        public boolean modeChangeable() {
            return pattern.getMode() == PropertyPattern.READ_WRITE;
        }

        public String getPropertyEditorClass() {
            return propertyEditorClass;
        }

        public void setPropertyEditorClass(String propertyEditorClass) {
            this.propertyEditorClass = propertyEditorClass;
            setModified();
        }

        /** Returns the call to constructor of PropertyDescriptor */
        String getCreationString () {
            StringBuilder sb = new StringBuilder( 100 );

            sb.append( "new PropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( declaringClassName + ".class, " ); // NOI18N

            if ( getterName != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + getterName + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( setterName != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + setterName + "\" )" ); // NOI18N
            else
                sb.append( "null )"); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) { 
                return BIF_PROPERTY_RW + "S"; // NOI18N
            }
            else {
                if ( mode == PropertyPattern.READ_ONLY )
                    return BIF_PROPERTY_RO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else if ( mode == PropertyPattern.WRITE_ONLY )
                    return BIF_PROPERTY_WO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_PROPERTY_RW + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        @Override
        List<String> getCustomizationStrings () {
            List<String> col = super.getCustomizationStrings();
            StringBuilder sb = new StringBuilder( 100 );

            if ( bound ) {
                sb.setLength( 0 );
                sb.append( TEXT_BOUND ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( constrained ) {
                sb.setLength( 0 );
                sb.append( TEXT_CONSTRAINED ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( propertyEditorClass != null && propertyEditorClass.trim().length() > 0 ) {
                sb.setLength( 0 );
                sb.append( TEXT_PROPERTY_EDITOR ).append( " ( "); // NOI18N
                sb.append( propertyEditorClass ).append( " )" ); // NOI18N
                col.add( sb.toString() );
            }

            return col;
        }

        void analyzeCustomizationString( String statement ) {
            String n = getBracketedName();
            String stBound = n + "." + TEXT_BOUND; // NOI18N
            String stConstrained = n + "." + TEXT_CONSTRAINED; // NOI18N
            String stPropertyEditor = n + "." + TEXT_PROPERTY_EDITOR; // NOI18N
            int peIndex;
            
            if ( statement.indexOf( stBound ) != -1 ) {
                setBound( true );
                return;
            }

            if ( statement.indexOf( stConstrained ) != -1 ) {
                setConstrained( true );
                return;
            }

            peIndex = statement.indexOf( stPropertyEditor );
            if ( peIndex != -1 ) {
                String paramString = statement.substring(peIndex + stPropertyEditor.length());
                String[] params = BiAnalyser.getParameters( paramString );
                if ( params.length > 0 )
                    setPropertyEditorClass( params[0] );
                return;
            }
        }

        void analyzeCreationString( String statement ) {

            String[] params = BiAnalyser.getParameters( statement );

            // Analyses if there is mode restriction in the existing BeanInfo
            if ( params.length == 4 && mode == PropertyPattern.READ_WRITE ) {
                if ( params[2].equals( "null" ) ) // NOI18N
                    mode = PropertyPattern.WRITE_ONLY;
                else if ( params[3].equals( "null" ) ) // NOI18N
                    mode = PropertyPattern.READ_ONLY;
            }
        }
    }

    public static final class IdxProperty extends Property {

        private boolean niGetter;
        private boolean niSetter;

        IdxPropertyPattern pattern;
        private String indexedGetterName;
        private String indexedSetterName;

        IdxProperty( IdxPropertyPattern pp, CompilationInfo javac, BiAnalyser bia ) throws GenerateBeanException {
            super( pp, javac, bia );
            pattern = pp;

            niGetter = hasNiGetter();
            niSetter = hasNiSetter();
            ElementHandle<ExecutableElement> indexedGetterHandle = pattern.getIndexedGetterMethod();
            indexedGetterName = indexedGetterHandle == null
                    ? null
                    : indexedGetterHandle.resolve(javac).getSimpleName().toString();
            ElementHandle<ExecutableElement> indexedSetterHandle = pattern.getIndexedSetterMethod();
            indexedSetterName = indexedSetterHandle == null
                    ? null
                    : indexedSetterHandle.resolve(javac).getSimpleName().toString();
        }

        boolean isNiGetter() {
            return niGetter;
        }

        void setNiGetter( boolean niGetter ) {
            this.niGetter = hasNiGetter() ? niGetter : false;
            setModified();
        }

        boolean isNiSetter() {
            return niSetter;
        }

        void setNiSetter( boolean niSetter ) {
            this.niSetter = hasNiSetter() ? niSetter : false;
            setModified();
        }


        boolean hasNiGetter() {
            return pattern.getGetterMethod() != null;
        }

        boolean hasNiSetter() {
            return pattern.getSetterMethod() != null;
        }

        /** Returns the call to constructor of IndexedPropertyDescriptor */
        @Override
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );

            sb.append( "new IndexedPropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( getDeclaringClassName() + ".class, " ); // NOI18N

            if ( getGetterName() != null && niGetter )
                sb.append( "\"" + getGetterName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( getSetterName() != null && niSetter )
                sb.append( "\"" + getSetterName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( indexedGetterName != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + indexedGetterName + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( indexedSetterName != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + indexedSetterName + "\" )" ); // NOI18N
            else
                sb.append( "null )"); // NOI18N

            return sb.toString();
        }

        @Override
        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) {
                return BIF_IDXPROPERTY_RW + "S"; // NOI18N
            }
            else {
                if ( getMode() == PropertyPattern.READ_ONLY )
                    return BIF_IDXPROPERTY_RO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else if ( getMode() == PropertyPattern.WRITE_ONLY )
                    return BIF_IDXPROPERTY_WO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_IDXPROPERTY_RW + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        @Override
        void analyzeCreationString( String statement ) {
            String[] params = BiAnalyser.getParameters( statement );

            // Analyses if there is mode restriction in the existing BeanInfo
            if ( params.length == 6 && getMode() == PropertyPattern.READ_WRITE ) {
                if ( params[4].equals( "null" ) ) // NOI18N
                    setMode( PropertyPattern.WRITE_ONLY );
                else if ( params[5].equals( "null" ) ) // NOI18N
                    setMode( PropertyPattern.READ_ONLY );

                // Analayses if there is restriction on non indexed getter or setter
                if ( hasNiGetter() && params[2].equals( "null" ) ) // NOI18N
                    niGetter = false;
                if ( hasNiGetter() && params[3].equals( "null" ) ) // NOI18N
                    niSetter = false;

            }
        }

    }

    public static final class EventSet extends BiFeature implements Comparator {

        EventSetPattern pattern;

        private static final String TEXT_UNICAST = "setUnicast"; // NOI18N
        private static final String TEXT_IN_DEFAULT = "setInDefaultEventSet"; // NOI18N

        private boolean isInDefaultEventSet = true;
        private String creationString;

        EventSet( EventSetPattern esp, CompilationInfo javac, BiAnalyser bia ) throws GenerateBeanException {
            super( esp, bia );
            pattern = esp;
            creationString = initCreationString(javac);
        }

        public boolean isUnicast() {
            return pattern.isUnicast();
        }

        public boolean isInDefaultEventSet() {
            return isInDefaultEventSet;
        }

        public void setInDefaultEventSet( boolean isInDefaultEventSet ) {
            this.isInDefaultEventSet = isInDefaultEventSet;
        }

        /**
         * MUST be consistent w/ generator in BiAnalyser.
         * @return
         */
        String getBracketedName() {
            return "[EVENT_" + getName() + "]"; // NOI18N
        }

        public int compare(Object o1, Object o2) {
            // XXX was used to sort listener methods in initCreationString
            throw new UnsupportedOperationException();
//            if (!(o1 instanceof org.netbeans.jmi.javamodel.Method) ||
//                    !(o2 instanceof org.netbeans.jmi.javamodel.Method))
//                throw new IllegalArgumentException();
//            org.netbeans.jmi.javamodel.Method m1 = (org.netbeans.jmi.javamodel.Method) o1;
//            org.netbeans.jmi.javamodel.Method m2 = (org.netbeans.jmi.javamodel.Method) o2;
//
//            return m1.getName().compareTo(m2.getName());
        }

        /** Returns the call to constructor of EventSetDescriptor */
        String getCreationString () {
            return creationString;
        }
        
        private String initCreationString (CompilationInfo javac) throws GenerateBeanException {
            String code = "new EventSetDescriptor ( %1$s.class, \"%2$s\", %3$s.class, new String[] {%4$s}, \"%5$s\", \"%6$s\" )"; // NOI18N
            String paramdelim = ", "; //NOI18N
            StringBuilder methodList = new StringBuilder();
            TypeMirror listenerType = pattern.getType().resolve(javac);
            TypeElement listener = (TypeElement) ((DeclaredType) listenerType).asElement();
            // is sorting necessary here?
            for (ExecutableElement me : ElementFilter.methodsIn(listener.getEnclosedElements())) {
                methodList.append(paramdelim).append('"').append(me.getSimpleName()).append('"');
            }

            return String.format(code,
                    pattern.getDeclaringClass().resolve(javac).getQualifiedName(),
                    pattern.getName(),
                    pattern.getType().resolve(javac).toString(), // XXX ???
                    methodList.length() == 0? methodList: methodList.substring(paramdelim.length()),
                    pattern.getAddListenerMethod().resolve(javac).getSimpleName(),
                    pattern.getRemoveListenerMethod().resolve(javac).getSimpleName()
                    );
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) { 
                if ( isUnicast() )
                    return BIF_EVENTSET_UNICAST + "S"; // NOI18N
                else
                    return BIF_EVENTSET_MULTICAST + "S"; // NOI18N
            }
            else {
                if ( isUnicast() )
                    return BIF_EVENTSET_UNICAST + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_EVENTSET_MULTICAST + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        @Override
        List<String> getCustomizationStrings () {
            List<String> col = super.getCustomizationStrings();
            StringBuilder sb = new StringBuilder( 100 );

            if ( isUnicast() ) {
                sb.setLength( 0 );
                sb.append( TEXT_UNICAST ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( !isInDefaultEventSet ) {
                sb.setLength( 0 );
                sb.append( TEXT_IN_DEFAULT ).append( " ( false )" ); // NOI18N
                col.add( sb.toString() );
            }

            return col;
        }

        void analyzeCustomizationString( String statement ) {
            String n = getBracketedName();
//            String stUnicast = new String( n + "."  + TEXT_UNICAST ); // NOI18N
            String stInDefault = n + "." + TEXT_IN_DEFAULT; // NOI18N
            /*
            if ( statement.indexOf( stUnicast ) != -1 ) {
              setUnicast( true );
              return;
        }
            */
            if ( statement.indexOf( stInDefault ) != -1 ) {
                setInDefaultEventSet( false );
                return;
            }
        }

        void analyzeCreationString( String statement ) {
        }

    }

    public static final class Method extends BiFeature {
        private ElementHandle<ExecutableElement> element;
        private String varName;
        private String toolTip;
        private String creationString;

        Method( ExecutableElement me, PatternAnalyser pa, CompilationInfo javac, BiAnalyser bia ) throws GenerateBeanException {
            super( me.getSimpleName().toString(), "\"\"", bia ); //NOI18N
            element = ElementHandle.create(me);
            toolTip = initToolTip(me, javac);
            creationString = initCreationString(me);
        }
        
        String getBracketedName() {
            return "[METHOD_" + getName() + "]"; // NOI18N
        }
        
        private static String getTypeClass(TypeMirror type) {
            TypeKind kind = type.getKind();
            if (kind.isPrimitive()) {
                return kind.name();
            } else if (kind == TypeKind.ARRAY) {
                return resolveArrayClass((ArrayType) type);
            } else if (kind == TypeKind.DECLARED) {
                return ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
            } else {
                throw new IllegalStateException("Unknown type" + type); // NOI18N
            }
        }

        private static String resolveArrayClass(ArrayType array) {
            TypeMirror type = array;
            StringBuilder dim = new StringBuilder();
            for (int i = 0; type.getKind() == TypeKind.ARRAY; i++) {
                type = ((ArrayType) type).getComponentType();
                dim.append("[]"); // NOI18N
            }
            
            return getTypeClass(type) + dim;
        }

        @Override
        public String getToolTip() {
            return this.toolTip;
        }
        
        private static String initToolTip(ExecutableElement element, CompilationInfo javac) {
            return ElementHeaders.getHeader(element, javac,
                    ElementHeaders.NAME + ElementHeaders.PARAMETERS);
        }
        
        ElementHandle<ExecutableElement> getElement() {
            return element;
        }
        
        // Returns the call to constructor of MethodDescriptor 
        String getCreationString () {
            return creationString;
        }
        
        private static String initCreationString (ExecutableElement element) {
            TypeElement enclClass = (TypeElement) element.getEnclosingElement();
            String code = "new MethodDescriptor(%1$s.class.getMethod(\"%2$s\", new Class[] {%3$s}))"; // NOI18N
            String paramdelim = ", "; //NOI18N
            StringBuilder sb = new StringBuilder();
            for (VariableElement param : element.getParameters()) {
                sb.append(paramdelim).append(getTypeClass(param.asType())).append(".class"); // NOI18N
            }

            return String.format(
                    code,
                    enclClass.getQualifiedName(),
                    element.getSimpleName(),
                    sb.length() == 0? sb: sb.substring(paramdelim.length())
                    );
        }
        
        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon )
                return BIF_METHOD + "S"; // NOI18N
            else
                return BIF_METHOD + (this.isIncluded() ? "S" : "N"); // NOI18N
        }
        
        void analyzeCustomizationString( String statement ) {
        }
        
        void analyzeCreationString( String statement ) {
        }
        
        /** Analyzes the bean info code for all customizations */
        @Override
        void analyzeCustomization ( Collection<String> code ) throws GenerateBeanException {
            if (element != null) {
                // find the method identifier
                String creation = (String) BiAnalyser.normalizeText(this.getCreationString()).toArray()[0];
                Iterator<String> it = code.iterator();
                int index;
                
                while( it.hasNext() ) {
                    String statement = (String) it.next();
                    if ((index = statement.indexOf(creation)) > -1) {
                        this.varName = statement.substring(statement.indexOf("methods[METHOD_") + 15, index - 2); // NOI18N
                        break;
                    }
                }
                
                element = null;
            }
            
            String realName = this.getName();
            this.setName(varName);
            super.analyzeCustomization(code);
            this.setName(realName);
        }
        
    }

    public int compareTo(Object other) {
        if (!(other instanceof BiFeature))
            return -1;
        BiFeature bf = (BiFeature)other;
        return getName().compareToIgnoreCase(bf.getName());
    }
}
