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
package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author mkuchtiak
 */
public class BookToolBarMVElement extends ToolBarMultiViewElement {
    private ToolBarDesignEditor comp;
    private SectionView view;
    private BookDataObject dObj;
    private PanelFactory factory;
    
    /** Creates a new instance of DesignMultiViewElement */
    public BookToolBarMVElement(BookDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        setVisualEditor(comp);
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        view=new BookView(dObj);
        comp.setContentView(view);
        try {
            view.openPanel(dObj.getBook());
        } catch(java.io.IOException ex){}
        view.checkValidity();
    }
    
    private class BookView extends SectionView {
        BookView(BookDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                Book book = dObj.getBook();
                Node bookNode = new BookNode(book);
                
                Chapter[] chapters = book.getChapter();
                Node[] chapterNode = new Node[chapters.length];
                Children ch = new Children.Array();
                for (int i=0;i<chapters.length;i++) {
                    chapterNode[i] = new ChapterNode(chapters[i]);
                }
                ch.add(chapterNode);
                Node chaptersNode = new SectionContainerNode(ch);
                chaptersNode.setDisplayName("Chapters");
                rootChildren.add(new Node[]{bookNode,chaptersNode});
                // add panels
                addSection(new SectionPanel(this,bookNode,book)); //NOI18N
                
                SectionContainer chaptersCont = new SectionContainer(this,chaptersNode,"Chapters");
                //jspPGCont.setHeaderActions(new javax.swing.Action[]{addAction});

                // creatings section panels for Chapters
                SectionPanel[] pan = new SectionPanel[chapters.length];
                for (int i=0;i<chapters.length;i++) {
                    pan[i] = new SectionPanel(this, chapterNode[i], chapters[i]);
                    //pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                    chaptersCont.addSection(pan[i]);
                }
                addSection(chaptersCont);
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid Book");
            }
            setRoot(root);
        }
        
        public Error validateView() {
            try {
                Book book = dObj.getBook();
                String title = book.getTitle();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(book,"title"); //NOI18N
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                Chapter[] chapters = book.getChapter();
                for (int i=0;i<chapters.length;i++) {
                    title = chapters[i].getTitle();
                    if (title==null || title.length()==0) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                        return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                    }
                    for (int j=0;j<chapters.length;j++) {
                        String tit = chapters[j].getTitle();
                        if (i!=j && title.equals(tit)) {
                            Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                            return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                        }
                    }
                }
            } catch (java.io.IOException ex){}
            return null;
        }
    }
    
    private class BookNode extends org.openide.nodes.AbstractNode {
        BookNode(Book book) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(book.getTitle());
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }    
    }
    private class ChapterNode extends org.openide.nodes.AbstractNode {
        ChapterNode(Chapter chapter) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(chapter.getTitle());
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }    
    }
    
}
