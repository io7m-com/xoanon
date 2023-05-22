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

package com.io7m.xoanon.demo;

import com.io7m.xoanon.commander.XBVersion;
import com.io7m.xoanon.commander.XCommanders;
import com.io7m.xoanon.commander.api.XCApplicationInfo;
import com.io7m.xoanon.commander.api.XCTestInfo;
import javafx.application.Platform;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;

import static com.io7m.xoanon.commander.api.XCTestState.FAILED;
import static com.io7m.xoanon.commander.api.XCTestState.INITIAL;
import static com.io7m.xoanon.commander.api.XCTestState.RUNNING;
import static com.io7m.xoanon.commander.api.XCTestState.SUCCEEDED;
import static java.time.OffsetDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A small demo application that creates a lot of fake tests.
 */

public final class DemoMain
{
  private DemoMain()
  {

  }

  /**
   * A small demo application that creates a lot of fake tests.
   *
   * @param args The command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    final var rng =
      SecureRandom.getInstanceStrong();
    final var root =
      Path.of(URI.create("jrt:/"));

    final List<String> names;
    try (var stream = Files.list(root)) {
      names = stream.sorted()
        .map(Path::toString)
        .toList();
    }

    try (var cmd = XCommanders.boot().get(10L, SECONDS)) {
      Thread.sleep(1_000);

      cmd.setApplicationInfo(
        new XCApplicationInfo(
          "com.io7m.xoanon",
          XBVersion.MAIN_VERSION,
          XBVersion.MAIN_BUILD
        )
      );

      cmd.keyMap().get(30L, SECONDS);
      cmd.setTestCount(names.size());

      for (final var name : names) {
        cmd.setTestState(new XCTestInfo(now(), name, INITIAL));
        Thread.sleep(100L);
        cmd.setTestState(new XCTestInfo(now(), name, RUNNING));
        Thread.sleep(250L);

        if (rng.nextBoolean()) {
          cmd.setTestState(new XCTestInfo(now(), name, FAILED));
        } else {
          cmd.setTestState(new XCTestInfo(now(), name, SUCCEEDED));
        }
      }

      Thread.sleep(1_000L);
    }
    Platform.exit();
  }
}
