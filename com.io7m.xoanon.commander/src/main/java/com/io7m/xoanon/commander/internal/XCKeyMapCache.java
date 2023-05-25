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

package com.io7m.xoanon.commander.internal;

import com.io7m.xoanon.commander.api.XCKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * A simple cache for key maps.
 */

public final class XCKeyMapCache
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XCKeyMapCache.class);

  private static final StandardOpenOption[] FILE_OPTIONS = {
    WRITE, CREATE, TRUNCATE_EXISTING,
  };

  private final Path temporaryDirectory;
  private final Clock clock;

  /**
   * A simple cache for key maps.
   *
   * @param inClock The clock
   * @param inDirectory The temporary directory
   */

  public XCKeyMapCache(
    final Clock inClock,
    final Path inDirectory)
  {
    this.clock =
      Objects.requireNonNull(inClock, "inClock");
    this.temporaryDirectory =
      Objects.requireNonNull(inDirectory, "temporaryDirectory");
  }

  /**
   * Load a cached key map, if one exists.
   *
   * @return The cached map
   */

  public Optional<XCKeyMap> load()
  {
    try {
      final var xoanonDir =
        Files.createDirectories(this.temporaryDirectory.resolve("xoanon"));
      final var keyMapFile =
        xoanonDir.resolve("keymap.bin");

      final var time =
        Files.getLastModifiedTime(keyMapFile);
      final var timeOldest =
        Instant.now(this.clock).minus(1L, ChronoUnit.HOURS);
      final var timeOldestAcceptable =
        FileTime.from(timeOldest);

      if (time.compareTo(timeOldestAcceptable) < 0) {
        LOG.info(
          "keymap cache file {} is older than {}, ignoring it",
          keyMapFile,
          time);
        return Optional.empty();
      }

      try (var input = Files.newInputStream(keyMapFile)) {
        try (var objectInput = new ObjectInputStream(input)) {
          final var map = (XCKeyMap) objectInput.readObject();
          final var keyCount = map.keys().size();
          if (keyCount < 88) {
            LOG.info(
              "keymap cache file {} only contains {} keys; ignoring it",
              keyMapFile,
              Integer.valueOf(keyCount)
            );
            return Optional.empty();
          }

          LOG.info(
            "loaded keymap cache from {} ({} keys)",
            keyMapFile,
            Integer.valueOf(keyCount)
          );
          return Optional.of(map);
        }
      }
    } catch (final Exception e) {
      LOG.debug("failed to read keymap cache file: ", e);
      return Optional.empty();
    }
  }

  /**
   * Save the key map to the cache.
   *
   * @param keyMap The key map
   */

  public void save(
    final XCKeyMap keyMap)
  {
    Objects.requireNonNull(keyMap, "keyMap");

    try {
      final var xoanonDir =
        Files.createDirectories(this.temporaryDirectory.resolve("xoanon"));
      final var keyMapFile =
        xoanonDir.resolve("keymap.bin");

      try (var output = Files.newOutputStream(keyMapFile, FILE_OPTIONS)) {
        try (var objectOutput = new ObjectOutputStream(output)) {
          objectOutput.writeObject(keyMap);
          objectOutput.flush();
        }
      }

      LOG.info("wrote keymap cache to {}", keyMapFile);
    } catch (final Exception e) {
      LOG.debug("failed to write keymap cache file: ", e);
    }
  }
}
