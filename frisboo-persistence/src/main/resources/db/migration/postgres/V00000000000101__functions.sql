-- This function sets timestamps on insert and updates updated_at on meaningful changes.
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
CREATE OR REPLACE FUNCTION public.fcb_update_expired_at()
    RETURNS TRIGGER AS
$$
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

    NEW.expired_at = statement_timestamp() + INTERVAL '1 second' * NEW.ttl_in_second;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- This function derives verification_status from verified_at on every INSERT and UPDATE.
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
CREATE OR REPLACE FUNCTION public.fcb_validate_of_age(dob DATE)
    RETURNS BOOLEAN
    STABLE STRICT AS
$$
BEGIN
    RETURN dob <= (current_date - INTERVAL '18 years')::DATE;
END;
$$ language 'plpgsql';
