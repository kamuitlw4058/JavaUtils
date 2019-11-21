package com.kimi.java.utils

import java.io._
import java.util.concurrent.{Callable, FutureTask, ThreadPoolExecutor}

import scala.collection.JavaConverters._


object CmdUtils {

  def printLogHandler(line:String): Unit ={
    println(line)
  }

  private def _logHandler(inputString: InputStream, logHanlder: String => Unit = null, pool: ThreadPoolExecutor = null,waitEnd:Boolean=true): Unit = {
    val br = new BufferedReader(new InputStreamReader(inputString))
    val callable = new Callable[Integer] {
      override def call(): Integer = {
        var s = br.readLine()
        while (s != null) {
          if (logHanlder != null) {
            logHanlder(s)
          }
          s = br.readLine()
        }
        0
      }
    }

    if (pool == null) {
      val task = new FutureTask[Integer](callable)
      val thread = new Thread(task)
      thread.start()
      if (waitEnd){
        thread.join()
      }

    } else {
      val future = pool.submit(callable)
      if (waitEnd) {
        future.get()
      }
    }
  }


  private def _executeAsync(cmd: String, logPath: String = null, logHanlder: String => Unit = null, pool: ThreadPoolExecutor = null,waitEnd:Boolean=false): Process = {
    val cmd_list = cmd.split(" ").toList.asJava
    val pwd = System.getProperty("user.dir")
    val pb = new ProcessBuilder(cmd_list)
    var ret = 0
    pb.redirectErrorStream(true)
    pb.directory(new File(pwd))
    var bw: BufferedWriter = null
    if (logPath != null) {
      bw = new BufferedWriter(new FileWriter(logPath))
    }

    def logDefaultHandler(line: String): Unit = {
      if (bw != null) {
        bw.write(line +"\n")
      }

      if (logHanlder != null) {
        logHanlder(line)
      }
    }

    val p = pb.start()
    _logHandler(p.getInputStream, logDefaultHandler, pool,waitEnd)
    p
  }

  def execute(cmd: String, logPath: String = null, logHanlder: String => Unit = printLogHandler, pool: ThreadPoolExecutor = null): Int = {
    val p = _executeAsync(cmd, logPath, logHanlder, pool,waitEnd = true)
    val ret = p.waitFor()
    ret
  }



  def executeAsync(cmd: String, logPath: String = null, logHanlder: String => Unit = null, pool: ThreadPoolExecutor = null): Process = {
    val p = _executeAsync(cmd, logPath, logHanlder, pool)
    p
  }


  def main(args: Array[String]): Unit = {
    val status = execute("ls -la")
    println(status)
  }
}
