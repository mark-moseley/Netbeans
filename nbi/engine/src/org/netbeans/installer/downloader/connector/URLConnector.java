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
 * 
 * $Id$
 */
package org.netbeans.installer.downloader.connector;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */

public class URLConnector {
  
  private final MyProxySelector proxySelector = new MyProxySelector();
  
  public static final int SECOND = 1000;
  public static final int MINUTE = 60 * SECOND;
  int readTimeout = MINUTE / 3;
  int connectTimeout = MINUTE / 3;
  boolean doInput = true;
  boolean doOutput = false;
  boolean useCaches = false;
  
  boolean useProxy = false;
  
  private File settingFile;
  
  private static URLConnector instance;
  
  public static URLConnector getConnector() {
    if (instance != null) return instance;
    instance = new URLConnector();
     instance.settingFile = new File(DownloadManager.DM.getWd(),"settings.xml");
    if (instance.settingFile.exists()) {
       instance.load();
         LogManager.log("configuration was load from file: " + instance.settingFile);
       } else LogManager.log("file not exist, default configuration was set!");
    return instance;
  }
  
  private void addSystemProxies() {
    String host = System.getProperty("deployment.proxy.http.host");
    String stringPort = System.getProperty("deployment.proxy.http.port");
    int port = stringPort != null ? Integer.parseInt(stringPort): -1;
    if (host != null && port != -1) {
      final Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host , port));
      proxySelector.add(new MyProxy(httpProxy, MyProxyType.HTTP));
      useProxy = true;
    }
    
    host = System.getProperty("deployment.proxy.ftp.host");
    stringPort = System.getProperty("deployment.proxy.ftp.port");
    port = stringPort != null ? Integer.parseInt(stringPort): -1;
    if (host != null && port != -1) {
      final Proxy ftpProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
      proxySelector.add(new MyProxy(ftpProxy, MyProxyType.FTP));
      useProxy = true;
    }
    
    host = System.getProperty("deployment.proxy.socks.host");
    stringPort = System.getProperty("deployment.proxy.socks.port");
    port = stringPort != null ? Integer.parseInt(stringPort): -1;
    if (host != null && port != -1) {
      final Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
      proxySelector.add(new MyProxy(socksProxy, MyProxyType.SOCKS));
      useProxy = true;
    }
  }
  
  private URLConnector() {
    addSystemProxies();
  }
  
  private void load() {
    try {
      Document settings = DomUtil.parseXmlFile(settingFile);
      final DomVisitor visitor = new RecursiveDomVisitor() {
        public void visit(Element elemet) {
          if ("readTimeout".equals(elemet.getNodeName())) {
            readTimeout = Integer.parseInt(elemet.getTextContent().trim());
          } else if ("connectTimeout".equals(elemet.getNodeName())) {
            connectTimeout = Integer.parseInt(elemet.getTextContent().trim());
          } else if ("useProxy".equals(elemet.getNodeName())) {
            useProxy = Boolean.valueOf(elemet.getTextContent().trim());
          } else if ("proxy".equals(elemet.getNodeName())) {
            final MyProxy proxy = new MyProxy();
            proxy.readXML(elemet);
            proxySelector.add(proxy);
          } else super.visit(elemet);
        }
      };
      visitor.visit(settings);
    } catch (IOException ex) {
      LogManager.log("I/O error during loading. Default configuration was set!");
    } catch(ParseException ex) {
      LogManager.log("fail load connector settings: corrupted unparsable xml. Defualt configuration set.");
    }
  }
  
  public synchronized void dump() {
    try {
      final Document document = DomUtil.parseXmlFile("<connectSettings/>");
      final Element root = document.getDocumentElement();
      DomUtil.addElemet(root, "readTimeout", String.valueOf(readTimeout));
      DomUtil.addElemet(root, "connectTimeout", String.valueOf(connectTimeout));
      DomUtil.addElemet(root, "useProxy", String.valueOf(useProxy));
      DomUtil.addChild(root, proxySelector);
      DomUtil.writeXmlFile(document, settingFile);
    } catch(IOException ex) {
      LogManager.log("I/O error. Can't dump configuration");
    }  catch(ParseException wontHappend) {
        LogManager.log("fatal error can't parse <connectSettings/>");
    }
  }
  
  public void addProxy(MyProxy proxy) {
    proxySelector.add(proxy);
    dump();
  }
  
  public void removeProxy(MyProxyType type) {
    proxySelector.remove(type);
    dump();
  }
  
  public void setReadTimeout(int readTimeout) {
    if (readTimeout < 0) throw new IllegalArgumentException();
    this.readTimeout = readTimeout;
    dump();
  }
  
  public void setConnectTimeout(int connectTimeout) {
    if (connectTimeout < 0) throw new IllegalArgumentException();
    this.connectTimeout = connectTimeout;
    dump();
  }
  
  public void setUseProxy(boolean useProxy) {
    this.useProxy = useProxy;
    dump();
  }
  
  public int getReadTimeout() {
    return readTimeout;
  }
  
  public int getConnectTimeout() {
    return connectTimeout;
  }
  
  public boolean getUseProxy() {
    return useProxy;
  }
  
  public Proxy getPorxy(MyProxyType type) {
    final MyProxy proxy = proxySelector.getForType(type);
    return (proxy != null) ? proxy.getProxy(): null;
  }
  
  public URLConnection establishConnection(URL url) throws IOException {
    return establishConnection(url, new ArrayList<Pair<String, String>>(0));
  }
  
  public URLConnection establishConnection(URL url, List<Pair<String, String>> headerFields) throws IOException {
    Proxy proxy = null;
    URI uri = null;
    try {
      proxy = useProxy ? proxySelector.select(uri = url.toURI()).get(0): Proxy.NO_PROXY;
      final URLConnection connection = getConnectionThrowProxy(url, proxy);
      configure(connection, headerFields);
      connection.connect();
      return connection;
    } catch (URISyntaxException ex) {
      LogManager.log(url + " not complies with RFC 2396 can't be converted to URI");
      return url.openConnection(Proxy.NO_PROXY);
    } catch(IOException ex) {
      LogManager.log("failed to connect throw proxy: " + proxy + " to url: " + url
              + " " + ex.getClass().getName()+ ": " + ex.getMessage());
      proxySelector.connectFailed(uri, proxy.address(), ex);
      throw new IOException("failed to connect throw proxy: " + proxy + " to url: " + url
              + " " + ex.getClass().getName()+ ": " + ex.getMessage());
    }
  }
  
  private URLConnection getConnectionThrowProxy(URL url, Proxy proxy) throws IOException {
    try {
      return url.openConnection(proxy);
    }catch (SecurityException ex) {
      LogManager.log("no permission to connect to proxy: " + proxy);
    }catch (IllegalArgumentException ex) {
      LogManager.log(" proxy: " + proxy + "has wrong type!");
    } catch (UnsupportedOperationException ex) {
      LogManager.log(url.getProtocol() + " handler don't support openConnection throw proxy!");
    }
    throw new IOException("fail to configure proxy:" + proxy + " to url: " + url);
  }
  
  private void configure(URLConnection connection, List<Pair<String, String>> headerFields) {
    connection.setConnectTimeout(connectTimeout);
    connection.setReadTimeout(readTimeout);
    connection.setDoInput(doInput);
    connection.setDoOutput(doOutput);
    connection.setUseCaches(useCaches);
    for (Pair<String, String> pair : headerFields) {
      connection.setRequestProperty(pair.getFirst(), pair.getSecond());
    }
  }
}
