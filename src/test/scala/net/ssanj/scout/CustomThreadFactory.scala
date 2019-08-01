package net.ssanj.scout
import java.util.concurrent.ThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import Api._
import Printer._
import java.util.concurrent.ExecutorService

object CustomThreadFactory {

  object NamedThreadFactory extends ThreadFactory {

    private val count = new AtomicInteger(0)


    def newThread(runnable: Runnable): Thread = {
      val threadGroup = new ThreadGroup("named-thread-factory-group")
      new Thread(threadGroup, runnable, s"my-thread-${count.getAndIncrement()}")
    }
  }

 val scoutGroup = new ThreadGroup("scout")  

  def getThreadName(): String = Thread.currentThread().getName()

  def sleep(delay: Int): Unit = {
    Thread.sleep(delay)
  }

  def createChatty(message: String): Runnable = new Runnable {
    def run(): Unit = {
      println(s"${getThreadName}: starting $message")
      sleep(1000)
      println(s"${getThreadName}: ending $message")
    }
  }

  def scoutThread(): Thread = {
    val task = new Runnable() {
      def run(): Unit = {
        while(true) {
          println("-" * 100)
          println(showGroupedThreads(groupedThreads(List.empty[Filter]), showInfoShort))
          sleep(2000)
        }
      }
    }
    new Thread(scoutGroup, task, "scout-dumper")
  }

  def exitThread(exec: ExecutorService): Thread = new Thread(scoutGroup, new Runnable(){
    def run(): Unit = {
      val banner = ("=" * 50)      
      println(s"${banner} waiting for threads to complete ${banner}")
      sleep(10000)
      println(s"${banner} SHUT DOWN INITIATED ${banner}")
      exec.shutdown()
      println(s"${banner} SHUT DOWN COMPLETE!!!! ${banner}")
      sleep(5000)
      System.exit(0)
    }
  }, "scout-exiter")

  def main(args: Array[String]): Unit = {
    val exec = Executors.newFixedThreadPool(5, NamedThreadFactory)
    scoutThread().start()    
    (0 to 10).map(n => createChatty(s"I am $n")).foreach(exec.execute)
    exitThread(exec).start()
  }
}