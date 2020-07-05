package forex.services.rates

import cats.effect.IO
import forex.domain.Rate
import org.scalacheck.Gen
import util.Generators._
import cats.syntax.flatMap._

class RatesCacheModuleSpec extends BaseSpec {

  private def withCache(testCode: RatesCacheModule[IO] => Any): Any = {
    val cache = RatesCacheModule.empty[IO].unsafeRunSync()
    testCode(cache)
  }

  val rateMap: Gen[Map[Rate.Currencies, Rate]] =
    Gen.nonEmptyMap(rate.map(r => r.currencies -> r))

  behavior of "Rates cache"

  it should "successfully get every saved value" in {
    forAll(rateMap) { ratesMap =>
      withCache { cache =>
        cache.update(ratesMap).unsafeRunSync()
        ratesMap.foreach { case (key, value) =>
          cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }

  it should "always get the latest values" in {
    forAll(rateMap, rateMap) { (ratesMap1, ratesMap2) =>
      withCache { cache =>
        (cache.update(ratesMap1) >> cache.update(ratesMap2)).unsafeRunSync()
        ratesMap2.foreach { case (key, value) =>
          cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }

  it should "preserve old values" in {
    forAll(rateMap, rateMap) { (ratesMap1, ratesMap2) =>
      withCache { cache =>
        (cache.update(ratesMap1) >> cache.update(ratesMap2)).unsafeRunSync()
        ratesMap1.filterKeys(k => !ratesMap2.contains(k)).foreach { case (key, value) =>
          cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }
}
