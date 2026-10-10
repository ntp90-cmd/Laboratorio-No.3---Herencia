public class InstalacionElectricaCertificada extends Instalacion {
    private final double valorMateriales;

    public InstalacionElectricaCertificada(
            String codigo, String cliente, String direccion,
            double horasEstimadas, Zona zona,
            int puntos, double valorMateriales) {

        super(codigo, cliente, direccion, horasEstimadas, zona, puntos);

        positivo(valorMateriales, "Valor de materiales");
        this.valorMateriales = valorMateriales;

        MovimientoIngreso.redondear(
            super.calcularCosto() + 120 + valorMateriales * 0.05
        );
    }

    @Override
    public double calcularCosto() {
        return MovimientoIngreso.redondear(
            super.calcularCosto() + 120 + valorMateriales * 0.05
        );
    }

    @Override
    public int calcularDiasAtencion() {
        return super.calcularDiasAtencion() + 2;
    }

    @Override
    public int getMaximoVisitas() {
        return 2;
    }

    @Override
    public String getCategoria() {
        return "Instalacion electrica certificada";
    }

    public double getValorMateriales() {
        return valorMateriales;
    }
}
