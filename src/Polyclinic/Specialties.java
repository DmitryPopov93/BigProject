package Polyclinic;

public enum  Specialties{ROENTGENOLOGIST("Roentgenologist"), TRAUMATOLOGIST("Traumatologist"),
    OCULIST("Oculist"), OTOLARYNGOLOGIST("Otolaryngologist"), SURGEON("Surgeon");
    private String nameSpecialties;
    Specialties(String nameSpecialties) {
        this.nameSpecialties = nameSpecialties;
    }
    @Override
    public String toString() {
        return nameSpecialties;
    }
}
