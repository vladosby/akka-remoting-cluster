package remoting

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object RemoteActors extends App {
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("remoting/remoting.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")

  localSimpleActor ! "hello, local actor!"

  val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  remoteActorSelection ! "hello form local JVm"

  import localSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)
  val remoteActorSelectionFuture = remoteActorSelection.resolveOne()
  remoteActorSelectionFuture.onComplete{
    case Success(actorRef) => actorRef ! "I've resolve you in the future!"
    case Failure(ex) => println(s"Failure to resolve a future $ex")
  }
}

object RemoteActorJVM extends App {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoting.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  remoteSimpleActor ! "hello, remote actor!"
}