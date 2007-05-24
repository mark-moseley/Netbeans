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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Tomas Zezula
 */
public final class FastJar {

    private FastJar() {
    }
    
    
    private static final int GIVE_UP = 1<<16;
    
    
    private static class RandomAccessFileInputStream extends InputStream {
        
        private final RandomAccessFile b;
        
        public RandomAccessFileInputStream (RandomAccessFile b) {
            assert b != null;
            this.b = b;
        }        
    
        public int read (byte[] data, int offset, int size) throws IOException {
            int rem = available();
            int rlen;
            if (size<rem) {
                rlen = size;
            }
            else {
                rlen = rem;
            }
            return this.b.read(data, offset, rlen);
        }

        public int read() throws java.io.IOException {
            if (available()==0) {
                return -1;
            }
            else {
                return b.readByte();
            }
        }
        
        public int available () throws IOException {
             return (int) (this.b.length() - this.b.getFilePointer());
        }
        
        public void close () throws IOException {
            b.close ();
        }
    }
    
    public static final  class Entry {
        
        public final String name;
        final long offset;
        private final long dosTime;
        
        public Entry (String name, long offset, long time) {
            assert name != null;
            this.name = name;
            this.offset = offset;
            this.dosTime = time;
        }        
        
        public long getTime () {
            Date d = new Date((int)(((dosTime >> 25) & 0x7f) + 80),
                    (int)(((dosTime >> 21) & 0x0f) - 1),
                    (int)((dosTime >> 16) & 0x1f),
                    (int)((dosTime >> 11) & 0x1f),
                    (int)((dosTime >> 5) & 0x3f),
                    (int)((dosTime << 1) & 0x3e));
            return d.getTime();
        }
    }
    
    public static InputStream getInputStream (final File file, final Entry e) throws IOException {
        return getInputStream(file, e.offset);
    }
    
    static InputStream getInputStream (final File file, final long offset) throws IOException {
        RandomAccessFile  f = new RandomAccessFile (file, "r");     //NOI18N
        f.seek (offset);
        ZipInputStream in = new ZipInputStream (new RandomAccessFileInputStream (f));
        in.getNextEntry();
        return in;
    }
    
    static ZipEntry getZipEntry (final File file, final long offset) throws IOException {
        RandomAccessFile  f = new RandomAccessFile (file, "r");     //NOI18N
        try {
            f.seek (offset);
            ZipInputStream in = new ZipInputStream (new RandomAccessFileInputStream (f));
            try {
                return in.getNextEntry();
            } finally {
                in.close();
            }
        } finally {
            f.close ();
        }
    }

    public static Iterable<? extends Entry> list(File f) throws IOException {
        RandomAccessFile b = new RandomAccessFile (f,"r");      //NOI18N
        try {
            final long size = (int) b.length();
            b.seek (size-ZipFile.ENDHDR);                                           

            byte[] data = new byte[ZipFile.ENDHDR];        
            int giveup = 0;

            do {
                if (b.read(data, 0, ZipFile.ENDHDR)!=ZipFile.ENDHDR) {
                    throw new IOException ();
                }
                b.seek(b.getFilePointer()-(ZipFile.ENDHDR+1));
                giveup++;
                if (giveup > GIVE_UP) {
                    throw new IOException ();
                }
            } while (getsig(data) != ZipFile.ENDSIG);


            final long censize = endsiz(data);
            final long cenoff  = endoff(data);
            b.seek (cenoff);                                                        

            List<Entry> result = new LinkedList<Entry>();
            int cenread = 0;
            data = new byte[ZipFile.CENHDR];
            while (cenread < censize) {
                if (b.read(data, 0, ZipFile.CENHDR)!=ZipFile.CENHDR) {
                    throw new IOException ("No central table");         //NOI18N
                }             
                if (getsig(data) != ZipFile.CENSIG) {
                    throw new IOException("No central table");          //NOI18N
                }
                int cennam = cennam(data);
                int cenext = cenext(data);
                int cencom = cencom(data);
                long lhoff = cenoff(data);
                long centim = centim(data);
                String name = name(b, cennam);
                int seekby = cenext+cencom;
                int cendatalen = ZipFile.CENHDR + cennam + seekby;
                cenread+=cendatalen;
                result.add(new Entry(name,lhoff, centim));
                seekBy(b,seekby);
            }
            return result;
        } finally {
            b.close();
        }
    }

    private static final String name(final RandomAccessFile b, final int cennam) throws IOException {
	byte[] name = new byte[cennam];
	b.read(name, 0, cennam);
	return new String(name, "UTF-8");       //NOI18N
    }

    private static final long getsig(final byte[] b) throws IOException {return get32(b,0);}
    private static final long endsiz(final byte[] b) throws IOException {return get32(b,ZipFile.ENDSIZ);}
    private static final long endoff(final byte[] b) throws IOException {return get32(b,ZipFile.ENDOFF);}
    private static final long  cenlen(final byte[] b) throws IOException {return get32(b,ZipFile.CENLEN);}
    private static final long  censiz(final byte[] b) throws IOException {return get32(b,ZipFile.CENSIZ);}
    private static final long centim(final byte[] b) throws IOException {return get32(b,ZipFile.CENTIM);}
    private static final int  cennam(final byte[] b) throws IOException {return get16(b,ZipFile.CENNAM);}
    private static final int  cenext(final byte[] b) throws IOException {return get16(b,ZipFile.CENEXT);}
    private static final int  cencom(final byte[] b) throws IOException {return get16(b,ZipFile.CENCOM);}
    private static final long cenoff (final byte[] b) throws IOException {return get32(b,ZipFile.CENOFF);}
    private static final int lochow(final byte[] b) throws IOException {return get16(b,ZipFile.LOCHOW);}
    private static final int locname(final byte[] b) throws IOException {return get16(b,ZipFile.LOCNAM);}
    private static final int locext(final byte[] b) throws IOException {return get16(b,ZipFile.LOCEXT);}
    private static final long locsiz(final byte[] b) throws IOException {return get32(b,ZipFile.LOCSIZ);}
    
    private static final void seekBy(final RandomAccessFile b, int offset) throws IOException {
        b.seek(b.getFilePointer() + offset);
    }

    private static final int get16(final byte[] b, int off) throws IOException {        
        final int b1 = b[off];
	final int b2 = b[off+1];
        return (b1 & 0xff) | ((b2 & 0xff) << 8);
    }

    private static final long get32(final byte[] b, int off) throws IOException {
	final int s1 = get16(b, off);
	final int s2 = get16(b, off+2);
        return s1 | ((long)s2 << 16);
    }

}
