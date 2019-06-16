package com.tarpha.torrssen2.config;

import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.service.MyUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    MyUserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() { 
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }
    
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Override
	public void configure(WebSecurity web) throws Exception
	{
		web.ignoring().antMatchers("/css/**", "/script/**", "image/**", "/fonts/**", "lib/**");
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        Optional<Setting> optionalSetting = settingRepository.findByKey("USE_LOGIN");
        if(optionalSetting.isPresent()) {
            if(!Boolean.parseBoolean(optionalSetting.get().getValue())) {
                http.authorizeRequests()
                    .anyRequest().permitAll()
                    .and().headers().frameOptions().sameOrigin()
                    .and().csrf().disable();

            } else {
                http.authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .antMatchers("/h2-console/**").permitAll()
                    .anyRequest().authenticated()
                    .and().formLogin().permitAll()
                    .and().headers().frameOptions().sameOrigin()
                    .and().csrf().disable();
            }
        }
    }
 
}