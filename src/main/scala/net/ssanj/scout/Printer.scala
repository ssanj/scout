package net.ssanj.scout

import net.ssanj.scout.Thread._
import net.ssanj.scout.Api._

object Printer {

  val nl = System.lineSeparator

  val indent = " " * 2

  val stackTraceIndent = " " * 4

  def showInfo(info: Info): String = {
    val threadInfo = showInfoWithoutStackTrace(info)
    val traces = showStackTraces(info.stackTraces)
    s"${threadInfo}${traces}"
  }

  def showInfoWithoutStackTrace(info: Info): String = {
    List(
      s"id=${info.id.value}",
      s"name=${info.name}",
      s"className=${info.className.value}",
      s"priority=${info.priority}",
      s"state=${info.state}",
      s"group=${showGroup(info.group)}",
      s"alive=${info.attributes.alive}",
      s"daemon=${info.attributes.daemon}",
      s"interrupted=${info.attributes.interrupted}"
    ).mkString(",")
  }  

  def showInfoShort(info: Info): String = {
    val threadInfo = showInfoShortWithoutStackTrace(info)
    val traces = showStackTracesByDepth(info.stackTraces, 5)
    s"${threadInfo}${traces}"
  }

  def showInfoShortWithoutStackTrace(info: Info): String = {
    List(
      s"id=${info.id.value}",
      s"name=${info.name}",
      s"className=${info.className.value}",
      s"state=${info.state}",
      s"group=${Group.getName(info.group)}",
      s"alive=${Attributes.isAlive(info.attributes.alive)}",
      s"daemon=${Attributes.isDaemon(info.attributes.daemon)}"
    ).mkString(",")
  }

  def showStackTracesByDepth(st: List[StackElementInfo], depth: Int): String =  {
    st.
      take(depth).
      map(showStackTrace).
      mkString(s"${nl}${stackTraceIndent}", s"${nl}${stackTraceIndent}", "")
  }

  def showStackTraces(st: List[StackElementInfo]): String =  {
    st.
      map(showStackTrace).
      mkString(s"${nl}${stackTraceIndent}", s"${nl}${stackTraceIndent}", "")
  }

  def showGroupedThreads(groupedThreads: Map[String, Vector[Info]], showInfo: Info => String): String = {
    val groupedThreadsSortedByKey = groupedThreads.toVector.sortBy(_._1)

    val groupToThreads = groupedThreadsSortedByKey.map {
      case (k, v) =>
        val values: Map[String, String] = v.groupBy(_.name.split("-").dropRight(1).mkString("-")).
          mapValues(_.map(showInfo).mkString(s"${nl}${indent}"))
        s"${k}:${nl}${indent}${values.map(_._2).mkString(s"${nl}${indent}")}"
    }.mkString(nl)

    val groupLineage = {
      val uniqueGroups = groupedThreads.values.flatMap(_.map(_.group)).toSet.toVector
       val lineage = uniqueGroups.map(g => findParentThreadGroups(g).map(Group.getName).mkString(" > ")).mkString(s"${nl}${indent}")
       s"groups:${nl}${indent}${lineage}" 
    }

    s"${groupLineage}${nl}${nl}${groupToThreads}"
  }

  private def showOp[A](valueOp: Option[A], f: A => String, default: String): String = valueOp.map(f).getOrElse(default)

  def showStackTrace(st: StackElementInfo): String =  {
    val lineNumber = showOp[LineNumber](st.lineNumber, _.value.toString, "-")
    val location = s"${st.fileName.value}#${st.methodName.value}:${lineNumber}"
    
    s"className=${showOp[ClassName](st.className, _.value, "-")}, location=${location}"
  }

  def showGroup(group: Group): String = group match {
    case Group.System => "ThreadGroup(name=System)"
    case Group.SubGroup(name, activeCount, maxPriority, daemon, destroyed, parentName) =>
      s"ThreadGroup(name=${name}, activeCount=${activeCount}, maxPriority=${maxPriority}, daemon=${daemon}, destroyed=${destroyed}, parentName=${parentName}"
  }
}