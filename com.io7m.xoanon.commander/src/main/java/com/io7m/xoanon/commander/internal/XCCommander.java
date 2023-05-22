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

import com.io7m.xoanon.commander.XBVersion;
import com.io7m.xoanon.commander.api.XCApplicationInfo;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCTestInfo;
import com.io7m.xoanon.commander.api.XCTestState;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.commander.api.XCKey;
import com.io7m.xoanon.commander.api.XCKeyMap;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.animation.Interpolator.LINEAR;
import static javafx.scene.input.KeyCode.SHIFT;

/**
 * The main commander.
 */

public final class XCCommander
  implements XCCommanderType, Initializable
{
  private static final KeyCode[] ALL_KEY_CODES =
    KeyCode.values();

  private static final Logger LOG =
    LoggerFactory.getLogger(XCCommander.class);

  private final ScheduledExecutorService executor;
  private final XBStrings strings;
  private final Stage stage;
  private final ObservableList<XCTestInfo> testsList;
  private final AtomicReference<XCKeyMap> keyMap;
  private final Robot baseRobot;
  private final AtomicBoolean testsStarted;
  private final OffsetDateTime timeStarted;
  private final ConcurrentLinkedQueue<Stage> stagesCreated;
  private volatile int stagesCreatedCount;
  private volatile int stagesReleasedCount;
  private volatile XCTestState testsStateWorst;
  private volatile long testsTotal;
  private volatile long testsIndex;
  private volatile long testsFailed;

  @FXML private TextArea input;
  @FXML private TextField status;
  @FXML private Parent splash;
  @FXML private Parent diagnostics;
  @FXML private ListView<XCTestInfo> tests;
  @FXML private Pane info;
  @FXML private ProgressBar progress;
  @FXML private Label testVersion;
  @FXML private Label statusName;
  @FXML private Rectangle statusLight;
  @FXML private Pane testsInfoContainer;

  @FXML private TextField dataApp;
  @FXML private TextField dataCommit;
  @FXML private TextField dataDuration;
  @FXML private TextField dataHost;
  @FXML private TextField dataOS;
  @FXML private TextField dataRuntime;
  @FXML private TextField dataStarted;
  @FXML private TextField dataVersion;
  @FXML private TextField dataStagesCreated;
  @FXML private TextField dataStagesReleased;
  @FXML private TextField dataTestsExpected;
  @FXML private TextField dataTestsExecuted;
  @FXML private TextField dataTestsFailed;
  @FXML private TextField dataExecutionId;

  @FXML private Label heapText;
  @FXML private ProgressBar heapUsed;

  /**
   * Construct a commander.
   *
   * @param inStrings The strings
   * @param inStage   The stage hosting the commander
   */

  public XCCommander(
    final XBStrings inStrings,
    final Stage inStage)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.stage =
      Objects.requireNonNull(inStage, "stage");

    this.testsStateWorst =
      XCTestState.INITIAL;
    this.testsStarted =
      new AtomicBoolean(false);
    this.timeStarted =
      OffsetDateTime.now();

    this.executor =
      Executors.newSingleThreadScheduledExecutor(runnable -> {
        final var thread = new Thread(runnable);
        thread.setName(
          "com.io7m.xoanon.commander[%d]"
            .formatted(Long.valueOf(thread.getId())));
        thread.setDaemon(true);
        return thread;
      });

    this.testsList =
      FXCollections.observableArrayList();
    this.keyMap =
      new AtomicReference<>();
    this.baseRobot =
      new Robot();

    this.stagesCreatedCount = 0;
    this.stagesReleasedCount = 0;

    this.stagesCreated =
      new ConcurrentLinkedQueue<Stage>();
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    try {
      this.dataOS.setText(
        String.format(
          "%s %s %s",
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("os.arch"))
      );
    } catch (final Exception e) {
      LOG.debug("", e);
    }

    try {
      this.dataRuntime.setText(
        String.format(
          "%s %s",
          System.getProperty("java.vendor"),
          System.getProperty("java.version")
        )
      );
    } catch (final Exception e) {
      LOG.debug("", e);
    }

    try {
      this.dataStarted.setText(
        OffsetDateTime.now(ZoneId.of("UTC")).toString()
      );
    } catch (final Exception e) {
      LOG.debug("", e);
    }

    try {
      this.dataHost.setText(
        InetAddress.getLocalHost().getHostName()
      );
    } catch (final Exception e) {
      LOG.debug("", e);
    }

    this.dataStagesCreated.setText("0");
    this.dataStagesReleased.setText("0");
    this.dataTestsExecuted.setText("0");
    this.dataTestsFailed.setText("0");
    this.dataTestsExpected.setText("0");

    final var execId = UUID.randomUUID();
    this.dataExecutionId.setText(execId.toString());
    LOG.info("execution ID: {}", execId);

    this.testVersion.setText(
      "Xoanon Test Harness %s".formatted(XBVersion.MAIN_VERSION)
    );

    this.splash.setFocusTraversable(false);
    this.splash.setMouseTransparent(true);

    this.diagnosticsLock();

    this.status.setMouseTransparent(true);
    this.status.setFocusTraversable(false);

    this.tests.setMouseTransparent(true);
    this.tests.setFocusTraversable(false);

    this.tests.setFixedCellSize(16.0);
    this.tests.setCellFactory(new XBTestCellFactory(this.strings));
    this.tests.setItems(this.testsList);

    this.status.setText("Waiting...");
    this.statusName.setText(this.testsStateWorst.name());

    this.splash.setVisible(true);
    this.executor.schedule(
      () -> Platform.runLater(this::splashHide),
      1L,
      TimeUnit.SECONDS
    );

    this.executor.scheduleAtFixedRate(
      this::updateHeap, 0L, 1L, TimeUnit.SECONDS);
  }

  private void updateHeap()
  {
    final var runtime =
      Runtime.getRuntime();
    final var used =
      runtime.totalMemory() - runtime.freeMemory();
    final var max =
      runtime.totalMemory();

    final var usedProp =
      (double) used / (double) max;

    Platform.runLater(() -> {
      this.heapText.setText(
        String.format(
          "Heap: Used: %s Max: %s",
          Long.toUnsignedString(used),
          Long.toUnsignedString(max)
        ));
      this.heapUsed.setProgress(usedProp);
    });
  }

  private void splashHide()
  {
    final var fade = new FadeTransition(Duration.millis(500L));
    fade.setNode(this.splash);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setInterpolator(LINEAR);
    fade.playFromStart();
    fade.setOnFinished(event -> this.splash.setVisible(false));
  }

  private void splashShow(
    final Runnable onFinished)
  {
    this.splash.setVisible(true);

    final var pause = new FadeTransition(Duration.millis(1000L));
    pause.setNode(this.splash);
    pause.setFromValue(1.0);
    pause.setToValue(1.0);
    pause.setInterpolator(LINEAR);
    pause.setOnFinished(event -> onFinished.run());

    final var fade = new FadeTransition(Duration.millis(500L));
    fade.setNode(this.splash);
    fade.setFromValue(0.0);
    fade.setToValue(1.0);
    fade.setInterpolator(LINEAR);
    fade.setOnFinished(event -> pause.playFromStart());
    fade.playFromStart();
  }

  @Override
  public void close()
    throws Exception
  {
    final var future = new CompletableFuture<Void>();
    this.executor.schedule(() -> {
      try {
        future.complete(this.shutDown());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    }, 1L, TimeUnit.SECONDS);
    future.get(10L, TimeUnit.SECONDS);
  }

  private Void shutDown()
    throws Exception
  {
    Platform.runLater(() -> {
      this.status.setText("Shutting down...");
    });

    final var closeLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      this.splashShow(closeLatch::countDown);
    });
    closeLatch.await(30L, TimeUnit.SECONDS);

    this.executor.shutdown();
    return null;
  }

  @Override
  public void setTestState(
    final XCTestInfo test)
  {
    Objects.requireNonNull(test, "test");

    if (this.testsStarted.compareAndSet(false, true)) {
      this.executor.scheduleAtFixedRate(() -> {
        Platform.runLater(() -> {
          this.dataDuration.setText(
            java.time.Duration.between(
              this.timeStarted,
              OffsetDateTime.now()
            ).toString()
          );
        });
      }, 0L, 1L, TimeUnit.SECONDS);
    }

    Platform.runLater(() -> {
      switch (test.state()) {
        case FAILED -> {
          ++this.testsFailed;
        }
        case RUNNING -> {
          ++this.testsIndex;
        }
        case INITIAL, SUCCEEDED -> {

        }
      }

      this.testCountDisplaysUpdate();
      this.status.setText("%s %s".formatted(test.name(), test.state()));

      switch (this.testsStateWorst) {
        case INITIAL, RUNNING, SUCCEEDED -> {
          this.testsStateWorst = test.state();
          this.statusLight.setFill(XBTestColors.colorForTest(test));
          this.statusName.setText(test.state().name());
        }
        case FAILED -> {

        }
      }
    });

    Platform.runLater(() -> {
      final var newList =
        this.tests.getItems()
          .stream()
          .filter(t -> !Objects.equals(t.name(), test.name()))
          .collect(Collectors.toCollection(ArrayList::new));

      newList.add(test);
      newList.sort(Comparator.comparing(XCTestInfo::time).reversed());
      this.testsList.setAll(newList);
    });
  }

  @Override
  public void setTestCount(
    final long count)
  {
    Platform.runLater(() -> {
      this.testsTotal = count;
      this.testCountDisplaysUpdate();
    });
  }

  @Override
  public CompletableFuture<XCKeyMap> keyMap()
  {
    final var existing = this.keyMap.get();
    if (existing != null) {
      return CompletableFuture.completedFuture(existing);
    }

    final var future = new CompletableFuture<XCKeyMap>();
    this.executor.execute(() -> {
      try {
        future.complete(this.generateKeyMap());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  @Override
  public void sendToBack()
  {
    Platform.runLater(this.stage::toBack);
  }

  @Override
  public CompletableFuture<XCRobotType> robot()
  {
    return this.keyMap()
      .thenApply(k -> new XCRobot(k, this.baseRobot));
  }

  @Override
  public CompletableFuture<Stage> stageNew(
    final Consumer<Stage> onCreate)
  {
    final var stageFuture =
      XoFXThread.run(() -> {
        final var newStage = new Stage();
        newStage.setMinWidth(16.0);
        newStage.setMinHeight(16.0);
        newStage.setMaxWidth(3000.0);
        newStage.setMaxHeight(3000.0);
        newStage.setWidth(320.0);
        newStage.setHeight(240.0);
        newStage.show();
        newStage.toFront();

        ++this.stagesCreatedCount;
        this.dataStagesCreated.setText(
          Integer.toString(this.stagesCreatedCount)
        );
        this.stagesCreated.add(newStage);

        onCreate.accept(newStage);
        return newStage;
      });

    /*
     * Fetch the stage after a short delay. This gives time for the stage
     * to fully open and display any configured scene.
     */

    final var future = new CompletableFuture<Stage>();
    this.executor.schedule(() -> {
      try {
        future.complete(stageFuture.get());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    }, 100L, TimeUnit.MILLISECONDS);
    return future;
  }

  @Override
  public CompletableFuture<Void> stageCloseAll()
  {
    return XoFXThread.run(() -> {
      final var windows = List.copyOf(this.stagesCreated);
      this.stagesCreated.clear();
      this.stagesReleasedCount += windows.size();
      this.dataStagesReleased.setText(Integer.toString(this.stagesReleasedCount));

      for (final var window : windows) {
        try {
          window.close();
        } catch (final Throwable e) {
          LOG.error("close: {} ({}): ", window, window.getTitle(), e);
        }
      }
      return null;
    });
  }

  @Override
  public void setApplicationInfo(
    final XCApplicationInfo appInfo)
  {
    Platform.runLater(() -> {
      this.dataApp.setText(appInfo.name());
      this.dataVersion.setText(appInfo.version());
      this.dataCommit.setText(appInfo.build());
    });
  }

  private static boolean isAllowedKeyCode(
    final KeyCode code)
  {
    if (code.isLetterKey()) {
      return true;
    }
    if (code.isDigitKey()) {
      return true;
    }

    return switch (code) {
      case AMPERSAND,
        QUOTEDBL,
        QUOTE,
        POUND,
        PLUS,
        OPEN_BRACKET,
        MINUS,
        EXCLAMATION_MARK,
        DOLLAR,
        CLOSE_BRACKET,
        BRACERIGHT,
        BRACELEFT,
        BACK_QUOTE,
        ASTERISK -> {
        yield true;
      }
      default -> false;
    };
  }

  private void testCountDisplaysUpdate()
  {
    this.progress.setProgress(
      (double) this.testsIndex / (double) this.testsTotal
    );
    this.dataTestsExpected.setText(
      Long.toUnsignedString(this.testsTotal)
    );
    this.dataTestsExecuted.setText(
      Long.toUnsignedString(this.testsIndex)
    );
    this.dataTestsFailed.setText(
      Long.toUnsignedString(this.testsFailed)
    );
  }

  /**
   * Generate a keymap by pressing every non-special key on the keyboard
   * and recording what happens. The mapping can then be used to work backwards
   * from characters to keys when attempting to send key events to components.
   *
   * @return A keymap
   *
   * @throws Exception On errors
   */

  private XCKeyMap generateKeyMap()
    throws Exception
  {
    try {
      Platform.runLater(() -> {
        this.status.setText("Generating keymap...");
      });

      Platform.runLater(this::diagnosticsUnlock);

      final var candidateCodes =
        Arrays.stream(ALL_KEY_CODES)
          .filter(XCCommander::isAllowedKeyCode)
          .toList();

      final var newMappings =
        new ConcurrentHashMap<Character, XCKey>();

      final var index = new AtomicInteger(1);
      final var count = candidateCodes.size();

      for (final var code : candidateCodes) {
        Platform.runLater(() -> {
          this.progress.setProgress(
            index.doubleValue() / (double) count
          );
        });

        Platform.runLater(() -> {
          LOG.trace("check {}", code);
          this.status.setText("Generating keymap: Checking text for %s".formatted(
            code));

          final var bounds =
            this.input.localToScreen(this.input.getBoundsInLocal());
          final var target =
            new Point2D(bounds.getCenterX(), bounds.getCenterY());

          this.baseRobot.mouseMove(target);
          this.baseRobot.mouseClick(MouseButton.PRIMARY);
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.input.clear();
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.baseRobot.keyType(code);
        });

        pause();

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          final var text = this.input.getText();
          LOG.trace("code {} -> '{}'", code, text);
          if (text.isEmpty()) {
            return;
          }
          final var characters = text.toCharArray();
          final var character = characters[0];

          newMappings.put(
            Character.valueOf(character),
            new XCKey(code, false, false, false)
          );
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.input.clear();
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.baseRobot.keyPress(SHIFT);
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.baseRobot.keyType(code);
        });

        pause();

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          this.baseRobot.keyRelease(SHIFT);
        });

        Platform.requestNextPulse();
        Platform.runLater(() -> {
          final var text = this.input.getText();
          LOG.trace("SHIFT code {} -> '{}'", code, text);
          if (text.isEmpty()) {
            return;
          }
          final var characters = text.toCharArray();
          final var character = characters[0];

          newMappings.put(
            Character.valueOf(character),
            new XCKey(code, true, false, false)
          );
        });

        Platform.requestNextPulse();
        Platform.runLater(index::incrementAndGet);
      }

      Platform.requestNextPulse();
      Platform.runLater(() -> {
        this.status.setText("Generated keymap.");
      });

      final var latch = new CountDownLatch(1);
      Platform.runLater(latch::countDown);
      latch.await();

      LOG.debug(
        "Generated key map of size {}",
        Integer.valueOf(newMappings.size()));
      final var result = new XCKeyMap(Map.copyOf(newMappings));
      this.keyMap.set(result);
      return result;
    } catch (final Throwable e) {
      Platform.runLater(this::diagnosticsLock);
      throw e;
    } finally {
      Platform.requestNextPulse();
      this.releaseAllKeys();
    }
  }

  private static void pause()
  {
    try {
      Thread.sleep(1L * 16L);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void releaseAllKeys()
  {
    for (final var code : ALL_KEY_CODES) {
      Platform.runLater(() -> this.baseRobot.keyRelease(code));
      Platform.requestNextPulse();
    }
  }

  private void diagnosticsUnlock()
  {
    this.input.setFocusTraversable(true);
    this.input.setMouseTransparent(false);
  }

  private void diagnosticsLock()
  {
    this.input.setFocusTraversable(false);
    this.input.setMouseTransparent(true);
  }
}
