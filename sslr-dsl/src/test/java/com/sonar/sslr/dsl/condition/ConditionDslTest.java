/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.dsl.condition;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;

import org.junit.Test;

import com.sonar.sslr.dsl.DslRunner;

public class ConditionDslTest {

  StringBuilder output = new StringBuilder();

  @Test
  public void shouldParseBasicConditions() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "true", output);
    DslRunner.create(new ConditionPrinterDsl(), "false", output);
    DslRunner.create(new ConditionPrinterDsl(), "false and true", output);
    DslRunner.create(new ConditionPrinterDsl(), "false or true", output);
    DslRunner.create(new ConditionPrinterDsl(), "3 = 2", output);
    DslRunner.create(new ConditionPrinterDsl(), "3 != 2", output);
    DslRunner.create(new ConditionPrinterDsl(), "3 not equals 2", output);
  }

  @Test
  public void shouldComputePrimaryCondition() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "true", output).execute();
    assertThat(output.toString(), is("true"));
  }

  @Test
  public void shouldComputeLogicalAnd() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "true && false", output).execute();
    assertThat(output.toString(), is("false"));

    output = new StringBuilder();
    DslRunner.create(new ConditionPrinterDsl(), "true and true", output).execute();
    assertThat(output.toString(), is("true"));
  }

  @Test
  public void shouldComputeLogicalOr() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "true || false", output).execute();
    assertThat(output.toString(), is("true"));

    output = new StringBuilder();
    DslRunner.create(new ConditionPrinterDsl(), "false or false", output).execute();
    assertThat(output.toString(), is("false"));
  }

  @Test
  public void shouldComputeEqual() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "2 equals 2", output).execute();
    assertThat(output.toString(), is("true"));

    output = new StringBuilder();
    DslRunner.create(new ConditionPrinterDsl(), "3 = 4", output).execute();
    assertThat(output.toString(), is("false"));
  }

  @Test
  public void shouldComputeNotEqual() throws URISyntaxException {
    DslRunner.create(new ConditionPrinterDsl(), "2 != 2", output).execute();
    assertThat(output.toString(), is("false"));

    output = new StringBuilder();
    DslRunner.create(new ConditionPrinterDsl(), "3 not equals 4", output).execute();
    assertThat(output.toString(), is("true"));
  }

}
