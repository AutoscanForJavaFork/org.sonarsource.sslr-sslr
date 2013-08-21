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
package org.sonar.sslr.examples.grammars;

import com.sonar.sslr.api.Grammar;
import org.junit.Test;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class MemoizationGrammarTest {

  @Test
  public void should_be_slow_to_fail_to_parse_gramar_requiring_negative_memoization() {
    Grammar grammar = MemoizationGrammar.requiresNegativeMemoization();
    assertThat(grammar.rule(MemoizationGrammar.ROOT))
        .notMatches("aaaaaaaaaaaaaaaa") // Requires time T
        .notMatches("aaaaaaaaaaaaaaaaa") // Requires time 2*T
        .notMatches("aaaaaaaaaaaaaaaaaa"); // Requires time 4*T;
  }

}