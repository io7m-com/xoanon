xoanon
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.xoanon/com.io7m.xoanon.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.xoanon%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/com.io7m.xoanon/com.io7m.xoanon.svg?style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/xoanon/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m/xoanon.svg?style=flat-square)](https://codecov.io/gh/io7m/xoanon)

![xoanon](./src/site/resources/xoanon.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/xoanon/main.linux.temurin.current.yml)](https://github.com/io7m/xoanon/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/xoanon/main.linux.temurin.lts.yml)](https://github.com/io7m/xoanon/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/xoanon/main.windows.temurin.current.yml)](https://github.com/io7m/xoanon/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/xoanon/main.windows.temurin.lts.yml)](https://github.com/io7m/xoanon/actions?query=workflow%3Amain.windows.temurin.lts)|

## xoanon

A [Junit 5](https://junit.org/junit5/) extension designed for executing UI
tests for [JavaFX](https://openjfx.io/) applications and libraries.

### Features

  * Fullscreen test reporting UI.
  * Fast, reliable, convenient test robot interface.
  * Designed for use in continuous integration.
  * Automatic keyboard map generation for system-independent test execution.
  * Slow motion mode for assistance with debugging failing tests.
  * Written in pure Java 17.
  * [OSGi](https://www.osgi.org/) ready.
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready.
  * ISC license.
  * High-coverage automated test suite.


### Usage

Annotate tests classes with `@ExtendWith(XoExtension.class)`. This will
allow for the injection of the [commander](#commander) interface, the
[robot](#robot) interface, and the [keymap](#keymap-generation) interface.


```
@ExtendWith(XoExtension.class)
public final class ExampleTest
{
  @Test
  public void testButton(
    final XCCommanderType commander,
    final XCRobotType bot)
    throws Exception
  {
    ...
  }
}
```

#### Commander

The _commander_ is the main service used in the `xoanon` package. It is
started at the beginning of the execution of the entire test suite, and shut
down when the entire test suite completes. The _commander_ opens a maximized
window that is kept in the background during the entire test run:

![Commander](./src/site/resources/commander.png?raw=true)

The _commander_ provides various services to the tests being executed such
as cleaning up any `Stage`s that have been opened for each test, generating
a [keymap](#keymap-generation), and displaying real-time statistics of the
test suite execution in a manner designed to be captured by screen-capturing
software during the test runs.

#### Robot

The `xoanon` package provides a wrapper around the standard JavaFX
[Robot](https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/robot/Robot.html)
interface. The `xoanon` `XCRobotType` interface provides extra services
such as:

  * Looking up any node in any stage by `id`, CSS `class`, or exact text content.
  * Synchronous operations that wait for operations performed on the JavaFX
    UI thread to complete, and can be called from any thread.
  * Entry of text into components using strings as input, instead of having
    to submit raw `KeyCode` values.

##### Pauses

Unfortunately, JavaFX and (to a much greater extent) the underlying
platform-specific APIs used to display user interfaces are fraught with
typically-harmless race conditions. Typically, operations are pushed onto the
JavaFX UI thread, and this thread makes calls into whatever is the appropriate
UI library on the current platform (GTK, Windows, etc). The platform's UI
libraries will often have event handling threads of their own. While none of
this is a problem for interactions that occur at human speeds, UI automation
libraries tend to be able to perform operations faster than any human
realistically could. This tends to expose lots of very minor race conditions in
the underlying platform libraries, and tends to make UI tests inherently
timing-sensitive and prone to failure when operations are executed at superhuman
speeds.

The `xoanon` package works around this fragility by injecting small configurable
pauses into the operations it performs in order to avoid overwhelming and/or
breaking the underlying user interface library code.

The pause values can be configured separately for keyboard and mouse operations,
and the defaults are typically fine for most projects.

See the `XCRobotConfigurationType` interface for details.

##### Finding Nodes

The `XCRobotType` interface provides methods to find existing nodes in the
JavaFX scene graph. For example:

```
@Test
public void testFindWithTextInAnyStage(
  final XCRobotType bot,
  final XCCommanderType commander)
  throws Exception
{
  final var text = new AtomicReference<Label>();
  commander.stageNewAndWait(newStage -> {
    final var field = new Label();
    text.set(field);
    field.setText("ABCDEFGH");
    newStage.setScene(new Scene(field));
  });

  final var node = bot.findWithTextInAnyStage("ABCDEFGH");
  assertEquals(text.get(), node);
}
```

The above code creates a new `Stage`, adds a text field to it, and then
tries to find the text field by its `ABCDEFGH` text content. Similar methods
exist for finding nodes by ID, and many methods allow for casting the resulting
node directly to the expected type:

```
@Test
public void testFindCheckboxById(
  final XCRobotType bot,
  final XCCommanderType commander)
  throws Exception
{
  final var checkRef =
    new AtomicReference<CheckBox>();

  final var stage =
    commander.stageNewAndWait(newStage -> {
      final var checkBox = new CheckBox();
      checkRef.set(checkBox);
      checkBox.setSelected(false);
      checkBox.setId("x");
      newStage.setScene(new Scene(checkBox));
    });

  final CheckBox check = bot.findWithId(CheckBox.class, stage, "x");
  assertEquals(checkRef.get(), check);
}
```

Failing to locate a node results in a `NoSuchElementException` exception
being thrown in the context of the calling thread.

```
@Test
public void testTextFieldTextFindNonexistentText0(
  final XCCommanderType commander,
  final XCRobotType bot)
  throws Exception
{
  final var stage =
    commander.stageNewAndWait(newStage -> {

    });

  final var ex =
    assertThrows(ExecutionException.class, () -> {
      bot.findWithText(stage, "Clearly does not exist.");
    });

  assertInstanceOf(NoSuchElementException.class, ex.getCause());
}
```

##### Synchronous Execution

Most methods on the `XCRobotType` interface execute synchronously with
respect to the JavaFX UI thread. That is, the robot submits code to run on
the UI thread and then waits on the calling thread for the operation to
complete. This ensures that the JavaFX UI thread is not blocked by the
robot waiting and ensures that test code can be written in a straightforward
synchronous style. For example:


```
@Test
public void testRobot(
  final XCRobotType robot)
  throws Exception
{
  robot.execute(() -> ...);
  final var x = robot.evaluate(() -> ...);
  robot.execute(() -> ...);
}
```

The first call to `robot.execute()` executes the given lambda expression on
the JavaFX UI thread, and execution does not progress to the `robot.evaluate()`
call until the UI thread has completed the work. It's therefore possible to
safely write code such as the following:

```
var button = robot.evaluate(() -> {
  var b = new Button();
  b.setText("Hello 1");
  return b;
});

robot.execute(() -> {
  button.setText("Hello 2");
});

robot.execute(() -> {
  button.setText("Hello 3");
});
```

All operations on the `Button` are safely performed on the UI thread, but
the user writing the test gets to write the code in a straightforward
synchronous style.

The `XCRobotType` will _not_ wait indefinitely for operations to complete.
After all, a bug might cause the work on the UI thread to go into an infinite
loop. The `XCRobotConfigurationType` interface allows for setting a configurable
timeout value when waiting for work to complete on the UI thread. This value
is set to one second by default; most operations on the UI thread take on
the order of milliseconds. If an operation is taking a second or more on
the UI thread then it is likely not going to complete at all and the robot
should not sit there waiting for it.

#### Keymap Generation

The standard JavaFX `Robot` interface has a somewhat unfortunate design in
that keyboard input to components is provided by having the user enter raw
`KeyCode` values:

```
Robot robot;

robot.keyType(KeyCode.H);
robot.keyType(KeyCode.E);
robot.keyType(KeyCode.L);
robot.keyType(KeyCode.L);
robot.keyType(KeyCode.O);
```

Aside from being cumbersome, this also makes tests dependent on the user's
keyboard layout. For example, the following code probably intends to type an
`@` character:

```
Robot robot;

robot.keyType(KeyCode.AT);
```

Unfortunately, in some keyboard layouts, the `KeyCode.AT` code will actually
type a `"` character.

Worse, JavaFX provides no means to determine the current keyboard layout. The
solution that the `xoanon` package provides is _keymap generation_. Put simply,
at the start of the test suite execution, the [commander](#commander) presses
every "safe" key on the keyboard one at a time (and with modifiers such as the
`SHIFT` key) and observes the change that the key made to a text field. By
storing the mappings between key codes and the actual characters that those
key codes produced, the package obtains a fairly complete view of exactly which
key codes will result in any given character.

The resulting mapping is stored in a value of type `XCKeyMap` and can be
injected into tests upon request:


```
@Test
public void testKey(
  final XCKeyMap keyMap)
  throws Exception
{
  Assumptions.assumeTrue(keyMap.keys().containsKey('!'));
}
```

The keymap is generated once at the start of a test run, and takes on average
around ten seconds to generate. Due to the relatively long time it takes to
generate a map, the generated keymap is cached to disk (with a short expiration
time) and reused upon the next test run. This is so that developers running
tests repeatedly during development don't have to sit through endless cycles
of generating keymaps.

The generated keymap is used directly by the `XCRobotType` such that the
original example code can be rewritten as:

```
XCRobotType robot;

robot.typeText("hello");
```

The `XCRobotType` will also correctly handle any modifier keys that need to
be pressed:

```
XCRobotType robot;

robot.typeText("Hello!");  // Will press and release SHIFT as needed
robot.typeText("@");       // Will correctly type the @ symbol
```

#### Slow Motion Mode

The `XCRobotType` interface can (temporarily) be put in _slow motion_ mode.
This lengthens the [pauses](#pauses) between operations to one second, so that
a human watching the test can see exactly what went wrong.

The `slowMotionEnable()` method can be called in tests to enable slow motion,
and the `xoanon` package will disable it after each test so that subsequent
tests are not affected by the change:

```
@Test
public void testFailsForSomeReason(
  final XCRobotType robot)
  throws Exception
{
  robot.slowMotionEnable();
  ...
}
```

#### Test Structure Recommendations

Even though the `xoanon` package takes excessive care to try to make tests
reliable, UI tests are fundamentally quite fragile. It is commonplace for
well-written tests to fail for no apparent reason one out of every ten runs
due to tiny differences in thread scheduling and other sources of
nondeterministic latency.

Most UI tests should use, for example, the `@MinimumPassing` annotation from
the [percentpass](https://github.com/io7m/percentpass) package. This allows
for running a specified test repeatedly and then passing if a given minimum
number of test runs succeeded:

```
@MinimumPassing(executionCount = 5, passMinimum = 4)
public void testInitialNameOpen(
  final XCCommanderType commander,
  final XCRobotType robot)
  throws Exception
{
  ...
}
```

As UI tests typically take much longer to execute than normal UI tests, there
is a tension between increasing confidence in the test by running it more times,
and reducing the number of test runs so that the test suite does not take all
day to execute.

Experience has shown that running well-written tests five times and failing
if less than four of those runs succeeded appears to be a good balance.

#### Video Capture

The `xoanon` package is designed to permit running tests under continuous
integration systems such as [Jenkins](https://www.jenkins.io/) and
[GitHub Actions](https://docs.github.com/en/actions).

It is useful, when using these systems, to record a video of the screen
during each test run. When a test fails, it's useful to be able to see onscreen
what actually happened to cause the failure.

When using GitHub Actions, the following script could be used on Linux-based
containers to record the test suite run:

```
#!/bin/bash -ex

exec > >(tee build.txt) 2>&1

#---------------------------------------------------------------------
# Install all of the various required packages.
#
# We use:
#   xvfb    to provide a virtual X server
#   fluxbox to provide a bare-minimum window manager with click-to-focus
#   ffmpeg  to record the session
#   feh     to set a background
#

sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y install xvfb fluxbox feh ffmpeg

#---------------------------------------------------------------------
# Start Xvfb on a new display.
#

Xvfb :99 &
export DISPLAY=:99
sleep 1

#---------------------------------------------------------------------
# Start recording the session.
#

ffmpeg -f x11grab -y -r 60 -video_size 1280x1024 -i :99 -vcodec libx264 test-suite.webm &
FFMPEG_PID="$!"

#---------------------------------------------------------------------
# Start fluxbox on the X server.
#

fluxbox &
sleep 1

#---------------------------------------------------------------------
# Set a desktop image.
#

feh --bg-tile .github/workflows/wallpaper.jpg
sleep 1

#---------------------------------------------------------------------
# Execute the passed-in build command.
#

"$@"

#---------------------------------------------------------------------
# Wait a while, and then instruct ffmpeg to stop recording. This step
# is necessary because video files need to be processed when recording
# stops.
#

sleep 20
kill -INT "${FFMPEG_PID}"
```

This can be called from a workflow such as:

```
name: main.linux.temurin.current

on:
  push:
    branches: [ develop, feature/*, release/* ]
  pull_request:
    branches: [ develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: JDK
        uses: actions/setup-java@v1
        with:
          java-version: 20
      - name: Build
        run: ./.github/workflows/run-with-xvfb.sh mvn --errors clean verify site
      - name: Upload video
        uses: actions/upload-artifact@v2
        if: ${{ always() }}
        with:
          name: test-video
          path: test-suite.webm
      - name: Upload test logs
        uses: actions/upload-artifact@v2
        if: ${{ always() }}
        with:
          name: test-logs
          path: ./com.io7m.xoanon.tests/target/surefire-reports
```

This will execute `mvn --errors clean verify site` and produce a video of the
entire run. As mentioned, the [commander](#commander) is run as a maximized
window in the background giving detailed information on what is currently
executing.

