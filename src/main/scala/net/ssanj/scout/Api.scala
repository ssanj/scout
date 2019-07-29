package net.ssanj.scout

import java.lang.{Thread => JThread, ThreadGroup => JThreadGroup}
import net.ssanj.scout.Thread._

object Api {

  def getAllThreadInfo(): Vector[Info] = {
    import scala.collection.JavaConverters._
    JThread.getAllStackTraces().keySet().asScala.map(getThreadInfo).toVector
  }

  def groupedThreads(): Map[String, Vector[Info]] = getAllThreadInfo.groupBy { 
    case Info(_, _, _, _, _, Group.System, _, _) => "System"
    case Info(_, _, _, _, _, Group.SubGroup(name, _, _, _, _, _), _, _) => name
  }

  def findParentThreadGroups(group: Group): Vector[Group] = {
    group match {
      case Group.System => Vector.empty[Group]
      case Group.SubGroup(_, _, _, _, _, parent) =>  group +: findParentThreadGroups(parent)
    }
  }

  def getThreadInfo(jThread: JThread): Info = {
    val id = Id(jThread.getId())
    val name = jThread.getName()
    val priority = getPriority(jThread.getPriority())

    val state = jThread.getState() match {
        case JThread.State.NEW           => State.New
        case JThread.State.RUNNABLE      => State.Runnable
        case JThread.State.BLOCKED       => State.Blocked
        case JThread.State.WAITING       => State.Waiting
        case JThread.State.TIMED_WAITING => State.TimedWaiting
        case JThread.State.TERMINATED    => State.Terminated
    }

    val group = getGroup(jThread.getThreadGroup())

    val attributes = getAttributes(jThread)

    val stackTraces = Nil//getStackTraces(jThread.getStackTrace())

    val className = ClassName(jThread.getClass.getName)

    Info(id = id , 
         name = name,
         className = className,
         priority = priority, 
         state = state, 
         group = group, 
         attributes = attributes, 
         stackTraces = stackTraces)
  }

  def getPriority(priority: Int): Priority = priority match {
      case JThread.MIN_PRIORITY  => Priority.Min
      case JThread.NORM_PRIORITY => Priority.Normal
      case JThread.MAX_PRIORITY  => Priority.Max
      case other                 => Priority.Other(other)
    } 

  def getGroup(jThreadGroup: JThreadGroup): Group = {
    val threadGroup = Option(jThreadGroup)

    threadGroup.map { jtg =>
      val name = jtg.getName()
      val activeCount = Count(jtg.activeCount())
      val maxPriority = getPriority(jtg.getMaxPriority())
      val daemon = if (jtg.isDaemon()) IsDaemon.Daemon else IsDaemon.UI
      val destroyed = if (jtg.isDestroyed()) IsDestroyed.Destroyed else IsDestroyed.NotDestroyed
      val parent = getGroup(jtg.getParent())//Option(jtg.getParent()).map(ptg => ptg.getName()).getOrElse("System")

      Group.SubGroup(name = name, activeCount = activeCount, maxPriority = maxPriority, daemon = daemon, destroyed = destroyed, parent = parent)
    }.getOrElse(Group.System)
  }

  def getAttributes(jThread: JThread): Attributes = {
    val alive       = if (jThread.isAlive()) IsAlive.Alive else IsAlive.Dead
    val daemon      = if (jThread.isDaemon()) IsDaemon.Daemon else IsDaemon.UI
    val interrupted = if (jThread.isInterrupted()) IsInterrupted.Interrupted else IsInterrupted.NotInterrupted

    Attributes(alive = alive, daemon = daemon, interrupted = interrupted)
  }

  def getStackTraces(stackTraces: Array[StackTraceElement]): List[StackElementInfo] = {
    stackTraces.map { st =>
      val className  = Option(st.getClassName()).map(ClassName)
      val fileName   = FileName(st.getFileName())
      val lineNumber = Option(st.getLineNumber()).flatMap(ln => if (ln < 0) None else Option(ln)).map(LineNumber)
      val methodName = MethodName(st.getMethodName())

      StackElementInfo(className = className, fileName = fileName, lineNumber = lineNumber, methodName = methodName)
    }.toSeq.toList
  }

}