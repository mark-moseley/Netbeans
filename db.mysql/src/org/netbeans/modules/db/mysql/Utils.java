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

package org.netbeans.modules.db.mysql;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Various utility methods
 * 
 * @author David Van Couvering
 */
public class Utils {
    private static Logger LOGGER = Logger.getLogger(Utils.class.getName());
    
    public static void displayError(String msg, Exception ex) {
        LOGGER.log(Level.INFO, msg, ex);
        
        String reason = ex.getMessage() != null ? ex.getMessage() : 
            NbBundle.getMessage(Utils.class, "MSG_SeeErrorLog");
        
        msg = msg + ": " + reason;
        
	NotifyDescriptor d = new NotifyDescriptor.Message(msg, 
                NotifyDescriptor.ERROR_MESSAGE);
        
	DialogDisplayer.getDefault().notify(d);        
    }
    
    
    /**
     * Return true if this is a valid directory
     * @param path path to validate
     * @param emptyOK set to true if an empty/null string is OK
     */
    public static boolean isValidDirectory(String path, boolean emptyOK) {
        return isValidPath(path, true, emptyOK);
    }
    
    /**
     * Return true if this is a valid executable file
     * @param path path to validate
     * @param emptyOK set to true if an empty/null string is OK
     */
    public static boolean isValidExecutable(String path, boolean emptyOK) {
        return isValidPath(path, false, emptyOK);
    }
    
    /** Return true if this is a valid, non-empty executable file */
    public static boolean isValidExecutable(String path) {
        return isValidExecutable(path, false);
    }

    private static boolean isValidPath(String path, boolean isDirectory, boolean emptyOK) {
        if ( isEmpty(path) ) {
            return emptyOK;
        }
        File file = new File(path).getAbsoluteFile();
        if ( ! file.exists() ) {
            return false;
        }
        
        return (isDirectory && file.isDirectory()) || 
                (!isDirectory && file.isFile()) ||
                (Utilities.isMac() && !isDirectory && path.endsWith(".app"));
    }
    
    /**
     * Return true if this is a valid URL
     * @param url url to validate
     * @param emptyOK set to true if an empty/null string is OK
     * @return
     */
    public static boolean isValidURL(String url, boolean emptyOK) {
        if ( isEmpty(url) ) {
            return emptyOK;
        }

        try {
            new URL(url);
        } catch (MalformedURLException ex) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isEmpty(String val) {
        return val == null || val.length() == 0;
    }
    
    /**
     * Pop up a confirmation dialog
     * 
     * @param message
     *      The message to display
     *  
     * @return true if the user pressed [OK], false if they pressed [CANCEL]
     */
    public static boolean displayConfirmDialog(String message) {
        NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                message, NotifyDescriptor.OK_CANCEL_OPTION);

        Object result = DialogDisplayer.getDefault().notify(ndesc);

        return ( result == NotifyDescriptor.OK_OPTION );
    }
    
    public static void displayErrorMessage(String message) {
        NotifyDescriptor ndesc = new NotifyDescriptor(
                message, 
                NbBundle.getMessage(Utils.class, "MSG_ErrorDialogTitle"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE, 
                new Object[] { NotifyDescriptor.OK_OPTION },
                NotifyDescriptor.OK_OPTION);

        DialogDisplayer.getDefault().notify(ndesc);
    }
    
    /**
     * See if two strings are equal, taking into account possibility of
     * null
     */
    public static boolean stringEquals(String str1, String str2) {
        return  (str1 == null && str2 == null) ||
                (str2 != null && str1 != null && str1.equals(str2));
    }
    
    /**
     * Take a byte array encoded in UTF-8 and return the string for it.
     * Can't say I understand this, this is cut-and-paste code.
     * 
     * @param bytes the UTF-8 encoded byte array
     * @return the decoded string
     */
    public static String decodeUTF8ByteArray(byte[] bytes) 
        throws CharacterCodingException {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder(); // NOI18N
        ByteBuffer input = ByteBuffer.wrap(bytes);
        int outputLength = (int)(bytes.length * (double)decoder.maxCharsPerByte());
        if (outputLength == 0) {
            return null; // NOI18N
        }
        char[] chars = new char[outputLength];
        CharBuffer output = CharBuffer.wrap(chars);
        CoderResult result = decoder.decode(input, output, true);
        if (!result.isError() && !result.isOverflow()) {
            result = decoder.flush(output);
        }
        if (result.isError() || result.isOverflow()) {
            throw new CharacterCodingException();
        } else {
            return new String(chars, 0, output.position());
        }
    }
}
