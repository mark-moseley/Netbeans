package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

class ConstantElementImpl extends ModelElementImpl implements ConstantElement {
    ConstantElementImpl(FileScope inScope, ASTNodeInfo<Scalar> node) {
        this(inScope,node.getName(),inScope.getFile(),node.getRange());
    }

    ConstantElementImpl(IndexScopeImpl inScope, IndexedConstant indexedConstant) {
        this(inScope,indexedConstant.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedConstant.getFilenameUrl()),
                new OffsetRange(indexedConstant.getOffset(), indexedConstant.getOffset() + indexedConstant.getName().length()));
    }

    private ConstantElementImpl(ScopeImpl inScope, String name,
            Union2<String, FileObject> file, OffsetRange offsetRange) {
        super(inScope, name, file, offsetRange, PhpKind.CONSTANT);
    }


    @Override
    StringBuilder golden(int indent) {
        //TODO: not yet
        return new StringBuilder();
    }
}
