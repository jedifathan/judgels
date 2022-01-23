package judgels.jophiel.api.user;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Map;
import judgels.jophiel.api.AbstractServiceIntegrationTests;
import judgels.jophiel.api.session.Credentials;
import judgels.jophiel.api.session.SessionService;
import judgels.jophiel.api.user.info.UserInfo;
import judgels.jophiel.api.user.info.UserInfoService;
import judgels.jophiel.api.user.search.UserSearchService;
import org.junit.jupiter.api.Test;

class UserServiceIntegrationTests extends AbstractServiceIntegrationTests {
    private final UserService userService = createService(UserService.class);
    private final UserInfoService userInfoService = createService(UserInfoService.class);
    private final UserSearchService userSearchService = createService(UserSearchService.class);
    private final SessionService sessionService = createService(SessionService.class);

    @Test
    void create_user() {
        User nano = userService.createUser(adminHeader, new UserData.Builder()
                .username("nano")
                .password("pass")
                .email("nano@domain.com")
                .build());

        assertThat(nano.getUsername()).isEqualTo("nano");
        assertThat(nano.getEmail()).isEqualTo("nano@domain.com");

        // duplicate username
        assertThatThrownBy(() -> userService.createUser(adminHeader, new UserData.Builder()
                .username("nano")
                .password("pass")
                .email("other@domain2.com")
                .build()))
                .hasFieldOrPropertyWithValue("code", 500); // TODO(fushar): should be 400

        // duplicate email
        assertThatThrownBy(() -> userService.createUser(adminHeader, new UserData.Builder()
                .username("other")
                .password("pass")
                .email("nano@domain.com")
                .build()))
                .hasFieldOrPropertyWithValue("code", 500); // TODO(fushar): should be 400

        assertNotFound(() -> userService.getUser(adminHeader, "bogus"));
        assertThat(userService.getUser(adminHeader, nano.getJid())).isEqualTo(nano);

        User nani = userService.createUser(adminHeader, new UserData.Builder()
                .username("nani")
                .password("pass")
                .email("nani@domain.com")
                .build());

        sessionService.logIn(Credentials.of("nano", "pass"));

        UsersResponse response = userService.getUsers(adminHeader, empty(), empty(), empty());
        assertThat(response.getData().getPage()).contains(nani, nano);
        assertThat(response.getLastSessionTimesMap()).containsKeys(nano.getJid());
        assertThat(response.getLastSessionTimesMap()).doesNotContainKeys(nani.getJid());
    }

    @Test
    void upsert_users() throws IOException {
        // create
        UsersUpsertResponse response = userService.upsertUsers(adminHeader, "country,name,email,username,password\n"
                + "ID,Andi Indo,andi@judgels.com,andi,123\r\n"
                + "TH,Budi Thai,budi@judgels.com,budi,456\n");
        assertThat(response.getCreatedUsernames()).containsExactly("andi", "budi");
        assertThat(response.getUpdatedUsernames()).isEmpty();

        // update + create
        response = userService.upsertUsers(adminHeader, "country,name,email,username,password\r\n"
                + "TH,Budi Thai 2,budi2@judgels.com,budi,333\n"
                + "MY,Caca Malay,caca@judgels.com,caca,777\n");
        assertThat(response.getCreatedUsernames()).containsExactly("caca");
        assertThat(response.getUpdatedUsernames()).containsExactly("budi");

        // update only password
        response = userService.upsertUsers(adminHeader, "username,password\r\n"
                + "caca,778\n");
        assertThat(response.getUpdatedUsernames()).containsExactly("caca");

        // create with fixed jid
        response = userService.upsertUsers(adminHeader, "jid,email,username,password\r\n"
                + "JID123,dudi@judgels.com,dudi,888\n");
        assertThat(response.getCreatedUsernames()).containsExactly("dudi");

        // update without password
        response = userService.upsertUsers(adminHeader, "username,email\r\n"
                + "dudi,dudidudi@judgels.com\n");
        assertThat(response.getUpdatedUsernames()).containsExactly("dudi");

        Map<String, String> usernameToJid =
                userSearchService.translateUsernamesToJids(ImmutableSet.of("andi", "budi", "caca", "dudi"));

        User andi = userService.getUser(adminHeader, usernameToJid.get("andi"));
        assertThat(andi.getEmail()).isEqualTo("andi@judgels.com");
        UserInfo andiInfo = userInfoService.getInfo(adminHeader, andi.getJid());
        assertThat(andiInfo.getCountry()).contains("ID");
        assertThat(andiInfo.getName()).contains("Andi Indo");
        assertPermitted(() -> sessionService.logIn(Credentials.of("andi", "123")));

        User budi = userService.getUser(adminHeader, usernameToJid.get("budi"));
        assertThat(budi.getEmail()).isEqualTo("budi2@judgels.com");
        UserInfo budiInfo = userInfoService.getInfo(adminHeader, budi.getJid());
        assertThat(budiInfo.getCountry()).contains("TH");
        assertThat(budiInfo.getName()).contains("Budi Thai 2");
        assertPermitted(() -> sessionService.logIn(Credentials.of("budi", "333")));

        User caca = userService.getUser(adminHeader, usernameToJid.get("caca"));
        assertThat(caca.getEmail()).isEqualTo("caca@judgels.com");
        UserInfo cacaInfo = userInfoService.getInfo(adminHeader, caca.getJid());
        assertThat(cacaInfo.getCountry()).contains("MY");
        assertThat(cacaInfo.getName()).contains("Caca Malay");
        assertPermitted(() -> sessionService.logIn(Credentials.of("caca", "778")));

        User dudi = userService.getUser(adminHeader, usernameToJid.get("dudi"));
        assertThat(dudi.getJid()).isEqualTo("JID123");
        assertThat(dudi.getEmail()).isEqualTo("dudidudi@judgels.com");
        assertPermitted(() -> sessionService.logIn(Credentials.of("dudi", "888")));
    }
}
