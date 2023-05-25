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

import javafx.stage.Window;

import java.util.Optional;

/**
 * Robot functions related to configuration.
 */

public interface XCRobotConfigurationType
{
  /**
   * Disable slow motion mode.
   */

  void slowMotionDisable();

  /**
   * Enable slow motion mode. This causes the robot to insert long pauses
   * between operations in order to assist with debugging failing tests.
   * Note that this is reset by {@link #reset()}.
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
   * @return The "after mouse operation" pause time
   */

  long timePauseAfterMouseOperationMilliseconds();

  /**
   * Set the number of milliseconds by which to pause after each mouse operation
   * such as clicking, pointing, etc.
   *
   * @param ms The millisecond pause time
   */

  void setTimePauseAfterMouseOperationMilliseconds(long ms);

  /**
   * @return The "after keyboard operation" pause time
   */

  long timePauseAfterKeyboardOperationMilliseconds();

  /**
   * Set the number of milliseconds by which to pause after each keyboard operation
   * such as pressing a key, etc.
   *
   * @param ms The millisecond pause time
   */

  void setTimePauseAfterKeyboardOperationMilliseconds(long ms);

  /**
   * @return The time to wait between each click of a double click operation
   */

  long timePauseBetweenDoubleClickMilliseconds();

  /**
   * Set the number of milliseconds to wait between each click of a double
   * click operation.
   *
   * @param ms The pause time between clicks
   */

  void setTimePauseBetweenDoubleClickMilliseconds(long ms);

  /**
   * Release all keys, all mouse buttons, and reset any temporary
   * configuration state (such as {@link #slowMotionEnable()}). If a window
   * is provided, warp the cursor back to the center of that window.
   *
   * @param window The window
   */
  void reset(Optional<Window> window)
    throws Exception;
}
