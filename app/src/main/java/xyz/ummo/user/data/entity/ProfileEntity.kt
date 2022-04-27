package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.ummo.user.data.model.ProfileModel

@Entity(tableName = "profile")
class ProfileEntity : ProfileModel {

    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "profile_contact")
    override lateinit var profileContact: String

    @ColumnInfo(name = "profile_name")
    override lateinit var profileName: String

    @ColumnInfo(name = "profile_email")
    override var profileEmail: String? = null

    constructor()
    constructor(_profileName: String,
                _profileContact: String,
                _profileEmail: String) {
        profileName = _profileName
        profileContact = _profileContact
        profileEmail = _profileEmail
    }

}