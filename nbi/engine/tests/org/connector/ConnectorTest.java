/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import org.MyTestCase;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class ConnectorTest extends WithServerTestCase {
  
  public static URL smallest;
  public static URL small;
  public static URL noResource;
  
  static {
    try {
      smallest = new URL("http://localhost:8080/" + TestDataGenerator.testFiles[0]);
      small = new URL("http://127.0.0.1:8080/" + TestDataGenerator.testFiles[1]);
      noResource = new URL("http://localhost:8080/kadabra.data");
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    }
  }
  
  public void testDirect() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      assertEquals(TestDataGenerator.testFileSizes[0], connection.getContentLength());
      connection.getInputStream().close();
      connection = connector.establishConnection(small);
      assertEquals(TestDataGenerator.testFileSizes[1], connection.getContentLength());
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    try {
      connection = connector.establishConnection(noResource);
      connection.getInputStream().close();
      fail();
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    } finally {
      if (connection != null) {
        try {
          final InputStream in = connection.getInputStream();
          if (in != null) in.close();
        } catch (IOException ignored) {//skip
        }
      }
    }
  }
  
  public void testWithProxy() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();//what's the matter?It's seems to me that sometimes for localhost java URLConnection just ignor proxy as argument.
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
  
  public void testWithProxyWithByPassList() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    connector.addByPassHost("127.0.0.1");
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
    try {
      connection = connector.establishConnection(small);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
  }
}
