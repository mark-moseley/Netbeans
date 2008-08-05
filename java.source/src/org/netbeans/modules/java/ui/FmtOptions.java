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
package org.netbeans.modules.java.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.source.CodeStyle;
import static org.netbeans.api.java.source.CodeStyle.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;

import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class FmtOptions {

    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String labelIndent = "labelIndent"; //NOI18N
    public static final String absoluteLabelIndent = "absoluteLabelIndent"; //NOI18N
    public static final String indentTopLevelClassMembers = "indentTopLevelClassMembers"; //NOI18N
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final String rightMargin = "rightMargin"; //NOI18N
    
    public static final String preferLongerNames = "preferLongerNames"; //NOI18N
    public static final String fieldNamePrefix = "fieldNamePrefix"; //NOI18N
    public static final String fieldNameSuffix = "fieldNameSuffix"; //NOI18N
    public static final String staticFieldNamePrefix = "staticFieldNamePrefix"; //NOI18N
    public static final String staticFieldNameSuffix = "staticFieldNameSuffix"; //NOI18N
    public static final String parameterNamePrefix = "parameterNamePrefix"; //NOI18N
    public static final String parameterNameSuffix = "parameterNameSuffix"; //NOI18N
    public static final String localVarNamePrefix = "localVarNamePrefix"; //NOI18N
    public static final String localVarNameSuffix = "localVarNameSuffix"; //NOI18N
    public static final String qualifyFieldAccess = "qualifyFieldAccess"; //NOI18N
    public static final String useIsForBooleanGetters = "useIsForBooleanGetters"; //NOI18N
    public static final String addOverrideAnnotation = "addOverrideAnnotation"; //NOI18N
    public static final String makeLocalVarsFinal = "makeLocalVarsFinal"; //NOI18N
    public static final String makeParametersFinal = "makeParametersFinal"; //NOI18N
    public static final String classMembersOrder = "classMembersOrder"; //NOI18N
    
    public static final String classDeclBracePlacement = "classDeclBracePlacement"; //NOI18N
    public static final String methodDeclBracePlacement = "methodDeclBracePlacement"; //NOI18N
    public static final String otherBracePlacement = "otherBracePlacement"; //NOI18N
    public static final String specialElseIf = "specialElseIf"; //NOI18N
    public static final String redundantIfBraces = "redundantIfBraces"; //NOI18N
    public static final String redundantForBraces = "redundantForBraces"; //NOI18N
    public static final String redundantWhileBraces = "redundantWhileBraces"; //NOI18N
    public static final String redundantDoWhileBraces = "redundantDoWhileBraces"; //NOI18N
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final String alignMultilineAnnotationArgs = "alignMultilineAnnotationArgs"; //NOI18N
    public static final String alignMultilineImplements = "alignMultilineImplements"; //NOI18N
    public static final String alignMultilineThrows = "alignMultilineThrows"; //NOI18N
    public static final String alignMultilineParenthesized = "alignMultilineParenthesized"; //NOI18N
    public static final String alignMultilineBinaryOp = "alignMultilineBinaryOp"; //NOI18N
    public static final String alignMultilineTernaryOp = "alignMultilineTernaryOp"; //NOI18N
    public static final String alignMultilineAssignment = "alignMultilineAssignment"; //NOI18N
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final String placeElseOnNewLine = "placeElseOnNewLine"; //NOI18N
    public static final String placeWhileOnNewLine = "placeWhileOnNewLine"; //NOI18N
    public static final String placeCatchOnNewLine = "placeCatchOnNewLine"; //NOI18N
    public static final String placeFinallyOnNewLine = "placeFinallyOnNewLine"; //NOI18N
    public static final String placeNewLineAfterModifiers = "placeNewLineAfterModifiers"; //NOI18N
    
    public static final String wrapExtendsImplementsKeyword = "wrapExtendsImplementsKeyword"; //NOI18N
    public static final String wrapExtendsImplementsList = "wrapExtendsImplementsList"; //NOI18N
    public static final String wrapMethodParams = "wrapMethodParams"; //NOI18N
    public static final String wrapThrowsKeyword = "wrapThrowsKeyword"; //NOI18N
    public static final String wrapThrowsList = "wrapThrowsList"; //NOI18N
    public static final String wrapMethodCallArgs = "wrapMethodCallArgs"; //NOI18N
    public static final String wrapAnnotationArgs = "wrapAnnotationArgs"; //NOI18N
    public static final String wrapChainedMethodCalls = "wrapChainedMethodCalls"; //NOI18N
    public static final String wrapArrayInit = "wrapArrayInit"; //NOI18N
    public static final String wrapFor = "wrapFor"; //NOI18N
    public static final String wrapForStatement = "wrapForStatement"; //NOI18N
    public static final String wrapIfStatement = "wrapIfStatement"; //NOI18N
    public static final String wrapWhileStatement = "wrapWhileStatement"; //NOI18N
    public static final String wrapDoWhileStatement = "wrapDoWhileStatement"; //NOI18N
    public static final String wrapAssert = "wrapAssert"; //NOI18N
    public static final String wrapEnumConstants = "wrapEnumConstants"; //NOI18N
    public static final String wrapAnnotations = "wrapAnnotations"; //NOI18N
    public static final String wrapBinaryOps = "wrapBinaryOps"; //NOI18N
    public static final String wrapTernaryOps = "wrapTernaryOps"; //NOI18N
    public static final String wrapAssignOps = "wrapAssignOps"; //NOI18N
    
    public static final String blankLinesBeforePackage = "blankLinesBeforePackage"; //NOI18N
    public static final String blankLinesAfterPackage = "blankLinesAfterPackage"; //NOI18N
    public static final String blankLinesBeforeImports = "blankLinesBeforeImports"; //NOI18N
    public static final String blankLinesAfterImports = "blankLinesAfterImports"; //NOI18N
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final String spaceBeforeFinally = "spaceBeforeFinally"; //NOI18N
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final String spaceBeforeSynchronizedParen = "spaceBeforeSynchronizedParen"; //NOI18N
    public static final String spaceBeforeAnnotationParen = "spaceBeforeAnnotationParen"; //NOI18N    
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final String spaceBeforeFinallyLeftBrace = "spaceBeforeFinallyLeftBrace"; //NOI18N
    public static final String spaceBeforeSynchronizedLeftBrace = "spaceBeforeSynchronizedLeftBrace"; //NOI18N
    public static final String spaceBeforeStaticInitLeftBrace = "spaceBeforeStaticInitLeftBrace"; //NOI18N
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final String spaceWithinSynchronizedParens = "spaceWithinSynchronizedParens"; //NOI18N
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final String spaceWithinAnnotationParens = "spaceWithinAnnotationParens"; //NOI18N
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final String spaceWithinArrayInitBrackets = "spaceWithinArrayInitBrackets"; //NOI18N
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
    
    public static final String useSingleClassImport = "useSingleClassImport"; //NOI18N
    public static final String useFQNs = "useFQNs"; //NOI18N
    public static final String countForUsingStarImport = "countForUsingStarImport"; //NOI18N
    public static final String countForUsingStaticStarImport = "countForUsingStaticStarImport"; //NOI18N
    public static final String packagesForStarImport = "packagesForStarImport"; //NOI18N
    public static final String importsOrder = "importsOrder"; //NOI18N
    
    public static CodeStyleProducer codeStyleProducer;
    
    static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    static final String DEFAULT_PROFILE = "default"; // NOI18N
    static final String PROJECT_PROFILE = "project"; // NOI18N
    static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    static final String usedProfile = "usedProfile"; // NOI18N
    
    private static final String JAVA = "text/x-java"; //NOI18N
    
    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }
    
    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }
        
    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    
    public static boolean getGlobalExpandTabToSpaces() {
        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.EXPAND_TABS, getDefaultAsBoolean(expandTabToSpaces));
    }
    
    public static int getGlobalTabSize() {
        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.TAB_SIZE, getDefaultAsInt(tabSize));
    }
    
    public static int getGlobalSpacesPerTab() {
        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, getDefaultAsInt(spacesPerTab));
    }

    public static int getGlobalIndentSize() {
        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
    }

    public static int getGlobalRightMargin() {
        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, getDefaultAsInt(rightMargin));
    }
    
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);
        
        try {
            Integer.parseInt(value);
            return true;            
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    public static Preferences getPreferences(Project project) {
        if (project != null) {
            Preferences root = ProjectUtils.getPreferences(project, IndentUtils.class, true).node(CODE_STYLE_PROFILE);
            String profile = root.get(usedProfile, DEFAULT_PROFILE);
            if (PROJECT_PROFILE.equals(profile))
                return root.node(PROJECT_PROFILE).node(JAVA_MIME_TYPE); //NOI18N
        }
        return MimeLookup.getLookup(JAVA_MIME_TYPE).lookup(Preferences.class);
    }

    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static final String WRAP_ALWAYS  = WrapStyle.WRAP_ALWAYS.name();
    private static final String WRAP_IF_LONG  = WrapStyle.WRAP_IF_LONG.name();
    private static final String WRAP_NEVER  = WrapStyle.WRAP_NEVER.name();
    
    private static final String BP_NEW_LINE = BracePlacement.NEW_LINE.name();
    private static final String BP_NEW_LINE_HALF_INDENTED = BracePlacement.NEW_LINE_HALF_INDENTED.name();
    private static final String BP_NEW_LINE_INDENTED = BracePlacement.NEW_LINE_INDENTED.name();
    private static final String BP_SAME_LINE = BracePlacement.SAME_LINE.name(); 
    
    private static final String BGS_ELIMINATE = BracesGenerationStyle.ELIMINATE.name(); 
    private static final String BGS_LEAVE_ALONE = BracesGenerationStyle.LEAVE_ALONE.name(); 
    private static final String BGS_GENERATE = BracesGenerationStyle.GENERATE.name(); 
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "4"}, //NOI18N
            { spacesPerTab, "4"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "8"}, //NOI18N
            { labelIndent, "0"}, //NOI18N
            { absoluteLabelIndent, FALSE}, //NOI18N
            { indentTopLevelClassMembers, TRUE}, //NOI18N
            { indentCasesFromSwitch, TRUE}, //NOI18N
            { rightMargin, "120"}, //NOI18N

            { preferLongerNames, TRUE}, //NOI18N
            { fieldNamePrefix, ""}, //NOI18N // XXX null
            { fieldNameSuffix, ""}, //NOI18N // XXX null
            { staticFieldNamePrefix, ""}, //NOI18N // XXX null
            { staticFieldNameSuffix, ""}, //NOI18N // XXX null
            { parameterNamePrefix, ""}, //NOI18N // XXX null
            { parameterNameSuffix, ""}, //NOI18N // XXX null
            { localVarNamePrefix, ""}, //NOI18N // XXX null
            { localVarNameSuffix, ""}, //NOI18N // XXX null
            { qualifyFieldAccess, FALSE}, //NOI18N // XXX
            { useIsForBooleanGetters, TRUE}, //NOI18N
            { addOverrideAnnotation, TRUE}, //NOI18N
            { makeLocalVarsFinal, FALSE}, //NOI18N
            { makeParametersFinal, FALSE}, //NOI18N
            { classMembersOrder, ""}, //NOI18N // XXX

            { classDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { methodDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { otherBracePlacement, BP_SAME_LINE}, //NOI18N
            { specialElseIf, TRUE}, //NOI18N
            { redundantIfBraces, BGS_GENERATE}, //NOI18N
            { redundantForBraces, BGS_GENERATE}, //NOI18N
            { redundantWhileBraces, BGS_GENERATE}, //NOI18N
            { redundantDoWhileBraces, BGS_GENERATE}, //NOI18N
            { alignMultilineMethodParams, FALSE}, //NOI18N
            { alignMultilineCallArgs, FALSE}, //NOI18N
            { alignMultilineAnnotationArgs, FALSE}, //NOI18N
            { alignMultilineImplements, FALSE}, //NOI18N
            { alignMultilineThrows, FALSE}, //NOI18N
            { alignMultilineParenthesized, FALSE}, //NOI18N
            { alignMultilineBinaryOp, FALSE}, //NOI18N
            { alignMultilineTernaryOp, FALSE}, //NOI18N
            { alignMultilineAssignment, FALSE}, //NOI18N
            { alignMultilineFor, FALSE}, //NOI18N
            { alignMultilineArrayInit, FALSE}, //NOI18N
            { placeElseOnNewLine, FALSE}, //NOI18N 
            { placeWhileOnNewLine, FALSE}, //NOI18N
            { placeCatchOnNewLine, FALSE}, //NOI18N 
            { placeFinallyOnNewLine, FALSE}, //NOI18N 
            { placeNewLineAfterModifiers, FALSE}, //NOI18N

            { wrapExtendsImplementsKeyword, WRAP_NEVER}, //NOI18N
            { wrapExtendsImplementsList, WRAP_NEVER}, //NOI18N
            { wrapMethodParams, WRAP_NEVER}, //NOI18N
            { wrapThrowsKeyword, WRAP_NEVER}, //NOI18N
            { wrapThrowsList, WRAP_NEVER}, //NOI18N
            { wrapMethodCallArgs, WRAP_NEVER}, //NOI18N
            { wrapAnnotationArgs, WRAP_NEVER}, //NOI18N
            { wrapChainedMethodCalls, WRAP_NEVER}, //NOI18N
            { wrapArrayInit, WRAP_NEVER}, //NOI18N
            { wrapFor, WRAP_NEVER}, //NOI18N
            { wrapForStatement, WRAP_ALWAYS}, //NOI18N
            { wrapIfStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapDoWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapAssert, WRAP_NEVER}, //NOI18N
            { wrapEnumConstants, WRAP_NEVER}, //NOI18N
            { wrapAnnotations, WRAP_ALWAYS}, //NOI18N
            { wrapBinaryOps, WRAP_NEVER}, //NOI18N
            { wrapTernaryOps, WRAP_NEVER}, //NOI18N
            { wrapAssignOps, WRAP_NEVER}, //NOI18N

            { blankLinesBeforePackage, "0"}, //NOI18N
            { blankLinesAfterPackage, "1"}, //NOI18N
            { blankLinesBeforeImports, "1"}, //NOI18N 
            { blankLinesAfterImports, "1"}, //NOI18N
            { blankLinesBeforeClass, "1"}, //NOI18N 
            { blankLinesAfterClass, "0"}, //NOI18N
            { blankLinesAfterClassHeader, "1"}, //NOI18N 
            { blankLinesBeforeFields, "0"}, //NOI18N 
            { blankLinesAfterFields, "0"}, //NOI18N
            { blankLinesBeforeMethods, "1"}, //NOI18N
            { blankLinesAfterMethods, "0"}, //NOI18N

            { spaceBeforeWhile, TRUE}, //NOI18N // XXX
            { spaceBeforeElse, TRUE}, //NOI18N // XXX
            { spaceBeforeCatch, TRUE}, //NOI18N // XXX
            { spaceBeforeFinally, TRUE}, //NOI18N // XXX
            { spaceBeforeMethodDeclParen, FALSE}, //NOI18N
            { spaceBeforeMethodCallParen, FALSE}, //NOI18N
            { spaceBeforeIfParen, TRUE}, //NOI18N
            { spaceBeforeForParen, TRUE}, //NOI18N
            { spaceBeforeWhileParen, TRUE}, //NOI18N
            { spaceBeforeCatchParen, TRUE}, //NOI18N
            { spaceBeforeSwitchParen, TRUE}, //NOI18N
            { spaceBeforeSynchronizedParen, TRUE}, //NOI18N
            { spaceBeforeAnnotationParen, FALSE}, //NOI18N    
            { spaceAroundUnaryOps, FALSE}, //NOI18N
            { spaceAroundBinaryOps, TRUE}, //NOI18N
            { spaceAroundTernaryOps, TRUE}, //NOI18N
            { spaceAroundAssignOps, TRUE}, //NOI18N
            { spaceBeforeClassDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeMethodDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeIfLeftBrace, TRUE}, //NOI18N
            { spaceBeforeElseLeftBrace, TRUE}, //NOI18N
            { spaceBeforeWhileLeftBrace, TRUE}, //NOI18N
            { spaceBeforeForLeftBrace, TRUE}, //NOI18N
            { spaceBeforeDoLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSwitchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeTryLeftBrace, TRUE}, //NOI18N
            { spaceBeforeCatchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeFinallyLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSynchronizedLeftBrace, TRUE}, //NOI18N
            { spaceBeforeStaticInitLeftBrace, TRUE}, //NOI18N
            { spaceBeforeArrayInitLeftBrace, FALSE}, //NOI18N
            { spaceWithinParens, FALSE}, //NOI18N
            { spaceWithinMethodDeclParens, FALSE}, //NOI18N
            { spaceWithinMethodCallParens, FALSE}, //NOI18N
            { spaceWithinIfParens, FALSE}, //NOI18N
            { spaceWithinForParens, FALSE}, //NOI18N
            { spaceWithinWhileParens, FALSE}, //NOI18N
            { spaceWithinSwitchParens, FALSE}, //NOI18N
            { spaceWithinCatchParens, FALSE}, //NOI18N
            { spaceWithinSynchronizedParens, FALSE}, //NOI18N
            { spaceWithinTypeCastParens, FALSE}, //NOI18N
            { spaceWithinAnnotationParens, FALSE}, //NOI18N
            { spaceWithinBraces, FALSE}, //NOI18N
            { spaceWithinArrayInitBrackets, FALSE}, //NOI18N
            { spaceBeforeComma, FALSE}, //NOI18N
            { spaceAfterComma, TRUE}, //NOI18N
            { spaceBeforeSemi, FALSE}, //NOI18N
            { spaceAfterSemi, TRUE}, //NOI18N
            { spaceBeforeColon, TRUE}, //NOI18N
            { spaceAfterColon, TRUE}, //NOI18N
            { spaceAfterTypeCast, TRUE}, //NOI18N

            { useSingleClassImport, TRUE}, //NOI18N
            { useFQNs, FALSE}, //NOI18N
            { countForUsingStarImport, "5"}, //NOI18N
            { countForUsingStaticStarImport, "3"}, //NOI18N // XXX
            { packagesForStarImport, ""}, //NOI18N // XXX
            { importsOrder, ""}, //NOI18N // XXX
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
 
    
    // Support section ---------------------------------------------------------
      
    public static class CategorySupport implements ActionListener, DocumentListener, PreviewProvider, PreferencesCustomizer {

        public static final String OPTION_ID = "org.netbeans.modules.java.ui.FormatingOptions.ID";
                
        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;
        
        private static final ComboItem  bracePlacement[] = new ComboItem[] {
                new ComboItem( BracePlacement.SAME_LINE.name(), "LBL_bp_SAME_LINE" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE.name(), "LBL_bp_NEW_LINE" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE_HALF_INDENTED.name(), "LBL_bp_NEW_LINE_HALF_INDENTED" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE_INDENTED.name(), "LBL_bp_NEW_LINE_INDENTED" ) // NOI18N
            };
        private static final ComboItem  bracesGeneration[] = new ComboItem[] {
                new ComboItem( BracesGenerationStyle.GENERATE.name(), "LBL_bg_GENERATE" ), // NOI18N
                new ComboItem( BracesGenerationStyle.LEAVE_ALONE.name(), "LBL_bg_LEAVE_ALONE" ), // NOI18N
                new ComboItem( BracesGenerationStyle.ELIMINATE.name(), "LBL_bg_ELIMINATE" ) // NOI18N       
            };
        
        private static final ComboItem  wrap[] = new ComboItem[] {
                new ComboItem( WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS" ), // NOI18N
                new ComboItem( WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG" ), // NOI18N
                new ComboItem( WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER" ) // NOI18N
            };
        
        private final String previewText;
//        private String forcedOptions[][];
        
//        private boolean changed = false;
//        private boolean loaded = false;
        private final String id;
        protected final JPanel panel;
        private final List<JComponent> components = new LinkedList<JComponent>();                
        private JEditorPane previewPane;
        
        private final Preferences preferences;
        private final Preferences previewPrefs;
    
        protected CategorySupport(Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
            this.preferences = preferences;
            this.id = id;
            this.panel = panel;
            this.previewText = previewText != null ? previewText : NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default"); //NOI18N

            // Scan the panel for its components
            scan(panel, components);

            // Initialize the preview preferences
            Preferences forcedPrefs = new PreviewPreferences();
            for (String[] option : forcedOptions) {
                forcedPrefs.put( option[0], option[1]);
            }
            this.previewPrefs = new ProxyPreferences(preferences, forcedPrefs);

            // Load and hook up all the components
            loadFrom(preferences);
            addListeners();
        }
        
        protected void addListeners() {
            scan(ADD_LISTENERS, null);
        }
        
        protected void loadFrom(Preferences preferences) {
//            loaded = true;
            scan(LOAD, preferences);
//            loaded = false;
        }
//
//        public void applyChanges() {
//            storeTo(preferences);
//        }
//
        protected void storeTo(Preferences p) {
            scan(STORE, p);
        }
        
        protected void notifyChanged() {
//            if (loaded)
//                return;
            storeTo(preferences);
            refreshPreview();
        }

        // ActionListener implementation ---------------------------------------
        
        public void actionPerformed(ActionEvent e) {
            notifyChanged();
        }
        
        // DocumentListener implementation -------------------------------------
        
        public void insertUpdate(DocumentEvent e) {
            notifyChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            notifyChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            notifyChanged();
        }

        // PreviewProvider methods -----------------------------------------------------
        
        public JComponent getPreviewComponent() {
            if (previewPane == null) {
                previewPane = new JEditorPane();
                previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtOptions.class, "AN_Preview")); //NOI18N
                previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtOptions.class, "AD_Preview")); //NOI18N
                previewPane.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
                previewPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-java"));
                previewPane.setEditable(false);
            }
            return previewPane;
        }

        public void refreshPreview() {
            JEditorPane jep = (JEditorPane) getPreviewComponent();
            try {
                int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
                jep.putClientProperty("TextLimitLine", rm); //NOI18N
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }
            try {
                Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }

            CodeStyle codeStyle = codeStyleProducer.create(previewPrefs);
            jep.setIgnoreRepaint(true);
            jep.setText(Reformatter.reformat(previewText, codeStyle));
            jep.setIgnoreRepaint(false);
            jep.scrollRectToVisible(new Rectangle(0,0,10,10) );
            jep.repaint(100);
        }

        // PreferencesCustomizer implementation --------------------------------
        
        public JComponent getComponent() {
            return panel;
        }

        public String getDisplayName() {
            return panel.getName();
        }

        public String getId() {
            return id;
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        // PreferencesCustomizer.Factory implementation ------------------------

        public static final class Factory implements PreferencesCustomizer.Factory {

            private final String id;
            private final Class<? extends JPanel> panelClass;
            private final String previewText;
            private final String[][] forcedOptions;

            public Factory(String id, Class<? extends JPanel> panelClass, String previewText, String[]... forcedOptions) {
                this.id = id;
                this.panelClass = panelClass;
                this.previewText = previewText;
                this.forcedOptions = forcedOptions;
            }

            public PreferencesCustomizer create(Preferences preferences) {
                try {
                    return new CategorySupport(preferences, id, panelClass.newInstance(), previewText, forcedOptions);
                } catch (Exception e) {
                    return null;
                }
            }
        } // End of CategorySupport.Factory class
        
        // Private methods -----------------------------------------------------

        private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
            switch(operation) {
            case LOAD:
                loadData(jc, optionID, p);
                break;
            case STORE:
                storeData(jc, optionID, p);
                break;
            case ADD_LISTENERS:
                addListener(jc);
                break;
            }
        }

        private void scan(int what, Preferences p ) {
            for (JComponent jc : components) {
                Object o = jc.getClientProperty(OPTION_ID);
                if (o instanceof String) {
                    performOperation(what, jc, (String)o, p);
                } else if (o instanceof String[]) {
                    for(String oid : (String[])o) {
                        performOperation(what, jc, oid, p);
                    }
                }
            }
        }

        private void scan(Container container, List<JComponent> components) {
            for (Component c : container.getComponents()) {
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if (o instanceof String || o instanceof String[])
                        components.add(jc);
                }                    
                if (c instanceof Container)
                    scan((Container)c, components);
            }
        }

        /** Very smart method which tries to set the values in the components correctly
         */ 
        private void loadData( JComponent jc, String optionID, Preferences node ) {
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;                
                field.setText( node.get(optionID, getDefaultAsString(optionID)) );
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected( node.getBoolean(optionID, df));                
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                String value = node.get(optionID, getDefaultAsString(optionID) );
                ComboBoxModel model = createModel(value);
                cb.setModel(model);
                ComboItem item = whichItem(value, model);
                cb.setSelectedItem(item);
            }
            
        }

        private void storeData( JComponent jc, String optionID, Preferences node ) {
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                
                String text = field.getText();
                
                // XXX test for numbers
                if ( isInteger(optionID) ) {
                    try {
                        int i = Integer.parseInt(text);                        
                    } catch (NumberFormatException e) {
                        return;
                    }
                }

                // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
                // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
                // which is why we must not remove them. Proper solution would be to
                // store formatting preferences to MimeLookup and not use NbPreferences.
                // The problem currently is that MimeLookup based Preferences do not support subnodes.
                if (!optionID.equals(tabSize) &&
                    !optionID.equals(spacesPerTab) && !optionID.equals(indentSize) &&
                    getDefaultAsString(optionID).equals(text)
                ) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, text);
                }
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == checkBox.isSelected())
                    node.remove(optionID);
                else
                    node.putBoolean(optionID, checkBox.isSelected());
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                // Logger.global.info( cb.getSelectedItem() + " " + optionID);
                String value = ((ComboItem) cb.getSelectedItem()).value;
                if (getDefaultAsString(optionID).equals(value))
                    node.remove(optionID);
                else
                    node.put(optionID,value);
            }         
        }
        
        private void addListener( JComponent jc ) {
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                checkBox.addActionListener(this);
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                cb.addActionListener(this);
            }         
        }
        
            
        private ComboBoxModel createModel( String value ) {
            
            // is it braces placement?            
            for (ComboItem comboItem : bracePlacement) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( bracePlacement );
                }
            }
            
            // is it braces generation?
            for (ComboItem comboItem : bracesGeneration) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( bracesGeneration );
                }
            }
            
            // is it wrap
            for (ComboItem comboItem : wrap) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( wrap );
                }
            }
            
            return null;
        }
        
        private static ComboItem whichItem(String value, ComboBoxModel model) {
            
            for (int i = 0; i < model.getSize(); i++) {
                ComboItem item = (ComboItem)model.getElementAt(i);
                if ( value.equals(item.value)) {
                    return item;
                }
            }    
            return null;
        }

        private static class ComboItem {
            
            String value;
            String displayName;

            public ComboItem(String value, String key) {
                this.value = value;
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }
            
        }
    }
   
    public static class PreviewPreferences extends AbstractPreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }
        
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // read-only, no subnodes
    public static final class ProxyPreferences extends AbstractPreferences {
        
        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }
        
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String getSpi(String key) {
            for(Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<String>();
            for(Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[ keys.size() ]);
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    } // End of ProxyPreferences class
    
    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );
    
    }
    
}
