package com.myplantdiary.apps.telegram.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProps::class)
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/healthz", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/telegram/**").permitAll()
                    .anyRequest().permitAll()
            }
        return http.build()
    }
}

@Configuration
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class JwtResourceServerConfig(
    private val props: SecurityProps
) {
    @Bean
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/healthz", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/telegram/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/admin/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/admin/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/admin/**").authenticated()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(props.jwt.secret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).build()
    }
}

@ConfigurationProperties(prefix = "security.auth")
class SecurityProps(
    val enabled: Boolean = false,
    val jwt: JwtProps = JwtProps()
) {
    data class JwtProps(
        val issuer: String = "http://localhost",
        val audience: String = "mpd",
        val secret: String = "dev-secret-change-me"
    )
}

