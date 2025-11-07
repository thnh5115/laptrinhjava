package ccm.cva.verification.application.service;

import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.application.query.VerificationRequestQuery;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VerificationService {

    VerificationRequest create(CreateVerificationRequestCommand command);

    Page<VerificationRequest> search(VerificationRequestQuery query, Pageable pageable);

    VerificationRequest get(UUID id);

    VerificationRequest approve(UUID id, ApproveVerificationRequestCommand command);

    VerificationRequest reject(UUID id, RejectVerificationRequestCommand command);
}
