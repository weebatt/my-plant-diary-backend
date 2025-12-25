package com.myplantdiary.apps.monolith.security

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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
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
                    .requestMatchers("/plants/**").permitAll()
                    .requestMatchers("/dev/**").permitAll()
                    .anyRequest().permitAll()
            }
        return http.build()
    }
}

@Configuration
@EnableConfigurationProperties(SecurityProps::class)
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
                    .requestMatchers("/plants/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()) }
            }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(props.jwt.secret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).build()
    }

    @Bean
    fun jwtAuthConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val role = (jwt.claims["role"] as? String)?.uppercase() ?: "USER"
            val authorities = mutableListOf<GrantedAuthority>()
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
            authorities
        }
        return converter
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
