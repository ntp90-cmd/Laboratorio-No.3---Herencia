import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Jornada {
    private final int id;
    private final Cuadrilla cuadrilla;
    private final List<OrdenServicio> ordenes;
    private final Map<String, ResultadoVisita> resultados;
    private EstadoJornada estado;

    public Jornada(int id, Cuadrilla cuadrilla) {
        if (id <= 0 || cuadrilla == null) {
            throw new IllegalArgumentException(
                "Datos de jornada invalidos."
            );
        }

        this.id = id;
        this.cuadrilla = cuadrilla;
        ordenes = new ArrayList<>();
        resultados = new LinkedHashMap<>();
        estado = EstadoJornada.En_preparacion;
    }

    void asignarOrden(OrdenServicio orden) {
        exigirEstado(EstadoJornada.En_preparacion);

        if (orden == null
                || orden.getEstado() != EstadoOrden.Pendiente) {
            throw new IllegalStateException(
                "La orden debe estar pendiente y sin otra asignacion."
            );
        }

        if (orden.getVisitasRealizadas() >= orden.getMaximoVisitas()) {
            throw new IllegalStateException(
                "La orden agoto sus visitas."
            );
        }

        if (ordenes.contains(orden)
                || resultados.containsKey(orden.getCodigo())) {
            throw new IllegalStateException(
                "La orden ya pertenece a la jornada."
            );
        }

        if (!orden.esCompatibleCon(cuadrilla)) {
            throw new IllegalArgumentException(
                "La cuadrilla no cumple los requisitos del servicio."
            );
        }

        BigDecimal total = horasExactas().add(
            BigDecimal.valueOf(orden.getHorasEstimadas())
        );

        BigDecimal capacidad = BigDecimal.valueOf(
            cuadrilla.getCapacidadHoras()
        );

        if (total.compareTo(capacidad) > 0) {
            throw new IllegalArgumentException(
                "La asignacion supera la capacidad de la cuadrilla."
            );
        }

        orden.marcarAsignada();
        ordenes.add(orden);
    }

    void retirarOrden(OrdenServicio orden) {
        exigirEstado(EstadoJornada.En_preparacion);

        if (!ordenes.contains(orden)) {
            throw new IllegalArgumentException(
                "La orden no pertenece a esta jornada."
            );
        }

        orden.retirarAsignacion();
        ordenes.remove(orden);
    }

    void validarInicio() {
        exigirEstado(EstadoJornada.En_preparacion);

        if (ordenes.isEmpty()) {
            throw new IllegalStateException(
                "No puede iniciar una jornada vacia."
            );
        }

        for (OrdenServicio orden : ordenes) {
            if (orden.getEstado() != EstadoOrden.Asignada
                    || orden.getVisitasRealizadas()
                       >= orden.getMaximoVisitas()) {
                throw new IllegalStateException(
                    "Una orden no esta disponible para iniciar."
                );
            }
        }
    }

    void iniciar() {
        validarInicio();

        for (OrdenServicio orden : ordenes) {
            orden.iniciarVisita();
        }

        estado = EstadoJornada.En_curso;
    }

    void registrarResultado(String codigoOrden,
                            ResultadoVisita resultado) {
        exigirEstado(EstadoJornada.En_curso);

        if (resultado == null) {
            throw new IllegalArgumentException("Resultado invalido.");
        }

        String codigo = OrdenServicio.textoValido(
            codigoOrden, "Codigo de orden"
        );

        if (resultados.containsKey(codigo)) {
            throw new IllegalStateException(
                "Esta visita ya tiene resultado."
            );
        }

        OrdenServicio encontrada = null;

        for (OrdenServicio orden : ordenes) {
            if (orden.getCodigo().equals(codigo)) {
                encontrada = orden;
                break;
            }
        }

        if (encontrada == null) {
            throw new IllegalArgumentException(
                "La orden no pertenece a esta jornada."
            );
        }

        encontrada.registrarResultado(resultado);
        resultados.put(codigo, resultado);

        if (resultados.size() == ordenes.size()) {
            estado = EstadoJornada.Cerrada;
        }
    }

    private BigDecimal horasExactas() {
        BigDecimal suma = BigDecimal.ZERO;

        for (OrdenServicio orden : ordenes) {
            suma = suma.add(
                BigDecimal.valueOf(orden.getHorasEstimadas())
            );
        }

        return suma;
    }

    public double calcularHorasAsignadas() {
        return horasExactas().doubleValue();
    }

    public double calcularCapacidadDisponible() {
        return BigDecimal.valueOf(cuadrilla.getCapacidadHoras())
            .subtract(horasExactas())
            .doubleValue();
    }

    private void exigirEstado(EstadoJornada esperado) {
        if (estado != esperado) {
            throw new IllegalStateException(
                "Estado actual: " + estado
                + ". Estado requerido: " + esperado + "."
            );
        }
    }

    public int getId() {
        return id;
    }

    public Cuadrilla getCuadrilla() {
        return cuadrilla;
    }

    public EstadoJornada getEstado() {
        return estado;
    }

    public List<OrdenServicio> getOrdenes() {
        return Collections.unmodifiableList(
            new ArrayList<>(ordenes)
        );
    }
}