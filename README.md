# Forex-mtl

## Overview

## Architecture

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

4. **Forex-mtl has only one instance for each access key.**
For simplicity it is assumed that each application acts individually, without having to share the request quota with
other instances.