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
import com.io7m.xoanon.commander.api.XCFXThreadOperationType;
import com.io7m.xoanon.commander.api.XCKey;
import com.io7m.xoanon.commander.api.XCKeyMap;
import com.io7m.xoanon.commander.api.XCOnFXThread;
import com.io7m.xoanon.commander.api.XCRobotType;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
  private final AtomicBoolean slowMotion;
  private volatile long timeout;
  private volatile long timePauseAfterMouseOp;
  private volatile long timePauseBetweenDoubleClick;
  private volatile long timePauseAfterKeyboardOp;

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
    this.timeout =
      1000L;
    this.timePauseAfterMouseOp =
      150L;
    this.timePauseAfterKeyboardOp =
      48L;
    this.timePauseBetweenDoubleClick =
      50L;

    this.slowMotion =
      new AtomicBoolean(false);
  }

  @XCOnFXThread
  private static Node opSearchWithText(
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
        final var result = opSearchWithText(child, text);
        if (result != null) {
          return result;
        }
      }
    }

    return null;
  }

  @XCOnFXThread
  private static <T> void opSearchWithClass(
    final LinkedList<T> results,
    final Class<T> clazz,
    final Node node,
    final String cssClass)
  {
    final boolean ofType =
      clazz.isAssignableFrom(node.getClass());
    final boolean hasClass =
      node.getStyleClass().contains(cssClass);

    if (ofType && hasClass) {
      results.add(clazz.cast(node));
    }

    if (node instanceof final Parent parent) {
      for (final var child : parent.getChildrenUnmodifiable()) {
        opSearchWithClass(results, clazz, child, cssClass);
      }
    }
  }

  @XCOnFXThread
  private static <T> void opSearchWithType(
    final Collection<T> results,
    final Class<T> clazz,
    final Node node)
  {
    if (clazz.isAssignableFrom(node.getClass())) {
      results.add(clazz.cast(node));
    }

    if (node instanceof final Parent parent) {
      for (final var child : parent.getChildrenUnmodifiable()) {
        opSearchWithType(results, clazz, child);
      }
    }
  }

  @XCOnFXThread
  private static void opBringStageToFront(
    final Node node)
  {
    final var scene = node.getScene();
    final var window = scene.getWindow();
    final var stage = (Stage) window;

    final var title = stage.getTitle();
    LOG.trace("bringing stage {} ({}) to front", stage, title);
    stage.toFront();
    stage.requestFocus();
  }

  @XCOnFXThread
  private static boolean opStageIsFront(
    final Node node)
  {
    final var scene = node.getScene();
    final var window = scene.getWindow();
    return window.isShowing() && window.isFocused();
  }

  @XCOnFXThread
  private static <T> List<T> findAllInner(
    final Class<T> clazz,
    final Parent root)
  {
    final var results = new LinkedList<T>();
    opSearchWithType(results, clazz, root);
    return results;
  }


  @XCOnFXThread
  private static <T> List<T> findAllWithClassInner(
    final Class<T> clazz,
    final Parent root,
    final String cssClass)
  {
    final var results = new LinkedList<T>();
    opSearchWithClass(results, clazz, root, cssClass);
    return results;
  }

  @XCOnFXThread
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

  @XCOnFXThread
  private void opKeyPress(
    final KeyCode code)
  {
    LOG.trace("pressing {}", code);
    this.robot.keyPress(code);
  }

  @XCOnFXThread
  private void opKeyRelease(
    final KeyCode code)
  {
    LOG.trace("releasing {}", code);
    this.robot.keyRelease(code);
  }

  @XCOnFXThread
  private void opKeyType(
    final KeyCode code)
  {
    LOG.trace("typing {}", code);
    this.robot.keyType(code);
  }

  @XCOnFXThread
  private void opMousePress(
    final MouseButton button)
  {
    LOG.trace("pressing mouse {}", button);
    this.robot.mouseClick(button);
  }

  @XCOnFXThread
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
    }
    if (code.isAlt()) {
      Platform.runLater(() -> this.opKeyPress(ALT));
    }
    if (code.isControl()) {
      Platform.runLater(() -> this.opKeyPress(CONTROL));
    }

    Platform.runLater(() -> this.opKeyType(code.code()));

    if (code.isControl()) {
      Platform.runLater(() -> this.opKeyRelease(CONTROL));
    }
    if (code.isAlt()) {
      Platform.runLater(() -> this.opKeyRelease(ALT));
    }
    if (code.isShift()) {
      Platform.runLater(() -> this.opKeyRelease(SHIFT));
    }

    this.pauseAfterKeyboardOp();
  }

  @Override
  public void execute(
    final Runnable f)
    throws Exception
  {
    Objects.requireNonNull(f, "f");
    XCFXThread.runVWait(this.timeout, MILLISECONDS, f);
  }

  @Override
  public <T> T evaluate(
    final XCFXThreadOperationType<T> f)
    throws Exception
  {
    Objects.requireNonNull(f, "f");
    return XCFXThread.runAndWait(this.timeout, MILLISECONDS, f);
  }

  @Override
  public void slowMotionDisable()
  {
    this.slowMotion.set(false);
  }

  @Override
  public void slowMotionEnable()
  {
    this.slowMotion.set(true);
  }

  @Override
  public long timeoutMilliseconds()
  {
    return this.timeout;
  }

  @Override
  public void setTimeoutMilliseconds(
    final long ms)
  {
    this.timeout = Math.max(1L, ms);
  }

  @Override
  public long timePauseAfterMouseOperationMilliseconds()
  {
    return this.timePauseAfterMouseOp;
  }

  @Override
  public void setTimePauseAfterMouseOperationMilliseconds(
    final long ms)
  {
    this.timePauseAfterMouseOp = Math.max(1L, ms);
  }

  @Override
  public long timePauseAfterKeyboardOperationMilliseconds()
  {
    return this.timePauseAfterKeyboardOp;
  }

  @Override
  public void setTimePauseAfterKeyboardOperationMilliseconds(
    final long ms)
  {
    this.timePauseAfterKeyboardOp = Math.max(1L, ms);
  }

  @Override
  public long timePauseBetweenDoubleClickMilliseconds()
  {
    return this.timePauseBetweenDoubleClick;
  }

  @Override
  public void setTimePauseBetweenDoubleClickMilliseconds(
    final long ms)
  {
    this.timePauseBetweenDoubleClick = Math.max(1L, ms);
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
  public <T extends Node> List<T> findAllInStage(
    final Class<T> clazz,
    final Stage stage)
    throws Exception
  {
    return this.evaluate(() -> {
      final var scene = stage.getScene();
      if (scene != null) {
        return findAllInner(clazz, scene.getRoot());
      }
      return List.of();
    });
  }

  @Override
  public <T extends Node> List<T> findAll(
    final Class<T> clazz,
    final Parent parent)
    throws Exception
  {
    return this.evaluate(() -> findAllInner(clazz, parent));
  }

  @Override
  public <T extends Node> T findWithId(
    final Class<T> clazz,
    final Stage stage,
    final String id)
    throws Exception
  {
    return this.evaluate(() -> {
      final var scene = stage.getScene();
      if (scene != null) {
        final var result = scene.getRoot().lookup("#" + id);
        if (result != null) {
          return clazz.cast(result);
        }
      }

      throw new NoSuchElementException(
        "No element with id '%s'".formatted(id)
      );
    });
  }

  @Override
  public <T extends Node> T findWithId(
    final Class<T> clazz,
    final Parent root,
    final String id)
    throws Exception
  {
    return this.evaluate(() -> {
      final var result = root.lookup("#" + id);
      if (result != null) {
        return clazz.cast(result);
      }

      throw new NoSuchElementException(
        "No element with id '%s'".formatted(id)
      );
    });
  }

  @Override
  public <T extends Node> T findWithTextInAnyStage(
    final Class<T> clazz,
    final String text)
    throws Exception
  {
    return this.evaluate(() -> {
      final var windows =
        Window.getWindows()
          .stream()
          .map(w -> (Stage) w)
          .filter(Window::isShowing)
          .toList();

      for (final var window : windows) {
        final var scene = window.getScene();
        if (scene != null) {
          final var result = opSearchWithText(scene.getRoot(), text);
          if (result != null) {
            return clazz.cast(result);
          }
        }
      }

      throw new NoSuchElementException(
        "No element with text '%s'".formatted(text)
      );
    });
  }

  @Override
  public <T extends Node> T findWithText(
    final Class<T> clazz,
    final Stage stage,
    final String text)
    throws Exception
  {
    return this.evaluate(() -> {
      final var scene = stage.getScene();
      if (scene != null) {
        final var result = opSearchWithText(scene.getRoot(), text);
        if (result != null) {
          return clazz.cast(result);
        }
      }

      throw new NoSuchElementException(
        "No element with text '%s'".formatted(text)
      );
    });
  }

  @Override
  public <T extends Node> T findWithText(
    final Class<T> clazz,
    final Parent parent,
    final String text)
    throws Exception
  {
    return this.evaluate(() -> {
      final var result = opSearchWithText(parent, text);
      if (result != null) {
        return clazz.cast(result);
      }

      throw new NoSuchElementException(
        "No element with text '%s'".formatted(text)
      );
    });
  }

  @Override
  public <T extends Node> T findWithIdInAnyStage(
    final Class<T> clazz,
    final String id)
    throws Exception
  {
    return this.evaluate(() -> {
      final var windows =
        Window.getWindows()
          .stream()
          .map(w -> (Stage) w)
          .filter(Window::isShowing)
          .toList();

      for (final var window : windows) {
        final var scene = window.getScene();
        if (scene != null) {
          final var result = scene.getRoot().lookup("#" + id);
          if (result != null) {
            return clazz.cast(result);
          }
        }
      }

      throw new NoSuchElementException(
        "No element with id '%s'".formatted(id)
      );
    });
  }

  @Override
  public <T extends Node> List<T> findAllWithClassInStage(
    final Class<T> clazz,
    final Stage stage,
    final String cssClass)
    throws Exception
  {
    return this.evaluate(() -> {
      final var scene = stage.getScene();
      if (scene != null) {
        return findAllWithClassInner(clazz, scene.getRoot(), cssClass);
      }
      return List.of();
    });
  }

  @Override
  public <T extends Node> List<T> findAllWithClass(
    final Class<T> clazz,
    final Parent parent,
    final String cssClass)
    throws Exception
  {
    return this.evaluate(() -> findAllWithClassInner(clazz, parent, cssClass));
  }

  @Override
  public <T extends Node> List<T> findAllWithClassInAnyStage(
    final Class<T> clazz,
    final String cssClass)
    throws Exception
  {
    return this.evaluate(() -> {
      final var windows =
        Window.getWindows()
          .stream()
          .map(w -> (Stage) w)
          .filter(Window::isShowing)
          .toList();

      final var results = new LinkedList<T>();
      for (final var window : windows) {
        final var scene = window.getScene();
        if (scene != null) {
          opSearchWithClass(results, clazz, scene.getRoot(), cssClass);
        }
      }
      return results;
    });
  }

  @Override
  public void click(
    final Node node)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    this.waitUntil(this.timeout, () -> opStageIsFront(node));
    this.execute(() -> this.opPointMouseAt(node));
    this.pauseAfterMouseOp();
    this.execute(() -> this.opMousePress(MouseButton.PRIMARY));
    this.execute(() -> this.opMouseRelease(MouseButton.PRIMARY));
    this.pauseAfterMouseOp();
  }

  @Override
  public void doubleClick(
    final Node node)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    this.waitUntil(this.timeout, () -> opStageIsFront(node));
    this.execute(() -> this.opPointMouseAt(node));
    this.execute(() -> this.opMousePress(MouseButton.PRIMARY));
    this.execute(() -> this.opMouseRelease(MouseButton.PRIMARY));
    Thread.sleep(this.timePauseBetweenDoubleClick);
    this.execute(() -> this.opMousePress(MouseButton.PRIMARY));
    this.execute(() -> this.opMouseRelease(MouseButton.PRIMARY));
    this.pauseAfterMouseOp();
  }

  @Override
  public void pointAt(
    final Node node)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    this.waitUntil(this.timeout, () -> opStageIsFront(node));
    this.execute(() -> this.opPointMouseAt(node));
    this.pauseAfterMouseOp();
  }

  private void pauseAfterKeyboardOp()
  {
    try {
      final var time =
        this.slowMotion.get() ? 1000L : this.timePauseAfterKeyboardOp;
      Thread.sleep(time);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void pauseAfterMouseOp()
  {
    try {
      final var time =
        this.slowMotion.get() ? 1000L : this.timePauseAfterMouseOp;
      Thread.sleep(time);
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
    this.waitUntil(this.timeout, () -> opStageIsFront(node));
    this.execute(() -> this.opPointMouseAt(node));
    this.pauseAfterMouseOp();

    for (final var code : codes) {
      LOG.trace("code {}", code);
    }

    for (final var code : codes) {
      this.typeKey(code);
    }
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
  public void type(
    final List<XCKey> codes)
  {
    for (final var code : codes) {
      LOG.trace("code {}", code);
    }

    for (final var code : codes) {
      this.typeKey(code);
    }
  }

  @Override
  public void typeText(
    final String text)
  {
    final var characters =
      text.chars()
        .mapToObj(i -> Character.valueOf((char) i))
        .toList();

    this.type(this.keyMap.toCodes(characters));
  }

  @Override
  public void typeRaw(
    final KeyCode code)
    throws Exception
  {
    this.execute(() -> this.robot.keyType(code));
    this.pauseAfterKeyboardOp();
  }

  @Override
  public void typeRaw(
    final Node node,
    final KeyCode code)
    throws Exception
  {
    Platform.runLater(() -> opBringStageToFront(node));
    this.waitUntil(this.timeout, () -> opStageIsFront(node));
    this.execute(() -> this.opPointMouseAt(node));
    this.pauseAfterMouseOp();
    this.execute(() -> this.robot.keyType(code));
    this.pauseAfterKeyboardOp();
  }

  @Override
  public void waitForFrames(
    final int frames)
    throws Exception
  {
    for (var index = 0; index < frames; ++index) {
      XCFXThread.run(() -> {
        Thread.sleep(1L);
        return null;
      }).get(this.timeout, MILLISECONDS);
    }
  }

  @Override
  public void waitUntil(
    final long ms,
    final BooleanSupplier predicate)
    throws TimeoutException, Exception
  {
    final var duration =
      Duration.of(ms, ChronoUnit.MILLIS);
    final var timeThen =
      Instant.now();

    while (true) {
      try {
        final var isTrue =
          XCFXThread.run(() -> Boolean.valueOf(predicate.getAsBoolean()))
            .get(1L, MILLISECONDS);

        if (isTrue.booleanValue()) {
          return;
        }
      } catch (final TimeoutException e) {
        // Ignore.
      }
      final var timeNow = Instant.now();
      if (Duration.between(timeThen, timeNow).compareTo(duration) >= 0) {
        throw new TimeoutException(
          "Condition did not become true before the desired timeout.");
      }
    }
  }

  @Override
  public void reset(
    final Optional<Window> window)
    throws Exception
  {
    this.slowMotionDisable();

    if (window.isPresent()) {
      this.execute(() -> {
        final var actual = window.get();
        final var scene = actual.getScene();
        final var root = scene.getRoot();
        opBringStageToFront(root);
        this.opPointMouseAt(root);
      });
    }

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
