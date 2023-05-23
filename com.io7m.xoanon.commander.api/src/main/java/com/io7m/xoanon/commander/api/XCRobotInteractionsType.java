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

import java.util.List;

/**
 * The robot operations that interact with controls.
 */

public interface XCRobotInteractionsType
{
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
   * Double-click on the given node using the primary mouse button.
   *
   * @param node The node
   *
   * @throws Exception On errors
   */

  void doubleClick(Node node)
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
}
