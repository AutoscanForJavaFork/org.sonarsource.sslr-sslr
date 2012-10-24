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
package org.sonar.sslr.internal.matchers;

public class MatcherPathElement {

  private final Matcher matcher;
  private final int startIndex;
  private final int endIndex;

  public MatcherPathElement(Matcher matcher, int startIndex, int endIndex) {
    this.matcher = matcher;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public MatcherPathElement(BasicMatcherContext context) {
    this(context.getMatcher(), context.getStartIndex(), context.getCurrentIndex());
  }

  public Matcher getMatcher() {
    return matcher;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

}