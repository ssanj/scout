package net.ssanj.scout

import java.lang.{Thread => JThread, ThreadGroup => JThreadGroup}
import net.ssanj.scout.Thread._

object Api {

  def getAllThreadInfo(filters: Vector[Filter]): Vector[Info] = {
    getAllThreadInfoInternal(filters)(filterInfo(retainByFilter))
  }

  def filterInfo(singleFilter: (Filter, Info) => Boolean)(threadInfos: Vector[Info], filters: Vector[Filter]): Vector[Info] = {
    if (filters.isEmpty) threadInfos
    else {
      val (keeps, removes)= filters.partition { 
        case Filter(_, FilterType.Keep) => true 
        case _ => false 
      }

      //TODO: rewrite with pattern match
      if (keeps.isEmpty && removes.nonEmpty) {
        removes.foldLeft(threadInfos.toVector) {
          case (acc, filter) => acc.filter(singleFilter(filter, _))
        }
      } else if (keeps.nonEmpty && removes.isEmpty) {
        keeps.foldLeft(Set.empty[Info]) {
          case (acc, filter) => acc ++ threadInfos.filter(singleFilter(filter, _))
        }.toVector
      } else if (keeps.nonEmpty && removes.nonEmpty) {
        val uniqueKeeps = keeps.foldLeft(Set.empty[Info]) {
          case (acc, filter) => acc ++ threadInfos.filter(singleFilter(filter, _))
        }

        removes.foldLeft(uniqueKeeps.toVector) {
          case (acc, filter) => acc.filter(singleFilter(filter, _))
        }       
      } else Vector.empty[Info]
    }
  }

  def getAllThreadInfoInternal(filters: Vector[Filter])(filterFunc: (Vector[Info], Vector[Filter]) => Vector[Info]): Vector[Info] = {
    import scala.collection.JavaConverters._
    val allThreadInfo: Vector[Info] = JThread.getAllStackTraces().keySet().asScala.map(getThreadInfo).toVector
    filterFunc(allThreadInfo, filters)
  }

  def retainByFilter(filter: Filter, info: Info): Boolean = filter match {
    case Filter(FilterBy.ThreadName(reg), FilterType.Keep)      => reg.findFirstIn(info.name).isDefined
    case Filter(FilterBy.ThreadName(reg), FilterType.Remove)    => reg.findFirstIn(info.name).isEmpty
    case Filter(FilterBy.GroupName(reg), FilterType.Keep)       => reg.findFirstIn(Group.getName(info.group)).isDefined
    case Filter(FilterBy.GroupName(reg), FilterType.Remove)     => reg.findFirstIn(Group.getName(info.group)).isEmpty
    case Filter(FilterBy.ThreadState(state), FilterType.Keep)   => info.state == state
    case Filter(FilterBy.ThreadState(state), FilterType.Remove) => info.state != state
  }

  def groupedThreads(filters: Vector[Filter]): Map[String, Vector[Info]] = 
    getAllThreadInfo(filters).groupBy(t => Group.getName(t.group))
  
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

    val stackTraces = getStackTraces(jThread.getStackTrace())

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