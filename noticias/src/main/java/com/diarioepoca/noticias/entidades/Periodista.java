package com.diarioepoca.noticias.entidades;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Periodista extends Usuario {

    @OneToMany
    private List<Noticia> noticias;
    private Integer salario; //Sueldo mensual

}
