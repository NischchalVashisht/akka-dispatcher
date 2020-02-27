import akka.actor.TypedActor.Supervisor
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.knoldus.AkkaDispatcher
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.concurrent.duration._
class AkkaDispatcherSpec extends TestKit(ActorSystem("abc")) with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A simple actor " should {
    "send back" in {
      within(30.seconds) {
//        val config = ConfigFactory.load()
         val sender = TestProbe
       // val system = ActorSystem("LogFilesActorSystem")
        val actor = system.actorOf(Props(new AkkaDispatcher), name = "actor")

        actor ! "/home/knoldus/Downloads/Io2"
        val expectedMessage = 791
        expectMsg(expectedMessage)
      }
    }


  }
}