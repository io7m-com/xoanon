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
 * that thread (up to a configurable timeout value).
 */

public interface XCRobotType
{
  /**
   * @return The current timeout value in milliseconds
   */

  long timeoutMilliseconds();

  /**
   * Set the current timeout value in milliseconds.
   *
   * @param ms The number of milliseconds
   */

  void setTimeoutMilliseconds(long ms);

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
   * Find the node with the given ID by searching through all open stages.
   *
   * @param id The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  Node findWithIdInAnyStage(
    String id)
    throws Exception;

  /**
   * Find the node with the given ID by searching through all open stages,
   * casting it to {@code T}.
   *
   * @param <T>   The type of node
   * @param clazz The class
   * @param id    The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default <T extends Node> T findWithIdInAnyStage(
    final Class<T> clazz,
    final String id)
    throws Exception
  {
    return clazz.cast(this.findWithIdInAnyStage(id));
  }

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
   * Find the node with the given ID, casting it to {@code T}.
   *
   * @param <T>   The type of node
   * @param clazz The class
   * @param stage The stage within which to search
   * @param id    The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default <T extends Node> T findWithId(
    final Class<T> clazz,
    final Stage stage,
    final String id)
    throws Exception
  {
    return clazz.cast(this.findWithId(stage, id));
  }


  /**
   * Find the node with the given text content in any open stage.
   *
   * @param text The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  Node findWithTextInAnyStage(
    String text)
    throws Exception;

  /**
   * Find the node with the given text content in any open stage, casting
   * it to {@code T}.
   *
   * @param <T>   The type of node
   * @param clazz The class
   * @param text  The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default <T extends Node> T findWithTextInAnyStage(
    final Class<T> clazz,
    final String text)
    throws Exception
  {
    return clazz.cast(this.findWithTextInAnyStage(text));
  }

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
   * Find the node with the given text content, casting it to {@code T}.
   *
   * @param <T>   The type of node
   * @param clazz The class
   * @param stage The stage within which to search
   * @param text  The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default <T extends Node> T findWithText(
    final Class<T> clazz,
    final Stage stage,
    final String text)
    throws Exception
  {
    return clazz.cast(this.findWithText(stage, text));
  }

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
   * Find the node with the given text content, starting at the given parent
   * node.
   *
   * @param <T>    The type of node
   * @param clazz  The class
   * @param parent The parent
   * @param text   The text
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default <T extends Node> T findWithText(
    final Class<T> clazz,
    final Parent parent,
    final String text)
    throws Exception
  {
    return clazz.cast(this.findWithText(parent, text));
  }

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
