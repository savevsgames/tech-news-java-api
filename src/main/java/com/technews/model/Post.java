package com.technews.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@JsonIgnoreProperties({"hibernateInitiailizer", "handler"})
@Table(name = "post")
public class Post {
}
