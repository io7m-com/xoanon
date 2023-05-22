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

package com.io7m.xoanon.tests;

import com.io7m.percentpass.extension.PercentPassing;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCFXThread;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(XoExtension.class)
public final class XoExtensionTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtensionTest.class);

  @PercentPassing(executionCount = 6, passPercent = 50.0)
  public void testButton(
    final XCRobotType bot,
    final XCCommanderType commander)
    throws Exception
  {
    final var clicked =
      new AtomicBoolean(false);

    final var stage =
      commander.stageNewAndWait(newStage -> {
        final var button = new Button();
        button.setId("x");
        button.setOnMouseClicked(event -> {
          LOG.debug("click!");
          clicked.set(true);
        });
        newStage.setScene(new Scene(button));
      });

    final var node = bot.findWithId(stage, "x");
    bot.click(node);

    assertTrue(clicked.get());
  }

  @Test
  public void testTextFieldText(
    final XCRobotType bot,
    final XCCommanderType commander)
    throws Exception
  {
    final var text =
      new AtomicReference<String>();

    final var stage =
      commander.stageNewAndWait(newStage -> {
        final var field = new TextField();
        field.setId("x");
        field.textProperty()
          .addListener((observable, oldValue, newValue) -> {
            text.set(newValue);
          });
        newStage.setScene(new Scene(field));
      });

    final var node = bot.findWithId(stage, "x");
    bot.click(node);
    bot.typeText(node, "Hello!");

    assertEquals("Hello!", text.get());
  }

  @Test
  public void testStage(
    final XCCommanderType commander)
    throws Exception
  {
    XCFXThread.runVWait(1L, TimeUnit.SECONDS, () -> {
      final var stage = new Stage();
      commander.stageRegisterForClosing(stage);

      stage.setWidth(640.0);
      stage.setHeight(640.0);
      stage.show();
    });
  }
}
