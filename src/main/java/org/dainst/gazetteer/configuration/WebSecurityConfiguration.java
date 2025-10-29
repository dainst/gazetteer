package org.dainst.gazetteer.configuration;
import org.dainst.gazetteer.dao.UserPasswordChangeRequestRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.helpers.AuthenticationSuccessHandler;
import org.dainst.gazetteer.helpers.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfiguration {
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(
        UserRepository userRepository,
        UserPasswordChangeRequestRepository userPasswordChangeRequestRepository
    ) {
        AuthenticationSuccessHandler authenticationSuccessHandler = new AuthenticationSuccessHandler();
        authenticationSuccessHandler.setDefaultTargetUrl("/");
        authenticationSuccessHandler.setTargetUrlParameter("spring-security-redirect");
        authenticationSuccessHandler.setUserRepository(userRepository);
        authenticationSuccessHandler.setUserPasswordChangeRequestRepository(userPasswordChangeRequestRepository);
        return authenticationSuccessHandler;
    }

    @Bean 
    public PasswordEncoder passwordEncoder() { 
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        UserService userService = new UserService();
        userService.setUserRepository(userRepository);
        return userService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder bCryptPasswordEncoder,
            UserDetailsService userDetailsService
    ) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
          .userDetailsService(userDetailsService)
          .passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        AuthenticationSuccessHandler authenticationSuccessHandler
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable
        )
            .logout(logout ->
                 logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/")
            )
            .formLogin(form ->
                    form.loginPage("/login")
                        .loginProcessingUrl("/j_spring_security_check")
                        .failureUrl("/loginfailed")
                        .successHandler(authenticationSuccessHandler)
            ).authorizeHttpRequests( httpRequest ->
                httpRequest
                    .requestMatchers("/user").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/doc/**").hasRole("EDITOR")
                    .requestMatchers(HttpMethod.PUT, "/doc/**").hasRole("EDITOR")
                    .requestMatchers(HttpMethod.DELETE, "/doc/**").hasRole("EDITOR")
                    .requestMatchers(HttpMethod.POST, "/merge/**").hasRole("EDITOR")
                    .requestMatchers(HttpMethod.POST, "/duplicate/**").hasRole("EDITOR")
                    .requestMatchers(HttpMethod.POST, "/validation/**").hasRole("EDITOR")
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/userManagement/**").hasRole("ADMIN")
                    .requestMatchers("/recordGroupManagement/**").hasRole("USER")
                    .requestMatchers("/recordGroupUserManagement/**").hasRole("USER")
                    .requestMatchers("/editUser/**").hasRole("USER")
                    .requestMatchers("/globalChangeHistory/**").hasRole("EDITOR")
                    .requestMatchers("/*").permitAll()
                    .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
