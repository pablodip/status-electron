(ns status-im.react-native.js-dependencies)

(def action-button          #js {:default #js {:Item #js {}}})
(def android-sms-listener   #js {})
(def autolink               #js {:default #js {}})
(def config                 #js {:default #js {}})
(def camera                 #js {:constants #js {}})
(def circle-checkbox        #js {})
(def contacts               #js {})
(def dialogs                #js {})
(def dismiss-keyboard       #js {})
(def drawer                 #js {})
(def emoji-picker           #js {:default #js {}})
(def fs                     #js {})
(def http-bridge            #js {})
(def i18n                   #js {})
(def image-crop-picker      #js {})
(def image-resizer          #js {})
(def instabug               #js {})
(def invertible-scroll-view #js {})
(def linear-gradient        #js {})
(def mapbox-gl              #js {:setAccessToken (fn [])})
(def nfc                    #js {})
(def orientation            #js {})
(def popup-menu             #js {})
(def qr-code                #js {})
(def random-bytes           #js {})
(def react-native
  #js {:NativeModules      #js {}
       ;;TODO temporary using ios design for desktop, later desktop should be implemented (atm we have only ios or android)
       :Platform           #js {:OS "ios"}
       :Animated           #js {:View #js {}
                                :Text #js {}}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :Dimensions         #js {:get  (fn [])}})
(def realm                  #js {:schemaVersion (fn [])
                                 :close         (fn [])})
(def sortable-listview      #js {})
(def swiper                 #js {})
(def vector-icons           #js {:default #js {}})
(def webview-bridge         #js {:default #js {}})
(def svg                    #js {:default #js {}})
(def react-native-fcm       #js {:default #js {}})


