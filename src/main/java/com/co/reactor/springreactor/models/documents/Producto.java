package com.co.reactor.springreactor.models.documents;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Document(collection = "productos")
public class Producto {

    @Id
    @NotEmpty
    private String id;

    @NotEmpty
    private String nombre;
    @NotNull
    private Integer precio;
    @Valid
    private Categoria categoria;
    private String foto;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    public Producto(String nombre, Integer precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, Integer precio, Categoria categoria) {
        this(nombre, precio);
        this.categoria = categoria;
    }

    public Producto() {
    }
}
