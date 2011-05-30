/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl;

import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.till;
import static com.sonar.sslr.test.lexer.TokenUtils.lex;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.matcher.RuleBuilder;
import com.sonar.sslr.impl.matcher.RuleMatcher;
import com.sonar.sslr.impl.matcher.TokenValueMatcher;

public class ParsingStackTraceTest {

  private List<Token> tokens = lex("package com.test;\n" + "import java.util.*;\n" + "public abstract clas MyClass {\n"
      + "   public abstract void run();\n" + "}\n");
  TokenValueMatcher language = new TokenValueMatcher("language");
  private ParsingState state = new ParsingState(tokens);
  private RuleMatcher compilationUnit = ((RuleBuilder) new JavaGrammar().getRootRule()).getRule();

  @Before
  public void init() {
    Token copyBookToken = tokens.get(tokens.size() - 1);
    copyBookToken.setCopyBook(true);
    copyBookToken.setCopyBookOriginalFileName("file1");
    copyBookToken.setCopyBookOriginalLine(10);
  }

  @Test
  public void testGenerateFullStackTrace() {
    compilationUnit.isMatching(state);

    StringBuilder expected = new StringBuilder();
    expected.append("------\n");
    expected.append("    1 package com.test;\n");
    expected.append("    2 import java.util.*;\n");
    expected.append("-->   public abstract clas MyClass {\n");
    expected.append("    4    public abstract void run();\n");
    expected.append("    5 }\n");
    expected.append("------\n");
    expected.append("Expected : <\"class\"> but was : <clas [IDENTIFIER]> ('Dummy for unit tests': Line 3 / Column 16)\n");

    assertEquals(expected.toString(), ParsingStackTrace.generateFullStackTrace(state));
  }

  @Test
  public void testGenerateErrorOnCopyBook() {
    compilationUnit.isMatching(state);
    Token outpostMatcherToken = state.getOutpostMatcherToken();
    outpostMatcherToken.setCopyBook(true);
    outpostMatcherToken.setCopyBookOriginalFileName("file1");
    outpostMatcherToken.setCopyBookOriginalLine(20);

    StringBuilder expected = new StringBuilder();
    expected
        .append("Expected : <\"class\"> but was : <clas [IDENTIFIER]> (copy book 'Dummy for unit tests': Line 3 / Column 16 called from file 'file1': Line 20)\n");

    assertEquals(expected.toString(), ParsingStackTrace.generate(state));
  }

  @Test
  public void testGenerateFullStackTraceWhenEndOfFileIsReached() {
    tokens = lex("package com.test;\n" + "import java.util.*;\n" + "public abstract");
    state = new ParsingState(tokens);
    compilationUnit.isMatching(state);

    StringBuilder expected = new StringBuilder();
    expected.append("------\n");
    expected.append("    1 package com.test;\n");
    expected.append("    2 import java.util.*;\n");
    expected.append("-->   public abstract\n");
    expected.append("------\n");
    expected.append("Expected : <\"class\"> but was : <EOF> ('Dummy for unit tests')\n");

    assertEquals(expected.toString(), ParsingStackTrace.generateFullStackTrace(state));
  }

  public class JavaGrammar extends Grammar {

    public Rule compilationUnit;
    public Rule packageDeclaration;
    public Rule importDeclaration;
    public Rule classBlock;
    public Rule classDeclaration;

    public JavaGrammar() {
      compilationUnit.is(packageDeclaration, importDeclaration, classBlock);
      classBlock.is(classDeclaration);

      packageDeclaration.is("package", till(";"));
      importDeclaration.is("import", till(";"));
      classDeclaration.is("public", "abstract", "class");
    }

    public Rule getRootRule() {
      return compilationUnit;
    }
  }
}