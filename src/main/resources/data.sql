-- Drop the device_type check constraint if it exists (H2 specific)
-- This is needed because H2 creates check constraints for enum columns
-- and doesn't automatically update them when new enum values are added
ALTER TABLE IF EXISTS devices DROP CONSTRAINT IF EXISTS CONSTRAINT_8;

-- Also try common constraint naming patterns
ALTER TABLE IF EXISTS devices DROP CONSTRAINT IF EXISTS devices_device_type_check;
ALTER TABLE IF EXISTS devices DROP CONSTRAINT IF EXISTS chk_device_type;
