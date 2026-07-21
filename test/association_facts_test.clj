(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir f & xs] (ir/execute kir f (vec xs)))
(defn present [x] (when (second x) (nth x 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url" "url-provenance"
             "established-date" "last-revised-date" "retrieved-at"])
(def expected
  [{"id" "ctia.consumer-code-for-wireless-service" "title" "Consumer Code for Wireless Service"
    "association" "ctia" "isic" "6120" "country" "USA" "kind" "self-regulatory-code"
    "url" "https://api.ctia.org/wp-content/uploads/2020/03/CTIA-Consumer-Code-2020.pdf"
    "url-provenance" "official-association-site" "established-date" nil "last-revised-date" "2020-03" "retrieved-at" "2026-07-15"}
   {"id" "ctia.smartphone-anti-theft-voluntary-commitment" "title" "Smartphone Anti-Theft Voluntary Commitment"
    "association" "ctia" "isic" "6120" "country" "USA" "kind" "self-regulatory-code"
    "url" "https://api.ctia.org/docs/default-source/default-document-library/stolen-phone-commitment.pdf"
    "url-provenance" "official-association-site" "established-date" nil "last-revised-date" "2016-07-26" "retrieved-at" "2026-07-15"}])
(deftest reference-preserves-authority
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "ctia" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= ["2020-03" "2016-07-26"] (mapv #(present (call kir 'entry-field "ctia" % "last-revised-date")) [0 1])))
    (is (= [["consumer-protection" "fair-transaction"] ["consumer-protection" "device-security"]]
           (mapv (fn [i] (mapv #(present (call kir 'topic "ctia" i %)) [0 1])) [0 1])))
    (is (= ["ctia.consumer-code-for-wireless-service" "ctia.smartphone-anti-theft-voluntary-commitment"]
           (mapv #(present (call kir 'by-topic-id "ctia" "consumer-protection" %)) [0 1])))
    (is (= #{} (set (:effects kir))))
    (testing "fail closed" (is (zero? (call kir 'entry-count "gsma")))
      (is (nil? (present (call kir 'entry-field "ctia" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "ctia" 0 "established-date"))))
      (is (nil? (present (call kir 'topic "ctia" 0 2))))
      (is (zero? (call kir 'by-topic-count "ctia" "labor")))
      (is (nil? (present (call kir 'by-topic-id "ctia" "consumer-protection" 2)))))))
(defn compiler-root [] (nth (iterate #(.getParent ^java.nio.file.Path %)
  (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [x] (.encodeToString (java.util.Base64/getEncoder) x))
(deftest restricted-js-and-wasm-conform-semantically
  (let [js (compiler/compile-source source :js-kotoba-v1) wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source js) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        p (shell/sh "node" "--input-type=module" "-e"
            (str "import(process.argv[1]).then(async h=>{const j=await import('data:text/javascript;base64," js64 "');const w=await h.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const r=x=>{if(x['entry-field']('ctia',0n,'last-revised-date')[2]!=='2020-03'||x['entry-field']('ctia',1n,'last-revised-date')[2]!=='2016-07-26')throw Error('dates');if(x['by-topic-count']('ctia','consumer-protection')!==2n||x['by-topic-id']('ctia','consumer-protection',1n)[2]!=='ctia.smartphone-anti-theft-voluntary-commitment')throw Error('topics');};r(j.instantiateKotoba({}));r(w.instance.exports)}).catch(e=>{console.error(e);process.exit(99)})")
            (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit p)) (str (:out p) (:err p)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"] (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
