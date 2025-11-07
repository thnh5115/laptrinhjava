package ccm.cva.infrastructure.persistence.jpa;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * Legacy bridge kept to avoid breaking historical imports. The real repository bean lives under
 * {@code ccm.cva.verification.infrastructure.repository}. Marking this interface with
 * {@link NoRepositoryBean} prevents Spring Data from instantiating a duplicate bean while existing
 * callers can still compile against the old package.
 */
@NoRepositoryBean
public interface VerificationRequestRepository
        extends ccm.cva.verification.infrastructure.repository.VerificationRequestRepository {}
