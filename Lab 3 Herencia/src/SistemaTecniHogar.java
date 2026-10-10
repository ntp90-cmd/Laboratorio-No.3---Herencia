import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SistemaTecniHogar {
    private final Map<String, OrdenServicio> ordenes;
    private final Map<String, Cuadrilla> cuadrillas;
    private final Map<Integer, Jornada> jornadas;
    private final List<MovimientoIngreso> ingresos;
    private int siguienteIdJornada;

    public SistemaTecniHogar() {
        ordenes = new LinkedHashMap<>();
        cuadrillas = new LinkedHashMap<>();
        jornadas = new LinkedHashMap<>();
        ingresos = new ArrayList<>();
        siguienteIdJornada = 1;

        cargarDatosIniciales();
    }

    public void registrarOrden(OrdenServicio orden) {
        if (orden == null) {
            throw new IllegalArgumentException("Orden invalida.");
        }

        if (ordenes.containsKey(orden.getCodigo())) {
            throw new IllegalArgumentException(
                "El codigo de orden ya existe."
            );
        }

        if (orden.getEstado() != EstadoOrden.Pendiente
                || orden.getVisitasRealizadas() != 0) {
            throw new IllegalArgumentException(
                "Solo se registran ordenes nuevas."
            );
        }

        MovimientoIngreso cobro = new MovimientoIngreso(
            orden, ConceptoIngreso.Registro, orden.calcularCosto()
        );

        ordenes.put(orden.getCodigo(), orden);
        ingresos.add(cobro);
    }

    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("Cuadrilla invalida.");
        }

        if (cuadrillas.containsKey(cuadrilla.getCodigo())) {
            throw new IllegalArgumentException(
                "El codigo de cuadrilla ya existe."
            );
        }

        cuadrillas.put(cuadrilla.getCodigo(), cuadrilla);
    }

    public Jornada crearJornada(String codigoCuadrilla) {
        String codigo = OrdenServicio.textoValido(
            codigoCuadrilla, "Codigo de cuadrilla"
        );

        Cuadrilla cuadrilla = cuadrillas.get(codigo);

        if (cuadrilla == null) {
            throw new IllegalArgumentException(
                "No existe esa cuadrilla."
            );
        }

        for (Jornada jornada : jornadas.values()) {
            if (jornada.getCuadrilla() == cuadrilla
                    && jornada.getEstado() != EstadoJornada.Cerrada) {
                throw new IllegalStateException(
                    "La cuadrilla ya tiene una jornada sin cerrar."
                );
            }
        }

        Jornada jornada = new Jornada(siguienteIdJornada, cuadrilla);

        jornadas.put(siguienteIdJornada, jornada);
        siguienteIdJornada++;

        return jornada;
    }

    public void asignarOrden(int idJornada, String codigoOrden) {
        buscarJornada(idJornada).asignarOrden(
            buscarOrden(codigoOrden)
        );
    }

    public void retirarOrden(int idJornada, String codigoOrden) {
        buscarJornada(idJornada).retirarOrden(
            buscarOrden(codigoOrden)
        );
    }

    public void iniciarJornada(int idJornada) {
        Jornada jornada = buscarJornada(idJornada);
        jornada.validarInicio();

        List<MovimientoIngreso> cargos = new ArrayList<>();

        for (OrdenServicio orden : jornada.getOrdenes()) {
            if (orden.getVisitasRealizadas() >= 1) {
                cargos.add(new MovimientoIngreso(
                    orden, ConceptoIngreso.Visita_adicional, 40
                ));
            }
        }

        jornada.iniciar();
        ingresos.addAll(cargos);
    }

    public void registrarResultado(int idJornada, String codigoOrden,
                                   ResultadoVisita resultado) {
        buscarJornada(idJornada).registrarResultado(
            codigoOrden, resultado
        );
    }

    private OrdenServicio buscarOrden(String codigo) {
        String clave = OrdenServicio.textoValido(
            codigo, "Codigo de orden"
        );

        OrdenServicio orden = ordenes.get(clave);

        if (orden == null) {
            throw new IllegalArgumentException("No existe esa orden.");
        }

        return orden;
    }

    private Jornada buscarJornada(int id) {
        Jornada jornada = jornadas.get(id);

        if (jornada == null) {
            throw new IllegalArgumentException(
                "No existe esa jornada."
            );
        }

        return jornada;
    }

    public List<OrdenServicio> consultarOrdenes() {
        return new ArrayList<>(ordenes.values());
    }

    public List<Cuadrilla> consultarCuadrillas() {
        return new ArrayList<>(cuadrillas.values());
    }

    public String generarReporteOrdenes() {
        Map<String, EnumMap<EstadoOrden, Integer>> cantidades =
            new LinkedHashMap<>();

        for (OrdenServicio orden : ordenes.values()) {
            EnumMap<EstadoOrden, Integer> fila =
                cantidades.computeIfAbsent(
                    orden.getCategoria(),
                    clave -> new EnumMap<>(EstadoOrden.class)
                );

            fila.merge(orden.getEstado(), 1, Integer::sum);
        }

        StringBuilder texto = new StringBuilder(
            "Ordenes por categoria y estado\n"
        );

        for (Map.Entry<String, EnumMap<EstadoOrden, Integer>> fila
                : cantidades.entrySet()) {

            texto.append(fila.getKey()).append(":\n");

            for (EstadoOrden estado : EstadoOrden.values()) {
                texto.append("  ")
                    .append(estado)
                    .append(": ")
                    .append(fila.getValue().getOrDefault(estado, 0))
                    .append('\n');
            }
        }

        texto.append("Total de ordenes: ")
            .append(ordenes.size())
            .append('\n');

        return texto.toString();
    }

    public String generarReporteIngresos() {
        BigDecimal registro = BigDecimal.ZERO;
        BigDecimal visitas = BigDecimal.ZERO;

        Map<String, BigDecimal> categorias = new LinkedHashMap<>();

        for (OrdenServicio orden : ordenes.values()) {
            categorias.putIfAbsent(
                orden.getCategoria(), BigDecimal.ZERO
            );
        }

        for (MovimientoIngreso ingreso : ingresos) {
            BigDecimal monto = BigDecimal.valueOf(
                ingreso.getMonto()
            );

            if (ingreso.getConcepto() == ConceptoIngreso.Registro) {
                registro = registro.add(monto);
            } else {
                visitas = visitas.add(monto);
            }

            categorias.merge(
                ingreso.getOrden().getCategoria(),
                monto,
                BigDecimal::add
            );
        }

        StringBuilder texto = new StringBuilder("Ingresos\n");

        texto.append(String.format(
            Locale.US,
            "Por registro: Q%.2f%n"
                + "Por visitas adicionales: Q%.2f%n"
                + "Total: Q%.2f%n",
            registro, visitas, registro.add(visitas)
        ));

        for (Map.Entry<String, BigDecimal> fila
                : categorias.entrySet()) {

            texto.append(String.format(
                Locale.US, "%s: Q%.2f%n",
                fila.getKey(), fila.getValue()
            ));
        }

        return texto.toString();
    }

    public String generarReporteJornadas() {
        StringBuilder texto = new StringBuilder(
            "Jornadas sin cerrar\n"
        );

        boolean hay = false;

        for (Jornada jornada : jornadas.values()) {
            if (jornada.getEstado() == EstadoJornada.Cerrada) {
                continue;
            }

            hay = true;

            texto.append(String.format(
                Locale.US,
                "Jornada %d | Cuadrilla %s | %s"
                    + " | Horas: %.2f | Disponibles: %.2f%n",
                jornada.getId(),
                jornada.getCuadrilla().getCodigo(),
                jornada.getEstado(),
                jornada.calcularHorasAsignadas(),
                jornada.calcularCapacidadDisponible()
            ));

            for (OrdenServicio orden : jornada.getOrdenes()) {
                texto.append("  Orden: ")
                    .append(orden.getCodigo())
                    .append('\n');
            }
        }

        if (!hay) {
            texto.append("No hay jornadas abiertas.\n");
        }

        return texto.toString();
    }

    private void cargarDatosIniciales() {
        OrdenServicio[] ejemplos = {
            new Diagnostico(
                "D01", "Ana", "Zona 1", 1, Zona.Metropolitana
            ),
            new Diagnostico(
                "D02", "Luis", "Villa Nueva", 1.5, Zona.Periferica
            ),
            new Instalacion(
                "I01", "Marta", "Zona 5",
                2.5, Zona.Metropolitana, 5
            ),
            new Instalacion(
                "I02", "Pedro", "Mixco",
                2.2, Zona.Periferica, 2
            ),
            new InstalacionElectricaCertificada(
                "E01", "Sofia", "Zona 10",
                2.5, Zona.Metropolitana, 5, 2000
            ),
            new InstalacionElectricaCertificada(
                "E02", "Carlos", "San Miguel Petapa",
                3.2, Zona.Periferica, 2, 1000
            ),
            new MantenimientoAireAcondicionado(
                "A01", "Elena", "Zona 12",
                1.5, Zona.Metropolitana, 9000
            ),
            new MantenimientoAireAcondicionado(
                "A02", "Mario", "Zona 14",
                2, Zona.Metropolitana, 60000
            )
        };

        for (OrdenServicio orden : ejemplos) {
            ordenes.put(orden.getCodigo(), orden);
        }

        registrarCuadrilla(new Cuadrilla("C01", 8, true));
        registrarCuadrilla(new Cuadrilla("C02", 6, false));
        registrarCuadrilla(new Cuadrilla("C03", 4, false));
    }
}