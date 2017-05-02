(ns conduit.components.base
  (:require [rum.core :as rum]))

(rum/defc Icon [icon]
  [:i {:class (str "ion-" (name icon))}])

(rum/defc Button
  ([label]
   (Button {} label))
  ([{:keys [icon class type]} label]
   [:button.btn.btn-sm
    {:class
     (case type
       :primary "btn-outline-primary"
       :secondary "btn-outline-secondary"
       "btn-outline-primary")}
    (when icon (Icon icon))
    (when icon " ")
    label]))

(rum/defc ArticleMeta [{:keys [image username createdAt]} & children]
  [:header.article-meta
   [:a {:href "profile.html"}
    [:img {:src image}]]
   [:div.info
    [:a.author {:href ""} username]
    [:span.date (-> createdAt js/Date. .toDateString)]]
   children])

(rum/defc Tags [tags]
  [:ul.tags-list
   (->> tags
        (map (fn [tag]
               [:li.tag-default.tag-pill.tag-outline
                {:key tag}
                [:a {:href (str "#/tag/" tag)}
                 tag]])))])
