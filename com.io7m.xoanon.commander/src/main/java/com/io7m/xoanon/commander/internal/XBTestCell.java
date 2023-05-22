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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * A test cell.
 */

public final class XBTestCell
  extends ListCell<XCTestInfo>
{
  private final Parent root;
  private final XBTestCellController controller;

  /**
   * A test cell.
   *
   * @param strings The strings
   */

  public XBTestCell(
    final XBStrings strings)
  {
    Objects.requireNonNull(strings, "strings");

    try {
      final FXMLLoader loader =
        new FXMLLoader(
          XBTestCell.class.getResource(
            "/com/io7m/xoanon/commander/test.fxml"));
      loader.setResources(strings.resources());

      this.root = loader.load();
      this.controller = loader.getController();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  protected void updateItem(
    final XCTestInfo item,
    final boolean empty)
  {
    super.updateItem(item, empty);
    if (empty || item == null) {
      this.setGraphic(null);
    } else {
      this.controller.set(item);
      this.setGraphic(this.root);
    }
  }
}
