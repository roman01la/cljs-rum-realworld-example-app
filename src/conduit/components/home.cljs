(ns conduit.components.home
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.router :as router]
            [conduit.components.grid :as grid]
            [conduit.components.base :as base]
            [conduit.components.header :refer [Header]]
            [conduit.components.footer :refer [Footer]]))

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

(rum/defc ArticlePreview
  [{:keys [author createdAt favoritesCount title description slug tagList]}]
  (let [{:keys [image username]} author]
    [:div.article-preview
     (base/ArticleMeta
       {:username username
        :createdAt createdAt
        :image image}
       (base/Button {:icon :heart
                     :class "pull-xs-right"}
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


(rum/defc PageItem [label page slug]
  [:li.page-item
   (when (= label page)
     {:class "active"})
   [:a.page-link
    (if slug
      {:href (str "/" slug "/" label)}
      {:href (str "/" label)})
    label]])

(rum/defc Pagination [{:keys [page pages-count slug]}]
  (when-not (zero? pages-count)
    [:nav {}
     (map #(rum/with-key (PageItem % page slug) %)
          (range 1 (inc pages-count)))]))


(rum/defc Page [r {:keys [articles pagination tags tabs loading?]}]
  [:div.container.page
   (grid/Row
     (grid/Column "col-md-9"
       (FeedToggle tabs)
       (if (and loading? (nil? (seq articles)))
         [:div.loader "Loading articles..."]
         (->> articles
           (map #(rum/with-key (ArticlePreview %) (:createdAt %)))))
       (when-not loading?
         (Pagination pagination)))
     (grid/Column "col-md-3"
       (SideBar r tags)))])


(rum/defc Layout [r data]
  [:div.home-page
   (Banner)
   (Page r data)])


(rum/defc -Home < rum/static
  [r {:keys [articles loading? pages-count page]} tags id]
  [:div
   (Header r :home)
   (Layout r {:articles articles
              :loading? loading?
              :pagination
              {:pages-count pages-count
               :page page
               :slug id}
              :tags tags
              :tabs
              [{:label "Your Feed"
                :active? false
                :link "#/"}
               {:label "Global Feed"
                :active? (nil? id)
                :link "#/"}
               (when id
                 {:label (str " " id)
                  :icon "ion-pound"
                  :active? true})]})
   (Footer)])

(rum/defc Home <
  rum/reactive
  (router/mixin
    {:tag-articles :reset
     :articles :load
     :tags :load})
  [r route params]
  (let [articles (rum/react (citrus/subscription r [:articles]))
        tags (rum/react (citrus/subscription r [:tags]))]
    (-Home r articles tags nil)))

(rum/defc HomeTag <
  rum/reactive
  (router/mixin
    {:tag-articles :load
     :tags :load})
  [r route {:keys [id]}]
  (let [tag-articles (rum/react (citrus/subscription r [:tag-articles]))
        tags (rum/react (citrus/subscription r [:tags]))]
    (-Home r tag-articles tags id)))
