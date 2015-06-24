val project = Project(
  id = "robakka-id",
  base = file("."),
  settings = Project.defaultSettings ++
             Seq(
               name := """robakka""",
               scalaVersion := "2.11.6",
               scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
               javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m"),
               libraryDependencies ++= Seq(
                 "com.typesafe.akka" %% "akka-actor" % "2.3.11",
                 "org.jfree" % "jfreechart" % "1.0.19",
                 "com.github.scopt" %% "scopt" % "3.3.0")
             )
)

