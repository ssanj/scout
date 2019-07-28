package net.ssanj.scout

import net.ssanj.scout.Thread._

object Printer {

  def showInfo(info: Info): String = {
    List(
      s"id=${info.id.value}",
      s"name=${info.name}",
      s"className:${info.className.value}",
      s"priority=${info.priority}",
      s"state=${info.state}",
      s"group=${showGroup(info.group)}",
      s"alive=${info.attributes.alive}",
      s"daemon=${info.attributes.daemon}",
      s"interrupted=${info.attributes.interrupted}"
    ).mkString("Thread(", ",", ")")
  }

  private def showGroup(group: Group): String = group match {
    case Group.System => "System"
    case Group.SubGroup(name, activeCount, maxPriority, daemon, destroyed, parentName) =>
      s"ThreadGroup(name=${name}, activeCount=${activeCount}, maxPriority=${maxPriority}, daemon=${daemon}, destroyed=${destroyed}, parentName=${parentName}"
  }
}