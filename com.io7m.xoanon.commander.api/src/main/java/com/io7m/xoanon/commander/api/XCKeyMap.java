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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A map of characters to keys.
 *
 * @param keys The mapping
 */

public record XCKeyMap(
  Map<Character, XCKey> keys)
  implements Serializable
{
  /**
   * @return An empty mapping
   */

  public static XCKeyMap empty()
  {
    return new XCKeyMap(Map.of());
  }

  /**
   * Map a list of characters to key codes.
   *
   * @param characters The input characters
   *
   * @return The keycodes
   */

  public List<XCKey> toCodes(
    final Collection<Character> characters)
  {
    final var codes =
      new ArrayList<XCKey>(characters.size());

    for (final var character : characters) {
      codes.add(
        Optional.ofNullable(this.keys().get(character))
          .orElseThrow(() -> {
            return new IllegalArgumentException(
              "No key mapping is known for character '%s'"
                .formatted(character));
          })
      );
    }
    return List.copyOf(codes);
  }
}
