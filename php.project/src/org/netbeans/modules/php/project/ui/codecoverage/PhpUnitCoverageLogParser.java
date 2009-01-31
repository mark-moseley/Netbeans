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

package org.netbeans.modules.php.project.ui.codecoverage;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.ClassVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.LineVO;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser of PHPUnit XML coverage files (version 3.x).
 * @author Tomas Mysik
 */
public class PhpUnitCoverageLogParser extends DefaultHandler {
    enum Content { COVERAGE, FILE, CLASS };
    private static final Logger LOGGER = Logger.getLogger(PhpUnitCoverageLogParser.class.getName());

    private final XMLReader xmlReader;
    private final CoverageVO coverage;
    private FileVO file; // actual file
    private ClassVO clazz; // actual class
    private Content content = null;

    private PhpUnitCoverageLogParser(CoverageVO coverage) throws SAXException {
        assert coverage != null;
        this.coverage = coverage;
        xmlReader = PhpProjectUtils.createXmlReader();
        xmlReader.setContentHandler(this);
    }

    public static void parse(Reader reader, CoverageVO coverage) {
        try {
            PhpUnitCoverageLogParser parser = new PhpUnitCoverageLogParser(coverage);
            parser.xmlReader.parse(new InputSource(reader));
        } catch (SAXException ex) {
            // ignore (this can happen e.g. if one interrupts debugging)
            LOGGER.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            assert false;
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("coverage".equals(qName)) { // NOI18N
            processCoverage(attributes);
        } else if ("file".equals(qName)) { // NOI18N
            processFile(attributes);
        } else if ("class".equals(qName)) { // NOI18N
            processClass(attributes);
        } else if ("metrics".equals(qName)) { // NOI18N
            processMetrics(attributes);
        } else if ("line".equals(qName)) { // NOI18N
            processLine(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            endFile();
        } else if ("class".equals(qName)) { // NOI18N
            endClass();
        }
    }

    private void processCoverage(Attributes attributes) {
        assert content == null;
        content = Content.COVERAGE;
        coverage.setGenerated(getGenerated(attributes));
        coverage.setPhpUnitVersion(getPhpUnit(attributes));
    }

    private void processFile(Attributes attributes) {
        assert content.equals(Content.COVERAGE);
        assert file == null;
        content = Content.FILE;
        file = new FileVO(getPath(attributes));
        coverage.addFile(file);
    }

    private void processClass(Attributes attributes) {
        assert content.equals(Content.FILE);
        assert file != null;
        assert clazz == null;
        content = Content.CLASS;
        clazz = new ClassVO(getName(attributes), getNamespace(attributes));
        file.addClass(clazz);
    }

    private void processMetrics(Attributes attributes) {
        assert content != null;
        switch (content) {
            case COVERAGE:
                assert file == null;
                assert clazz == null;
                coverage.setMetrics(new CoverageVO.CoverageMetricsVO(
                        getFiles(attributes),
                        getLoc(attributes),
                        getNcloc(attributes),
                        getClasses(attributes),
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            case FILE:
                assert file != null;
                assert clazz == null;
                file.setMetrics(new CoverageVO.FileMetricsVO(
                        getLoc(attributes),
                        getNcloc(attributes),
                        getClasses(attributes),
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            case CLASS:
                assert file != null;
                assert clazz != null;
                clazz.setMetrics(new CoverageVO.ClassMetricsVO(
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            default:
                assert false : "Unknown content type: " + content;
                break;
        }
    }

    private void processLine(Attributes attributes) {
        assert file != null;
        assert clazz == null;
        file.addLine(new LineVO(
                getNum(attributes),
                getType(attributes),
                getCount(attributes)));
    }

    private void endFile() {
        assert content.equals(Content.FILE);
        assert file != null;
        file = null;
        content = Content.COVERAGE;
    }

    private void endClass() {
        assert content.equals(Content.CLASS);
        assert clazz != null;
        clazz = null;
        content = Content.FILE;
    }

    private long getGenerated(Attributes attributes) {
        return getLong(attributes, "generated"); // NOI18N
    }

    private String getPhpUnit(Attributes attributes) {
        return attributes.getValue("phpunit"); // NOI18N
    }

    private String getPath(Attributes attributes) {
        return FileUtil.normalizeFile(new File(attributes.getValue("name"))).getAbsolutePath(); // NOI18N
    }

    private String getName(Attributes attributes) {
        return attributes.getValue("name"); // NOI18N
    }

    private String getNamespace(Attributes attributes) {
        return attributes.getValue("namespace"); // NOI18N
    }

    private int getNum(Attributes attributes) {
        return getInt(attributes, "num"); // NOI18N
    }

    private String getType(Attributes attributes) {
        return attributes.getValue("type"); // NOI18N
    }

    private int getCount(Attributes attributes) {
        return getInt(attributes, "count"); // NOI18N
    }

    private int getFiles(Attributes attributes) {
        return getInt(attributes, "files"); // NOI18N
    }

    private int getLoc(Attributes attributes) {
        return getInt(attributes, "loc"); // NOI18N
    }

    private int getNcloc(Attributes attributes) {
        return getInt(attributes, "ncloc"); // NOI18N
    }

    private int getClasses(Attributes attributes) {
        return getInt(attributes, "classes"); // NOI18N
    }

    private int getMethods(Attributes attributes) {
        return getInt(attributes, "methods"); // NOI18N
    }

    private int getCoveredMethods(Attributes attributes) {
        return getInt(attributes, "coveredmethods"); // NOI18N
    }

    private int getStatements(Attributes attributes) {
        return getInt(attributes, "statements"); // NOI18N
    }

    private int getCoveredStatements(Attributes attributes) {
        return getInt(attributes, "coveredstatements"); // NOI18N
    }

    private int getElements(Attributes attributes) {
        return getInt(attributes, "elements"); // NOI18N
    }

    private int getCoveredElements(Attributes attributes) {
        return getInt(attributes, "coveredelements"); // NOI18N
    }

    private int getInt(Attributes attributes, String name) {
        int i = -1;
        try {
            i = Integer.parseInt(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return i;
    }

    private long getLong(Attributes attributes, String name) {
        long l = -1;
        try {
            l = Long.parseLong(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return l;
    }
}
