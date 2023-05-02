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


package com.io7m.xoanon.extension.internal;

import com.io7m.xoanon.extension.XoBotType;
import com.io7m.xoanon.extension.XoFXThread;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Set;

/**
 * The basic bot implementation.
 */

public final class XoBot implements XoBotType
{
  private final Stage rootStage;

  /**
   * The basic bot implementation.
   *
   * @param inRootStage The root stage
   */

  public XoBot(
    final Stage inRootStage)
  {
    this.rootStage =
      Objects.requireNonNull(inRootStage, "rootStage");
  }

  private static MouseEvent createMouseClick(
    final Scene scene,
    final double x,
    final double y,
    final MouseButton mouseButton,
    final Set<MouseModifier> modifiers,
    final int clickCount)
  {
    final var screenMouseX =
      scene.getWindow().getX() + scene.getX() + x;
    final var screenMouseY =
      scene.getWindow().getY() + scene.getY() + y;

    return new MouseEvent(
      MouseEvent.MOUSE_CLICKED,
      x,
      y,
      screenMouseX,
      screenMouseY,
      mouseButton,
      clickCount,
      modifiers.contains(MouseModifier.SHIFT),
      modifiers.contains(MouseModifier.CONTROL),
      modifiers.contains(MouseModifier.ALT),
      modifiers.contains(MouseModifier.META),
      modifiers.contains(MouseModifier.BUTTON_1),
      modifiers.contains(MouseModifier.BUTTON_2),
      modifiers.contains(MouseModifier.BUTTON_3),
      false,
      mouseButton == MouseButton.SECONDARY,
      false,
      null
    );
  }

  @Override
  public Stage stage()
  {
    return this.rootStage;
  }

  @Override
  public Node findWithId(
    final String id)
    throws Exception
  {
    return XoFXThread.run(() -> {
      final var scene =
        this.rootStage.getScene();
      final var root =
        scene.getRoot();

      return root.lookup("#" + id);
    }).get();
  }

  @Override
  public void click(
    final Node node)
    throws Exception
  {
    XoFXThread.run(() -> {
      node.fireEvent(
        createMouseClick(
          node.getScene(),
          0.0,
          0.0,
          MouseButton.PRIMARY,
          Set.of(),
          1
        )
      );
      return Unit.UNIT;
    }).get();
  }

  @Override
  public void type(
    final Node node,
    final String text)
    throws Exception
  {
    for (final var ch : text.toCharArray()) {
      XoFXThread.run(() -> {
        node.fireEvent(
          createKeyPress(
            node.getScene(),
            0.0,
            0.0,
            Character.toString(ch),
            Character.toString(ch)
          )
        );
        return Unit.UNIT;
      }).get();
    }
  }

  private enum Unit
  {
    UNIT
  }

  private enum MouseModifier
  {
    SHIFT,
    CONTROL,
    ALT,
    META,
    BUTTON_1,
    BUTTON_2,
    BUTTON_3
  }

  private static KeyEvent createKeyPress(
    final Scene scene,
    final double x,
    final double y,
    final String ch,
    final String text)
  {
    final var screenMouseX =
      scene.getWindow().getX() + scene.getX() + x;
    final var screenMouseY =
      scene.getWindow().getY() + scene.getY() + y;

    return new KeyEvent(
      KeyEvent.KEY_TYPED,
      ch,
      text,
      KeyCode.getKeyCode(ch),
      false,
      false,
      false,
      false
    );
  }
}
