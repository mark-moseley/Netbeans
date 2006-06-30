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

package org.netbeans.upgrade;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.upgrade.XMLStorage.Attribs;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class contains support static methods for loading / saving and 
 * translating coloring (fontsColors.xml) files. It calls XMLStorage utilities.
 *
 * @author Jan Jancura
 */
class ColoringStorage {

    
    // load ....................................................................
    
    static Map loadColorings (
        InputStream is, 
        String name
    ) {
        return (Map) XMLStorage.load (is, name, new ColoringsReader ());
    }    

    private static class ColoringsReader extends XMLStorage.Handler {
        private Map colorings = new HashMap ();
        private SimpleAttributeSet last;
        
        Object getResult () {
            return colorings;
        }
        
        public void startElement (
            String uri, 
            String localName,
            String name, 
            Attributes attributes
        ) throws SAXException {
            try {
                if (name.equals ("fontscolors")) {
                } else
                if (name.equals ("fontcolor")) {
                    String n = (String) attributes.getValue ("syntaxName");
                    if (n == null)
                        n = (String) attributes.getValue ("name");
                    if (n == null) {
                        System.out.println("no syntaxName " + attributes);
                        return;
                    }
                    SimpleAttributeSet a = new SimpleAttributeSet ();
                    a.addAttribute (
                        StyleConstants.NameAttribute, 
                        n
                    );
                    if (attributes.getValue ("bgColor") != null)
                        a.addAttribute (
                            StyleConstants.Background, 
                            XMLStorage.stringToColor (attributes.getValue ("bgColor"))
                        );
                    if (attributes.getValue ("foreColor") != null)
                        a.addAttribute (
                            StyleConstants.Foreground, 
                            XMLStorage.stringToColor (attributes.getValue ("foreColor"))
                        );
                    if (attributes.getValue ("underline") != null)
                        a.addAttribute (
                            StyleConstants.Underline, 
                            XMLStorage.stringToColor (attributes.getValue ("underline"))
                        );
                    if (attributes.getValue ("strikeThrough") != null)
                        a.addAttribute (
                            StyleConstants.StrikeThrough, 
                            XMLStorage.stringToColor (attributes.getValue ("strikeThrough"))
                        );
                    if (attributes.getValue ("waveUnderlined") != null)
                        a.addAttribute (
                            "waveUnderlined", 
                            XMLStorage.stringToColor (attributes.getValue ("waveUnderlined"))
                        );
                    if (attributes.getValue ("default") != null)
                        a.addAttribute (
                            "default", 
                            (String) attributes.getValue ("default")
                        );
                    colorings.put (n, a);
                    last = a;
                } else
                if (name.equals ("font")) {
                    if (attributes.getValue ("name") != null)
                        last.addAttribute (
                            StyleConstants.FontFamily,
                            attributes.getValue ("name")
                        );
                    if (attributes.getValue ("size") != null)
                        try {
                            last.addAttribute (
                                StyleConstants.FontSize,
                                Integer.decode (attributes.getValue ("size"))
                            );
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace ();
                        }
                    if (attributes.getValue ("style") != null) {
                        if (attributes.getValue ("style").indexOf ("bold") >= 0)
                            last.addAttribute (
                                StyleConstants.Bold,
                                Boolean.TRUE
                            );
                        if (attributes.getValue ("style").indexOf ("italic") >= 0)
                            last.addAttribute (
                                StyleConstants.Italic,
                                Boolean.TRUE
                            );
                    }
                }
            } catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        public InputSource resolveEntity (String pubid, String sysid) {
            return new InputSource (
		new java.io.ByteArrayInputStream (new byte [0])
	    );
        }
    }    

    
    // save colorings ..........................................................
    
    static void saveColorings (FileObject fo, Collection colorings) {
        final StringBuffer sb = XMLStorage.generateHeader ();
        XMLStorage.generateFolderStart (sb, "fontscolors", null, "");
        Iterator it = colorings.iterator ();
        while (it.hasNext ()) {
            AttributeSet category = (AttributeSet) it.next ();
            Attribs attributes = new Attribs (true);
            attributes.add (
		"name", 
		(String) category.getAttribute (StyleConstants.NameAttribute)
	    );
	    if (category.isDefined (StyleConstants.Foreground))
		attributes.add (
		    "foreColor", 
		    XMLStorage.colorToString (
			(Color) category.getAttribute (StyleConstants.Foreground)
		    )
		);
	    if (category.isDefined (StyleConstants.Background))
		attributes.add (
		    "bgColor", 
		    XMLStorage.colorToString (
			(Color) category.getAttribute (StyleConstants.Background)
		    )
		);
	    if (category.isDefined (StyleConstants.StrikeThrough))
		attributes.add (
		    "strikeThrough", 
		    XMLStorage.colorToString (
			(Color) category.getAttribute (StyleConstants.StrikeThrough)
		    )
		);
	    if (category.isDefined ("waveUnderlined"))
		attributes.add (
		    "waveUnderlined", 
		    XMLStorage.colorToString (
			(Color) category.getAttribute ("waveUnderlined")
		    )
		);
	    if (category.isDefined (StyleConstants.Underline))
		attributes.add (
		    "underline", 
		    XMLStorage.colorToString (
			(Color) category.getAttribute (StyleConstants.Underline)
		    )
		);
	    if (category.isDefined ("default"))
                attributes.add (
		    "default", 
		    (String) category.getAttribute ("default")
		);
	    if ( category.isDefined (StyleConstants.FontFamily) ||
                 category.isDefined (StyleConstants.FontSize) ||
                 category.isDefined (StyleConstants.Bold) ||
                 category.isDefined (StyleConstants.Italic)
            ) {
		XMLStorage.generateFolderStart (sb, "fontcolor", attributes, "    ");
		attributes = new Attribs (true);
                if (category.isDefined (StyleConstants.FontFamily))
                    attributes.add (
                        "name", 
                        (String) category.getAttribute (StyleConstants.FontFamily)
                    );
                if (category.isDefined (StyleConstants.FontSize))
                    attributes.add (
                        "size", 
                        "" + category.getAttribute (StyleConstants.FontSize)
                    );
                if (category.isDefined (StyleConstants.Bold) ||
                    category.isDefined (StyleConstants.Italic)
                ) {
                    Boolean bold = Boolean.FALSE, italic = Boolean.FALSE;
                    if (category.isDefined (StyleConstants.Bold))
                        bold = (Boolean) category.getAttribute (StyleConstants.Bold);
                    if (category.isDefined (StyleConstants.Italic))
                        italic = (Boolean) category.getAttribute (StyleConstants.Italic);
                    attributes.add ("style", 
                        bold.booleanValue () ?
                            (italic.booleanValue () ?
                                "bold+italic" : 
                                "bold") :
                            (italic.booleanValue () ?
                                "italic" : "plain")
                    );
                }
		XMLStorage.generateLeaf (sb, "font", attributes, "        ");
		XMLStorage.generateFolderEnd (sb, "fontcolor", "    ");
	    } else
		XMLStorage.generateLeaf (sb, "fontcolor", attributes, "    ");
        }
        XMLStorage.generateFolderEnd (sb, "fontscolors", "");
        XMLStorage.save (fo, new String (sb));
    }
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    private static String getFolderName (
        String[] mimeTypes, 
        String profile
    ) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("Editors");
        int i, k = mimeTypes.length;
        for (i = 0; i < k; i++)
            sb.append ('/').append (mimeTypes [i]);
        if (profile != null)
            sb.append ('/').append (profile);
        return sb.append ('/').toString ();
    }    
    
    /**
     * Crates FileObject for given mimeTypes and profile.
     */ 
    private static FileObject createFileObject (
        FileObject      root,
        String[]        mimeTypes, 
        String          profile,
        String          fileName
    ) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        try {
            FileObject fo = getFO (fs.getRoot (), "Editors");
            int i, k = mimeTypes.length;
            for (i = 0; i < k; i++)
                fo = getFO (fo, mimeTypes [i]);
            if (profile != null)
                fo = getFO (fo, profile);
            if (fileName == null)
                return fo;
            FileObject fo1 = fo.getFileObject (fileName);
            if (fo1 != null) return fo1;
            return fo.createData (fileName);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }    
       
    private static FileObject getFO (FileObject fo, String next) throws IOException {
        FileObject fo1 = fo.getFileObject (next);
        if (fo1 == null) 
            return fo.createFolder (next);
        return fo1;
    }
}
