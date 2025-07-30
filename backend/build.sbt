name := "german-learning-backend"

version := "1.0"

scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test,
      "org.postgresql" % "postgresql" % "42.7.2",
      "com.typesafe.play" %% "play-json" % "2.10.4",
      "org.mindrot" % "jbcrypt" % "0.4",
      "com.auth0" % "java-jwt" % "4.4.0",
      
      // ZIO dependencies
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-streams" % "2.0.19",
      "dev.zio" %% "zio-json" % "0.6.2",
      "dev.zio" %% "zio-http" % "3.0.0-RC4",
      "dev.zio" %% "zio-config" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC16",
      
      // Pekko dependencies (Apache Pekko is Akka's successor)
      "org.apache.pekko" %% "pekko-actor-typed" % "1.0.2",
      "org.apache.pekko" %% "pekko-stream" % "1.0.2",
      "org.apache.pekko" %% "pekko-http" % "1.0.1",
      "org.apache.pekko" %% "pekko-cluster-typed" % "1.0.2",
      "org.apache.pekko" %% "pekko-persistence-typed" % "1.0.2",
      
      // Database with ZIO
      "io.getquill" %% "quill-zio" % "4.8.0",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.0"
    )
  )

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
