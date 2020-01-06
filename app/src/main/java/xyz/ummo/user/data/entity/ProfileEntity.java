package xyz.ummo.user.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import xyz.ummo.user.data.model.ProfileModel;

@Entity(tableName = "profile")
public class ProfileEntity implements ProfileModel {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    private int profileId;

    @NonNull
    @ColumnInfo(name = "profile_name")
    private String profileName;

    @NonNull
    @ColumnInfo(name = "profile_contact")
    private String profileContact;

    @NonNull
    @ColumnInfo(name = "profile_email")
    private String profileEmail;

    /*@NonNull
    @ColumnInfo(name = "profile_location")
    private String profileLocation;

    @NonNull
    @ColumnInfo(name = "profile_rating")
    private String profileRating;

    @NonNull
    @ColumnInfo(name = "profile_pic")
    private String profilePic;*/

    public ProfileEntity(){}

    public ProfileEntity(@NonNull String _profileName,
                         @NonNull String _profileContact,
                         @NonNull String _profileEmail/*,
                         @NonNull String _profileLocation,
                         @NonNull String _profileRating,
                         @NonNull String _profilePic*/){
        this.profileName = _profileName;
        this.profileContact = _profileContact;
        this.profileEmail = _profileEmail;
        /*this.profileLocation = _profileLocation;
        this.profileRating = _profileRating;
        this.profilePic = _profilePic;*/
    }

    @Override
    @NonNull
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(@NonNull int profileId) {
        this.profileId = profileId;
    }

    @Override
    @NonNull
    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(@NonNull String profileName) {
        this.profileName = profileName;
    }

    @Override
    @NonNull
    public String getProfileContact() {
        return profileContact;
    }

    public void setProfileContact(@NonNull String profileContact) {
        this.profileContact = profileContact;
    }

    @Override
    @NonNull
    public String getProfileEmail() {
        return profileEmail;
    }

    public void setProfileEmail(@NonNull String profileEmail) {
        this.profileEmail = profileEmail;
    }

    /*@Override
    @NonNull
    public String getProfileLocation() {
        return profileLocation;
    }

    public void setProfileLocation(@NonNull String profileLocation) {
        this.profileLocation = profileLocation;
    }

    @Override
    @NonNull
    public String getProfileRating() {
        return profileRating;
    }

    public void setProfileRating(@NonNull String profileRating) {
        this.profileRating = profileRating;
    }

    @Override
    @NonNull
    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(@NonNull String profilePic) {
        this.profilePic = profilePic;
    }*/
}
