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


package com.io7m.xoanon.commander.internal;

import com.io7m.xoanon.commander.api.XCFXThread;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.commander.api.XCKey;
import com.io7m.xoanon.commander.api.XCKeyMap;
import javafx.application.Platform;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.SHIFT;

/**
 * The basic bot implementation.
 */

public final class XCRobot implements XCRobotType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XCRobot.class);

  private static final KeyCode[] ALL_KEY_CODES =
    KeyCode.values();

  private static final List<MouseButton> ALL_MOUSE_BUTTONS =
    Stream.of(MouseButton.values())
      .filter(b -> b != MouseButton.NONE)
      .toList();

  private final XCKeyMap keyMap;
  private final Robot robot;

  /**
   * The basic bot implementation.
   *
   * @param inKeyMap    The keyboard map
   * @param inBaseRobot The base JavaFX robot
   */

  public XCRobot(
    final XCKeyMap inKeyMap,
    final Robot inBaseRobot)
  {
    this.keyMap =
      Objects.requireNonNull(inKeyMap, "keyMap");
    this.robot =
      Objects.requireNonNull(inBaseRobot, "inBaseRobot");
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

  @XCFXThread
  private static void opBringStageToFront(
    final Node node)
  {
    final var scene = node.getScene();
    final var window = scene.getWindow();
    final var stage = (Stage) window;

    final var title = stage.getTitle();
    LOG.trace("bringing stage {} ({}) to front", stage, title);
    stage.toFront();
  }

  @XCFXThread
  private void opPointMouseAt(
    final Node node)
  {
    final var bounds =
      node.localToScreen(node.getBoundsInLocal());
    final var centerX =
      bounds.getCenterX();
    final var centerY =
      bounds.getCenterY();

    LOG.trace(
      "pointing mouse at {} ({}x{})",
      node,
      Double.valueOf(centerX),
      Double.valueOf(centerY)
    );

    if (Double.isFinite(centerX) && Double.isFinite(centerY)) {
      this.robot.mouseMove(new Point2D(centerX, centerY));
      return;
    }

    throw new IllegalStateException(
      "CenterX: %s CenterY: %s"
        .formatted(Double.valueOf(centerX), Double.valueOf(centerY))
    );
  }

  @XCFXThread
  private void opKeyPress(
    final KeyCode code)
  {
    LOG.trace("pressing {}", code);
    this.robot.keyPress(code);
  }

  @XCFXThread
  private void opKeyRelease(
    final KeyCode code)
  {
    LOG.trace("releasing {}", code);
    this.robot.keyRelease(code);
  }

  @XCFXThread
  private void opKeyType(
    final KeyCode code)
  {
    LOG.trace("typing {}", code);
    this.robot.keyType(code);
  }

  @XCFXThread
  private void opMousePress(
    final MouseButton button)
  {
    LOG.trace("pressing mouse {}", button);
    this.robot.mouseClick(button);
  }

  @XCFXThread
  private void opMouseRelease(
    final MouseButton button)
  {
    LOG.trace("releasing mouse {}", button);
    this.robot.mouseRelease(button);
  }

  private void typeKey(
    final XCKey code)
  {
    if (code.isShift()) {
      Platform.runLater(() -> this.opKeyPress(SHIFT));
      next();
    }
    if (code.isAlt()) {
      Platform.runLater(() -> this.opKeyPress(ALT));
      next();
    }
    if (code.isControl()) {
      Platform.runLater(() -> this.opKeyPress(CONTROL));
      next();
    }

    Platform.runLater(() -> this.opKeyType(code.code()));
    next();

    if (code.isControl()) {
      Platform.runLater(() -> this.opKeyRelease(CONTROL));
      next();
    }
    if (code.isAlt()) {
      Platform.runLater(() -> this.opKeyRelease(ALT));
      next();
    }
    if (code.isShift()) {
      Platform.runLater(() -> this.opKeyRelease(SHIFT));
      next();
    }
    pause();
  }

  private static void next()
  {
    Platform.requestNextPulse();
  }

  @Override
  public void waitForStageToClose(
    final Stage stage,
    final long milliseconds)
    throws Exception
  {
    for (var t = 0L; t < milliseconds; ++t) {
      if (stage.isShowing()) {
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
    final Stage stage,
    final String id)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var scene =
        stage.getScene();
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
    final Stage stage,
    final String text)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var scene =
        stage.getScene();
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

  @Override
  public void click(
    final Node node)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    next();

    XoFXThread.runVWait(1L, SECONDS, () -> this.opPointMouseAt(node));
    next();

    Platform.runLater(() -> this.opMousePress(MouseButton.PRIMARY));
    next();

    Platform.runLater(() -> this.opMouseRelease(MouseButton.PRIMARY));
    next();

    pause();
  }

  private static void pause()
  {
    try {
      Thread.sleep(16L * 2L);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void type(
    final Node node,
    final List<XCKey> codes)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    next();

    XoFXThread.runVWait(1L, SECONDS, () -> this.opPointMouseAt(node));
    next();

    for (final var code : codes) {
      LOG.trace("code {}", code);
    }

    for (final var code : codes) {
      this.typeKey(code);
    }

    pause();
  }

  @Override
  public void typeText(
    final Node node,
    final String text)
    throws Exception
  {
    final var characters =
      text.chars()
        .mapToObj(i -> Character.valueOf((char) i))
        .toList();

    this.type(node, this.keyMap.toCodes(characters));
  }

  @Override
  public void sleepForFrames(
    final int frames)
    throws Exception
  {
    for (var index = 0; index < frames; ++index) {
      XoFXThread.run(() -> {
        Thread.sleep(1L);
        return null;
      }).get(1L, SECONDS);
    }
  }

  @Override
  public void reset()
  {
    for (final var code : ALL_KEY_CODES) {
      Platform.runLater(() -> this.opKeyRelease(code));
      Platform.requestNextPulse();
    }
    for (final var button : ALL_MOUSE_BUTTONS) {
      Platform.runLater(() -> this.opMouseRelease(button));
      Platform.requestNextPulse();
    }
  }
}
