/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.parser;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

/**
 * This class provides a default implementation of the
 * {@link ErrorHandler} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class DefaultErrorHandler implements ErrorHandler {

    /**
     * The instance of this class.
     */
    public final static ErrorHandler INSTANCE = new DefaultErrorHandler();

    /**
     * This class does not need to be instantiated.
     */
    protected DefaultErrorHandler() {
    }

    /**
     * <b>SAC</b>: Implements {ErrorHandler#warning(CSSParseException)}.
     */
    public void warning(CSSParseException e) {
// <rave> #96870 At least provide logging of the problem.
//        // Do nothing
// ====
        Logger logger = getLogger();
        IllegalStateException ise = new IllegalStateException("Warning: " + e.getMessage(), e); // NOI18N
        logger.log(Level.FINE, null, ise);
// </rave>
    }

    /**
     * <b>SAC</b>: Implements {ErrorHandler#error(CSSParseException)}.
     */
    public void error(CSSParseException e) {
// <rave> #96870 At least provide logging of the problem.
//        // Do nothing
// ====
        Logger logger = getLogger();
        IllegalStateException ise = new IllegalStateException("Error: " + e.getMessage(), e); // NOI18N
        logger.log(Level.FINE, null, ise);
// </rave>
    }

    /**
     * <b>SAC</b>: Implements {ErrorHandler#fatalError(CSSParseException)}.
     */
    public void fatalError(CSSParseException e) {
        throw e;
    }
    
// <rave>
    private static Logger getLogger() {
        return Logger.getLogger(DefaultErrorHandler.class.getName());
    }
// </rave>
}
