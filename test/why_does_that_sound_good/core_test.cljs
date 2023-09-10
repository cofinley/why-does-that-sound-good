(ns why-does-that-sound-good.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [why-does-that-sound-good.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
