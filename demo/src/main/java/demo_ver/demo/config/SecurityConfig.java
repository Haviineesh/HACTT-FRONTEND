package demo_ver.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import demo_ver.demo.service.ManageUserService;

@Configuration
@EnableWebSecurity

// Handles the security configuration of the application
public class SecurityConfig extends WebSecurityConfiguration {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SpringSecurityDialect springSecurityDialect() {
                return new SpringSecurityDialect();
        }

        @Bean
        public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver, SpringSecurityDialect sec) {
                final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
                templateEngine.setTemplateResolver(templateResolver);
                templateEngine.addDialect(sec);
                return templateEngine;
        }

        private final RestTemplate restTemplate = new RestTemplate();

        @Bean
        public ManageUserService manageUserService(PasswordEncoder passwordEncoder,
        RestTemplate restTemplate) {
        return new ManageUserService(passwordEncoder, restTemplate);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);
                authenticationManagerBuilder.userDetailsService(manageUserService(passwordEncoder(),
                restTemplate))
                .passwordEncoder(passwordEncoder());
                return http
                                .formLogin(form -> form
                                                .loginPage("/login").permitAll()
                                                .defaultSuccessUrl("/home", true))
                                .logout(logout -> logout
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .logoutUrl("/logout").permitAll())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/**").permitAll()
                                                .requestMatchers("/login", "/forgotpassword", "/resetpassword")
                                                .permitAll()
                                                .requestMatchers("/resources/**", "/static/**", "/webjars/**")
                                                .permitAll()
                                                .requestMatchers("/manageuser", "/adduser", "/deleteuser/{userID}",
                                                                "/edituser/{userID}", "/updateuser", "/manageroles",
                                                                "/createrole", "/editrole/{id}", "/editrole",
                                                                "/deleterole/{id}")
                                                .hasRole("Admin")
                                                .requestMatchers("/view", "/add", "/save", "/deleteCase/{idtest_cases}",
                                                                "/editCase/{idtest_cases}", "/update",
                                                                "/testcases/details/{idtest_cases}",
                                                                "/testcases/approveStatus/{idtest_cases}",
                                                                "/testcases/rejectStatus/{idtest_cases}",
                                                                "/testcases/setUnderReview/{idtest_cases}",
                                                                "/testcases/setNeedsRevision/{idtest_cases}")
                                                .hasAnyRole("Tester", "Product Manager", "Developer", "Stakeholder",
                                                                "QA", "Business Analyst", "UX Designer", "Tech Lead",
                                                                "Scrum Master",
                                                                "System Architect", "Security Engineer",
                                                                "DevOps Engineer")
                                                .requestMatchers("/home", "/changepassword")
                                                .hasAnyRole("Admin", "Tester", "Product Manager", "Developer",
                                                                "Stakeholder", "QA",
                                                                "Business Analyst", "UX Designer", "Tech Lead",
                                                                "Scrum Master",
                                                                "System Architect", "Security Engineer",
                                                                "DevOps Engineer")
                                                .anyRequest().authenticated())
                                .csrf(AbstractHttpConfigurer::disable)
                                .logout(logout -> logout.addLogoutHandler((request, response, authentication) -> {
                                        SecurityContextHolder.clearContext();
                                }))
                                .build();
        }
}
