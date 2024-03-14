# aeftc/planner
semi-parallel task management framework for Java/Kotlin systems, designed for FIRST Tech Challenge

### works well with:
* non-multithreaded environments, or environments where managing multithreading is
a hassle
* user-defined APIs or APIs with `tick()` or similar step functions
* systems that need to be able to stop at any time (ex. emergency stop, shutdown, etc.)

### doesn't work well with:
* schedulers being "owned" by multiple threads (especially if more than one can `tick()` schedulers!)
* high-performance systems where thread start-up is okay (threads would make more sense)
* blocking APIs (unless delegated to a thread and polled, which sort of defeats the point of this library)

## Features and goals
### Current features
* semi-parallel task management. create a `Scheduler()`, add some tasks with `Scheduler#task` and its DSL
* `Scheduler#tick()` to run all tasks in the scheduler one tick
* blocking `Scheduler#runToCompletion(Predicate p)` with exit condition
### Goals
* pause tasks (`Task#onPause` `Task#onResume` `Scheduler#pause` `Scheduler#resume`)
* task groups (exclusive and/or `Scheduler#waitForAll`)
* make cancellation options more explicit (cancel running tasks? call `onCancel`? `onFinish`? drop queued tasks?)
* drop-in profiler version of `Scheduler`: analyze time usage per tick, find slow tasks
  * stretch: cool flame graphs (external program)
* explicit `Scheduler` interface instead of closed implementation

## Usage
Not published yet. Clone and build with Gradle.