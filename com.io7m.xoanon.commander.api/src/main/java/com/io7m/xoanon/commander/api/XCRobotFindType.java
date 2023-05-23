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
import javafx.stage.Stage;

import java.util.Collection;

/**
 * Robot functions related to finding nodes.
 */

public interface XCRobotFindType
{
  /**
   * Find all nodes in the given stage that are of type {@code clazz} (or some subtype of it).
   *
   * @param clazz The class
   * @param stage The stage
   * @param <T>   The type of descendants
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  <T extends Node> Collection<T> findAllInStage(
    Class<T> clazz,
    Stage stage)
    throws Exception;

  /**
   * Find all descendants of the given {@code parent} that are of type {@code clazz} (or some subtype of it).
   *
   * @param clazz  The class
   * @param parent The parent
   * @param <T>    The type of descendants
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  <T extends Node> Collection<T> findAll(
    Class<T> clazz,
    Parent parent)
    throws Exception;

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
}
