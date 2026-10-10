public enum EstadoJornada {
    En_preparacion,
    En_curso,
    Cerrada;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}