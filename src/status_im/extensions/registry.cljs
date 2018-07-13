(ns status-im.extensions.registry
  (:require [pluto.reader :as reader]
            [pluto.registry :as registry]
            [pluto.storage :as storage]
            [pluto.storage.gist :as gist]
            [status-im.extensions.core :as extension]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.ui.components.react :as react]))

(def components
  {'view react/view
   'text react/text})

(def capacities
  {:components components
   :events     [{:name 'events/status.wallet.send}]
   :hooks      {'hooks/status.collectibles {:properties {:name     :string
                                                         :symbol   :keyword
                                                         :view     :view
                                                         :contract :string}}
                'hooks/status.chat.commands {:properties {:scope         #{:personal-chats :public-chats}
                                                          :description   :string
                                                          :short-preview :view
                                                          :preview       :view
                                                          :parameters    [{:id           :keyword
                                                                           :type         {:one-of #{:text :phone :password :number}}
                                                                           :placeholder  :string
                                                                           :suggestions? :view}]}}}})

(def registry (registry/new-registry))

(defn active? [m]
  (= :pluto.registry/active (:pluto.registry/state m)))

(defn activate! [id]
  (registry/activate! registry id))

(defn deactivate! [id]
  (registry/deactivate! registry id))

(defn collectibles []
  (registry/hooks registry 'hooks/status.collectibles))

(defn command-hook->command [[id {:keys [description scope parameters preview short-preview]}]]
  (reify protocol/Command
    (id [_] (name id))
    (scope [_] scope)
    (description [_] description)
    (parameters [_] parameters)
    (validate [_ _ _])
    (on-send [_ _ _])
    (on-receive [_ _ _])
    (short-preview [_ o] (short-preview o))
    (preview [_ o] (preview o))))

(defn all []
  (registry/all registry))

(defn chat-commands []
  (map command-hook->command (registry/hooks registry 'hooks/status.chat.commands)))

(defn install [id m]
  (try
    (let [{:keys [data errors]} (reader/parse {:capacities capacities} m)]
      (when errors
        (println "Failed to parse status extensions" errors))
      (registry/add! registry id data)
      (registry/activate! registry id))
    (catch :default e (println "EXC" e))))

(def storages
  {:gist (gist/GistStorage.)})

(defn read-extension [o]
  (-> o :value first :content reader/read))

(defn load-from [url f]
  (let [[type id] (extension/url->storage-details url)
        storage   (get storages type)]
    (when (and storage id)
      (storage/fetch storage
                     {:value id}
                     #(f id %)))))
