/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl.matcher;

import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.*;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.*;
import static com.sonar.sslr.impl.matcher.HamcrestMatchMatcher.*;
import static com.sonar.sslr.impl.matcher.MyPunctuator.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TokenCountMatcherTest {

  @Test
  public void ok() {
    assertThat(and(tokenCount(TokenCountMatcher.Operator.EQUAL, 2, till("b"))), match("a b"));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.EQUAL, 3, till("b"))), match("a a b"));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.LESS_THAN, 5, till("b"))), match("a b"));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.GREATER_THAN, 1, till("b"))), match("a b"));

    assertThat(and(tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b"))), not(match("a b")));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.EQUAL, 4, till("b"))), not(match("a a b")));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.LESS_THAN, 2, till("b"))), not(match("a b")));
    assertThat(and(tokenCount(TokenCountMatcher.Operator.GREATER_THAN, 5, till("b"))), not(match("a a a a b")));
  }

  @Test
  public void testToString() {
    assertEquals(tokenCount(TokenCountMatcher.Operator.EQUAL, 2, till("b")).toString(), "tokenCount(TokenCountMatcher.Operator.EQUAL, 2)");
  }

  @Test
  public void testEqualsAndHashCode() {
    assertThat(tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b")) == tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b")),
        is(true));
    assertThat(
        tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b")) == tokenCount(TokenCountMatcher.Operator.LESS_THAN, 0, till("b")),
        is(false));
    assertThat(tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b")) == tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("z")),
        is(false));
    assertThat(tokenCount(TokenCountMatcher.Operator.EQUAL, 0, till("b")) == isOneOfThem(LEFT, RIGHT), is(false));
  }

}
