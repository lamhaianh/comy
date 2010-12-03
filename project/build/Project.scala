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

  // Servlet container
  //val jetty7      = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "provided"
  val jetty6      = "org.mortbay.jetty" % "jetty"        % "6.1.18" % "test"
  val jspApi      = "org.mortbay.jetty" % "jsp-api-2.1"  % "6.1.14"
  val jspCompiler = "org.mortbay.jetty" % "jsp"          % "5.5.12"

  val sonatype = "sonatype" at "https://oss.sonatype.org/content/repositories/releases"

  val javaNetRepo    = "java.net"   at "http://download.java.net/maven/2/"          // For both JSF and EL
  val glassfishRepo  = "Glassfish"  at "http://download.java.net/maven/glassfish/"  // For JSTL
  val primeFacesRepo = "PrimeFaces" at "http://repository.prime.com.tr"

  override def libraryDependencies = Set(
    // http://www.coreservlets.com/JSF-Tutorial/jsf2/index.html says that we need
    // these 4 JARs
    "com.sun.faces"          % "jsf-api"   % "2.0.3",
    "com.sun.faces"          % "jsf-impl"  % "2.0.3",  // Does not work if set to "provided"
    "javax.servlet.jsp.jstl" % "jstl-api"  % "1.2",
    "org.glassfish.web"      % "jstl-impl" % "1.2",

    // http://musingsofaprogrammingaddict.blogspot.com/2009/12/running-jsf-2-on-embedded-jetty.html
    "javax.el"          % "el-api"  % "2.2",
    "org.glassfish.web" % "el-impl" % "2.2",

    // For REST API
    "org.scalatra" %% "scalatra" % "2.0.0.M2",

    // For Admin UI
    "org.primefaces" % "primefaces" % "2.2.RC2",

    "ch.qos.logback" %  "logback-classic"   % "0.9.26",
    "org.mongodb"    %  "mongo-java-driver" % "2.3"
  ) ++ super.libraryDependencies

  // Paths ---------------------------------------------------------------------

  override def unmanagedClasspath = super.unmanagedClasspath +++ ("config")
}
