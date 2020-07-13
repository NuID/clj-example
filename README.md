<p align="right"><a href="https://nuid.io"><img src="https://nuid.io/svg/logo.svg" width="20%"></a></p>

# nuid.clj-example

This repository contains a distilled and documented demonstration of using the
[`nuid.credential`](https://github.com/nuid/credential) authentication library
in Clojure to interact with NuID's Auth API. The library works identically from
ClojureScript.

The primary goal of this repository is to demonstrate the request and response
data for each endpoint, as well as the relationship between endpoints in a
typical authentication flow. In other words, `nuid.clj-example` is not intended
to be used as a bundled client library or dependency, although one of those is
on our roadmap! Similar logic could be expressed many different ways according
to specific needs, dependencies, etc.. We'd love to help adapt and expand this
example to other contexts, so [get in touch](mailto:support@nuid.io)!

Additional documentation can be found by registering for an API Key and reading
the documentation in the [portal](https://portal.nuid.io). We will be publishing
prettier and better in every way documentation over the coming months. And
always feel welcome to [reach out](mailto:support@nuid.io) with any questions that
arise.

## Prerequisites

* [`jvm`](https://www.java.com/en/download/)
* [`clj`](https://clojure.org/guides/getting_started)
* An API Key (freely available at the [portal](https://portal.nuid.io))

## Getting started

The example is meant to be explored in the REPL. It is recommended to evaluate
individual forms and inspect request and response data.

The typical starting point will be to open the
[`nuid.clj-example`](https://github.com/NuID/clj-example/blob/main/src/nuid/clj_example.clj)
namespace and start a REPL from the namespace as prescribed by your editor and
configuration. Once the REPL has been started and the namespace has been
evaluated, the forms in the `(comment ,,,)` block will invoke the API with the
appropriate data.

## Licensing

Apache v2.0 or MIT
