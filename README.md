![ClojureScript + Rum example app](logo.png)


> ### ClojureScript + Rum codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

## Development

- Install Java
- Install Leiningen `brew install leiningen`
- Install rlwrap `brew install rlwrap`
- Install NPM dependencies `npm i`
- Run development server `rlwrap lein figwheel dev`
- Build for production `lein cljsbuild once min`

## Want to contribute?

- Explore [RealWorld](https://github.com/gothinkster/realworld) project repo
- Read [front-end spec](https://github.com/gothinkster/realworld/tree/master/spec#frontend-specs) (requirements) of the project
- Learn [Rum](https://github.com/tonsky/rum/) and [Citrus](https://github.com/roman01la/citrus) libraries
- Choose a task from what's left to do
- Create an issue with description of the task so everyone knows what are you working on
- Follow the code style as much as possible, we want the codebase to be consistent
- Send PRs with small commits for a review, it's easier to understand small changes
- Join our Gitter chat [realworld-dev/clojurescript](https://gitter.im/realworld-dev/clojurescript)
