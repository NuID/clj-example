(ns nuid.clj-example
  (:require
   [clj-http.client :as http]
   [clojure.data.json :as json]
   [nuid.credential :as credential]
   [nuid.credential.challenge :as challenge]
   [nuid.credential.lib :as credential.lib]
   [nuid.zk.protocol :as zk.protocol]))

(def ^:private encode-body
  "NOTE: `nuid.credential.lib/stringify` and `nuid.credential.lib/keywordize`
  are helpers necessary for use with encoders that don't respect namespaces.
  Encoders that maintain namespaces alleviate this concern and don't require
  additional transformation. For that reason it is recommended to use `Transit`
  when interacting with the NuID Auth API, if available."
  (comp
   json/write-str
   credential.lib/stringify))

(def ^:private decode-body
  (comp
   credential.lib/keywordize
   (fn [x] (json/read-str x :key-fn keyword))))

(defn post-credential
  "Create a new credential based on `:nuid.clj-example/secret` and register it
  to Ethereum's Rinkeby test network. In the future, developers will have
  control over how credentials are routed and stored."
  [{::keys [api-key api-root-url secret] :or {secret "testSecret123"}}]
  (let [endpoint (str api-root-url "/credential")
        verified (challenge/->verified secret zk.protocol/default)
        body     (encode-body {::credential/verified verified})
        params   {:headers      {"X-API-Key" api-key}
                  :content-type :json
                  :body         body}
        response (http/post endpoint params)]
    (if (= (:status response) 201)
      (update response :body decode-body)
      response)))

(defn get-credential
  "Retrieve public credential data by its persistent identifier, `:nu/id`. The
  `:nu/id` of a given credential is simply an encoded public key.

  NOTE: Public credential data can also be retrieved by alternative addresses,
  such as a ledger transaction id, IPFS hash, torrent address, etc.. The NuID
  Auth API aims to give these persistence facilities a unified interface to
  facilitate easy retrieval for developers. Credential data can also be
  retrieved directly from any persistence abstraction with public read
  semantics."
  [{::keys [api-key api-root-url] :nu/keys [id]}]
  (let [endpoint (str api-root-url "/credential/" id)
        params   {:headers {"X-API-Key" api-key}}
        response (http/get endpoint params)]
    (if (= (:status response) 200)
      (update response :body decode-body)
      response)))

(defn post-challenge
  "Issue a short-lived, time-bound challenge against public credential data. The
  challenge can be used to create a stateless authentication flow for
  persistent, cross-service identities.

  NOTE: This endpoint considers any well-formed credential valid input. The
  credential needn't be registered through the NuID Auth API, e.g. using
  `post-credential`, and needn't be persisted at all. This allows the
  `/challenge` endpoint to serve OTP and ephemeral identity use-cases in
  addition to traditional login."
  [{::keys [api-key api-root-url] :nuid/keys [credential]}]
  (let [endpoint (str api-root-url "/challenge")
        body     (encode-body {:nuid/credential credential})
        params   {:headers      {"X-API-Key" api-key}
                  :content-type :json
                  :body         body}
        response (http/post endpoint params)]
    (if (= (:status response) 201)
      (update response :body decode-body)
      response)))

(defn post-verify
  "Verify a `nuid.credential/proof` derived from a given challenge as returned
  by `/challenge`.

  NOTE: Currently, the NuID Auth API supports a JWT-based flow, but in-built
  support for OAuth, OIDC, and other standards-based protocols are on the
  immediate roadmap."
  [{::keys [api-key api-root-url] ::challenge/keys [jwt] ::credential/keys [proof]}]
  (let [endpoint (str api-root-url "/challenge/verify")
        body     (encode-body {::challenge/jwt jwt ::credential/proof proof})
        params   {:headers      {"X-API-Key" api-key}
                  :content-type :json
                  :body         body}]
    (http/post endpoint params)))

(comment

  (def opts
    {::api-root-url "https://auth.nuid.io"
     ::api-key      "YOUR-API-KEY-HERE"
     ::secret       "testSecret123"})

  (def post-credential-response
    (post-credential opts))

  (def get-credential-response
    (let [id   (get-in post-credential-response [:body :nu/id])
          opts (assoc opts :nu/id id)]
      (get-credential opts)))

  (def post-challenge-response
    (let [credential (get-in get-credential-response [:body :nuid/credential])
          opts       (assoc opts :nuid/credential credential)]
      (post-challenge opts)))

  (def post-verify-response
    "NOTE: the `nuid.credential.challenge/jwt` from `post-challenge-response`
    expires after 5 seconds by default. The following will result in a `400` if
    the JWT has expired, which makes this invocation time-sensitive with respect
    to evaluating `post-challenge-response`."
    (let [jwt       (get-in post-challenge-response [:body ::challenge/jwt])
          challenge (challenge/<-jwt jwt)
          proof     (challenge/->proof (::secret opts) challenge)
          opts      (assoc opts ::challenge/jwt jwt ::credential/proof proof)]
      (try
        (post-verify opts)
        (catch Exception e
          (if (= (:status (ex-data e)) 400)
            (prn "Response status 400. NOTE: JWT may have expired.")
            (throw e))))))

  )
