package org.draff;

import org.draff.objectdb.Model;

import java.util.Date;

/**
 * Created by dave on 1/7/16.
 */
public class UserDetail implements Model {
  long id;
  long retrievedAt;
  String screenName;
  String location;
  String description;
  String url;
  long statusesCount;
  long listedCount;
  long followersCount;
  long favouritesCount;
  long utcOffset;
  long friendsCount;
  Date createdAt;
  String timeZone;
  String lang;
  boolean isGeoEnabled;
  boolean isVerified;
  boolean isTranslator;
  boolean isContributorsEnabled;
  boolean isProtected;
  String profileImageURL;
  String profileBackgroundColor;
  String profileTextColor;
  String profileLinkColor;
  String profileSidebarFillColor;
  String profileSidebarBorderColor;
  boolean isProfileUseBackgroundImage;
  boolean isDefaultProfile;
  boolean isShowAllInlineMedia;
  boolean isDefaultProfileImage;
  String profileBackgroundImageURL;
  String profileBannerURL;
  boolean isProfileBackgroundTiled;

  UserDetail() {}
  UserDetail(twitter4j.User user) {
    setFieldsFromTwitterUser(user);
  }

  void setFieldsFromTwitterUser(twitter4j.User u) {
    retrievedAt = System.currentTimeMillis();

    id = u.getId();
    screenName = u.getScreenName();
    location = u.getLocation();
    description = u.getDescription();
    url = u.getURL();

    statusesCount = u.getStatusesCount();
    listedCount = u.getListedCount();
    followersCount = u.getFollowersCount();
    favouritesCount = u.getFavouritesCount();
    utcOffset = u.getUtcOffset();
    friendsCount = u.getFriendsCount();

    createdAt = u.getCreatedAt();
    timeZone = u.getTimeZone();
    lang = u.getLang();
    isGeoEnabled = u.isGeoEnabled();
    isVerified = u.isVerified();
    isTranslator = u.isTranslator();
    isContributorsEnabled = u.isContributorsEnabled();
    isProtected = u.isProtected();

    profileImageURL = u.getProfileImageURL();
    profileBackgroundColor = u.getProfileBackgroundColor();
    profileTextColor = u.getProfileTextColor();
    profileLinkColor = u.getProfileLinkColor();
    profileSidebarFillColor = u.getProfileSidebarFillColor();
    profileSidebarBorderColor = u.getProfileSidebarBorderColor();
    isProfileUseBackgroundImage = u.isProfileUseBackgroundImage();
    isDefaultProfile = u.isDefaultProfile();
    isShowAllInlineMedia = u.isShowAllInlineMedia();
    isDefaultProfileImage = u.isDefaultProfileImage();
    profileBackgroundImageURL = u.getProfileBackgroundImageURL();
    profileBannerURL = u.getProfileBannerURL();
    isProfileBackgroundTiled = u.isProfileBackgroundTiled();
  }
}
