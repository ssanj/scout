package net.ssanj.scout

import net.ssanj.scout.Thread._
import net.ssanj.scout.Api._

object Printer {

  val nl = System.lineSeparator

  def showInfo(info: Info): String = {
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
    ).mkString("Thread(", ",", ")")
  }  

  def showInfoShort(info: Info): String = {
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

  def showGroupedThreads(groupedThreads: Map[String, Vector[Info]]): String = {
    val indent = " " * 2
    val groupedThreadsSortedByKey = groupedThreads.toVector.sortBy(_._1)

    val groupToThreads = groupedThreadsSortedByKey.map {
      case (k, v) =>
        val values: Map[String, String] = v.groupBy(_.name.split("-").dropRight(1).mkString("-")).
          mapValues(_.map(showInfoShort).mkString(s"${nl}${indent}"))
        s"${k}:${nl}${indent}${values.map(_._2).mkString(s"${nl}${indent}")}"
    }.mkString(nl)

    val groupLineage = {
      val uniqueGroups = groupedThreads.values.toVector.flatMap(_.map(_.group)).toSet.toVector
       val lineage = uniqueGroups.map(g => findParentThreadGroups(g).map(Group.getName).mkString(" > ")).mkString(s"${nl}${indent}")
       s"groups:${nl}${indent}${lineage}"
    }

    s"${groupLineage}${nl}${nl}${groupToThreads}"
  }

  private def showGroup(group: Group): String = group match {
    case Group.System => "ThreadGroup(name=System)"
    case Group.SubGroup(name, activeCount, maxPriority, daemon, destroyed, parentName) =>
      s"ThreadGroup(name=${name}, activeCount=${activeCount}, maxPriority=${maxPriority}, daemon=${daemon}, destroyed=${destroyed}, parentName=${parentName}"
  }
}