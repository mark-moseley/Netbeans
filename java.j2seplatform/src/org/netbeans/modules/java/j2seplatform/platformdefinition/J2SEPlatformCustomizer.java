/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;


import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;



public class J2SEPlatformCustomizer extends JTabbedPane {

    private static final int CLASSPATH = 0;
    private static final int SOURCES = 1;
    private static final int JAVADOC = 2;

    private J2SEPlatformImpl platform;

    public J2SEPlatformCustomizer (J2SEPlatformImpl platform) {
        this.platform = platform;
        this.initComponents ();
    }


    private void initComponents () {
        this.addTab(NbBundle.getMessage(J2SEPlatformCustomizer.class,"TXT_Classes"), createPathTab(CLASSPATH));
        this.addTab(NbBundle.getMessage(J2SEPlatformCustomizer.class,"TXT_Sources"), createPathTab(SOURCES));
        this.addTab(NbBundle.getMessage(J2SEPlatformCustomizer.class,"TXT_Javadoc"), createPathTab(JAVADOC));
    }


    private JComponent createPathTab (int type) {
        return new PathView (this.platform, type);
    }


    private static class PathView extends JPanel {

        private JList resources;
        private JButton addButton;
//        private JButton addURLButton;
        private JButton removeButton;
        private JButton moveUpButton;
        private JButton moveDownButton;
        private int type;

        public PathView (J2SEPlatformImpl platform, int type) {
            this.type = type;
            this.initComponents (platform);
        }

        private void initComponents (J2SEPlatformImpl platform) {
            this.setLayout(new GridBagLayout());
            JLabel label = new JLabel ();
            String key = null;
            String mneKey = null;
            switch (type) {
                case CLASSPATH:
                    key = "TXT_JDKClasspath";       //NOI18N
                    mneKey = "MNE_JDKClasspath";    //NOI18N
                    break;
                case SOURCES:
                    key = "TXT_JDKSources";         //NOI18N
                    mneKey = "MNE_JDKSources";      //NOI18N
                    break;
                case JAVADOC:
                    key = "TXT_JDKJavadoc";         //NOI18N
                    mneKey = "MNE_JDKJavadoc";      //NOI8N
                    break;
                default:
                    assert false : "Illegal type of panel";     //NOI18N
                    return;
            }
            label.setText (NbBundle.getMessage(J2SEPlatformCustomizer.class,key));
            label.setDisplayedMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class,mneKey).charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (12,12,2,0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints(label,c);
            this.add (label);
            this.resources = new JList(new PathModel(platform,type));
            this.resources.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    selectionChanged ();
                }
            });
            JScrollPane spane = new JScrollPane (this.resources);
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.gridheight = 5;
            c.insets = new Insets (0,12,12,6);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints(spane,c);
            this.add (spane);
            label.setLabelFor (spane);
            if (type == SOURCES || type == JAVADOC) {                
                this.addButton = new JButton ();
                String text;
                char mne;
                if (type == SOURCES) {
                    text = NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_Add");
                    mne = NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_Add").charAt(0);
                }
                else {
                    text = NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_AddZip");
                    mne = NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_AddZip").charAt(0);
                }
                this.addButton.setText(text);
                this.addButton.setMnemonic(mne);
                addButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        addPathElement ();
                    }
                });
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (4,6,5,12);
                ((GridBagLayout)this.getLayout()).setConstraints(addButton,c);
                this.add (addButton);
//                if (this.type == JAVADOC) {
//                    addURLButton  = new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_AddURL"));
//                    addURLButton.setMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_AddURL").charAt(0));
//                    addURLButton.addActionListener(new ActionListener () {
//                        public void actionPerformed(ActionEvent e) {
//                            addURLElement ();
//                        }
//                    });
//                    c = new GridBagConstraints();
//                    c.gridx = 1;
//                    c.gridy = 2;
//                    c.gridwidth = GridBagConstraints.REMAINDER;
//                    c.fill = GridBagConstraints.HORIZONTAL;
//                    c.anchor = GridBagConstraints.NORTHWEST;
//                    c.insets = new Insets (0,6,6,12);
//                    ((GridBagLayout)this.getLayout()).setConstraints(addURLButton,c);
//                    this.add (addURLButton);
//                }
                removeButton = new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_Remove"));
                removeButton.setMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_Remove").charAt(0));
                removeButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        removePathElement ();
                    }
                });
                removeButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 3;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (0,6,6,12);
                ((GridBagLayout)this.getLayout()).setConstraints(removeButton,c);
                this.add (removeButton);
                moveUpButton = new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_Up"));
                moveUpButton.setMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_Up").charAt(0));
                moveUpButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveUpPathElement ();
                    }
                });
                moveUpButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 4;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (6,6,6,12);
                ((GridBagLayout)this.getLayout()).setConstraints(moveUpButton,c);
                this.add (moveUpButton);
                moveDownButton = new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class, "CTL_Down"));
                moveDownButton.setMnemonic (NbBundle.getMessage(J2SEPlatformCustomizer.class, "MNE_Down").charAt(0));
                moveDownButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveDownPathElement ();
                    }
                });
                moveDownButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 5;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (0,6,12,12);
                ((GridBagLayout)this.getLayout()).setConstraints(moveDownButton,c);
                this.add (moveDownButton);
            }
        }

//        private void addURLElement() {
//            JPanel p = new JPanel ();
//            GridBagLayout lm = new GridBagLayout();
//            p.setLayout (lm);
//            GridBagConstraints c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.insets = new Insets (12,12,12,6);
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JLabel label = new JLabel (NbBundle.getMessage(J2SEPlatformCustomizer.class,"CTL_AddJavadocURLMessage"));
//            label.setDisplayedMnemonic ('U');
//            lm.setConstraints(label,c);
//            p.add (label);
//            c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.gridwidth = GridBagConstraints.REMAINDER;
//            c.insets = new Insets (12,0,12,6);
//            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JTextField text = new JTextField ();
//            text.setColumns(30);
//            text.setText (NbBundle.getMessage(J2SEPlatformCustomizer.class,"TXT_DefaultProtocol"));
//            text.selectAll();
//            label.setLabelFor(text);
//            lm.setConstraints(text,c);
//            p.add (text);            
//            JButton[] options = new JButton[] {
//                new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class,"CTL_AddJavadocURLTitle")),
//                new JButton (NbBundle.getMessage(J2SEPlatformCustomizer.class,"CTL_Cancel"))
//            };
//            options[0].setMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class,"MNE_Add").charAt(0));
//            options[1].setMnemonic(NbBundle.getMessage(J2SEPlatformCustomizer.class,"MNE_Cancel").charAt(0));
//            DialogDescriptor input = new DialogDescriptor (
//                p,
//                NbBundle.getMessage(J2SEPlatformCustomizer.class,"CTL_AddJavadocURLTitle"),
//                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);            
//            if (DialogDisplayer.getDefault().notify(input) == options[0]) {
//                try {
//                    String value = text.getText();
//                    URL url = new URL (value);
//                    ((PathModel)this.resources.getModel()).addPath(url);
//                    this.resources.setSelectedIndex (this.resources.getModel().getSize()-1);
//                } catch (MalformedURLException mue) {
//                    DialogDescriptor.Message message = new DialogDescriptor.Message (
//                        NbBundle.getMessage(J2SEPlatformCustomizer.class,"CTL_InvalidURLFormat"),
//                        DialogDescriptor.ERROR_MESSAGE);
//                    DialogDisplayer.getDefault().notify(message);
//                }
//            }
//        }

        private void addPathElement () {
            JFileChooser chooser = new JFileChooser ();
            chooser.setMultiSelectionEnabled (true);
            String title = null;
            String message = null;
            String approveButtonName = null;
            String approveButtonNameMne = null;
            if (this.type == SOURCES) {
                title = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_OpenSources");
                message = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_Sources");
                approveButtonName = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_OpenSources");
                approveButtonNameMne = NbBundle.getMessage (J2SEPlatformCustomizer.class,"MNE_OpenSources");
            }
            else if (this.type == JAVADOC) {
                title = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_OpenJavadoc");
                message = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_Javadoc");
                approveButtonName = NbBundle.getMessage (J2SEPlatformCustomizer.class,"TXT_OpenJavadoc");
                approveButtonNameMne = NbBundle.getMessage (J2SEPlatformCustomizer.class,"MNE_OpenJavadoc");
            }
            chooser.setDialogTitle(title);
            chooser.setApproveButtonText(approveButtonName);
            chooser.setApproveButtonMnemonic (approveButtonNameMne.charAt(0));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter (new SimpleFileFilter(message,new String[] {"ZIP","JAR"}));   //NOI18N
            if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();


                PathModel model = (PathModel) this.resources.getModel();
                if (!model.addPath (f)) {
                    new NotifyDescriptor.Message (NbBundle.getMessage(J2SEPlatformCustomizer.class,"TXT_CanNotAddResolve"),
                            NotifyDescriptor.ERROR_MESSAGE);
                }
                this.resources.setSelectedIndex (this.resources.getModel().getSize()-1);
            }
        }

        private void removePathElement () {
            int index = this.resources.getSelectedIndex();
            if (index == -1) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.removePath (index);
            if ( index< this.resources.getModel().getSize() - 2) {
                this.resources.setSelectedIndex (index+1);
            }
            else if (index>0) {
                this.resources.setSelectedIndex (index-1);
            }
        }

        private void moveDownPathElement () {
            int index = this.resources.getSelectedIndex();
            if (index == -1) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveDownPath (index);
            this.resources.setSelectedIndex (index+1);
        }

        private void moveUpPathElement () {
            int index = this.resources.getSelectedIndex();
            if (index == -1) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveUpPath (index);
            this.resources.setSelectedIndex (index-1);
        }

        private void selectionChanged () {
            if (this.type == CLASSPATH) {
                return;
            }
            int index = this.resources.getSelectedIndex();
            this.removeButton.setEnabled (index != -1);
            this.moveUpButton.setEnabled (index>0);
            this.moveDownButton.setEnabled(index>-1 && index<this.resources.getModel().getSize()-1);
        }

    }


    private static class PathModel extends AbstractListModel/*<String>*/ {

        private J2SEPlatformImpl platform;
        private int type;
        private java.util.List data;

        public PathModel (J2SEPlatformImpl platform, int type) {
            this.platform = platform;
            this.type = type;
        }

        public int getSize() {
            return this.getData().size();
        }

        public Object getElementAt(int index) {
            java.util.List list = this.getData();
            URL url = (URL)list.get(index);
            if ("jar".equals(url.getProtocol())) {      //NOI18N
                URL fileURL = FileUtil.getArchiveFile (url);
                if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                    // really the root
                    url = fileURL;
                } else {
                    // some subdir, just show it as is
                    return url.toExternalForm();
                }
            }
            if ("file".equals(url.getProtocol())) {
                File f = new File (URI.create(url.toExternalForm()));
                return f.getAbsolutePath();
            }
            else {
                return url.toExternalForm();
            }
        }

        private void removePath (int index) {
            java.util.List data = getData();
            data.remove(index);
            updatePlatform ();
            fireIntervalRemoved(this,index,index);
        }

        private void moveUpPath (int index) {
            java.util.List data = getData ();
            Object p2 = data.get (index);
            Object p1 = data.set (index-1,p2);
            data.set (index,p1);
            updatePlatform ();
            fireContentsChanged(this,index-1,index);
        }

        private void moveDownPath (int index) {
            java.util.List data = getData ();
            Object p1 = data.get (index);
            Object p2 = data.set (index+1,p1);
            data.set (index,p2);
            updatePlatform();
            fireContentsChanged(this,index,index+1);
        }

        private boolean addPath (File f) {
            try {
                URL url = f.toURI().toURL();
                return this.addPath (url);
            } catch (MalformedURLException mue) {
                return false;
            }
        }

        private boolean addPath (URL url) {
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot (url);
            }
            java.util.List data = getData();
            int oldSize = data.size ();
            data.add (url);
            updatePlatform();
            fireIntervalAdded(this,oldSize,oldSize);
            return true;
        }

        private synchronized java.util.List getData () {
            if (this.data == null) {
                switch (this.type) {
                    case CLASSPATH:
                        this.data = getPathList (this.platform.getBootstrapLibraries());
                        break;
                    case SOURCES:
                        this.data = getPathList (this.platform.getSourceFolders());
                        break;
                    case JAVADOC:
                        this.data = new ArrayList(this.platform.getJavadocFolders());
                        break;
                }
            }
            return this.data;
        }

        private static java.util.List getPathList (ClassPath cp) {
            java.util.List result = new ArrayList ();
            for (Iterator it = cp.entries().iterator(); it.hasNext();) {
                ClassPath.Entry entry = (ClassPath.Entry) it.next ();
                result.add (entry.getURL());
            }
            return result;
        }

        private static ClassPath createClassPath (java.util.List/*<URL>*/ roots) {
            java.util.List resources = new ArrayList ();
            for (Iterator it = roots.iterator(); it.hasNext();) {
                URL url = (URL) it.next ();
                resources.add (ClassPathSupport.createResource(url));
            }
            return ClassPathSupport.createClassPath(resources);
        }

        private void updatePlatform () {
            switch (this.type) {
                case SOURCES:
                    this.platform.setSourceFolders(createClassPath(data));
                    break;
                case JAVADOC:
                    this.platform.setJavadocFolders (data);
                    break;
                default:
                    assert false : "Trying to update unknown property";     //NOI18N
            }
        }
    }


    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring (index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }


}
