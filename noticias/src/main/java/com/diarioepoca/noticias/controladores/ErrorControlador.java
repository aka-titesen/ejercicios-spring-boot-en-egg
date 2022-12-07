package com.diarioepoca.noticias.controladores;

import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorControlador implements ErrorController { //Implementa ErrorController

    /*Si se produce un error vamos a recuperar el código de error que viene del servidor*/
 /*El RequestMapping está al nivel del método y no al nivel de la clase. Éste método se ejecutará con todo recurso que active /error sin importar que sea get o post se ejecutará*/
    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) { //Recibe una petición http de la clase HttpServletRequest

        ModelAndView errorPage = new ModelAndView("error"); //Donde mandaremos el código de error y el mensaje con la llave error
        String errorMsg = ""; //Éste string guardará el mensaje dependiendo del código de error
        int httpErrorCode = getErrorCode(httpRequest); //En base a ese código de error vamos a ejecutar nuestro switch

        switch (httpErrorCode) {
            case 400: {
                errorMsg = "El recurso solicitado no existe.";
                break;
            }
            case 403: {
                errorMsg = "No tiene permisos para acceder al recurso.";
                break;
            }
            case 401: {
                errorMsg = "No se encuentra autorizado.";
                break;
            }
            case 404: {
                errorMsg = "El recurso solicitado no fue encontrado.";
                break;
            }
            case 500: {
                errorMsg = "Ocurrió un error interno.";
                break;
            }
        }
        errorPage.addObject("codigo", httpErrorCode); //Agregamos el código al ModelAndView
        errorPage.addObject("mensaje", errorMsg); //Agregamos el mensaje al ModelAndView
        return errorPage; //Devolvemos el ModelAndView
    }

    private int getErrorCode(HttpServletRequest httpRequest) { //Éste método recibe la petición 
        return (Integer) httpRequest.getAttribute("javax.servlet.error.status_code"); //Nos traemos el "status_code" (Estado del http) lo castea a un entero y lo devuelve
    }

    public String getErrorPath() {
        return "/error.html";
    }

}
