package remoting

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object RemoteActors extends App {
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("remoting/remoting.conf"))
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("remoting/remoting.conf").getConfig("remoteSystem"))

  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")

  localSimpleActor ! "hello, local actor!"
  remoteSimpleActor ! "hello, remote actor!"
}
