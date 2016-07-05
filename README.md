# Twitter Followers Data Fetcher

[![Build Status](https://travis-ci.org/draffensperger/twitter-fetch.svg?branch=master)](https://travis-ci.org/draffensperger/twitter-fetch) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/3a82792bcc1c475c87f0977e831562fe)](https://www.codacy.com/app/d-raffensperger/twitter-fetch?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=draffensperger/twitter-fetch&amp;utm_campaign=Badge_Grade)

Twitter has a great API for fetching followers, but it's rate limited (which
makes sense). So if you want to do analysis on large numbers of Twitter
followers it's nice to home something that you can run in the background and
leave alone for a while.

This particular Twitter fetcher stores Twitter followers in the Google Cloud
Datastore.

## Deploying

cat key_base64.txt | base64 -D > key2.p12
keytool -list -keystore key2.p12 -storetype PKCS12 
keytool -list -keystore TwitterCommunities-207475cbab9e.p12 -storetype PKCS12 


http://localhost:8158/_ah/admin

## Setting up a local development environment

CONFIG_FILE=development.secrets bin/run_fetcher
CONFIG_FILE=development.secrets bin/run_command retrieve-followers 196399788


## Community Analysis

https://en.wikipedia.org/wiki/Community_structure#Algorithms_for_finding_communities


## Potential improvements

issues to deal with:
- tests not running
- get this deployed already
- dependencies not pulling in
- upgrade to v1beta3 api
- make docker build process more efficient
- improve code style as per the Codacy feedback

