package io.allquantor.scemini.client.gemini

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import io.allquantor.scemini.adt.gemini.GeminiEvents.{ChangeEvent, GeminiEvent}
import io.allquantor.scemini.adt.gemini.GeminiConstants.{GeminiEventReasons, GeminiEventTypes, CurrencyPairs}
import io.allquantor.scemini.adt.gemini.GeminiConstants.CurrencyPairs.CurrencyPair
import io.allquantor.scemini.client.ExchangePlatformClient
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class GeminiMarketsTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer.create(system)
  implicit val ec = system.dispatcher

  override def afterAll {
    system.terminate()
  }


  "Gemini MarketClient" should "retrieve gemini sandbox market stream " in {

    val currencyPairs = Seq(CurrencyPairs.btcusd,CurrencyPairs.ethbtc)
    val client = ExchangePlatformClient.asGeminiClient(currencyPairs)
    val source = client.source

    type ResultType = Either[io.circe.Error, GeminiEvent]
    val testSink = TestSink.probe[ResultType](system)

    source.
      runWith(testSink)
      .ensureSubscription()
      .request(1)
      .expectNextChainingPF(
        { case Right(e:GeminiEvent) => e.currencyPair.get shouldBe an[CurrencyPair] })


  }
}
