/*
 * testReformat2.java
 *
 * Created on January 15, 2007, 2:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.java.editor.formatting;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;

/**
 * Inner classes, anonymous classes, annotation formatting
 * @author ms159439
 */
public class testReformat2<E> extends ArrayList<E> implements List<E> {

    /** Creates a new instance of testReformat2 */
    public testReformat2() {
    }

    //                               .:xxxxxxxx:.
    //                             .xxxxxxxxxxxxxxxx.
    //                            :xxxxxxxxxxxxxxxxxxx:.
    //                           .xxxxxxxxxxxxxxxxxxxxxxx:
    //                          :xxxxxxxxxxxxxxxxxxxxxxxxx:
    //                          xxxxxxxxxxxxxxxxxxxxxxxxxxX:
    //                          xxx:::xxxxxxxx::::xxxxxxxxx:
    //                         .xx:   ::xxxxx:     :xxxxxxxx
    //                         :xx  x.  xxxx:  xx.  xxxxxxxx
    //                         :xx xxx  xxxx: xxxx  :xxxxxxx
    //                         'xx 'xx  xxxx:. xx'  xxxxxxxx
    //                          xx ::::::xx:::::.   xxxxxxxx
    //                          xx:::::.::::.:::::::xxxxxxxx
    //                          :x'::::'::::':::::':xxxxxxxxx.
    //                          :xx.::::::::::::'   xxxxxxxxxx
    //                          :xx: '::::::::'     :xxxxxxxxxx.
    //                         .xx     '::::'        'xxxxxxxxxx.
    //                       .xxxx                     'xxxxxxxxx.
    //                     .xxxx                         'xxxxxxxxx.
    //                   .xxxxx:                          xxxxxxxxxx.
    //                  .xxxxx:'                          xxxxxxxxxxx.
    //                 .xxxxxx:::.           .       ..:::_xxxxxxxxxxx:.
    //                .xxxxxxx''      ':::''            ''::xxxxxxxxxxxx.
    //                xxxxxx            :                  '::xxxxxxxxxxxx
    //               :xxxx:'            :                    'xxxxxxxxxxxx:
    //              .xxxxx              :                     ::xxxxxxxxxxxx
    //              xxxx:'                                    ::xxxxxxxxxxxx
    //              xxxx               .                      ::xxxxxxxxxxxx.
    //          .:xxxxxx               :                      ::xxxxxxxxxxxx::
    //          xxxxxxxx               :                      ::xxxxxxxxxxxxx:
    //          xxxxxxxx               :                      ::xxxxxxxxxxxxx:
    //          ':xxxxxx               '                      ::xxxxxxxxxxxx:'
    //            .:. xx:.                                   .:xxxxxxxxxxxxx'
    //          ::::::.'xx:.            :                  .:: xxxxxxxxxxx':
    //  .:::::::::::::::.'xxxx.                            ::::'xxxxxxxx':::.
    //  ::::::::::::::::::.'xxxxx                          :::::.'.xx.'::::::.
    //  ::::::::::::::::::::.'xxxx:.                       :::::::.'':::::::::
    //  ':::::::::::::::::::::.'xx:'                     .'::::::::::::::::::::..
    //    :::::::::::::::::::::.'xx                    .:: :::::::::::::::::::::::
    //  .:::::::::::::::::::::::. xx               .::xxxx :::::::::::::::::::::::
    //  :::::::::::::::::::::::::.'xxx..        .::xxxxxxx ::::::::::::::::::::'
    //  '::::::::::::::::::::::::: xxxxxxxxxxxxxxxxxxxxxxx :::::::::::::::::'
    //    '::::::::::::::::::::::: xxxxxxxxxxxxxxxxxxxxxxx :::::::::::::::'
    //        ':::::::::::::::::::_xxxxxx::'''::xxxxxxxxxx '::::::::::::'
    //             '':.::::::::::'                        `._'::::::''
    class myButton extends JButton {

        public myButton() {
            this.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    //              a8888b.
                    //             d888888b.
                    //             8P"YP"Y88
                    //             8|o||o|88
                    //             8'    .88
                    //             8`._.' Y8.
                    //            d/      `8b.
                    //           dP   .    Y8b.
                    //          d8:'  "  `::88b
                    //         d8"         'Y88b
                    //        :8P    '      :888
                    //         8a.   :     _a88P
                    //       ._/"Yaa_:   .| 88P|
                    //  jgs  \    YP"    `| 8P  `.
                    //  a:f  /     \.___.d|    .'
                    //       `--..__)8888P`._.'
                    myButtonActionPerformed(arg0);
                }
            });
        }

        /**
         * A javadoc comment
         */
        public void myButtonActionPerformed(ActionEvent evt) {
            System.out.println("-==-" +
                    "These smiling eyes are just a mirror for the sun.");
        }

        @Override
        @SuppressWarnings("unchecked")
        public void paint(Graphics arg0) {
            super.paint(arg0);
        }
    }

    @Deprecated
    class Doktor implements Serializable {
    }
}

class Objects {

    @SuppressWarnings({
"unchecked", "unused"
})
    static <T> T cast(final Object target) {
        return (T) target;
    }
}
