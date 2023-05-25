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
 * Convenient extensions to the robot functions related to finding nodes.
 */

public interface XCRobotFindType extends XCRobotFindPrimitivesType
{
  /**
   * Find the node with the given ID by searching through all open stages.
   *
   * @param id The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default Node findWithIdInAnyStage(
    final String id)
    throws Exception
  {
    return this.findWithIdInAnyStage(Node.class, id);
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

  default Node findWithId(
    final Stage stage,
    final String id)
    throws Exception
  {
    return this.findWithId(Node.class, stage, id);
  }

  /**
   * Find the node with the given ID.
   *
   * @param parent The parent within which to search
   * @param id     The ID
   *
   * @return The node
   *
   * @throws Exception On errors
   */

  default Node findWithId(
    final Parent parent,
    final String id)
    throws Exception
  {
    return this.findWithId(Node.class, parent, id);
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

  default Node findWithTextInAnyStage(
    final String text)
    throws Exception
  {
    return this.findWithTextInAnyStage(Node.class, text);
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

  default Node findWithText(
    final Stage stage,
    final String text)
    throws Exception
  {
    return this.findWithText(Node.class, stage, text);
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

  default Node findWithText(
    final Parent parent,
    final String text)
    throws Exception
  {
    return this.findWithText(Node.class, parent, text);
  }

  /**
   * Find all nodes in the given stage that have the given CSS class.
   *
   * @param stage    The stage
   * @param cssClass The CSS class
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  default List<Node> findAllWithClassInStage(
    final Stage stage,
    final String cssClass)
    throws Exception
  {
    return this.findAllWithClassInStage(Node.class, stage, cssClass);
  }

  /**
   * Find all descendants of the given {@code parent} that have the
   * given CSS class.
   *
   * @param parent   The parent
   * @param cssClass The CSS class
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  default List<Node> findAllWithClass(
    final Parent parent,
    final String cssClass)
    throws Exception
  {
    return this.findAllWithClass(Node.class, parent, cssClass);
  }

  /**
   * Find all descendants of the given {@code parent} that have the
   * given CSS class.
   *
   * @param parent   The parent
   * @param cssClass The CSS class
   *
   * @return The set of matching descendants
   *
   * @throws Exception On errors
   */

  default List<Node> findAllWithClassInAnyStage(
    final Parent parent,
    final String cssClass)
    throws Exception
  {
    return this.findAllWithClassInAnyStage(Node.class, cssClass);
  }
}
