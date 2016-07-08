# Twitter Followers Data Fetcher

[![Build Status](https://travis-ci.org/draffensperger/twitter-fetch.svg?branch=master)](https://travis-ci.org/draffensperger/twitter-fetch) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/3a82792bcc1c475c87f0977e831562fe)](https://www.codacy.com/app/d-raffensperger/twitter-fetch?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=draffensperger/twitter-fetch&amp;utm_campaign=Badge_Grade)

Twitter has a great API for fetching followers, but it's rate limited (which
makes sense). So if you want to do analysis on large numbers of Twitter
followers it's nice to home something that you can run in the background and
leave alone for a while.

This particular Twitter fetcher stores Twitter followers in the Google Cloud
Datastore.

## Deploying

In order to deploy this you will need to convert your key to Base64 format.
You can do that using a command like this: `cat YourKey.p12 | base64`.

This is built as a docker container https://hub.docker.com/r/draffensperger/twitter-fetch/ and a simple way to deploy this is to use any hosting service that will run Docker containers. For instance, you can use Digital Ocean's 1-click docker image.

Here's an example of the commands you would need to do the deployment assuming
you have a server that has docker installed. The first thing to do is to set
up a `prod.secrets.env` file with the configuration needed.

Then you can run the following to get the app running using docker:

```
DROPLET_IP={your droplet ip address}
scp prod.secrets.env root@$DROPLET_IP:/root

ssh root@"$DROPLET_IP"
docker pull draffensperger/twitter-fetch
docker run --env-file prod.secrets.env draffensperger/twitter-fetch &
```

## Developing and testing locally

You can run run a local datastore emulator. To access the admin interface you
can go to `http://localhost:8158/_ah/admin`.

CONFIG_FILE=development.secrets bin/run_fetcher
CONFIG_FILE=development.secrets bin/run_command retrieve-followers 196399788

## Setting up a local development environment

## Community Analysis

The eventual goal is to use this fetched Twitter data for community analysis. 

https://en.wikipedia.org/wiki/Community_structure#Algorithms_for_finding_communities

## Potential improvements

These things could be improved in the app
- debug the rate limiting stuff: just make it log lots of stuff
- tests not running
- get this deployed already
- dependencies not pulling in
- upgrade to v1beta3 api
- make docker build process more efficient
- improve code style as per the Codacy feedback
- datastore is about 10x as expensive as just cloud files
- use ExecutorService instead of simple threads
- use ScheduledExecutorService and make it clean to do a shutdown
