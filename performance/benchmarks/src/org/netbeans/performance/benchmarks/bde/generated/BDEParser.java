/* Generated By:JJTree&JavaCC: Do not edit this line. BDEParser.java */
package org.netbeans.performance.benchmarks.bde.generated;
import org.netbeans.performance.benchmarks.bde.TestSpecBuilder;

public class BDEParser/*@bgen(jjtree)*/implements BDEParserTreeConstants, BDEParserConstants {/*@bgen(jjtree)*/
  protected static JJTBDEParserState jjtree = new JJTBDEParserState();
    public static TestSpecBuilder parseTestSpec(String spec) throws Exception {
        BDEParser t = new BDEParser(new java.io.StringReader(spec));
        return new TestSpecBuilder(t.Start());
    }

  static final public void Letter() throws ParseException {
    jj_consume_token(11);
  }

  static final public void Digit() throws ParseException {
    jj_consume_token(12);
  }

  static final public void String() throws ParseException {
                         /*@bgen(jjtree) String */
  ASTString jjtn000 = new ASTString(JJTSTRING);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(13);
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 14:
          ;
          break;
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        jj_consume_token(14);
      }
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  static final public ASTStart Start() throws ParseException {
                           /*@bgen(jjtree) Start */
  ASTStart jjtn000 = new ASTStart(JJTSTART);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 16:
        LoadDefinition();
        jj_consume_token(15);
        break;
      default:
        jj_la1[1] = jj_gen;
        ;
      }
      TestDefinitionList();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 15:
        jj_consume_token(15);
        StoreDefinition();
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
    {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  static final public void LoadDefinition() throws ParseException {
                                         /*@bgen(jjtree) LoadDefinition */
  ASTLoadDefinition jjtn000 = new ASTLoadDefinition(JJTLOADDEFINITION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(16);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 23:
        ClassName();
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  static final public void StoreDefinition() throws ParseException {
                                           /*@bgen(jjtree) StoreDefinition */
  ASTStoreDefinition jjtn000 = new ASTStoreDefinition(JJTSTOREDEFINITION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(17);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 23:
        ClassName();
        break;
      default:
        jj_la1[4] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  static final public void Identifier() throws ParseException {
                                 /*@bgen(jjtree) Identifier */
  ASTIdentifier jjtn000 = new ASTIdentifier(JJTIDENTIFIER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(18);
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  static final public void TestDefinitionList() throws ParseException {
                                                 /*@bgen(jjtree) TestDefinitionList */
  ASTTestDefinitionList jjtn000 = new ASTTestDefinitionList(JJTTESTDEFINITIONLIST);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      TestDefinition();
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 19:
          ;
          break;
        default:
          jj_la1[5] = jj_gen;
          break label_2;
        }
        jj_consume_token(19);
        TestDefinition();
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  static final public void TestDefinition() throws ParseException {
                                         /*@bgen(jjtree) TestDefinition */
  ASTTestDefinition jjtn000 = new ASTTestDefinition(JJTTESTDEFINITION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      ClassName();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 20:
        jj_consume_token(20);
        MethodFilterList();
        jj_consume_token(21);
        break;
      default:
        jj_la1[6] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 22:
        jj_consume_token(22);
        ArgDataList();
        break;
      default:
        jj_la1[7] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ClassName() throws ParseException {
                               /*@bgen(jjtree) ClassName */
  ASTClassName jjtn000 = new ASTClassName(JJTCLASSNAME);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(23);
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void MethodFilterList() throws ParseException {
                                             /*@bgen(jjtree) MethodFilterList */
  ASTMethodFilterList jjtn000 = new ASTMethodFilterList(JJTMETHODFILTERLIST);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      MethodFilter();
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 24:
          ;
          break;
        default:
          jj_la1[8] = jj_gen;
          break label_3;
        }
        jj_consume_token(24);
        MethodFilter();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void MethodFilter() throws ParseException {
                                     /*@bgen(jjtree) MethodFilter */
  ASTMethodFilter jjtn000 = new ASTMethodFilter(JJTMETHODFILTER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(25);
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ArgDataList() throws ParseException {
                                  /*@bgen(jjtree) ArgDataList */
  ASTArgDataList jjtn000 = new ASTArgDataList(JJTARGDATALIST);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(20);
      ArgDataSeries();
      jj_consume_token(21);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 24:
          ;
          break;
        default:
          jj_la1[9] = jj_gen;
          break label_4;
        }
        jj_consume_token(24);
        jj_consume_token(20);
        ArgDataSeries();
        jj_consume_token(21);
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ArgDataSeries() throws ParseException {
                                       /*@bgen(jjtree) ArgDataSeries */
  ASTArgDataSeries jjtn000 = new ASTArgDataSeries(JJTARGDATASERIES);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      ArgData();
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 15:
          ;
          break;
        default:
          jj_la1[10] = jj_gen;
          break label_5;
        }
        jj_consume_token(15);
        ArgData();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ArgData() throws ParseException {
                           /*@bgen(jjtree) ArgData */
  ASTArgData jjtn000 = new ASTArgData(JJTARGDATA);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Id();
      jj_consume_token(26);
      ValSpecList();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void Id() throws ParseException {
                 /*@bgen(jjtree) Id */
  ASTId jjtn000 = new ASTId(JJTID);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Identifier();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ValSpecList() throws ParseException {
                                   /*@bgen(jjtree) ValSpecList */
  ASTValSpecList jjtn000 = new ASTValSpecList(JJTVALSPECLIST);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      ValSpec();
      label_6:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 24:
          ;
          break;
        default:
          jj_la1[11] = jj_gen;
          break label_6;
        }
        jj_consume_token(24);
        ValSpec();
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void ValSpec() throws ParseException {
                           /*@bgen(jjtree) ValSpec */
  ASTValSpec jjtn000 = new ASTValSpec(JJTVALSPEC);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 13:
        String();
        break;
      case INTEGER_LITERAL:
        IntegerDef();
        break;
      default:
        jj_la1[12] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
     if (jjtc000) {
       jjtree.clearNodeScope(jjtn000);
       jjtc000 = false;
     } else {
       jjtree.popNode();
     }
     if (jjte000 instanceof ParseException) {
       {if (true) throw (ParseException)jjte000;}
     }
     if (jjte000 instanceof RuntimeException) {
       {if (true) throw (RuntimeException)jjte000;}
     }
     {if (true) throw (Error)jjte000;}
    } finally {
     if (jjtc000) {
       jjtree.closeNodeScope(jjtn000, true);
     }
    }
  }

  static final public void IntegerDef() throws ParseException {
                                 /*@bgen(jjtree) IntegerDef */
  ASTIntegerDef jjtn000 = new ASTIntegerDef(JJTINTEGERDEF);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Integer();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 27:
        jj_consume_token(27);
        Integer();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 19:
          jj_consume_token(19);
          Integer();
          break;
        default:
          jj_la1[13] = jj_gen;
          ;
        }
        break;
      default:
        jj_la1[14] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static final public void Integer() throws ParseException {
                           /*@bgen(jjtree) Integer */
  ASTInteger jjtn000 = new ASTInteger(JJTINTEGER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(INTEGER_LITERAL);
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  static private boolean jj_initialized_once = false;
  static public BDEParserTokenManager token_source;
  static ASCII_CharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[15];
  static final private int[] jj_la1_0 = {0x4000,0x10000,0x8000,0x800000,0x800000,0x80000,0x100000,0x400000,0x1000000,0x1000000,0x8000,0x1000000,0x2080,0x80000,0x8000000,};

  public BDEParser(java.io.InputStream stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new BDEParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  public BDEParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new BDEParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  public BDEParser(BDEParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  public void ReInit(BDEParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 15; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[28];
    for (int i = 0; i < 28; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 15; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 28; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
