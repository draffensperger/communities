package org.draff.models;

import java.util.Date;

/**
 * Created by dave on 1/7/16.
 */
public class UserDetail {
  public long id;
  public long retrievedAt;
  public String screenName;
  public String location;
  public String description;
  public long statusesCount;
  public long listedCount;
  public long followersCount;
  public long favouritesCount;
  public long utcOffset;
  public long friendsCount;
  public Date createdAt;
  public String timeZone;
  public String lang;
  public boolean isGeoEnabled;
  public boolean isVerified;
  public boolean isTranslator;
  public boolean isContributorsEnabled;
  public boolean isProtected;
  public String profileImageURL;
  public String profileBackgroundColor;
  public String profileTextColor;
  public String profileLinkColor;
  public String profileSidebarFillColor;
  public String profileSidebarBorderColor;
  public boolean isProfileUseBackgroundImage;
  public boolean isDefaultProfile;
  public boolean isShowAllInlineMedia;
  public boolean isDefaultProfileImage;
  public String profileBackgroundImageURL;
  public String profileBannerURL;
  public boolean isProfileBackgroundTiled;

  public UserDetail() {}
  public UserDetail(twitter4j.User user) {
    setFieldsFromTwitterUser(user);
  }

  public void setFieldsFromTwitterUser(twitter4j.User u) {
    retrievedAt = System.currentTimeMillis();

    id = u.getId();
    screenName = u.getScreenName();
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
    isDefaultProfileImage = u.isDefaultProfileImage();
    profileBackgroundImageURL = u.getProfileBackgroundImageURL();
    profileBannerURL = u.getProfileBannerURL();
    isProfileBackgroundTiled = u.isProfileBackgroundTiled();
  }
}
