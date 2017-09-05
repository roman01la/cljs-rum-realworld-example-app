(ns conduit.components.profile
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.mixins :as mixins]
            [conduit.components.base :as base]))

(rum/defc Banner
  [{:keys [image username bio following]} current-user on-follow on-unfollow]
  (let [profile-owner? (= (:username current-user) username)]
    [:.col-xs-12.col-md-10.offset-md-1
     (when-not (empty? image) [:img.user-img {:src image}])
     [:h4 username]
     [:p bio]
     [:.pull-xs-right
      (when current-user
        (if-not profile-owner?
          (base/FollowButton {:username username
                              :following? following
                              :on-follow on-follow
                              :on-unfollow on-unfollow})
          (base/Button {:href (str "#/settings")
                        :icon :gear-a
                        :type :secondary}
                       "Edit Profile Settings")))]]))

(rum/defc Profile < rum/reactive
  (mixins/dispatch-on-mount
    (fn [_ _ {:keys [id]} {:keys [token]}]
      {:profile [:load-profile id token]}))
  [r route params current-user]
  (let [{:keys [profile loading?]} (rum/react (citrus/subscription r [:profile]))
        {:keys [image username bio following]} profile
        user-token (:token current-user)
        on-follow #(citrus/dispatch! r :profile :follow username user-token {:dispatch [:profile :update]})
        on-unfollow #(citrus/dispatch! r :profile :unfollow username user-token {:dispatch [:profile :update]})]
    (when (and profile (not loading?))
      [:.profile-page
       [:.user-info
        [:.container
         [:.row
          (Banner profile current-user on-follow on-unfollow)
          [:.container
           [:.row
            [:.col-xs-12.col-md-10.offset-md-1]]]]]]])))
