package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

/**A Stream of characters fed to the lexer from a InputStream that can
 * be rewound via mark()/rewind() methods.
 * <p>
 * A dynamic array is used to buffer up all the input characters.  Normally,
 * "k" characters are stored in the buffer.  More characters may be stored during
 * guess mode (testing syntactic predicate), or when LT(i>k) is referenced.
 * Consumption of characters is deferred.  In other words, reading the next
 * character is not done by conume(), but deferred until needed by LA or LT.
 * <p>
 *
 * @see antlr.CharQueue
 */
// SAS: added this class to handle Binary input w/ FileInputStream

import java.io.IOException;
import java.io.InputStream;

public class ByteBuffer extends InputBuffer {
    /** Create a character buffer */
    public ByteBuffer(InputStream input) {
        try {
            int pos = 0;
            char curChar;
            do {
                if (pos == data.length) {
                    resizeData(0);
                }
                curChar = (char) input.read();
                data[pos] = curChar;
                pos++;
            } while (curChar != CharScanner.EOF_CHAR);
	} catch (IOException io) {
            System.err.println("tmp error: can't load input: " + io);
        }
    }
}
