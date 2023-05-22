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

import com.io7m.xoanon.commander.api.XCCommanderType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * The main commander application.
 */

public final class XBApplication extends Application
{
  private XCCommander controller;

  /**
   * The main commander application.
   */

  public XBApplication()
  {

  }

  /**
   * Boot the commander application and return it.
   *
   * @return The commander
   *
   * @throws IOException On errors
   */

  public static XCCommanderType boot()
    throws IOException
  {
    final var stage = new Stage();
    final var app = new XBApplication();
    app.start(stage);
    return app.controller;
  }

  @Override
  public void start(
    final Stage stage)
    throws IOException
  {
    final var xml =
      XBApplication.class.getResource("/com/io7m/xoanon/commander/main.fxml");
    final var strings =
      new XBStrings(Locale.getDefault());

    final var loader = new FXMLLoader(xml, strings.resources());
    loader.setControllerFactory(param -> new XCCommander(strings, stage));

    final Parent pane =
      loader.load();
    this.controller =
      loader.getController();

    pane.getStylesheets().add(XBCSS.mainStylesheet().toString());
    stage.setScene(new Scene(pane));
    stage.setMaximized(true);
    stage.show();
  }
}
