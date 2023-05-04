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


package com.io7m.xoanon.extension.internal;

import com.io7m.xoanon.extension.XoBotType;
import com.io7m.xoanon.extension.XoFXThread;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.scene.input.KeyCode.SHIFT;

/**
 * The basic bot implementation.
 */

public final class XoBot implements XoBotType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoBot.class);

  private final Stage rootStage;
  private final Robot robot;

  /**
   * The basic bot implementation.
   *
   * @param inRootStage The root stage
   */

  public XoBot(
    final Stage inRootStage)
  {
    this.rootStage =
      Objects.requireNonNull(inRootStage, "rootStage");
    this.robot =
      new Robot();
  }

  @Override
  public Stage stage()
  {
    return this.rootStage;
  }

  @Override
  public void waitForStageToClose(
    final long milliseconds)
    throws Exception
  {
    for (long t = 0L; t < milliseconds; ++t) {
      if (this.rootStage.isShowing()) {
        Thread.sleep(1L);
      } else {
        return;
      }
    }

    throw new TimeoutException(
      "Timed out waiting for the stage to close."
    );
  }

  @Override
  public Robot robot()
  {
    return this.robot;
  }

  @Override
  public Node findWithId(
    final String id)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var scene =
        this.rootStage.getScene();
      final var root =
        scene.getRoot();

      final var result = root.lookup("#" + id);
      if (result == null) {
        throw new NoSuchElementException(
          "No element with ID: %s".formatted(id)
        );
      }

      return result;
    }).get(1L, SECONDS);
  }

  @Override
  public Node findWithText(
    final String text)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var scene =
        this.rootStage.getScene();
      final var root =
        scene.getRoot();

      final var result = findWithTextSearch(root, text);
      if (result == null) {
        throw new NoSuchElementException(
          "No element with text: %s".formatted(text)
        );
      }

      return result;
    }).get(1L, SECONDS);
  }

  @Override
  public Node findWithText(
    final Parent parent,
    final String text)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var result = findWithTextSearch(parent, text);
      if (result == null) {
        throw new NoSuchElementException(
          "No element with text: %s".formatted(text)
        );
      }

      return result;
    }).get(1L, SECONDS);
  }

  private static Node findWithTextSearch(
    final Node node,
    final String text)
  {
    if (node instanceof final Labeled label) {
      if (Objects.equals(label.getText(), text)) {
        return label;
      }
    }

    if (node instanceof final Parent parent) {
      for (final var child : parent.getChildrenUnmodifiable()) {
        final var result = findWithTextSearch(child, text);
        if (result != null) {
          return result;
        }
      }
    }

    return null;
  }

  @Override
  public void click(
    final Node node)
    throws Exception
  {
    this.bringStageToFront();
    this.pointMouseAtAndWait(node);
    this.clickMouseAndPause();
  }

  private void clickMouseAndPause()
    throws InterruptedException, ExecutionException, TimeoutException
  {
    XoFXThread.run(() -> {
      LOG.trace("clicking mouse");
      this.robot.mouseClick(MouseButton.PRIMARY);
      return null;
    }).get(1L, SECONDS);
    pause();
  }

  private static void pause()
    throws InterruptedException
  {
    Thread.sleep(16L);
  }

  @Override
  public void type(
    final Node node,
    final KeyCode... codes)
    throws Exception
  {
    this.bringStageToFront();
    this.pointMouseAtAndWait(node);

    for (final var code : codes) {
      XoFXThread.run(() -> {
        LOG.trace("typing {}", code);
        this.robot.keyType(code);
        return null;
      }).get(1L, SECONDS);
      pause();
    }
  }

  private void pointMouseAtAndWait(
    final Node node)
    throws Exception
  {
    XoFXThread.run(() -> {
      this.pointMouseAt(node);
      return null;
    }).get(1L, SECONDS);
    pause();
  }

  private void bringStageToFront()
    throws Exception
  {
    XoFXThread.run(() -> {
      LOG.trace(
        "bringing stage {} ({}) to front",
        this.rootStage,
        this.rootStage.getTitle()
      );
      this.rootStage.toFront();
      return null;
    }).get(1L, SECONDS);
    pause();
  }

  @Override
  public void typeWithShift(
    final Node node,
    final KeyCode... codes)
    throws Exception
  {
    this.bringStageToFront();
    this.pointMouseAtAndWait(node);

    XoFXThread.run(() -> {
      LOG.trace("pressing shift");
      this.robot.keyPress(SHIFT);
      return null;
    }).get(1L, SECONDS);
    pause();

    for (final var code : codes) {
      XoFXThread.run(() -> {
        LOG.trace("typing code {}", code);
        this.robot.keyType(code);
        return null;
      }).get(1L, SECONDS);
      pause();
    }

    XoFXThread.run(() -> {
      LOG.trace("releasing shift");
      this.robot.keyRelease(SHIFT);
      return null;
    }).get(1L, SECONDS);
    pause();
  }

  private void pointMouseAt(
    final Node node)
  {
    LOG.trace("pointing mouse at {}", node);

    final var bounds =
      node.localToScreen(node.getBoundsInLocal());

    this.robot.mouseMove(
      new Point2D(bounds.getCenterX(), bounds.getCenterY())
    );
  }

  @Override
  public void sleepForFrames(
    final int frames)
    throws Exception
  {
    for (int index = 0; index < frames; ++index) {
      XoFXThread.run(() -> {
        Thread.sleep(1L);
        return null;
      }).get(1L, SECONDS);
    }
  }
}
