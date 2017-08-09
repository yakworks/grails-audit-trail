#!/usr/bin/env bash

set -e

rm -rf ./audit-trails-plugin/build
rm -rf ./demo-app/build

echo "### Running plugin tests ###"
(cd ./audit-trail-plugin && ./gradlew clean check assemble --stacktrace)

echo "### Running demo app tests ###"
(cd ./audit-trail-plugin && ./gradlew clean check --stacktrace)

if [[ $TRAVIS_BRANCH == 'master' && $TRAVIS_REPO_SLUG == "9ci/grails-audit-trail" && $TRAVIS_PULL_REQUEST == 'false' ]]; then
	echo "### publishing plugin Bintray"
	(cd ./audit-trail-plugin && ./gradlew bintrayUpload)

else
  echo "Not on master branch, so not publishing"
  echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
  echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
fi