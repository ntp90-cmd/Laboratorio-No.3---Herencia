import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Aplicacion {
    private final SistemaTecniHogar sistema;
    private final Scanner entrada;

    public Aplicacion() {
        sistema = new SistemaTecniHogar();
        entrada = new Scanner(System.in, "UTF-8");
    }

    public static void main(String[] args) {
        new Aplicacion().ejecutar();
    }

    public void ejecutar() {
        System.out.println("Bienvenido a Tecnihogar.");
        System.out.println(
            "Datos de ejemplo cargados sin ingresos."
        );

        boolean continuar = true;

        while (continuar) {
            try {
                mostrarMenu();
                int opcion = leerEntero("Opcion: ");

                if (opcion == 0) {
                    continuar = false;
                } else {
                    procesarOpcion(opcion);
                }

            } catch (IllegalArgumentException
                    | IllegalStateException e) {
                System.out.println(
                    "Operacion rechazada: " + e.getMessage()
                );

            } catch (NoSuchElementException e) {
                System.out.println("Fin de la entrada.");
                continuar = false;
            }
        }

        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println("\nTecnihogar");
        System.out.println("1. Cotizar servicio");
        System.out.println("2. Registrar orden");
        System.out.println("3. Registrar cuadrilla");
        System.out.println("4. Crear jornada");
        System.out.println("5. Asignar orden");
        System.out.println("6. Retirar orden");
        System.out.println("7. Iniciar jornada");
        System.out.println("8. Registrar resultado de visita");
        System.out.println("9. Consultar ordenes");
        System.out.println("10. Consultar cuadrillas");
        System.out.println("11. Reporte de ordenes");
        System.out.println("12. Reporte de ingresos");
        System.out.println("13. Reporte de jornadas");
        System.out.println("0. Salir");
    }

    private void procesarOpcion(int opcion) {
        switch (opcion) {
            case 1: {
                OrdenServicio cotizacion = leerServicio(false);
                mostrarCotizacion(cotizacion);

                System.out.println(
                    "Cotizacion realizada. No se registro ni cobro."
                );
                break;
            }

            case 2: {
                OrdenServicio orden = leerServicio(true);
                mostrarCotizacion(orden);

                if (leerSiNo("Aceptar y registrar el cobro (s/n): ")) {
                    sistema.registrarOrden(orden);

                    System.out.println(
                        "Orden registrada y cobrada. Estado: Pendiente."
                    );
                } else {
                    System.out.println(
                        "Registro cancelado. Sin cambios ni cobros."
                    );
                }
                break;
            }

            case 3: {
                String codigo = leerTexto("Codigo de cuadrilla: ");
                double horas = leerDecimal("Capacidad de horas: ");

                boolean certificada = leerSiNo(
                    "Certificada en refrigeracion (s/n): "
                );

                sistema.registrarCuadrilla(
                    new Cuadrilla(codigo, horas, certificada)
                );

                System.out.println("Cuadrilla registrada.");
                break;
            }

            case 4: {
                Jornada jornada = sistema.crearJornada(
                    leerTexto("Codigo de cuadrilla: ")
                );

                System.out.println(
                    "Jornada creada: " + jornada.getId()
                );
                break;
            }

            case 5:
                sistema.asignarOrden(
                    leerEntero("Numero de jornada: "),
                    leerTexto("Codigo de orden: ")
                );

                System.out.println(
                    "Orden asignada, sin cobros adicionales."
                );
                break;

            case 6:
                sistema.retirarOrden(
                    leerEntero("Numero de jornada: "),
                    leerTexto("Codigo de orden: ")
                );

                System.out.println(
                    "Orden retirada; vuelve a pendiente."
                );
                break;

            case 7:
                sistema.iniciarJornada(
                    leerEntero("Numero de jornada: ")
                );

                System.out.println(
                    "Jornada iniciada. Se registraron los cargos "
                    + "adicionales que correspondan."
                );
                break;

            case 8: {
                int id = leerEntero("Numero de jornada: ");
                String codigo = leerTexto("Codigo de orden: ");

                int resultado = leerEntero(
                    "Resultado (1 completada / 2 fallida): "
                );

                if (resultado != 1 && resultado != 2) {
                    throw new IllegalArgumentException(
                        "El resultado debe ser 1 o 2."
                    );
                }

                ResultadoVisita valor = resultado == 1
                    ? ResultadoVisita.Completada
                    : ResultadoVisita.Fallida;

                sistema.registrarResultado(id, codigo, valor);

                System.out.println(
                    "Resultado registrado. La jornada cierra "
                    + "cuando todas sus ordenes tienen resultado."
                );
                break;
            }

            case 9:
                for (OrdenServicio orden : sistema.consultarOrdenes()) {
                    System.out.printf(
                        Locale.US,
                        "%s | %s | Cliente: %s | Direccion: %s"
                            + " | %s | %.2f h | %s | Visitas: %d/%d"
                            + " | Precio: Q%.2f%n",
                        orden.getCodigo(),
                        orden.getCategoria(),
                        orden.getCliente(),
                        orden.getDireccion(),
                        orden.getZona(),
                        orden.getHorasEstimadas(),
                        orden.getEstado(),
                        orden.getVisitasRealizadas(),
                        orden.getMaximoVisitas(),
                        orden.calcularCosto()
                    );
                }
                break;

            case 10:
                for (Cuadrilla cuadrilla
                        : sistema.consultarCuadrillas()) {
                    System.out.printf(
                        Locale.US,
                        "%s | Capacidad: %.2f h | Refrigeracion: %s%n",
                        cuadrilla.getCodigo(),
                        cuadrilla.getCapacidadHoras(),
                        cuadrilla.isCertificadaRefrigeracion()
                            ? "Si" : "No"
                    );
                }
                break;

            case 11:
                System.out.print(sistema.generarReporteOrdenes());
                break;

            case 12:
                System.out.print(sistema.generarReporteIngresos());
                break;

            case 13:
                System.out.print(sistema.generarReporteJornadas());
                break;

            default:
                throw new IllegalArgumentException(
                    "Seleccione una opcion del 0 al 13."
                );
        }
    }

    private OrdenServicio leerServicio(boolean registro) {
        System.out.println("1. Diagnostico");
        System.out.println("2. Instalacion");
        System.out.println("3. Instalacion electrica certificada");
        System.out.println("4. Mantenimiento de aire acondicionado");

        int categoria = leerEntero("Categoria: ");

        if (categoria < 1 || categoria > 4) {
            throw new IllegalArgumentException("Categoria invalida.");
        }

        String codigo = registro
            ? leerTexto("Codigo unico: ") : "Cotizacion";

        String cliente = registro
            ? leerTexto("Nombre del cliente: ") : "Consulta";

        String direccion = registro
            ? leerTexto("Direccion: ") : "Sin registrar";

        double horas = leerDecimal("Horas estimadas: ");

        int opcionZona = leerEntero(
            "Zona (1 metropolitana / 2 periferica): "
        );

        if (opcionZona != 1 && opcionZona != 2) {
            throw new IllegalArgumentException("Zona invalida.");
        }

        Zona zona = opcionZona == 1
            ? Zona.Metropolitana : Zona.Periferica;

        switch (categoria) {
            case 1:
                return new Diagnostico(
                    codigo, cliente, direccion, horas, zona
                );

            case 2:
                return new Instalacion(
                    codigo, cliente, direccion, horas, zona,
                    leerEntero("Cantidad de puntos: ")
                );

            case 3:
                return new InstalacionElectricaCertificada(
                    codigo, cliente, direccion, horas, zona,
                    leerEntero("Cantidad de puntos: "),
                    leerDecimal("Valor de materiales (Q): ")
                );

            case 4:
                return new MantenimientoAireAcondicionado(
                    codigo, cliente, direccion, horas, zona,
                    leerEntero("Capacidad en btu: ")
                );

            default:
                throw new IllegalArgumentException(
                    "Categoria invalida."
                );
        }
    }

    private void mostrarCotizacion(OrdenServicio orden) {
        System.out.printf(
            Locale.US,
            "Costo: Q%.2f | Atencion estimada: %d dias habiles%n",
            orden.calcularCosto(),
            orden.calcularDiasAtencion()
        );
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);

            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println(
                    "Ingrese un numero entero valido."
                );
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);

            try {
                double valor = Double.parseDouble(
                    texto.replace(',', '.')
                );

                if (Double.isFinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Formato numerico incorrecto.");
            }

            System.out.println(
                "Ingrese un decimal finito, sin separadores de miles."
            );
        }
    }

    private String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();

            if (!texto.isEmpty()) {
                return texto;
            }

            System.out.println("El dato no puede estar vacio.");
        }
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            String valor = leerTexto(mensaje);

            if (valor.equalsIgnoreCase("s")) {
                return true;
            }

            if (valor.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("Responda s o n.");
        }
    }
}