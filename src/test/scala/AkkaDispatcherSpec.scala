
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, ImplicitSender}
import com.knoldus.AkkaDispatcher

import org.scalatest._

import scala.concurrent.duration._
class AkkaDispatcherSpec extends TestKit(ActorSystem("abc")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A simple actor " should {
    "send back" in {
      within(30.seconds) {
        val actor = system.actorOf(Props(new AkkaDispatcher), name = "actor")

        actor ! "/home/knoldus/Downloads/Io2"
        val expectedMessage ="Error->  0  Warn-> 1454 Info-> 256"
        expectMsg(expectedMessage)
      }
    }


  }
}