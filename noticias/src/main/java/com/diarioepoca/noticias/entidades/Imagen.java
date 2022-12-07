package com.diarioepoca.noticias.entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Imagen implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String mime; //Es el atributo que asigna el formato del archivo de la imágen 

    private String nombre; //Nombre de la imágen
    @Lob
    @Basic(fetch = FetchType.LAZY) //Con éste la imágen solo se va a cargar cuando lo pidamos
    private byte[] contenido; //Se convertirá la imágen en bytes y será guardado en éste arreglo

}
