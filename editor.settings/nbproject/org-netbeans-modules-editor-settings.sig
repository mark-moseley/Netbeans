#Signature file v4.0
#Version 

CLSS public java.lang.Object
cons public Object()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.api.editor.settings.AttributesUtilities
meth public !varargs static javax.swing.text.AttributeSet createComposite(javax.swing.text.AttributeSet[])
meth public !varargs static javax.swing.text.AttributeSet createImmutable(java.lang.Object[])
meth public !varargs static javax.swing.text.AttributeSet createImmutable(javax.swing.text.AttributeSet[])
supr java.lang.Object
hfds ATTR_DISMANTLED_STRUCTURE
hcls Composite,Immutable,Proxy

CLSS public final org.netbeans.api.editor.settings.CodeTemplateDescription
cons public CodeTemplateDescription(java.lang.String,java.lang.String,java.lang.String)
cons public CodeTemplateDescription(java.lang.String,java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String)
meth public java.lang.String getAbbreviation()
meth public java.lang.String getDescription()
meth public java.lang.String getParametrizedText()
meth public java.lang.String getUniqueId()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getContexts()
supr java.lang.Object
hfds abbreviation,contexts,description,parametrizedText,uniqueId

CLSS public abstract org.netbeans.api.editor.settings.CodeTemplateSettings
cons public CodeTemplateSettings()
meth public abstract java.util.Collection<org.netbeans.api.editor.settings.CodeTemplateDescription> getCodeTemplateDescriptions()
meth public abstract javax.swing.KeyStroke getExpandKey()
supr java.lang.Object

CLSS public final org.netbeans.api.editor.settings.EditorStyleConstants
fld public final static java.lang.Object Default
fld public final static java.lang.Object DisplayName
fld public final static java.lang.Object WaveUnderlineColor
meth public java.lang.String toString()
supr java.lang.Object
hfds representation

CLSS public final org.netbeans.api.editor.settings.FontColorNames
fld public final static java.lang.String BLOCK_SEARCH_COLORING = "block-search"
fld public final static java.lang.String CARET_ROW_COLORING = "highlight-caret-row"
fld public final static java.lang.String CODE_FOLDING_BAR_COLORING = "code-folding-bar"
fld public final static java.lang.String CODE_FOLDING_COLORING = "code-folding"
fld public final static java.lang.String DEFAULT_COLORING = "default"
fld public final static java.lang.String GUARDED_COLORING = "guarded"
fld public final static java.lang.String HIGHLIGHT_SEARCH_COLORING = "highlight-search"
fld public final static java.lang.String INC_SEARCH_COLORING = "inc-search"
fld public final static java.lang.String LINE_NUMBER_COLORING = "line-number"
fld public final static java.lang.String SELECTION_COLORING = "selection"
fld public final static java.lang.String STATUS_BAR_BOLD_COLORING = "status-bar-bold"
fld public final static java.lang.String STATUS_BAR_COLORING = "status-bar"
supr java.lang.Object

CLSS public abstract org.netbeans.api.editor.settings.FontColorSettings
cons public FontColorSettings()
fld public final static java.lang.String PROP_FONT_COLORS = "fontColors"
meth public abstract javax.swing.text.AttributeSet getFontColors(java.lang.String)
meth public abstract javax.swing.text.AttributeSet getTokenFontColors(java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.api.editor.settings.KeyBindingSettings
cons public KeyBindingSettings()
meth public abstract java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding> getKeyBindings()
supr java.lang.Object

CLSS public final org.netbeans.api.editor.settings.MultiKeyBinding
cons public MultiKeyBinding(javax.swing.KeyStroke,java.lang.String)
cons public MultiKeyBinding(javax.swing.KeyStroke[],java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int getKeyStrokeCount()
meth public int hashCode()
meth public java.lang.String getActionName()
meth public java.lang.String toString()
meth public java.util.List<javax.swing.KeyStroke> getKeyStrokeList()
meth public javax.swing.KeyStroke getKeyStroke(int)
supr java.lang.Object
hfds actionName,keyStrokeList
hcls UnmodifiableArrayList

CLSS public final org.netbeans.api.editor.settings.SimpleValueNames
fld public final static java.lang.String CARET_BLINK_RATE = "caret-blink-rate"
fld public final static java.lang.String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"
fld public final static java.lang.String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"
fld public final static java.lang.String CARET_ITALIC_INSERT_MODE = "caret-italic-insert-mode"
fld public final static java.lang.String CARET_ITALIC_OVERWRITE_MODE = "caret-italic-overwrite-mode"
fld public final static java.lang.String CARET_TYPE_INSERT_MODE = "caret-type-insert-mode"
fld public final static java.lang.String CARET_TYPE_OVERWRITE_MODE = "caret-type-overwrite-mode"
fld public final static java.lang.String CODE_FOLDING_ENABLE = "code-folding-enable"
fld public final static java.lang.String COMPLETION_AUTO_POPUP = "completion-auto-popup"
fld public final static java.lang.String COMPLETION_AUTO_POPUP_DELAY = "completion-auto-popup-delay"
fld public final static java.lang.String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"
fld public final static java.lang.String COMPLETION_INSTANT_SUBSTITUTION = "completion-instant-substitution"
fld public final static java.lang.String COMPLETION_NATURAL_SORT = "completion-natural-sort"
fld public final static java.lang.String COMPLETION_PANE_MAX_SIZE = "completion-pane-max-size"
fld public final static java.lang.String COMPLETION_PANE_MIN_SIZE = "completion-pane-min-size"
fld public final static java.lang.String EXPAND_TABS = "expand-tabs"
fld public final static java.lang.String HIGHLIGHT_CARET_ROW = "highlight-caret-row"
fld public final static java.lang.String HIGHLIGHT_MATCH_BRACE = "highlight-match-brace"
fld public final static java.lang.String INDENT_SHIFT_WIDTH = "indent-shift-width"
fld public final static java.lang.String JAVADOC_AUTO_POPUP = "javadoc-auto-popup"
fld public final static java.lang.String JAVADOC_AUTO_POPUP_DELAY = "javadoc-auto-popup-delay"
fld public final static java.lang.String JAVADOC_BG_COLOR = "javadoc-bg-color"
fld public final static java.lang.String JAVADOC_PREFERRED_SIZE = "javadoc-preferred-size"
fld public final static java.lang.String LINE_HEIGHT_CORRECTION = "line-height-correction"
fld public final static java.lang.String LINE_NUMBER_VISIBLE = "line-number-visible"
fld public final static java.lang.String MARGIN = "margin"
fld public final static java.lang.String SCROLL_FIND_INSETS = "scroll-find-insets"
fld public final static java.lang.String SCROLL_JUMP_INSETS = "scroll-jump-insets"
fld public final static java.lang.String SHOW_DEPRECATED_MEMBERS = "show-deprecated-members"
fld public final static java.lang.String SPACES_PER_TAB = "spaces-per-tab"
fld public final static java.lang.String STATUS_BAR_CARET_DELAY = "status-bar-caret-delay"
fld public final static java.lang.String STATUS_BAR_VISIBLE = "status-bar-visible"
fld public final static java.lang.String TAB_SIZE = "tab-size"
fld public final static java.lang.String TEXT_LEFT_MARGIN_WIDTH = "text-left-margin-width"
fld public final static java.lang.String TEXT_LIMIT_LINE_COLOR = "text-limit-line-color"
fld public final static java.lang.String TEXT_LIMIT_LINE_VISIBLE = "text-limit-line-visible"
fld public final static java.lang.String TEXT_LIMIT_WIDTH = "text-limit-width"
supr java.lang.Object

