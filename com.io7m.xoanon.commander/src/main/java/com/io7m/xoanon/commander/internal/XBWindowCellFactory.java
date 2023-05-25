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

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.Objects;

/**
 * A factory of cells.
 */

public final class XBWindowCellFactory
  implements Callback<
  ListView<Window>,
  ListCell<Window>>
{
  private final XBStrings strings;

  /**
   * A factory of cells.
   *
   * @param inStrings The strings
   */

  public XBWindowCellFactory(
    final XBStrings inStrings)
  {
    this.strings = Objects.requireNonNull(inStrings, "inStrings");
  }

  @Override
  public ListCell<Window> call(
    final ListView<Window> param)
  {
    return new XBWindowCell(this.strings);
  }
}
