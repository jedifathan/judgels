package judgels.jophiel.user.profile;

import java.util.Optional;
import javax.inject.Inject;
import judgels.jophiel.api.user.profile.UserProfile;
import judgels.jophiel.persistence.UserProfileDao;
import judgels.jophiel.persistence.UserProfileModel;

public class UserProfileStore {
    private final UserProfileDao profileDao;

    @Inject
    public UserProfileStore(UserProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public UserProfile getProfile(String userJid) {
        return profileDao.selectByUserJid(userJid)
                .map(UserProfileStore::fromModel)
                .orElse(new UserProfile.Builder().build());
    }

    public UserProfile upsertProfile(String userJid, UserProfile profile) {
        Optional<UserProfileModel> maybeModel = profileDao.selectByUserJid(userJid);
        if (maybeModel.isPresent()) {
            UserProfileModel model = maybeModel.get();
            toModel(userJid, profile, model);
            return fromModel(profileDao.update(model));
        } else {
            UserProfileModel model = new UserProfileModel();
            toModel(userJid, profile, model);
            return fromModel(profileDao.insert(model));
        }
    }

    private static void toModel(String userJid, UserProfile profile, UserProfileModel model) {
        model.userJid = userJid;
        model.name = profile.getName().orElse(null);
        model.gender = profile.getGender().orElse(null);
        model.nationality = profile.getNationality().orElse(null);
        model.homeAddress = profile.getHomeAddress().orElse(null);
        model.institution = profile.getInstitution().orElse(null);
        model.country = profile.getCountry().orElse(null);
        model.province = profile.getProvince().orElse(null);
        model.city = profile.getCity().orElse(null);
        model.shirtSize = profile.getShirtSize().orElse(null);
    }

    private static UserProfile fromModel(UserProfileModel model) {
        return new UserProfile.Builder()
                .name(Optional.ofNullable(model.name))
                .gender(Optional.ofNullable(model.gender))
                .nationality(Optional.ofNullable(model.nationality))
                .homeAddress(Optional.ofNullable(model.homeAddress))
                .institution(Optional.ofNullable(model.institution))
                .country(Optional.ofNullable(model.country))
                .province(Optional.ofNullable(model.province))
                .city(Optional.ofNullable(model.city))
                .shirtSize(Optional.ofNullable(model.shirtSize))
                .build();
    }
}