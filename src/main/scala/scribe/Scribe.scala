package scribe

import java.util.Date
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Props, ActorSystem, Actor}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Scribe {
  lazy val config = ConfigFactory.load()
  lazy val metrics = new Metrics()
  val hostName = java.net.InetAddress.getLocalHost.getHostName

  val system = ActorSystem("ScribeSystem")
  val scribeActor = system.actorOf(Props[ScribeActor], name = "scribe-actor")

  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.{read, write}
  implicit val formats = Serialization.formats(NoTypeHints)

  def Sread[T](json: String)(implicit m: Manifest[T]): T = read[T](json)
  def Swrite[T <: AnyRef](entity: T): String = write[T](entity)

  def error(category : String, message : String) = {
    scribeActor ! LogMessage(category,message,Level.ERROR.toString,hostName,new Date().toString)
  }

  def info(category : String, message : String) = {
    scribeActor ! LogMessage(category,message,Level.INFO.toString,hostName,new Date().toString)
  }
}

case class LogMessage(
                     category: String,
                     message: String,
                     level: String,
                     host: String,
                     ts: String
                     )

class ScribeActor extends Actor {

  def logger = LoggerFactory.getLogger(this.getClass)

  def receive = {
    case m : LogMessage if m.level == Level.ERROR.toString => {
      val i = Scribe.metrics.errors.get()+1
      Scribe.metrics.errors.set(i)
      logger.error(Scribe.Swrite(m))
    }
    case m : LogMessage if m.level == Level.INFO.toString => {
      val i = Scribe.metrics.info.get()+1
      Scribe.metrics.info.set(i)
      logger.info(Scribe.Swrite(m))
    }
    case _ => {
      val i = Scribe.metrics.other.get()+1
      Scribe.metrics.other.getAndSet(i)
    }
  }
}

class Metrics {
    val errors : AtomicReference[Long] = new AtomicReference(0L)
    val info : AtomicReference[Long] = new AtomicReference(0L)
    val backlog : AtomicReference[Long] = new AtomicReference(0L)
    val other : AtomicReference[Long] = new AtomicReference(0L)
}

object Level extends Enumeration {
  type level = Value
  val ERROR = Value("Error")
  val INFO = Value("Info")
}