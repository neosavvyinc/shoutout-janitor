import AssemblyKeys._

assemblySettings ++ Set(jarName in assembly := "shoutout-janitor.jar",
  mainClass in assembly := None,
  test in assembly := {},
  mergeStrategy in assembly <<= (mergeStrategy in assembly) {(old) =>
    {
      case PathList("org", "hamcrest", xs @ _*)         => MergeStrategy.first
      case "logback.properties" =>  MergeStrategy.discard
      case "logback.xml" =>  MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case x => old(x)
    }
  }
)
