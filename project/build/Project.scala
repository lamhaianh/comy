import sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) {
  override def compileOptions = super.compileOptions ++
    Seq("-deprecation",
        "-Xmigration",
        "-Xcheckinit",
        "-Xwarninit",
        "-encoding", "utf8")
        .map(x => CompileOption(x))

  override def javaCompileOptions = JavaCompileOption("-Xlint:unchecked") :: super.javaCompileOptions.toList

  // Repos ---------------------------------------------------------------------

  val sonatypeSnapshot = "Sonatype Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots"  // For s3m
  val javaNet          = "java.net"          at "http://download.java.net/maven/2/"                        // For both JSF and EL
  val glassfish        = "Glassfish"         at "http://download.java.net/maven/glassfish/"                // For JSTL

  override def libraryDependencies = Set(
    // For REST API
    "tv.cntt" %% "s3m" % "1.0-SNAPSHOT",

    // JSF
    "javax"       % "javaee-web-api" % "6.0" % "provided",
    "javax.el"    % "el-api"         % "2.2" % "provided",  // Required by jsf-api
    "javax.faces" % "jsf-api"        % "2.0" % "provided",  // For compiling beans

    "ch.qos.logback"  % "logback-classic"   % "0.9.26",
    "org.mongodb"     % "mongo-java-driver" % "2.3",
    "org.expressme"   % "JOpenId"           % "1.08"
  ) ++ super.libraryDependencies
}
