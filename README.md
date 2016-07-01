[![Build Status](https://travis-ci.org/draffensperger/twitter-fetch.svg?branch=master)](https://travis-ci.org/draffensperger/twitter-fetch)

# Twitter Followers Data Fetcher

Twitter has a great API for fetching followers, but it's rate limited (which
makes sense). So if you want to do analysis on large numbers of Twitter
followers it's nice to home something that you can run in the background and
leave alone for a while.

This particular Twitter fetcher stores Twitter followers in the Google Cloud
Datastore.
