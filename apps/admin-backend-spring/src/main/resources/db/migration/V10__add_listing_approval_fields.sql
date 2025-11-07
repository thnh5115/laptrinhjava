-- V10: Add approval tracking fields to listings table

-- Add new columns for approval tracking
ALTER TABLE listings
ADD COLUMN approved_by BIGINT COMMENT 'Admin who approved/rejected the listing',
ADD COLUMN approved_at TIMESTAMP NULL COMMENT 'When the listing was approved/rejected',
ADD COLUMN reject_reason TEXT COMMENT 'Reason for rejection or delisting';

-- Add foreign key for approved_by
ALTER TABLE listings
ADD CONSTRAINT fk_listing_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add index for approval queries
CREATE INDEX idx_listing_approved_by ON listings(approved_by);
CREATE INDEX idx_listing_approved_at ON listings(approved_at DESC);

-- Update existing APPROVED listings to have approval timestamp
UPDATE listings 
SET approved_at = updated_at, 
    approved_by = (SELECT id FROM users WHERE email = 'admin@test.local' LIMIT 1)
WHERE status = 'APPROVED' AND approved_at IS NULL;

-- Update existing REJECTED listings to have rejection info
UPDATE listings 
SET approved_at = updated_at,
    approved_by = (SELECT id FROM users WHERE email = 'admin@test.local' LIMIT 1),
    reject_reason = 'Legacy rejection - migrated from old system'
WHERE status = 'REJECTED' AND approved_at IS NULL;

-- Verify migration
-- Expected: All APPROVED/REJECTED listings should have approved_by and approved_at set
-- PENDING listings should have these fields as NULL
