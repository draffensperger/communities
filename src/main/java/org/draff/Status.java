package org.draff;

import java.util.Date;

/**
 * Created by dave on 1/7/16.
 */
public class Status {
  long id;
  long userId;
  long retrievedAt;

  Date createdAt;
  String text;
  String source;
  boolean isTruncated;
  long inReplyToStatusId;
  long inReplyToUserId;
  String inReplyToScreenName;
  boolean isFavorited;
  boolean isRetweeted;
  long favoriteCount;
  boolean isRetweet;
  long retweetCount;
  boolean isRetweetedByMe;
  long currentUserRetweetId;
  boolean isPossiblySensitive;
  String lang;

  long[] contributors;
  String[] withheldInCountries;
  Scopes Scopes;
  GeoLocation geoLocation;
  Place place;
  Status retweetedStatus;

  public void setFieldsFromTwitterStatus(twitter4j.Status s) {
    Date getCreatedAt();
    long getId();
    String getText();
    String getSource();
    boolean isTruncated();
    long getInReplyToStatusId();
    long getInReplyToUserId();
    String getInReplyToScreenName();
    GeoLocation getGeoLocation();
    Place getPlace();
    boolean isFavorited();
    boolean isRetweeted();
    long getFavoriteCount();
    User getUser();
    boolean isRetweet();
    Status getRetweetedStatus();
    long[] getContributors();
    long getRetweetCount();
    boolean isRetweetedByMe();
    long getCurrentUserRetweetId();
    boolean isPossiblySensitive();
    String getLang();
    Scopes getScopes();
    String[] getWithheldInCountries();
  }
}
