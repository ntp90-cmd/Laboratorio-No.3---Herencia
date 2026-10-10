public enum EstadoOrden {
    Pendiente,
    Asignada,
    En_atencion,
    Completada,
    Cancelada;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}
