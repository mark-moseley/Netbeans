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

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
* A property editor for Font class.
*
* @author Ian Formanek
*/
public class FontEditor implements PropertyEditor, XMLPropertyEditor {

    // static .....................................................................................

    static final Integer[] sizes = new Integer [] {
                                       Integer.valueOf (3),
                                       Integer.valueOf (5),
                                       Integer.valueOf (8),
                                       Integer.valueOf (10),
                                       Integer.valueOf (12),
                                       Integer.valueOf (14),
                                       Integer.valueOf (18),
                                       Integer.valueOf (24),
                                       Integer.valueOf (36),
                                       Integer.valueOf (48)
                                   };

    static final String[] styles = new String [] {
                                       NbBundle.getMessage(FontEditor.class, "CTL_Plain"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_Bold"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_Italic"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_BoldItalic")
                                   };

    // variables ..................................................................................

    private Font font;
    private String fontName;
    private PropertyChangeSupport support;


    // init .......................................................................................

    public FontEditor() {
        support = new PropertyChangeSupport (this);
    }


    // main methods .......................................................................................

    public Object getValue () {
        return font;
    }

    public void setValue (Object object) {
        if (font != null && font.equals (object)) {
            return ;
        } else if (font == null && object == null) {
            return ;
        }
        
        if (object instanceof Font) {
            font = (Font) object;
        } else if (object == null) {
            font = null;
        } else {
            assert false : "Object " + object + " is instanceof Font or null";
        }
        
        if (font != null) {
            fontName = font.getName () + " " + font.getSize () + " " + getStyleName (font.getStyle ()); // NOI18N
        } else {
            fontName = null;
        }
        
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return fontName;
    }

    public void setAsText (String string) {
        return;
    }

    public String getJavaInitializationString () {
        return "new java.awt.Font(\"" + font.getName () + "\", " + font.getStyle () + // NOI18N
               ", " + font.getSize () + ")"; // NOI18N
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return true;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
        Font originalFont = g.getFont ();
        
        // Fix of 21713, set default value
        if ( font == null ) setValue( null );
        
        Font paintFont = font == null ? originalFont : font; // NOI18N
        assert paintFont != null : "paintFont must exist.";
        
        FontMetrics fm = g.getFontMetrics (paintFont);
        if (fm.getHeight() > rectangle.height) {
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                // don't use deriveFont() - see #49973 for details
                paintFont = new Font(paintFont.getName(), paintFont.getStyle(), 12);
            } else {
                paintFont = paintFont.deriveFont(12f);
            }
            fm = g.getFontMetrics (paintFont);
        }
        g.setFont (paintFont);
        g.drawString (fontName == null ? "null" : fontName, // NOI18N
                      rectangle.x,
                      rectangle.y + (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
        g.setFont (originalFont);
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new FontPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }


    // helper methods .......................................................................................

    String getStyleName (int i) {
        if ((i & Font.BOLD) > 0)
            if ((i & Font.ITALIC) > 0) return NbBundle.getMessage(FontEditor.class, "CTL_BoldItalic");
            else return NbBundle.getMessage(FontEditor.class, "CTL_Bold");
        else
            if ((i & Font.ITALIC) > 0) return NbBundle.getMessage(FontEditor.class, "CTL_Italic");
            else return NbBundle.getMessage(FontEditor.class, "CTL_Plain");
    }
    
    static private String[] fonts;
    static private String [] getFonts () {
        if (fonts == null) {
            try {
                fonts = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();
            } catch (RuntimeException e) {
                fonts = new String[0]; //NOI18N
                if (org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_MAC) {
                    String msg = NbBundle.getMessage(FontEditor.class, "MSG_AppleBug"); //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                } else {
                    throw e;
                }
            }
        }
        return fonts;
    }


    // innerclasses ............................................................................................

    class FontPanel extends JPanel {

        JTextField tfFont, tfStyle, tfSize;
        JList lFont, lStyle, lSize;

        static final long serialVersionUID =8377025140456676594L;

        FontPanel () {
            setLayout (new BorderLayout ());
            setBorder(new EmptyBorder(12, 12, 0, 11));
            
            Font font = (Font) getValue ();
            if (font == null) {
                if (getFonts ().length > 0) {
                    font = new Font (fonts[0], Font.PLAIN, 10);
                } else {
                    font = UIManager.getFont ("Label.font"); // NOI18N
                }
            }

            lFont = new JList (getFonts ());
            lFont.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_Font"));
            lStyle = new JList (styles);
            lStyle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_FontStyle"));
            lSize = new JList (sizes);
            lSize.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_Size"));
            tfSize = new JTextField (String.valueOf(font.getSize ()));
            tfSize.getAccessibleContext().setAccessibleDescription(lSize.getAccessibleContext().getAccessibleDescription());
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_FontCustomEditor"));

            GridBagLayout la = new GridBagLayout ();
            GridBagConstraints c = new GridBagConstraints ();
            setLayout (la);

            c.gridwidth = 1;
            c.weightx = 1.0;
            c.insets = new Insets (0, 0, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            JLabel l = new JLabel (NbBundle.getMessage(FontEditor.class, "CTL_Font"));                    //NoI18N
            l.setDisplayedMnemonic(NbBundle.getMessage(FontEditor.class, "CTL_Font_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(lFont);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (0, 5, 0, 0);
            l = new JLabel (NbBundle.getMessage(FontEditor.class, "CTL_FontStyle"));                           //NoI18N
            l.setDisplayedMnemonic(NbBundle.getMessage(FontEditor.class, "CTL_FontStyle_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(lStyle);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (0, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            l = new JLabel (NbBundle.getMessage(FontEditor.class, "CTL_Size"));                           //NoI18N
            l.setDisplayedMnemonic(NbBundle.getMessage(FontEditor.class, "CTL_Size_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(tfSize);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (5, 0, 0, 0);
            c.gridwidth = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            tfFont = new JTextField (font.getName ());
            tfFont.setEnabled (false);
            la.setConstraints (tfFont, c);
            add (tfFont);

            c.insets = new Insets (5, 5, 0, 0);
            tfStyle = new JTextField (getStyleName (font.getStyle ()));
            tfStyle.setEnabled (false);
            la.setConstraints (tfStyle, c);
            add (tfStyle);

            c.insets = new Insets (5, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            
            tfSize.addKeyListener( new KeyAdapter() {
                                    public void keyPressed(KeyEvent e) {
                                        if ( e.getKeyCode() == KeyEvent.VK_ENTER )
                                            setValue ();
                                    }
                                });
            
            tfSize.addFocusListener (new FocusAdapter () {
                                         public void focusLost (FocusEvent evt) {
                                             setValue ();
                                         }
                                     });
            la.setConstraints (tfSize, c);
            add (tfSize);

            c.gridwidth = 1;
            c.insets = new Insets (5, 0, 0, 0);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            lFont.setVisibleRowCount (5);
            lFont.setSelectedValue(font.getName (), true);
            lFont.addListSelectionListener (new ListSelectionListener () {
                                                public void valueChanged (ListSelectionEvent e) {
                                                    if (!lFont.isSelectionEmpty ()) {
                                                        if (getFonts ().length > 0) { //Mac bug workaround
                                                            int i = lFont.getSelectedIndex ();
                                                            tfFont.setText (getFonts () [i]);
                                                            setValue ();
                                                        }
                                                    }
                                                }
                                            }
                                           );
            JScrollPane sp = new JScrollPane (lFont);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            la.setConstraints (sp, c);
            add (sp);

            lStyle.setVisibleRowCount (5);
            lStyle.setSelectedValue(getStyleName (font.getStyle ()), true);
            lStyle.addListSelectionListener (new ListSelectionListener () {
                                                 public void valueChanged (ListSelectionEvent e) {
                                                     if (!lStyle.isSelectionEmpty ()) {
                                                         int i = lStyle.getSelectedIndex ();
                                                         tfStyle.setText (styles [i]);
                                                         setValue ();
                                                     }
                                                 }
                                             }
                                            );
            sp = new JScrollPane (lStyle);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            lSize.getAccessibleContext().setAccessibleName(tfSize.getAccessibleContext().getAccessibleName());
            lSize.setVisibleRowCount (5);
            updateSizeList(font.getSize ());
            lSize.addListSelectionListener (new ListSelectionListener () {
                                                public void valueChanged (ListSelectionEvent e) {
                                                    if (!lSize.isSelectionEmpty ()) {
                                                        int i = lSize.getSelectedIndex ();
                                                        tfSize.setText (String.valueOf(sizes [i]));
                                                        setValue ();
                                                    }
                                                }
                                            }
                                           );
            sp = new JScrollPane (lSize);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weighty = 2.0;
            JPanel p = new JPanel (new BorderLayout());
            p.setBorder (new TitledBorder (" " + NbBundle.getMessage(FontEditor.class, "CTL_Preview") + " "));

            JPanel pp = new JPanel () {
                            public Dimension getPreferredSize () {
                                return new Dimension (150, 60);
                            }

                            public void paint (Graphics g) {
                                //          super.paint (g);
                                FontEditor.this.paintValue (g, new Rectangle (0, 0, this.getSize().width - 1, this.getSize().height - 1));
                            }
                        };
            p.add ("Center", pp); // NOI18N
            c.insets = new Insets (12, 0, 0, 0);
            la.setConstraints (p, c);
            add (p);
        }

        public Dimension getPreferredSize () {
            return new Dimension (400, 250);
        }

        private void updateSizeList(int size) {
            if (java.util.Arrays.asList(sizes).contains(Integer.valueOf(size)))
                lSize.setSelectedValue(Integer.valueOf(size), true);
            else
                lSize.clearSelection();
        }

        void setValue () {
            int size = 12;
            try {
                size = Integer.parseInt (tfSize.getText ());
                updateSizeList(size);
            } catch (NumberFormatException e) {
                return;
            }
            int i = lStyle.getSelectedIndex (), ii = Font.PLAIN;
            switch (i) {
            case 0: ii = Font.PLAIN;break;
            case 1: ii = Font.BOLD;break;
            case 2: ii = Font.ITALIC;break;
            case 3: ii = Font.BOLD | Font.ITALIC;break;
            }
            FontEditor.this.setValue (new Font (tfFont.getText (), ii, size));
            invalidate();
            java.awt.Component p = getParent();
            if (p != null) {
                p.validate();
            } 
            repaint();
        }
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_FONT = "Font"; // NOI18N

    public static final String ATTR_NAME = "name"; // NOI18N
    public static final String ATTR_STYLE = "style"; // NOI18N
    public static final String ATTR_SIZE = "size"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_FONT.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
            String style = attributes.getNamedItem (ATTR_STYLE).getNodeValue (); // [PENDING - style names]
            String size = attributes.getNamedItem (ATTR_SIZE).getNodeValue ();
            setValue (new Font (name, Integer.parseInt (style), Integer.parseInt (size)));
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        if (font == null) {
            IllegalArgumentException iae = new IllegalArgumentException();
            Exceptions.attachLocalizedMessage(iae,
                                              NbBundle.getMessage(FontEditor.class,
                                                                  "MSG_FontIsNotInitialized")); // NOI18N
            Exceptions.printStackTrace(iae);
            return null;
        }
        
        org.w3c.dom.Element el = doc.createElement (XML_FONT);
        el.setAttribute (ATTR_NAME, font.getName ());
        el.setAttribute (ATTR_STYLE, Integer.toString (font.getStyle ()));
        el.setAttribute (ATTR_SIZE, Integer.toString (font.getSize ()));
        return el;
    }

}
