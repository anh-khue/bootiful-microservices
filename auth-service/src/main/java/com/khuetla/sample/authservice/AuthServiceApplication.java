package com.khuetla.sample.authservice;

import lombok.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;

@EnableResourceServer
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}


@RestController
class PrincipalRestController {

    @GetMapping("/user")
    Principal getPrincipal(Principal principal) {
        return principal;
    }
}


@Configuration
@EnableAuthorizationServer
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    OAuthConfiguration(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("mercury")
                .secret("tlvc")
                .authorizedGrantTypes("password")
                .scopes("openid");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager);
    }
}


@Component
class AccountsInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    AccountsInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... strings) throws Exception {
        Stream.of("akhue,cloud", "jlong,spring", "pwebb,boot")
                .map(pair -> pair.split(","))
                .map(tuple -> new Account(tuple[0], tuple[1], true))
                .forEach(accountRepository::save);

        this.accountRepository.findAll()
                .forEach(account -> System.out.println(account.getUsername() + " | " + account.getPassword()));
    }
}


@Service
class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.accountRepository.findByUsername(username)
                .map(account -> new User(
                        account.getUsername(),
                        account.getPassword(),
                        account.isActive(),
                        account.isActive(),
                        account.isActive(),
                        account.isActive(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")
                ))
                .orElseThrow(() -> new UsernameNotFoundException("No user with username '" + username + "' found!"));
    }
}


interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
}


@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Account {

    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private boolean active;

}
