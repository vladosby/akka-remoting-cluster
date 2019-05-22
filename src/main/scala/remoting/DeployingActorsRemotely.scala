package part2_remoting

import akka.actor.{ActorSystem, Address, AddressFromURIString, Deploy, Props}
import akka.remote.RemoteScope
import com.typesafe.config.ConfigFactory
import remoting.SimpleActor

object DeployingActorsRemotely_LocalApp extends App {
  val system = ActorSystem("LocalActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf").getConfig("localApp"))

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor") // /user/remoteActor
  simpleActor ! "hello, remote actor!"

  println(simpleActor)

  val remoteSystemAddress: Address = AddressFromURIString("akka://RemoteActorSystem@localhost:2552")
  val remotelyDeployedActor = system.actorOf(
    Props[SimpleActor].withDeploy(
      Deploy(scope = RemoteScope(remoteSystemAddress))
    )
  )

  remotelyDeployedActor ! "hi, remotely deployed actor!"
}

object DeployingActorsRemotely_RemoteApp extends App {
  val system = ActorSystem("RemoteActorSystem", ConfigFactory.load("remoting/deployingActorsRemotely.conf").getConfig("remoteApp"))
}