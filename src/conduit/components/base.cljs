(ns conduit.components.base
  (:require [rum.core :as rum]))

(rum/defc Icon [icon]
  [:i {:class (str "ion-" (name icon))}])

(defn- btn-class [class type size outline?]
  (str
    class
    " "
    (case size
      :L "btn-lg"
      "btn-sm")
    " "
    (case type
      :secondary (str "btn-" (if outline? "outline-secondary" "secondary"))
      (str "btn-" (if outline? "outline-primary" "primary")))))

(rum/defc Button
  ([label]
   (Button {} label))
  ([{:keys [icon class type size outline? disabled? on-click]} label]
   [:button.btn
    {:class    (btn-class class type size outline?)
     :disabled disabled?
     :on-click on-click}
    (when icon (Icon icon))
    (when icon " ")
    label]))

(rum/defc ArticleMeta [{:keys [image username createdAt]} & children]
  [:header.article-meta
   [:a {:href "profile.html"}
    [:img {:src image}]]
   [:div.info
    [:a.author {:href (str "#/profile/" username)} username]
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

(rum/defc FollowButton [username following? on-follow on-unfollow]
  (Button
   {:icon     :plus-round
    :type     :secondary
    :on-click (if following? on-unfollow on-follow)}
   (if following?
     (str "Unfollow " username " ")
     (str "Follow " username " "))))
