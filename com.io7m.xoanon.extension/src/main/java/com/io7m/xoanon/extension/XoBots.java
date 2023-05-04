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

import com.io7m.xoanon.extension.internal.XoBot;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

/**
 * Functions to create robots.
 */

public final class XoBots
{
  private XoBots()
  {

  }

  /**
   * Create a new robot for the given stage.
   *
   * @param stage The stage
   *
   * @return A new robot
   *
   * @throws Exception On errors
   */

  public static XoBotType createForStage(
    final Stage stage)
    throws Exception
  {
    return XoFXThread.run(() -> {
      return new XoBot(stage);
    }).get(1L, TimeUnit.SECONDS);
  }
}
