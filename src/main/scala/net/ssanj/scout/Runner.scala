package net.ssanj.scout

// import java.lang.{Thread => JThread}
import Api._
import Printer._

object Runner {

  def main(args: Array[String]) {
    // val info = getThreadInfo(JThread.currentThread())
    // println(showInfo(info, ","))
    // JThread.currentThread().getThreadGroup().list()
    // getAllThreadInfo().foreach(t => println(showInfo(t)))
    println(showGroupedThreads(groupedThreads(), showInfoShort))
  }
}
