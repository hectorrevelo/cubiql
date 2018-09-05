(ns graphql-qb.types-test
  (:require [clojure.test :refer :all]
            [graphql-qb.types :refer :all]
            [graphql-qb.types.scalars :as scalars]
            [graphql-qb.types :as types]
            [graphql-qb.query-model :as qm])
  (:import [java.net URI]
           [java.util Date]))

(deftest project-result-test
  (testing "Dimension with ref period type"
    (let [dim (->Dimension (URI. "http://dim") 1 ref-period-type)
          period-uri (URI. "http://period")
          period-label "Period label"
          period-start (Date. 112, 0 1)
          period-end (Date. 113 5 13)
          bindings {:dim1 period-uri :dim1label period-label :dim1begintime period-start :dim1endtime period-end}
          expected {:uri period-uri
                    :label period-label
                    :start (scalars/grafter-date->datetime period-start)
                    :end (scalars/grafter-date->datetime period-end)}]
      (is (= expected (project-result dim bindings)))))

  (testing "Dimensions with ref area type"
    (let [dim (->Dimension (URI. "http://dim") 1 (->RefAreaType))
          area-uri (URI. "http://refarea")
          area-label "Area label"
          bindings {:dim1 area-uri :dim1label area-label}]
      (is (= {:uri area-uri :label area-label} (project-result dim bindings)))))

  (testing "Dimension with codelist type"
    (let [type types/enum-type
          dim (->Dimension (URI. "http://dim") 1 type)
          value (URI. "http://val1")
          bindings {:dim1 value}]
      (is (= value (project-result dim bindings)))))

  (testing "Measure matching measure type"
    (let [uri (URI. "http://measure")
          measure (->MeasureType uri 1 true)
          value 4
          bindings {:mp uri :mv 4}]
      (is (= value (project-result measure bindings)))))

  (testing "Measure not matching measure type"
    (let [uri (URI. "http://measure1")
          measure (->MeasureType uri 1 true)
          bindings {:mp (URI. "http://measure2") :mv 5}]
      (is (nil? (project-result measure bindings))))))

(deftest apply-filter-test
  (testing "Ref period dimension"
    (let [dim-uri (URI. "http://dim")
          dim (->Dimension dim-uri 1 ref-period-type)
          starts-after (scalars/parse-datetime "2000-01-01T00:00:00Z")
          ends-after (scalars/parse-datetime "2000-08-01T00:00:00Z")
          filter {:starts_after starts-after
                  :ends_after ends-after}
          model (apply-filter dim qm/empty-model filter)]
      (is (= ::qm/var (qm/get-path-binding-value model [:dim1 :begin :time])))
      (is (= ::qm/var (qm/get-path-binding-value model [:dim1 :end :time]) )))))

