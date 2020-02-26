package com.knoldus

import java.io.{File, FileWriter}

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

class AkkaDispatcher extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: File =>
      val result = readAndOperate(msg)
      log.info(self.path + "")
      sender() ! result
    case _ => log.info("default case")
  }

  def readAndOperate(filename: File): Map[String, Option[Int]] = {
    val fw = new FileWriter("/home/knoldus/Downloads/akkaDispatcher/src/Test.txt")
    val copyFileName = filename.toString
    val source = Source.fromFile(s"$copyFileName")
    val resultMap = source.getLines().flatMap(_.split(" ")).toList.groupBy((word: String) => word).mapValues(_.length)
    source.close()
    fw.write((Map("error" -> resultMap.get("[ERROR]")) ++ Map("warn" -> resultMap.get("[WARN]")) ++ Map("info" -> resultMap.get("[INFO]"))).toString())
    fw.close()
    Map("error" -> resultMap.get("[ERROR]")) ++ Map("warn" -> resultMap.get("[WARN]")) ++ Map("info" -> resultMap.get("[INFO]"))
  }
}

class AkkaSecheduler extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: String => val source = Source.fromFile("/home/knoldus/Downloads/akkaDispatcher/src/Test.txt")
      val fw = source.getLines().toList
      log.info(fw.toString())
  }
}


object AkkaDispatcher extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("LogFilesActorSystem", config.getConfig("configuration"))
  val threads = 5
  val ref = system.actorOf(RoundRobinPool(threads, supervisorStrategy = one).props(Props[AkkaDispatcher]), "FileOperation")
  val pathObj = new File("/home/knoldus/Downloads/Io2/")
  val list = pathObj.listFiles().toList
  val upadateFileList = list.filter(_.isFile)
  val result = upadateFileList.map(ll => ref ? ll)
  implicit val timeout: Timeout = Timeout(8.seconds)
  val actor = system.actorOf(Props(new AkkaSecheduler), name = "actor")
  val cancellable = system.scheduler.schedule(0.milliseconds, 50.milliseconds, actor, "")
  val newResult = Future.sequence(result)
  val finalResult = newResult.mapTo[List[Map[String, Option[Int]]]].map(_.foldLeft(Map("error" -> 0, "warn" -> 0, "info" -> 0)) { (result, countMap) => {
    result ++ Map("error" -> (result("error") + countMap("error").getOrElse(0))) ++ Map("warn" -> (result("warn") + countMap("warn").getOrElse(0))) ++ Map("info" -> (result("info") + countMap("info").getOrElse(0)))
  }
  })

  def one: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10.seconds) {
      case _: ArithmeticException => println("Arithmetic ayi h bhai")
        Stop
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
  }

  finalResult.map(println)


}


