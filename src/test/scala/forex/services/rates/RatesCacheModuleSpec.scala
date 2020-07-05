package forex.services.rates

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import forex.BaseSpec
import forex.domain.Rate
import forex.services.rates.cache.RatesCacheModule
import util.Generators._

class RatesCacheModuleSpec extends BaseSpec {

  behavior of "Rates cache"

  it should "successfully get every saved value" in {
    forAll(rateMap) { ratesMap =>
      withCache { cache =>
        cache.update(ratesMap).unsafeRunSync()
        ratesMap.foreach {
          case (key, value) =>
            cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }

  it should "always get the latest values" in {
    forAll(rateMap, rateMap) { (ratesMap1, ratesMap2) =>
      withCache { cache =>
        (cache.update(ratesMap1) >> cache.update(ratesMap2)).unsafeRunSync()
        ratesMap2.foreach {
          case (key, value) =>
            cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }

  it should "preserve old values" in {
    forAll(rateMap, rateMap) { (ratesMap1, ratesMap2) =>
      withCache { cache =>
        (cache.update(ratesMap1) >> cache.update(ratesMap2)).unsafeRunSync()
        ratesMap1.filterKeys(k => !ratesMap2.contains(k)).foreach {
          case (key, value) =>
            cache.get(key).unsafeRunSync() shouldBe Some(value)
        }
      }
    }
  }

  it should "return None for missing values" in {
    forAll(rateMap, rateMap) { (ratesMap1, ratesMap2) =>
      withCache { cache =>
        cache.update(ratesMap1).unsafeRunSync()
        ratesMap2.keys.filterNot(ratesMap1.contains).foreach { key =>
          cache.get(key).unsafeRunSync() shouldBe None
        }
      }
    }
  }

  private def withCache(testCode: RatesCacheModule[IO] => Any): Any = {
    val cache = Ref.of[IO, Map[Rate.Currencies, Rate]](Map.empty).unsafeRunSync()
    testCode(new RatesCacheModule[IO](cache))
  }
}
