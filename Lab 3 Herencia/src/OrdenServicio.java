public abstract class OrdenServicio {
    private final String codigo;
    private final String cliente;
    private final String direccion;
    private final double horasEstimadas;
    private final Zona zona;
    private EstadoOrden estado;
    private int visitasRealizadas;

    protected OrdenServicio(String codigo, String cliente,
                            String direccion, double horasEstimadas,
                            Zona zona) {
        this.codigo = textoValido(codigo, "Codigo");
        this.cliente = textoValido(cliente, "Cliente");
        this.direccion = textoValido(direccion, "Direccion");

        positivo(horasEstimadas, "Horas estimadas");

        if (zona == null) {
            throw new IllegalArgumentException("Zona invalida.");
        }

        this.horasEstimadas = horasEstimadas;
        this.zona = zona;
        estado = EstadoOrden.Pendiente;
        visitasRealizadas = 0;
    }

    public abstract double calcularCosto();

    public abstract int calcularDiasAtencion();

    public abstract int getMaximoVisitas();

    public abstract boolean esCompatibleCon(Cuadrilla cuadrilla);

    public abstract String getCategoria();

    protected double calcularTarifaBase() {
        return zona == Zona.Metropolitana ? 100 : 175;
    }

    void marcarAsignada() {
        exigirEstado(EstadoOrden.Pendiente);

        if (visitasRealizadas >= getMaximoVisitas()) {
            throw new IllegalStateException(
                "La orden no tiene visitas disponibles."
            );
        }

        estado = EstadoOrden.Asignada;
    }

    void retirarAsignacion() {
        exigirEstado(EstadoOrden.Asignada);
        estado = EstadoOrden.Pendiente;
    }

    void iniciarVisita() {
        exigirEstado(EstadoOrden.Asignada);

        if (visitasRealizadas >= getMaximoVisitas()) {
            throw new IllegalStateException(
                "La orden agoto sus visitas."
            );
        }

        visitasRealizadas++;
        estado = EstadoOrden.En_atencion;
    }

    void registrarResultado(ResultadoVisita resultado) {
        exigirEstado(EstadoOrden.En_atencion);

        if (resultado == null) {
            throw new IllegalArgumentException("Resultado invalido.");
        }

        if (resultado == ResultadoVisita.Completada) {
            estado = EstadoOrden.Completada;
        } else if (visitasRealizadas < getMaximoVisitas()) {
            estado = EstadoOrden.Pendiente;
        } else {
            estado = EstadoOrden.Cancelada;
        }
    }

    private void exigirEstado(EstadoOrden esperado) {
        if (estado != esperado) {
            throw new IllegalStateException(
                "Estado actual: " + estado
                + ". Estado requerido: " + esperado + "."
            );
        }
    }

    protected static String textoValido(String valor, String nombre) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                nombre + " no puede estar vacio."
            );
        }

        return valor.trim();
    }

    protected static void positivo(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                nombre + " debe ser un numero positivo y finito."
            );
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getHorasEstimadas() {
        return horasEstimadas;
    }

    public Zona getZona() {
        return zona;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public int getVisitasRealizadas() {
        return visitasRealizadas;
    }
}