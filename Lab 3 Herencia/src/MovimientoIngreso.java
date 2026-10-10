import java.math.BigDecimal;
import java.math.RoundingMode;

public class MovimientoIngreso {
    private final OrdenServicio orden;
    private final ConceptoIngreso concepto;
    private final double monto;

    public MovimientoIngreso(OrdenServicio orden,
                             ConceptoIngreso concepto, double monto) {
        if (orden == null || concepto == null) {
            throw new IllegalArgumentException("Cobro incompleto.");
        }

        OrdenServicio.positivo(monto, "Monto");

        this.orden = orden;
        this.concepto = concepto;
        this.monto = redondear(monto);
    }

    static double redondear(double monto) {
        if (!Double.isFinite(monto) || monto < 0) {
            throw new IllegalArgumentException(
                "El monto calculado esta fuera de rango."
            );
        }

        return BigDecimal.valueOf(monto)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    public OrdenServicio getOrden() {
        return orden;
    }

    public ConceptoIngreso getConcepto() {
        return concepto;
    }

    public double getMonto() {
        return monto;
    }
}