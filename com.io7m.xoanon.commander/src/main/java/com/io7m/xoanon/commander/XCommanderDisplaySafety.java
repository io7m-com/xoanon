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


package com.io7m.xoanon.commander;

import com.io7m.xoanon.commander.internal.XBStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Functions to determine display "safety". Most test suites will take over
 * the keyboard, mouse, and display, for extended periods. This can be mildly
 * disastrous if the user is not expecting it.
 *
 * @see "https://github.com/io7m-com/xoanon/issues/19"
 */

public final class XCommanderDisplaySafety
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XCommanderDisplaySafety.class);

  private static final Pattern LOCAL_DISPLAY_PATTERN =
    Pattern.compile("(.*):0(\\.[0-9]+)?");

  private XCommanderDisplaySafety()
  {

  }

  /**
   * @param environment The environment
   *
   * @return {@code true} if running on the current display is permitted
   */

  public static boolean isDisplayPermitted(
    final Map<String, String> environment)
  {
    final var display =
      environment.get("DISPLAY");

    LOG.debug("DISPLAY environment variable: {}", display);
    if (display == null) {
      LOG.debug("Cannot determine if the display is local; assuming that it is!");
      return false;
    }

    final var matcher = LOCAL_DISPLAY_PATTERN.matcher(display);
    if (!matcher.matches()) {
      LOG.debug("DISPLAY does not match {}", LOCAL_DISPLAY_PATTERN);
      LOG.debug("Assuming a non-local display.");
      return true;
    }

    final var hostname =
      matcher.group(1);
    final var screen =
      matcher.group(2);

    LOG.debug("DISPLAY hostname: {}", hostname);
    LOG.debug("DISPLAY screen:   {}", screen);

    return switch (hostname) {
      case "localhost", "" -> {
        LOG.debug("Display appears to be local.");
        yield isDisplayPermittedOverride(environment);
      }
      default -> {
        LOG.debug("Non-localhost hostname.");
        LOG.debug("Assuming a non-local display.");
        yield true;
      }
    };
  }

  /**
   * Check that tests are permitted on the current display.
   *
   * @param environment The environment
   */

  public static void checkDisplayPermitted(
    final Map<String, String> environment)
  {
    if (!isDisplayPermitted(environment)) {
      try {
        final var strings = new XBStrings(Locale.getDefault());
        throw new UnsupportedOperationException(
          strings.format("displaySafetyError")
        );
      } catch (final IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static boolean isDisplayPermittedOverride(
    final Map<String, String> environment)
  {
    final var displayOverride =
      environment.get("XOANON_REALLY_USE_LOCAL_DISPLAY");

    if (displayOverride == null) {
      return false;
    }

    return "true".equals(displayOverride);
  }
}
