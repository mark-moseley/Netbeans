/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.xmlpull.v1.XmlPullParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.openide.util.Exceptions;

public class DelayExtensionProvider implements PacketExtensionProvider {

    public DelayExtensionProvider() {
    }

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        Date stamp = null;
        try {
            synchronized (DelayInformation.UTC_FORMAT) {
                stamp = DelayInformation.UTC_FORMAT.parse(parser.getAttributeValue("", "stamp")); //NOI18N
            }
        } catch (ParseException e) {
            // Try again but assuming that the date follows JEP-82 format
            // (Jabber Date and Time Profiles)
            try {
                synchronized (DelayInformation.NEW_UTC_FORMAT) {
                    stamp = DelayInformation.NEW_UTC_FORMAT
                            .parse(parser.getAttributeValue("", "stamp")); //NOI18N
                }
            } catch (ParseException e1) {
                try {
                    // Last attempt. Try parsing the date assuming that it does not include milliseconds
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //NOI18N
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC")); //NOI18N
                    stamp = formatter.parse(parser.getAttributeValue("", "stamp")); //NOI18N
                } catch (ParseException e2) {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); //NOI18N
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC")); //NOI18N
                        String stampString = parser.getAttributeValue("", "stamp"); //NOI18N
                        String modifed = stampString.substring(0, 19) + "UTC" + stampString.substring(stampString.length() - 6); //NOI18N
                        stamp = formatter.parse(modifed);
                    } catch (ParseException e3) {
                            Exceptions.printStackTrace(e3);
                            stamp = new Date();
                    }
                }
            }
        }
        DelayInformation delayInformation = new DelayInformation(stamp);
        delayInformation.setFrom(parser.getAttributeValue("", "from")); //NOI18N
        delayInformation.setReason(parser.nextText());
        return delayInformation;
    }

}