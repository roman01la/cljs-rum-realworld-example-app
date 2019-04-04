(ns conduit.components.home
  (:require [rum.core :as rum]
            [bidi.bidi :as bidi]
            [citrus.core :as citrus]
            [conduit.mixins :as mixins]
            [conduit.routes :refer [routes]]
            [conduit.components.grid :as grid]
            [conduit.components.base :as base]))

(rum/defc Banner []
  [:div.banner
   [:div.container
    [:h1.logo-font "conduit"]
    [:p "A place to share your knowledge."]]])


(rum/defc FeedToggleItem [{:keys [label active? disabled? link icon]}]
  [:li.nav-item
   [:a.nav-link
    {:href link
     :class
           (cond
             active? "active"
             disabled? "disabled"
             :else nil)}
    (when icon
      [:i {:class icon}])
    label]])

(rum/defc FeedToggle [tabs]
  [:div.feed-toggle
   [:ul.nav.nav-pills.outline-active
    (map #(rum/with-key (FeedToggleItem %) (:label %)) tabs)]])

(rum/defc ArticlePreview < rum/reactive
  [r article]
  (let [{:keys [author createdAt favoritesCount title description slug tagList favorited]} article
        {:keys [image username]} author
        token (rum/react (citrus/subscription r [:user :token]))
        {route :handler} (rum/react (citrus/subscription r [:router]))
        [on-favorite-success-handler
         on-unfavorite-success-handler] (if (= route :tag)
                                          [{:dispatch [:tag-articles :update slug :article]}
                                           {:dispatch [:tag-articles :update slug :article]}]
                                          [{:dispatch [:articles :update slug :article]}
                                           {:dispatch [:articles :update slug :article]}])
        on-favorite #(citrus/dispatch! r :articles :favorite slug token on-favorite-success-handler)
        on-unfavorite #(citrus/dispatch! r :articles :unfavorite slug token on-unfavorite-success-handler)]
    [:div.article-preview
     (base/ArticleMeta
       {:username  username
        :createdAt createdAt
        :image     image}
       (base/Button
         {:icon     :heart
          :class    "pull-xs-right"
          :on-click (if favorited on-unfavorite on-favorite)
          :outline? (not favorited)}
         favoritesCount))
     [:main
      [:a.preview-link {:href (str "#/article/" slug)}
       [:h1 title]
       [:p description]]]
     [:div.article-footer
      [:a.preview-link {:href (str "#/article/" slug)}
       [:span "Read more..."]]
      (base/Tags tagList)]]))


(rum/defc TagItem [tag]
  [:a.tag-pill.tag-default {:href (str "#/tag/" tag)}
   tag])

(rum/defc SideBar [r tags]
  [:div.sidebar
   [:p "Popular Tags"]
   [:div.tag-list
    (map #(rum/with-key (TagItem %) %) tags)]])


(rum/defc PageItem [page route current-page slug]
  (let [path (apply bidi/path-for (into [routes route] (when slug [:id slug])))]
    [:li.page-item
     (when (= (if (not= js/isNaN current-page) current-page 1) page)
       {:class "active"})
     [:a.page-link {:href (str "#" (if (= "/" path) "" path) "/page/" page)}
      page]]))

(rum/defc Pagination [{:keys [route page pages-count slug]}]
  (when-not (zero? pages-count)
    [:nav {}
     (map #(rum/with-key (PageItem % route (-> page (or 1) js/parseInt) slug) %)
          (range 1 (inc pages-count)))]))


(rum/defc Page [r {:keys [articles pagination tags tabs loading?]}]
  [:div.container.page
   (grid/Row
     (grid/Column "col-md-9"
                  (FeedToggle tabs)
                  (if (and loading? (nil? (seq articles)))
                    [:div.loader "Loading articles..."]
                    (->> articles
                         (map #(rum/with-key (ArticlePreview r %) (:createdAt %)))))
                  (when-not loading?
                    (Pagination pagination)))
     (grid/Column "col-md-3"
                  (SideBar r tags)))])


(rum/defc Layout [r data]
  [:div.home-page
   (Banner)
   (Page r data)])

(rum/defc -Home < rum/static
  [r route page {:keys [articles loading? pages-count]} tags id]
  (Layout r {:articles articles
             :loading? loading?
             :pagination
                       {:pages-count pages-count
                        :page        page
                        :slug        id
                        :route       route}
             :tags     tags
             :tabs
                       [{:label   "Your Feed"
                         :active? false
                         :link    "#/"}
                        {:label   "Global Feed"
                         :active? (nil? id)
                         :link    "#/"}
                        (when id
                          {:label   (str " " id)
                           :icon    "ion-pound"
                           :active? true})]}))

(rum/defc Home <
  rum/reactive
  (mixins/dispatch-on-mount
    (fn [_ _ {:keys [page]}]
      {:tag-articles [:reset]
       :articles     [:load {:page page}]
       :tags         [:load]}))
  [r route {:keys [page]}]
  (let [articles (rum/react (citrus/subscription r [:articles]))
        tags (rum/react (citrus/subscription r [:tags]))]
    (-Home r route page articles tags nil)))

(rum/defc HomeTag <
  rum/reactive
  (mixins/dispatch-on-mount
    (fn [_ _ {:keys [id page]}]
      {:tag-articles [:load {:tag id :page page}]
       :tags         [:load]}))
  [r route {:keys [id page]}]
  (let [tag-articles (rum/react (citrus/subscription r [:tag-articles]))
        tags (rum/react (citrus/subscription r [:tags]))]
    (-Home r route page tag-articles tags id)))
