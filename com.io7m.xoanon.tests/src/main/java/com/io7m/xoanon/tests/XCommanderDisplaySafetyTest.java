/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.xoanon.commander.XCommanderDisplaySafety;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class XCommanderDisplaySafetyTest
{
  private static final String[] DISPLAYS_CONSIDERED_LOCAL = {
    ":0",
    ":0.0",
    ":0.1",
    "localhost:0",
    "localhost:0.0",
    "localhost:0.1"
  };

  private static final String[] DISPLAYS_CONSIDERED_NON_LOCAL = {
    ":1",
    ":1.0",
    ":1.1",
    "localhost:1",
    "localhost:1.0",
    "localhost:1.1",
    "example.com:0",
    "example.com:0.0",
    "example.com:0.1"
  };

  @TestFactory
  public Stream<DynamicTest> testCheckLocal()
  {
    return Stream.of(DISPLAYS_CONSIDERED_LOCAL)
      .map(s -> {
        return DynamicTest.dynamicTest("testCheckLocal_" + s, () -> {
          final var ex =
            assertThrows(UnsupportedOperationException.class, () -> {
              XCommanderDisplaySafety.checkDisplayPermitted(
                Map.ofEntries(
                  Map.entry("DISPLAY", s)
                )
              );
            });
          System.out.println(ex.getMessage());
        });
      });
  }

  @TestFactory
  public Stream<DynamicTest> testCheckLocalNotOverridden()
  {
    return Stream.of(DISPLAYS_CONSIDERED_LOCAL)
      .map(s -> {
        return DynamicTest.dynamicTest(
          "testCheckLocalNotOverridden_" + s,
          () -> {
            assertThrows(
              UnsupportedOperationException.class,
              () -> {
                XCommanderDisplaySafety.checkDisplayPermitted(
                  Map.ofEntries(
                    Map.entry(
                      "XOANON_REALLY_USE_LOCAL_DISPLAY",
                      "false"),
                    Map.entry("DISPLAY", s)
                  )
                );
              });
          });
      });
  }

  @TestFactory
  public Stream<DynamicTest> testCheckNotLocal()
  {
    return Stream.of(DISPLAYS_CONSIDERED_NON_LOCAL)
      .map(s -> {
        return DynamicTest.dynamicTest("testCheckNotLocal_" + s, () -> {
          XCommanderDisplaySafety.checkDisplayPermitted(
            Map.ofEntries(
              Map.entry("DISPLAY", s)
            )
          );
        });
      });
  }

  @TestFactory
  public Stream<DynamicTest> testCheckLocalOverridden()
  {
    return Stream.of(DISPLAYS_CONSIDERED_LOCAL)
      .map(s -> {
        return DynamicTest.dynamicTest("testCheckLocalOverridden_" + s, () -> {
          XCommanderDisplaySafety.checkDisplayPermitted(
            Map.ofEntries(
              Map.entry("XOANON_REALLY_USE_LOCAL_DISPLAY", "true"),
              Map.entry("DISPLAY", s)
            )
          );
        });
      });
  }

  @TestFactory
  public Stream<DynamicTest> testCheckNotLocalUndefined()
  {
    return Stream.of(DISPLAYS_CONSIDERED_NON_LOCAL)
      .map(s -> {
        return DynamicTest.dynamicTest("testCheckNotLocalUndefined_" + s, () -> {
          final var ex =
            assertThrows(UnsupportedOperationException.class, () -> {
              XCommanderDisplaySafety.checkDisplayPermitted(
                Map.ofEntries()
              );
            });
          System.out.println(ex.getMessage());
        });
      });
  }
}
