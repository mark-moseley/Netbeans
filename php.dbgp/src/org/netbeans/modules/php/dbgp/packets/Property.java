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
package org.netbeans.modules.php.dbgp.packets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.w3c.dom.Node;

import sun.misc.BASE64Decoder;


/**
 * @author ads
 *
 */
public class Property extends BaseMessageChildElement {

    static final String         PROPERTY    ="property";     // NOI18N

    private static final String NUMCHILDREN = "numchildren";// NOI18N

    static final String         ENCODING    = "encoding";   // NOI18N

    private static final String KEY         = "key";        // NOI18N

    private static final String ADDRESS     = "address";    // NOI18N

    private static final String PAGESIZE    = "pagesize";   // NOI18N

    private static final String PAGE        = "page";       // NOI18N

    private static final String NAME        = "name";       // NOI18N     
    
    private static final String FULL_NAME   = "fullname";   // NOI18N
    
    private static final String TYPE        = "type";       // NOI18N
    
    private static final String CLASS_NAME  = "classname";  // NOI18N
    
    private static final String CONSTANT    = "constant";   // NOI18N
    
    private static final String CHILDREN    = "children";   // NOI18N
    
    private static final String FACET       = "facet";      // NOI18N
    
    static final         String SIZE        = "size";       // NOI18N
    
    Property( Node node ){
        super( node );
    }
    
    public String getName(){
        return getAttribute( NAME );
    }
    
    public void setName( String value ) {
        Node node = getNode().getAttributes().getNamedItem( NAME );
        if ( node == null ) {
            node = getNode().getOwnerDocument().createAttribute( NAME );
            getNode().appendChild(node);
        }
        node.setNodeValue(value );
    }
    
    public String getFullName(){
        return getAttribute( FULL_NAME );
    }
    
    public String getType(){
        return getAttribute( TYPE );
    }
    
    public String getClassName(){
        return getAttribute(  CLASS_NAME );
    }
    
    public boolean isConstant(){
        return getInt( CONSTANT ) >0;
    }
    
    public boolean hasChildren(){
        return getInt( CHILDREN ) >0;
    }
    
    public int getSize(){
        return getInt( SIZE );
    }
    
    public int getPage(){
        return getInt( PAGE );
    }
    
    public int getPageSize(){
        return getInt( PAGESIZE );
    }
    
    public int getAddress(){
        return getInt( ADDRESS );
    }
    
    public String getKey(){
        return getAttribute( KEY );
    }
    
    public String getFacet() {
        return getAttribute( FACET );
    }
    
    public Encoding getEncoding(){
        String enc = getAttribute( ENCODING );
        return Encoding.forString( enc );
    }
    
    public int getChildrenSize(){
        return getInt( NUMCHILDREN );
    }
    
    public List<Property> getChildren(){
        List<Node> nodes = getChildren( PROPERTY );
        List<Property> result = new ArrayList<Property>( nodes.size() );
        for (Node node : nodes) {
            result.add( new Property( node ) );
        }
        return result;
    }
    
    public byte[] getValue() throws UnsufficientValueException {
        String value = DbgpMessage.getNodeValue( getNode() );
        byte[] result = null;
        if ( value == null ){
            result =  new byte[0];
        }
        else {
            Encoding enc = getEncoding();
            if ( Encoding.NONE.equals( enc ) || enc == null ){
                try {
                    result = value.getBytes( DbgpMessage.ISO_CHARSET );
                }
                catch (UnsupportedEncodingException e) {
                    assert false;
                    result= new byte[0];
                }
            }
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                result = decoder.decodeBuffer( value );
            }
            catch( IOException e ){
                result = new byte[0];
            }
        }
        return getValue( result );
    }
    
    public String getStringValue() throws UnsufficientValueException {
        Encoding enc = getEncoding();
        if ( Encoding.BASE64.equals( enc )){
            return new String( getValue() );
        }
        String result =  DbgpMessage.getNodeValue( getNode() );
        try {
            if( result != null && 
                    result.getBytes( DbgpMessage.ISO_CHARSET ).length < getSize() ) 
            {
                throw new UnsufficientValueException();
            }
        }
        catch (UnsupportedEncodingException e) {
            assert false;
            return "";
        }
        return result;
    }
    
    public static boolean equals( Property one , Property two ) {
        if ( one == null ) {
            return two == null;
        }
        else {
            byte[] value;
            try {
                value = one.getValue();
            }
            catch (UnsufficientValueException e) {
                return false;
            }
            if ( two == null ) {
                return false;
            }
            byte[] secondValue;
            try {
                secondValue = two.getValue();
            }
            catch (UnsufficientValueException e) {
                return false;
            }
            return Arrays.equals(value, secondValue);
        }
    }
    
    private byte[] getValue( byte[] bytes ) throws UnsufficientValueException {
        if ( bytes.length >= getSize() ) {
            return bytes;
        }
        else {
            throw new UnsufficientValueException();
        }
    }
    
    public enum Encoding {
        BASE64,
        NONE;
        
        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString()
        {
            return super.toString().toLowerCase();
        }
        
        
        static Encoding forString( String str ){
            Encoding[] encodings = Encoding.values();
            for (Encoding encoding : encodings) {
                if( encoding.toString().equals( str )){
                    return encoding;
                }
            }
            return null;
        }
     }

}
