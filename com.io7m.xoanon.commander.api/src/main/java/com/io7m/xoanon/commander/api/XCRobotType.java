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

package com.io7m.xoanon.commander.api;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.List;

/**
 * A robot that can send events to JavaFX nodes. All methods submit work to the
 * JavaFX application thread, and block until the operations have completed on
 * that thread.
 */

public interface XCRobotType
{
  /**
   * Wait for the default stage to close.
   *
   * @param stage        The stage
   * @param milliseconds The time to wait in milliseconds
   *
   * @throws Exception On errors
   */

  void waitForStageToClose(
    Stage stage,
    long milliseconds)
    throws Exception;

  /**
   * @return The underlying JavaFX roboto
   */

  Robot robot();

  /**
   * Find the node with the given ID.
   *
   * @param stage The stage within which to search
   * @param id    The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  Node findWithId(
    Stage stage,
    String id)
    throws Exception;

  /**
   * Find the node with the given text content.
   *
   * @param stage The stage within which to search
   * @param text  The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  Node findWithText(
    Stage stage,
    String text)
    throws Exception;

  /**
   * Find the node with the given text content, starting at the given parent
   * node.
   *
   * @param parent The parent
   * @param text   The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  Node findWithText(
    Parent parent,
    String text)
    throws Exception;

  /**
   * Click on the given node using the primary mouse button.
   *
   * @param node The node
   *
   * @throws Exception On errors
   */

  void click(Node node)
    throws Exception;

  /**
   * Point the mouse cursor at the given node.
   *
   * @param node The node
   *
   * @throws Exception On errors
   */

  void pointAt(Node node)
    throws Exception;

  /**
   * Type the given key codes on the given node.
   *
   * @param node  The node
   * @param codes The codes
   *
   * @throws Exception On errors
   */

  void type(
    Node node,
    List<XCKey> codes)
    throws Exception;

  /**
   * Type text on the given node.
   *
   * @param node The node
   * @param text The text
   *
   * @throws Exception On errors
   */

  void typeText(
    Node node,
    String text)
    throws Exception;

  /**
   * Do nothing for the given number of JavaFX frames.
   *
   * @param frames The frames
   *
   * @throws Exception On errors
   */

  void sleepForFrames(int frames)
    throws Exception;

  /**
   * Release all keys, and all mouse buttons.
   */
  void reset();
}
