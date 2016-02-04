package org.draff.model;

import com.google.auto.value.AutoValue;

import org.draff.objectdb.Model;

import java.time.Instant;
import java.util.Date;

import javax.annotation.Nullable;

/**
 * Created by dave on 1/7/16.
 */
@AutoValue
public abstract class UserDetail implements Model {
  public abstract long id();
  public abstract long retrievedAt();
  public abstract String screenName();
  public abstract String screenNameLower();
  @Nullable public abstract String location();
  @Nullable public abstract String description();
  @Nullable public abstract String url();
  public abstract long statusesCount();
  public abstract long listedCount();
  public abstract long followersCount();
  public abstract long favouritesCount();
  public abstract long utcOffset();
  public abstract long friendsCount();
  @Nullable public abstract Instant createdAt();
  @Nullable public abstract String timeZone();
  @Nullable public abstract String lang();
  public abstract boolean isGeoEnabled();
  public abstract boolean isVerified();
  public abstract boolean isTranslator();
  public abstract boolean isContributorsEnabled();
  public abstract boolean isProtected();
  @Nullable public abstract String profileImageURL();
  @Nullable public abstract String profileBackgroundColor();
  @Nullable public abstract String profileTextColor();
  @Nullable public abstract String profileLinkColor();
  @Nullable public abstract String profileSidebarFillColor();
  @Nullable public abstract String profileSidebarBorderColor();
  public abstract boolean isProfileUseBackgroundImage();
  public abstract boolean isDefaultProfile();
  public abstract boolean isShowAllInlineMedia();
  public abstract boolean isDefaultProfileImage();
  @Nullable public abstract String profileBackgroundImageURL();
  @Nullable public abstract String profileBannerURL();
  public abstract boolean isProfileBackgroundTiled();

  UserDetail() {}
  public static UserDetail createFrom(twitter4j.User user) {
    return buildFromTwitterUser(user);
  }

  public static Builder builder() {
    return new AutoValue_UserDetail.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder id(long value);
    public abstract Builder retrievedAt(long value);
    public abstract Builder screenName(String value);
    public abstract Builder screenNameLower(String value);
    public abstract Builder location(String value);
    public abstract Builder description(String value);
    public abstract Builder url(String value);
    public abstract Builder statusesCount(long value);
    public abstract Builder listedCount(long value);
    public abstract Builder followersCount(long value);
    public abstract Builder favouritesCount(long value);
    public abstract Builder utcOffset(long value);
    public abstract Builder friendsCount(long value);
    public abstract Builder createdAt(Instant value);
    public abstract Builder timeZone(String value);
    public abstract Builder lang(String value);
    public abstract Builder isGeoEnabled(boolean value);
    public abstract Builder isVerified(boolean value);
    public abstract Builder isTranslator(boolean value);
    public abstract Builder isContributorsEnabled(boolean value);
    public abstract Builder isProtected(boolean value);
    public abstract Builder profileImageURL(String value);
    public abstract Builder profileBackgroundColor(String value);
    public abstract Builder profileTextColor(String value);
    public abstract Builder profileLinkColor(String value);
    public abstract Builder profileSidebarFillColor(String value);
    public abstract Builder profileSidebarBorderColor(String value);
    public abstract Builder isProfileUseBackgroundImage(boolean value);
    public abstract Builder isDefaultProfile(boolean value);
    public abstract Builder isShowAllInlineMedia(boolean value);
    public abstract Builder isDefaultProfileImage(boolean value);
    public abstract Builder profileBackgroundImageURL(String value);
    public abstract Builder profileBannerURL(String value);
    public abstract Builder isProfileBackgroundTiled(boolean value);
    public abstract UserDetail build();
  }

  private static UserDetail buildFromTwitterUser(twitter4j.User u) {
    Builder builder = builder();

    builder.retrievedAt(System.currentTimeMillis());
    builder.id(u.getId());
    builder.screenName(u.getScreenName());
    builder.screenNameLower(u.getScreenName().toLowerCase());
    builder.location(u.getLocation());
    builder.description(u.getDescription());
    builder.url(u.getURL());
    builder.statusesCount(u.getStatusesCount());
    builder.listedCount(u.getListedCount());
    builder.followersCount(u.getFollowersCount());
    builder.favouritesCount(u.getFavouritesCount());
    builder.utcOffset(u.getUtcOffset());
    builder.friendsCount(u.getFriendsCount());

    if (u.getCreatedAt() != null) {
      builder.createdAt(u.getCreatedAt().toInstant());
    }

    builder.timeZone(u.getTimeZone());
    builder.lang(u.getLang());
    builder.isGeoEnabled(u.isGeoEnabled());
    builder.isVerified(u.isVerified());
    builder.isTranslator(u.isTranslator());
    builder.isContributorsEnabled(u.isContributorsEnabled());
    builder.isProtected(u.isProtected());
    builder.profileImageURL(u.getProfileImageURL());
    builder.profileBackgroundColor(u.getProfileBackgroundColor());
    builder.profileTextColor(u.getProfileTextColor());
    builder.profileLinkColor(u.getProfileLinkColor());
    builder.profileSidebarFillColor(u.getProfileSidebarFillColor());
    builder.profileSidebarBorderColor(u.getProfileSidebarBorderColor());
    builder.isProfileUseBackgroundImage(u.isProfileUseBackgroundImage());
    builder.isDefaultProfile(u.isDefaultProfile());
    builder.isShowAllInlineMedia(u.isShowAllInlineMedia());
    builder.isDefaultProfileImage(u.isDefaultProfileImage());
    builder.profileBackgroundImageURL(u.getProfileBackgroundImageURL());
    builder.profileBannerURL(u.getProfileBannerURL());
    builder.isProfileBackgroundTiled(u.isProfileBackgroundTiled());

    return builder.build();
  }
}
