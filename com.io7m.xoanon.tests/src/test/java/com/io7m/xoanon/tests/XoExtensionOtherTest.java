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

import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(XoExtension.class)
public final class XoExtensionOtherTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtensionOtherTest.class);

  @Test
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
}
