/*
 * @(#)Style.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 *
 * Linked list of class names and styles
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.0, 1999/05/22
 * @version 1.0.1, 1999/05/29
 * @version 1.1, 1999/06/18 Java Bean
 * @version 1.2, 1999/07/10 Tidy Release 7 Jul 1999
 * @version 1.3, 1999/07/30 Tidy Release 26 Jul 1999
 * @version 1.4, 1999/09/04 DOM support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class Style {

    public Style(String tag, String tagClass, String properties, Style next)
    {
        this.tag  = tag;
        this.tagClass = tagClass;
        this.properties = properties;
        this.next  = next;
    }

    public Style(String tag, String tagClass, String properties)
    {
        this(tag, tagClass, properties, null);
    }

    public Style()
    {
        this(null, null, null, null);
    }

    public String tag;
    public String tagClass;
    public String properties;
    public Style  next;

}
