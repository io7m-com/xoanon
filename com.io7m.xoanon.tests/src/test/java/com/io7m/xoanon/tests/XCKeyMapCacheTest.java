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


package com.io7m.xoanon.tests;

import com.io7m.xoanon.commander.api.XCKey;
import com.io7m.xoanon.commander.api.XCKeyMap;
import com.io7m.xoanon.commander.internal.XCKeyMapCache;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class XCKeyMapCacheTest
{
  private Path directory;
  private XCFakeClock clock;
  private Path cacheFile;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.clock =
      new XCFakeClock();
    this.directory =
      XCTestDirectories.createTempDirectory();
    this.cacheFile =
      this.directory
        .resolve("xoanon")
        .resolve("keymap.bin");
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    XCTestDirectories.deleteDirectory(this.directory);
  }

  private static XCKeyMap bigMap()
  {
    return  new XCKeyMap(
      IntStream.range(10, 300)
        .mapToObj(x -> Map.entry((char) x, new XCKey(KeyCode.T,false,false,false)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  @Test
  public void testCacheTooOld()
    throws IOException
  {
    final var cache =
      new XCKeyMapCache(this.clock, this.directory);

    final Instant timeNow =
      Instant.parse("2000-01-01T00:00:00+00:00");
    final Instant timeThen =
      timeNow.minus(24L, ChronoUnit.HOURS);

    this.clock.setTime(timeNow);

    cache.save(bigMap());
    Files.setLastModifiedTime(this.cacheFile, FileTime.from(timeThen));
    assertEquals(Optional.empty(), cache.load());
  }

  @Test
  public void testCacheTooSmall()
    throws IOException
  {
    final var cache =
      new XCKeyMapCache(this.clock, this.directory);

    final Instant timeNow =
      Instant.parse("2000-01-01T00:00:00+00:00");

    this.clock.setTime(timeNow);

    cache.save(new XCKeyMap(Map.of()));
    Files.setLastModifiedTime(this.cacheFile, FileTime.from(timeNow));
    assertEquals(Optional.empty(), cache.load());
  }

  @Test
  public void testCacheBroken()
    throws IOException
  {
    final var cache =
      new XCKeyMapCache(this.clock, this.directory);

    final Instant timeNow =
      Instant.parse("2000-01-01T00:00:00+00:00");

    this.clock.setTime(timeNow);

    Files.createDirectories(this.cacheFile.getParent());
    Files.writeString(this.cacheFile, "Not a keymap.", CREATE, WRITE);
    Files.setLastModifiedTime(this.cacheFile, FileTime.from(timeNow));
    assertEquals(Optional.empty(), cache.load());
  }

  @Test
  public void testCacheGood()
    throws IOException
  {
    final var cache =
      new XCKeyMapCache(this.clock, this.directory);

    final Instant timeNow =
      Instant.parse("2000-01-01T00:00:00+00:00");

    this.clock.setTime(timeNow);

    final var map = bigMap();
    cache.save(map);
    Files.setLastModifiedTime(this.cacheFile, FileTime.from(timeNow));
    assertEquals(Optional.of(map), cache.load());
  }
}
