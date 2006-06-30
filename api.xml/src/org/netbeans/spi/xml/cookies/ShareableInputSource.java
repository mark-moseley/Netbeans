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

package org.netbeans.spi.xml.cookies;

import org.xml.sax.InputSource;
import java.io.*;

/**
 * Input source that can be sequentially shared including its steams.
 * Use {@link #reset} before passing it to subsequent procesor. It
 * is read only.
 */
final class ShareableInputSource extends InputSource {

    private ByteStream stream;
    private CharacterStream reader;
    private boolean initialized[] = new boolean[2];

    private final InputSource peer;
    private final int bufferSize;
    // #32939 keep the buffer big enough to be able to validate large XML documets
    private final static int BUFFER_SIZE = 1024 * 1024 + 7;
    private IOException resetException;

    public static ShareableInputSource create(InputSource peer) {
        if (peer == null) throw new NullPointerException();
        if (peer instanceof ShareableInputSource) {
            return (ShareableInputSource) peer;
        } else {
            return new ShareableInputSource(peer, BUFFER_SIZE);
        }
    }

    private ShareableInputSource(InputSource peer, int bufferSize) {
        this.peer = peer;
        this.bufferSize = bufferSize;
    }

    public InputStream getByteStream() {
        InputStream in = peer.getByteStream();
        if (initialized[1] == false && in != null) {
           stream = new ByteStream(in , bufferSize);
           stream.mark(bufferSize);
           initialized[1] = true;
        }
        return stream;
    }

    public Reader getCharacterStream() {
        Reader in = peer.getCharacterStream();
        if (initialized[0] == false && in != null) {
            reader = new CharacterStream(in, bufferSize/2);
            initialized[0] = true;
            try {
                reader.mark(bufferSize/2);
            } catch (IOException ex) {
                resetException = ex;
            }
        }
        return reader;
    }

    /**
     * Prepate this instance for next parser
     */
    public void reset() throws IOException {
        if (resetException != null) throw resetException;
        if (initialized[1]) stream.reset();
        if (initialized[0]) reader.reset();
    }

    /**
     * Close shared streams
     */
    public void closeAll() throws IOException {
        if (initialized[1]) stream.internalClose();
        if (initialized[0]) reader.internalClose();        
    }
    
    public String getEncoding() {
        return peer.getEncoding();
    }

    public String getSystemId() {
        return peer.getSystemId();
    }

    public String getPublicId() {
        return peer.getPublicId();
    }
    
    private static class ByteStream extends BufferedInputStream {
        public ByteStream(InputStream peer, int buffer) {
            super(peer, buffer);
        }
        
        public void close() throws IOException {
            // nothing, we are shared
        }
        
        private void internalClose() throws IOException {
            super.close();
        }
    }
    
    private static class CharacterStream extends BufferedReader {
        public CharacterStream(Reader peer, int buffer) {
            super(peer, buffer);
        }
        
        public void close() throws IOException {
            // nothing, we are shared
        }
        
        private void internalClose() throws IOException {
            super.close();
        }
    }
    
}
