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

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Information about a test. A test is expected to be uniquely identified
 * by the {@link #id()}.
 *
 * @param time  The time of the last update
 * @param id    The unique test ID
 * @param name  The display name
 * @param state The test state
 */

public record XCTestInfo(
  OffsetDateTime time,
  String id,
  String name,
  XCTestState state)
{
  /**
   * Information about a test. A test is expected to be uniquely identified
   * by the {@link #id()}.
   *
   * @param time  The time of the last update
   * @param id    The unique test ID
   * @param name  The display name
   * @param state The test state
   */

  public XCTestInfo
  {
    Objects.requireNonNull(time, "time");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(state, "state");
  }
}
