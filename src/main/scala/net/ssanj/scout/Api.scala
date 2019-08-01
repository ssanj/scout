package net.ssanj.scout

import net.ssanj.scout.model._

object Api {

  def getAllThreadInfo(filters: List[Filter]): Vector[ScoutThread] = {
    getAllThreadInfoInternal(filters)(filterInfo(retainByFilter))
  }

  def filterInfo(singleFilter: (Filter, ScoutThread) => Boolean)(threadInfos: Vector[ScoutThread], filters: List[Filter]): Vector[ScoutThread] = {
    if (filters.isEmpty) threadInfos
    else {
      val (keeps, removes)= filters.partition { 
        case Filter(_, FilterType.Keep) => true 
        case _ => false 
      }

      //TODO: rewrite with pattern match
      (keeps, removes) match {
        case (Nil, _ :: _) => removes.foldLeft(threadInfos.toVector) {
          case (acc, filter) => acc.filter(singleFilter(filter, _))
        }

        case (_ :: _, Nil) => keeps.foldLeft(Set.empty[ScoutThread]) {
          case (acc, filter) => acc ++ threadInfos.filter(singleFilter(filter, _))
        }.toVector

        case (_ :: _, _ :: _) => 
          val uniqueKeeps = keeps.foldLeft(Set.empty[ScoutThread]) {
            case (acc, filter) => acc ++ threadInfos.filter(singleFilter(filter, _))
          }

          removes.foldLeft(uniqueKeeps.toVector) {
            case (acc, filter) => acc.filter(singleFilter(filter, _))
          }       

        case (Nil, Nil) => Vector.empty[ScoutThread]
      }
    }
  }

  def getAllThreadInfoInternal(filters: List[Filter])(filterFunc: (Vector[ScoutThread], List[Filter]) => Vector[ScoutThread]): Vector[ScoutThread] = {
    import scala.collection.JavaConverters._
    val allThreadInfo: Vector[ScoutThread] = Thread.getAllStackTraces().keySet().asScala.map(getThreadInfo).toVector
    filterFunc(allThreadInfo, filters)
  }

  def retainByFilter(filter: Filter, info: ScoutThread): Boolean = filter match {
    case Filter(FilterBy.ThreadName(reg), FilterType.Keep)      => reg.findFirstIn(info.name.value).isDefined
    case Filter(FilterBy.ThreadName(reg), FilterType.Remove)    => reg.findFirstIn(info.name.value).isEmpty
    case Filter(FilterBy.GroupName(reg), FilterType.Keep)       => reg.findFirstIn(Group.getName(info.group)).isDefined
    case Filter(FilterBy.GroupName(reg), FilterType.Remove)     => reg.findFirstIn(Group.getName(info.group)).isEmpty
    case Filter(FilterBy.ThreadState(state), FilterType.Keep)   => info.state == state
    case Filter(FilterBy.ThreadState(state), FilterType.Remove) => info.state != state
  }

  def groupedThreads(filters: List[Filter]): Map[String, Vector[ScoutThread]] = 
    getAllThreadInfo(filters).groupBy(t => Group.getName(t.group))
  
  def findParentThreadGroups(group: Group): Vector[Group] = {
    group match {
      case Group.System => Vector.empty[Group]
      case Group.SubGroup(_, _, _, _, _, parent) =>  group +: findParentThreadGroups(parent)
    }
  }

  def getThreadInfo(jThread: Thread): ScoutThread = {
    val id = Id(jThread.getId())
    val name = ThreadName(jThread.getName())
    val priority = getPriority(jThread.getPriority())

    val state = jThread.getState() match {
        case Thread.State.NEW           => State.New
        case Thread.State.RUNNABLE      => State.Runnable
        case Thread.State.BLOCKED       => State.Blocked
        case Thread.State.WAITING       => State.Waiting
        case Thread.State.TIMED_WAITING => State.TimedWaiting
        case Thread.State.TERMINATED    => State.Terminated
    }

    val group = getGroup(jThread.getThreadGroup())

    val attributes = getAttributes(jThread)

    val stackTraces = getStackTraces(jThread.getStackTrace())

    val className = ClassName(jThread.getClass.getName)

    ScoutThread(id = id , 
                name = name,
                className = className,
                priority = priority, 
                state = state, 
                group = group, 
                attributes = attributes, 
                stackTraces = stackTraces)
  }

  def getPriority(priority: Int): Priority = priority match {
      case Thread.MIN_PRIORITY  => Priority.Min
      case Thread.NORM_PRIORITY => Priority.Normal
      case Thread.MAX_PRIORITY  => Priority.Max
      case other                 => Priority.Other(other)
    } 

  def getGroup(jThreadGroup: ThreadGroup): Group = {
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

  def getAttributes(jThread: Thread): Attributes = {
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