#!/usr/bin/env bash

set -e

rm -rf ./audit-trails-plugin/build
rm -rf ./demo-app/build

echo "### Running plugin tests ###"
(cd ./audit-trail-plugin && ./gradlew clean check assemble --stacktrace --no-daemon)

echo "### Running demo app tests ###"
(cd ./audit-trail-plugin && ./gradlew clean check --stacktrace --no-daemon)

if [[ -n $TRAVIS_TAG ]] || [[ $TRAVIS_BRANCH == 'master' && $TRAVIS_REPO_SLUG == "9ci/grails-audit-trail" && $TRAVIS_PULL_REQUEST == 'false' ]]; then

    if [[ -n $TRAVIS_TAG ]]
    then
        echo "### publishing release to BinTray"
        (cd ./audit-trail-plugin && ./gradlew bintrayUpload --no-daemon)
    else
         echo "### publishing snapshot"
         (cd ./audit-trail-plugin && ./gradlew publish --no-daemon)
    fi

else
  echo "Not a Tag or Not on master branch, not publishing"
  echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
  echo "TRAVIS_TAG: $TRAVIS_TAG"
  echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
fi