package part2_remoting

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import remoting.SimpleActor

object DeployingActorsRemotely_LocalApp extends App {
  val system = ActorSystem("LocalActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf").getConfig("localApp"))

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor") // /user/remoteActor
  simpleActor ! "hello, remote actor!"

  println(simpleActor)
}

object DeployingActorsRemotely_RemoteApp extends App {
  val system = ActorSystem("RemoteActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf").getConfig("remoteApp"))
}