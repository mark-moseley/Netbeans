#!/bin/bash

file="diff20kb.diff"
x=0

echo "" > $file
# write while the file size is less than 20kb
while [ `ls -s $file|cut -d' ' -f1` -lt 41 ]
do
    x=$((x+1))
    cat >> $file << EOF
--- file$x.pass	16 Jul 2007 17:33:29 -0000	1.8
+++ file$x.pass	6 Nov 2007 10:21:16 -0000	1.9
@@ -1,8 +1,8 @@
-[<% Li|, LinkedList <E>, <% LinkedList, 491]
+[<% Li|, LinkedList <E>, <% LinkedList, 458]
 LinkageError
 LinkedList <E>
-End cursor position = 505
-[<% C|, Card, <% Card, 603]
+End cursor position = 472
+[<% C|, Card, <% Card, 570]
 ServletConfig config
 Object clone ()
 char
@@ -21,22 +21,23 @@
 Comparable <T>
 Compiler
 Cookie
-End cursor position = 611
-[<% LinkedList l = new LinkedList();l.|, void clear (), <% LinkedList l = new LinkedList();l.clear(), 821]
-boolean add (Object o )
+End cursor position = 578
+[<% LinkedList l = new LinkedList();l.|, void clear (), <% LinkedList l = new LinkedList();l.clear(), 788]
+boolean add (Object e )
 void add (int index , Object element )
 boolean addAll (Collection c )
 boolean addAll (int index , Collection c )
-void addFirst (Object o )
-void addLast (Object o )
+void addFirst (Object e )
+void addLast (Object e )
 void clear ()
 Object clone ()
 boolean contains (Object o )
 boolean containsAll (Collection c )
+Iterator descendingIterator ()
 Object element ()
 boolean equals (Object o )
 Object get (int index )
-Class <? extends Object> getClass ()
+Class <?> getClass ()
 Object getFirst ()
 Object getLast ()
 int hashCode ()
@@ -48,15 +49,25 @@
 ListIterator listIterator (int index )
 void notify ()
 void notifyAll ()
-boolean offer (Object o )
+boolean offer (Object e )
+boolean offerFirst (Object e )
+boolean offerLast (Object e )
 Object peek ()
+Object peekFirst ()
+Object peekLast ()
 Object poll ()
+Object pollFirst ()
+Object pollLast ()
+Object pop ()
+void push (Object e )
 Object remove ()
 boolean remove (Object o )
 Object remove (int index )
 boolean removeAll (Collection c )
 Object removeFirst ()
+boolean removeFirstOccurrence (Object o )
 Object removeLast ()
+boolean removeLastOccurrence (Object o )
 boolean retainAll (Collection c )
 Object set (int index , Object element )
 int size ()
@@ -67,4 +78,19 @@
 void wait ()
 void wait (long timeout )
 void wait (long timeout , int nanos )
-End cursor position = 866
+End cursor position = 833
+[<% org.test.TestBean t = new org.test.TestBean(); t.|, void setName (String name ), <% org.test.TestBean t = new org.test.TestBean(); t.setName(name), 1056]
+boolean equals (Object obj )
+Class <?> getClass ()
+Integer getId ()
+String getName ()
+int hashCode ()
+void notify ()
+void notifyAll ()
+void setId (Integer id )
+void setName (String name )
+String toString ()
+void wait ()
+void wait (long timeout )
+void wait (long timeout , int nanos )
+End cursor position = 1121

EOF

done

