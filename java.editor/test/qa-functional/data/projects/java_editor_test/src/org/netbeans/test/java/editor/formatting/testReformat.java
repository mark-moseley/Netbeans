/*
* testReformat.java
*
* Created on January 10, 2007, 4:29 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.test.java.editor.formatting;

/**
*
* @author jp159440
*/
public class testReformat {

/** Creates a new instance of testReformat */
public testReformat() {
}

public String doSmt(int a,
String b,
boolean c) {
do {
switch (a) {
case 1:
System.out.println("one");
break;
case 2:
System.out.println("two");
default:
if(a>4) {
while(a!=4) {
a--;
}
} else if(a>2)
a=4;
}
} while (a==2);
  //comment

/**
*  a     g
*   b   f
*    c e
*     d
*    c e
*   b   f
*  a     g
*/
return "done";
// comments
}

}
