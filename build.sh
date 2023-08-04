sbt fullLinkJS
mkdir lib
cp target/scala-3.3.0/jmh-benchmark-action-opt/main.js lib/main.js
yarn run package