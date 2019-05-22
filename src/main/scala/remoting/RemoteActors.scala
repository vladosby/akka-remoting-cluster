package remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSystem, Identify, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object RemoteActors extends App {
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("remoting/remoting.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")

  localSimpleActor ! "hello, local actor!"

  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  remoteActorSelection ! "hello form local JVm"

  import localSystem.dispatcher

  implicit val timeout: Timeout = Timeout(3 seconds)
  val remoteActorSelectionFuture = remoteActorSelection.resolveOne()
  remoteActorSelectionFuture.onComplete {
    case Success(actorRef) => actorRef ! "I've resolve you in the future!"
    case Failure(ex) => println(s"Failure to resolve a future $ex")
  }

  class ActorResolver extends Actor with ActorLogging {
    override def preStart(): Unit = {
      log.info("ActorResolver preStart method working")
      val selection = context.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
      selection ! Identify(42)
    }

    override def receive: Receive = {
      case ActorIdentity(42, Some(actorRef)) =>
        actorRef ! "Thank you for identifying yourself!"
    }
  }

  localSystem.actorOf(Props[ActorResolver], "localActorResolver")
}

object RemoteActorJVM extends App {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoting.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  remoteSimpleActor ! "hello, remote actor!"
}