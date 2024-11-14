package dk.martinersej.plugin.database;

public enum Constraint {
    NOT_NULL("NOT NULL"),
    UNIQUE("UNIQUE"),
    DEFAULT("DEFAULT"),
    CHECK("CHECK"),
    AUTOINCREMENT("AUTOINCREMENT"),
    ON_DELETE("ON DELETE"),
    ON_UPDATE("ON UPDATE"),
    PRIMARY_KEY("PRIMARY KEY"),
    FOREIGN_KEY("FOREIGN KEY"),
    REFERENCES("REFERENCES");

    private final String constraint;

    Constraint(String constraint) {
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        return constraint;
    }
}

