/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.symboltable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.symboltable.Scope;
import com.sonar.sslr.api.symboltable.Symbol;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GlobalScope implements Scope {

  private final List<Symbol> members = Lists.newArrayList();

  public AstNode getAstNode() {
    return null;
  }

  public Scope getEnclosingScope() {
    return null;
  }

  public Collection<Scope> getNestedScopes() {
    return Collections.emptyList();
  }

  public void addNestedScope(Scope nestedScope) {
    throw new UnsupportedOperationException();
  }

  public Collection<Symbol> getMembers() {
    return members;
  }

  public void define(Symbol symbol) {
    members.add(symbol);
  }

  public Symbol lookup(String name, Predicate predicate) {
    return null;
  }

}