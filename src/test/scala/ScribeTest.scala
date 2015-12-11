import akka.actor.{Props, ActorSystem}
import org.scalatest._
import org.scalatest.junit.ShouldMatchersForJUnit
import scribe._

class ScribeTest extends FunSpec with ShouldMatchers {

  val system = ActorSystem("HelloSystem")

  val loggingActor = system.actorOf(Props[ScribeActor], name = "loggingactor")

  describe("Scribe Tests Using API") {

    it("should create an error and info") {
      Scribe.error("br.category","Information here")
      Scribe.error("br.category","Information here")

      Scribe.info("br.category","Information here")
      Scribe.info("br.category","Information here")


      Thread.sleep(500)

      Scribe.metrics.errors.get() should equal (2)
      Scribe.metrics.info.get() should equal (2)

    }

  }
}
