(ns git-log
  (:require
    [clojure.string :as str]
    [clojure.java.shell :refer [sh]]))

(defn log-lines [n]
  (-> (apply sh "git" "log" "--stat=9999" "--oneline" (when n (str "-" n)))
    :out
    (str/split #"\n")))

(defn partition-by-commit [lines]
  (partition-by
    (let [commit (atom nil)]
      #(if (re-find #"^ " %)
         @commit
         (reset! commit (re-find #"^[0-9a-f]+" %))))
    lines))

(defn parse-changes [lines]
  (into {}
    (keep #(let [[filename rst] (str/split % #"\|")
                 num-lines-changed (-> rst
                                     (str/triml)
                                     (str/split #" ")
                                     first)]
             (when-not (= "Bin" num-lines-changed)
               [(str/trim filename) {:commits 1
                                     :lines (Integer/parseInt num-lines-changed)}])))
    (butlast lines)))

(defn group-by-commit [groups]
  (map #(do {(re-find #"^[0-9a-f]+" (first %))
             (parse-changes (rest %))})
    groups))

;; TODO: handle file renames eg: "src/main/dev/fisher/ui/workspaces/{workspace_manager.cljs => workspace.cljs}"
;; TODO: try parsing log data for changes on a var level (defn, etc...)

(defn git-change-info-by-file []
  (->> (log-lines nil)
    (partition-by-commit)
    (group-by-commit)
    (mapcat vals)
    (reduce (partial merge-with (partial merge-with +)))))

(comment
  (->> (git-change-info-by-file)
    (sort-by (comp (or :commits :lines) second))
    (take-last 10))
  )
