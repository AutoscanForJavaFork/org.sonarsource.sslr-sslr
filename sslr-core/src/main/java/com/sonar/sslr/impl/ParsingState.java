/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.impl;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.RecognitionExceptionListener;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.events.ExtendedStackTrace;
import com.sonar.sslr.impl.events.ParsingEventListener;
import com.sonar.sslr.impl.matcher.Matcher;
import com.sonar.sslr.impl.matcher.MemoizedMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParsingState {

  private final Token[] tokens;

  public int lexerIndex = 0;
  public final int lexerSize;

  private int outpostMatcherTokenIndex = -1;
  private Matcher outpostMatcher;

  private final Set<RecognitionExceptionListener> listeners = new HashSet<RecognitionExceptionListener>();
  private final AstNode[] astNodeMemoization;
  private final MemoizedMatcher[] astMatcherMemoization;
  public ParsingEventListener[] parsingEventListeners;
  public ExtendedStackTrace extendedStackTrace;

  public ParsingState(List<Token> tokens) {
    this.tokens = tokens.toArray(new Token[tokens.size()]);
    lexerSize = this.tokens.length;
    astNodeMemoization = new AstNode[lexerSize + 1];
    astMatcherMemoization = new MemoizedMatcher[lexerSize + 1];
  }

  public final Token popToken(Matcher matcher) {
    if (lexerIndex >= outpostMatcherTokenIndex) {
      outpostMatcherTokenIndex = lexerIndex;
      outpostMatcher = matcher;
    }
    if (lexerIndex >= lexerSize) {
      throw BacktrackingEvent.create();
    }
    return tokens[lexerIndex++];
  }

  public final boolean hasNextToken() {
    return lexerIndex < lexerSize;
  }

  public final Token peekToken(int index, Matcher matcher) {
    if (index > outpostMatcherTokenIndex) {
      outpostMatcherTokenIndex = index;
      outpostMatcher = matcher;
    }
    if (index >= lexerSize) {
      throw BacktrackingEvent.create();
    }
    return tokens[index];
  }

  public final Token peekToken(Matcher matcher) {
    return peekToken(lexerIndex, matcher);
  }

  public final Token readToken(int tokenIndex) {
    if (tokenIndex >= tokens.length) {
      return null;
    }
    return tokens[tokenIndex];
  }

  public final Matcher getOutpostMatcher() {
    return outpostMatcher;
  }

  public Token getOutpostMatcherToken() {
    if (outpostMatcherTokenIndex >= lexerSize || outpostMatcherTokenIndex == -1) {
      return null;
    }
    return tokens[outpostMatcherTokenIndex];
  }

  public final int getOutpostMatcherTokenIndex() {
    return outpostMatcherTokenIndex;
  }

  public final int getOutpostMatcherTokenLine() {
    if (outpostMatcherTokenIndex < lexerSize) {
      return tokens[outpostMatcherTokenIndex].getLine();
    }
    return tokens[lexerSize - 1].getLine();
  }

  public final void memoizeAst(MemoizedMatcher matcher, AstNode astNode) {
    astNode.setToIndex(lexerIndex);
    astNodeMemoization[astNode.getFromIndex()] = astNode;
    astMatcherMemoization[astNode.getFromIndex()] = matcher;
  }

  public final void deleteMemoizedAstAfter(int index) {
    for (int i = index; i <= outpostMatcherTokenIndex; i++) {
      astMatcherMemoization[i] = null;
      astNodeMemoization[i] = null;
    }
  }

  public final boolean hasMemoizedAst(MemoizedMatcher matcher) {
    if (astMatcherMemoization[lexerIndex] == matcher) {
      return true;
    }
    return false;
  }

  public final AstNode getMemoizedAst(MemoizedMatcher matcher) {
    if (hasMemoizedAst(matcher)) {
      return astNodeMemoization[lexerIndex];
    }
    return null;
  }

  public final Token peekTokenIfExists(int index, Matcher matcher) {
    try {
      return peekToken(index, matcher);
    } catch (BacktrackingEvent e) {
      return null;
    }
  }

  public final void addListeners(RecognitionExceptionListener... listeners) {
    for (RecognitionExceptionListener listener : listeners) {
      this.listeners.add(listener);
    }
  }

  public final void notifyListeners(RecognitionException recognitionException) {
    for (RecognitionExceptionListener listener : listeners) {
      listener.processRecognitionException(recognitionException);
    }
  }
}
