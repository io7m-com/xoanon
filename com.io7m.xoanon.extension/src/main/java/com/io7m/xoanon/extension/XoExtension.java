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

import javafx.application.Platform;
import javafx.scene.robot.Robot;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple JavaFX extension for JUnit 5 tests.
 */

public final class XoExtension
  implements BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback,
  AfterEachCallback,
  ParameterResolver
{
  private static final AtomicBoolean FX_PLATFORM_STARTED =
    new AtomicBoolean(false);

  private final ConcurrentLinkedQueue<Stage> createdStages =
    new ConcurrentLinkedQueue<>();

  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtension.class);

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
    final var latch = new CountDownLatch(1);
    if (FX_PLATFORM_STARTED.compareAndSet(false, true)) {
      LOG.trace("starting JavaFX platform");
      Platform.setImplicitExit(false);
      Platform.startup(latch::countDown);
      LOG.trace("waiting for JavaFX platform to start");
      latch.await(1L, TimeUnit.MINUTES);
      LOG.trace("JavaFX platform started");
    } else {
      Platform.runLater(latch::countDown);
      LOG.trace("waiting for JavaFX platform to restore");
      latch.await(1L, TimeUnit.MINUTES);
      LOG.trace("JavaFX platform restored");
    }
  }

  @Override
  public void afterAll(
    final ExtensionContext context)
    throws InterruptedException
  {
    final var latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);

    LOG.trace("waiting for JavaFX platform to settle");
    latch.await(3L, TimeUnit.SECONDS);
  }

  @Override
  public boolean supportsParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    return Objects.equals(requiredType, Stage.class);
  }

  @Override
  public Object resolveParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    if (Objects.equals(requiredType, Stage.class)) {
      try {
        return this.createStage(extensionContext);
      } catch (final Exception e) {
        throw new ParameterResolutionException(e.getMessage(), e);
      }
    }

    throw new ParameterResolutionException(
      "Unrecognized requested parameter type: %s".formatted(requiredType)
    );
  }

  private Stage createStage(
    final ExtensionContext context)
    throws Exception
  {
    final var stageRef = new AtomicReference<Stage>();

    XoFXThread.run(() -> {
      final var stage = new Stage();
      this.createdStages.add(stage);

      stage.setMinWidth(320.0);
      stage.setMinHeight(240.0);
      stage.setMaxWidth(3000.0);
      stage.setMaxHeight(3000.0);
      stage.setTitle(context.getDisplayName());
      stage.show();
      stageRef.set(stage);
      return null;
    }).get(1L, TimeUnit.SECONDS);

    /*
     * Wait for the window to open, and then reset the mouse cursor so that
     * it is in the middle of the window.
     */

    Thread.sleep(100L);

    XoFXThread.run(() -> {
      final var stage = stageRef.get();
      final var x = stage.getX() + (stage.getWidth() / 2.0);
      final var y = stage.getY() + (stage.getHeight() / 2.0);

      LOG.trace("resetting mouse to {}x{}", x, y);
      final var robot = new Robot();
      robot.mouseMove(x, y);
      return null;
    }).get(1L, TimeUnit.SECONDS);

    return Objects.requireNonNull(stageRef.get(), "Stage");
  }

  @Override
  public void afterEach(
    final ExtensionContext context)
    throws Exception
  {
    XoFXThread.run(() -> {
      final var windows = List.copyOf(this.createdStages);
      for (final var window : windows) {
        try {
          window.close();
        } catch (final Throwable e) {
          LOG.error("close: {} ({}): ", window, window.getTitle(), e);
        }
      }
      return null;
    }).get(5L, TimeUnit.SECONDS);
  }

  @Override
  public void beforeEach(
    final ExtensionContext context)
    throws Exception
  {

  }
}
