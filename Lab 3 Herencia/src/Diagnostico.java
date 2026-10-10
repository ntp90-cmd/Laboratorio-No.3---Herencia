public class Diagnostico extends OrdenServicio {

    public Diagnostico(String codigo, String cliente, String direccion,
                       double horasEstimadas, Zona zona) {
        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (horasEstimadas > 1.5) {
            throw new IllegalArgumentException(
                "Un diagnostico no puede superar 1.5 horas."
            );
        }
    }

    @Override
    public double calcularCosto() {
        return getZona() == Zona.Metropolitana ? 150 : 225;
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.Metropolitana ? 1 : 2;
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
        return "Diagnostico";
    }
}
