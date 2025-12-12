-- This function updates the `updated_at` column of a row to the current timestamp
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

-- This function increments the `version` column of a row by 1.
CREATE OR REPLACE FUNCTION public.fcb_update_version_column()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.version = 1;
        RETURN NEW;
    END IF;

    IF TG_OP != 'UPDATE' THEN
        RETURN NEW;
    END IF;

    IF (OLD IS NOT DISTINCT FROM NEW) THEN
        RETURN OLD;
    END IF;

    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- This function sets the `expired_at` column based on the `ttl_in_second` value.
CREATE OR REPLACE FUNCTION public.fcb_update_expired_at()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP != 'INSERT' THEN
        RETURN NEW;
    END IF;

    NEW.expired_at = statement_timestamp() + INTERVAL '1 second' * NEW.ttl_in_second;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- This function updates the `verification_status` column based on the `verified_at` value.
CREATE OR REPLACE FUNCTION public.fcb_update_verification_status()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.verified_at IS NOT DISTINCT FROM OLD.verified_at THEN
        RETURN NEW;
    END IF;

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
    IF dob IS NULL THEN
        RETURN false;
    END IF;

    RETURN dob <= (current_date - INTERVAL '18 years')::DATE;
END;
$$ language 'plpgsql';
