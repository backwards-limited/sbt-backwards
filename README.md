# SBT Backwards

## Introduction

[https://binx.io/blog/2018/12/08/the-scala-build-tool/](Article):

```bash
➜ jenv local 15

➜ sbt new scala/scala-seed.g8
```

A Build builds projects; projects define themselves using settings; then what is a Setting.
A Setting is a Key -> Value pair. Something like:

```scala
name := "my-project"
version := "1.0.0-SNAPSHOT"
libraryDependencies += "foo" %% "bar" %% "1.0.0"
```

A Task is a Key -> Value pair that is evaluated on demand.
A Task exists to be evaluated every time it is needed.
Most of the time Tasks are used for doing side effects like the task ‘clean’ or the task ‘compile’.

We can show the result of either a Setting or a Task using the sbt console with the help of the ‘show’ command.
For example, when we type ‘show name’, sbt will evaluate the Key ‘name’ and return the evaluated value.
Of course, because ‘name’ is a Setting, the initialization has already been done when Sbt started, so it will return the value immediately:

```scala
➜ sbt
[info] welcome to sbt 1.4.6 (AdoptOpenJDK Java 15.0.1)

sbt:sbt-playground> name
[info] sbt-playground

sbt:sbt-playground> show name
[info] sbt-playground
```

Interestingly:

```scala
sbt:sbt-playground> show clean
[info] ()
```

The Task clean evaluates to the value ‘()’ of type Unit which is returned, because it does side effects like deleting the contents of the ‘./target’ directory.

## Mission Statement

So a Build contains one or more projects.

A project defines itself using settings.

A Setting is just a Key -> Value pair that is initialized only once

A Task is a Key -> Value pair that will be evaluated on demand.

## Configurations

To get the value of the key ‘sourceDirectories’ in the Configuration ‘Test’ we type:

```scala
sbt:sbt-playground> test:sourceDirectories
  
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/test/scala
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/test/scala-2.13
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/test/scala-2
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/test/java
[info] * /Users/davidainslie/workspace/scala/sbt-playground/target/scala-2.13/src_managed/test
```

and for the Configuration ‘Compile’ we type:

```scala
sbt:sbt-playground> compile:sourceDirectories
  
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/main/scala
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/main/scala-2.13
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/main/scala-2
[info] * /Users/davidainslie/workspace/scala/sbt-playground/src/main/java
[info] * /Users/davidainslie/workspace/scala/sbt-playground/target/scala-2.13/src_managed/main
```

For all configurations use "*" e.g.

```scala
sbt:sbt-playground> *:name
[info] sbt-playground
```

Let's say that we want to do something strange like setting the name of the project to a different name for the Configuration ‘Test’ only e.g.

```scala
sbt:sbt-playground> name in Test := "blah-blah"
```

We can also type the following in a Sbt session in which case the setting will not be persistent but only for the duration of the Sbt console session:

```scala
sbt:sbt-playground> set name in Test := "blah-blah"
```

Configurations can be scoped by task.
In sbt this type of configuration is used when launching the REPL using the ‘console’ eg: ‘sbt console’.
We have to configure the ‘initialCommands’ settingKey which is of type ‘String’ and set the scope to the taskKey ‘console’.
When we launch the REPL from sbt then the following expressions will be evaluated:

```scala
initialCommands in console :=
  """
  import scalaz._
  import Scalaz._

  val xs = List(1, 2, 3, 4, 5)
  """
```

## Keys

To be able to configure anything in the build, from a Setting to a Task, Keys play an important role because a Key allows us to bind a value to a name.
A Key is simply a name that can be created with the method ‘settingKey’ and ‘taskKey’, and then you can use the newly created key and bind that key to a specific value in a specific Configuration and Task.

For example, in the build we have:

```scala
name := "sbt-backwards",
name in Test := "sbt-backwards-in-test",
name in Compile in compile := "sbt-backwards-in-compile-for-the-task-compile",
```

and when we query:

```scala
sbt:sbt-backwards> name
[info] sbt-backwards

sbt:sbt-backwards> *:name
[info] sbt-backwards

sbt:sbt-backwards> test:name
[info] sbt-backwards-in-test

sbt:sbt-backwards> compile:compile::name
[info] sbt-backwards-in-compile-for-the-task-compile
```

The last one is interesting - noting "compile" mentioned twice and a final "::".
Read this as: Give me the value for the key 'name' in the configuration 'Compile' for the task 'compile'.

A fully-qualified reference to a setting or task looks like:

```bash
{<build-uri>}<project-id>/config:intask::key
```

Let's "inspect" a new task:

```scala
lazy val task1 = taskKey[Unit]("task 1")
lazy val task2 = taskKey[Unit]("task 2")
lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")
task2 := println("Task 2")
task3 := println("Task 3")

task3 := (task3 dependsOn task2 dependsOn task1).value
```

```scala
sbt:sbt-backwards> inspect task3
[info] Task: Unit
[info] Description:
[info]  task 3
[info] Provided by:
[info]  ProjectRef(uri("file:/Users/davidainslie/workspace/backwards/sbt-backwards/"), "root") / task3
[info] Defined at:
[info]  /Users/davidainslie/workspace/backwards/sbt-backwards/build.sbt:47
[info]  /Users/davidainslie/workspace/backwards/sbt-backwards/build.sbt:49
[info] Dependencies:
[info]  task2
[info]  task1
[info] Delegates:
[info]  task3
[info]  ThisBuild / task3
[info]  Global / task3
```

Summary:
- A scope is a tuple of components in three axes: subproject axis; configuration axis; task axis
- There's a special scope component "*" (also called Global) for any of the scope axes
- There's a special scope component "ThisBuild" (written {.} in the shell) for all the subprojects
- Test extends Runtime which extends Compile configuration
- A key can be further scoped using .in(...)

## Custom Configuration

We can also create our own configurations.
E.g. define a configuration called ‘my-config’ that will be used by the task ‘myOtherTask’:

```scala
lazy val MyConfig = config("my-config")

lazy val myOtherSetting = settingKey[String]("My other setting")

myOtherSetting := "mysetting for the current project, all configurations and all tasks"

myOtherSetting in MyConfig := "mysetting for the current project, for the MyConfig configuration and all tasks"

myOtherSetting in MyConfig in myOtherTask := "mysetting for the current project, for the MyConfig configuration for the task myOtherTask only"

lazy val myOtherTask = taskKey[Unit]("My other task")

myOtherTask := {
  val str = (myOtherSetting in MyConfig in myOtherTask).value
  println(str)
}
```

```scala
sbt:sbt-backwards> myOtherSetting
[info] mysetting for the current project, all configurations and all tasks

sbt:sbt-backwards> my-config:myOtherSetting
[info] mysetting for the current project, for the MyConfig configuration and all tasks

sbt:sbt-backwards> my-config:myOtherTask::myOtherSetting
[info] mysetting for the current project, for the MyConfig configuration for the task myOtherTask only

sbt:sbt-backwards> myOtherTask
mysetting for the current project, for the MyConfig configuration for the task myOtherTask only
```

## Dependent Tasks

We can make tasks dependent on one another:

```scala
lazy val taskA = taskKey[String]("task A")

lazy val taskB = taskKey[String]("task B")

taskA := {
  println("Evaluating taskA")
  "Hello"
}

taskB := {
  println("Evaluating taskB")
  s"${taskA.value} World!"
}
```

```scala
sbt:sbt-backwards> show taskA
Evaluating taskA
[info] Hello

sbt:sbt-backwards> show taskB
Evaluating taskA
Evaluating taskB
[info] Hello World!
```

and tasks for a certain configuration:

```scala
lazy val taskC = taskKey[String]("task C")

lazy val taskD = taskKey[String]("task D")

taskC := {
  println("Evaluating taskC for current project for all configurations")
  "Hello all config"
}

taskC in Test := {
  println("Evaluating taskC for current project for Test config")
  "Hello test config"
}

taskC in Compile := {
  println("Evaluating taskC for current project for Compile config")
  "Hello compile config"
}

taskD := {
  println("Evaluating taskD for current project for all configurations")
  val taskCValue = (taskC in Test).value
  s"$taskCValue World!"
}
```

```scala
sbt:sbt-backwards> show taskC
Evaluating taskC for current project for all configurations
[info] Hello all config

sbt:sbt-backwards> show compile:taskC
Evaluating taskC for current project for Compile config
[info] Hello compile config

sbt:sbt-backwards> show test:taskC
Evaluating taskC for current project for Test config
[info] Hello test config

sbt:sbt-backwards> show taskD
Evaluating taskC for current project for Test config
Evaluating taskD for current project for all configurations
[info] Hello test config World!
```

## Dependency Key Operator