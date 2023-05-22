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

import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <p>The commander interface.</p>
 *
 * <p>A <i>commander</i> accepts the results of running tests, and supplies
 * resources such as new stages, robots for automation, keyboard maps, etc.</p>
 */

public interface XCCommanderType extends AutoCloseable
{
  /**
   * Set the state of a given test.
   *
   * @param test The test
   */

  @XCOnAnyThread
  void setTestState(XCTestInfo test);

  /**
   * Set the expected number of tests.
   *
   * @param count The test count
   */

  @XCOnAnyThread
  void setTestCount(long count);

  /**
   * Determine the current keyboard map and return it.
   *
   * @return The keyboard map
   */

  @XCOnAnyThread
  CompletableFuture<XCKeyMap> keyMap();

  /**
   * Send the commander window to the back.
   */

  @XCOnAnyThread
  void sendToBack();

  /**
   * @return A robot for automated tests
   */

  @XCOnAnyThread
  CompletableFuture<XCRobotType> robot();

  /**
   * Create a new stage, initializing it using the given function.
   *
   * @param onCreate The initialization function
   *
   * @return A new stage
   */

  @XCOnAnyThread
  CompletableFuture<Stage> stageNew(Consumer<Stage> onCreate);

  /**
   * Create a new stage, initializing it using the given function.
   *
   * @param onCreate The initialization function
   *
   * @return A new stage
   *
   * @throws Exception On errors
   */

  @XCOnAnyThread
  default Stage stageNewAndWait(
    final Consumer<Stage> onCreate)
    throws Exception
  {
    return this.stageNew(onCreate)
      .get(5L, TimeUnit.SECONDS);
  }

  /**
   * Close all stages that have ever been returned by {@link #stageNew(Consumer)}
   *
   * @return The operation in progress
   */

  @XCOnAnyThread
  CompletableFuture<Void> stageCloseAll();

  /**
   * Set the application info to be displayed by the commander window.
   *
   * @param info The information
   */

  @XCOnAnyThread
  void setApplicationInfo(XCApplicationInfo info);

}
