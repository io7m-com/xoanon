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

/**
 * A robot that can send events to JavaFX nodes. All methods submit work to the
 * JavaFX application thread, and block until the operations have completed on
 * that thread (up to a configurable timeout value).
 */

public interface XCRobotType
  extends XCRobotFindType,
  XCRobotInteractionsType, XCRobotConfigurationType, XCRobotWaitingType
{
  /**
   * Execute {@code f} on the UI thread, and wait for it to complete
   * (subject to the configured timeout).
   *
   * @param f The runnable
   *
   * @throws Exception On errors
   * @see XCFXThread#run(XCFXThreadOperationType)
   */

  void execute(Runnable f)
    throws Exception;

  /**
   * Evaluate {@code f} on the UI thread, and wait for it to complete
   * (subject to the configured timeout).
   *
   * @param f   The function
   * @param <T> The type of results
   *
   * @return The result of {@code f}
   *
   * @throws Exception On errors
   * @see XCFXThread#run(XCFXThreadOperationType)
   */

  <T> T evaluate(XCFXThreadOperationType<T> f)
    throws Exception;

  /**
   * @return The underlying JavaFX robot
   */

  Robot robot();
}
