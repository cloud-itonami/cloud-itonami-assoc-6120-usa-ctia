# ADR 0001: Kotoba is the CTIA catalog source authority

- Status: Accepted
- Date: 2026-07-21

`src/association_facts.kotoba` is the sole production source. It preserves the
month-precision `2020-03` and complete `2016-07-26` revisions, absent
establishment dates, and both ordered topic pairs. Unknown values and indexes
fail closed; no effects are declared. Reference semantics, restricted
JavaScript, and instantiated typed WebAssembly are CI-qualified. Clojure and
the JVM are compiler/test hosts only.
