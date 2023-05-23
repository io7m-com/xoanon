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

package com.io7m.xoanon.commander.api;

import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

/**
 * A robot that can send events to JavaFX nodes. All methods submit work to the
 * JavaFX application thread, and block until the operations have completed on
 * that thread (up to a configurable timeout value).
 */

public interface XCRobotType extends XCRobotFindType, XCRobotInteractionsType
{
  /**
   * Disable slow motion mode.
   */

  void slowMotionDisable();

  /**
   * Enable slow motion mode. This causes the robot to insert long pauses
   * between operations in order to assist with debugging failing tests.
   */

  void slowMotionEnable();

  /**
   * @return The current timeout value in milliseconds
   */

  long timeoutMilliseconds();

  /**
   * Set the current timeout value in milliseconds.
   *
   * @param ms The number of milliseconds
   */

  void setTimeoutMilliseconds(long ms);

  /**
   * Wait for the default stage to close.
   *
   * @param stage        The stage
   * @param milliseconds The time to wait in milliseconds
   *
   * @throws Exception On errors
   */

  void waitForStageToClose(
    Stage stage,
    long milliseconds)
    throws Exception;

  /**
   * @return The underlying JavaFX roboto
   */

  Robot robot();

  /**
   * Do nothing for the given number of JavaFX frames.
   *
   * @param frames The frames
   *
   * @throws Exception On errors
   */

  void sleepForFrames(int frames)
    throws Exception;

  /**
   * Wait until the given predicate is true. The predicate is evaluated
   * repeatedly on the JavaFX UI thread.
   *
   * @param ms        The maximum number of milliseconds to wait
   * @param predicate The predicate to evaluate
   *
   * @throws TimeoutException If the predicate does not return {@code true}
   *                          before {@code ms} milliseconds have elapsed.
   * @throws Exception        On errors
   */

  void waitUntil(
    long ms,
    BooleanSupplier predicate)
    throws TimeoutException, Exception;

  /**
   * Release all keys, and all mouse buttons.
   */
  void reset();
}
