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
package org.netbeans.test.editor.app.gui;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import java.util.Vector;
import java.io.File;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.test.editor.app.Main;

import org.netbeans.test.editor.app.core.*;
import org.netbeans.test.editor.app.core.Test;
import org.netbeans.test.editor.app.gui.QuestionDialog;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;
import org.netbeans.test.editor.app.tests.GenerateTests;
import org.netbeans.test.editor.app.util.Scheduler;

/**
 *
 *
 *
 * @author  ehucka
 *
 * @version
 *
 */

public class TestEditorFrame extends javax.swing.JFrame {
    
    private static String TITLE="Editor Test Application";
    
    private TestNodeDelegate root;
    
    private Test test;
    
    private TreeDialog tree=null;
    
    File currentDirectory;
    
    String oldpath,oldpackage;
    
    /** Creates new form TestEditorFrame */
    
    public TestEditorFrame() {
        initComponents();
        pack();
        this.setSize(800,500);
        this.setLocation(300,100);
        oldpath="";
        oldpackage="";
        //        currentDirectory=new File(System.getProperty("user.dir"));
        currentDirectory=new File("/nbcvs/nball/editor/test/qa-functional/src/org/netbeans/test/editor/app");
    }
    
    public EventLoggingEditorPane getEditor() {
        return (EventLoggingEditorPane)editor;
    }
    
    public void setTest(Test test) {
        this.test=test;
        root = (TestNodeDelegate)test.getNodeDelegate();
        if (tree != null) {
            tree.setRootContext(root);
        }
    }
    
    public void newRootWindow() {
        if (tree == null) {
            tree=new TreeDialog(this);
            tree.setRootContext(root);
            addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    if (tree != null) {
                        tree.setLocation(getX()-tree.getWidth(), getY());
                    }
                }
                
                public void componentResized(ComponentEvent e) {
                    if (tree != null) {
                        tree.setSize(tree.getWidth(),getHeight());
                        tree.setLocation(getX()-tree.getWidth(), getY());
                    }
                }
            });
            viewExplorerM.setEnabled(false);
            tree.addWindowListener(new WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    viewExplorerM.setEnabled(true);
                    tree=null;
                }
            });
            tree.show();
        }
    }
    
    public Test getTest() {
        return test;
    }
    
    public TestNodeDelegate getRootNode() {
        return root;
    }
    
    /** This method is called from within the constructor to
     *
     * initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is
     *
     * always regenerated by the FormEditor.
     *
     */
    
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        editor = new EventLoggingEditorPane();
        jPanel4 = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        history = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        viewExplorerM = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Editor Test Application");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                TestEditorFrame.this.exitForm(evt);
            }
        });

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(new javax.swing.border.TitledBorder("Editor"));
        jPanel2.setMinimumSize(new java.awt.Dimension(300, 200));
        jPanel2.setPreferredSize(new java.awt.Dimension(300, 200));
        jScrollPane3.setAutoscrolls(true);
        editor.setMinimumSize(new java.awt.Dimension(200, 100));
        editor.setPreferredSize(new java.awt.Dimension(6, 23));
        jScrollPane3.setViewportView(editor);

        jPanel2.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(new javax.swing.border.TitledBorder("History"));
        jPanel4.setMinimumSize(new java.awt.Dimension(500, 75));
        jPanel4.setPreferredSize(new java.awt.Dimension(500, 75));
        historyScrollPane.setViewportView(history);

        jPanel4.add(historyScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel4, gridBagConstraints);

        jMenu1.setText("Test");
        jMenuItem1.setText("New");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.newTest(evt);
            }
        });

        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Open");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.openTest(evt);
            }
        });

        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Save");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.saveTest(evt);
            }
        });

        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Save as ...");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.saveAsTest(evt);
            }
        });

        jMenu1.add(jMenuItem4);

        jMenu1.add(jSeparator1);

        jMenuItem5.setText("Exit");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.exitTest(evt);
            }
        });

        jMenu1.add(jMenuItem5);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Tools");
        jMenuItem6.setText("Generate Test");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.generateTest(evt);
            }
        });

        jMenu3.add(jMenuItem6);

        jMenu3.add(jSeparator2);

        jMenuItem7.setText("Actions");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.testThreads(evt);
            }
        });

        jMenu3.add(jMenuItem7);

        jMenuBar1.add(jMenu3);

        jMenu2.setText("View");
        viewExplorerM.setText("Explorer");
        viewExplorerM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestEditorFrame.this.viewExplorer(evt);
            }
        });

        jMenu2.add(viewExplorerM);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents
    
    private void testThreads(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testThreads
        // Add your handling code here:
        try {
            java.io.PrintWriter pw=new java.io.PrintWriter(new java.io.FileWriter("/tmp/actions.lst"));
            TestLogAction tl=new TestLogAction(1);
            String[] keymap=tl.getKeyMaps();
            for (int i=0;i < keymap.length;i++) {
                pw.println("        <TestLogAction Name=\""+keymap[i]+"\" Command=\"\" />");
            }
            pw.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_testThreads
    
    private void generateTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateTest
        // Add your handling code here:
        if (Main.getFileName() == null) {
            System.err.println("You have not specify xml file of the Test.");
            return;
        }
        TestGenerateDialog dlg = new TestGenerateDialog(this,currentDirectory.getAbsolutePath());
        if (oldpath.length() > 0) {
            dlg.setPath(oldpath);
        }
        if (oldpackage.length() > 0) {
            dlg.setPackageName(oldpackage);
        }
        dlg.show();
        if (dlg.getState()) {
            new Thread() {
                String path,pack;
                
                public void start(String pth,String pck) {
                    path=pth;
                    pack=pck;
                    oldpath=path;
                    oldpackage=pack;
                    start();
                }
                
                public void run() {
                    try {
                        GenerateTests.generateTest(test, Main.getFileName(), path, pack);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start(dlg.getPath(),dlg.getPackageName());
        }
    }//GEN-LAST:event_generateTest
    
    private void viewExplorer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewExplorer
        // Add your handling code here:
        newRootWindow();
    }//GEN-LAST:event_viewExplorer
    
  private void exitTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitTest
      // Add your handling code here:
      exitForm(null);
  }//GEN-LAST:event_exitTest
  
  private void saveAsTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsTest
      // Add your handling code here:
      String dlgFile;
      
      if ((dlgFile = fileDlg(false)) != null) {
          new Thread() {
              private String file;
              
              public void start(String afile) {
                  file = afile;
                  start();
              }
              
              public void run() {
                  Main.saveAsTest(file);
              }
          }.start(dlgFile);
      }
  }//GEN-LAST:event_saveAsTest
  
  private void saveTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTest
      // Add your handling code here:
      if (Main.isNoname()) {
          saveAsTest(null);
      }
      
      new Thread() {
          public void run() {
              Main.saveTest();
          }
      }.start();
  }//GEN-LAST:event_saveTest
  
  private void openTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTest
      // Add your handling code here:
      String dlgFile;
      
      if ((dlgFile = fileDlg(true)) != null) {
          new Thread() {
              
              private String file;
              
              public void start(String afile) {
                  file = afile;
                  start();
              }
              public void run() {
                  Main.openTest(file);
              }
          }.start(dlgFile);
      }
  }//GEN-LAST:event_openTest
  
  private void newTest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTest
      // Add your handling code here:
      if (!Main.newTest()) {
          if (quest("Test was modified. Save it?")) {
              saveTest(null);
          }
      }
      Main.newTest();
  }//GEN-LAST:event_newTest
  /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (Main.isChanged()) {
            if (quest("Test is modified. Save it?")) {
                saveTest(null);
            }
        }
        
        if (tree != null) {
            tree.close();
        }
        Main.finish();  //I think, it's useless now.
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    public void killFrame() {
        setVisible(false);
    }
    
    public boolean quest(String question) {
        QuestionDialog dlg=new QuestionDialog(this,question);
        dlg.show();
        return dlg.getAnswer();
    }
    
    public String fileDlg(boolean open) {
        
        JFileChooser fch;
        File file = null;
        System.err.println(currentDirectory);
        fch=new JFileChooser(currentDirectory);
        fch.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String s=f.getName().substring(f.getName().lastIndexOf('.')+1).toLowerCase();
                if (f.isDirectory() || s.compareTo("xml") == 0) {
                    return true;
                }
                return false;
            }
            
            public String getDescription() {
                return "XML Test files";
            }
        });
        if (open) {
            fch.setDialogTitle("Open Test ...");
            if (fch.showOpenDialog(fch) == JFileChooser.APPROVE_OPTION)
                if (!(file = fch.getSelectedFile()).isFile())
                    return null;
        } else {
            fch.setDialogTitle("Save Test As ...");
            if (fch.showSaveDialog(fch) == JFileChooser.APPROVE_OPTION) {
                file = fch.getSelectedFile();
            }
        }
        if (file == null)
            return null;
        else
            currentDirectory=file.getParentFile();
        return file.getAbsolutePath();
    }
    
    public void appendHistory(String text) {
        history.append(text);
        history.repaint();
        JScrollBar bar=historyScrollPane.getVerticalScrollBar();
        if (bar != null) {
            bar.setValue(bar.getMaximum());
        }
    }
/*
    private int horizontalScrollBarPolicy() {
        return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS/;
    }
 
    private int verticalScrollBarPolicy() {
        return ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
    }
 */
    /** Getter for property tree.
     * @return Value of property tree.
     *
     */
    public TreeDialog getTree() {
        return tree;
    }
    
    public void setTitleFileName(String name) {
        setTitle(TITLE+" ["+name+"]");
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem viewExplorerM;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JEditorPane editor;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JTextArea history;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuBar jMenuBar1;
    // End of variables declaration//GEN-END:variables
    
}

