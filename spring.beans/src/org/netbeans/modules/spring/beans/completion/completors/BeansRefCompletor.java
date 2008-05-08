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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.QueryProgress;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefCompletor extends Completor {

    final private boolean includeGlobal;

    public BeansRefCompletor(boolean includeGlobal) {
        this.includeGlobal = includeGlobal;
    }

    @Override
    protected void computeCompletionItems(final CompletionContext context, QueryProgress progress) throws IOException {
        final FileObject fo = context.getFileObject();
        SpringConfigModel model = SpringConfigModel.forFileObject(fo);
        if (model == null) {
            return;
        }
        
        final String prefix = context.getTypedPrefix();

        final List<String> cNames = new ArrayList<String>();
        String tagName = context.getTag().getNodeName();
        // get current bean parameters
        if (tagName.equals(BeansElements.BEAN) && SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), BeansAttributes.ID)) {
            String cId = SpringXMLConfigEditorUtils.getAttribute(context.getTag(), BeansAttributes.ID); 
            cNames.add(cId);
        }
        if (tagName.equals(BeansElements.BEAN) && SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), BeansAttributes.NAME)) { 
            List<String> names = StringUtils.tokenize(
                    SpringXMLConfigEditorUtils.getAttribute(context.getTag(), BeansAttributes.NAME),
                    SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS);
            cNames.addAll(names);
        }

        if(progress.isCancelled()) {
            return;
        }
        
        model.runReadAction(new Action<SpringBeans>() {

            public void run(SpringBeans sb) {
                List<SpringBean> beans = includeGlobal ? sb.getBeans() : sb.getFileBeans(fo).getBeans();
                Map<String, SpringBean> name2Bean = getName2Beans(sb, beans, includeGlobal); // if local beans, then add only bean ids;

                for (String beanName : name2Bean.keySet()) {
                    if (!beanName.startsWith(prefix) || cNames.contains(beanName)) {
                        continue;
                    }
                    SpringBean bean = name2Bean.get(beanName);
                    SpringXMLConfigCompletionItem item =
                            SpringXMLConfigCompletionItem.createBeanRefItem(context.getCurrentToken().getOffset() + 1,
                            beanName, bean, fo);
                    addItem(item);
                }
            }

            private Map<String, SpringBean> getName2Beans(SpringBeans sb, List<SpringBean> beans, boolean addNames) {
                Map<String, SpringBean> name2Bean = new HashMap<String, SpringBean>();
                for (SpringBean bean : beans) {
                    String beanId = bean.getId();
                    if (beanId != null) {
                        name2Bean.put(beanId, bean);
                    }
                    if (addNames) {
                        List<String> beanNames = bean.getNames();
                        for (String beanName : beanNames) {
                            name2Bean.put(beanName, bean);
                        }
                    }
                }
                
                // handle aliases also
                if(addNames) {
                    Set<String> aliases = sb.getAliases();
                    for (String alias : aliases) {
                        SpringBean bean = sb.findBean(alias);
                        if (bean != null) {
                            name2Bean.put(alias, bean);
                        }
                    }
                }

                return name2Bean;
            }
        });

        setAnchorOffset(context.getCurrentToken().getOffset() + 1);
    }
}
