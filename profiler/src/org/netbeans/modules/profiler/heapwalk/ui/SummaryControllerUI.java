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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.util.Enumeration;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.heapwalk.SummaryController;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.StringWriter;
import java.lang.Thread.State;
import java.net.URL;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.lib.profiler.heap.ThreadObjectGCRoot;
import org.netbeans.modules.profiler.utils.GoToSourceHelper;
import org.netbeans.modules.profiler.utils.JavaSourceLocation;

/**
 *
 * @author Jiri Sedlacek
 */
public class SummaryControllerUI extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    // --- Presenter -------------------------------------------------------------
    private static class Presenter extends JToggleButton {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static ImageIcon ICON_INFO = ImageUtilities.loadImageIcon("org/netbeans/modules/profiler/resources/infoTab.png", false); // NOI18N

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Presenter() {
            super();
            setText(VIEW_TITLE);
            setToolTipText(VIEW_DESCR);
            setIcon(ICON_INFO);
            setMargin(new java.awt.Insets(getMargin().top, getMargin().top, getMargin().bottom, getMargin().top));
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String SHOW_SYSPROPS_URL = "file:/sysprops"; // NOI18N
    private static final String SHOW_THREADS_URL = "file:/threads"; // NOI18N
    private static final String OPEN_THREADS_URL = "file:/stackframe/";     // NOI18N


    // -----
    // I18N String constants
    private static final String VIEW_TITLE = NbBundle.getMessage(SummaryControllerUI.class, "SummaryControllerUI_ViewTitle"); // NOI18N
    private static final String VIEW_DESCR = NbBundle.getMessage(SummaryControllerUI.class, "SummaryControllerUI_ViewDescr"); // NOI18N
    private static final String IN_PROGRESS_MSG = NbBundle.getMessage(SummaryControllerUI.class,
                                                                      "SummaryControllerUI_InProgressMsg"); // NOI18N
    private static final String NOT_AVAILABLE_MSG = NbBundle.getMessage(SummaryControllerUI.class,
                                                                        "SummaryControllerUI_NotAvailableMsg"); // NOI18N
    private static final String SYSTEM_PROPERTIES_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                               "SummaryControllerUI_SystemPropertiesString"); // NOI18N
    private static final String SUMMARY_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                     "SummaryControllerUI_SummaryString"); // NOI18N
    private static final String ENVIRONMENT_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                         "SummaryControllerUI_EnvironmentString"); // NOI18N
    private static final String FILE_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                       "SummaryControllerUI_FileItemString"); // NOI18N
    private static final String FILE_SIZE_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                            "SummaryControllerUI_FileSizeItemString"); // NOI18N
    private static final String DATE_TAKEN_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                             "SummaryControllerUI_DateTakenItemString"); // NOI18N
    private static final String TOTAL_BYTES_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                              "SummaryControllerUI_TotalBytesItemString"); // NOI18N
    private static final String TOTAL_CLASSES_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                                "SummaryControllerUI_TotalClassesItemString"); // NOI18N
    private static final String TOTAL_INSTANCES_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                                  "SummaryControllerUI_TotalInstancesItemString"); // NOI18N
    private static final String CLASSLOADERS_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                               "SummaryControllerUI_ClassloadersItemString"); // NOI18N
    private static final String GCROOTS_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                          "SummaryControllerUI_GcRootsItemString"); // NOI18N
    private static final String FINALIZERS_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                          "SummaryControllerUI_FinalizersItemString"); // NOI18N
    private static final String OS_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class, "SummaryControllerUI_OsItemString"); // NOI18N
    private static final String ARCHITECTURE_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                               "SummaryControllerUI_ArchitectureItemString"); // NOI18N
    private static final String JAVA_HOME_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                            "SummaryControllerUI_JavaHomeItemString"); // NOI18N
    private static final String JVM_ITEM_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                      "SummaryControllerUI_JvmItemString"); // NOI18N
    private static final String SHOW_SYSPROPS_LINK_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                                "SummaryControllerUI_ShowSysPropsLinkString"); // NOI18N
    private static final String THREADS_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                               "SummaryControllerUI_ThreadsString"); // NOI18N
    private static final String SHOW_THREADS_LINK_STRING = NbBundle.getMessage(SummaryControllerUI.class,
                                                                                "SummaryControllerUI_ShowThreadsLinkString"); // NOI18N
                                                                                                                               // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton presenter;
    private HTMLTextArea dataArea;

    // --- UI definition ---------------------------------------------------------
    private Properties systemProperties;
    private SummaryController summaryController;

    // --- Private implementation ------------------------------------------------
    private boolean systemPropertiesComputed = false;
    private boolean showSysprops = false;
    private boolean showThreads = false;
    private String stackTrace;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public SummaryControllerUI(SummaryController summaryController) {
        this.summaryController = summaryController;

        initComponents();
        refreshSummary();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = new Presenter();
        }

        return presenter;
    }

    private Properties getSystemProperties() {
        if (!systemPropertiesComputed) {
            systemProperties = summaryController.getHeapFragmentWalker().getHeapFragment().getSystemProperties();
            systemPropertiesComputed = true;
        }

        return systemProperties;
    }

    private String computeEnvironment() {
        Properties sysprops = getSystemProperties();

        if (sysprops == null) {
            return NOT_AVAILABLE_MSG;
        }

        String patchLevel = sysprops.getProperty("sun.os.patch.level", ""); // NOI18N
        String os = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                    + MessageFormat.format(OS_ITEM_STRING,
                                           new Object[] {
                                               sysprops.getProperty("os.name", NOT_AVAILABLE_MSG), // NOI18N
                                               sysprops.getProperty("os.version", ""), // NOI18N
                                               ("unknown".equals(patchLevel) ? "" : patchLevel) // NOI18N
                                           });

        String arch = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                      + MessageFormat.format(ARCHITECTURE_ITEM_STRING,
                                             new Object[] {
                                                 sysprops.getProperty("os.arch", NOT_AVAILABLE_MSG), // NOI18N
                                                 sysprops.getProperty("sun.arch.data.model", "?") + "bit" // NOI18N
                                             });

        String jdk = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                     + MessageFormat.format(JAVA_HOME_ITEM_STRING,
                                            new Object[] { sysprops.getProperty("java.home", NOT_AVAILABLE_MSG) }); // NOI18N

        String jvm = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                     + MessageFormat.format(JVM_ITEM_STRING,
                                            new Object[] {
                                                sysprops.getProperty("java.vm.name", NOT_AVAILABLE_MSG), // NOI18N
                                                sysprops.getProperty("java.vm.version", ""), // NOI18N
                                                sysprops.getProperty("java.vm.info", "") // NOI18N
                                            });

        return "<b><img border='0' align='bottom' src='nbresloc:/org/netbeans/modules/profiler/heapwalk/ui/resources/sysinfo.png'>&nbsp;&nbsp;" // NOI18N
               + ENVIRONMENT_STRING + "</b><br><hr>" + os + "<br>" + arch + "<br>" + jdk + "<br>" + jvm; // NOI18N
    }

    private String computeSummary() {
        File file = summaryController.getHeapFragmentWalker().getHeapDumpFile();
        Heap heap = summaryController.getHeapFragmentWalker().getHeapFragment();
        HeapSummary hsummary = heap.getSummary();
        long finalizers = computeFinalizers(heap);
        int nclassloaders = 0;
        JavaClass cl = heap.getJavaClassByName("java.lang.ClassLoader"); // NOI18N

        if (cl != null) {
            nclassloaders = cl.getInstancesCount();

            Collection<JavaClass> jcs = cl.getSubClasses();

            for (JavaClass jc : jcs) {
                nclassloaders += jc.getInstancesCount();
            }
        }
        NumberFormat numberFormat = (NumberFormat)NumberFormat.getInstance().clone();
        numberFormat.setMaximumFractionDigits(1);
        
        String filename = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                          + MessageFormat.format(FILE_ITEM_STRING,
                                                 new Object[] {
                                                     file != null && file.exists() ? file.getAbsolutePath() : NOT_AVAILABLE_MSG
                                                 });

        String filesize = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                          + MessageFormat.format(FILE_SIZE_ITEM_STRING,
                                                 new Object[] {
                                                     file != null && file.exists() ? 
                                                         numberFormat.format(file.length()/(1024 * 1024.0)) + " MB" : // NOI18N
                                                         NOT_AVAILABLE_MSG
                                                 });

        String dateTaken = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                           + MessageFormat.format(DATE_TAKEN_ITEM_STRING, new Object[] { new Date(hsummary.getTime()).toString() });

        String liveBytes = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                           + MessageFormat.format(TOTAL_BYTES_ITEM_STRING,
                                                  new Object[] { numberFormat.format(hsummary.getTotalLiveBytes()) });

        String liveClasses = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                             + MessageFormat.format(TOTAL_CLASSES_ITEM_STRING,
                                                    new Object[] { numberFormat.format(heap.getAllClasses().size()) });

        String liveInstances = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                               + MessageFormat.format(TOTAL_INSTANCES_ITEM_STRING,
                                                      new Object[] {
                                                          numberFormat.format(hsummary.getTotalLiveInstances())
                                                      });

        String classloaders = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                              + MessageFormat.format(CLASSLOADERS_ITEM_STRING,
                                                     new Object[] { numberFormat.format(nclassloaders) });

        String gcroots = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                         + MessageFormat.format(GCROOTS_ITEM_STRING,
                                                new Object[] { numberFormat.format(heap.getGCRoots().size()) });

        String finalizersInfo = "&nbsp;&nbsp;&nbsp;&nbsp;" // NOI18N
                         + MessageFormat.format(FINALIZERS_ITEM_STRING,
                                                new Object[] { numberFormat.format(finalizers) });

          return "<b><img border='0' align='bottom' src='nbresloc:/org/netbeans/modules/profiler/resources/memory.png'>&nbsp;&nbsp;" // NOI18N
               + SUMMARY_STRING + "</b><br><hr>" + dateTaken + "<br>" + filename + "<br>" + filesize + "<br><br>" + liveBytes // NOI18N
               + "<br>" + liveClasses + "<br>" + liveInstances + "<br>" + classloaders + "<br>" + gcroots + "<br>" + finalizersInfo; // NOI18N
    }

    private long computeFinalizers(Heap heap) {
        JavaClass finalizerClass = heap.getJavaClassByName("java.lang.ref.Finalizer"); // NOI18N
        if (finalizerClass != null) {
            Instance queue = (Instance) finalizerClass.getValueOfStaticField("queue"); // NOI18N
            if (queue != null) {
                Long len = (Long) queue.getValueOfField("queueLength"); // NOI18N
                if (len != null) {
                    return len.longValue();
                }
            }
        }
        return -1;
    }
    
    private String computeSystemProperties(boolean showSystemProperties) {
        Properties sysprops = getSystemProperties();

        if (sysprops == null) {
            return NOT_AVAILABLE_MSG;
        }

        return "<b><img border='0' align='bottom' src='nbresloc:/org/netbeans/modules/profiler/heapwalk/ui/resources/properties.png'>&nbsp;&nbsp;" // NOI18N
               + SYSTEM_PROPERTIES_STRING + "</b><br><hr>" // NOI18N
               + (showSystemProperties ? formatSystemProperties(sysprops)
                                       : ("&nbsp;&nbsp;&nbsp;&nbsp;<a href='" + SHOW_SYSPROPS_URL + "'>" + SHOW_SYSPROPS_LINK_STRING + "</a>")); // NOI18N
    }

    private String computeThreads(boolean showThreads) {
        return "<b><img border='0' align='bottom' src='nbresloc:/org/netbeans/modules/profiler/resources/threadsWindow.png'>&nbsp;&nbsp;" // NOI18N
               + THREADS_STRING + "</b><br><hr>" // NOI18N
               + (showThreads ? getStackTrace()
                                       : ("&nbsp;&nbsp;&nbsp;&nbsp;<a href='" + SHOW_THREADS_URL + "'>" + SHOW_THREADS_LINK_STRING + "</a><br>&nbsp;")); // NOI18N
        // NOTE: the above HTML string should be terminated by newline to workaround HTML rendering bug in JDK 5, see Issue 120157
    }

    private synchronized String getStackTrace() {
        if(stackTrace == null) {
            StringWriter sw = new StringWriter();
            Heap h = summaryController.getHeapFragmentWalker().getHeapFragment();
            Collection<GCRoot> roots = h.getGCRoots();
            // Use this to enable VisualVM color scheme for threads dumps:
            // sw.append("<pre style='color: #cc3300;'>"); // NOI18N
            sw.append("<pre>"); // NOI18N
            for (GCRoot root : roots) {
                if(root.getKind().equals(GCRoot.THREAD_OBJECT)) {
                    ThreadObjectGCRoot threadRoot = (ThreadObjectGCRoot)root;
                    Instance threadInstance = threadRoot.getInstance();
                    if (threadInstance != null) {
                        PrimitiveArrayInstance chars = (PrimitiveArrayInstance)threadInstance.getValueOfField("name");  // NOI18N
                        List<String> charsList = chars.getValues();
                        char charArr[] = new char[charsList.size()];
                        int j = 0;
                        for(String ch: charsList) {
                            charArr[j++] = ch.charAt(0);
                        }
                        String threadName = new String(charArr);
                        Boolean daemon = (Boolean)threadInstance.getValueOfField("daemon"); // NOI18N
                        Integer priority = (Integer)threadInstance.getValueOfField("priority"); // NOI18N
                        Long threadId = (Long)threadInstance.getValueOfField("tid");    // NOI18N
                        Integer threadStatus = (Integer)threadInstance.getValueOfField("threadStatus"); // NOI18N
                        State tState = sun.misc.VM.toThreadState(threadStatus.intValue());
                        StackTraceElement stack[] = threadRoot.getStackTrace();
                        // --- Use this to enable VisualVM color scheme for threads dumps: ---
                        // sw.append("&nbsp;&nbsp;<span style=\"color: #0033CC\">"); // NOI18N
                        sw.append("&nbsp;&nbsp;<b>");   // NOI18N
                        // -------------------------------------------------------------------
                        sw.append("\""+threadName+"\""+(daemon.booleanValue() ? " daemon" : "")+" prio="+priority+" tid="+threadId+" "+tState);    // NOI18N
                        // --- Use this to enable VisualVM color scheme for threads dumps: ---
                        // sw.append("</span><br>"); // NOI18N
                        sw.append("</b><br>");   // NOI18N
                        // -------------------------------------------------------------------
                        if(stack != null) {
                            for(int i = 0; i < stack.length; i++) {
                                String stackElHref;
                                StackTraceElement stackElement = stack[i];

                                if (summaryController.getHeapFragmentWalker().getHeapDumpProject() != null) {
                                    String className = stackElement.getClassName();
                                    String method = stackElement.getMethodName();
                                    int lineNo = stackElement.getLineNumber();
                                    String stackUrl = OPEN_THREADS_URL+className+"|"+method+"|"+lineNo; // NOI18N

                                    // --- Use this to enable VisualVM color scheme for threads dumps: ---
                                    // stackElHref = "&nbsp;&nbsp;<a style=\"color: #CC3300;\" href=\""+stackUrl+"\">"+stackElement+"</a>"; // NOI18N
                                    stackElHref = "<a href=\""+stackUrl+"\">"+stackElement+"</a>";    // NOI18N
                                    // -------------------------------------------------------------------
                                } else {
                                    stackElHref = stackElement.toString();
                                }
                                sw.append("\tat "+stackElHref+"<br>");  // NOI18N
                            }
                        }
                    } else {
                        sw.append("&nbsp;&nbsp;Unknown thread"); // NOI18N
                    }
                    sw.append("<br>");  // NOI18N
                }
            }
            sw.append("</pre>"); // NOI18N
            stackTrace = sw.toString();
        }
        return stackTrace;
    }

    private void refreshSummary() {
        if (!showSysprops) {
            dataArea.setText(IN_PROGRESS_MSG);
        }

        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    String summary = "<nobr>" + computeSummary() + "</nobr>"; // NOI18N
                    String environment = "<nobr>" + computeEnvironment() + "</nobr>"; // NOI18N
                    String properties = "<nobr>" + computeSystemProperties(showSysprops) + "</nobr>"; // NOI18N
                    String threads = "<nobr>" + computeThreads(showThreads) + "</nobr>"; // NOI18N
                    final String dataAreaText = summary + "<br><br>" // NOI18N
                                                + environment + "<br><br>" // NOI18N
                                                + properties + "<br><br>" // NOI18N
                                                + threads;

                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                dataArea.setText(dataAreaText);
                                dataArea.setCaretPosition(0);
                            }
                        });
                }
            });
    }

    private String formatSystemProperties(Properties properties) {
        StringBuffer text = new StringBuffer(200);
        List keys = new ArrayList();
        Enumeration en = properties.propertyNames();
        Iterator keyIt;
        
        while (en.hasMoreElements()) {
            keys.add(en.nextElement());
        }
        Collections.sort(keys);
        keyIt = keys.iterator();

        while (keyIt.hasNext()) {
            String key = (String) keyIt.next();
            String val = properties.getProperty(key);

            if ("line.separator".equals(key) && val != null) {  // NOI18N
                val = val.replace("\n", "\\n"); // NOI18N
                val = val.replace("\r", "\\r"); // NOI18N
            }

            text.append("<nobr>&nbsp;&nbsp;&nbsp;&nbsp;<b>"); // NOI18N
            text.append(key);
            text.append("</b>="); // NOI18N
            text.append(val);
            text.append("</nobr><br>"); // NOI18N
        }

        return text.toString();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        // Top separator
        JSeparator separator = new JSeparator() {
            public Dimension getMaximumSize() {
                return new Dimension(super.getMaximumSize().width, 1);
            }

            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 1);
            }
        };

        separator.setBackground(getBackground());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(separator, constraints);

        // dataArea
        dataArea = new HTMLTextArea() {
                protected void showURL(URL url) {
                    if (url == null) return;
                    String urls = url.toString();
                    if (urls.equals(SHOW_SYSPROPS_URL)) {
                        showSysprops = true;
                    } else if (urls.equals(SHOW_THREADS_URL)) {
                        showThreads = true;
                    } else if (urls.startsWith(OPEN_THREADS_URL)) {
                        urls = urls.substring(OPEN_THREADS_URL.length());
                        String parts[] = urls.split("\\|"); // NOI18N
                        String className = parts[0];
                        String method = parts[1];
                        int linenumber = Integer.parseInt(parts[2]);
                        GoToSourceHelper.openSource(summaryController.getHeapFragmentWalker().getHeapDumpProject(),
                                                    new JavaSourceLocation(className, method, linenumber));
        }
                    refreshSummary();
                }
            };

        JScrollPane dataAreaScrollPane = new JScrollPane(dataArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dataAreaScrollPane.setBorder(BorderFactory.createEmptyBorder());
        dataAreaScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        dataAreaScrollPane.setBackground(dataArea.getBackground());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(dataAreaScrollPane, constraints);

        // UI tweaks
        setBackground(dataArea.getBackground());
    }
}
