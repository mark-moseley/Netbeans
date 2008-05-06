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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.openide.util.NbBundle;

/**
 * @author aa160298
 */
public class GlassPane extends JPanel implements ActionListener, FocusListener, MouseListener {
    
    private JPanel labelPane;
    private JButton hideButton;
    private JComponent decoration;
    private JTextComponent editorPane;
    private JScrollPane scrollPane;
    private StringBuffer html = new StringBuffer();
    private boolean myEditable;
    
    private int px = 0;
    private int py = 0;
    
    private Shape borderShape = null;
    public GlassPane(String text, ActionListener actionListener, boolean editable, JComponent decoration){
        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(26, 26, 26, 26)); 
        setPreferredSize(new Dimension(320, 180)); 
        myEditable = editable;
        this.decoration = decoration;
        
        setOpaque(false);

        if (editable) {
          editorPane = new JTextArea();
          ((JTextArea) editorPane).setLineWrap(true);
          ((JTextArea) editorPane).setWrapStyleWord(true);
        }
        else {
          editorPane = new JEditorPane() {
              protected void paintComponent(Graphics g) {
                  Graphics2D g2 = (Graphics2D) g.create();
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  super.paintComponent(g2);
                  g2.dispose();
              }
          };
          String type = "text/html"; // NOI18N
          ((JEditorPane) editorPane).setContentType(type);
          ((JEditorPane) editorPane).setEditorKitForContentType(type, new HTMLEditorKit());
        }
        editorPane.setEditable(editable);
        editorPane.setBackground(null);
        editorPane.setBorder(null);
        editorPane.setOpaque(false);
        scrollPane = createScrollPane(editorPane);

        InputMap oldIM = editorPane.getInputMap();
        InputMap newIM = new InputMap();
        
        for (KeyStroke ks : oldIM.allKeys()) {
            if (ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
                            KeyEvent.ALT_DOWN_MASK))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_M, 
                            KeyEvent.CTRL_DOWN_MASK))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent
                            .VK_CONTEXT_MENU, 0))
                    || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 
                            KeyEvent.SHIFT_DOWN_MASK)))
            { 
                continue;
            }
            newIM.put(ks, oldIM.get(ks));
        }
        if ( !editable) {
          editorPane.setInputMap(WHEN_FOCUSED, newIM);
        }
        labelPane = new JPanel(new BorderLayout(10, 0));
        labelPane.setBackground(null);
        labelPane.setOpaque(false);
        int margin;

        if (editable) {
          margin = 0;
        }
        else {
          margin = 8;
        }
        labelPane.setBorder(new EmptyBorder(0, margin, 0, margin));
        hideButton = new HideButton();
        hideButton.setMargin(new Insets(2, 2, 2, 2));
        hideButton.addActionListener(this);
        
        JPanel headerPane = new JPanel(new BorderLayout(5, 0));
        headerPane.setBorder(new UnderlineBorder(0, 0, 6, 0, STROKE));
        headerPane.add(labelPane, BorderLayout.CENTER);
        headerPane.add(hideButton, BorderLayout.EAST);
        headerPane.setBackground(null);
        headerPane.setOpaque(false);
        
        add(scrollPane, BorderLayout.CENTER); 
        add(headerPane, BorderLayout.NORTH);
        
        this.addMouseListener(this);
        scrollPane.addMouseListener(this);
        editorPane.addMouseListener(this);

        editorPane.addFocusListener(this);
        updateText(text);

        myActionListener = actionListener;
    }

    @Override
    public void requestFocus() {
      editorPane.requestFocus();
    }
    
    public JComponent getDecoration(){
        return decoration;
    }
    
    public boolean contains(int x, int y) {
        Shape borderShape = getBorderShape();
        return (borderShape != null) && borderShape
                .contains(0.5 + x, 0.5 + y);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        borderShape = null;
    }
    
    public void addHeader(Icon icon, String text) {
      addHeader(icon, text, TEXT_COLOR);
    }

    public void addHeader(Icon icon, String text, Color color) {
        HeaderLabel label = new HeaderLabel(icon, text, color);
        
        int headersCount = labelPane.getComponentCount();
        
        if (headersCount == 0) {
            labelPane.add(label, BorderLayout.WEST);
            int size = label.getPreferredSize().height;
            hideButton.setPreferredSize(new Dimension(size, size));
        } else if (headersCount == 1) {
            label.setHorizontalAlignment(JLabel.CENTER);
            labelPane.add(label, BorderLayout.CENTER);
        } else if (headersCount == 2) {
            labelPane.add(label, BorderLayout.EAST);
        } else {
            throw new IndexOutOfBoundsException("Too many headers"); // NOI18N
        }
    }

    public void removeHeaders() {
        labelPane.removeAll();
    }

    public void removeHTML() {
        html.delete(0, html.length());
    }
    
    public void addListItem(String iconPath, String description) {
        if (html.length() == 0) {
            fillHTMLHeader();
        } else {
            fillHTMLDivider();
        }
        
        int iconSize;
        int iconSpace;
        
        if (iconPath.indexOf("explicit") >= 0) { // NOI18N
            iconSize = 12;
            iconSpace = 0;
        } else {
            iconSize = 10;
            iconSpace = 1;
        }
        
        fillHTMLSpacer();
        fillHTMLItem(Decoration.class.getResource(iconPath), iconSize,
                iconSpace, description);
        fillHTMLSpacer();
    }
    
    public void updateText(String text) {
      if (text != null) {
        editorPane.setText(text);
      }
    }

    public void updateHTML() {
        if (html.length() == 0) {
            fillHTMLHeader();
        } 
        fillHTMLFooter();
        String newText = html.toString();
        String oldText = editorPane.getText();

        if ( !equals(newText, oldText)) {
            editorPane.setText(newText);
            editorPane.setCaretPosition(editorPane.getDocument()
                .getStartPosition().getOffset());
        }
        html.delete(0, html.length());
    }

    private boolean equals(String s1, String s2) {
        if (s1 == s2) return true;
        if (s1 == null) return false;
        if (s2 == null) return false;
        return s1.equals(s2);
    }
    
    private void fillHTMLHeader() {
        Font font = new JLabel().getFont();
        html.append("<html><head>"); // NOI18N
        html.append("<style> TD { font-family: "); // NOI18N
        html.append(font.getFamily());
        html.append("; font-size: "); // NOI18N
        html.append(font.getSize());
        html.append("pt; } </style>"); // NOI18N
        html.append("</head><body>"); // NOI18N
    }
    
    private void fillHTMLFooter() {
        html.append("</body></html>"); // NOI18N
    }
    
    private void fillHTMLDivider() {
        html.append("<table cellpadding=0 cellspacing=0 border=0 width=100%>"); // NOI18N
        html.append("<tr><td bgcolor=#999999><img src=\""); // NOI18N
        html.append(E_IMAGE_URL);
        html.append("\" width=1 height=1></td></tr></table>"); // NOI18N
    }
    
    private void fillHTMLItem(URL iconURL, int iconSize, int iconSpace, 
            String text) 
    {
        if (text == null) {
            text = "";
        } else {
            text = text.replace("&", "&amp;"); // NOI18N
            text = text.replace("<", "&lt;"); // NOI18N
            text = text.replace(">", "&gt;"); // NOI18N
        }
        html.append("<table cellpadding=0 cellspacing=0 border=0 width=100%>"); // NOI18N
        html.append("<tr valign=top><td width=19 align=right>"); // NOI18N
        html.append("&nbsp;<img src=\""); // NOI18N
        html.append(iconURL);
        html.append("\" width="); // NOI18N
        html.append(iconSize);
        html.append(" height="); // NOI18N
        html.append(iconSize);
        html.append(" hspace="); // NOI18N
        html.append(iconSpace);
        html.append(" vspace="); // NOI18N
        html.append(iconSpace);
        html.append("></td><td width=4><img src=\"");
        html.append(E_IMAGE_URL);
        html.append("\" width=4 height=1></td><td>"); // NOI18N
        html.append(text);
        html.append("</td><td align=right>&nbsp;</td></tr></table>"); // NOI18N
    }
    
    private void fillHTMLSpacer() {
        html.append("<table cellpadding=0 cellspacing=0 border=0 width=100%>"); // NOI18N
        html.append("<tr><td><img src=\""); // NOI18N
        html.append(E_IMAGE_URL);
        html.append("\" width=1 height=5></td></tr></table>"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        hidePane();
    }

    private void hidePane() {
        if (getParent() == null) {
          return;
        }
//System.out.println();
//System.out.println();
//System.out.println("getParent() 1: " + getParent());
//System.out.println();
//System.out.println("getParent() 2: " + getParent().getParent());
//System.out.println();
//System.out.println("getParent() 3: " + getParent().getParent().getParent());
        DesignView designView = (DesignView) (getParent().getParent().getParent());
        designView.getOverlayView().remove(this);
        
        hideButton.getModel().setArmed(false);
        hideButton.getModel().setPressed(false);
        hideButton.getModel().setRollover(false);
        hideButton.getModel().setSelected(false);
        
        designView.revalidate();
        designView.repaint();

        if (myActionListener != null) {
          myActionListener.actionPerformed(
            new ActionEvent(getText(), 0, null));
        }
    }

    public String getText() {
      return editorPane.getText();
    }
    
    public void paintThumbnail(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g;
//        
//        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
//                RenderingHints.VALUE_STROKE_NORMALIZE);
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                RenderingHints.VALUE_ANTIALIAS_ON);
//        
//        Shape borderShape = getBorderShape();
//        g2.setPaint(new Color(FILL.getRed(), FILL.getGreen(), FILL.getBlue(), 
//                128));
//        g2.fill(borderShape);
//        
//        g2.setStroke(new FStroke(1).createStroke(g2));
//        g2.setPaint(STROKE);
//        g2.draw(borderShape);
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
        Shape borderShape = getBorderShape();
        
        if (borderShape != null) {
            g2.setPaint(FILL);
            g2.fill(borderShape);

            g2.setPaint(STROKE);
            g2.draw(borderShape);
        }
        
        g2.dispose();
    }
    
    public void setAnchorPoint(Point point) {
        setAnchorPoint(point.x, point.y);
    }
    
    public void setAnchorPoint(int px, int py) {
        if (this.px != px || this.py != py) {
            this.px = px;
            this.py = py;
            borderShape = null;
            repaint();
        }
    }
    
    private Shape getBorderShape() {
        if (borderShape == null) {
            Insets insets = getInsets();
            
            int w = getWidth();
            int h = getHeight();
            
            if (w == 0 || h == 0) {
                return null;
            }
            
            int x0 = insets.left - INTERNAL_INSET;
            int y0 = insets.top - INTERNAL_INSET;
            
            int rrw = w - x0 - (insets.right - INTERNAL_INSET);
            int rrh = h - y0 - (insets.bottom - INTERNAL_INSET);
            
            Area area = new Area(new RoundRectangle2D.Double(
                    x0 + 0.5, y0 + 0.5, 
                    rrw - 1, rrh - 1, INTERNAL_INSET * 2, INTERNAL_INSET * 2));
            
            double cx = x0 + 0.5 * rrw;
            double cy = y0 + 0.5 * rrh;
            
            double ax = px - cx;
            double ay = py - cy;
            
            // cx + ax * t = 0.5
            // cx + ax * t = w - 0.5
            // cy + ay * t = 0.5
            // cy + ay * t = h - 0.5
            
            double t = Double.MAX_VALUE;

            double temp = (0.5 - cx) / ax;
            if (0.0 < temp && temp < t) t = temp;
            
            temp = (w - 0.5 - cx) / ax;
            if (0.0 < temp && temp < t) t = temp;
            
            temp = (0.5 - cy) / ay;
            if (0.0 < temp && temp < t) t = temp;
            
            temp = (h - 0.5 - cy) / ay;
            if (0.0 < temp && temp < t) t = temp;
            
            double dx = t * ax;
            double dy = t * ay;
            double dlen = Math.sqrt(dx * dx + dy * dy);
            
            double nlen = Math.min(rrw, rrh) / 4.0;
            double nx = -dy * nlen / dlen;
            double ny = dx * nlen / dlen;
            
            GeneralPath gp = new GeneralPath();
            gp.moveTo((float) (cx + dx), (float) (cy + dy));
            gp.lineTo((float) (cx + nx), (float) (cy + ny));
            gp.lineTo((float) (cx - nx), (float) (cy - ny));
            gp.closePath();
            
            area.add(new Area(gp));
            
            borderShape = area;
        }
        
        return borderShape;
    }
    
//    private Shape createBorderShape() {
//        GeneralPath gp = new GeneralPath();
//        
//        Insets inests = getBorder().getBorderInsets(this);
//        
//        float x0 = 0.5f;
//        float x1 = inests.left - 6 + 0.5f;
//        float x2 = getWidth() - 0.5f;
//        
//        float y0 = 0.5f;
//        float y1 = getHeight() - 0.5f;
//        
//        float arcSize = Math.min(Math.min(x2 - x1, y1 - y0), 10);
//
//        gp.moveTo(x0, y0);
//        gp.lineTo(x1, y0 + 14);
//        gp.append(new Arc2D.Float(x1, y0, arcSize, arcSize, 
//                180, -90, Arc2D.OPEN), true);
//        gp.append(new Arc2D.Float(x2 - arcSize, y0, arcSize, arcSize, 
//                90, -90, Arc2D.OPEN), true);
//        gp.append(new Arc2D.Float(x2 - arcSize, y1 - arcSize, arcSize, arcSize, 
//                0, -90, Arc2D.OPEN), true);
//        gp.append(new Arc2D.Float(x1, y1 - arcSize, arcSize, arcSize, 
//                -90, -90, Arc2D.OPEN), true);
//        gp.lineTo(x1, Math.min(y0 + arcSize / 2 + 14 + 14, y1 - arcSize / 2));
//        gp.closePath();
//        
//        return gp;
//    }
    
    private static JScrollPane createScrollPane(JComponent content) {
        JScrollPane res = new JScrollPane(content);
        res.getVerticalScrollBar().setUnitIncrement(16);
        res.setBorder(null);
        res.setOpaque(false);
        res.setBackground(null);
        res.getViewport().setBackground(null);
        res.getViewport().setOpaque(false);
        
        prepareScrollBar(res.getVerticalScrollBar());
        prepareScrollBar(res.getHorizontalScrollBar());
        
        return res;
    }
    
    private static void prepareScrollBar(JScrollBar scrollBar) {
        scrollBar.setOpaque(false);
        for (int i = scrollBar.getComponentCount() - 1; i >= 0; i--) {
            Component c = scrollBar.getComponent(i);
            
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(false);
            }
        }
    }

    private void moveOnTop() {
        JComponent parent = (JComponent) getParent();
        if (parent == null) return;
        
        int currentIndex = parent.getComponentZOrder(this);
        int topIndex = currentIndex;
        
        for (int i = currentIndex; i >= 0; i--) {
            if (parent.getComponent(i) instanceof GlassPane) {
                topIndex = i;
            }
        }
        
        if (currentIndex != topIndex) {
            parent.setComponentZOrder(this, topIndex);
            parent.revalidate();
            parent.repaint();
        }
    }
    
    public void focusGained(FocusEvent e) {
        moveOnTop();
    }

    public void mouseClicked(MouseEvent e) {
        moveOnTop();
    }

    public void focusLost(FocusEvent e) {
      if (myEditable) {
          hidePane();
      }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    private static class HideButton extends JButton {
        public HideButton() {
            setFocusable(false);
            setOpaque(false);
            setToolTipText(NbBundle.getMessage(getClass(), "LBL_GlassPane_Hide")); // NOI18N
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int w = getWidth();
            int h = getHeight();
            
            float size = Math.max(2, 0.5f * Math.min(w, h) - 4.6f);
            
            float cx = 0.5f * w;
            float cy = 0.5f * h;
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint(STROKE);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.JOIN_ROUND, 
                    BasicStroke.CAP_ROUND));
            g2.draw(new Line2D.Float(cx - size, cy - size, cx + size, cy + size));
            g2.draw(new Line2D.Float(cx + size, cy - size, cx - size, cy + size));
            g2.dispose();
        }
    }
    
    private static class HeaderLabel extends JLabel {
        public HeaderLabel(Icon icon, String text, Color color) {
            this(text);
            setIcon(icon);
            setForeground(color);
        }

        public HeaderLabel(String text) {
            this();
            setText(text);
        }
        
        public HeaderLabel() {
            setBackground(null);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            
            Object oldAntialiasing = g2.getRenderingHint(RenderingHints
                    .KEY_ANTIALIASING);
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            super.paintComponent(g);
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    oldAntialiasing);
        }
    }
    
    private static class UnderlineBorder implements Border {
        private int top;
        private int left;
        private int bottom;
        private int right;
        private Color color;
        
        public UnderlineBorder(int top, int left, int bottom, int right,
                Color color) 
        {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.color = color;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, 
                int w, int h) 
        {
            Color oldColor = g.getColor();
            
            g.setColor(color);
            g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
            g.setColor(oldColor);
        }
        
        public Insets getBorderInsets(Component c) {
            return new Insets(top, left, bottom, right);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private ActionListener myActionListener;
    private static final Color FILL = Color.WHITE;
    private static final Color STROKE = new Color(0x444444);
    private static final URL E_IMAGE_URL = Decoration.class.getResource("resources/e.png"); // NOI18N
    private static final Color TEXT_COLOR = new Color(0xBB2200);
    
    private static final double BASE_LENGTH = 20;
    
    public static final int INTERNAL_INSET=6;
}
