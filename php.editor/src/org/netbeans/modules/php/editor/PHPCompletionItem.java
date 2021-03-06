/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.php.editor.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class PHPCompletionItem implements CompletionProposal {
    private static final String PHP_KEYWORD_ICON = "org/netbeans/modules/php/editor/resources/php16Key.png"; //NOI18N
    protected static ImageIcon keywordIcon = null;
    protected final CompletionRequest request;
    private final ElementHandle element;

    PHPCompletionItem(ElementHandle element, CompletionRequest request) {
        this.request = request;
        this.element = element;
        keywordIcon = new ImageIcon(ImageUtilities.loadImage(PHP_KEYWORD_ICON));
    }

    public int getAnchorOffset() {
        return request.anchor;
    }

    public ElementHandle getElement() {
        return element;
    }

    public String getName() {
        return element.getName();
    }

    public String getInsertPrefix() {
        return getName();
    }

    public String getSortText() {
        if (getElement() instanceof IndexedElement) {
            IndexedElement indexedElement = (IndexedElement) getElement();

            if (indexedElement.isResolved()) {
                return "-" + getName(); //NOI18N

            }
        }
        return getName();
    }

    public int getSortPrioOverride() {
        return 0;
    }

    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    public ImageIcon getIcon() {
        return null;
    }

    public Set<Modifier> getModifiers() {
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    public boolean isSmart() {
        // true for elements defined in the currently file
        if (getElement() instanceof IndexedElement) {
            IndexedElement indexedElement = (IndexedElement) getElement();
            String url = indexedElement.getFilenameUrl();
            return url != null && url.equals(request.currentlyEditedFileURL);
        }

        return false;
    }

    public String getCustomInsertTemplate() {
        return null;
    }

    public String getRhsHtml(HtmlFormatter formatter) {
        if (element.getIn() != null) {
            formatter.appendText(element.getIn());
            return formatter.getText();
        } else if (element instanceof IndexedElement) {
            IndexedElement ie = (IndexedElement) element;
            if (ie.getFile().isPlatform()){
                return NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform");
            }

            String filename = ie.getFilenameUrl();
            if (filename != null) {
                int index = filename.lastIndexOf('/');
                if (index != -1) {
                    filename = filename.substring(index + 1);
                }

                formatter.appendText(filename);
                return formatter.getText();
            }
        }

        return null;
    }

    static class KeywordItem extends PHPCompletionItem {
        private String description = null;
        private String keyword = null;
        private static final List<String> CLS_KEYWORDS =
                Arrays.asList(PHPCodeCompletion.PHP_CLASS_KEYWORDS);


        KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                formatter.appendHtml(description);
                return formatter.getText();

            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            return keywordIcon;
        }

        @Override
        public boolean isSmart() {
            return CLS_KEYWORDS.contains(getName()) ? true : super.isSmart();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            KeywordCompletionType type = PHPCodeCompletion.PHP_KEYWORDS.get(getName());
            if (type == null) {
                return null;
            }
            switch(type) {
                case SIMPLE:
                    builder.append(getName());
                    break;
                case ENDS_WITH_SPACE:
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                    break;
                case CURSOR_INSIDE_BRACKETS:
                    builder.append(getName());
                    builder.append(" (${cursor})"); //NOI18N
                    break;
                case ENDS_WITH_CURLY_BRACKETS:
                    builder.append(getName());
                    builder.append(" {${cursor}"); //NOI18N
                    break;
                case ENDS_WITH_SEMICOLON:
                    builder.append(getName());
                    builder.append(";"); //NOI18N
                    break;
                case ENDS_WITH_COLON:
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                    break;
                default:
                    assert false : type.toString();
                    break;
            }
            return builder.toString();
        }
    }

    static class SuperGlobalItem extends PHPCompletionItem{
        private String name;

        public SuperGlobalItem(CompletionRequest request, String name) {
            super(new PredefinedSymbolElement(name), request);
            this.name = name;
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.emphasis(true);
            formatter.appendText(getName());
            formatter.emphasis(false);
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return "$" + name; //NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            //todo insert array brackets for array vars
            return super.getCustomInsertTemplate();
        }

        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            formatter.appendText(NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform"));
            return formatter.getText();
        }

        public String getDocumentation(){
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return keywordIcon;
        }
    }

    static class ConstantItem extends PHPCompletionItem {
        private IndexedConstant constant = null;

        ConstantItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
            this.constant = constant;
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            IndexedConstant constant = ((IndexedConstant)getElement());
            formatter.name(getKind(), true);

            if (constant.isResolved()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }

            formatter.name(getKind(), false);

            return formatter.getText();
        }

        public ElementKind getKind() {
            return ElementKind.GLOBAL;
        }
    }

    static class ClassItem extends PHPCompletionItem {
        private boolean endWithDoubleColon;
        ClassItem(IndexedClass clazz, CompletionRequest request) {
            this(clazz, request, false);
        }
        ClassItem(IndexedClass clazz, CompletionRequest request, boolean endWithDoubleColon) {
            super(clazz, request);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                builder.append(getName());
                builder.append("::${cursor}"); //NOI18N
                return builder.toString();
            }
            return super.getCustomInsertTemplate();
        }


    }

    public static ImageIcon getInterfaceIcon() {
        return InterfaceItem.icon();
    }

    static class InterfaceItem extends PHPCompletionItem {
        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N
        private static ImageIcon INTERFACE_ICON = null;
        InterfaceItem(IndexedInterface iface, CompletionRequest request) {
            super(iface, request);
        }

        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        private static ImageIcon icon() {
            if (INTERFACE_ICON == null) {
                INTERFACE_ICON = new ImageIcon(ImageUtilities.loadImage(PHP_INTERFACE_ICON));
            }
            return INTERFACE_ICON;
        }

        @Override
        public ImageIcon getIcon() {
            return icon();
        }
    }

    static class VariableItem extends PHPCompletionItem {
        private boolean insertDollarPrefix = true;

        VariableItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            String typeName = ((IndexedConstant)getElement()).getTypeName();

            if (typeName == null) {
                typeName = "?"; //NOI18N
            }

            formatter.type(true);
            formatter.appendText(typeName);
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getName() {
            String name = super.getName();

            if (!insertDollarPrefix && name.startsWith("$")){ //NOI18N
                return name.substring(1);
            }

            return name;
        }

        void doNotInsertDollarPrefix(){
            insertDollarPrefix = false;
        }
    }

    /**
     * It's used in the case that a top varibale is found in more files.
     * Such variable should be rendered only one time in the cc without
     * a file and type.
     */
    static class UnUniqueVaraibaleItems extends VariableItem {

        public UnUniqueVaraibaleItems(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.type(true);
            formatter.appendText("?");
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "";  //NOI18N
        }
    }

    static class ClassConstantItem extends VariableItem {
        ClassConstantItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }
    }


    static class SpecialFunctionItem extends KeywordItem{
        public SpecialFunctionItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append(" '${cursor}';"); //NOI18N
            return builder.toString();
        }
    }

    static class ReturnItem extends KeywordItem{
        public ReturnItem(CompletionRequest request) {
            super("return", request); //NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            return "return ${cursor};"; //NOI18N
        }
    }

    static class MagicMethodNameItem extends MagicMethodItem {
        public MagicMethodNameItem(IndexedFunction function, CompletionRequest request) {
            super(function, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            return super.getNameAndFunctionBodyForTemplate();
        }        
    }

    static class MagicMethodItem extends FunctionDeclarationItem {
        public MagicMethodItem(IndexedFunction function, CompletionRequest request) {
            super(function, request, 0,false);
        }
        
        @Override
        public boolean isSmart() {
            return false;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return super.getLhsHtml(formatter);
        }

        @Override
        protected boolean emphasisName() {
            return false;
        }

        @Override
        protected String getFunctionBodyForTemplate() {
            return "${cursor};\n";//NOI18N
        }
    }

    static class NewClassItem extends FunctionItem {
        public NewClassItem(IndexedFunction function, CompletionRequest request, int optionalArgCount) {
            super(function, request, optionalArgCount);
        }

        @Override
        public String getName() {
            String in = getElement().getIn();
            return (in != null) ? in : super.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }
    }
    static class FunctionItem extends PHPCompletionItem {
        private int optionalArgCount = 0;

        FunctionItem(IndexedFunction function, CompletionRequest request, int optionalArgCount) {
            super(function, request);
            this.optionalArgCount = optionalArgCount;
        }

        public IndexedFunction getFunction(){
            return (IndexedFunction)getElement();
        }

        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append("("); //NOI18N

            List<String> params = getInsertParams();

            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                template.append("${php-cc-"); //NOI18N
                template.append(Integer.toString(i));
                template.append(" default=\""); // NOI18N
                template.append(param);
                template.append("\"}"); //NOI18N

                if (i < params.size() - 1){
                    template.append(", "); //NOI18N
                }
            }

            template.append(')');

            return template.toString();
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();

            formatter.name(kind, true);

            if (emphasisName()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }

            formatter.name(kind, false);

            formatter.appendHtml("("); // NOI18N
            formatter.parameters(true);
            appendParamsStr(formatter);
            formatter.parameters(false);
            formatter.appendHtml(")"); // NOI18N

            return formatter.getText();
        }

        protected boolean emphasisName() {
            return getFunction().isResolved();
        }

        public List<String> getInsertParams() {
            List<String> insertParams = new LinkedList<String>();
            String parameters[] = getFunction().getParameters().toArray(new String[0]);
            boolean paramsToSkip[] = new boolean[parameters.length];
            int optionalArgList[] = getFunction().getOptionalArgs();

            for (int i = 0, j = optionalArgCount; i < optionalArgList.length; i++, j --) {
                if (j <= 0){
                    paramsToSkip[optionalArgList[i]] = true;
                }
            }

            for (int i = 0; i < parameters.length; i++) {
                String param = parameters[i];

                if (!paramsToSkip[i]){
                    insertParams.add(param);
                }
            }

            return insertParams;
        }

        @Override
        public String getSortText() {
            int order = optionalArgCount;
            return getName() + order;
        }

        private void appendParamsStr(HtmlFormatter formatter){
            String parameters[] = getFunction().getParameters().toArray(new String[0]);
            int optionalArgList[] = getFunction().getOptionalArgs();
            boolean paramsToSkip[] = new boolean[parameters.length];
            boolean optionalArgs[] = new boolean[parameters.length];

            for (int i = 0, j = optionalArgCount; i < optionalArgList.length; i++, j --) {
                optionalArgs[optionalArgList[i]] = true;

                if (j <= 0){
                    paramsToSkip[optionalArgList[i]] = true;
                }
            }

            boolean firstParam = true;

            for (int i = 0; i < parameters.length; i++) {
                if (!paramsToSkip[i]) {
                    String param = parameters[i];

                    if (firstParam) {
                        firstParam = false;
                    } else {
                        formatter.appendText(", "); // NOI18N
                    }

                    if (optionalArgs[i]) {
                        formatter.appendText(param);
                    } else {
                        formatter.emphasis(true);
                        formatter.appendText(param);
                        formatter.emphasis(false);
                    }
                }
            }
        }
    }

    static class FunctionDeclarationItem extends FunctionItem {
        private boolean isIface;
        public FunctionDeclarationItem(IndexedFunction function, CompletionRequest request, int optionalArgCount,boolean isIface) {
            super(function, request, optionalArgCount);
            this.isIface = isIface;
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            String modifierStr = getFunction().getModifiersString();            
            if (modifierStr.length() != 0) {
                modifierStr = modifierStr.replace("abstract","").trim();//NOI18N
                template.append(modifierStr);
            }
            template.append(" ").append("function");//NOI18N
            template.append(getNameAndFunctionBodyForTemplate());
            return template.toString();
        }

        protected String getNameAndFunctionBodyForTemplate() {
            StringBuilder template = new StringBuilder();
            final String functionSignature = getFunction().getFunctionSignature();
            template.append(" ").append(functionSignature);//NOI18N
            template.append(" ").append("{\n");//NOI18N
            template.append(getFunctionBodyForTemplate());//NOI18N
            template.append("}");//NOI18N
            return template.toString();
        }

        /**
         * @return body or null
         */
        protected String getFunctionBodyForTemplate() {
            StringBuilder template = new StringBuilder();
            if (isIface) {
                template.append("${cursor};\n");//NOI18N
            } else {
                String functionSignature = getFunction().getFunctionSignature();
                template.append("${cursor}parent::"+ functionSignature +";\n");//NOI18N
            }
            return template.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.getLhsHtml(formatter));
            /* TODO: uncomment but first be sure that it really works
            sb.append(" - ");//NOI18N
            if (isIface || getFunction().isAbstract()) {
                sb.append("implement"); //NOI18N
            } else {
                sb.append("override"); //NOI18N
            }
             */
            //for now
            sb.append(' ').append(NbBundle.getMessage(PHPCompletionItem.class, "Generate"));//NOI18N
            return sb.toString();
        }

        @Override
        public boolean isSmart() {
            return true;
        }
    }

    static class CompletionRequest {
        public  int anchor;
        public  PHPParseResult result;
        public  CompilationInfo info;
        public  String prefix;
        public  String currentlyEditedFileURL;
        PHPIndex index;
    }
}
