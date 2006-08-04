/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * InlineIncludeHint.java
 *
 * Created on August 2, 2005, 1:24 PM
 *
 */
package org.netbeans.modules.mobility.editor.hints;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.mdr.MDRepository;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.hints.spi.ChangeInfo;
import org.netbeans.modules.editor.hints.spi.Hint;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.mobility.antext.preprocessor.LineParserTokens;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.mobility.antext.preprocessor.PPToken;
import org.netbeans.modules.mobility.editor.actions.RecommentAction;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class ReplaceOldSyntaxHint extends Hint {
    
    final protected Document doc;
    final protected ArrayList<PPLine> lineList;
    protected PPBlockInfo block;
    
    /** Creates a new instance of InlineIncludeHint */
    public ReplaceOldSyntaxHint(final Document doc, final ArrayList<PPLine> lineList, final PPBlockInfo block) {
        this.doc = doc;
        this.lineList = lineList;
        this.block = block;
    }
    
    protected boolean isNegative(final List<PPToken> tokens) {
        return tokens.size() > 1 && tokens.get(1).getType() == LineParserTokens.OP_NOT;
    }
    
    protected List<String> extractAbilities(final List<PPToken> tokens) {
        final ArrayList<String> abilities = new ArrayList<String>();
        for ( final PPToken tok : tokens ) {
            if (tok.getType() == LineParserTokens.ABILITY) abilities.add(tok.getText());
        }
        return abilities;
    }
    
    public synchronized ChangeInfo implement() {
        final MDRepository rep = JavaModel.getJavaRepository();
        rep.beginTrans(false);
        try {
            NbDocument.runAtomic((StyledDocument)doc, new Runnable() {
                public void run() {
                    try {
                        final PPLine startLine  = lineList.get(block.getStartLine()-1);
                        final List<PPToken> tokens = startLine.getTokens();
                        boolean negative = isNegative(tokens);
                        final List<String> abilities = extractAbilities(tokens);
                        PPBlockInfo elseBlock = findContraryBlock(negative, abilities);
                        if (elseBlock != null && elseBlock.getStartLine() < block.getStartLine()) {
                            //else is before if -> exchange the blocks and switch the negation
                            final PPBlockInfo x = elseBlock;
                            elseBlock = block;
                            block = x;
                            negative ^= true;
                        }
                        if (elseBlock != null) {
                            doc.insertString(removeLine(doc, elseBlock.getEndLine()), "//#endif", null); //NOI18N
                            doc.insertString(removeLine(doc, elseBlock.getStartLine()), "//#else", null); //NOI18N
                            doc.remove(removeLine(doc, block.getEndLine()), 1);
                        } else {
                            doc.insertString(removeLine(doc, block.getEndLine()), "//#endif", null); //NOI18N
                        }
                        doc.insertString(removeLine(doc, block.getStartLine()), "//#if " + constructCondition(negative, abilities), null); //NOI18N
                    } catch (BadLocationException ble) {
                        ErrorManager.getDefault().notify(ble);
                    }
                    RecommentAction.actionPerformed(doc);
                }
            });
        } finally {
            rep.endTrans();
        }
        return null;
    }
    
    protected String constructCondition(final boolean negative, final List<String> abilities) {
        final StringBuffer sb = new StringBuffer();
        for ( String str : abilities ) {
            if (sb.length() > 0) sb.append(" || "); //NOI18N
            sb.append(str);
        }
        return negative ? "!(" + sb.toString() + ")" : sb.toString(); //NOI18N
    }
    
    protected int removeLine(final Document doc, final int line) throws BadLocationException {
        final int i = Utilities.getRowStartFromLineOffset((BaseDocument)doc, line - 1);
        doc.remove(i, Utilities.getRowEnd((BaseDocument)doc, i) - i);
        return i;
    }
    
    private PPBlockInfo checkContraryBlock(PPBlockInfo b, final boolean negative, final List<String> abilities) {
        while (b != null && b.getType() != PPLine.OLDIF) b = b.getParent();
        if (b != null && b.getType() == PPLine.OLDIF) {
            final PPLine startLine  = lineList.get(b.getStartLine()-1);
            final List<PPToken> tokens = startLine.getTokens();
            if (negative != isNegative(tokens)) {
                final List<String> abs = extractAbilities(tokens);
                if (abs.size() == abilities.size() && abs.containsAll(abilities)) {
                    return b;
                }
            }
        }
        return null;
    }
    
    protected PPBlockInfo findContraryBlock(final boolean negative, final List<String> abilities) {
        if (block.getStartLine() > 1) {
            final PPBlockInfo b = checkContraryBlock(lineList.get(block.getStartLine()-2).getBlock(), negative, abilities);
            if (b != null) return b;
        }
        if (block.getEndLine() < lineList.size()) {
            final PPBlockInfo b = checkContraryBlock(lineList.get(block.getEndLine()).getBlock(), negative, abilities);
            if (b != null) return b;
        }
        return null;
    }
    
    public int getType() {
        return SUGGESTION;
    }
    
    public String getText() {
        return NbBundle.getMessage(ReplaceOldSyntaxHint.class, "HintReplaceOldSyntax"); //NOI18N
    }
}
