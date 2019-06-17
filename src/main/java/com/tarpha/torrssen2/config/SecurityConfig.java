package com.tarpha.torrssen2.config;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.domain.User;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.repository.UserRepository;
import com.tarpha.torrssen2.service.MyUserDetailsService;

import org.apache.commons.io.FileUtils;
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
    UserRepository userRepository;

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

        String symmetricKey = symmetricKey();
        String path = System.getProperty("user.home") + File.separator + "data" + File.separator + "symmetricKey";
        FileUtils.writeStringToFile(new File(path), symmetricKey, "UTF-8", false);

        User recovery = userRepository.findByUsername("recovery");
        if(recovery == null) {
            User user = new User();
            user.setUsername("recovery");
            user.setPassword(encoder().encode(symmetricKey));
            userRepository.save(user);
        } 
        
        User torrssen = userRepository.findByUsername("torrssen");
        if(torrssen == null) {
            User user = new User();
            user.setUsername("torrssen");
            user.setPassword("");
            userRepository.save(user);
        }

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

    public String symmetricKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
 
}