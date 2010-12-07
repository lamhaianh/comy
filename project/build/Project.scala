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

  val sonatype = "sonatype" at "https://oss.sonatype.org/content/repositories/releases"

  val javaNetRepo    = "java.net"   at "http://download.java.net/maven/2/"          // For both JSF and EL
  val glassfishRepo  = "Glassfish"  at "http://download.java.net/maven/glassfish/"  // For JSTL
  val primeFacesRepo = "PrimeFaces" at "http://repository.prime.com.tr"

  override def libraryDependencies = Set(
    "javax.servlet" % "servlet-api" % "2.5",  // For compiling servlets
    "javax.el"      % "el-api"      % "2.2",  // Required by jsf-api
    "javax.faces"   % "jsf-api"     % "2.0",  // For compiling beans

    // For REST API
    "org.scalatra" %% "scalatra" % "2.0.0.M2",

    // For Admin UI
    "org.primefaces" % "primefaces" % "2.2.RC2",

    "ch.qos.logback"  % "logback-classic"      % "0.9.26",
    "org.mongodb"     % "mongo-java-driver"    % "2.3",
    "org.openid4java" % "openid4java-consumer" % "0.9.5",

    //-------------------------------------------------------------------------

    // Use Jetty as the embeded container when developing
    // SBT says that Jetty must be specified as "test"
    "org.eclipse.jetty" % "jetty-webapp"  % "8.0.0.M2" % "test",

    // Jetty is just a servlet container. It doest not include JSF, JSP, JSTL, EL.
    // http://www.coreservlets.com/JSF-Tutorial/jsf2/index.html
    // http://musingsofaprogrammingaddict.blogspot.com/2009/12/running-jsf-2-on-embedded-jetty.html
    "com.sun.faces"     % "jsf-impl"  % "2.0.3" % "runtime",  // Does not work if set to "test" or "provided"
    "org.glassfish.web" % "jstl-impl" % "1.2"   % "runtime",
    "org.glassfish.web" % "el-impl"   % "2.2"   % "runtime"
  ) ++ super.libraryDependencies
}
