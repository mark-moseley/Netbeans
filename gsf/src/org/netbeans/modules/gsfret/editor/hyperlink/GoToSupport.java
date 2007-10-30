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
package org.netbeans.modules.gsfret.editor.hyperlink;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.gsf.DeclarationFinder;
import org.netbeans.api.gsf.DeclarationFinder.AlternativeLocation;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.Completable;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.gsf.GsfHtmlFormatter;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 */
public class GoToSupport {
    /** Jump straight to declarations */
    static final boolean IM_FEELING_LUCKY = Boolean.getBoolean("gsf.im_feeling_lucky");
    
    private GoToSupport() {
    }
    
    public static String getGoToElementTooltip(final Document doc, final int offset) {
        return perform(doc, offset, true);
    }

    public static String performGoTo(final Document doc, final int offset) {
        return perform(doc, offset, false);
    }
    
    private static String perform(final Document doc, final int offset, final boolean tooltip) {
        if (tooltip && PopupUtil.isPopupShowing()) {
            return null;
        }

        try {
            final FileObject fo = getFileObject(doc);

            if (fo == null) {
                return null;
            }

            Source js = Source.forFileObject(fo);
            final String[] result = new String[1];

            js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                        throws Exception {
                        if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                            return;
                        }

                        Language language = controller.getLanguage();

                        if (language != null) {
                            DeclarationFinder finder = language.getDeclarationFinder();

                            if (finder != null) {
                                // Isn't this a waste of time? Unused
                                getIdentifierSpan(doc, offset);

                                DeclarationLocation location =
                                    finder.findDeclaration(controller, offset);

                                if (tooltip) {
                                    Completable completer = language.getCompletionProvider();
                                    if (location != DeclarationLocation.NONE && completer != null) {
                                        Element element = location.getElement();
                                        if (element != null) {
                                            String documentation = completer.document(controller, element);
                                            if (documentation != null) {
                                                result[0] = "<html><body>" + documentation; // NOI18N
                                            }
                                        }
                                    }
                                    
                                    return;
                                } else if (location != DeclarationLocation.NONE) {
                                    URL url = location.getUrl();
                                    if (url != null) {
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                    } else {
                                        
                                        if (!IM_FEELING_LUCKY && location.getAlternativeLocations().size() > 0 &&
                                                !PopupUtil.isPopupShowing()) {
                                            // Many alternatives - pop up a dialog and make the user choose
                                            if (chooseAlternatives(doc, offset, location.getAlternativeLocations())) {
                                                return;
                                            }
                                        }
                                        
                                        UiUtils.open(location.getFileObject(), location.getOffset());

                                        String desc = "Description not yet implemented";
                                        result[0] = "<html><body>" + desc;
                                    }

                                    return;
                                }
                            }
                        }

                        Toolkit.getDefaultToolkit().beep();
                        result[0] = null;

                        return;
                    }
                }, true);

            return result[0];
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /** TODO - MOVE TO UTILITTY LIBRARY */
    private static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }
        
        return null;
    }
    
    private static boolean chooseAlternatives(Document doc, int offset, List<AlternativeLocation> alternatives) {
        Collections.sort(alternatives);
        
        // Prune results a bit
        int MAX_COUNT = 30; // Don't show more items than this
        String previous = "";
        GsfHtmlFormatter formatter = new GsfHtmlFormatter();
        int count = 0;
        List<AlternativeLocation> pruned = new ArrayList<AlternativeLocation>(alternatives.size());
        for (AlternativeLocation alt : alternatives) {
            String s = alt.getDisplayHtml(formatter);
            if (!s.equals(previous)) {
                pruned.add(alt);
                previous = s;
                count++;
                if (count == MAX_COUNT) {
                    break;
                }
            }
        }
        alternatives = pruned;
        if (alternatives.size() <= 1) {
            return false;
        }
        
        JTextComponent target = findEditor(doc);
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                Point point = new Point(rectangle.x, rectangle.y+rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);

                String caption = NbBundle.getMessage(GoToSupport.class, "ChooseDecl");
                PopupUtil.showPopup(new DeclarationPopup(caption, alternatives), caption, point.x, point.y, true, 0);

                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return false;
    }
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

        return (od != null) ? od.getPrimaryFile() : null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        return GoToSupport.getIdentifierSpan(doc, offset);
    }

    public static int[] getIdentifierSpan(Document doc, int offset) {
        FileObject fo = getFileObject(doc);

        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }

        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(fo.getMIMEType());

        if (language == null) {
            return null;
        }

        DeclarationFinder finder = language.getDeclarationFinder();

        if (finder == null) {
            return null;
        }

        OffsetRange range = finder.getReferenceSpan(doc, offset);
        if (range != OffsetRange.NONE) {
            return new int[] { range.getStart(), range.getEnd() };
        }
        
        return null;
    }
}
