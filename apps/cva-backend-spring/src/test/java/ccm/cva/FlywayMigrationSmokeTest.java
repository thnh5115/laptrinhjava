package ccm.cva;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

class FlywayMigrationSmokeTest {

    @Test
    void allMigrationsApplyAgainstH2InMysqlMode() {
        Flyway flyway = Flyway.configure()
            .dataSource("jdbc:h2:mem:flyway_smoke;MODE=MySQL;DATABASE_TO_UPPER=false", "sa", "sa")
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
        try {
            flyway.migrate();
        } catch (FlywayException ex) {
            ex.printStackTrace(System.err);
            throw ex;
        }
    }
}
