(ns conduit.routes)

(def routes
  ["/" [["" :home]
        [["page/" :page] :home]
        [["tag/" :id] [["" :tag]
                       [["/page/" :page] :tag]]]
        [["article/" :id] :article]
        ["editor" [["" :editor]
                   [["/" :slug] :editor]]]
        ["login" :login]
        ["register" :register]]])
