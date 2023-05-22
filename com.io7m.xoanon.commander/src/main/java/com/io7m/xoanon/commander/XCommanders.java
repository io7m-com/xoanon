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

package com.io7m.xoanon.commander;

import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.internal.XBApplication;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

/**
 * Functions to boot a commander.
 */

public final class XCommanders
{
  private XCommanders()
  {

  }

  /**
   * Boot a commander, starting up the JavaFX Platform.
   *
   * @return The operation in progress
   *
   * @see Platform#startup(Runnable)
   */

  public static CompletableFuture<XCCommanderType> boot()
  {
    final var future = new CompletableFuture<XCCommanderType>();
    Platform.startup(() -> {
      try {
        future.complete(XBApplication.boot());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }
}


