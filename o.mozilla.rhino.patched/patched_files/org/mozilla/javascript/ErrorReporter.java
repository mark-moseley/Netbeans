/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Norris Boyd
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

// API class

package org.mozilla.javascript;

/**
 * This is interface defines a protocol for the reporting of
 * errors during JavaScript translation or execution.
 *
 * @author Norris Boyd
 */

public interface ErrorReporter {

    /**
     * Report a warning.
     *
     * The implementing class may choose to ignore the warning
     * if it desires.
     *
     * @param message a String describing the warning
     * @param sourceName a String describing the JavaScript source
     * where the warning occured; typically a filename or URL
     * @param line the line number associated with the warning
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    void warning(String message, String sourceName, int line,
                 String lineSource, int lineOffset
                 // <netbeans>
                 // @param id A unique id for each type of error message,
                 //   which can be used to for example quickly dispatch
                 //   to error-specific handlers
                 // @param params A value or array of values that are specific
                 //   for each error.
                 , String id, Object params
                 // </netbeans>
                 );

    /**
     * Report an error.
     *
     * The implementing class is free to throw an exception if
     * it desires.
     *
     * If execution has not yet begun, the JavaScript engine is
     * free to find additional errors rather than terminating
     * the translation. It will not execute a script that had
     * errors, however.
     *
     * @param message a String describing the error
     * @param sourceName a String describing the JavaScript source
     * where the error occured; typically a filename or URL
     * @param line the line number associated with the error
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    void error(String message, String sourceName, int line,
               String lineSource, int lineOffset
                 // <netbeans>
                 // @param id A unique id for each type of error message,
                 //   which can be used to for example quickly dispatch
                 //   to error-specific handlers
                 // @param params A value or array of values that are specific
                 //   for each error.
                 , String id, Object params
                 // </netbeans>
               );

    /**
     * Creates an EvaluatorException that may be thrown.
     *
     * runtimeErrors, unlike errors, will always terminate the
     * current script.
     *
     * @param message a String describing the error
     * @param sourceName a String describing the JavaScript source
     * where the error occured; typically a filename or URL
     * @param line the line number associated with the error
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     * @return an EvaluatorException that will be thrown.
     */
    EvaluatorException runtimeError(String message, String sourceName,
                                    int line, String lineSource,
                                    int lineOffset);
}
