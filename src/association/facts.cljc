(ns association.facts
  "Industry self-regulatory rule catalog for CTIA -- The Wireless
  Association (Wikidata Q5014574) -- a 14th industry-association-level
  source (see cloud-itonami-assoc-6419-jpn-zenginkyo, -6512-jpn-sonpo,
  -6612-jpn-jsda, -6419-deu-bankenverband, -6612-usa-finra,
  -6512-usa-naic, -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf,
  -6511-jpn-seiho, -6910-jpn-nichibenren, -6810-jpn-recaj,
  -6411-jpn-boj for the first thirteen) per ADR-2607141700
  (cloud-itonami-compliance-fact-federation). The FIRST entry aligned to
  ISIC 6120 (wireless telecommunications activities) -- a new industry
  code for this family. A rule not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.

  www.ctia.org itself failed WebFetch with a TLS certificate error
  (\"unable to verify the first certificate\"), a new failure mode for
  this family (distinct from the 403s and JS-only pages seen elsewhere).
  Rather than fabricate a citation, this catalog pivoted to
  api.ctia.org -- the same organization's own document-hosting
  subdomain -- which served both PDFs cleanly. The Consumer Code for
  Wireless Service PDF was verified by directly reading its rendered
  text (Read tool); the Smartphone Anti-Theft Voluntary Commitment PDF
  was verified the same way, and its embedded PDF metadata additionally
  confirms a document creation date of 2016-07-26.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"ctia"
   [{:association-rule/id "ctia.consumer-code-for-wireless-service"
     :association-rule/title "Consumer Code for Wireless Service"
     :association-rule/association "ctia"
     :association-rule/isic "6120"
     :association-rule/country "USA"
     :association-rule/kind :self-regulatory-code
     :association-rule/url "https://api.ctia.org/wp-content/uploads/2020/03/CTIA-Consumer-Code-2020.pdf"
     :association-rule/url-provenance :official-association-site
     :association-rule/last-revised-date "2020-03"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:consumer-protection :fair-transaction}}
    {:association-rule/id "ctia.smartphone-anti-theft-voluntary-commitment"
     :association-rule/title "Smartphone Anti-Theft Voluntary Commitment"
     :association-rule/association "ctia"
     :association-rule/isic "6120"
     :association-rule/country "USA"
     :association-rule/kind :self-regulatory-code
     :association-rule/url "https://api.ctia.org/docs/default-source/default-document-library/stolen-phone-commitment.pdf"
     :association-rule/url-provenance :official-association-site
     :association-rule/last-revised-date "2016-07-26"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:consumer-protection :device-security}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-6120-usa-ctia Wave 0 (ADR-2607141700): "
                 (count (get catalog "ctia")) " ctia entries seeded with an "
                 "official api.ctia.org citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
