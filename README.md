[![Build Status](https://travis-ci.org/draffensperger/twitter-fetch.svg?branch=master)](https://travis-ci.org/draffensperger/twitter-fetch)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3a82792bcc1c475c87f0977e831562fe)](https://www.codacy.com/app/d-raffensperger/twitter-fetch?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=draffensperger/twitter-fetch&amp;utm_campaign=Badge_Grade)

# Twitter Followers Data Fetcher

Twitter has a great API for fetching followers, but it's rate limited (which
makes sense). So if you want to do analysis on large numbers of Twitter
followers it's nice to home something that you can run in the background and
leave alone for a while.

This particular Twitter fetcher stores Twitter followers in the Google Cloud
Datastore.
