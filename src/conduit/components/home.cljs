(ns conduit.components.home
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
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
  [{:keys [author createdAt favoritesCount title description slug]}]
  (let [{:keys [image username]} author]
    [:div.article-preview
     [:div.article-meta
      [:a {:href "profile.html"}
       [:img {:src image}]]
      [:div.info
       [:a.author {:href ""} username]
       [:span.date (-> createdAt js/Date. .toDateString)]]
      (base/Button {:icon "ion-heart"} favoritesCount)]
     [:a.preview-link {:href (str "#/article/" slug)}
      [:h1 title]
      [:p description]
      [:span "Read more..."]]]))


(rum/defc TagItem [tag]
  [:a.tag-pill.tag-default {:href (str "#/tag/" tag)}
   tag])

(rum/defc SideBar [r tags]
  [:div.sidebar
   [:p "Popular Tags"]
   [:div.tag-list
    (map #(rum/with-key (TagItem %) %) tags)]])


(rum/defc PageItem [label page]
  [:li.page-item
   (case label
     page {:class "active"}
     "..." {:class "disabled"}
     nil)
   [:a.page-link label]])

(rum/defc Pagination [{:keys [page pages-count]}]
  (when-not (zero? pages-count)
    [:nav
     (when (> page 1) (PageItem "←" page))
     (for [p (range page (+ page 5))]
       (PageItem p page))
     (PageItem "..." page)
     (for [p (range (- pages-count 4) (inc pages-count))]
       (PageItem p page))]))


(rum/defc Page [r {:keys [articles pagination tags tabs]}]
  [:div.container.page
   (grid/Row
     (grid/Column 9
       (FeedToggle tabs)
       (map #(rum/with-key (ArticlePreview %) (:createdAt %)) articles)
       (Pagination pagination))
     (grid/Column 3
       (SideBar r tags)))])


(rum/defc Layout [r data]
  [:div.home-page
   (Banner)
   (Page r data)])


(rum/defc Home <
  rum/static
  rum/reactive
  (router/mixin {:tag-articles :reset
                 :articles :load
                 :tags :load})
  [r route {:keys [id]}]
  (let [articles (rum/react (scrum/subscription r [:articles]))
        tag-articles (rum/react (scrum/subscription r [:tag-articles]))
        tags (rum/react (scrum/subscription r [:tags]))
        articles
        (cond
          (and (= route :home) id) tag-articles
          (= route :home) articles)]
    [:div
     (Header r :home)
     (Layout r {:articles (:articles articles)
                :pagination
                {:pages-count (:pages-count articles)
                 :page (:page articles)}
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
     (Footer)]))
