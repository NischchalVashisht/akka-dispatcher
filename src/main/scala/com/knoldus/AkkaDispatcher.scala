package com.knoldus

import java.io.File

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

class AkkaDispatcher extends Actor with ActorLogging {
  override def receive:Receive = {

    case fileDirectory:String =>
      val pathObj = new File(fileDirectory)
      val list = pathObj.listFiles().toList
      val updateFileList = list.filter(_.isFile)

      implicit val timeout:Timeout = Timeout(8.seconds)
      val ref = context.actorOf(RoundRobinPool(10,supervisorStrategy = one).props(Props[AkkaScheduler]))
      val result = updateFileList.map(file => ref ? file)
      val newResult = Future.sequence(result)
      val finalResult = newResult.mapTo[List[Map[String,Option[Int]]]]
        .map(_.foldLeft(Map("error" -> 0,"warn" -> 0,"info" -> 0)){ (result,countMap) => {
          result ++ Map("error" -> (result("error") + countMap("error")
            .getOrElse(0))) ++ Map("warn" -> (result("warn") + countMap("warn")
            .getOrElse(0))) ++ Map("info" -> (result("info") + countMap("info").getOrElse(0)))
        }
        })
     val abc= finalResult.map(x => ("Error->  " + x("error") / 10 + "  Warn-> " + x("warn") / 10 + " Info-> " + x("info") / 10))
      log.info(" " + sender.path + " " + finalResult)
      abc.pipeTo(sender)

    case _ => log.info("default case")
  }

  private def one:SupervisorStrategy = {
    OneForOneStrategy(withinTimeRange = 10.seconds,maxNrOfRetries = 5){
      case _:ArithmeticException => log.info("Arithmetic ayi h bhai")
        Stop
      case _:NullPointerException => Restart
      case _:IllegalArgumentException => Stop
      case _:Exception => Escalate
    }
  }
}

class AkkaScheduler extends Actor with ActorLogging {

  override def receive:Receive = {
    case msg:File =>
      val result = readAndOperate(msg)
      sender() ! result

  }

  private def readAndOperate(filename:File):Map[String,Option[Int]] = {
    val copyFileName = filename.toString
    val source = Source.fromFile(s"$copyFileName")
    val resultMap = source.getLines().flatMap(_.split(" ")).toList.groupBy((word:String) => word).mapValues(_.length)
    source.close()
    Map("error" -> resultMap.get("[ERROR]")) ++ Map("warn" -> resultMap.get("[WARN]")) ++ Map("info" -> resultMap.get("[INFO]"))
  }
}

