package forex.services.rates

import cats.effect.IO
import forex.BaseSpec
import forex.domain.Rate
import forex.services.rates.cache.RatesCacheAlgebra
import forex.services.rates.errors.Error.RateNotAvailable
import util.Generators._

class OneFrameRatesModuleSpec extends BaseSpec {

  behavior of "One-frame rates module"

  it should "be consistent with the cache without updates" in {
    forAll(rateMap) { rates =>
      val service = new OneFrameRatesModule[IO](dummyCache(rates))
      rates.foreach { case (key, value) =>
        service.get(key).unsafeRunSync() shouldBe Right(value)
      }
    }
  }

  it should "be consistent with the cache after updates" in {
    forAll(rateMap, rateMap) { (initialRates, rates) =>
      val cache = dummyCache(initialRates)
      val service = new OneFrameRatesModule[IO](cache)
      cache.update(rates).unsafeRunSync()
      rates.foreach { case (key, value) =>
        service.get(key).unsafeRunSync() shouldBe Right(value)
      }
    }
  }

  it should "return RateNotAvailable errors for missing rates" in {
    forAll(rateMap, rateMap) { (initialRates, rates) =>
      val cache = dummyCache(initialRates)
      val service = new OneFrameRatesModule[IO](cache)
      rates.keys.filterNot(initialRates.contains).foreach { key =>
        service.get(key).unsafeRunSync() shouldBe Left(RateNotAvailable(key))
      }
    }
  }

  private def dummyCache(initialValues: Map[Rate.Currencies, Rate]): RatesCacheAlgebra[IO] = new RatesCacheAlgebra[IO] {
    private var map: Map[Rate.Currencies, Rate] =
      initialValues

    override def get(currencies: Rate.Currencies): IO[Option[Rate]] =
      IO.pure(map.get(currencies))

    override def update(rates: Map[Rate.Currencies, Rate]): IO[Unit] =
      IO.delay { map = map ++ rates }
  }
}
