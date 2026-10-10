public enum ConceptoIngreso {
    Registro,
    Visita_adicional;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}