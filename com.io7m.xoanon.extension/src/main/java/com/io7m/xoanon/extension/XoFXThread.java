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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Functions to execute code on the JavaFX application thread.
 */

public final class XoFXThread
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoFXThread.class);

  private XoFXThread()
  {

  }

  /**
   * Execute the given function on the JavaFX thread. If this is already the
   * JavaFX thread, execute the function directly.
   *
   * @param supplier The function
   * @param <T>      The type of returned values
   *
   * @return The operation in progress
   */

  public static <T> CompletableFuture<T> run(
    final XoFXThreadOperationType<T> supplier)
  {
    final var future = new CompletableFuture<T>();

    if (Platform.isFxApplicationThread()) {
      try {
        future.complete(supplier.execute());
      } catch (final Throwable e) {
        LOG.debug("error: ", e);
        future.completeExceptionally(e);
      }
      return future;
    }

    try {
      Platform.runLater(() -> {
        try {
          future.complete(supplier.execute());
        } catch (final Throwable e) {
          LOG.debug("error: ", e);
          future.completeExceptionally(e);
        }
      });
    } catch (final Throwable e) {
      LOG.debug("error: ", e);
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Run the given code on the FX thread and wait for it to complete.
   *
   * @param time     The timeout
   * @param unit     The timeout unit
   * @param supplier The code
   * @param <T>      The type of returned values
   *
   * @return The result of the given supplier
   *
   * @throws ExecutionException   On errors
   * @throws InterruptedException On interruption
   * @throws TimeoutException     On timeouts
   */

  public static <T> T runAndWait(
    final long time,
    final TimeUnit unit,
    final XoFXThreadOperationType<T> supplier)
    throws ExecutionException, InterruptedException, TimeoutException
  {
    return run(supplier).get(time, unit);
  }
}
