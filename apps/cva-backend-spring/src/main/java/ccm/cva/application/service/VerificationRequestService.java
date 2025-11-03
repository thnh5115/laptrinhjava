package ccm.cva.application.service;

import ccm.cva.application.service.dto.ApproveVerificationRequestCommand;
import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.application.service.dto.RejectVerificationRequestCommand;
import ccm.cva.application.service.dto.VerificationRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VerificationRequestService {

    VerificationRequestDto create(CreateVerificationRequestCommand command);

    Page<VerificationRequestDto> findAll(Pageable pageable);

    VerificationRequestDto findById(UUID id);

    VerificationRequestDto approve(UUID id, ApproveVerificationRequestCommand command);

    VerificationRequestDto reject(UUID id, RejectVerificationRequestCommand command);
}
