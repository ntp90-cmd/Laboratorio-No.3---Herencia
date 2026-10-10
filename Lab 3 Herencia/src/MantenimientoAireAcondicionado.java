public class MantenimientoAireAcondicionado extends OrdenServicio {
    private final int btu;

    public MantenimientoAireAcondicionado(
            String codigo, String cliente, String direccion,
            double horasEstimadas, Zona zona, int btu) {

        super(codigo, cliente, direccion, horasEstimadas, zona);

        if (zona != Zona.Metropolitana) {
            throw new IllegalArgumentException(
                "El mantenimiento solo se ofrece en zona metropolitana."
            );
        }

        if (btu < 9000 || btu > 60000) {
            throw new IllegalArgumentException(
                "La capacidad debe estar entre 9000 y 60000 btu."
            );
        }

        this.btu = btu;

        MovimientoIngreso.redondear(
            calcularTarifaBase() + 110 * horasEstimadas + 80
        );
    }

    @Override
    public double calcularCosto() {
        return MovimientoIngreso.redondear(
            calcularTarifaBase() + 110 * getHorasEstimadas() + 80
        );
    }

    @Override
    public int calcularDiasAtencion() {
        return 2;
    }

    @Override
    public int getMaximoVisitas() {
        return 1;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla != null
            && cuadrilla.isCertificadaRefrigeracion();
    }

    @Override
    public String getCategoria() {
        return "Mantenimiento de aire acondicionado";
    }

    public int getBtu() {
        return btu;
    }
}