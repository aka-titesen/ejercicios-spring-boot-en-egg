package com.diarioepoca.noticias.servicios;

import com.diarioepoca.noticias.entidades.Imagen;
import com.diarioepoca.noticias.entidades.Periodista;
import com.diarioepoca.noticias.entidades.Usuario;
import com.diarioepoca.noticias.enumeraciones.Rol;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.repositorios.PeriodistaRepositorio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PeriodistaServicios implements UserDetailsService {

    @Autowired
    private PeriodistaRepositorio periodistaRepositorio;
    @Autowired
    private ImagenServicios imagenServicios;

    @Transactional
    public void crearPeriodista(MultipartFile archivo, String nombre, String email, String password, String passwordDos, Integer salario) throws NoticiaException {

        validarSiExistePeriodista(email);

        validar(nombre, email, password, passwordDos);

        Periodista periodista = new Periodista();

        periodista.setNombre(nombre);
        periodista.setEmail(email);
        periodista.setPassword(new BCryptPasswordEncoder().encode(password)); //Encriptamos la contrase??a
        periodista.setActivo(true);
        periodista.setAlta(new Date());
        periodista.setBaja(null);
        periodista.setRol(Rol.PERIODISTA);
        periodista.setNoticias(null);
        periodista.setSalario(salario);

        Imagen imagen = imagenServicios.guardarImagen(archivo); //Servicio para persistir en la Bd la foto de perfil del usuario

        periodista.setFoto(imagen); //Establecemos la imagen al atributo del usuario

        periodistaRepositorio.save(periodista);

    }

    public void validar(String nombre, String email, String password, String passwordDos) throws NoticiaException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NoticiaException("El nombre del usuario no puede ser nulo o estar vacio");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new NoticiaException("La contrase??a del usuario no puede ser nulo o estar vacio");
        }
        if (password == null || password.trim().isEmpty() || password.length() <= 6) {
            throw new NoticiaException("La contrase??a del usuario no puede ser nulo o estar vacio o menor a 6 cifras");
        }
        if (passwordDos == null || passwordDos.trim().isEmpty() || passwordDos.length() <= 6) {
            throw new NoticiaException("La segunda contrase??a del usuario no puede ser nulo o estar vacio a 6 cifras");
        }
        if (!password.equals(passwordDos)) {
            throw new NoticiaException("Las contrase??as ingresadas deben ser iguales");
        }
    }

    private void validarSiExistePeriodista(String email) throws NoticiaException {
        if (periodistaRepositorio.buscarPeriodistaPorEmail(email) != null) {
            throw new NoticiaException("Ya hay un usuario con el mismo correo electronico");
        }
    }

    @Transactional(readOnly = true)
    public List<Periodista> listarPeriodistas() {
        List<Periodista> auxiliar = new ArrayList();
        List<Periodista> periodistas = new ArrayList();
        auxiliar = periodistaRepositorio.findAll();
        for (Periodista periodista : auxiliar) {
            if (periodista.isActivo()) {
                periodistas.add(periodista);
            }
        }
        return periodistas;
    }

    @Transactional(readOnly = true)
    public Periodista getOne(Integer id) {
        return periodistaRepositorio.getOne(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        /*
        Obtenemos el usuario que intenta ingresar a trav??s de su email.
        Buscamos un usuario de nuestro dominio y transformarlo al dominio de Spring Security
         */
        Periodista periodista = periodistaRepositorio.buscarPeriodistaPorEmail(email);

        if (periodista != null) { //Ac?? empezamos a trabajar con Spring Security

            List<GrantedAuthority> permisos = new ArrayList(); //??sta lista va a contener todos los permisos que tendr?? el usuario

            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + periodista.getRol().toString()); //ROLE_Usuario

            permisos.add(p); //Guardamos el permiso de la l??nea 80 a la lista de permisos

            /*
            Vamos a atrapar los atributos del periodista que inicio sesi??n en la plataforma y guardarlo en la sesi??n web m??s precisamente se va a guardar en la clase 
            ServletRequestAttributes y en el objeto attr.
            Llamamos a la clase RequestContextHolder que maneja los datos del usuario que se logea y con el m??todo currentRequestAttributes(); obtenemos al objeto usuario de la sesi??n actual.
            Es decir de la solicitud HTTP
             */
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            /*
            Vamos a guardar los atributos de la sesi??n actual capturada en un objeto sesion de la clase HttpSession; es decir,
            vamos a guardar el llamado que nos trae el atributo y en base a eso la sesi??n, de la solicitud HTTP que pedimos antes
             */
            HttpSession sesion = attr.getRequest().getSession(true);

            sesion.setAttribute("usuarioSesion", periodista); //A la sesi??n actual a??adimos el objeto actual; es decir el usuario que est?? logeado bajo una llave

            return new User(periodista.getEmail(), periodista.getPassword(), permisos); //??sta clase recibe el username, password y los permisos que tendr?? el usuario; es decir otorga los permisos a un usuario
        } else {
            return null;
        }
    }
}
