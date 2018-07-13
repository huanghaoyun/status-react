(ns status-im.ui.screens.extensions.add.events
  (:require [re-frame.core :as re-frame]
            [status-im.extensions.registry :as registry]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :extension/store
 (fn [{:keys [db]} [_ id m]]
   {:db (assoc db :extension {:id id :data m})}))

(re-frame/reg-fx
 ::load
 (fn [url]
   (registry/load-from url #(re-frame/dispatch [:extension/store %1 (registry/read-extension %2)]))))

(re-frame/reg-fx
 :extension/load-and-show
 (fn [url]
   (registry/load-from url #(re-frame/dispatch [:extension/show {:id %1 :data (registry/read-extension %2)}]))))

(handlers/register-handler-fx
 :extension/load
 (fn [_ [_ url]]
   {::load url}))

(re-frame/reg-fx
 ::toggle-activation
 (fn [[id m]]
   (if (registry/active? m)
     (registry/deactivate! id)
     (registry/activate! id))))

(handlers/register-handler-fx
 :extensions/toggle-activation
 (fn [_ [_ id m]]
   {::toggle-activation [id m]}))

(re-frame/reg-fx
 ::install
 (fn [[id ext]]
   (registry/install id ext)
   (when-let [commands (seq (registry/chat-commands))]
     (re-frame/dispatch [:load-commands commands]))))

(handlers/register-handler-fx
 :extension/install
 (fn [_ [_ id ext]]
   {::install [id ext]
    :show-confirmation {:title     (i18n/label :t/success)
                        :content   (i18n/label :t/extension-installed)
                        :on-accept #(re-frame/dispatch [:navigate-to-clean :home])
                        :on-cancel nil}}))

(handlers/register-handler-fx
 :extension/show
 (fn [_ [_ ext]]
   {:dispatch [:navigate-to :show-extension ext]}))
