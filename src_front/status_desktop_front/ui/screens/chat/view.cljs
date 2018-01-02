(ns status-desktop-front.ui.screens.chat.view
  (:require [status-desktop-front.react-native-web :as react]
            [status-desktop-front.ui.components.tabs :refer [main-tabs]]
            [re-frame.core :as re-frame]
            [status-desktop-front.ui.screens.chat.profile.views :as profile.views]
            [status-desktop-front.ui.components.icons :as icons]
            [status-desktop-front.web3-provider :as protocol]
            [clojure.string :as string]
            [status-im.utils.gfycat.core :as gfycat.core]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.ui.screens.chats-list.styles :as chats-list.styles])
  (:require-macros [status-im.utils.views :as views]))

(defn message [text me? {:keys [outgoing message-id chat-id message-status user-statuses
                                from current-public-key] :as message}]
  (reagent.core/create-class
    {:component-did-mount
     #(when (and message-id
                 chat-id
                 (not outgoing)
                 (not= :seen message-status)
                 (not= :seen (keyword (get-in user-statuses [current-public-key :status]))))
        (re-frame/dispatch [:send-seen! {:chat-id    chat-id
                                         :from       from
                                         :message-id message-id}]))
     :reagent-render
     (fn []
       [react/view {:style {:padding-bottom 8 :padding-horizontal 60 :flex-direction :row :flex 1}}

        (when me?
          [react/view {:style {:flex 1}}])
        [react/view {:style {:padding 12 :background-color :white :border-radius 8}}
         [react/text
          text]]])}))

(views/defview messages-view [chat-id]
  (let [scroll-ref (atom nil)
        messages (re-frame/subscribe [:get-chat-messages chat-id])
        current-public-key (re-frame/subscribe [:get-current-public-key])]
    [react/view {:style {:flex 1 :background-color "#eef2f5"}}
     [react/scroll-view {:onContentSizeChange #(.scrollToEnd @scroll-ref) :ref #(reset! scroll-ref %)}
      [react/view {:style {:padding-vertical 60}}
       (for [[index {:keys [from content] :as message-obj}] (map-indexed vector (reverse @messages))]
         ^{:key index} [message content (= from @current-public-key) message-obj])]]]))

(views/defview status-view []
  [react/view {:style {:flex 1 :background-color "#eef2f5" :align-items :center  :justify-content :center}}
   [react/text {:style {:font-size 18 :color "#939ba1"}}
    "Status.im"]])

(views/defview toolbar-chat-view []
  (views/letsubs [name             [:chat :name]
                  chat-id          [:get-current-chat-id]
                  pending-contact? [:current-contact :pending?]
                  public-key       [:chat :public-key]]
    (let [chat-name (if (string/blank? name)
                      (gfycat.core/generate-gfy public-key)
                      (or name;(get-contact-translated chat-id :name name)
                          "Chat name"))];(label :t/chat-name)))]
      [react/view {:style {:height 64 :align-items :center :padding-horizontal 11 :justify-content :center}}
       [react/text {:style {:font-size 16 :color :black :font-weight "600"}}
        chat-name]
       (when pending-contact?
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:add-pending-contact chat-id])}
          [react/view ;style/add-contact
           [react/text {:style {:font-size 14 :color "#939ba1" :margin-top 3}}
            "Add to contacts"]]])])))
       ;[react/text {:style {:font-size 14 :color "#939ba1" :margin-top 3}}
        ;"Contact status not implemented"]])))

(views/defview chat-view []
  (views/letsubs [current-chat [:get-current-chat]
                  input-text [:chat :input-text]
                  inp-ref (atom nil)]
    [react/view {:style {:flex 1 :background-color "#eef2f5"}}
     [toolbar-chat-view]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [messages-view (:chat-id current-chat)]
     [react/view {:style {:height     90 :margin-horizontal 16 :margin-bottom 16 :background-color :white :border-radius 12
                          :box-shadow "0 0.5px 4.5px 0 rgba(0, 0, 0, 0.04)"}}
      [react/view {:style {:flex-direction :row :margin-horizontal 16 :margin-top 16}}
       [react/view {:style {:flex 1}}
        [react/text-input {:value       (or input-text "")
                           :placeholder "Type a message..."
                           :auto-focus true
                           :ref #(reset! inp-ref %)
                           :on-key-press (fn [e]
                                           (let [native-event (.-nativeEvent e)
                                                 key (.-key native-event)]
                                             (when (= key "Enter")
                                               (js/setTimeout #(.focus @inp-ref) 200)
                                               (re-frame/dispatch [:send-current-message]))))
                           :on-change   (fn [e]
                                          (let [native-event (.-nativeEvent e)
                                                text (.-text native-event)]
                                            (re-frame/dispatch [:set-chat-input-text text])))}]]
       [react/touchable-highlight {:on-press (fn []
                                                (js/setTimeout #(.focus @inp-ref) 200)
                                                (re-frame/dispatch [:send-current-message]))}
        [react/view {:style {:margin-left     16 :width 30 :height 30 :border-radius 15 :background-color "#eef2f5" :align-items :center
                             :justify-content :center}}
         [icons/icon :icons/dropdown-up]]]]]]))

(views/defview new-contact []
  (views/letsubs [new-contact-identity [:get :contacts/new-identity]]
    [react/view {:style {:flex 1 :background-color "#eef2f5"}}
     [react/view {:style {:height 64 :align-items :center :padding-horizontal 11 :justify-content :center}}
      [react/text {:style {:font-size 16 :color :black :font-weight "600"}}
       "Add new contact"]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/view {:style {:height     90 :margin-horizontal 16 :margin-bottom 16 :background-color :white :border-radius 12
                          :box-shadow "0 0.5px 4.5px 0 rgba(0, 0, 0, 0.04)"}}
      [react/view {:style {:flex-direction :row :margin-horizontal 16 :margin-top 16}}
       [react/view {:style {:flex 1}}
        [react/text-input {:placeholder "Public key"
                           :on-change   (fn [e]
                                          (let [native-event (.-nativeEvent e)
                                                text (.-text native-event)]
                                            (re-frame/dispatch [:set :contacts/new-identity text])))}]]
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:add-contact-handler new-contact-identity])}
        [react/view {:style {:margin-left     16 :width 30 :height 30 :border-radius 15 :background-color "#eef2f5" :align-items :center
                             :justify-content :center}}
         [icons/icon :icons/ok]]]]]]))

(defn contact-item [{:keys [whisper-identity name] :as contact}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:open-chat-with-contact contact])}
   [react/view {:style {:padding 12 :background-color :white}}
    [react/text
     name]]])

(views/defview unviewed-indicator [chat-id]
  (let [unviewed-messages-count (re-frame/subscribe [:unviewed-messages-count chat-id])]
    (when (pos? @unviewed-messages-count)
      [react/view {:style (merge chats-list.styles/new-messages-container {:justify-content :center})}
       [react/text {:style chats-list.styles/new-messages-text
                    :font  :medium}
        @unviewed-messages-count]])))

(views/defview chat-list-item-inner-view [{:keys [chat-id name color online
                                                  group-chat contacts public?
                                                  public-key unremovable?] :as chat}]
  (let [name (or name
                 (gfycat/generate-gfy public-key))]
    [react/view {:style {:padding 12 :background-color :white :flex-direction :row :align-items :center}}
     [react/text
      name]
     [react/view {:style {:flex 1}}]
     [unviewed-indicator chat-id]]))

(defn chat-list-item [[chat-id chat]]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat chat-id])}
   [react/view
    [chat-list-item-inner-view (assoc chat :chat-id chat-id)]]])

(views/defview chat-list-view []
  (views/letsubs [chats [:filtered-chats]]
    [react/view {:style {:flex 1 :background-color :white}}
     [react/view {:style {:height 64 :align-items :center :flex-direction :row :padding-horizontal 11}}
      [icons/icon :icons/hamburger]
      [react/view {:style {:flex 1 :margin-horizontal 11 :height 38 :border-radius 8 :background-color "#edf1f3"}}]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
       [icons/icon :icons/add]]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/scroll-view
      [react/view
       (for [[index chat] (map-indexed vector chats)]
         ^{:key (:chat-id chat)} [chat-list-item chat])]]]))

(views/defview contacts-list-view []
  (views/letsubs [contacts [:all-added-group-contacts-filtered nil]]
    [react/view {:style {:flex 1 :background-color "#eef2f5"}}
     [react/view {:style {:height 64 :align-items :center :flex-direction :row :padding-horizontal 11}}
      [icons/icon :icons/hamburger]
      [react/view {:style {:flex 1 :margin-horizontal 11 :height 38 :border-radius 8 :background-color "#edf1f3"}}]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
       [icons/icon :icons/add]]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/scroll-view
      [react/view
       (for [[index contact] (map-indexed vector contacts)]
         ^{:key index} [contact-item contact])]]]))


(views/defview tab-views []
  (views/letsubs [tab [:get :left-view-id]]
    (when tab
      (let [component (case tab
                        :profile profile.views/profile
                        :contact-list contacts-list-view
                        :chat-list chat-list-view
                        react/view)]
        [react/view {:style {:flex 1}}
         [component]]))))

(views/defview main-views []
  (views/letsubs [view-id [:get :view-id]]
    (when view-id
      (let [component (case view-id
                        :chat chat-view
                        :chat-list status-view
                        :new-contact new-contact
                        react/view)]
        [react/view {:style {:flex 1}}
         [component]]))))

(views/defview chat []
  [react/view {:style {:flex 1 :flex-direction :row}}
   [react/view {:style {:width 340 :background-color :white}}
    [react/view {:style {:flex 1}}
     [tab-views]]
    [main-tabs]]
   [main-views]])
