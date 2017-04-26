(ns conduit.components.home
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [conduit.api :as api]
            [conduit.components.grid :as grid]
            [conduit.components.base :as base]
            [conduit.components.header :refer [Header]]
            [conduit.components.footer :refer [Footer]]))

(rum/defc Banner []
  [:div.banner
   [:div.container
    [:h1.logo-font "conduit"]
    [:p "A place to share your knowledge."]]])


(rum/defc FeedToggleItem [{:keys [label active? disabled?]}]
  [:li.nav-item
   [:a.nav-link
    {:href ""
     :class
     (cond
       active? "active"
       disabled? "disabled"
       :else nil)}
    label]])

(rum/defc FeedToggle []
  [:div.feed-toggle
   [:ul.nav.nav-pills.outline-active
    (FeedToggleItem {:label "Your Feed"
                     :disabled? true})
    (FeedToggleItem {:label "Global Feed"
                     :active? true})]])


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


(rum/defc Page [r {:keys [articles tags]}]
  [:div.container.page
   (grid/Row
     (grid/Column 9
       (FeedToggle)
       (map #(rum/with-key (ArticlePreview %) (:createdAt %)) articles))
     (grid/Column 3
       (SideBar r tags)))])


(rum/defc Layout [r data]
  [:div.home-page
   (Banner)
   (Page r data)])


(rum/defc Home <
  (api/mixin {:articles :articles
              :tags :tags})
  rum/reactive
  [r]
  (let [articles (rum/react (scrum/subscription r [:articles] :global))
        tags (rum/react (scrum/subscription r [:tags]))]
    [:div
     (Header r :home)
     (Layout r {:articles articles
                :tags tags})
     (Footer)]))
