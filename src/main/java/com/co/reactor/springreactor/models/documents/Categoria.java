package com.co.reactor.springreactor.models.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "categorias")
public class Categoria {
    @Id
    protected String id;
    protected String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Categoria() {
    }
}
