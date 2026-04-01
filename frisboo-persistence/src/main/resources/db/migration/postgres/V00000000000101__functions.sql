-- This function sets timestamps on insert and updates updated_at on meaningful changes.
-- Ownership: database-managed. Do not set created_at or updated_at from application code.
CREATE OR REPLACE FUNCTION public.fcb_handle_timestamps()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.created_at = statement_timestamp();
        NEW.updated_at = statement_timestamp();
        RETURN NEW;
    END IF;

    IF TG_OP != 'UPDATE' THEN
        RETURN NEW;
    END IF;

    IF OLD IS NOT DISTINCT FROM NEW THEN
        RETURN OLD;
    END IF;

    NEW.created_at = OLD.created_at;
    NEW.updated_at = statement_timestamp();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- This function sets expired_at based on ttl_in_second on INSERT.
-- Validates that ttl_in_second is non-null, positive, and bounded (max 30 days = 2592000 seconds).
CREATE OR REPLACE FUNCTION public.fcb_update_expired_at()
    RETURNS TRIGGER AS
$$
DECLARE
    max_ttl_seconds CONSTANT INTEGER := 2592000;
BEGIN
    IF TG_OP != 'INSERT' THEN
        RETURN NEW;
    END IF;

    IF NEW.ttl_in_second IS NULL THEN
        RAISE EXCEPTION 'ttl_in_second must not be null';
    END IF;

    IF NEW.ttl_in_second <= 0 THEN
        RAISE EXCEPTION 'ttl_in_second must be positive, got %', NEW.ttl_in_second;
    END IF;

    IF NEW.ttl_in_second > max_ttl_seconds THEN
        RAISE EXCEPTION 'ttl_in_second exceeds maximum of % seconds, got %', max_ttl_seconds, NEW.ttl_in_second;
    END IF;

    NEW.expired_at = statement_timestamp() + INTERVAL '1 second' * NEW.ttl_in_second;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- This function derives verification_status from verified_at on every INSERT and UPDATE.
-- The incoming verification_status value is always overwritten to enforce the invariant.
CREATE OR REPLACE FUNCTION public.fcb_update_verification_status()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.verified_at IS NOT NULL THEN
        NEW.verification_status = 'VERIFIED';
    ELSIF TG_OP = 'UPDATE' AND OLD.verified_at IS NOT NULL THEN
        NEW.verification_status = 'REVOKED';
    ELSE
        NEW.verification_status = 'PENDING';
    END IF;

    RETURN NEW;
END;
$$ language 'plpgsql';

-- Determines if dob is at least 18 years before current_date.
-- STRICT: returns NULL on NULL input (Postgres handles this automatically).
CREATE OR REPLACE FUNCTION public.fcb_validate_of_age(dob DATE)
    RETURNS BOOLEAN
    STABLE STRICT AS
$$
BEGIN
    RETURN dob <= (current_date - INTERVAL '18 years')::DATE;
END;
$$ language 'plpgsql';
