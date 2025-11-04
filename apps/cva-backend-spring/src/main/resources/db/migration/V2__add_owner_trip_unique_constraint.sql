ALTER TABLE verification_requests
    ADD CONSTRAINT uk_verification_requests_owner_trip UNIQUE (owner_id, trip_id);
