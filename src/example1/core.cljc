(ns example1.core
  (:gen-class)
  (:require
   [fierycod.holy-lambda-ring-adapter.core :as hlra]
   [fierycod.holy-lambda.core :as h]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.coercion :as coercion]
   [muuntaja.core :as m]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.coercion.spec]
   [ring.util.http-response :as response]))

(defn sample-handler
  "Sample ring handler"
  [request]
  (response/ok {:message "Hello World from the ring handler!"}))

(def routes
  [["/" {:get sample-handler}]])

(def middlewares
  [;;query params and form params
   parameters/parameters-middleware
   ;;content negotiation
   muuntaja/format-negotiate-middleware
   ;;encoding response body
   muuntaja/format-response-middleware
   ;;exception handling
   exception/exception-middleware
   ;;decoding request body
   muuntaja/format-request-middleware
   ;;coercing response body
   coercion/coerce-response-middleware
   ;;coercing request body
   coercion/coerce-request-middleware
   ;;multipart params
   multipart/multipart-middleware])

(def app
  (ring/ring-handler
   (ring/router
    routes
    {:data {:coercion reitit.coercion.spec/coercion
            :middleware middlewares
            :muuntaja m/instance}})))

(def HttpApiProxyGateway (hlra/ring<->hl-middleware app))

;; Specify the Lambda's entry point as a static main function when generating a class file
(h/entrypoint [#'HttpApiProxyGateway])

;; Executes the body in a safe agent context for native configuration generation.
;; Useful when it's hard for agent payloads to cover all logic branches.
