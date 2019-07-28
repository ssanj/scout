package net.ssanj.scout

// import java.lang.{Thread => JThread}
import Api._
import Printer.showInfo

object Runner {

  def main(args: Array[String]) {
    // val info = getThreadInfo(JThread.currentThread())
    // println(showInfo(info, ","))
    // JThread.currentThread().getThreadGroup().list()
    // getAllThreadInfo().foreach(t => println(showInfo(t)))
    println(
      groupedThreads().toVector.sortBy(_._1).map {
        case (k, v) => s"${k} -> ${v.map(showInfo).mkString(",")}"
      }.mkString("\n")
    )
  }
}
