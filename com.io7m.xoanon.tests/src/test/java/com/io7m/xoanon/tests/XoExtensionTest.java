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
import com.io7m.xoanon.commander.XBVersion;
import com.io7m.xoanon.commander.api.XCApplicationInfo;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCFXThread;
import com.io7m.xoanon.commander.api.XCKeyMap;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(XoExtension.class)
public final class XoExtensionTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtensionTest.class);

  @BeforeAll
  public static void beforeAll()
  {
    XoExtension.setApplicationInfo(
      new XCApplicationInfo(
        "com.io7m.xoanon",
        XBVersion.MAIN_VERSION,
        XBVersion.MAIN_BUILD
      )
    );
  }

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
  public void testTextFieldTextFind(
    final XCRobotType bot,
    final XCCommanderType commander)
    throws Exception
  {
    final var text =
      new AtomicReference<String>();

    commander.stageNewAndWait(newStage -> {
      final var field = new TextField();
      field.setId("x");
      field.textProperty()
        .addListener((observable, oldValue, newValue) -> {
          text.set(newValue);
        });
      newStage.setScene(new Scene(field));
    });

    final var node = bot.findWithIdInAnyStage("x");
    bot.click(node);
    bot.typeText(node, "Hello!");

    assertEquals("Hello!", text.get());
  }

  @Test
  public void testTextFieldTextFindNonexistentId(
    final XCRobotType bot)
  {
    final var ex =
      assertThrows(ExecutionException.class, () -> {
        bot.findWithIdInAnyStage("x");
      });

    assertInstanceOf(NoSuchElementException.class, ex.getCause());
  }

  @Test
  public void testTextFieldTextFindNonexistentText(
    final XCRobotType bot)
  {
    final var ex =
      assertThrows(ExecutionException.class, () -> {
        bot.findWithTextInAnyStage("Clearly does not exist.");
      });

    assertInstanceOf(NoSuchElementException.class, ex.getCause());
  }

  @Test
  public void testDoubleClick(
    final XCRobotType bot,
    final XCCommanderType commander)
    throws Exception
  {
    final var doubleClicked =
      new AtomicBoolean(false);

    commander.stageNewAndWait(newStage -> {
      final var button = new Button("OK");
      button.setId("x");
      button.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) {
          doubleClicked.set(true);
        }
      });
      newStage.setScene(new Scene(button));
    });

    final var node = bot.findWithIdInAnyStage(Button.class, "x");
    bot.doubleClick(node);

    assertTrue(doubleClicked.get());
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

  @TestFactory
  public Stream<DynamicTest> testCommonKeys(
    final XCKeyMap keyMap)
  {
    final var expectedCharacters = new ArrayList<Character>();
    for (char c = 'a'; c <= 'z'; ++c) {
      expectedCharacters.add(Character.valueOf(c));
      expectedCharacters.add(Character.valueOf(Character.toUpperCase(c)));
    }
    expectedCharacters.add(Character.valueOf('0'));
    expectedCharacters.add(Character.valueOf('1'));
    expectedCharacters.add(Character.valueOf('2'));
    expectedCharacters.add(Character.valueOf('3'));
    expectedCharacters.add(Character.valueOf('4'));
    expectedCharacters.add(Character.valueOf('5'));
    expectedCharacters.add(Character.valueOf('6'));
    expectedCharacters.add(Character.valueOf('7'));
    expectedCharacters.add(Character.valueOf('8'));
    expectedCharacters.add(Character.valueOf('9'));

    expectedCharacters.add(Character.valueOf('!'));
    expectedCharacters.add(Character.valueOf('"'));
    expectedCharacters.add(Character.valueOf('$'));
    expectedCharacters.add(Character.valueOf('%'));
    expectedCharacters.add(Character.valueOf('^'));
    expectedCharacters.add(Character.valueOf('&'));
    expectedCharacters.add(Character.valueOf('*'));
    expectedCharacters.add(Character.valueOf('('));
    expectedCharacters.add(Character.valueOf(')'));
    expectedCharacters.add(Character.valueOf('-'));
    expectedCharacters.add(Character.valueOf('_'));
    expectedCharacters.add(Character.valueOf('='));
    expectedCharacters.add(Character.valueOf('+'));
    expectedCharacters.add(Character.valueOf('['));
    expectedCharacters.add(Character.valueOf(']'));
    expectedCharacters.add(Character.valueOf('{'));
    expectedCharacters.add(Character.valueOf('}'));
    expectedCharacters.add(Character.valueOf(':'));
    expectedCharacters.add(Character.valueOf(';'));
    expectedCharacters.add(Character.valueOf('@'));
    expectedCharacters.add(Character.valueOf('\''));
    expectedCharacters.add(Character.valueOf('~'));
    expectedCharacters.add(Character.valueOf('#'));
    expectedCharacters.add(Character.valueOf('<'));
    expectedCharacters.add(Character.valueOf('>'));
    expectedCharacters.add(Character.valueOf(','));
    expectedCharacters.add(Character.valueOf('.'));
    expectedCharacters.add(Character.valueOf('?'));
    expectedCharacters.add(Character.valueOf('/'));
    expectedCharacters.add(Character.valueOf('\\'));
    expectedCharacters.add(Character.valueOf('|'));
    expectedCharacters.add(Character.valueOf('`'));

    return expectedCharacters.stream()
      .map(c -> {
        return DynamicTest.dynamicTest(
          "testCommonKeys_%s".formatted(c),
          () -> {
            assertTrue(
              keyMap.keys().containsKey(c),
              () -> "Key map contains '%s'".formatted(c)
            );
          });
      });
  }

  @Test
  public void testWaitUntil(
    final XCRobotType bot,
    final XCCommanderType commander)
    throws Exception
  {
    commander.stageNewAndWait(newStage -> {
      final var checkBox = new CheckBox();
      checkBox.setSelected(false);
      checkBox.setId("x");
      newStage.setScene(new Scene(checkBox));
    });

    final var check =
      bot.findWithIdInAnyStage(CheckBox.class, "x");

    assertThrows(TimeoutException.class, () -> {
      bot.waitUntil(1_000L, check::isSelected);
    });

    Platform.runLater(() -> {
      check.setSelected(true);
    });

    bot.waitUntil(1_000L, check::isSelected);
  }
}
