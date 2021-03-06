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

package org.netbeans.lib.collab.util;

import java.io.*;
import java.util.*;


/**
 *
 * String Converter class for encoding Password in text files
 */
public class StringConverter {


    static public String encode(String pass, int encode_val){
        try {
            char pa[] = new char [pass.length()];
            pass.getChars(0, pass.length(), pa, 0);
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < pa.length; i++) {
                pa[i] = (char)(pa[i] ^ encode_val);
                buf.append(UnicodeFormatter.charToHex(pa[i]));
            } 
            
            return buf.toString();
        } catch (Exception e) {        
            System.out.println("encode:"+e);
        }
        return "";
    }
    
    
   
    //encode val can be 0x00be
    static public String decode(String pass, int encode_val){
        StringBuffer buf = new StringBuffer(pass);
        try {
            StringBuffer ret = new StringBuffer();
            for(int x = 0; x < buf.length(); x += 4){
                String tmp = buf.substring(x, x+4);    
                int i = Integer.parseInt(tmp, 16);
                ret.append(String.valueOf((char)(i ^ encode_val)));
            }
            return ret.toString();            
        } catch (Exception e) {        
            System.out.println("decode:"+e);
        }
        return "";
    }
    
    

}

