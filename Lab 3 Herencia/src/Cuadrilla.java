public class Cuadrilla {
    private final String codigo;
    private final double capacidadHoras;
    private final boolean certificadaRefrigeracion;

    public Cuadrilla(String codigo, double capacidadHoras,
                     boolean certificadaRefrigeracion) {
        this.codigo = OrdenServicio.textoValido(
            codigo, "Codigo de cuadrilla"
        );

        OrdenServicio.positivo(capacidadHoras, "Capacidad");

        this.capacidadHoras = capacidadHoras;
        this.certificadaRefrigeracion = certificadaRefrigeracion;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getCapacidadHoras() {
        return capacidadHoras;
    }

    public boolean isCertificadaRefrigeracion() {
        return certificadaRefrigeracion;
    }
}