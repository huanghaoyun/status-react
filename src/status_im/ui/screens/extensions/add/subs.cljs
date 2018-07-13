(ns status-im.ui.screens.extensions.add.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :get-extension
 (fn [db]
   (:extension db)))
