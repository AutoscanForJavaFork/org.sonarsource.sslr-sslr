/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonarsource.sslr.api;

import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AstNodeTest {

  @Test
  public void testAddChild() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    AstNode stat = new AstNode(new NodeType(), "stat", null);
    AstNode assign = new AstNode(new NodeType(), "assign", null);
    expr.addChild(stat);
    expr.addChild(assign);

    assertThat(expr.getChildren(), hasItems(stat, assign));
  }

  @Test
  public void testAddNullChild() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    expr.addChild(null);

    assertFalse(expr.hasChildren());
  }

  @Test
  public void testAddChildWhichMustBeSkippedFromAst() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    AstNode all = new AstNode(new NodeType(true), "all", null);
    AstNode stat = new AstNode(new NodeType(), "stat", null);
    all.addChild(stat);
    expr.addChild(all);

    AstNode many = new AstNode(new NodeType(true), "many", null);
    AstNode print = new AstNode(new NodeType(), "print", null);
    many.addChild(print);
    expr.addChild(many);

    assertThat(expr.getChildren(), hasItems(stat, print));
  }

  @Test
  public void testAddMatcherChildWithoutChildren() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    AstNode all = new AstNode(new NodeType(true), "all", null);
    expr.addChild(all);

    assertThat(expr.getChildren().size(), is(0));
  }

  @Test
  public void testHasChildren() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    assertFalse(expr.hasChildren());
  }

  @Test
  public void testNextSibling() {
    AstNode expr1 = new AstNode(new NodeType(), "expr1", null);
    AstNode expr2 = new AstNode(new NodeType(), "expr2", null);
    AstNode statement = new AstNode(new NodeType(), "statement", null);

    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(expr1.nextSibling(), is(expr2));
    assertThat(expr2.nextSibling(), is(nullValue()));
  }

  @Test
  public void testPreviousSibling() {
    AstNode expr1 = new AstNode(new NodeType(), "expr1", null);
    AstNode expr2 = new AstNode(new NodeType(), "expr2", null);
    AstNode statement = new AstNode(new NodeType(), "statement", null);

    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(expr1.previousSibling(), is(nullValue()));
    assertThat(expr2.previousSibling(), is(expr1));
  }

  @Test
  public void testFindFirstDirectChild() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    NodeType statRule = new NodeType();
    AstNode stat = new AstNode(statRule, "stat", null);
    AstNode identifier = new AstNode(new NodeType(), "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertThat(expr.findFirstDirectChild(statRule), is(stat));
    NodeType anotherRule = new NodeType();
    assertThat(expr.findFirstDirectChild(anotherRule, statRule), is(stat));
  }

  @Test
  public void testFindFirstChildAndHasChildren() {
    AstNode expr = new AstNode(new NodeType(), "expr", null);
    AstNode stat = new AstNode(new NodeType(), "stat", null);
    NodeType indentifierRule = new NodeType();
    AstNode identifier = new AstNode(indentifierRule, "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertThat(expr.findFirstChild(indentifierRule), is(identifier));
    assertTrue(expr.hasChildren(indentifierRule));
    NodeType anotherRule = new NodeType();
    assertThat(expr.findFirstChild(anotherRule), is(nullValue()));
    assertFalse(expr.hasChildren(anotherRule));
  }

  @Test
  public void testHasParents() {
    NodeType exprRule = new NodeType();
    AstNode expr = new AstNode(exprRule, "expr", null);
    AstNode stat = new AstNode(new NodeType(), "stat", null);
    AstNode identifier = new AstNode(new NodeType(), "identifier", null);
    expr.addChild(stat);
    expr.addChild(identifier);

    assertTrue(identifier.hasParents(exprRule));
    assertFalse(identifier.hasParents(new NodeType()));
  }

  @Test
  public void testGetLastChild() {
    AstNode expr1 = new AstNode(new NodeType(), "expr1", null);
    AstNode expr2 = new AstNode(new NodeType(), "expr2", null);
    AstNode statement = new AstNode(new NodeType(), "statement", null);
    statement.addChild(expr1);
    statement.addChild(expr2);

    assertThat(statement.getLastChild(), is(expr2));
  }

  private class NodeType implements AstNodeType {

    private boolean skippedFromAst = false;

    public NodeType() {

    }

    public NodeType(boolean skippedFromAst) {
      this.skippedFromAst = skippedFromAst;
    }

    public boolean hasToBeSkippedFromAst() {
      return skippedFromAst;
    }

  }
}