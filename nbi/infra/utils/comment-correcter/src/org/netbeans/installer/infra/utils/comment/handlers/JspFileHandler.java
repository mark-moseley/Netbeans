/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.utils.comment.handlers;

import org.netbeans.installer.infra.utils.comment.utils.Utils;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link FileHandler} implementation capable of handling JSP documents.
 *
 * @author Kirill Sorokin
 */
public class JspFileHandler extends BlockFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link JspFileHandler}. The constuctor
     * simply falls back to the
     * {@link BlockFileHandler#BlockFileHandler(Pattern, String, String, String)}
     * passing in the parameters relevant to JSP files.
     */
    public JspFileHandler() {
        super(COMMENT_PATTERN,
                COMMENT_START,
                COMMENT_PREFIX,
                COMMENT_END);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(final File file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        
        if (!file.isFile()) {
            return false;
        }
        
        return file.getName().endsWith(".jsp") || // NOI18N
                file.getName().endsWith(".jspx"); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getCommentPosition() {
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        final Matcher matcher = COMMENT_POSITION_PATTERN.matcher(contents);
        if (matcher.find()) {
            return contents.indexOf(matcher.group(1));
        } else {
            return 0;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final Pattern COMMENT_POSITION_PATTERN = Pattern.compile(
            "\\A\\s*(?:<\\?xml.*?\\?>)?\\s*(.*)", // NOI18N
            Pattern.MULTILINE | Pattern.DOTALL);
    
    /**
     * The regular expression pattern which matches the initial comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "\\A\\s*(?:<\\?xml.*?\\?>)?\\s*(<%--.*?--%>\\s*\\n)", // NOI18N
            Pattern.MULTILINE | Pattern.DOTALL);
    
    /**
     * The comment opening string.
     */
    private static final String COMMENT_START =
            "<%--" + Utils.NL; // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX =
            "  "; // NOI18N
    
    /**
     * The comment closing string.
     */
    private static final String COMMENT_END =
            "--%>" + Utils.NL; // NOI18N
}
