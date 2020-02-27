package com.knoldus

import java.io.{File, FileWriter}

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.io.Udp.SimpleSender
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

class AkkaDispatcher extends Actor with ActorLogging {
  override def receive: Receive = {

    case fileDirectory: String =>
      val pathObj = new File(fileDirectory)
      val list = pathObj.listFiles().toList
      val upadateFileList = list.filter(_.isFile)

      implicit val timeout: Timeout = Timeout(8.seconds)
      val ref = context.actorOf(RoundRobinPool(5, supervisorStrategy = one).props(Props[AkkaScheduler]))
      val result = upadateFileList.map(file => ref ? file)
      val newResult = Future.sequence(result)
      val finalResult = newResult.mapTo[List[Map[String, Option[Int]]]].map(_.foldLeft(Map("error" -> 0, "warn" -> 0, "info" -> 0)) { (result, countMap) => {
        result ++ Map("error" -> (result("error") + countMap("error").getOrElse(0))) ++ Map("warn" -> (result("warn") + countMap("warn").getOrElse(0))) ++ Map("info" -> (result("info") + countMap("info").getOrElse(0)))
      }
      })
      Thread.sleep(3000)
     // finalResult.map(x => log.info("Error->  "+x("error")/10+"  Warn-> "+x("warn")/10+" Info-> "+x("info")/10))
        //  sender ! finalResult
      log.info(" "+sender.path+" "+finalResult)
      finalResult.pipeTo(sender)
//
    case _ => log.info("default case")
  }

 private def one: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10.seconds) {
      case _: ArithmeticException => println("Arithmetic ayi h bhai")
        Stop
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
  }
}

class AkkaScheduler extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: File =>
      val result = readAndOperate(msg)
      sender() ! result

  }

 private def readAndOperate(filename: File): Map[String, Option[Int]] = {
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

//
//object AkkaDispatcher extends App {
//
//  val config = ConfigFactory.load()
//  val system = ActorSystem("LogFilesActorSystem", config.getConfig("configuration"))
//  val actor = system.actorOf(Props(new AkkaDispatcher), name = "actor")
//
//  system.scheduler.scheduleAtFixedRate(0.seconds, 10.seconds, actor, "/home/knoldus/Downloads/Io2")
//
//      actor ! 123
//
//}


