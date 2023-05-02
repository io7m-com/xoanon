/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.xoanon.extension;

import com.io7m.xoanon.extension.internal.XoBot;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A simple JavaFX extension for JUnit 5 tests.
 */

public final class XoExtension
  implements BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback,
  AfterEachCallback,
  ExtensionContext.Store.CloseableResource,
  ParameterResolver
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtension.class);

  private volatile Stage defaultStage;

  /**
   * A simple JavaFX extension for JUnit 5 tests.
   */

  public XoExtension()
  {

  }

  @Override
  public void beforeAll(
    final ExtensionContext context)
    throws InterruptedException
  {
    LOG.trace("starting JavaFX platform");
    final var latch = new CountDownLatch(1);
    Platform.startup(() -> {
      final var stage = new Stage();
      stage.setMinWidth(64.0);
      stage.setMinHeight(64.0);
      stage.setMaxHeight(64.0);
      stage.setMaxWidth(64.0);
      stage.show();

      this.defaultStage = stage;
      latch.countDown();
    });

    LOG.trace("waiting for JavaFX platform to start");
    latch.await(1L, TimeUnit.MINUTES);
    LOG.trace("JavaFX platform started");
  }

  @Override
  public void afterAll(
    final ExtensionContext context)
    throws InterruptedException
  {
    final var latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);

    LOG.trace("waiting for JavaFX platform to settle");
    latch.await(1L, TimeUnit.MINUTES);

    LOG.trace("stopping JavaFX platform");
    Platform.exit();
  }

  @Override
  public void close()
  {

  }

  @Override
  public boolean supportsParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    return Objects.equals(requiredType, XoBotType.class);
  }

  @Override
  public Object resolveParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    if (Objects.equals(requiredType, XoBotType.class)) {
      return new XoBot(this.defaultStage);
    }

    throw new IllegalStateException(
      "Unrecognized requested parameter type: %s".formatted(requiredType)
    );
  }

  @Override
  public void afterEach(
    final ExtensionContext context)
  {

  }

  @Override
  public void beforeEach(
    final ExtensionContext context)
  {

  }
}
