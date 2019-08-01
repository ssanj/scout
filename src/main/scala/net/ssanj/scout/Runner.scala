package net.ssanj.scout

import Api._
import Printer._

object Runner {

  def main(args: Array[String]) {
    // val info = getThreadInfo(JThread.currentThread())
    // println(showInfo(info, ","))
    // JThread.currentThread().getThreadGroup().list()
    // getAllThreadInfo().foreach(t => println(showInfo(t)))
    // println(showGroupedThreads(groupedThreads(Nil), showInfoShort))
    // val regMatch = raw"run-main".r
    val filters = List.empty[Filter]//Vector(Filter(FilterBy.ThreadName(regMatch), FilterType.Keep))
    println(showGroupedThreads(groupedThreads(filters), showInfoShort))
  }
}
