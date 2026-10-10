public class Instalacion extends OrdenServicio {
    private final int puntos;

    public Instalacion(String codigo, String cliente, String direccion,
                       double horasEstimadas, Zona zona, int puntos) {
        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (puntos <= 0) {
            throw new IllegalArgumentException(
                "Los puntos deben ser un entero positivo."
            );
        }

        this.puntos = puntos;

        MovimientoIngreso.redondear(
            calcularTarifaBase()
            + 90 * Math.ceil(Math.max(horasEstimadas, puntos * 0.75))
        );
    }

    protected double calcularHorasFacturables() {
        double horasPorPuntos = puntos * 0.75;
        double horas = Math.max(getHorasEstimadas(), horasPorPuntos);

        return Math.ceil(horas);
    }

    @Override
    public double calcularCosto() {
        return MovimientoIngreso.redondear(
            calcularTarifaBase() + 90 * calcularHorasFacturables()
        );
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.Metropolitana ? 3 : 5;
    }

    @Override
    public int getMaximoVisitas() {
        return 3;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla != null;
    }

    @Override
    public String getCategoria() {
        return "Instalacion";
    }

    public int getPuntos() {
        return puntos;
    }
}