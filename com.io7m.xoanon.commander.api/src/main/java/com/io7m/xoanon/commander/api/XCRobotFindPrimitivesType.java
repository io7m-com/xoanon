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

import java.util.List;

/**
 * Robot functions related to finding nodes.
 */

public interface XCRobotFindPrimitivesType
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

  <T extends Node> List<T> findAllInStage(
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

  <T extends Node> List<T> findAll(
    Class<T> clazz,
    Parent parent)
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

  <T extends Node> T findWithId(
    Class<T> clazz,
    Stage stage,
    String id)
    throws Exception;

  /**
   * Find the node with the given ID, casting it to {@code T}.
   *
   * @param <T>   The type of node
   * @param clazz The class
   * @param root  The root node within which to search
   * @param id    The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  <T extends Node> T findWithId(
    Class<T> clazz,
    Parent root,
    String id)
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

  <T extends Node> T findWithTextInAnyStage(
    Class<T> clazz,
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

  <T extends Node> T findWithText(
    Class<T> clazz,
    Stage stage,
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

  <T extends Node> T findWithText(
    Class<T> clazz,
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

  <T extends Node> T findWithIdInAnyStage(
    Class<T> clazz,
    String id)
    throws Exception;

  /**
   * Find all nodes in the given stage that are of type {@code clazz}
   * (or some subtype of it), and have the given CSS class.
   *
   * @param clazz    The class
   * @param stage    The stage
   * @param cssClass The CSS class
   * @param <T>      The type of descendants
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  <T extends Node> List<T> findAllWithClassInStage(
    Class<T> clazz,
    Stage stage,
    String cssClass)
    throws Exception;

  /**
   * Find all descendants of the given {@code parent} that are of type
   * {@code clazz} (or some subtype of it), and have the given CSS class.
   *
   * @param clazz    The class
   * @param parent   The parent
   * @param cssClass The CSS class
   * @param <T>      The type of descendants
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  <T extends Node> List<T> findAllWithClass(
    Class<T> clazz,
    Parent parent,
    String cssClass)
    throws Exception;

  /**
   * Find all nodes in any visible stage that are of type {@code clazz}
   * (or some subtype of it), and have the given CSS class.
   *
   * @param clazz    The class
   * @param cssClass The CSS class
   * @param <T>      The type of descendants
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  <T extends Node> List<T> findAllWithClassInAnyStage(
    Class<T> clazz,
    String cssClass)
    throws Exception;
}
