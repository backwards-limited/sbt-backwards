import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.backwards"
ThisBuild / organizationName := "backwards"

// first define a task key
lazy val mytest = taskKey[Unit]("My test key to show how scoped settings work")

// then implement the task key
mytest := {
  val dirs = (sourceDirectories in Test).value
  println(dirs)
}

lazy val mycompile = taskKey[Unit]("My compile key to show how scoped settings work")

mycompile := {
  val dirs = (sourceDirectories in Compile).value
  println(dirs)
}

// A configuration can also be scoped to a specific Task:
lazy val mysetting = settingKey[String]("My setting")

mysetting := "mysetting for the current project, all configurations and all tasks"

mysetting in Test := "mysetting for the current project, for the Test configuration and all tasks"

mysetting in Test in myTask := "mysetting for the current project, for the Test configuration for the task myTask only"

lazy val myTask = taskKey[Unit]("My task")

myTask := {
  val str = (mysetting in Test in myTask).value
  println(str)
}

// The following example shows three tasks where task3 is dependent on task2 and task1:
lazy val task1 = taskKey[Unit]("task 1")
lazy val task2 = taskKey[Unit]("task 2")
lazy val task3 = taskKey[Unit]("task 3")

task1 := println("Task 1")
task2 := println("Task 2")
task3 := println("Task 3")

task3 := (task3 dependsOn task2 dependsOn task1).value

// Custom configuration
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

// Dependent tasks
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

// Tasks for a certain configuration
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

initialCommands in console :=
  """
  import scalaz._
  import Scalaz._

  val xs = List(1, 2, 3, 4, 5)
  """

lazy val root = (project in file("."))
  .settings(
    name := "sbt-backwards",
    name in Test := "sbt-backwards-in-test",
    name in Compile in compile := "sbt-backwards-in-compile-for-the-task-compile",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaz
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
