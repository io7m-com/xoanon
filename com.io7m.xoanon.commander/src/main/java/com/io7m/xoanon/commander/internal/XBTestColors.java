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

import com.io7m.xoanon.commander.api.XCTestInfo;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Colors for tests.
 */

public final class XBTestColors
{
  static final Color COLOR_FAILED =
    Color.web("#ff1f79");

  static final Color COLOR_INITIAL =
    Color.web("#c0c0c0");

  static final Color COLOR_RUNNING =
    Color.web("#1fb8ff");

  static final Color COLOR_SUCCEEDED =
    Color.web("#5ffa9f");

  private XBTestColors()
  {

  }

  static Paint colorForTest(
    final XCTestInfo item)
  {
    return switch (item.state()) {
      case FAILED -> COLOR_FAILED;
      case INITIAL -> COLOR_INITIAL;
      case RUNNING -> COLOR_RUNNING;
      case SUCCEEDED -> COLOR_SUCCEEDED;
    };
  }
}
