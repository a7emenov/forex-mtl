# Forex-mtl

## Overview

Forex-mtl is a simple application that acts as a local proxy for getting exchange rates. It's a service that can be
consumed by other internal services to get the exchange rate between a set of currencies, so they don't have to care about
the specifics of third-party providers. Precise requirements are specified in [Task.md](Task.md).

## Architecture

Forex-mtl consists of 2 main components:

* **API**  
The only API method accepts currency pairs as query string parameters to get exchange rates for. If the rate can not be 
found, the API doesn't fail but returns a `404` code instead.

* **RatesService**
Provides exchange rates for the API. It operates based on a cache that is asynchronously updated every `N` seconds by 
querying one-frame API. Cache update failures do not prevent the service from functioning.

## Requirements

* Scala build tool (sbt) >= 1.3.5
* Java Development Kit (JDK) = 8 
* Docker console client >= 17.5
* Docker-compose client >= 3.6

## Configuration

The project can be configured by providing a different `application.conf` file in the classpath or the following variables:

* `API_PORT` - the port for the API to accept connections on. Example: `80`.
* `API_TIMEOUT` - timeout for the API requests. Example: `10 seconds`.
* `ONEFRAME_API_URL` - url of one-frame rates API. Example: `http://127.0.0.1/rates`.
* `ONEFRAME_API_ACCESS_TOKEN` - token to access one-frame API. Example: `secure-token`.
* `CACHE_REFRESH_INTERVAL` - time interval between consequent cache updates. Example: `10 seconds`.

## Building the project

A Docker image of the project can be published to a local docker registry by running the following command:
`sbt docker:publishLocal`

See the [native packager](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html) documentation page for 
additional details.

Once an image is built, a sample system can be started by running the `docker-compose up` command from the project's root.

## Assumptions

1. **Either one-frame API is always available or is able to recover within 10 minutes.**  
Per requirements Forex-mtl is able to query the API 1000 times a day (~1 request every 86 seconds) given that it has access
to only one authentication token, but it also has to make at least one query every 10 minutes (~1 request every 600 
seconds, 144 requests per day). That means that additional 856 requests per day are allowed to uphold the SLA, but the
precise strategy should depend on one-frame API's availability pattern.

2. **Inverse currency rates can not be calculated by simply inverting the original price**.  
While it may be tempting to save on the request number to calculate the rate `B => A` by inverting the rate `A => B`, in
reality it's rarely the case and the difference may be substantial enough to the customer.

3. **One-frame API will function normally when provided 72 parameters.** (Implication of `1.` and `2.`)  
If Forex-mtl is to send exchange rates requests individually for each currency (9 currency values), to get all exchange
rates it would require 72 requests (`8 + 7 + ... + 0` for `A => B` and the same number for the inverses) at least every 10
minutes, accounting for 10368 daily requests in total. Therefore, it order to uphold the SLA it is assumed that getting
all exchange rates data through a single request will not cause significant delays or errors from open-frame API.

4. **Forex-mtl has only one instance for each access token.**
For simplicity it is assumed that each application acts individually, without having to share the request quota with
other instances.

## Further improvements

* Add integration tests for the API and one-frame http client.
* Add OpenAPI specification.
* Add ["golden tests"](https://ro-che.info/articles/2017-12-04-golden-tests) for the API specification.
* Add performance tests.