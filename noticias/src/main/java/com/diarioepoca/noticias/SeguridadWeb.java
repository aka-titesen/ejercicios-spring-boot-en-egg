package com.diarioepoca.noticias;

import com.diarioepoca.noticias.servicios.AdministradorServicios;
import com.diarioepoca.noticias.servicios.PeriodistaServicios;
import com.diarioepoca.noticias.servicios.UsuarioServicios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
En ésta clase vamos a escribir todas las configuraciones que vayamos necesitando para aplicar seguridad a TODO nuestro sistema
 */
 /*
Tenemos que avisarle a Spring que ésta clase va a manejar los componentes necesarios de Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SeguridadWeb extends WebSecurityConfigurerAdapter {

    @Autowired
    public UsuarioServicios usuarioServicios;
    @Autowired
    public AdministradorServicios administradorServicios;
    @Autowired
    public PeriodistaServicios periodistaServicios;

    /*
    Configuramos el manejador de seguridad que tiene Spring Security, y le vamos a decir cual es el servicio que tiene que utilizar para autenticar un usuario.
    *PASOS* 
    1.- Primero llamamos a userDatailsService que hicimos en la clase UsuarioServicios
    2.- Y para autenticar a los usuarios de nuestra aplicación le pasamos usuarioServicio
    3.- Vamos a codificar la contraseña, llamando al método passwordEncoder 
    4.- Pasandole BCryptPasswordEncoder que especifica el tipo de encriptación
    
    *FUNCIONAMIENTO*
    1.- Cuando se registra un usuario vamos a autenticarlo con userDetailsService
    2.- Una vez autenticado le encriptamos la contraseña con passwordEncoder
    3.- Y especificamos el tipo de encriptación con BCryptPasswordEncoder
    
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(usuarioServicios)
                .passwordEncoder(new BCryptPasswordEncoder()); //Encripto seguridad la contraseña de los usuarios

        auth.userDetailsService(administradorServicios)
                .passwordEncoder(new BCryptPasswordEncoder()); //Encripto seguridad la contraseña de los administradores

        auth.userDetailsService(periodistaServicios)
                .passwordEncoder(new BCryptPasswordEncoder()); //Encripto seguridad la contraseña de los periodistas
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*
        El objeto http de la clase HttpSecurity va a autorizar determinados parámetros.
        Siempre que estemos ingresando a determinadas partes de nuestro sistema que permita los archivos cuya ruta tengan,
        como por ejemplo css, js (javascript), imágenes (img) o cualquier otro archivo estático al que queramos acceder.
        ¿Qué vamos a hacer con ésto?, Las anteriores archivos van a ser permitidos por cualquier persona que acceda; es decir, no hace falta que sea un usuario registrado para poder ver los archivos
         */
        http
                .authorizeRequests()
                .antMatchers("/administrador/*").hasRole("ADMINISTRADOR")
                .antMatchers("/periodista/*").hasRole("PERIODISTA")
                .antMatchers("/css/*", "/js/*", "/img/*", "/**")
                .permitAll() // 1.- Autoriza los css, js, img, y todo archivo estático
                .and().formLogin() //Empezamos a configurar para que autorice el inicio sesión de un usuario, con formLogin le decimos que pertenece al formulario de login
                .loginPage("/login") //Aclaramos cual es nuestra página de login a Spring Boot (Ésto vendría a ser un método get que devuelva una vista del formulario de inicio de sesión)
                .loginProcessingUrl("/logincheck") //Éste es un método que genera de manera automatica Spring Boot para procesar el inicio de sesión (Ésto vendría a ser un método post y debe coincidir con el action del formulario del login) 
                .usernameParameter("email") //Empezamos a configurar las credenciales como el email que sería el username
                .passwordParameter("password") //La contraseña
                .defaultSuccessUrl("/index") //Es la ruta en donde iremos si el login es exitoso
                .permitAll() //Autoriza las siguientes acciones
                .and().logout()
                .logoutUrl("/logout") //Configuramos que cuando el usuario ingrese a determinada url, se cierre la sesión
                .logoutSuccessUrl("/login") //Configuramos que cuando el cierre de sesión sea exitoso se redireccione al index
                .permitAll() //Autoriza el cierre de sesión
                .and().csrf()
                .disable();
    }
}
