name := "rest-example-akka"

libraryDependencies ++= Seq(
  "com.typesafe.akka"        %% "akka-actor"           % "2.6.17",
  "com.typesafe.akka"        %% "akka-stream"          % "2.6.17",
  "com.typesafe.akka"        %% "akka-http"            % "10.2.7",
  "com.typesafe.akka"        %% "akka-http-spray-json" % "10.2.7"
)