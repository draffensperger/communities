package org.draff;

import java.util.Date;

/**
 * Created by dave on 1/7/16.
 */
public class UserDetails {
  long id;
  long retrievedAt;
  String screenName;
  String url;
  String location;
  String description;

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
  boolean isFollowRequestSent;
  boolean isDefaultProfileImage;
  String profileBackgroundImageURL;
  String profileBannerURL;
  String profileBannerRetinaURL;
  String profileBannerIPadURL;
  String profileBannerIPadRetinaURL;
  String profileBannerMobileURL;
  String profileBannerMobileRetinaURL;
  boolean isProfileBackgroundTiled;

  public void setFieldsFromTwitterUser(twitter4j.User u) {
    retrievedAt = System.currentTimeMillis();
    //status = u.getStatus();

    id = u.getId();
    screenName = u.getScreenName();
    url = u.getURL();
    location = u.getLocation();
    description = u.getDescription();

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
    isFollowRequestSent = u.isFollowRequestSent();
    isDefaultProfileImage = u.isDefaultProfileImage();
    profileBackgroundImageURL = u.getProfileBackgroundImageURL();
    profileBannerURL = u.getProfileBannerURL();
    profileBannerRetinaURL = u.getProfileBannerRetinaURL();
    profileBannerIPadURL = u.getProfileBannerIPadURL();
    profileBannerIPadRetinaURL = u.getProfileBannerIPadRetinaURL();
    profileBannerMobileURL = u.getProfileBannerMobileURL();
    profileBannerMobileRetinaURL = u.getProfileBannerMobileRetinaURL();
    isProfileBackgroundTiled = u.isProfileBackgroundTiled();
  }
}
