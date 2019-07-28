package net.ssanj.scout

import java.lang.{Thread => JThread}
import net.ssanj.scout.api.getThreadInfo

object HelloWorld {

  def main(args: Array[String]) {
    val info = getThreadInfo(JThread.currentThread())
    println(info)
  }
}
