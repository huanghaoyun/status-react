(ns status-im.ui.screens.extensions.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.extensions.registry :as registry]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.extensions.styles :as styles]))

(def wnode-icon
  [react/view (styles/wnode-icon true)
   [vector-icons/icon :icons/wnode {:color :white}]])

(defn navigate-to-add-extension [wnode-id]
  (re-frame/dispatch [:navigate-to :add-extension wnode-id]))

(defn- render-extension [[id m]]
  [list/list-item-with-checkbox
   {:checked?        (registry/active? m)
    :on-value-change #(re-frame/dispatch [:extensions/toggle-activation id m %])}
   [list/item
    wnode-icon
    [list/item-content
     [list/item-primary (get-in m ['meta :name])]
     [list/item-secondary id]]]])

(defn extensions-settings []
  [react/view {:flex 1}
   [status-bar/status-bar]
   [toolbar/toolbar {}
    toolbar/default-nav-back
    [toolbar/content-title (i18n/label :t/extensions)]
    [toolbar/actions
     [(toolbar.actions/add false (partial navigate-to-add-extension nil))]]]
   [react/view styles/wrapper
    [list/flat-list {:data                    (into [] (registry/all))
                     :default-separator?      false
                     :key-fn                  first
                     :render-fn               render-extension
                     :content-container-style {:flex-grow 1  :justify-content :center}
                     :empty-component         [react/text {:style styles/empty-list}
                                               (i18n/label :t/no-extension)]}]]])
