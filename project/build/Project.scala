import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val localMavenRepo = "Local Maven Repo" at
    "file://"+Path.userHome+"/.m2/repository"

  val jbossRepo = "JBoss Repo" at
    "https://repository.jboss.org/nexus/content/groups/public/"

  override def libraryDependencies =
    Set(
      "org.jboss.netty" % "netty" % "3.2.0.CR1" % "compile->default"
    ) ++ super.libraryDependencies
}

