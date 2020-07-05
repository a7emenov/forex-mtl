package forex.services.rates.cache

private[rates] trait RatesCacheUpdateAlgebra[F[_]] {

  def runSynchronousUpdates: F[Unit]
}
