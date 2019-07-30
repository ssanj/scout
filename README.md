# boon [ ![Download](https://api.bintray.com/packages/ssanj/maven/scout/images/download.svg) ](https://bintray.com/ssanj/maven/scout/_latestVersion)


# Scout #

A small tool to quickly dump out information about running Threads and Thread groups

## Usage

Add the following to your `build.sbt` file:

```scala
libraryDependencies += "net.ssanj" %% "scout" % "0.0.1" 

resolvers += Resolver.bintrayRepo("ssanj", "maven")
```

To dump out all Threads and their Thread groups use:

```scala
import Api._
import Printer._

println(showGroupedThreads(groupedThreads()))
```

which will output something like:

```
groups:
  main > system
  system

main:
  id=192,name=scala-execution-context-global-192,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=Waiting,group=main,alive=true,daemon=true
  id=194,name=scala-execution-context-global-194,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=Waiting,group=main,alive=true,daemon=true
  id=191,name=scala-execution-context-global-191,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=Waiting,group=main,alive=true,daemon=true
  id=43,name=scala-execution-context-global-43,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=Waiting,group=main,alive=true,daemon=true
  id=193,name=scala-execution-context-global-193,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=TimedWaiting,group=main,alive=true,daemon=true
  id=195,name=scala-execution-context-global-195,className=scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2,state=Waiting,group=main,alive=true,daemon=true
  id=112,name=sbt-socket-server,className=sbt.internal.server.Server$$anon$2$$anon$1,state=Runnable,group=main,alive=true,daemon=false
  id=13,name=Log4j2-TF-1-AsyncLogger[AsyncContext@5f2108b5]-1,className=org.apache.logging.log4j.core.util.Log4jThread,state=TimedWaiting,group=main,alive=true,daemon=true
  id=186,name=pool-22-thread-5,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=188,name=pool-22-thread-7,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=187,name=pool-22-thread-6,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=184,name=pool-22-thread-3,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=189,name=pool-22-thread-8,className=java.lang.Thread,state=Waiting,group=main,alive=true,daemon=false
  id=182,name=pool-22-thread-1,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=185,name=pool-22-thread-4,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
  id=183,name=pool-22-thread-2,className=java.lang.Thread,state=TimedWaiting,group=main,alive=true,daemon=false
system:
  id=14,name=processreaper,className=java.lang.Thread,state=TimedWaiting,group=system,alive=true,daemon=true
  id=4,name=Signal Dispatcher,className=java.lang.Thread,state=Runnable,group=system,alive=true,daemon=true
  id=2,name=Reference Handler,className=java.lang.ref.Reference$ReferenceHandler,state=Waiting,group=system,alive=true,daemon=true
  id=3,name=Finalizer,className=java.lang.ref.Finalizer$FinalizerThread,state=Waiting,group=system,alive=true,daemon=true
```

See the [Api](https://github.com/ssanj/scout/blob/master/src/main/scala/net/ssanj/scout/Api.scala) class for more options.

## Publishing

To publish a new version perform the following tasks:

```
publish
bintrayRelease
```