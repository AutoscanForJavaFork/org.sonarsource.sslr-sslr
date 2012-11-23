/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.sslr.api;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * the parser is in charge to construct an abstract syntax tree (AST) which is a tree representation of the abstract syntactic structure of
 * source code. Each node of the tree is an AstNode and each node denotes a construct occurring in the source code which starts at a given
 * Token.
 *
 * @see Token
 */
public class AstNode {

  protected final AstNodeType type;
  private final String name;
  private final Token token;
  private List<AstNode> children = Collections.EMPTY_LIST;
  private int childIndex = -1;
  private AstNode parent;
  private int fromIndex;
  private int toIndex;

  public AstNode(Token token) {
    this(token.getType(), token.getType().getName(), token);
  }

  public AstNode(AstNodeType type, String name, @Nullable Token token) {
    this.type = type;
    this.token = token;
    this.name = name;
  }

  /**
   * Get the parent of this node in the tree.
   *
   * @param parent
   */
  public AstNode getParent() {
    return parent;
  }

  public void addChild(AstNode child) {
    if (child != null) {
      if (children.isEmpty()) {
        children = new ArrayList<AstNode>();
      }
      if (child.hasToBeSkippedFromAst()) {
        if (child.hasChildren()) {
          for (AstNode subChild : child.children) {
            addChildToList(subChild);
          }
        }
      } else {
        addChildToList(child);
      }
    }
  }

  private void addChildToList(AstNode child) {
    children.add(child);
    child.childIndex = children.size() - 1;
    child.parent = this;
  }

  /**
   * @return true if this AstNode has some children.
   */
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  /**
   * Get the list of children.
   *
   * @return list of children
   */
  public List<AstNode> getChildren() {
    return children;
  }

  public int getNumberOfChildren() {
    return children.size();
  }

  /**
   * Get the desired child
   *
   * @param index
   *          the index of the child (start at 0)
   * @return the AstNode child
   */
  public AstNode getChild(int index) {
    if (index >= getNumberOfChildren()) {
      throw new IllegalStateException("The AstNode '" + this + "' has only " + getNumberOfChildren()
        + " children. Requested child index is wrong : " + index);
    }
    return children.get(index);
  }

  /**
   * Get the next sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
   */
  public AstNode nextAstNode() {
    AstNode nextSibling = nextSibling();
    if (nextSibling != null) {
      return nextSibling;
    }
    if (parent != null) {
      return parent.nextAstNode();
    }
    return null;
  }

  /**
   * Get the previous sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
   */
  public AstNode previousAstNode() {
    AstNode previousSibling = previousSibling();
    if (previousSibling != null) {
      return previousSibling;
    }
    if (parent != null) {
      return parent.previousAstNode();
    }
    return null;
  }

  /**
   * Get the next sibling AstNode if exists in the tree.
   *
   * @return next sibling AstNode
   */
  public AstNode nextSibling() {
    if (parent == null) {
      return null;
    }
    if (parent.getNumberOfChildren() > childIndex + 1) {
      return parent.children.get(childIndex + 1);
    }
    return null;
  }

  /**
   * Get the previous sibling AstNode if exists in the tree.
   *
   * @return previous sibling AstNode
   */
  public AstNode previousSibling() {
    if (parent == null) {
      return null;
    }
    if (childIndex > 0) {
      return parent.children.get(childIndex - 1);
    }
    return null;
  }

  /**
   * Get the Token's value associated to this AstNode
   *
   * @return token's value
   */
  public String getTokenValue() {
    if (token == null) {
      return null;
    }
    return token.getValue();
  }

  /**
   * Get the Token's original value associated to this AstNode
   *
   * @return token's original value
   */
  public String getTokenOriginalValue() {
    if (token == null) {
      return null;
    }
    return token.getOriginalValue();
  }

  /**
   * Get the Token associated to this AstNode
   */
  public Token getToken() {
    return token;
  }

  /**
   * Get the Token's line associated to this AstNode
   *
   * @return token's line
   */
  public int getTokenLine() {
    return token.getLine();
  }

  public boolean hasToken() {
    return token != null;
  }

  public String getName() {
    return name;
  }

  public int getFromIndex() {
    return fromIndex;
  }

  public void setFromIndex(int fromIndex) {
    this.fromIndex = fromIndex;
  }

  public int getToIndex() {
    return toIndex;
  }

  public boolean hasToBeSkippedFromAst() {
    if (type == null) {
      return true;
    }
    if (AstNodeSkippingPolicy.class.isAssignableFrom(type.getClass())) {
      return ((AstNodeSkippingPolicy) type).hasToBeSkippedFromAst(this);
    }
    return false;
  }

  public void setToIndex(int toIndex) {
    this.toIndex = toIndex;
  }

  public boolean is(AstNodeType... types) {
    for (AstNodeType expectedType : types) {
      if (this.type == expectedType) {
        return true;
      }
    }

    return false;
  }

  public boolean isNot(AstNodeType... types) {
    return !is(types);
  }

  /**
   * Returns first child of one of specified types.
   * <p>
   * In the following case, {@code findFirstDirectChild("B")} would return "B2":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   * </pre>
   *
   * @return first child of one of specified types, or null if not found
   */
  public AstNode findFirstDirectChild(AstNodeType... nodeTypes) {
    for (AstNode child : children) {
      for (AstNodeType nodeType : nodeTypes) {
        if (child.type == nodeType) {
          return child;
        }
      }
    }
    return null;
  }

  /**
   * @deprecated in 1.17, use {@link #findFirstDescendant(AstNodeType...)} instead
   */
  @Deprecated
  public AstNode findFirstChild(AstNodeType... nodeTypes) {
    return findFirstDescendant(nodeTypes);
  }

  /**
   * Returns first descendant of one of specified types.
   * <p>
   * In the following case, {@code findFirstChild("B")} would return "B1":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   * </pre>
   *
   * @return first descendant of one of specified types, or null if not found
   * @since 1.17
   */
  public AstNode findFirstDescendant(AstNodeType... nodeTypes) {
    for (AstNode child : children) {
      for (AstNodeType nodeType : nodeTypes) {
        if (child.type == nodeType) {
          return child;
        }
        AstNode node = child.findFirstDescendant(nodeTypes);
        if (node != null) {
          return node;
        }
      }
    }
    return null;
  }

  /**
   * Returns the first child of this node.
   *
   * @return the first child, or null if there is no child
   */
  public AstNode getFirstChild() {
    return children.isEmpty() ? null : children.get(0);
  }

  /**
   * Returns children of specified types.
   * In the following case, {@code findDirectChildren("B")} would return "B2" and "B3":
   * <p>
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ B3
   * </pre>
   *
   * @return children of specified types, never null
   */
  public List<AstNode> findDirectChildren(AstNodeType... nodeTypes) {
    List<AstNode> result = new ArrayList<AstNode>();
    for (AstNode child : children) {
      for (AstNodeType nodeType : nodeTypes) {
        if (child.type == nodeType) {
          result.add(child);
        }
      }
    }
    return result;
  }

  /**
   * @deprecated in 1.17, use {@link #findDescendants(AstNodeType...)} instead
   */
  @Deprecated
  public List<AstNode> findChildren(AstNodeType... nodeTypes) {
    return findDescendants(nodeTypes);
  }

  /**
   * Returns descendants of specified types.
   * Be careful, this method searches among all descendants whatever is their depth, so favor {@link #findDirectChildren(AstNodeType...)} when possible.
   * <p>
   * In the following case, {@code findDescendants("B", "C")} would return "C1", "B1", "B2" and "B3":
   * <pre>
   * A1
   *  |__ C1
   *  |    |__ B1
   *  |__ B2
   *  |__ D1
   *  |__ B3
   * </pre>
   *
   * @return descendants of specified types, never null
   * @since 1.17
   */
  public List<AstNode> findDescendants(AstNodeType... nodeTypes) {
    List<AstNode> result = new ArrayList<AstNode>();
    findDescendants(result, nodeTypes);
    return result;
  }

  private void findDescendants(List<AstNode> result, AstNodeType... nodeTypes) {
    for (AstNodeType nodeType : nodeTypes) {
      if (is(nodeType)) {
        result.add(this);
      }
    }
    if (hasChildren()) {
      for (AstNode child : children) {
        child.findDescendants(result, nodeTypes);
      }
    }
  }

  /**
   * Returns the last child of this node.
   *
   * @return the last child, or null if there is no child
   */
  public AstNode getLastChild() {
    return children.isEmpty() ? null : children.get(children.size() - 1);
  }

  /**
   * @return true if this node has some direct children with the requested node types
   */
  public boolean hasDirectChildren(AstNodeType... nodeTypes) {
    return findFirstDirectChild(nodeTypes) != null;
  }

  /**
   * @deprecated in 1.17, use {@link #hasDescendant(AstNodeType...)} instead
   */
  @Deprecated
  public boolean hasChildren(AstNodeType... nodeTypes) {
    return hasDescendant(nodeTypes);
  }

  /**
   * @return true if this node has a descendant of one of specified types
   * @since 1.17
   */
  public boolean hasDescendant(AstNodeType... nodeTypes) {
    return findFirstDescendant(nodeTypes) != null;
  }

  /**
   * @deprecated in 1.17, use {@link #hasAncestor(AstNodeType)} instead
   */
  @Deprecated
  public boolean hasParents(AstNodeType nodeType) {
    return hasAncestor(nodeType);
  }

  /**
   * @return true if this node has an ancestor of the specified type
   * @since 1.17
   */
  public boolean hasAncestor(AstNodeType nodeType) {
    return findFirstAncestor(nodeType) != null;
  }

  /**
   * @deprecated in 1.17, use {@link #findFirstAncestor(AstNodeType)} instead
   */
  @Deprecated
  public AstNode findFirstParent(AstNodeType nodeType) {
    return findFirstAncestor(nodeType);
  }

  /**
   * @return first ancestor of the specified type, or null if not found
   * @since 1.17
   */
  public AstNode findFirstAncestor(AstNodeType nodeType) {
    if (parent == null) {
      return null;
    } else if (parent.type == nodeType) {
      return parent;
    }
    return parent.findFirstAncestor(nodeType);
  }

  public boolean isCopyBookOrGeneratedNode() {
    return getToken().isCopyBook() || getToken().isGeneratedCode();
  }

  public AstNodeType getType() {
    return type;
  }

  /**
   * Return all tokens contained in this tree node. Those tokens can be directly or indirectly attached to this node.
   */
  public List<Token> getTokens() {
    List<Token> tokens = new ArrayList<Token>();
    getTokens(tokens);
    return tokens;
  }

  private void getTokens(List<Token> tokens) {
    if (!hasChildren()) {
      tokens.add(token);
    } else {
      for (int i = 0; i < children.size(); i++) {
        children.get(i).getTokens(tokens);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(name);
    if (token != null) {
      result.append(" token='").append(token.getValue()).append("'");
      result.append(" line=").append(token.getLine());
      result.append(" column=").append(token.getColumn());
      result.append(" uri='").append(token.getURI() + "'");
    }
    return result.toString();
  }

  public Token getLastToken() {
    AstNode lastAstNode = this;
    while (lastAstNode.getLastChild() != null) {
      lastAstNode = lastAstNode.getLastChild();
    }
    return lastAstNode.getToken();
  }

}
