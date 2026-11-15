package cl.duoc.linceops.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "geocercas")
public class Geocerca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String puntoReferencia; // Ej: "Hotel Centro" o "Aeropuerto El Tepual"

    @Column(nullable = false)
    private Double latitudCentro;

    @Column(nullable = false)
    private Double longitudCentro;

    @Column(nullable = false)
    private Integer radioMetros = 150; // Requisito de la pauta

    // Constructores, Getters y Setters
    public Geocerca() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPuntoReferencia() { return puntoReferencia; }
    public void setPuntoReferencia(String puntoReferencia) { this.puntoReferencia = puntoReferencia; }
    public Double getLatitudCentro() { return latitudCentro; }
    public void setLatitudCentro(Double latitudCentro) { this.latitudCentro = latitudCentro; }
    public Double getLongitudCentro() { return longitudCentro; }
    public void setLongitudCentro(Double longitudCentro) { this.longitudCentro = longitudCentro; }
    public Integer getRadioMetros() { return radioMetros; }
    public void setRadioMetros(Integer radioMetros) { this.radioMetros = radioMetros; }
}