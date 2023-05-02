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

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Functions to execute code on the JavaFX application thread.
 */

public final class XoFXThread
{
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
    final Supplier<T> supplier)
  {
    final var future = new CompletableFuture<T>();

    if (Platform.isFxApplicationThread()) {
      try {
        future.complete(supplier.get());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
      return future;
    }

    try {
      Platform.runLater(() -> {
        try {
          future.complete(supplier.get());
        } catch (final Throwable e) {
          future.completeExceptionally(e);
        }
      });
    } catch (final Throwable e) {
      future.completeExceptionally(e);
    }
    return future;
  }
}
