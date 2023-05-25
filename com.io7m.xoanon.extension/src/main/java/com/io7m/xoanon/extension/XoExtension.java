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

package com.io7m.xoanon.extension;

import com.io7m.xoanon.commander.XCommanders;
import com.io7m.xoanon.commander.api.XCApplicationInfo;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCFXThread;
import com.io7m.xoanon.commander.api.XCKeyMap;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.commander.api.XCTestInfo;
import com.io7m.xoanon.commander.api.XCTestState;
import javafx.application.Platform;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.io7m.xoanon.commander.api.XCTestState.FAILED;
import static com.io7m.xoanon.commander.api.XCTestState.INITIAL;
import static com.io7m.xoanon.commander.api.XCTestState.SUCCEEDED;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A simple JavaFX extension for JUnit 5 tests.
 */

public final class XoExtension
  implements BeforeAllCallback,
  BeforeEachCallback,
  AfterEachCallback,
  ParameterResolver,
  TestWatcher,
  LauncherSessionListener,
  TestExecutionListener
{
  private static final AtomicBoolean FX_PLATFORM_STARTED =
    new AtomicBoolean(false);

  private static XCCommanderType COMMANDER;

  private static final Logger LOG =
    LoggerFactory.getLogger(XoExtension.class);

  private static final ArrayList<TestIdentifier> TESTS_EXPECTED =
    new ArrayList<>();

  /**
   * A simple JavaFX extension for JUnit 5 tests.
   */

  public XoExtension()
  {

  }

  /**
   * Set the application being tested.
   *
   * @param info The info
   */

  public static void setApplicationInfo(
    final XCApplicationInfo info)
  {
    COMMANDER.setApplicationInfo(info);
  }

  @Override
  public void launcherSessionOpened(
    final LauncherSession session)
  {
    /*
     * Register a test execution listener so that the commander can be
     * shut down exactly once after all the tests have executed.
     */

    session.getLauncher()
      .registerTestExecutionListeners(this);
  }

  @Override
  public void testPlanExecutionStarted(
    final TestPlan testPlan)
  {
    testPlan.countTestIdentifiers(p -> {
      return switch (p.getType()) {
        case CONTAINER -> {
          yield false;
        }
        case TEST, CONTAINER_AND_TEST -> {
          TESTS_EXPECTED.add(p);
          yield true;
        }
      };
    });
  }

  @Override
  public void testPlanExecutionFinished(
    final TestPlan testPlan)
  {
    try {
      if (COMMANDER != null) {
        COMMANDER.close();
      }
    } catch (final Exception e) {
      LOG.error("close: ", e);
    }
  }

  @Override
  public void beforeAll(
    final ExtensionContext context)
    throws Exception
  {
    if (FX_PLATFORM_STARTED.compareAndSet(false, true)) {
      LOG.trace("starting JavaFX platform");
      Platform.setImplicitExit(false);
      COMMANDER = XCommanders.boot().get(30L, SECONDS);
      Thread.sleep(2_000L);

      TESTS_EXPECTED.forEach(identifier -> {
        COMMANDER.setTestState(new XCTestInfo(
          OffsetDateTime.now(),
          identifier.getUniqueId(),
          identifier.getDisplayName(),
          INITIAL
        ));
      });
    }
  }

  @Override
  public boolean supportsParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    return Objects.equals(requiredType, XCCommanderType.class)
      || Objects.equals(requiredType, XCKeyMap.class)
      || Objects.equals(requiredType, XCRobotType.class);
  }

  @Override
  public void dynamicTestRegistered(
    final TestIdentifier testIdentifier)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        testIdentifier.getUniqueId(),
        testIdentifier.getDisplayName(),
        INITIAL
      )
    );
  }

  @Override
  public void executionStarted(
    final TestIdentifier testIdentifier)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        testIdentifier.getUniqueId(),
        testIdentifier.getDisplayName(),
        XCTestState.RUNNING
      )
    );
  }

  @Override
  public void executionFinished(
    final TestIdentifier testIdentifier,
    final TestExecutionResult testExecutionResult)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        testIdentifier.getUniqueId(),
        testIdentifier.getDisplayName(),
        switch (testExecutionResult.getStatus()) {
          case FAILED -> FAILED;
          case ABORTED -> FAILED;
          case SUCCESSFUL -> SUCCEEDED;
        }
      )
    );
  }

  @Override
  public Object resolveParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final var requiredType =
      parameterContext.getParameter().getType();

    if (Objects.equals(requiredType, XCCommanderType.class)) {
      try {
        return COMMANDER;
      } catch (final Exception e) {
        throw new ParameterResolutionException(e.getMessage(), e);
      }
    }

    if (Objects.equals(requiredType, XCKeyMap.class)) {
      try {
        return COMMANDER.keyMap().get(30L, SECONDS);
      } catch (final Exception e) {
        throw new ParameterResolutionException(e.getMessage(), e);
      }
    }

    if (Objects.equals(requiredType, XCRobotType.class)) {
      try {
        return COMMANDER.robot().get(30L, SECONDS);
      } catch (final Exception e) {
        throw new ParameterResolutionException(e.getMessage(), e);
      }
    }

    throw new ParameterResolutionException(
      "Unrecognized requested parameter type: %s".formatted(requiredType)
    );
  }

  @Override
  public void afterEach(
    final ExtensionContext context)
    throws Exception
  {
    /*
     * It's possible for tests to leave the current key and mouse state
     * in a mess. Explicitly reset both the mouse and all keys.
     */

    try {
      final var bot = COMMANDER.robot().get(5L, SECONDS);
      bot.reset(Optional.of(COMMANDER.stage()));
    } catch (final Exception e) {
      LOG.error("error resetting input: ", e);
    }

    /*
     * Close any and all stages the test may have opened.
     */

    COMMANDER.stageCloseAll()
      .get(5L, SECONDS);
  }

  @Override
  public void testDisabled(
    final ExtensionContext context,
    final Optional<String> reason)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        context.getUniqueId(),
        context.getDisplayName(),
        SUCCEEDED
      )
    );
  }

  @Override
  public void testSuccessful(
    final ExtensionContext context)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        context.getUniqueId(),
        context.getDisplayName(),
        SUCCEEDED
      )
    );
  }

  @Override
  public void testAborted(
    final ExtensionContext context,
    final Throwable cause)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        context.getUniqueId(),
        context.getDisplayName(),
        FAILED
      )
    );
  }

  @Override
  public void testFailed(
    final ExtensionContext context,
    final Throwable cause)
  {
    if (COMMANDER == null) {
      return;
    }

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        context.getUniqueId(),
        context.getDisplayName(),
        FAILED
      )
    );
  }

  @Override
  public void beforeEach(
    final ExtensionContext context)
    throws Exception
  {
    if (COMMANDER == null) {
      return;
    }

    XCFXThread.runVWait(1L, SECONDS, () -> {
      COMMANDER.stage().toBack();
    });

    COMMANDER.setTestState(
      new XCTestInfo(
        OffsetDateTime.now(),
        context.getUniqueId(),
        context.getDisplayName(),
        XCTestState.RUNNING
      )
    );
  }
}
