resolvers += Resolver.url("Marmeladburk releases", url("http://releases.marmeladburk.fidesmo.com"))(Resolver.ivyStylePatterns)

resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("com.fidesmo" % "base-project" % "0.1.7")

addSbtPlugin("com.hanhuy.sbt" % "android-sdk-plugin" % "1.5.7")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
